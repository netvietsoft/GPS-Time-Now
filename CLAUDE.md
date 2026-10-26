# CLAUDE.md — Claude Code Execution Adapter

> **Adapter Notice**: Per `Development Workspace Standard V2.1.2` (Section 2 & Section 3), this file is a thin tool-specific pointer and **NOT** a separate set of rules.

Before performing any action, Claude Code MUST follow the **Mandatory Start Protocol**:

1. Read [`AGENTS.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/AGENTS.md) (Canonical AI Agent Constitution).
2. Read [`Docs/rules.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/rules.md) (Engineering, Rebranding, and Security Rules).
3. Search [`PROJECT_ERROR.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/PROJECT_ERROR.md) and [`ACQUIREMENTS.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/ACQUIREMENTS.md) for relevant `ERR-*` and `ACQ-*` entries.
4. Check [`.ai/project.yaml`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/project.yaml), [`.ai/state.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/state.json), and [`.ai/locks.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/locks.json).
5. Ensure a valid `TASK ID` exists before modifying any file, stay strictly within `files_allowed`, never weaken tests to make them pass, and run `.\scripts\verify.ps1` before finishing.
