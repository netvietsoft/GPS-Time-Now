# ADR-004: Complete Brand Sanitization & NextAI Namespace Migration (`com.nextai.camera`)

- **Status**: `ACCEPTED`
- **Date**: `2026-10-26`
- **Owner**: `Agent 1 - ARCHITECT`

## Context
Requirements 2 & 3 of `VAN-HANH/Task.txt` mandate stripping all legacy brand identifiers from namespaces, classes, functions, comments, folders, packages, and APIs, and replacing them with NextAI brand tokens: `next`, `nextai`, `next.ai`, `nextai.solution`, `nextai.technology`, `nextai.global`, `nextaiglobal`, `nextaitechnology`, `nextaisolution`.

## Decision
Adopt the following canonical brand mapping across the entire `CONVERT/` workspace:
- **Android Application ID & Root Package**: `com.nextai.camera`
- **Camera SDK Package**: `com.nextai.camerasdk`
- **Embodied AI Package**: `com.nextai.embodied`
- **API Route Prefix**: `/next/` (matches the approved brand token `next`)
- **Cloud & Web Domains**: `api.nextai.technology`, `adx.nextai.technology`, `teamspace.nextai.global`, `cdn.nextai.global`, `verify.nextai.solution`
- **Anti-Fake Code Prefix**: `NEXT9X-...`

## Reason
- Eliminates all trademark and brand dependencies on the original application.
- Keeps `/next/*` endpoint paths clean and consistent across Android, Backend, and CMS.

## Rejected Alternatives
- Changing only visible UI strings while leaving legacy Java/Kotlin package paths intact.

## Impact
- Verified by automated brand-hygiene check in `scripts/verify.ps1` (`ACQ-20261026-004`).
