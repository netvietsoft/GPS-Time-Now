# Independent review of dev commit `16502ec`

Review ID: `AUDIT-20261027-001`

Reviewed: 2026-10-27

Decision: **CHANGES REQUESTED — do not close AUD-001, AUD-002, AUD-004, AUD-005, AUD-006, AUD-007, AUD-008 or PAR-001..004 yet.** The other items also remain unverified unless independently retested against their acceptance criteria.

The dev commit contains substantial improvements. This review distinguishes passing local tests from production proof. Paths and line numbers below refer to `16502ec`.

## Verification performed

- Fast-forwarded `main` from `0ef6345` to `16502ec`; working tree clean.
- `Backend`: `npm ci --ignore-scripts --no-audit --no-fund`, `npm run build`, `npm test` passed (15/15 groups) when run alone.
- `scripts/verify.ps1` passed when run alone. When `npm test` and `scripts/verify.ps1` ran concurrently, one process crashed with `EPERM` while renaming `Backend/data/runtime/nextai_durable_db.json.tmp` to the shared JSON database. The two tests were deliberately launched concurrently for this check; the result does not negate the sequential pass, but demonstrates unsafe shared-file operation.
- Independent HTTP request without a token to `POST /nextai/v1/auth/token` with `{"userId":"audit_unknown_user","role":"super_admin","tenantId":"foreign_tenant"}` returned HTTP 200, `role=super_admin`, `tenantId=foreign_tenant` and an access token. The token value was not logged.
- No Gradle wrapper or APK artifact exists in this checkout at `apps/android/gradlew.bat` or `apps/android/app/build/outputs/apk/debug/TimeNow-Original-v10.0.281-arm64.apk`. Android build/device claims were not reproduced here.

## Blocking findings

### REV-001 — P0 — Public login issues administrator tokens from client input (`AUD-001`)

`Backend/src/server.ts:915` marks `auth/token` public. At `:1392-1400`, the handler accepts `userId`, `role` and `tenantId` from the request, defaulting to `super_admin`, then signs a valid token. The later 401/403 tests only exercise tokens already issued by this server; they do not establish a trusted identity. A caller can request an admin role and another tenant without proving ownership.

**Required:** authenticate credentials or a trusted identity provider before issuing tokens; derive role and tenant server-side from durable account records; deny unknown users. Add an unauthenticated request asking for `super_admin` and a foreign `tenantId` to the negative tests. Close `AUD-001` only when it is rejected.

### REV-002 — P0 — Play purchase is verified with a server-local HMAC, not Google Play (`AUD-002`)

`Backend/src/server.ts:438-448` signs a synthetic receipt using `NEXTAI_PLAY_BILLING_SECRET`; `:1530-1668` accepts that signature as purchase proof. It also takes `purchaseState` and `expiryTimeMs` from the caller. `Backend/tests/verify_backend.js` calls the same exported signing helper to create its "valid" receipt. This proves the local protocol, not a Google Play purchase. Billing defaults to enabled at `server.ts:696`.

**Required:** validate purchase tokens with Google Play Developer API in sandbox/production, use provider-sourced state and expiry, bind purchase to the authenticated account, and process refund/revocation from provider data. Until then, billing should fail closed outside a clearly isolated simulator. Keep `AUD-002` open.

### REV-003 — P1 — Android security gate checks a different manifest from the build script (`AUD-006`, `AUD-007`)

`scripts/verify.ps1` and `.github/workflows/ci-quality-gate.yml` inspect `apps/android/app/src/main/AndroidManifest.xml`, which disallows cleartext. Yet `apps/android/build_and_install_apk.ps1:18,46,68` uses a hard-coded `E:\...` path and generates another manifest with `targetSdkVersion="32"` and `android:usesCleartextTraffic="true"`. The claimed signed APK is absent from this checkout. The CI workflow builds/tests Backend only and greps Android source files; it does not compile or inspect the Android artifact.

**Required:** choose one reproducible Android build path, generate release and debug artifacts from reviewed configuration, inspect the packaged manifest with Android tooling, and run Android build/tests in CI. Do not claim HTTPS-only release from source-file grep alone.

### REV-004 — P1 — Shared JSON state is not a production database (`AUD-005`, `AUD-008`)

`Backend/src/server.ts:727-728` writes one fixed `.tmp` file and renames it to the database path. Concurrent test processes collided with `EPERM`; there is no transaction, locking, multi-instance coordination or durable database migration. Sequential restart persistence is useful, but insufficient for concurrent production writes. `scripts/test.ps1` also requires an APK artifact absent from a fresh clone, while CI does not build it.

**Required:** use a transactional datastore and migration/backup/restore tests, or explicitly restrict this JSON store to single-process local demo. Test simultaneous writes and restart after interrupted write. Make the full test command reproducible from a clean clone.

### REV-005 — P1 — Payout remains a local provider simulation (`AUD-004`)

`Backend/src/server.ts:1890-1995` records a withdrawal as `PENDING_PROVIDER` and later accepts a signed local webhook, but the commit does not contain a production provider integration or reconciliation with a provider transfer. `earnEnabled` defaults to true at `:695`. This improves the previous immediate-success behavior, but does not prove real-money payout.

**Required:** keep payouts disabled outside an isolated sandbox until provider integration, signed provider callbacks, reconciliation, failure/refund handling and operational evidence exist. Close `AUD-004` only for a clearly scoped disabled release or a verified end-to-end provider flow.

### REV-006 — P1 — Parity submission does not yet contain independent reference evidence (`PAR-001..004`)

`Git-audit/PARITY-AUDIT.md` adds narrative and links to existing screenshots/schema/tests, but does not supply the original Time mark APK hash or a paired, reproducible original-versus-reconstruction run for camera/video/UI/data on the same conditions. Backend tests cannot establish equivalence with an unavailable original server. Retain `NEEDS EVIDENCE` for `PAR-001..004`.

**Required:** provide the versioned original artifact manifest/hash, paired fixtures and device outputs, case-by-case differences, and an auditor rerun. Mark inferred backend behavior separately from observed APK behavior.

## Review rule

The dev team's `READY_FOR_AUDITOR` labels are submissions, not closure. Keep the production release gate closed until each required fix and its negative tests are independently reproduced on the corresponding commit. Do not commit production keys, real purchase tokens or personal data as evidence.
