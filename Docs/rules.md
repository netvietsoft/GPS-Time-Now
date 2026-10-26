# Docs/rules.md — Canonical Engineering, Rebranding & Governance Rules

> **Authority**: Together with [`AGENTS.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/AGENTS.md), this file is the **Canonical Runtime Law** of the NextAI Camera (`com.nextai.camera`) workspace (`Development Workspace Standard V2.1.2`).

---

## 1. Framework & Technology Standards

- **Android Client (`apps/android/`)**:
  - Language: **Kotlin 2.0** (`jvmTarget = "17"`).
  - UI Framework: **Jetpack Compose** (Material 3) + Navigation Compose.
  - Camera & Graphics: **CameraX** + **OpenGL ES 3.0 GLSL 3D LUT Shaders** (`NextAIGlLutRenderer`).
  - Local Database: **Room SQLite v61** (`NextAICameraDB`, 42 tables).
  - Native C++ / Edge AI: **C++17** (`CMakeLists.txt` with `-fuse-ld=lld`), 7 ARM64 `.so` libraries (`198` `STT_FUNC`, `159` `Java_*`).
- **Backend API, CMS & Database (`Backend/`)**:
  - Runtime & Language: **Node.js 20+** + **TypeScript 5.x** (strict mode).
  - Server Port: **`4580`** (within `1000..10000` per `VAN-HANH/Task.txt` #5).
  - API Prefix: `/next/*` (177 endpoints returning `BaseResponse<T>`).
  - Database: **Relational OLTP** (42 client parity tables + server SaaS tables) + **OLAP Star Schema** (`fact_*` and `dim_*` analytical cubes).
- **Mini-Games & Web Hub (`games/`)**:
  - Standalone HTML5 Canvas / WebGL mini-games in `CONVERT/games/<game-id>/` aggregated by `CONVERT/games/index.html` and served at `http://localhost:4580/games`.

---

## 2. Mandatory Brand Sanitization Rules (`VAN-HANH/Task.txt` #2 & #3)

1. **Zero Legacy Brand Leakage**: Production code in `CONVERT/apps/`, `CONVERT/Backend/`, `CONVERT/games/`, and `CONVERT/design/` MUST NOT contain legacy brand identifiers (`xhey`, `timemark`, `oceangalaxy`, `secneo`).
2. **Approved NextAI Brand Tokens**:
   - Package Name / Application ID: `com.nextai.camera`
   - Internal SDK / Embodied Packages: `com.nextai.camerasdk`, `com.nextai.embodied`
   - Brand Identifiers: `next`, `nextai`, `next.ai`, `nextai.solution`, `nextai.technology`, `nextai.global`, `nextaiglobal`, `nextaitechnology`, `nextaisolution`
   - Product Titles: **NextAI Camera**, **NextAI Plus**, **NextAI Teamspace**, **NextAI VerifyAuth**, **NextAI Earn**.

---

## 3. Advertising & Sensitive Key Rules (`VAN-HANH/Task.txt` #4 & Section 16)

1. **Official Test Ad Keys Only**: All ad integrations in `CONVERT/` MUST use Google's official AdMob Test IDs (`ca-app-pub-3940256099942544~3347511713` and corresponding test unit IDs).
2. **No Hardcoded Live Secrets**: Never commit live production keys, private `.pem`/`.jks` files, or cloud secret keys.
3. **Chairman Production Checklist**: Any credential using a demo/test placeholder MUST be documented in [`Tasksrequiring.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md).

---

## 4. Coding, Naming & Documentation Conventions

- **Naming**:
  - Variables, functions, methods: `camelCase`
  - Classes, Jetpack Compose `@Composable` functions, TypeScript interfaces/types: `PascalCase`
  - Constants & SQLite/Room table names: `UPPER_SNAKE_CASE` or exact Room v61 parity snake_case (`new_watermarks_table`, `team_space_entry`, etc.)
- **Comments & Docstrings (AGENTS.md Rule #15)**:
  - Write JSDoc/KDoc for public APIs, complex business rules, cryptographic/steganographic algorithms (`JpegPacker`, `Aegis`), and non-obvious architectural decisions.
  - Comments must explain **WHY**, contracts, and edge-case risks — never repeat what the code already states.
- **Forbidden Libraries**:
  - Do not use `moment.js`, `com.secneo.apkwrapper.*`, or package-locked SenseTime `.lic` loaders (`ERR-20261026-004`).

---

## 5. Error Handling, Testing & Project Knowledge Loop

1. **Search Before Fixing**: When encountering any build, linker, runtime, or test error, agents MUST search [`PROJECT_ERROR.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/PROJECT_ERROR.md) and [`ACQUIREMENTS.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/ACQUIREMENTS.md) first.
2. **Never Weaken Tests**: Never delete, skip, or alter expected test assertions just to make a test suite green.
3. **Loop Guard**: If an issue cannot be resolved after **3 attempts** (or `max_fix_cycles: 5` in automated loops), stop immediately and report `[NEED_HUMAN_INTERVENTION]`.
4. **Update Project Memory**: Update `PROJECT_ERROR.md` and `ACQUIREMENTS.md` in the same commit whenever a reusable root cause or architectural optimization is verified.
5. **Commit Format**: Use `type(scope): subject` or `[TASK-ID][agent-name] Summary` and never report fabricated test results.
