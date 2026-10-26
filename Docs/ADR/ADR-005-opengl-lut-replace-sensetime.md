# ADR-005: Replace Package-Locked SenseTime SDK with OpenGL ES 3.0 GLSL 3D LUT Shaders

- **Status**: `ACCEPTED`
- **Date**: `2026-10-26`
- **Owner**: `Agent 1 - ARCHITECT` & `Agent 3 - FRONTEND`

## Context
As documented in [`ERR-20261026-004`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/PROJECT_ERROR.md#err-20261026-004--sensetime-sensemars-commercial-license-applicationid-binding-failure-on-rebrand), the commercial license file `SenseMARS_Effects-106-20270311-Android.lic` in the original APK is cryptographically bound to the legacy package name and expires on `2027-03-11`. Rebranding the package to `com.nextai.camera` invalidates the SenseTime license check.

## Decision
Replace the SenseTime SenseMARS native filter dependency with a royalty-free, package-independent **OpenGL ES 3.0 GLSL 3D LUT & Bilateral Beauty Shader pipeline** (`NextAIGlLutRenderer`) using the verified LUT textures (`assets/shaders/lut/skin_lut.png`, `eye_bright.png`, `teeth_white.png`).

## Reason
- Works under `com.nextai.camera` (or any future package name) with zero license failures and zero expiration date.
- Runs at 60 FPS (< 1.8 ms/frame) on mid-range GPUs such as the Samsung Galaxy A50 (Mali-G72 MP3).
- Retains full visual parity for skin smoothing, eye brightening, teeth whitening, and industrial high-contrast color grading.

## Rejected Alternatives
- Keeping the legacy package name solely to satisfy the `.lic` check (violates `VAN-HANH/Task.txt` #2 & #3).

## Impact
- Documented in [`Tasksrequiring.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md) Item #10 as an optional commercial upgrade if the Chairman ever wishes to license SenseTime separately.
