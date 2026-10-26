# PROJECT_MEMORY.md — Operational Workspace Memory

> **Role**: Operational runtime context for active agents (`Development Workspace Standard V2.1.2`, Section 57.2).  
> **For Durable Error Memory**: See [`PROJECT_ERROR.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/PROJECT_ERROR.md).  
> **For Distilled Reusable Knowledge**: See [`ACQUIREMENTS.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/ACQUIREMENTS.md).

---

## 1. Active Project Context

- **Project Name**: Timemark Original Baseline (`v10.0.281`) & NextAI Camera Full-Stack Reconstruction
- **Package / Namespace**: `com.nextai.camera`
- **Workspace Profile**: `RECONSTRUCTION` + `PRODUCT` (`Design-Gated: true`)
- **Backend / CMS / Games Hub Port**: `4580` (`http://localhost:4580`)
- **Physical Test Devices (Verified via Wireless ADB TLS)**:
  1. **Samsung Galaxy A07 (`SM-A075F`)** — Android 16 (API 36), Serial `R83L80E1LXX`, IP `192.168.1.11:35161` (paired via port `39327`, code `735909`)
  2. **Samsung Galaxy A50s (`SM-A507FN`)** — Android 11 (API 30), Serial `R58MA581ZZA`, IP `192.168.1.2:35977` (paired via port `28721`, code `741742`)
- **Critical Workflow & UI Rule (User Directive)**:
  - *"Quá nhiều chi tiết tạp nham thêm vào. Tôi cần giao diện gốc của app gốc, ghép chuẩn rồi mới thiết kế đc app mới"*
  - **Rule**: Maintain a **100% pure 1:1 Original Timemark UI (`v10.0.281`)** stitched directly from `decompiled/resources/res/` with **zero non-original clutter** (no debug top headers, no extra game buttons on the camera screen, no custom banners) so the original app baseline is verified first before designing any new app UI.

---

## 2. Core Reconstructed Subsystems

1. **1:1 Original Timemark Android App Baseline (`CONVERT/apps/android/`)**:
   - **Signed APK Artifact**: [`Timemark-Original-v10.0.281-arm64.apk`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android/app/build/outputs/apk/debug/Timemark-Original-v10.0.281-arm64.apk) (`3.56 MB`), built via [`build_and_install_apk.ps1`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android/build_and_install_apk.ps1) using persistent [`nextai_debug.keystore`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android/nextai_debug.keystore).
   - **Verified Live Hardware Screenshots**:
     - Samsung Galaxy A07 (`SM-A075F`): [`samsung_a07_original_camera.png`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/screenshots/samsung_a07_original_camera.png)
     - Samsung Galaxy A50s (`SM-A507FN`): [`samsung_a50_original_camera.png`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/screenshots/samsung_a50_original_camera.png)
   - **Authentic Decompiled Resources Integrated (`66` `.webp`/`.png` drawables + `16` fonts + `4` shutter MP3s)**:
     - Top Camera Bar (`view_preview_title_normal_global.xml`, `52dp` white bar): `home_tool_border_menu.png` + `vip_crown.webp`, `flash_off_dark.webp` / `flash_auto_dark.webp` / `flash_on_dark.webp`, Ratio pill (`3:4`, `9:16`, `1:1`, `Full` with `ratio_pop_view_*.webp`), `tool_border_safe_green.webp` (Verify Center), `img_camera_switch.webp`.
     - Live Camera2 Viewfinder (`activity_camera_preview.xml`): Real hardware `Camera2` preview + live `LocationManager` & `Geocoder` street address + draggable/clickable live watermark overlay (`BebasNeue.otf` / `Segment7Standard.otf` / `RobotoMono-Bold.ttf` + `ic_watermark_ltr.xml` + `Photo Code` strip) + zoom pills (`2`, `1x`, `.6`).
     - Bottom Camera Action Bar (`layout_preview_bottom_fun_global.xml` + `view_preview_bottom_camera_action_global.xml`): Mode strip (`Video`, `Photo`, `Attendance`, `Checklist`) and 4 bottom controls (`Preview` with live thumbnail, `Teamspace` `teamspace_shoot.webp`, `cam_shoot.webp` / `cam_shoot_video.webp` `80dp` shutter, `Templates` `icon_preview_folder.webp` + `"Watermark"`).
   - **4 Core Java Classes (`CONVERT/apps/android/app/src/main/java/com/nextai/camera/`)**:
     - [`NextAiApplication.java`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android/app/src/main/java/com/nextai/camera/NextAiApplication.java): Loads the 7 ARM64 `.so` libraries (`libjpeg.so`, `libmp3lame.so`, `libffmpeg.so`, `libxheymediav8.so`, `libxhey_videoedit.so`, `libnative-lib.so`, `libnnpackage-f99779bd.so`).
     - [`MainActivity.java`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android/app/src/main/java/com/nextai/camera/MainActivity.java): Implements the exact original `PreviewActivity` layout, Camera2 API capture, high-res bitmap watermark burning, `MediaPlayer` shutter sound, and live GPS geocoding.
     - [`WatermarkModel.java`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android/app/src/main/java/com/nextai/camera/WatermarkModel.java): Implements all 14 original Timemark templates from `default_watermarks_global.json` (`ID 34`, `map01`, `ID 21`, `ID 10`, `ID 120`, `ID 20`, `ID 80`, `dasher01`, `service01`, `ID 60`, `ID 130`, `ID 100`, `ID 90`, `ID 43`), field toggles, SHA-256 `Photo Code` generation, and `JpegPacker` (`XHEY1` magic trailer) lossless dual-stream JPEG packing/unpacking.
     - [`OriginalDialogs.java`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/apps/android/app/src/main/java/com/nextai/camera/OriginalDialogs.java): Implements the 8 original dialogs/screens matching their decompiled XML layouts (`fragment_universal_watermark_list_global.xml`, `layout_watermark_edit_header_global.xml`, `setting_dialog_view_new.xml`, `ratio_setting_dialog_global.xml`, `layout_fragment_photo_verify.xml`, `fragment_layout_teamspace_home.xml`, `fragment_local_pic_preview.xml`, `activity_purchase.xml`).

2. **Native C++17 & Edge AI (`CONVERT/apps/android/app/src/main/cpp/` & `jniLibs/arm64-v8a/`)**:
   - 7 ARM64 `.so` libraries (`198` `STT_FUNC`, `159` `Java_*`, `sh_info = 6`, `0` stubs).
   - Real NCNN graph execution (`SteelDetect` rebar counting, `VLPR` license plate detection, `ZeroDCE` low-light enhancement, `WatermarkRemove`), `PaddleOCR`, `JpegPacker` EOI tail steganography, `Aegis` hardware anti-spoofing, and `.mcap` robotics muxer.

3. **Backend API, Admin CMS & OLTP/OLAP Database (`CONVERT/Backend/`)**:
   - TypeScript + Node.js server listening on port `4580`.
   - Serves all 177 `/next/*` API endpoints (`TodayApiService`, `ClusterApiService`, `EarnApiService`, `DualModeApiService`, `S3/OSS STS Token API`) wrapped in standard `BaseResponse<T>` (`{"code": 200, "msg": "success", "status": 0, "data": ...}`).
   - Relational OLTP schema (42 Room parity tables + server SaaS tables) + OLAP star-schema analytical cubes (`fact_photo_captures`, `fact_attendance_events`, `fact_revenue_transactions`, `fact_robotics_mcap_telemetry`).
   - Built-in Enterprise Admin CMS at `http://localhost:4580/cms`.

4. **Mini-Games Collection & HTML5 Web Game Hub (`CONVERT/games/`)**:
   - Dedicated `games/` directory containing 5 playable HTML5 Canvas mini-games (`rebar-tycoon`, `geo-patrol-defender`, `truth-detective`, `robo-hand-trainer`, `tower-crane-builder`) and the unified Web Game Hub (`CONVERT/games/index.html`) — kept cleanly separated from the original Timemark camera UI baseline.
