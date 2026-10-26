# Antigravity Design Gate Adapter (`.agents/rules/design-gate.md`)

> **Adapter Notice (Section 53.B)**: Thin adapter pointing to the canonical Design Governance pipeline.

1. When a task has `design_impact` of `LOW`, `MEDIUM`, or `HIGH`, load [`Docs/Design/DESIGN_SYSTEM.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Design/DESIGN_SYSTEM.md) and check [`.ai/design/approved/DESIGN-0001.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/design/approved/DESIGN-0001.json).
2. Do NOT perform production UI implementation for `MEDIUM` or `HIGH` impact tasks unless a valid `APPROVED` record exists in `.ai/design/approved/`.
3. Never treat internal model reasoning as a substitute for human design approval.
