# NextAI Camera & Enterprise Field SaaS Platform (`com.nextai.camera`)

> **Workspace Standard**: V2.1.2 (Design-Gated + Full-Stack Reconstruction + Project Knowledge Learning Loop)  
> **Version**: `10.0.281-nextai` (`versionCode: 100028100`)  
> **Brand Identity**: `NextAI` (`next`, `nextai`, `next.ai`, `nextai.solution`, `nextai.technology`, `nextai.global`)  
> **Backend & CMS Port**: `http://localhost:4580` (`/next/*` API Gateway, Admin CMS, Web Games Hub)

---

## 1. Project Overview & Objectives

**NextAI Camera** (`com.nextai.camera`) is an industrial-grade anti-tamper GPS/NTP timestamp camera, enterprise field management SaaS (**NextAI Teamspace**), cryptographic photo verification suite (**VerifyAuth**), edge-AI computer vision toolkit (Rebar Steel Counting, License Plate Recognition, Low-Light ZeroDCE, Document/QR Scanner), embodied robotics data crowdsourcing platform (**NextAI Earn `.mcap`**), and interactive HTML5 Field Mini-Games Hub.

### Core Reconstruction Goals (`VAN-HANH/Task.txt`)
1. **Android Native & Modern UI**: Rebuilt with **Kotlin + Jetpack Compose (Material 3)** + CameraX + Room SQLite (42 tables, schema version 61) + Native C++17 JNI (`libnextai_native.so` / 7 ARM64 shared libraries, 198 `STT_FUNC`, 159 `Java_*` exports).
2. **100% Brand Sanitization**: Zero legacy brand strings; unified under `com.nextai.camera` and `nextai.technology` / `nextai.global` namespaces.
3. **AdTech Test Keys by Default**: Uses Google official AdMob test IDs (`ca-app-pub-3940256099942544~3347511713`) and sandboxed mediation rules; production credentials required before store release are tracked in [Tasksrequiring.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md).
4. **Self-Hosted Backend, CMS & Relational/OLAP Schema**: Built in `Backend/` using **TypeScript + Node.js (Express/Fastify)** running on port `4580`, implementing all 177 API endpoints (`/next/*`), relational OLTP tables (42 client parity + server SaaS tables) and OLAP analytical views, plus a full Admin CMS.
5. **100% Asset & Native Parity**: Reuses all verified shaders, LUTs, NCNN models, fonts, sounds, and watermark templates from `SOURCE/`, supplemented by local cloud storage emulation (`S3`/`R2` STS endpoints).
6. **Real Device Verification**: Configured for physical ADB wireless debugging (`192.168.1.2:35977`, pairing port `28721`, pairing code `741742`, target device **Samsung Galaxy A50**).
7. **Dedicated Games Directory & HTML5 Web Game Hub**: Standalone mini-games catalog in `games/` with an integrated HTML5 Web Game Hub.

---

## 2. Technology Stack

| Layer | Technology | Location |
|---|---|---|
| **Android Client** | Kotlin 2.0, Jetpack Compose, Material 3, CameraX, Room v61 (42 tables), Retrofit2/OkHttp3, Coroutines/Flow, OpenGL ES 3.0 LUT Shaders | [apps/android/](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android) |
| **Native C++ / Edge AI** | C++17, NDK (ARM64-v8a `ld.lld`), NCNN (`SteelDetect`, `VLPR`, `ZeroDCE`, `WatermarkRemove`), PaddleOCR, `JpegPacker` Steganography, `Aegis` Anti-Spoof, ROS2 `.mcap` | [apps/android/app/src/main/cpp/](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android/app/src/main/cpp) |
| **Backend API & OLTP/OLAP** | TypeScript 5.x, Node.js 20+, Express, SQLite3 / PostgreSQL compatible OLTP + OLAP Analytics Engine (Port `4580`) | [Backend/](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Backend) |
| **Admin CMS Portal** | TypeScript + HTML5/Tailwind SPA served by Backend (`/cms`) | [Backend/cms/](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Backend/cms) |
| **Mini-Games & Web Hub** | HTML5 Canvas / WebGL / JS Game Collection (5 Industrial & Field Puzzle Games + Unified Arcade Hub) | [games/](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/games) |
| **Design System** | NextAI Industrial High-Contrast Design System v2.1 (`390x844 dp`, Outdoor Sunlight Readability) | [design/approved/](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/approved) |

---

## 3. Quick Setup & Execution (Portable Relative Paths)

### 3.1. Start Backend Server, Admin CMS & Web Games Hub (Port `4580`)
```powershell
cd Backend
npm ci
npm run build
npm test
npm start
```
- **API Gateway Base URL**: `http://127.0.0.1:4580/nextai/v1/` (Production: `https://api.nextai.global/nextai/v1/`)
- **Health & Runtime Probes**: `http://127.0.0.1:4580/health`
- **Enterprise Admin CMS (Requires Bearer Admin Token)**: `http://127.0.0.1:4580/cms`
- **HTML5 Web Games Hub**: `http://127.0.0.1:4580/games` (or open `games/index.html`)

### 3.2. Build & Verify Android Application (`com.nextai.camera`)
```powershell
powershell -ExecutionPolicy Bypass -File .\apps\android\build_and_install_apk.ps1
```

### 3.3. Connect & Deploy to Real Test Device via ADB Reverse
```powershell
adb reverse tcp:4580 tcp:4580
adb install -r apps\android\app\build\outputs\apk\debug\TimeNow-Original-v10.0.281-arm64.apk
```

### 3.4. Run Workspace Validation, Security & Verification Suite
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\validate-project-memory.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\verify.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\test.ps1
```

### 3.5. Start Always-Awake 5-Minute `Audit-git` Watchdog
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\audit-git-watchdog.ps1 -IntervalSeconds 300
```

---

## 4. Environment Readiness Separation (`AUD-009`)

| Environment Tier | Status | Scope & Evidence |
|---|---|---|
| **`local_verified`** | `VERIFIED (15/15 PASS)` | Full Backend TypeScript build + 15-group positive/negative test suite (`AUD-001`..`AUD-009`), durable JSON disk state across restarts (`Backend/data/runtime/nextai_durable_db.json`), signed Android APK (`3.59 MB`), verified on physical Samsung Galaxy A07 (`SM-A075F`) & Samsung Galaxy A50s (`SM-A507FN`). |
| **`staging_ready`** | `READY` | GitHub Actions CI gate (`.github/workflows/ci-quality-gate.yml`), `Backend/package-lock.json` (`npm ci`), HTTPS-only Android `network_security_config.xml` (`usesCleartextTraffic="false"`), RBAC + tenant isolation + CORS origin allowlist + feature flags (`PROD-01`..`PROD-14`). |
| **`production_pending_keys`** | `PENDING CHAIRMAN LIVE KEYS` | External third-party production credentials tracked in [Tasksrequiring.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md) (Google Play Developer Service Account JSON, production AdMob IDs, live Pix/SPEI banking webhook secrets, AWS S3/Cloudflare R2 production buckets, release Play Signing keystore). |

---

## 4. Canonical Navigation & Governance Links

- **Agent Constitution (Start Protocol)**: [AGENTS.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/AGENTS.md)
- **Engineering & Coding Rules**: [Docs/rules.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/rules.md)
- **Directory Structure Reference**: [Docs/CAU_TRUC_THU_MUC.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/CAU_TRUC_THU_MUC.md)
- **Durable Error Memory**: [PROJECT_ERROR.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/PROJECT_ERROR.md)
- **Distilled Project Knowledge**: [ACQUIREMENTS.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/ACQUIREMENTS.md)
- **Production Keys Checklist (For Chairman)**: [Tasksrequiring.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md)
- **Reconstruction & Task Progress**: [UPDATETODOS.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/UPDATETODOS.md)
- **Architecture Documentation**: [Docs/Architecture/overview.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Architecture/overview.md)
- **Architecture Decision Records (ADR)**: [Docs/ADR/README.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/ADR/README.md)
- **API & Database Contracts**: [Docs/Contracts/openapi.yaml](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Contracts/openapi.yaml) | [Docs/Contracts/database-schema.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Contracts/database-schema.md)
- **Design System & Prototypes**: [Docs/Design/DESIGN_SYSTEM.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Design/DESIGN_SYSTEM.md) | [design/approved/preview_screens.html](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/approved/preview_screens.html)
- **Reconstruction Parity & Evidence**: [Docs/Reconstruction/overview.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Reconstruction/overview.md) | [Docs/Reconstruction/parity.md](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Reconstruction/parity.md)
