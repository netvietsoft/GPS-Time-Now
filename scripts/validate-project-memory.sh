#!/usr/bin/env bash
# ============================================================================
# validate-project-memory.sh — Project Knowledge Validation Gate (Section 57.10)
# Standard: Development Workspace Standard V2.1.2
# ============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONVERT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ERR_FILE="$CONVERT_ROOT/PROJECT_ERROR.md"
ACQ_FILE="$CONVERT_ROOT/ACQUIREMENTS.md"

echo "============================================================================"
echo "  PROJECT KNOWLEDGE VALIDATION GATE (Standard V2.1.2 Section 57.10)"
echo "============================================================================"

[[ -f "$ERR_FILE" ]] || { echo "Missing $ERR_FILE"; exit 1; }
[[ -f "$ACQ_FILE" ]] || { echo "Missing $ACQ_FILE"; exit 1; }

grep -Eq "^## ERR-[0-9]{8}-[0-9]{3}" "$ERR_FILE" || { echo "No valid ERR-ID in PROJECT_ERROR.md"; exit 1; }
grep -Eq "^### Verification" "$ERR_FILE" || { echo "Missing ### Verification in PROJECT_ERROR.md"; exit 1; }
grep -Eq "^### Prevention / Guardrail" "$ERR_FILE" || { echo "Missing ### Prevention / Guardrail in PROJECT_ERROR.md"; exit 1; }

grep -Eq "^## ACQ-[0-9]{8}-[0-9]{3}" "$ACQ_FILE" || { echo "No valid ACQ-ID in ACQUIREMENTS.md"; exit 1; }
grep -Eq "^### When to use" "$ACQ_FILE" || { echo "Missing ### When to use in ACQUIREMENTS.md"; exit 1; }
grep -Eq "^### When not to use" "$ACQ_FILE" || { echo "Missing ### When not to use in ACQUIREMENTS.md"; exit 1; }
grep -Eq "^### Evidence / Verification" "$ACQ_FILE" || { echo "Missing ### Evidence / Verification in ACQUIREMENTS.md"; exit 1; }

if grep -Eq "BEGIN (RSA |EC )?PRIVATE KEY|sk_live_[0-9a-zA-Z]{24}" "$ERR_FILE" "$ACQ_FILE"; then
  echo "Live secret detected in project knowledge files!"
  exit 1
fi

echo "  [PASS] PROJECT_ERROR.md and ACQUIREMENTS.md passed all V2.1.2 checks."
exit 0
