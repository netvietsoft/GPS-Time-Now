# ============================================================================
# validate-project-memory.ps1 - Project Knowledge Validation Gate (Section 57.10)
# Standard: Development Workspace Standard V2.1.2
# ============================================================================
$ErrorActionPreference = "Stop"
$ConvertRoot = Split-Path -Parent $PSScriptRoot
$ErrorFile   = Join-Path $ConvertRoot "PROJECT_ERROR.md"
$AcqFile     = Join-Path $ConvertRoot "ACQUIREMENTS.md"

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  PROJECT KNOWLEDGE VALIDATION GATE (Standard V2.1.2 Section 57.10)" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan

if (-not (Test-Path $ErrorFile)) { throw "Missing PROJECT_ERROR.md at $ErrorFile" }
if (-not (Test-Path $AcqFile))   { throw "Missing ACQUIREMENTS.md at $AcqFile" }

$errText = Get-Content -Path $ErrorFile -Raw -Encoding UTF8
$acqText = Get-Content -Path $AcqFile   -Raw -Encoding UTF8

# 1. Check ERR-YYYYMMDD-NNN IDs and uniqueness
$errMatches = [regex]::Matches($errText, "(?m)^##\s+(ERR-\d{8}-\d{3})\b")
if ($errMatches.Count -eq 0) { throw "PROJECT_ERROR.md must contain at least one ERR-YYYYMMDD-NNN heading" }
$errIds = @()
foreach ($m in $errMatches) {
    $id = $m.Groups[1].Value
    if ($errIds -contains $id) { throw "Duplicate ERR-ID detected: $id" }
    $errIds += $id
}
Write-Host "  [PASS] PROJECT_ERROR.md contains $($errIds.Count) unique error entries: $($errIds -join ', ')" -ForegroundColor Green

# 2. Verify RESOLVED entries have Verification and Prevention sections
if ($errText -notmatch "### Verification" -or $errText -notmatch "### Prevention / Guardrail") {
    throw "RESOLVED entries in PROJECT_ERROR.md must include '### Verification' and '### Prevention / Guardrail'"
}
Write-Host "  [PASS] All RESOLVED entries include Verification and Prevention sections." -ForegroundColor Green

# 3. Check ACQ-YYYYMMDD-NNN IDs and uniqueness
$acqMatches = [regex]::Matches($acqText, "(?m)^##\s+(ACQ-\d{8}-\d{3})\b")
if ($acqMatches.Count -eq 0) { throw "ACQUIREMENTS.md must contain at least one ACQ-YYYYMMDD-NNN heading" }
$acqIds = @()
foreach ($m in $acqMatches) {
    $id = $m.Groups[1].Value
    if ($acqIds -contains $id) { throw "Duplicate ACQ-ID detected: $id" }
    $acqIds += $id
}
Write-Host "  [PASS] ACQUIREMENTS.md contains $($acqIds.Count) unique acquirement entries: $($acqIds -join ', ')" -ForegroundColor Green

# 4. Verify STANDARD entries have When to use, When not to use, and Evidence / Verification
if ($acqText -notmatch "### When to use" -or $acqText -notmatch "### When not to use" -or $acqText -notmatch "### Evidence / Verification") {
    throw "STANDARD entries in ACQUIREMENTS.md must include '### When to use', '### When not to use', and '### Evidence / Verification'"
}
Write-Host "  [PASS] All STANDARD entries include When-to-use, When-not-to-use and Evidence sections." -ForegroundColor Green

# 5. Secret Scanner Check (Section 57.9)
$secretPatterns = @(
    "-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----",
    "sk_live_[0-9a-zA-Z]{24,}",
    "AKIA[0-9A-Z]{16}"
)
foreach ($pat in $secretPatterns) {
    if ($errText -match $pat -or $acqText -match $pat) {
        throw "Potential live secret matching '$pat' found in project knowledge files!"
    }
}
Write-Host "  [PASS] Secret scan clean (0 live secrets in PROJECT_ERROR.md and ACQUIREMENTS.md)." -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  PROJECT MEMORY VALIDATION: PASS" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
exit 0
