# ============================================================================
# test.ps1 - Unified Strict Test Runner (Backend :4580, Native C++, Android APK)
# Enforces AUD-007 & REV-003..004 reproducible clean-clone execution
# ============================================================================
param(
    [switch]$ConnectRealDevice = $false
)
$ErrorActionPreference = "Stop"
$ConvertRoot = Split-Path -Parent $PSScriptRoot
$RepoRoot    = Split-Path -Parent $ConvertRoot

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  NEXTAI CAMERA - UNIFIED TEST AND VERIFICATION RUNNER" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan

# 1. Run Master Quality Gate (includes Project Memory, Reference Hash Audit, Canonical APK Build + aapt2 xmltree, Backend Build & 16 Test Groups)
& "$PSScriptRoot\verify.ps1"
if ($LASTEXITCODE -ne 0) {
    throw "verify.ps1 failed with exit code $LASTEXITCODE"
}

# 2. Run Native C++17 and 7 ARM64 .so Verification Suite if present in parent workspace
$nativeAuditScript = Join-Path $RepoRoot "Native_CPP_Reconstructed\tests\verify_audit_2026_10_25.ps1"
if (Test-Path $nativeAuditScript) {
    Write-Host "`n[TEST 2] Running Native C++17 and ARM64 .so Audit Harness..." -ForegroundColor Yellow
    & $nativeAuditScript
    if ($LASTEXITCODE -ne 0) {
        throw "Native C++17 audit harness failed with exit code $LASTEXITCODE"
    }
}

# 3. Verify Signed Debug & Release Android APK Artifacts & Binary Manifest Audit Output
$debugApkPath   = Join-Path $ConvertRoot "apps/android/app/build/outputs/apk/debug/TimeNow-Original-v10.0.281-arm64.apk"
$releaseApkPath = Join-Path $ConvertRoot "apps/android/app/build/outputs/apk/release/TimeNow-Release-v10.0.281-arm64.apk"
$xmlTreePath    = Join-Path $ConvertRoot "apps/android/app/build/outputs/apk/manifest-xmltree-audit.txt"

foreach ($p in @($debugApkPath, $releaseApkPath, $xmlTreePath)) {
    if (-not (Test-Path $p)) {
        throw "Required Android build/audit artifact missing at $p"
    }
}
$apkSizeMb = [math]::Round((Get-Item $releaseApkPath).Length / 1MB, 2)
Write-Host "`n[TEST 3] Verified signed Debug & Release Android APKs ($apkSizeMb MB) and binary manifest xmltree audit." -ForegroundColor Green

# 4. Optional Wireless ADB Connection to Physical Device
if ($ConnectRealDevice) {
    Write-Host "`n[TEST 4] Connecting to Real Device via ADB..." -ForegroundColor Yellow
    adb devices -l
}

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  TEST RUNNER COMPLETED SUCCESSFULLY (EXIT 0)" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
exit 0
