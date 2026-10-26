# Product Changelog (`Docs/Changelog/CHANGELOG.md`)

## Audit documentation — 2026-10-26

- Added `Git-audit/` with baseline findings, required evidence and a production backlog (`AUDIT-20261026-001`). This records open release blockers; no product behavior changed.

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
