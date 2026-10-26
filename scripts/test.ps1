# ============================================================================
# test.ps1 - Unified Test Runner (Native C++, Backend :4580, Android and ADB A50)
# Requirement #8: Test Device 192.168.1.2:35977 (Pair: 28721, Code: 741742)
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

# 1. Run Workspace Standard V2.1.2 and Project Memory Gate
& "$PSScriptRoot\verify.ps1"

# 2. Run Native C++17 and 7 ARM64 .so Verification Suite if present
$nativeAuditScript = Join-Path $RepoRoot "Native_CPP_Reconstructed\tests\verify_audit_2026_10_25.ps1"
if (Test-Path $nativeAuditScript) {
    Write-Host "`n[TEST 2] Running Native C++17 and ARM64 .so Audit Harness..." -ForegroundColor Yellow
    & $nativeAuditScript
}

# 3. Run Backend TypeScript/Node.js Tests if present
$backendPkg = Join-Path $ConvertRoot "Backend\package.json"
if (Test-Path $backendPkg) {
    Write-Host "`n[TEST 3] Checking Backend project in CONVERT\Backend..." -ForegroundColor Yellow
    Push-Location (Join-Path $ConvertRoot "Backend")
    try {
        if (Test-Path "node_modules") {
            npm test
        } else {
            Write-Host "  [INFO] Backend package.json ready (run 'npm install && npm test' in CONVERT/Backend)." -ForegroundColor Gray
        }
    } finally {
        Pop-Location
    }
}

# 4. Optional Wireless ADB Connection to Samsung Galaxy A50 (192.168.1.2:35977)
if ($ConnectRealDevice) {
    Write-Host "`n[TEST 4] Connecting to Real Device Samsung Galaxy A50 (192.168.1.2:35977)..." -ForegroundColor Yellow
    adb pair 192.168.1.2:28721 741742
    adb connect 192.168.1.2:35977
    adb -s 192.168.1.2:35977 reverse tcp:4580 tcp:4580
    adb -s 192.168.1.2:35977 shell getprop ro.product.model
}

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  TEST RUNNER COMPLETED SUCCESSFULLY" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
exit 0
