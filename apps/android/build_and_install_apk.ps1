$ErrorActionPreference = "Stop"

$sdkRoot   = "C:\Program Files\Unity\Hub\Editor\2020.3.16f1\Editor\Data\PlaybackEngines\AndroidPlayer\SDK"
$jdkRoot   = "C:\Program Files\Unity\Hub\Editor\2020.3.16f1\Editor\Data\PlaybackEngines\AndroidPlayer\OpenJDK"
$btRoot    = "$sdkRoot\build-tools\30.0.2"
$androidJar = "$sdkRoot\platforms\android-32\android.jar"

$aapt2     = "$btRoot\aapt2.exe"
$zipalign  = "$btRoot\zipalign.exe"
$d8Jar     = "$btRoot\lib\d8.jar"
$apksignerJar = "$btRoot\lib\apksigner.jar"
$javac     = "$jdkRoot\bin\javac.exe"
$java      = "$jdkRoot\bin\java.exe"
$jar       = "$jdkRoot\bin\jar.exe"
$keytool   = "$jdkRoot\bin\keytool.exe"
$adb       = "$env:TEMP\android-platform-tools\platform-tools\adb.exe"

$androidRoot = "E:\Decompiler\APP\IMAGE\Timemark\CONVERT\apps\android"
$appMain     = "$androidRoot\app\src\main"
$buildDir    = "$androidRoot\build\apk_build"
$outApkDir   = "$androidRoot\app\build\outputs\apk\debug"

Write-Host "======================================================================"
Write-Host "  TIME NOW ORIGINAL UI (v10.0.281) - CLEAN BUILD & DEVICE DEPLOY"
Write-Host "======================================================================"

if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null
New-Item -ItemType Directory -Force -Path $outApkDir | Out-Null

# 1. Compile Android resources with aapt2
Write-Host "[1/6] Compiling Time Now Android resources with aapt2..."
$compiledResZip = "$buildDir\compiled_res.zip"
& $aapt2 compile --dir "$appMain\res" -o $compiledResZip
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed" }

# 2. Generate clean AndroidManifest.xml matching original Timemark icon, label & theme
$manifestBuild = "$buildDir\AndroidManifest.xml"
$manifestContent = @"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.nextai.camera"
    android:versionCode="100028100"
    android:versionName="10.0.281">

    <uses-sdk android:minSdkVersion="26" android:targetSdkVersion="32" />

    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
    <uses-feature android:glEsVersion="0x00020000" android:required="false" />

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:name="com.nextai.camera.NextAiApplication"
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher_xhey"
        android:roundIcon="@drawable/ic_launcher_round_xhey"
        android:label="@string/i_app_name"
        android:supportsRtl="true"
        android:usesCleartextTraffic="true"
        android:largeHeap="true"
        android:theme="@style/Theme.NextAICamera">

        <activity
            android:name="com.nextai.camera.MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"@
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($manifestBuild, $manifestContent, $utf8NoBom)

$unalignedApk = "$buildDir\unaligned.apk"
$genJavaDir   = "$buildDir\gen_java"
New-Item -ItemType Directory -Force -Path $genJavaDir | Out-Null

& $aapt2 link -o $unalignedApk `
    -I $androidJar `
    --manifest $manifestBuild `
    --java $genJavaDir `
    --auto-add-overlay `
    -0 arsc -0 so -0 mp3 -0 otf -0 ttf `
    $compiledResZip
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed" }

# 3. Compile Java sources
Write-Host "[2/6] Compiling Java sources (MainActivity, OriginalDialogs, WatermarkModel, NextAiApplication)..."
$classesDir = "$buildDir\classes"
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

$srcJavaDir = "$appMain\java\com\nextai\camera"
$javaFiles = @(
    Get-ChildItem -Path $genJavaDir -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
    Get-ChildItem -Path $srcJavaDir -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
)

& $javac -source 1.8 -target 1.8 -encoding UTF-8 -bootclasspath $androidJar -d $classesDir $javaFiles
if ($LASTEXITCODE -ne 0) { throw "javac compilation failed" }

# 4. Dexing with d8
Write-Host "[3/6] Converting .class files to classes.dex with d8..."
$classFiles = Get-ChildItem -Path $classesDir -Filter "*.class" -Recurse | Select-Object -ExpandProperty FullName
& $java -cp $d8Jar com.android.tools.r8.D8 --lib $androidJar --min-api 26 --output $buildDir $classFiles
if ($LASTEXITCODE -ne 0) { throw "d8 dexing failed" }

# 5. Package classes.dex, assets/fonts, assets/watermark, and lib/arm64-v8a/*.so
Write-Host "[4/6] Packaging classes.dex, original fonts/watermark assets, and 7 ARM64 .so libraries..."
$stageDir = "$buildDir\apk_stage"
New-Item -ItemType Directory -Force -Path "$stageDir\lib\arm64-v8a" | Out-Null
New-Item -ItemType Directory -Force -Path "$stageDir\assets\fonts" | Out-Null
New-Item -ItemType Directory -Force -Path "$stageDir\assets\watermark" | Out-Null

Copy-Item -Force "$buildDir\classes.dex" "$stageDir\classes.dex"
Copy-Item -Force "$appMain\jniLibs\arm64-v8a\*.so" "$stageDir\lib\arm64-v8a\"
Copy-Item -Force "$appMain\assets\fonts\*.otf" "$stageDir\assets\fonts\" -ErrorAction SilentlyContinue
Copy-Item -Force "$appMain\assets\fonts\*.ttf" "$stageDir\assets\fonts\" -ErrorAction SilentlyContinue
Copy-Item -Force "$appMain\assets\watermark\*.json" "$stageDir\assets\watermark\" -ErrorAction SilentlyContinue

Push-Location $stageDir
& $jar uf $unalignedApk "classes.dex"
& $jar uf0 $unalignedApk "assets"
& $jar uf0 $unalignedApk "lib"
Pop-Location

# 6. Align & Sign APK
Write-Host "[5/6] Running zipalign & apksigner..."
$alignedApk = "$buildDir\aligned.apk"
& $zipalign -p -f 4 $unalignedApk $alignedApk
if ($LASTEXITCODE -ne 0) { throw "zipalign failed" }

$keystore = "$androidRoot\nextai_debug.keystore"
if (-not (Test-Path $keystore)) {
    & $keytool -genkeypair -v `
        -keystore $keystore `
        -storepass "nextai2026" `
        -keypass "nextai2026" `
        -alias "nextai_key" `
        -keyalg RSA -keysize 2048 -validity 10000 `
        -dname "CN=Timemark Original, OU=Mobile, O=Timemark, L=Hanoi, S=HN, C=VN"
}

$finalApk = "$outApkDir\TimeNow-Original-v10.0.281-arm64.apk"
& $java -jar $apksignerJar sign `
    --ks $keystore `
    --ks-key-alias "nextai_key" `
    --ks-pass "pass:nextai2026" `
    --key-pass "pass:nextai2026" `
    --v1-signing-enabled true `
    --v2-signing-enabled true `
    --out $finalApk `
    $alignedApk
if ($LASTEXITCODE -ne 0) { throw "apksigner failed" }
Copy-Item -Force $finalApk "$outApkDir\Timemark-Original-v10.0.281-arm64.apk"

$apkSizeMB = [Math]::Round((Get-Item $finalApk).Length / 1MB, 2)
Write-Host "  [BUILD PASS] Signed APK: $finalApk ($apkSizeMB MB)"

# 7. Deploy to Connected Devices
Write-Host "[6/6] Deploying to connected Samsung devices..."
& $adb start-server | Out-Null
Start-Sleep -Seconds 2
$devicesOutput = & $adb devices
$serials = @()
foreach ($line in $devicesOutput) {
    if ($line -match "^(\S+)\s+device\b") {
        $serials += $Matches[1]
    }
}
# Deduplicate by physical serial ro.serialno so we don't install twice on the same phone
$seenHwSerials = @{}
foreach ($s in $serials) {
    $hwSerial = (& $adb -s $s shell getprop ro.serialno).Trim()
    if ($seenHwSerials.ContainsKey($hwSerial)) { continue }
    $seenHwSerials[$hwSerial] = $true

    $model = (& $adb -s $s shell getprop ro.product.model).Trim()
    Write-Host "  -> Installing on $model ($s / $hwSerial)..."
    & $adb -s $s shell settings put global verifier_verify_adb_installs 0 | Out-Null
    & $adb -s $s shell settings put global package_verifier_enable 0 | Out-Null
    & $adb -s $s push $finalApk /data/local/tmp/timemark_orig.apk | Out-Null
    & $adb -s $s shell pm install -r -g /data/local/tmp/timemark_orig.apk
    if ($LASTEXITCODE -ne 0) { throw "pm install failed on $s" }

    & $adb -s $s shell pm grant com.nextai.camera android.permission.CAMERA 2>$null
    & $adb -s $s shell pm grant com.nextai.camera android.permission.ACCESS_FINE_LOCATION 2>$null
    & $adb -s $s shell pm grant com.nextai.camera android.permission.ACCESS_COARSE_LOCATION 2>$null
    & $adb -s $s shell am force-stop com.nextai.camera
    & $adb -s $s shell am start -n "com.nextai.camera/com.nextai.camera.MainActivity"
    Write-Host "  [DEPLOY PASS] Launched Original Timemark UI on $model ($s)!"
}
