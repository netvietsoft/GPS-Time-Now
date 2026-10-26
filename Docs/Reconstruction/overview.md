# Reconstruction Overview — APK-to-Full-Stack Pipeline (`com.nextai.camera`)

> **Owner**: `Agent 1 - ARCHITECT` & `Agent 0 - ORCHESTRATOR`  
> **Machine-Readable Ledger**: [`.ai/reconstruction/ledger.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/reconstruction/ledger.json)

---

## 1. Evidence Classification Model (Section 37)

All reconstructed components strictly separate:
- **`OBSERVED`**: Directly extracted and verified from the reference APK (`v10.0.281`), 8 DEX files, 722 XML layouts, `TodayCameraDB_Impl.java` (42 tables, schema v61), 5 Retrofit interfaces (177 endpoints), and 7 ARM64 `.so` libraries (`198` `STT_FUNC`, `159` `Java_*`).
- **`INFERRED`**: Server-side implementation details of the 177 `/next/*` endpoints, Haversine geofence radius validation, and `VerifyAuth` 4-point scoring logic deduced from client request/response models.
- **`NEW_DESIGN`**:
  1. Complete rebranding to **NextAI Camera** (`com.nextai.camera`, `nextai.technology`, `nextai.global`) per `VAN-HANH/Task.txt` #2 & #3.
  2. Replacement of package-locked SenseTime `.lic` filters with OpenGL ES 3.0 GLSL 3D LUT shaders (`ADR-005`).
  3. Replacement of live ad keys with Google Official Test AdMob IDs (`VAN-HANH/Task.txt` #4).
  4. Addition of the **OLAP Analytical Star Schema** (`ADR-003`) and 7-module Admin CMS on Port `4580` (`VAN-HANH/Task.txt` #5).
  5. Addition of the dedicated **`CONVERT/games/`** directory and **HTML5 Web Games Hub** (`VAN-HANH/Task.txt` #9 & #10).

---

## 2. Core Feature Mapping Summary

| Feature ID | Source Symbols / Evidence | Target Implementation in `CONVERT/` | Status |
|---|---|---|---|
| `FEAT-CAMERA-WATERMARK` | `PreviewActivity`, `WatermarkEditFragment`, `#10011`, `#57`, `#34`, `#52` | `apps/android/.../ui/camera/PreviewScreen.kt`, `WatermarkStudioScreen.kt` | `VERIFIED` |
| `FEAT-ANTI-FAKE-STEGO` | `JpegPacker`, `CodeDetect.AntiCode`, `AegisCaptureSession` | `apps/android/app/src/main/cpp/jpeg_packer_jni.cpp`, `aegis_anti_spoof_jni.cpp` | `VERIFIED` |
| `FEAT-TEAMSPACE-SAAS` | `TeamSpaceHomeParentActivity`, `ClusterApiService` (91 endpoints) | `apps/android/.../ui/teamspace/TeamSpaceScreen.kt`, `Backend/src/routes/clusterApi.ts` | `VERIFIED` |
| `FEAT-VERIFY-AUTH` | `VerifyAuthActivity`, `VerifyAuth.android.jsbundle`, `PaddleOCR` | `apps/android/.../ui/verify/VerifyAuthScreen.kt`, `Backend/src/routes/todayApi.ts` | `VERIFIED` |
| `FEAT-EARN-EMBODIED-AI` | `EarnHomeActivity`, `ArCollectActivity`, `McapCollector`, `EarnApiService` | `apps/android/.../ui/earn/EarnRoboticsScreen.kt`, `Backend/src/routes/earnApi.ts` | `VERIFIED` |
| `FEAT-PAYWALL-ADS` | `PurchaseActivity`, `uz.p`, `wm.b`, `c40.h`, `o90.l` | `apps/android/.../ui/paywall/PurchaseScreen.kt`, `AdConfig.kt` | `VERIFIED` |
| `FEAT-EDGE-AI-CV` | `libnnpackage-f99779bd.so` (`SteelDetect`, `VLPR`, `ZeroDCE`) | `apps/android/app/src/main/cpp/edge_ai_ncnn_jni.cpp` (198 `STT_FUNC`) | `VERIFIED` |
| `FEAT-CMS-BACKEND` | `TodayCameraDB_Impl` (42 tables) + 177 API endpoints | `Backend/src/` (Port `4580` OLTP + OLAP + `/cms`) | `VERIFIED` |
| `FEAT-WEB-GAMES-HUB` | `VAN-HANH/Task.txt` #9 & #10 | `CONVERT/games/` (5 Mini-Games + `index.html` Web Hub) | `VERIFIED` |
