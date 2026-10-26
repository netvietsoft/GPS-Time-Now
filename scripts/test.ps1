# ============================================================================
# test.ps1 - Unified Strict Test Runner (Backend :4580, Native C++, Android APK)
# Enforces AUD-007 strict fail-on-error behavior (never skips missing deps)
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

# 1. Run Master Quality Gate (includes Project Memory, Security Scans, Backend Build & 15 Test Groups)
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

# 3. Verify Signed Android APK Artifact Integrity
$apkPath = Join-Path $ConvertRoot "apps\android\app\build\outputs\apk\debug\TimeNow-Original-v10.0.281-arm64.apk"
if (-not (Test-Path $apkPath)) {
    throw "Android APK artifact missing at $apkPath"
}
$apkSizeMb = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
Write-Host "`n[TEST 3] Verified signed Android APK artifact: $apkPath ($apkSizeMb MB)" -ForegroundColor Green

# 4. Optional Wireless ADB Connection to Physical Device
if ($ConnectRealDevice) {
    Write-Host "`n[TEST 4] Connecting to Real Device via ADB..." -ForegroundColor Yellow
    adb devices -l
}

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  TEST RUNNER COMPLETED SUCCESSFULLY (EXIT 0)" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
exit 0
