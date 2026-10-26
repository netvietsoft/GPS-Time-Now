# ADR-001: Rebuild Android Client with Kotlin 2.0, Jetpack Compose & CameraX

- **Status**: `ACCEPTED`
- **Date**: `2026-10-26`
- **Owner**: `Agent 1 - ARCHITECT` & `Agent 3 - FRONTEND`

## Context
Requirement 1 of `VAN-HANH/Task.txt` mandates rebuilding the application using **Kotlin + Jetpack Compose + optimal modern choices**. The legacy APK mixed 722 XML layouts, obfuscated Java classes, SecNeo wrapper stubs, and two embedded React Native JS bundles (`VerifyAuth.android.jsbundle` and `questionaire.android.jsbundle`), causing memory overhead and slow startup.

## Decision
Rebuild the entire Android application in `CONVERT/apps/android/` using **Kotlin 2.0 + Jetpack Compose (Material 3)**, **CameraX**, **Room v61** (42 tables), **Retrofit2/OkHttp3**, and **C++17 JNI** (`libnextai_native.so`). Convert the legacy React Native `VerifyAuth` and `Questionnaire` bundles into native Jetpack Compose screens (`VerifyAuthScreen.kt`).

## Reason
- Eliminates React Native bridge overhead (~28 MB RAM savings) and 722 fragmented XML layouts.
- Provides reactive state management (`StateFlow`) for real-time 1 Hz atomic clock updates, GPS DMS coordinates, and NCNN rebar detection overlays.
- Directly satisfies Requirement 1 of `VAN-HANH/Task.txt`.

## Rejected Alternatives
- 1:1 mechanical Java-to-Kotlin translation of obfuscated classes (forbidden by Standard V2.1.2 Section 36).
- Retaining legacy React Native bundles alongside XML views.

## Impact
- Clean modular package hierarchy under `com.nextai.camera.*`.
- 100% UI adherence to the approved `390 × 844 dp` design specification (`DESIGN-0001`).
