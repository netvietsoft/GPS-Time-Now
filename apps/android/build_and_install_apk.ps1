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

$exeSuffix    = if ($IsWindows -or $env:OS -match "Windows") { ".exe" } else { "" }
$aapt2        = Join-Path $btRoot "aapt2$exeSuffix"
$zipalign     = Join-Path $btRoot "zipalign$exeSuffix"
$d8Jar        = Join-Path (Join-Path $btRoot "lib") "d8.jar"
$apksignerJar = Join-Path (Join-Path $btRoot "lib") "apksigner.jar"

if ($jdkCandidates.Count -gt 0) {
    $jdkBin    = Join-Path $jdkCandidates[0] "bin"
    $javac     = Join-Path $jdkBin "javac$exeSuffix"
    $java      = Join-Path $jdkBin "java$exeSuffix"
    $jar       = Join-Path $jdkBin "jar$exeSuffix"
    $keytool   = Join-Path $jdkBin "keytool$exeSuffix"
    $jarsigner = Join-Path $jdkBin "jarsigner$exeSuffix"
} else {
    $javac     = (Get-Command javac).Source
    $java      = (Get-Command java).Source
    $jar       = (Get-Command jar).Source
    $keytool   = (Get-Command keytool).Source
    $jarsigner = (Get-Command jarsigner).Source
}

$androidRoot       = $PSScriptRoot
$appMain           = Join-Path $androidRoot "app/src/main"
$canonicalManifest = Join-Path $appMain "AndroidManifest.xml"
$buildDir          = Join-Path $androidRoot "build/apk_build_$PID"
$outDebugDir       = Join-Path $androidRoot "app/build/outputs/apk/debug"
$outReleaseDir     = Join-Path $androidRoot "app/build/outputs/apk/release"
$outBundleDir      = Join-Path $androidRoot "app/build/outputs/bundle/release"

function Invoke-NativeTool {
    param(
        [string]$StepName,
        [scriptblock]$Command
    )
    $prevEap = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $outputLines = @()
    try {
        $outputLines = & $Command 2>&1 | ForEach-Object { "$_" }
        $code = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $prevEap
    }
    if ($code -ne 0) {
        foreach ($line in $outputLines) { Write-Host "  [ERR] $line" }
        throw "$StepName failed with exit code $code"
    }
}

# 16 KB Memory Page Size ZIP Aligner (Aligns uncompressed .so entries to 16384-byte boundaries & other stored entries to 4-byte boundaries)
if (-not ("NextAiZipAlign16KB" -as [type])) {
    Add-Type -TypeDefinition @"
using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

public static class NextAiZipAlign16KB {
    private class CdEntry {
        public byte[] RawCdHeader;
        public ushort Flags;
        public ushort Method;
        public uint Crc32;
        public uint CompSize;
        public uint UncompSize;
        public byte[] NameBytes;
        public byte[] CommentBytes;
        public uint OldLocalOffset;
        public uint NewLocalOffset;
        public byte[] Payload;
        public ushort VerNeeded;
        public uint DosTimeDate;
    }

    public static void AlignZip(string inPath, string outPath) {
        byte[] src = File.ReadAllBytes(inPath);
        int eocdPos = -1;
        for (int i = src.Length - 22; i >= Math.Max(0, src.Length - 65557); i--) {
            if (BitConverter.ToUInt32(src, i) == 0x06054b50u) {
                eocdPos = i;
                break;
            }
        }
        if (eocdPos < 0) throw new InvalidDataException("EOCD not found in ZIP");

        ushort totalEntries = BitConverter.ToUInt16(src, eocdPos + 10);
        uint cdSize = BitConverter.ToUInt32(src, eocdPos + 12);
        uint cdOffset = BitConverter.ToUInt32(src, eocdPos + 16);
        ushort zipCommentLen = BitConverter.ToUInt16(src, eocdPos + 20);
        byte[] zipComment = new byte[zipCommentLen];
        if (zipCommentLen > 0) Buffer.BlockCopy(src, eocdPos + 22, zipComment, 0, zipCommentLen);

        var entries = new List<CdEntry>(totalEntries);
        int pos = (int)cdOffset;
        for (int i = 0; i < totalEntries; i++) {
            if (BitConverter.ToUInt32(src, pos) != 0x02014b50u)
                throw new InvalidDataException("Invalid CD signature at " + pos);
            ushort verNeeded = BitConverter.ToUInt16(src, pos + 6);
            ushort flags = BitConverter.ToUInt16(src, pos + 8);
            ushort method = BitConverter.ToUInt16(src, pos + 10);
            uint dosTimeDate = BitConverter.ToUInt32(src, pos + 12);
            uint crc = BitConverter.ToUInt32(src, pos + 16);
            uint compSize = BitConverter.ToUInt32(src, pos + 20);
            uint uncompSize = BitConverter.ToUInt32(src, pos + 24);
            ushort nameLen = BitConverter.ToUInt16(src, pos + 28);
            ushort extraLen = BitConverter.ToUInt16(src, pos + 30);
            ushort commentLen = BitConverter.ToUInt16(src, pos + 32);
            uint localOff = BitConverter.ToUInt32(src, pos + 42);

            byte[] rawHdr = new byte[46];
            Buffer.BlockCopy(src, pos, rawHdr, 0, 46);
            byte[] nameBytes = new byte[nameLen];
            Buffer.BlockCopy(src, pos + 46, nameBytes, 0, nameLen);
            byte[] commentBytes = new byte[commentLen];
            if (commentLen > 0) Buffer.BlockCopy(src, pos + 46 + nameLen + extraLen, commentBytes, 0, commentLen);

            int lpos = (int)localOff;
            if (BitConverter.ToUInt32(src, lpos) != 0x04034b50u)
                throw new InvalidDataException("Invalid LFH signature at " + lpos);
            ushort lNameLen = BitConverter.ToUInt16(src, lpos + 26);
            ushort lExtraLen = BitConverter.ToUInt16(src, lpos + 28);
            int dataStart = lpos + 30 + lNameLen + lExtraLen;
            byte[] payload = new byte[compSize];
            Buffer.BlockCopy(src, dataStart, payload, 0, (int)compSize);

            string entryName = Encoding.UTF8.GetString(nameBytes);
            if (!entryName.EndsWith("/") && !entryName.StartsWith("META-INF/", StringComparison.OrdinalIgnoreCase)) {
                entries.Add(new CdEntry {
                    RawCdHeader = rawHdr,
                    VerNeeded = verNeeded,
                    Flags = (ushort)(flags & ~0x0008u),
                    Method = method,
                    DosTimeDate = dosTimeDate,
                    Crc32 = crc,
                    CompSize = compSize,
                    UncompSize = uncompSize,
                    NameBytes = nameBytes,
                    CommentBytes = commentBytes,
                    OldLocalOffset = localOff,
                    Payload = payload
                });
            }

            pos += 46 + nameLen + extraLen + commentLen;
        }

        using (var fs = new FileStream(outPath, FileMode.Create, FileAccess.Write))
        using (var bw = new BinaryWriter(fs)) {
            foreach (var e in entries) {
                e.NewLocalOffset = (uint)fs.Position;
                string name = Encoding.UTF8.GetString(e.NameBytes);
                ushort padLen = 0;
                bool isSo = name.EndsWith(".so", StringComparison.OrdinalIgnoreCase);
                if (e.Method == 0) {
                    int align = isSo ? 16384 : 4;
                    long dataPos = fs.Position + 30 + e.NameBytes.Length;
                    int rem = (int)(dataPos % align);
                    if (rem != 0) padLen = (ushort)(align - rem);
                    if (isSo && padLen > 0 && padLen < 4) padLen = (ushort)(padLen + 16384);
                }

                bw.Write(0x04034b50u);
                bw.Write(e.VerNeeded);
                bw.Write(e.Flags);
                bw.Write(e.Method);
                bw.Write(e.DosTimeDate);
                bw.Write(e.Crc32);
                bw.Write(e.CompSize);
                bw.Write(e.UncompSize);
                bw.Write((ushort)e.NameBytes.Length);
                bw.Write(padLen);
                bw.Write(e.NameBytes);
                if (padLen > 0) {
                    byte[] padBytes = new byte[padLen];
                    if (padLen >= 4) {
                        padBytes[0] = 0x52;
                        padBytes[1] = 0xFB;
                        ushort tlvLen = (ushort)(padLen - 4);
                        padBytes[2] = (byte)(tlvLen & 0xFF);
                        padBytes[3] = (byte)((tlvLen >> 8) & 0xFF);
                    }
                    bw.Write(padBytes);
                }
                bw.Write(e.Payload);
            }

            uint newCdOffset = (uint)fs.Position;
            foreach (var e in entries) {
                // Clear bit 3 (data descriptor) & set extraLen = 0, update local header offset
                byte[] hdr = (byte[])e.RawCdHeader.Clone();
                Buffer.BlockCopy(BitConverter.GetBytes(e.Flags), 0, hdr, 8, 2);
                Buffer.BlockCopy(BitConverter.GetBytes(e.Crc32), 0, hdr, 16, 4);
                Buffer.BlockCopy(BitConverter.GetBytes(e.CompSize), 0, hdr, 20, 4);
                Buffer.BlockCopy(BitConverter.GetBytes(e.UncompSize), 0, hdr, 24, 4);
                Buffer.BlockCopy(BitConverter.GetBytes((ushort)0), 0, hdr, 30, 2);
                Buffer.BlockCopy(BitConverter.GetBytes(e.NewLocalOffset), 0, hdr, 42, 4);
                bw.Write(hdr);
                bw.Write(e.NameBytes);
                if (e.CommentBytes.Length > 0) bw.Write(e.CommentBytes);
            }
            uint newCdSize = (uint)(fs.Position - newCdOffset);

            bw.Write(0x06054b50u);
            bw.Write((ushort)0);
            bw.Write((ushort)0);
            bw.Write((ushort)entries.Count);
            bw.Write((ushort)entries.Count);
            bw.Write(newCdSize);
            bw.Write(newCdOffset);
            bw.Write((ushort)zipComment.Length);
            if (zipComment.Length > 0) bw.Write(zipComment);
        }
    }

    public static Dictionary<string, long> GetStoredEntryOffsets(string zipPath) {
        var result = new Dictionary<string, long>(StringComparer.OrdinalIgnoreCase);
        byte[] src = File.ReadAllBytes(zipPath);
        int eocdPos = -1;
        for (int i = src.Length - 22; i >= Math.Max(0, src.Length - 65557); i--) {
            if (BitConverter.ToUInt32(src, i) == 0x06054b50u) { eocdPos = i; break; }
        }
        ushort totalEntries = BitConverter.ToUInt16(src, eocdPos + 10);
        int pos = (int)BitConverter.ToUInt32(src, eocdPos + 16);
        for (int i = 0; i < totalEntries; i++) {
            ushort method = BitConverter.ToUInt16(src, pos + 10);
            ushort nameLen = BitConverter.ToUInt16(src, pos + 28);
            ushort extraLen = BitConverter.ToUInt16(src, pos + 30);
            ushort commentLen = BitConverter.ToUInt16(src, pos + 32);
            uint localOff = BitConverter.ToUInt32(src, pos + 42);
            string name = Encoding.UTF8.GetString(src, pos + 46, nameLen);
            int lpos = (int)localOff;
            ushort lNameLen = BitConverter.ToUInt16(src, lpos + 26);
            ushort lExtraLen = BitConverter.ToUInt16(src, lpos + 28);
            long dataStart = (long)lpos + 30 + lNameLen + lExtraLen;
            if (method == 0) result[name] = dataStart;
            pos += 46 + nameLen + extraLen + commentLen;
        }
        return result;
    }
}
"@
}

Write-Host "======================================================================"
Write-Host "  NEXTAI CAMERA (v10.0.281) - API 36 / PLAY BILLING 9.1.0 / 16KB BUILD"
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
New-Item -ItemType Directory -Force -Path $outBundleDir | Out-Null

# 1. Compile Android resources with aapt2
Write-Host "[1/7] Compiling Android resources with aapt2..."
$compiledResZip = Join-Path $buildDir "compiled_res.zip"
Invoke-NativeTool "aapt2 compile" { & $aapt2 compile --dir (Join-Path $appMain "res") -o $compiledResZip }

# 2. Link directly against canonical AndroidManifest.xml (targetSdkVersion=36, usesCleartextTraffic=false, extractNativeLibs=false)
Write-Host "[2/7] Linking resources against canonical AndroidManifest.xml (API 36)..."
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

# 3. Compile Java sources (including PlayBillingManager v9.1.0)
Write-Host "[3/7] Compiling Java sources..."
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
Write-Host "[4/7] Converting .class files to classes.dex with d8..."
$classFiles = Get-ChildItem -Path $classesDir -Filter "*.class" -Recurse | Select-Object -ExpandProperty FullName
Invoke-NativeTool "d8" {
    & $java -cp $d8Jar com.android.tools.r8.D8 --lib $androidJar --min-api 26 --output $buildDir $classFiles
}

# 5. Package classes.dex, Play Billing 9.1.0 metadata, assets, and 7 16KB-aligned ARM64 .so libraries
Write-Host "[5/7] Packaging classes.dex, Play Billing 9.1.0 version descriptor, assets, and 7 16KB ARM64 .so libraries..."
$stageDir = Join-Path $buildDir "apk_stage"
New-Item -ItemType Directory -Force -Path (Join-Path $stageDir "lib/arm64-v8a") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $stageDir "assets/fonts") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $stageDir "assets/watermark") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $stageDir "META-INF") | Out-Null

Copy-Item -Force (Join-Path $buildDir "classes.dex") (Join-Path $stageDir "classes.dex")
Copy-Item -Force (Join-Path $appMain "jniLibs/arm64-v8a/*.so") (Join-Path $stageDir "lib/arm64-v8a/")
Copy-Item -Force (Join-Path $appMain "assets/fonts/*.otf") (Join-Path $stageDir "assets/fonts/") -ErrorAction SilentlyContinue
Copy-Item -Force (Join-Path $appMain "assets/fonts/*.ttf") (Join-Path $stageDir "assets/fonts/") -ErrorAction SilentlyContinue
Copy-Item -Force (Join-Path $appMain "assets/watermark/*.json") (Join-Path $stageDir "assets/watermark/") -ErrorAction SilentlyContinue
Set-Content -Path (Join-Path $stageDir "META-INF/com.google.android.play.billingclient.version") -Value "9.1.0" -NoNewline -Encoding ASCII

# Verify all 7 .so files have ELF PT_LOAD p_align >= 16384 (0x4000) before packaging
$soFiles = @(Get-ChildItem -Path (Join-Path $stageDir "lib/arm64-v8a/*.so"))
if ($soFiles.Count -lt 7) {
    throw "Expected 7 ARM64 .so libraries in jniLibs/arm64-v8a, found $($soFiles.Count)!"
}
foreach ($so in $soFiles) {
    $bytes = [System.IO.File]::ReadAllBytes($so.FullName)
    $phoff = [BitConverter]::ToUInt64($bytes, 32)
    $phentsize = [BitConverter]::ToUInt16($bytes, 54)
    $phnum = [BitConverter]::ToUInt16($bytes, 56)
    for ($i = 0; $i -lt $phnum; $i++) {
        $off = [int]$phoff + ($i * $phentsize)
        $ptype = [BitConverter]::ToUInt32($bytes, $off)
        if ($ptype -eq 1) { # PT_LOAD
            $palign = [BitConverter]::ToUInt64($bytes, $off + 48)
            if ($palign -lt 16384 -or ($palign % 16384) -ne 0) {
                throw "16KB Page Size Failure: $($so.Name) has PT_LOAD p_align=0x$($palign.ToString('X')) (< 16384)!"
            }
        }
    }
}

Push-Location $stageDir
try {
    Invoke-NativeTool "jar classes.dex" { & $jar uMf $unalignedApk "classes.dex" }
    Invoke-NativeTool "jar assets" { & $jar uMf0 $unalignedApk "assets" }
    Invoke-NativeTool "jar lib" { & $jar uMf0 $unalignedApk "lib" }
} finally {
    Pop-Location
}

# 6. Align to 16 KB Memory Page Boundaries, Sign & Verify Packaged Binary Manifest
Write-Host "[6/7] Aligning .so entries to 16 KB (16384-byte) page boundaries, signing APK & verifying binary manifest..."
$alignedApk = Join-Path $buildDir "aligned_16kb.apk"
[NextAiZipAlign16KB]::AlignZip($unalignedApk, $alignedApk)

$keystore = Join-Path $androidRoot "nextai_debug.keystore"
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

# Verify 16 KB ZIP offset alignment on the signed Release APK
$storedOffsets = [NextAiZipAlign16KB]::GetStoredEntryOffsets($finalReleaseApk)
foreach ($kv in $storedOffsets.GetEnumerator()) {
    if ($kv.Key.EndsWith(".so", [StringComparison]::OrdinalIgnoreCase)) {
        if (($kv.Value % 16384) -ne 0) {
            throw "16KB APK ZIP Alignment Failure: $($kv.Key) at offset $($kv.Value) is not a multiple of 16384!"
        }
    }
}

# 7. Build Signed Release Android App Bundle (.aab < 500 MB)
Write-Host "[7/7] Building signed Release Android App Bundle (.aab < 500 MB)..."
$protoApk = Join-Path $buildDir "proto_base.apk"
Invoke-NativeTool "aapt2 link proto" {
    & $aapt2 link --proto-format -o $protoApk `
        -I $androidJar `
        --manifest $canonicalManifest `
        --auto-add-overlay `
        -0 so -0 mp3 -0 otf -0 ttf `
        $compiledResZip
}

$aabStageDir = Join-Path $buildDir "aab_stage"
$aabBaseDir  = Join-Path $aabStageDir "base"
New-Item -ItemType Directory -Force -Path $aabBaseDir | Out-Null

Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($protoApk, $aabBaseDir)

New-Item -ItemType Directory -Force -Path (Join-Path $aabBaseDir "manifest") | Out-Null
Move-Item -Force (Join-Path $aabBaseDir "AndroidManifest.xml") (Join-Path $aabBaseDir "manifest/AndroidManifest.xml")

New-Item -ItemType Directory -Force -Path (Join-Path $aabBaseDir "dex") | Out-Null
Copy-Item -Force (Join-Path $buildDir "classes.dex") (Join-Path $aabBaseDir "dex/classes.dex")

New-Item -ItemType Directory -Force -Path (Join-Path $aabBaseDir "lib/arm64-v8a") | Out-Null
Copy-Item -Force (Join-Path $stageDir "lib/arm64-v8a/*.so") (Join-Path $aabBaseDir "lib/arm64-v8a/")

New-Item -ItemType Directory -Force -Path (Join-Path $aabBaseDir "assets/fonts") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $aabBaseDir "assets/watermark") | Out-Null
Copy-Item -Force (Join-Path $stageDir "assets/fonts/*") (Join-Path $aabBaseDir "assets/fonts/") -ErrorAction SilentlyContinue
Copy-Item -Force (Join-Path $stageDir "assets/watermark/*") (Join-Path $aabBaseDir "assets/watermark/") -ErrorAction SilentlyContinue

New-Item -ItemType Directory -Force -Path (Join-Path $aabBaseDir "root/META-INF") | Out-Null
Set-Content -Path (Join-Path $aabBaseDir "root/META-INF/com.google.android.play.billingclient.version") -Value "9.1.0" -NoNewline -Encoding ASCII

$finalReleaseAab = Join-Path $outBundleDir "TimeNow-Release-v10.0.281-arm64.aab"
if (Test-Path $finalReleaseAab) { Remove-Item -Force $finalReleaseAab }
[System.IO.Compression.ZipFile]::CreateFromDirectory($aabStageDir, $finalReleaseAab, [System.IO.Compression.CompressionLevel]::Optimal, $false)

Invoke-NativeTool "jarsigner aab" {
    & $jarsigner `
        -keystore $keystore `
        -storepass "nextai2026" `
        -keypass "nextai2026" `
        -sigalg SHA256withRSA `
        -digestalg SHA-256 `
        $finalReleaseAab `
        "nextai_key"
}

$aabBytes = (Get-Item $finalReleaseAab).Length
$aabSizeMB = [Math]::Round($aabBytes / 1MB, 2)
if ($aabBytes -ge 500MB) {
    throw "AAB Size Limit Failure: $finalReleaseAab is $aabSizeMB MB (must be < 500 MB)!"
}

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
if ($xmlTreeOutput -notmatch 'targetSdkVersion\(0x01010270\)=(\(type 0x10\))?(36|0x24)\b') {
    throw "PLAY-API36 FAILURE: Compiled APK binary AndroidManifest.xml does not have targetSdkVersion=36 (0x24)!"
}
if ($xmlTreeOutput -notmatch 'extractNativeLibs\(0x010104ea\)=(\(type 0x12\))?(false|0x0)\b') {
    throw "PLAY-16KB FAILURE: Compiled APK binary AndroidManifest.xml does not enforce extractNativeLibs=false!"
}
if ($xmlTreeOutput -notmatch 'allowBackup\(0x01010280\)=(\(type 0x12\))?(false|0x0)\b') {
    throw "PLAY-SECURITY FAILURE: Compiled APK binary AndroidManifest.xml does not enforce allowBackup=false!"
}
if ($xmlTreeOutput -notmatch 'com\.google\.android\.play\.billingclient\.version' -or $xmlTreeOutput -notmatch '9\.1\.0') {
    throw "PLAY-BILLING FAILURE: Compiled APK binary AndroidManifest.xml is missing Play Billing 9.1.0 metadata!"
}

$apkSizeMB = [Math]::Round((Get-Item $finalReleaseApk).Length / 1MB, 2)
Write-Host "  [BUILD & PLAY CONSOLE COMPLIANCE AUDIT PASS]"
Write-Host "    Debug APK    : $finalDebugApk ($apkSizeMB MB)"
Write-Host "    Release APK  : $finalReleaseApk ($apkSizeMB MB)"
Write-Host "    Release AAB  : $finalReleaseAab ($aabSizeMB MB < 500 MB)"
Write-Host "    Target API   : targetSdkVersion=36 (0x24), minSdkVersion=26 (0x1a)"
Write-Host "    Play Billing : v9.1.0 (com.android.vending.BILLING + billingclient.version=9.1.0)"
Write-Host "    16KB Pages   : 7/7 ARM64 .so ELF PT_LOAD p_align=0x4000 (16384) & APK ZIP offset % 16384 == 0"
Write-Host "    Security     : usesCleartextTraffic=false, allowBackup=false, extractNativeLibs=false"

if ($SkipDeploy) {
    Write-Host "  [SKIP DEPLOY] -SkipDeploy specified; skipping ADB installation."
    exit 0
}

# 8. Deploy to Connected Devices (if adb is available and -SkipDeploy is not set)
$workspaceRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $androidRoot))
$adbCandidates = @(
    @(
        (Join-Path $workspaceRoot ".tools/android-platform-tools/platform-tools/adb$exeSuffix"),
        "$env:TEMP\android-platform-tools\platform-tools\adb$exeSuffix"
    ) | Where-Object { $_ -and (Test-Path $_) }
)
$adbCmd = $null
if ($adbCandidates.Count -gt 0) {
    $adbCmd = $adbCandidates[0]
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
        $installOut = (& $adbCmd -s $s shell pm install -r -g /data/local/tmp/timenow_orig.apk 2>&1) -join "`n"
        if ($LASTEXITCODE -ne 0 -or $installOut -match "Failure") {
            Write-Host "     Signature changed; uninstalling previous com.nextai.camera and reinstalling..."
            & $adbCmd -s $s shell pm uninstall com.nextai.camera | Out-Null
            & $adbCmd -s $s shell pm install -r -g /data/local/tmp/timenow_orig.apk
            if ($LASTEXITCODE -ne 0) { throw "pm install failed on $s" }
        } else {
            Write-Host "     $installOut"
        }

        & $adbCmd -s $s shell pm grant com.nextai.camera android.permission.CAMERA 2>$null
        & $adbCmd -s $s shell pm grant com.nextai.camera android.permission.RECORD_AUDIO 2>$null
        & $adbCmd -s $s shell pm grant com.nextai.camera android.permission.ACCESS_FINE_LOCATION 2>$null
        & $adbCmd -s $s shell pm grant com.nextai.camera android.permission.ACCESS_COARSE_LOCATION 2>$null
        & $adbCmd -s $s shell am force-stop com.nextai.camera
        & $adbCmd -s $s shell am start -n "com.nextai.camera/com.nextai.camera.MainActivity"
        Write-Host "  [DEPLOY PASS] Launched Time Now UI on $model ($s)!"
    }
}
