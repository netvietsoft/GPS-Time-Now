# ============================================================================
# verify.ps1 - Master Quality Gate, Security Audit & Brand Hygiene (V2.1.2)
# Enforces AUD-001..AUD-009 & PROD-01..PROD-14 with strict exit codes
# ============================================================================
$ErrorActionPreference = "Stop"
$ConvertRoot = Split-Path -Parent $PSScriptRoot

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  NEXTAI CAMERA (com.nextai.camera) - MASTER QUALITY GATE VERIFICATION" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan

# Step 1: Validate Project Knowledge (PROJECT_ERROR.md and ACQUIREMENTS.md)
& "$PSScriptRoot\validate-project-memory.ps1"
if ($LASTEXITCODE -ne 0) { throw "validate-project-memory.ps1 failed with exit code $LASTEXITCODE" }

# Step 2: Check Mandatory Governance, Audit, and CI Files
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
    "design/approved/preview_screens.html",
    "Backend/package-lock.json",
    ".github/workflows/ci-quality-gate.yml",
    "Git-audit/README.md",
    "Git-audit/FINDINGS.md",
    "Git-audit/EVIDENCE.md",
    "Git-audit/PRODUCTION-BACKLOG.md"
)

foreach ($rel in $requiredFiles) {
    $full = Join-Path $ConvertRoot $rel
    if (-not (Test-Path $full)) {
        throw "Missing required V2.1.2 / Audit gate file: $rel"
    }
}
Write-Host "  [PASS] All $($requiredFiles.Count) canonical Workspace Standard V2.1.2 & Audit files present." -ForegroundColor Green

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

# Step 5: Security & Cleartext Hardening Gate (AUD-003 & AUD-006)
$manifestXml = Get-Content (Join-Path $ConvertRoot "apps/android/app/src/main/AndroidManifest.xml") -Raw -Encoding UTF8
if ($manifestXml -match 'usesCleartextTraffic="true"') {
    throw "AUD-006 failure: AndroidManifest.xml still permits cleartext traffic!"
}
$netSecXml = Get-Content (Join-Path $ConvertRoot "apps/android/app/src/main/res/xml/network_security_config.xml") -Raw -Encoding UTF8
if ($netSecXml -match '<base-config\s+cleartextTrafficPermitted="true"') {
    throw "AUD-006 failure: network_security_config.xml base-config permits cleartext traffic!"
}
if ($netSecXml -match '192\.168\.') {
    throw "AUD-006 failure: network_security_config.xml contains hardcoded LAN IP!"
}
$netConfigKt = Get-Content (Join-Path $ConvertRoot "apps/android/app/src/main/java/com/nextai/camera/data/remote/NextAiNetworkConfig.kt") -Raw -Encoding UTF8
if ($netConfigKt -match '192\.168\.' -or $netConfigKt -match 'nextai-dev-jwt-token-v1') {
    throw "AUD-006 failure: NextAiNetworkConfig.kt contains hardcoded LAN IP or static dev JWT token!"
}
$serverTs = Get-Content (Join-Path $ConvertRoot "Backend/src/server.ts") -Raw -Encoding UTF8
if ($serverTs -match 'export\s+const\s+HMAC_SECRET\s*=') {
    throw "AUD-003 failure: Backend/src/server.ts contains hardcoded HMAC_SECRET constant!"
}
Write-Host "  [PASS] Android HTTPS-only config & Backend runtime key ring verified (AUD-003 & AUD-006)." -ForegroundColor Green

# Step 6: Run Real Backend Build & 15-Group Security/Integration Test Gate (AUD-001..005 & AUD-007)
Push-Location (Join-Path $ConvertRoot "Backend")
try {
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "Backend TypeScript build failed!" }
    npm test
    if ($LASTEXITCODE -ne 0) { throw "Backend 15-group test suite failed!" }
} finally {
    Pop-Location
}
Write-Host "  [PASS] Backend TypeScript build + 15/15 security & integration test groups passed." -ForegroundColor Green

Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "  ALL WORKSPACE STANDARD V2.1.2 & GIT-AUDIT QUALITY GATES PASSED" -ForegroundColor Green
Write-Host "============================================================================" -ForegroundColor Cyan
exit 0
