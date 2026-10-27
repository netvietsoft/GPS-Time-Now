# Product Changelog (`Docs/Changelog/CHANGELOG.md`)

## Audit remediation submission (`AUDIT-20261027-001` / `REV-001..006`) — 2026-10-27

- **`REV-001` (`AUD-001`)**: Hardened `center/app/login` and `auth/token` in `Backend/src/server.ts` with a durable server-side account store (`dbState.accounts`) and salted SHA-256 credential verification (`hashAccountCredential`). Denied unknown accounts (`401 ERR_UNKNOWN_ACCOUNT`), forbade client-supplied `role` or `tenantId` escalation (`403 ERR_CLIENT_ROLE_OR_TENANT_OVERRIDE_FORBIDDEN`), and added negative tests in `Backend/tests/verify_backend.js` (`[11/16]`).
- **`REV-002` (`AUD-002`)**: Set `featureFlags.billingEnabled = false` by default (fails closed with `503 ERR_BILLING_FEATURE_DISABLED`), removed local HMAC receipt signing (`createSignedPlayReceipt` / `NEXTAI_PLAY_BILLING_SECRET`), rejected client-supplied `purchaseState` and `expiryTimeMs` (`400 ERR_CLIENT_STATE_INJECTION_FORBIDDEN`), and verified purchases strictly against the server-side provider ledger (`dbState.providerPlayPurchases`).
- **`REV-003` (`AUD-006`, `AUD-007`)**: Removed the inline cleartext manifest generator and hardcoded `E:\` paths from `apps/android/build_and_install_apk.ps1`, linked the single canonical `apps/android/app/src/main/AndroidManifest.xml` (`usesCleartextTraffic="false"`, `networkSecurityConfig="@xml/network_security_config"`, `targetSdkVersion="34"`), inspected the compiled binary APK manifest with `aapt2 dump xmltree`, and integrated clean-clone APK build + verification into `scripts/verify.ps1`, `scripts/test.ps1`, and `.github/workflows/ci-quality-gate.yml`.
- **`REV-004` (`AUD-005`, `AUD-008`)**: Added cross-process file locking (`withCrossProcessFileLock`), unique per-write `.tmp` files, Windows `EPERM`/`EACCES`/`EBUSY` retry, transactional `.bak` recovery from interrupted writes, `backupDurableState`/`restoreDurableState`, schema v1->v2 migration (`migrateDurableStatePayload`), and a 6-process concurrent write stress test in `Backend/tests/verify_backend.js` (`[16/16]`).
- **`REV-005` (`AUD-004`)**: Set `featureFlags.earnEnabled = false` and `featureFlags.payoutEnabled = false` by default (fails closed with `503`), and required payout webhook callbacks (`app/reward/video/withdrawal/webhook`) to reconcile against `dbState.providerPayoutTransfers` (`422 ERR_PROVIDER_TRANSFER_RECONCILIATION_FAILED` when unmatched).
- **`REV-006` (`PAR-001..004`)**: Added `Git-audit/reference-manifest.json` with SHA-256 hashes and byte sizes of `Timemark.apk`, `timemark.db`, 7 ARM64 `.so` libraries, `WatermarkDefaultList.json`, and SQL schemas, verified automatically in `scripts/verify.ps1`.

## Independent audit review — 2026-10-27

- Added `Git-audit/REVIEW-16502ec.md` with verification results and open blockers in dev commit `16502ec` (`AUDIT-20261027-001`).

## Audit documentation — 2026-10-26

- Added `Git-audit/` with baseline findings, required evidence and a production backlog (`AUDIT-20261026-001`). This records open release blockers; no product behavior changed.
- Clarified that the product reconstructs Time mark v10.0.281; separated original-app parity evidence from NextAI production readiness in `Git-audit/PARITY-AUDIT.md`.

> **Format**: Development Workspace Standard V2.1.2 (Section 20.A)

---

## [10.0.281-nextai] — 2026-10-26

### Added
- **Android Kotlin + Jetpack Compose Application (`CONVERT/apps/android/`)**:
  - Full single-activity Material 3 Compose implementation under package `com.nextai.camera`.
  - 8 interactive screens: Camera & Live Watermark (`#10011`, `#57`, `#34`, `#52`), Watermark Studio (with AI Logo Cutout & LLM Note Generator), NextAI Teamspace SaaS, Batch Export & `JpegPacker` Unpack, `VerifyAuth` Anti-Fake Center, Dual-Mode Paywall (`NextAI Plus` & `Business Plan`), `NextAI Earn` Embodied AI `.mcap` Recorder, and Field Mini-Games Arcade.
  - Room v61 local SQLite database (`NextAICameraDB`) with all 42 tables.
  - Native C++17 JNI (`libnextai_native.so` / 7 ARM64 `.so` libraries, 198 `STT_FUNC`, 159 `Java_*` exports) for NCNN (`SteelDetect`, `VLPR`, `ZeroDCE`), `PaddleOCR`, `JpegPacker`, `Aegis`, and `.mcap` muxing.
  - Royalty-free OpenGL ES 3.0 GLSL 3D LUT shader pipeline (`ADR-005`).
- **Self-Hosted TypeScript + Node.js Backend & Admin CMS (`CONVERT/Backend/` on Port `4580`)**:
  - All 177 `/next/*` API endpoints returning `BaseResponse<T>`.
  - Dual-plane database: 42 Relational OLTP tables + 8 OLAP Star Schema analytical tables seeded with rich realistic data.
  - 7-Module Enterprise Admin CMS at `http://localhost:4580/cms`.
- **Standalone Mini-Games & HTML5 Web Games Hub (`CONVERT/games/`)**:
  - 5 playable HTML5 Canvas games (`rebar-tycoon`, `geo-patrol-defender`, `truth-detective`, `robo-hand-trainer`, `tower-crane-builder`) and unified Web Hub (`CONVERT/games/index.html` & `http://localhost:4580/games`).
- **Workspace Standard V2.1.2 Governance**:
  - Created `AGENTS.md`, `PROJECT_ERROR.md`, `ACQUIREMENTS.md`, `Tasksrequiring.md`, `UPDATETODOS.md`, `.ai/`, `.agents/`, and verification scripts.

### Changed
- **100% Rebranding to NextAI**: Replaced all legacy brand references (`xhey`, `timemark`, `oceangalaxy`, `secneo`) with `next`, `nextai`, `next.ai`, `nextai.solution`, `nextai.technology`, and `nextai.global` (`com.nextai.camera`).
- **AdTech Test Keys**: Replaced all live advertising keys with Google Official Test AdMob IDs (`ca-app-pub-3940256099942544~3347511713`).

### Fixed
- Resolved ELF64 `.dynsym` `sh_info=6` local-in-global symbol ordering for `ld.lld --fatal-warnings` (`ERR-20261026-001`).
- Resolved Windows x64 `LLP64` vs Bionic `LP64` `uintptr_t` width mismatch in NDK 28 host harness (`ERR-20261026-002`).
- Resolved LLVM 19 `_LIBCPP_EXTERN_TEMPLATE` non-member `operator+` instantiation in NDK 28 (`ERR-20261026-003`).
- Mitigated SenseTime `.lic` package-name lock on rebrand via OpenGL ES 3.0 LUT shaders (`ERR-20261026-004`).
