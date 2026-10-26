# ============================================================================
# verify.ps1 - Master Quality Gate and Brand Hygiene Verification (Standard V2.1.2)
# ============================================================================
$ErrorActionPreference = "Stop"
$ConvertRoot = Split-Path -Parent $PSScriptRoot

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  NEXTAI CAMERA (com.nextai.camera) - MASTER QUALITY GATE VERIFICATION" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan

# Step 1: Validate Project Knowledge (PROJECT_ERROR.md and ACQUIREMENTS.md)
& "$PSScriptRoot\validate-project-memory.ps1"

# Step 2: Check Mandatory Governance and Architecture Files
$requiredFiles = @(
    "README.md",
    "AGENTS.md",
    "PROJECT_ERROR.md",
    "ACQUIREMENTS.md",
    "PROJECT_MEMORY.md",
    "TASK_LOG.md",
    "UPDATETODOS.md",
    "Tasksrequiring.md",
    "LIBRARY.MD",
    "CLAUDE.md",
    ".clinerules",
    ".gitignore",
    ".env.example",
    ".ai/project.yaml",
    ".ai/agents.yaml",
    ".ai/state.json",
    ".ai/locks.json",
    ".ai/reconstruction/ledger.json",
    ".ai/design/approved/DESIGN-0001.json",
    "Docs/rules.md",
    "Docs/Contracts/openapi.yaml",
    "Docs/Contracts/database-schema.md",
    "Docs/Design/DESIGN_SYSTEM.md",
    "design/approved/design_tokens.json",
    "design/approved/preview_screens.html"
)

foreach ($rel in $requiredFiles) {
    $full = Join-Path $ConvertRoot $rel
    if (-not (Test-Path $full)) {
        throw "Missing required V2.1.2 file: $rel"
    }
}
Write-Host "  [PASS] All $($requiredFiles.Count) canonical Workspace Standard V2.1.2 files present." -ForegroundColor Green

# Step 3: Validate JSON files in .ai/ and design/
$jsonFiles = @(
    ".ai/state.json",
    ".ai/locks.json",
    ".ai/reconstruction/ledger.json",
    ".ai/design/approved/DESIGN-0001.json",
    "design/approved/design_tokens.json"
)
foreach ($jf in $jsonFiles) {
    $parsed = Get-Content (Join-Path $ConvertRoot $jf) -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($null -eq $parsed) { throw "Invalid JSON in $jf" }
}
Write-Host "  [PASS] All .ai/ and design/ JSON state and token records are valid." -ForegroundColor Green

# Step 4: Verify Rebranded Design Artifacts have zero legacy brand strings
$designHtml = Get-Content (Join-Path $ConvertRoot "design/approved/preview_screens.html") -Raw -Encoding UTF8
$designJson = Get-Content (Join-Path $ConvertRoot "design/approved/design_tokens.json") -Raw -Encoding UTF8
foreach ($legacy in @("timemark", "xhey", "oceangalaxy", "secneo")) {
    if ($designHtml -match "(?i)$legacy" -or $designJson -match "(?i)$legacy") {
        throw "Legacy brand token '$legacy' found in design/approved/ artifacts!"
    }
}
Write-Host "  [PASS] Rebranded design artifacts contain 0 legacy brand references." -ForegroundColor Green

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  ALL WORKSPACE STANDARD V2.1.2 QUALITY GATES PASSED" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
exit 0
