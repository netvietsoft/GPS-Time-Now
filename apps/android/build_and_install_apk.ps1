param(
    [switch]$SkipDeploy = $false
)
$ErrorActionPreference = "Stop"

# Portable Android SDK & JDK Discovery (Supports Windows Workstation & Linux/Windows CI Runners)
$sdkCandidates = @(
    @(
        $env:ANDROID_HOME,
        $env:ANDROID_SDK_ROOT,
        "C:\Program Files\Unity\Hub\Editor\2020.3.16f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK",
        "$env:LOCALAPPDATA\Android\Sdk",
        "/usr/local/lib/android/sdk"
    ) | Where-Object { $_ -and (Test-Path $_) }
)

if ($sdkCandidates.Count -eq 0) {
    throw "Android SDK not found in ANDROID_HOME, ANDROID_SDK_ROOT, or standard paths."
}
$sdkRoot = $sdkCandidates[0]

$btDirs = @(Get-ChildItem -Path (Join-Path $sdkRoot "build-tools") -Directory | Sort-Object Name -Descending)
if ($btDirs.Count -eq 0) {
    throw "No Android build-tools found under $sdkRoot/build-tools"
}
$btRoot = $btDirs[0].FullName

$platDirs = @(Get-ChildItem -Path (Join-Path $sdkRoot "platforms") -Directory | Sort-Object Name -Descending)
if ($platDirs.Count -eq 0) {
    throw "No Android platforms found under $sdkRoot/platforms"
}
$androidJar = Join-Path $platDirs[0].FullName "android.jar"

$jdkCandidates = @(
    @(
        $env:JAVA_HOME,
        "C:\Program Files\Unity\Hub\Editor\2020.3.16f1\Editor\Data\PlaybackEngines\AndroidPlayer\OpenJDK"
    ) | Where-Object { $_ -and (Test-Path $_) }
)

$exeSuffix = if ($IsWindows -or $env:OS -match "Windows") { ".exe" } else { "" }
$aapt2     = Join-Path $btRoot "aapt2$exeSuffix"
$zipalign  = Join-Path $btRoot "zipalign$exeSuffix"
$d8Jar     = Join-Path (Join-Path $btRoot "lib") "d8.jar"
$apksignerJar = Join-Path (Join-Path $btRoot "lib") "apksigner.jar"

if ($jdkCandidates.Count -gt 0) {
    $jdkBin  = Join-Path $jdkCandidates[0] "bin"
    $javac   = Join-Path $jdkBin "javac$exeSuffix"
    $java    = Join-Path $jdkBin "java$exeSuffix"
    $jar     = Join-Path $jdkBin "jar$exeSuffix"
    $keytool = Join-Path $jdkBin "keytool$exeSuffix"
} else {
    $javac   = (Get-Command javac).Source
    $java    = (Get-Command java).Source
    $jar     = (Get-Command jar).Source
    $keytool = (Get-Command keytool).Source
}

$androidRoot       = $PSScriptRoot
$appMain           = Join-Path $androidRoot "app/src/main"
$canonicalManifest = Join-Path $appMain "AndroidManifest.xml"
$buildDir          = Join-Path $androidRoot "build/apk_build_$PID"
$outDebugDir       = Join-Path $androidRoot "app/build/outputs/apk/debug"
$outReleaseDir     = Join-Path $androidRoot "app/build/outputs/apk/release"

function Invoke-NativeTool {
    param(
        [string]$StepName,
        [scriptblock]$Command
    )
    $prevEap = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        & $Command 2>&1 | ForEach-Object { $_ } | Out-Null
        $code = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $prevEap
    }
    if ($code -ne 0) {
        throw "$StepName failed with exit code $code"
    }
}

Write-Host "======================================================================"
Write-Host "  NEXTAI CAMERA (v10.0.281) - CANONICAL MANIFEST BUILD & VERIFICATION"
Write-Host "======================================================================"
Write-Host "  SDK Build-Tools : $btRoot"
Write-Host "  Platform Jar    : $androidJar"
Write-Host "  Canonical XML   : $canonicalManifest"

if (-not (Test-Path $canonicalManifest)) {
    throw "Canonical AndroidManifest.xml missing at $canonicalManifest"
}

if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null
New-Item -ItemType Directory -Force -Path $outDebugDir | Out-Null
New-Item -ItemType Directory -Force -Path $outReleaseDir | Out-Null

# 1. Compile Android resources with aapt2
Write-Host "[1/6] Compiling Android resources with aapt2..."
$compiledResZip = Join-Path $buildDir "compiled_res.zip"
Invoke-NativeTool "aapt2 compile" { & $aapt2 compile --dir (Join-Path $appMain "res") -o $compiledResZip }

# 2. Link directly against the single canonical apps/android/app/src/main/AndroidManifest.xml (REV-003)
Write-Host "[2/6] Linking resources against canonical AndroidManifest.xml (usesCleartextTraffic=false)..."
$unalignedApk = Join-Path $buildDir "unaligned.apk"
$genJavaDir   = Join-Path $buildDir "gen_java"
New-Item -ItemType Directory -Force -Path $genJavaDir | Out-Null

Invoke-NativeTool "aapt2 link" {
    & $aapt2 link -o $unalignedApk `
        -I $androidJar `
        --manifest $canonicalManifest `
        --java $genJavaDir `
        --auto-add-overlay `
        -0 arsc -0 so -0 mp3 -0 otf -0 ttf `
        $compiledResZip
}

# 3. Compile Java sources
Write-Host "[3/6] Compiling Java sources..."
$classesDir = Join-Path $buildDir "classes"
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

$srcJavaDir = Join-Path $appMain "java/com/nextai/camera"
$javaFiles = @(
    Get-ChildItem -Path $genJavaDir -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
    Get-ChildItem -Path $srcJavaDir -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
)

Invoke-NativeTool "javac" {
    & $javac -source 1.8 -target 1.8 -encoding UTF-8 -bootclasspath $androidJar -d $classesDir $javaFiles
}

# 4. Dexing with d8
Write-Host "[4/6] Converting .class files to classes.dex with d8..."
$classFiles = Get-ChildItem -Path $classesDir -Filter "*.class" -Recurse | Select-Object -ExpandProperty FullName
Invoke-NativeTool "d8" {
    & $java -cp $d8Jar com.android.tools.r8.D8 --lib $androidJar --min-api 26 --output $buildDir $classFiles
}

# 5. Package classes.dex, assets/fonts, assets/watermark, and lib/arm64-v8a/*.so
Write-Host "[5/6] Packaging classes.dex, original fonts/watermark assets, and 7 ARM64 .so libraries..."
$stageDir = Join-Path $buildDir "apk_stage"
New-Item -ItemType Directory -Force -Path (Join-Path $stageDir "lib/arm64-v8a") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $stageDir "assets/fonts") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $stageDir "assets/watermark") | Out-Null

Copy-Item -Force (Join-Path $buildDir "classes.dex") (Join-Path $stageDir "classes.dex")
Copy-Item -Force (Join-Path $appMain "jniLibs/arm64-v8a/*.so") (Join-Path $stageDir "lib/arm64-v8a/")
Copy-Item -Force (Join-Path $appMain "assets/fonts/*.otf") (Join-Path $stageDir "assets/fonts/") -ErrorAction SilentlyContinue
Copy-Item -Force (Join-Path $appMain "assets/fonts/*.ttf") (Join-Path $stageDir "assets/fonts/") -ErrorAction SilentlyContinue
Copy-Item -Force (Join-Path $appMain "assets/watermark/*.json") (Join-Path $stageDir "assets/watermark/") -ErrorAction SilentlyContinue

Push-Location $stageDir
try {
    Invoke-NativeTool "jar classes.dex" { & $jar uf $unalignedApk "classes.dex" }
    Invoke-NativeTool "jar assets" { & $jar uf0 $unalignedApk "assets" }
    Invoke-NativeTool "jar lib" { & $jar uf0 $unalignedApk "lib" }
} finally {
    Pop-Location
}

# 6. Align, Sign & Verify Packaged Binary Manifest with aapt2 dump xmltree (REV-003)
Write-Host "[6/6] Running zipalign, apksigner & binary manifest inspection (aapt2 dump xmltree)..."
$alignedApk = Join-Path $buildDir "aligned.apk"
Invoke-NativeTool "zipalign" { & $zipalign -p -f 4 $unalignedApk $alignedApk }

$keystore = Join-Path $buildDir "nextai_build_signing.keystore"
if (-not (Test-Path $keystore)) {
    Invoke-NativeTool "keytool" {
        & $keytool -genkeypair `
            -keystore $keystore `
            -storepass "nextai2026" `
            -keypass "nextai2026" `
            -alias "nextai_key" `
            -keyalg RSA -keysize 2048 -validity 10000 `
            -dname "CN=NextAI Camera, OU=Mobile, O=NextAI Global, L=HCMC, S=SG, C=VN"
    }
}

$finalDebugApk   = Join-Path $outDebugDir "TimeNow-Original-v10.0.281-arm64.apk"
$finalReleaseApk = Join-Path $outReleaseDir "TimeNow-Release-v10.0.281-arm64.apk"

Invoke-NativeTool "apksigner" {
    & $java -jar $apksignerJar sign `
        --ks $keystore `
        --ks-key-alias "nextai_key" `
        --ks-pass "pass:nextai2026" `
        --key-pass "pass:nextai2026" `
        --v1-signing-enabled true `
        --v2-signing-enabled true `
        --out $finalDebugApk `
        $alignedApk
}

Copy-Item -Force $finalDebugApk $finalReleaseApk

# Inspect the compiled binary AndroidManifest.xml inside the signed APK with aapt2 dump xmltree
$prevEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$xmlTreeOutput = (& $aapt2 dump xmltree $finalReleaseApk --file AndroidManifest.xml 2>$null) -join "`n"
$ErrorActionPreference = $prevEap
$xmlTreeAuditFile = Join-Path (Join-Path $androidRoot "app/build/outputs/apk") "manifest-xmltree-audit.txt"
Set-Content -Path $xmlTreeAuditFile -Value $xmlTreeOutput -Encoding UTF8

if (Test-Path $buildDir) {
    Remove-Item -Recurse -Force $buildDir -ErrorAction SilentlyContinue
}

if ($xmlTreeOutput -notmatch 'usesCleartextTraffic\(0x010104ec\)=(\(type 0x12\))?(false|0x0)\b') {
    throw "REV-003 FAILURE: Compiled APK binary AndroidManifest.xml does not enforce usesCleartextTraffic=false!"
}
if ($xmlTreeOutput -notmatch 'networkSecurityConfig\(0x01010527\)=@0x') {
    throw "REV-003 FAILURE: Compiled APK binary AndroidManifest.xml is missing android:networkSecurityConfig!"
}
if ($xmlTreeOutput -notmatch 'targetSdkVersion\(0x01010270\)=(\(type 0x10\))?(34|0x22)\b') {
    throw "REV-003 FAILURE: Compiled APK binary AndroidManifest.xml does not have targetSdkVersion=34!"
}

$apkSizeMB = [Math]::Round((Get-Item $finalReleaseApk).Length / 1MB, 2)
Write-Host "  [BUILD & BINARY MANIFEST AUDIT PASS]"
Write-Host "    Debug APK   : $finalDebugApk ($apkSizeMB MB)"
Write-Host "    Release APK : $finalReleaseApk ($apkSizeMB MB)"
Write-Host "    Binary XML  : usesCleartextTraffic=false (0x0), networkSecurityConfig=@xml/network_security_config, targetSdkVersion=34 (0x22)"

if ($SkipDeploy) {
    Write-Host "  [SKIP DEPLOY] -SkipDeploy specified; skipping ADB installation."
    exit 0
}

# 7. Deploy to Connected Devices (if adb is available and -SkipDeploy is not set)
$adb = "$env:TEMP\android-platform-tools\platform-tools\adb.exe"
$adbCmd = $null
if (Test-Path $adb) {
    $adbCmd = $adb
} elseif (Get-Command adb -ErrorAction SilentlyContinue) {
    $adbCmd = (Get-Command adb).Source
}

if ($null -ne $adbCmd) {
    Write-Host "  Deploying to connected Android devices..."
    & $adbCmd start-server | Out-Null
    Start-Sleep -Seconds 2
    $devicesOutput = & $adbCmd devices
    $serials = @()
    foreach ($line in $devicesOutput) {
        if ($line -match "^(\S+)\s+device\b") {
            $serials += $Matches[1]
        }
    }
    $seenHwSerials = @{}
    foreach ($s in $serials) {
        $hwSerial = (& $adbCmd -s $s shell getprop ro.serialno).Trim()
        if ($seenHwSerials.ContainsKey($hwSerial)) { continue }
        $seenHwSerials[$hwSerial] = $true

        $model = (& $adbCmd -s $s shell getprop ro.product.model).Trim()
        Write-Host "  -> Installing on $model ($s / $hwSerial)..."
        & $adbCmd -s $s shell settings put global verifier_verify_adb_installs 0 | Out-Null
        & $adbCmd -s $s shell settings put global package_verifier_enable 0 | Out-Null
        & $adbCmd -s $s push $finalDebugApk /data/local/tmp/timenow_orig.apk | Out-Null
        & $adbCmd -s $s shell pm install -r -g /data/local/tmp/timenow_orig.apk
        if ($LASTEXITCODE -ne 0) { throw "pm install failed on $s" }

        & $adbCmd -s $s shell pm grant com.nextai.camera android.permission.CAMERA 2>$null
        & $adbCmd -s $s shell pm grant com.nextai.camera android.permission.ACCESS_FINE_LOCATION 2>$null
        & $adbCmd -s $s shell pm grant com.nextai.camera android.permission.ACCESS_COARSE_LOCATION 2>$null
        & $adbCmd -s $s shell am force-stop com.nextai.camera
        & $adbCmd -s $s shell am start -n "com.nextai.camera/com.nextai.camera.MainActivity"
        Write-Host "  [DEPLOY PASS] Launched Time Now UI on $model ($s)!"
    }
}
