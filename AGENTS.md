# AGENTS.md — Canonical AI Agent Constitution (Workspace Standard V2.1.2)

> **Authority**: This file (`AGENTS.md`) together with [`Docs/rules.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/rules.md) constitutes the **Canonical Runtime Law** of the NextAI Camera (`com.nextai.camera`) workspace, implementing `Development_Workspace_Standard_V2.1_Design_Gated 29-9-2026.txt` (Version `2.1.2`).

---

## 1. Mandatory Start Protocol (Read Order Before Any Action)

Every AI Agent (Claude Code, Antigravity, Codex, DeepSeek, Cline, Roo Code) MUST execute the following protocol in exact order before modifying any file:

1. **Read [`README.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/README.md)** — Understand project scope, stack, and ports.
2. **Read [`AGENTS.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/AGENTS.md)** — Load this Constitution and precedence hierarchy.
3. **Read [`Docs/rules.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/rules.md)** — Load coding, naming, rebranding, security, and testing rules.
4. **Search [`PROJECT_ERROR.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/PROJECT_ERROR.md)** — Filter by module, tag, error signature, and dependency of your task to avoid repeating past failed attempts.
5. **Read [`ACQUIREMENTS.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/ACQUIREMENTS.md)** — Apply all `VALIDATED` and `STANDARD` knowledge entries relevant to your task.
6. **Read [`.ai/project.yaml`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/project.yaml), [`.ai/agents.yaml`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/agents.yaml), and [`.ai/state.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/state.json)**.
7. **Read relevant Architecture & Contracts** in [`Docs/Architecture/`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Architecture/overview.md) and [`Docs/Contracts/`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Contracts/openapi.yaml).
8. **If UI/UX impact exists**, read [`Docs/Design/DESIGN_SYSTEM.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Design/DESIGN_SYSTEM.md) and verify `.ai/design/approved/DESIGN-0001.json`.
9. **If Reconstruction task**, read [`Docs/Reconstruction/overview.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Reconstruction/overview.md) and [`.ai/reconstruction/ledger.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/reconstruction/ledger.json).
10. **Check [`.ai/locks.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/locks.json)**, `git status`, and active branch/worktree.
11. **Read [`UPDATETODOS.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/UPDATETODOS.md), [`PROJECT_MEMORY.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/PROJECT_MEMORY.md), [`TASK_LOG.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/TASK_LOG.md), and [`LIBRARY.MD`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/LIBRARY.MD)**.
12. **Before finishing any task**, evaluate and update `PROJECT_ERROR.md` / `ACQUIREMENTS.md` in the same commit/PR if reusable error lessons or distilled patterns were discovered.

---

## 2. Runtime Context Loading Policy (Tiered Context Budget)

- **TIER 0 — ALWAYS LOAD**: `AGENTS.md`, `Docs/rules.md`, active Task record, `.ai/project.yaml`, `.ai/state.json`, `.ai/locks.json`, and matching entries in `PROJECT_ERROR.md` & `ACQUIREMENTS.md`.
- **TIER 1 — LOAD BY TASK**: Specific `Docs/Architecture/*.md`, `Docs/Contracts/*`, `Docs/ADR/*`, `Docs/Design/DESIGN_SYSTEM.md` (when `design_impact != NONE`), and `.ai/reconstruction/ledger.json`.
- **TIER 2 — LOAD ON DEMAND**: Decompiled reference artifacts in `E:\Decompiler\APP\IMAGE\Timemark\SOURCE\`, historical reports, and unrelated modules.

---

## 3. The 24 Hard Laws for All AI Agents

1. **Never modify code/config/data without a valid `TASK ID`** (or `INCIDENT ID` for emergency hotfixes; read-only audits may use `REQUEST`/`AUDIT ID`).
2. **Never modify files outside your assigned task scope** (`files_allowed` vs `files_forbidden`).
3. **Never modify a file locked by another agent** in `.ai/locks.json`.
4. **Never commit directly to `main`** during multi-agent feature loops without passing Quality Gates.
5. **Never modify tests merely to make them pass**.
6. **Never delete failing tests**.
7. **Never skip or disable tests** without explicit human approval.
8. **Never disable lint, typecheck, or security checks**.
9. **Never install new dependencies without justification** and checking `LIBRARY.MD`.
10. **Never alter system architecture without an approved ADR** in `Docs/ADR/`.
11. **Never change database schema without a migration and rollback strategy**.
12. **Never expose secrets** or commit live production credentials.
13. **Never use destructive Git commands** (`git reset --hard`, `git clean -fd`) automatically.
14. **Never run `git push --force`**.
15. **Always run verification scripts** (`scripts/verify.ps1`, `scripts/validate-project-memory.ps1`) before checkpoint commits.
16. **Always update task state** and `.ai/reconstruction/ledger.json` via atomic state rules.
17. **Always update [`Docs/Changelog/CHANGELOG.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Changelog/CHANGELOG.md)** for meaningful changes.
18. **Always create a handoff record** in `.ai/handoffs/` before ending a session.
19. **Stop autonomous fix loops after `max_fix_cycles: 5`** and escalate with `[NEED_HUMAN_INTERVENTION]`.
20. **Never guess or fabricate project state** — inspect code, schemas, and logs first. Never report fake completion.
21. **Always read `PROJECT_ERROR.md` and `ACQUIREMENTS.md`** before planning or modifying code.
22. **Never mark an error `RESOLVED` without repeatable verification and a prevention guardrail**.
23. **Always update durable project knowledge (`PROJECT_ERROR.md` / `ACQUIREMENTS.md`)** when a reusable error or engineering principle is discovered.
24. **Never store live secrets, private keys, or personal data in project-memory files**; document missing production keys in [`Tasksrequiring.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md).

---

## 4. 14-Agent Role Matrix Summary

| Agent ID | Role Name | Code Write | Primary Scope |
|---|---|---|---|
| **Agent 0** | `CEO - ORCHESTRATOR` | No | Task graph, state, locks, `UPDATETODOS.md`, `Tasksrequiring.md`, watchdog |
| **Agent 1** | `ARCHITECT` | No | System design, `Docs/Architecture/**`, `Docs/ADR/**`, `Docs/Contracts/**` |
| **Agent 2** | `BACKEND` | Yes | `Backend/src/**`, OLTP/OLAP database, 177 `/next/*` API routes, workers |
| **Agent 3** | `FRONTEND` | Yes | `apps/android/**` (Kotlin + Jetpack Compose), `Backend/cms/**`, `games/**` |
| **Agent 4** | `INTEGRATION/API` | Yes | Retrofit/OkHttp clients, S3/R2 STS tokens, MSAL, Nominatim, Webhooks |
| **Agent 5** | `PAYMENT` | Yes | Google Play Billing (`uz.p` equivalent), Stripe checkout, Pix/SPEI payouts |
| **Agent 6** | `TESTER` | Tests Only | `tests/**`, Android/Backend unit & parity tests, ADB device test (`192.168.1.2:35977`) |
| **Agent 7** | `FIXER` | Yes | Scoped bug fixes from `bug_report.json` without weakening tests |
| **Agent 8** | `REVIEWER` | No | Code, architecture, concurrency, API contract, and parity review |
| **Agent 9** | `SECURITY` | No | Secret scan, SQLi/XSS/CSRF, Aegis signature verification, JWT & STS audit |
| **Agent 10** | `DEVOPS` | Yes | `scripts/**`, build automation, port `4580` runner, deployment runbooks |
| **Agent 11** | `DESIGNER` | Prototypes | `design/**`, `.ai/design/**`, `Docs/Design/DESIGN_SYSTEM.md` |
| **Agent 12** | `DOCUMENTATION` | Docs Only | `README.md`, `Docs/**`, `PROJECT_MEMORY.md`, `TASK_LOG.md`, `CHANGELOG.md` |
| **Agent 13** | `ADVERSARY_AUDITOR` | Audit Only | Red-team verification, AST/code parity audit against `SOURCE/`, 6-gate challenges |

---

## 5. Conflict Precedence Hierarchy

When two instructions appear to conflict, resolve strictly in this order:
1. **Security / Legal / Compliance & Workspace Boundary Rules**
2. **`AGENTS.md` and `Docs/rules.md` (Canonical Runtime Law)**
3. **Approved ADR / OpenAPI Contract / Database Schema (`Docs/ADR/`, `Docs/Contracts/`)**
4. **Verified Tests & Runtime Behavior on current version**
5. **`ACQUIREMENTS.md` (`STANDARD` / `VALIDATED` entries)**
6. **`PROJECT_ERROR.md` (`RESOLVED` entries)**
7. **`CANDIDATE` / `INVESTIGATING` entries or tool-specific adapters (`.agents/`, `CLAUDE.md`, `.clinerules`)**
