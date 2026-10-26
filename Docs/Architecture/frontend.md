# Frontend Architecture — Android Kotlin + Jetpack Compose & HTML5 Web Hub

> **Owner**: `Agent 1 - ARCHITECT` & `Agent 3 - FRONTEND`  
> **Package**: `com.nextai.camera`  
> **Related ADR**: [`ADR-001`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/ADR/ADR-001-kotlin-jetpack-compose.md), [`ADR-005`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/ADR/ADR-005-opengl-lut-replace-sensetime.md)

---

## 1. Android Application Layering (`CONVERT/apps/android/`)

The Android application replaces legacy fragmented XML/React-Native hybrids with a unified, single-activity **Kotlin 2.0 + Jetpack Compose (Material 3)** architecture following Unidirectional Data Flow (UDF):

```text
com.nextai.camera/
├── NextAICameraApp.kt                 # Application entry point (Clean replacement of SecNeo AW wrapper)
├── MainActivity.kt                    # Single-Activity Compose Host + Navigation Graph
├── ui/
│   ├── theme/                         # NextAI Industrial High-Contrast Design Tokens (#2563EB, #10B981, #F59E0B)
│   ├── camera/PreviewScreen.kt        # CameraX Viewfinder + Live Watermark #10011 + Mini-Map Compass + Aegis Badge
│   ├── studio/WatermarkStudioScreen.kt# PGC/UGC Template Store + AI Logo Cutout + LLM Prompt Generator
│   ├── teamspace/TeamSpaceScreen.kt   # B2B Projects, S3 Quota, Geofence Timeclock, Photo Stream, Checklists
│   ├── export/ExportUnpackScreen.kt   # Batch PDF / Excel .xlsx / Earth .kmz Export + JpegPacker Unpack
│   ├── verify/VerifyAuthScreen.kt     # 4-Point Photo Authenticity Inspector (Replacing VerifyAuth RN Bundle)
│   ├── paywall/PurchaseScreen.kt      # Dual-Tab Paywall (NextAI Plus & Business Plan) + Exit Retention Survey
│   ├── earn/EarnRoboticsScreen.kt     # Embodied AI .mcap Recorder (21 Hand Landmarks + 4-IMU + Pix/SPEI Payout)
│   └── games/GamesArcadeScreen.kt     # Embedded WebView/Native Bridge to NextAI Field Mini-Games Hub
├── gl/NextAIGlLutRenderer.kt          # OpenGL ES 3.0 GLSL 3D LUT Shader Pipeline (skin_lut, eye_bright, teeth_white)
├── data/
│   ├── local/NextAICameraDatabase.kt  # Room v61 Database with all 42 Entities & DAOs
│   └── remote/NextAIApiService.kt     # Retrofit2 interfaces for all 177 /next/* endpoints
└── nativebridge/NextAINativeBridge.kt # JNI bindings to C++17 NCNN, JpegPacker, Aegis, PaddleOCR, and McapCollector
```

---

## 2. Screen-to-Legacy Parity Mapping

| Screen Flow | Legacy Activity / Bundle | Reconstructed Compose Screen | Key Components & State |
|---|---|---|---|
| **1. Camera & Live Watermark** | `PreviewActivity` | `PreviewScreen.kt` | `topControlBar`, `viewfinderCanvas`, `miniMapCompassWidget`, `liveWatermarkCard` (`#10011`), `antiFakeCodeStrip` |
| **2. Watermark Studio** | `WatermarkEditFragment` | `WatermarkStudioScreen.kt` | Template grid (`#10011`, `#57` Steel AI, `#34` Geofence, `#52` VLPR), field toggles, AI logo cutout |
| **3. Teamspace SaaS** | `TeamSpaceHomeParentActivity` | `TeamSpaceScreen.kt` | S3 storage bar, Geofence radius check (`32m / 150m`), OCR photo timeline |
| **4. Export & Unpack** | `LocalPreviewActivity` | `ExportUnpackScreen.kt` | `JpegPacker` lossless watermark remover + PDF/XLSX/KMZ/Collage batch exporter |
| **5. VerifyAuth Center** | `VerifyAuthActivity` (RN) | `VerifyAuthScreen.kt` | 4-point cryptographic & OCR inspection + PDF certificate generator |
| **6. Dual Paywall** | `PurchaseActivity` | `PurchaseScreen.kt` | `PAGE_STATUS_PRO` (`NextAI Plus`) & `PAGE_STATUS_BUSINESS` + `PaywallExitSurvey` |
| **7. Earn Robotics AI** | `EarnHomeActivity` / `ArCollectActivity` | `EarnRoboticsScreen.kt` | MediaPipe 21 hand landmarks overlay, 200Hz 4-IMU `.mcap` muxer, BRL Pix / MXN SPEI payout |
| **8. Mini-Games Hub** | New Requirement (`Task.txt` #9 & #10) | `GamesArcadeScreen.kt` & `CONVERT/games/` | 5 HTML5 Canvas industrial & field games + unified leaderboard |
