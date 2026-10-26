---
name: design-mode
description: Canonical adapter for NextAI Camera Design Gate workflow and Industrial High-Contrast Design System
---

# Design Mode Skill Adapter (`.agents/skills/design-mode/SKILL.md`)

> **Adapter Notice (Section 53.D)**: Delegates all design tokens, viewport rules, and approval workflows to the canonical workspace files.

1. Load canonical tokens and specifications from [`Docs/Design/DESIGN_SYSTEM.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Design/DESIGN_SYSTEM.md) and [`design/approved/design_tokens.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/approved/design_tokens.json).
2. Inspect the interactive reference prototype at [`design/approved/preview_screens.html`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/approved/preview_screens.html).
3. Store all machine-readable design state strictly inside `.ai/design/` (`DESIGN-0001.json`).
