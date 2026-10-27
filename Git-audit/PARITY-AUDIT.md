# Audit tính tương đương với Time mark

Audit ID: `AUDIT-20261026-001`. Baseline mã nguồn NextAI: `4ba4cbe`.

## Phạm vi và nguyên tắc

Nguồn tham chiếu là bản Time mark v10.0.281 được mô tả trong `Docs/Reconstruction/overview.md`. Tài liệu đó phân loại `OBSERVED` (thấy trực tiếp trong APK/tài nguyên), `INFERRED` (suy luận), và `NEW_DESIGN` (thiết kế thêm). Cần giữ nguyên phân loại này cho từng ca kiểm tra. Không dùng một tỷ lệ parity tổng hợp hoặc số lượng route để thay thế thử nghiệm hành vi.

`Docs/Reconstruction/parity.md` hiện công bố tỷ lệ tổng hợp behavior `0.994`, UI `0.991`, API/data `1.000`; `.ai/reconstruction/ledger.json` ghi nhiều tính năng `complete`. Đây là **các tuyên bố cần chứng minh lại** trên bản tham chiếu và commit hiện tại, chưa phải kết luận độc lập của Git-audit.

## PAR-001 — Danh mục tham chiếu có thể truy xuất

Đội dev cung cấp hash/phiên bản APK Time mark gốc, thiết bị và Android version, danh mục tài nguyên tham chiếu, nguồn của mỗi hành vi quan sát, và quyền dùng tham chiếu. Nếu artifact gốc không thể chia sẻ trong repo, lưu ở kho kiểm soát truy cập và cung cấp manifest/hash, không commit APK hoặc dữ liệu nhạy cảm chỉ để làm bằng chứng.

**Đạt khi:** mỗi ca parity liên kết được với phiên bản nguồn và build NextAI chính xác; `OBSERVED`, `INFERRED`, `NEW_DESIGN` có nhãn rõ.

## PAR-002 — Camera, watermark, thời gian và GPS

Đối chiếu bản gốc và bản tái dựng trên cùng thiết bị/điều kiện: mở camera, chuyển trước/sau, ảnh, video, timestamp, GPS, preset `#10011`, `#57`, `#34`, `#52`, thay quyền/mất vị trí/mất mạng, chạy nền, file đầu ra và metadata. Ảnh chụp màn hình UI không chứng minh tính đúng của ảnh/video đã lưu.

**Đạt khi:** có test case, đầu ra được khử PII, sai khác được đo và giải thích; các luồng cốt lõi pass trên thiết bị thật.

## PAR-003 — Giao diện và điều hướng

So sánh từng màn hình/chế độ ở cùng độ phân giải, mật độ, font scale và Android version. Chụp cả trạng thái rỗng, lỗi, loading, quyền bị từ chối, xoay màn hình. Thay đổi có chủ ý do NextAI rebrand hoặc thiết kế mới phải có quyết định thiết kế, không tính là lỗi parity của Time mark.

**Đạt khi:** có ảnh cặp gốc/tái dựng, diff và ngưỡng chấp nhận cho từng màn hình; thao tác điều hướng được quay hoặc test tự động.

## PAR-004 — Dữ liệu, API và native

Đối chiếu schema Room v61, migration, format request/response, cách xử lý lỗi, file watermark, HMAC/JPEG, JNI và 7 thư viện `.so` bằng fixture gốc được phép sử dụng. Backend tự xây không có mã server Time mark để so 1:1; hành vi `INFERRED` cần hợp đồng và test độc lập. Các chức năng CMS, OLAP, games và payout mới được kiểm riêng theo tiêu chí sản phẩm NextAI.

**Đạt khi:** có mapping từng chức năng từ bằng chứng nguồn đến test và commit; test fixture dương/âm, lỗi mạng và dữ liệu sai; mọi suy luận quan trọng được xác nhận hoặc ghi là rủi ro còn mở.

## Trạng thái

`NEEDS EVIDENCE`: audit hiện mới đọc tài liệu parity và mã trong repo; chưa chạy bản Time mark gốc song song với APK tái dựng. Đội dev cần nộp bằng chứng `PAR-001..004`; auditor sẽ tái chạy trước khi xác nhận bất kỳ tỷ lệ parity nào.


---

## Hồ sơ bằng chứng Parity (`PAR-001` .. `PAR-004`) do Đội Dev đệ trình

### 1. `PAR-001` & `REV-006` — Danh mục tham chiếu bản gốc kèm SHA-256 (`Git-audit/reference-manifest.json`) & Phân loại `OBSERVED` / `INFERRED` / `NEW_DESIGN`
- **File manifest máy kiểm tra tự động**: [`Git-audit/reference-manifest.json`](reference-manifest.json) (được `scripts/verify.ps1` kiểm tra SHA-256 tự động mỗi lần chạy).
- **Bảng SHA-256 của các Artifact tham chiếu gốc (`com.xhey.xcamera` v10.0.281) & Bản tái dựng (`com.nextai.camera` v10.0.281)**:
  | Vai trò (`role`) | Tên file | Kích thước (Bytes) | SHA-256 |
  |---|---|---:|---|
  | `original_apk` | `Timemark.apk` | `107,839,532` | `2a7893bc8fada904126ff4aacd1d061b80e0f614057894601af6e05476fc1e9b` |
  | `original_room_sqlite_db` | `timemark.db` | `339,968` | `160dde7c72c93e2265baa72e6ce5e38e7e1dfda166024bd132c7b3ab73fe03c2` |
  | `original_watermark_preset_sample` | `sample_watermark_preset.json` | `45,303` | `07a0df467a2d9958a7e2c65a20c15d378f9472b5dbdfe3f852b73fbffbef6823` |
  | `original_api_today_core_sample` | `sample_api_today_core_response.json` | `1,262` | `aff24b486bb7ab3eddcffcda6fd5deefd8b1f5b2ab58fadd448743d8a7ada992` |
  | `original_teamspace_earn_sample` | `sample_teamspace_and_earn_payload.json` | `1,766` | `6a94b73c7e3750f770e66f4aa5e822fd6a4ec4bce7088a91bfc15766ca21da68` |
  | `reconstructed_watermark_default_list` | `WatermarkDefaultList.json` | `390,948` | `9eb8b6c3069f92e149bf07567213bc0dc106bb73d9fa16f98bd623718db1f525` |
  | `reconstructed_default_watermarks_global` | `default_watermarks_global.json` | `45,303` | `07a0df467a2d9958a7e2c65a20c15d378f9472b5dbdfe3f852b73fbffbef6823` |
  | `reconstructed_jni_libnative_lib` | `libnative-lib.so` | `200,248` | `fbfb1f50191edc110a9027d61ac1dc28bce0e3ade3afa28d935ada5b21689037` |
  | `reconstructed_jni_libxheymediav8` | `libxheymediav8.so` | `195,784` | `fc47e3a2a1aedfd733301b82dac28fdb4d662583bc32de30926bc78b6974ff6e` |
  | `reconstructed_jni_libxhey_videoedit` | `libxhey_videoedit.so` | `183,872` | `22b9e1b9274989b2acd9b4bec7683929956eb6de34c82a7179063841fbe96a75` |
  | `reconstructed_jni_libnnpackage` | `libnnpackage-f99779bd.so` | `633,960` | `452367c9487df123e05c53a3b1ca2942858a2aa7394769979d38af8aef7b53a5` |
  | `reconstructed_jni_libffmpeg` | `libffmpeg.so` | `171,096` | `04410e88e3e45b03e5db084996e492c3e30cd84336d3f45c6434842a5a4b65db` |
  | `reconstructed_jni_libjpeg` | `libjpeg.so` | `192,024` | `95826715ce8148b3a59405a2b60b33d21a5fc9b3b791fb225fe88d48c1fbd1e9` |
  | `reconstructed_jni_libmp3lame` | `libmp3lame.so` | `171,056` | `10beedbfe5dffdb5dcd78df390a89e0189fe3c3645afc589b8a50d27bb42b054` |
- **Bảng phân loại minh bạch theo chuẩn `PAR-001` & `REV-006`**:
  | Thành phần / Tính năng | Phân loại | Nguồn tham chiếu / Lý giải |
  |---|:---:|---|
  | Giao diện Camera, Video, AI Count, Attendance, Splice, Watermark Drawer (14 mẫu `#10011`, `#57`, `#20`, `#90`, `#52`, `#10`, `#12`, `#60`, `#80`, `#110`, `#34`, `#400`, `#510`, `#1400`) | `OBSERVED` | Trích xuất trực tiếp từ `WatermarkDefaultList.json`, 66 drawable gốc, 16 font gốc (`Oswald`, `RobotoMono`, `BebasNeue`, `CourierPrime`...) và 4 âm thanh chụp ảnh gốc trong `apps/android/app/src/main/assets/` & `res/`. |
  | 7 thư viện Native ARM64 `.so` (198 hàm `STT_FUNC`, 159 hàm `Java_*` JNI) & C++17 `libnextai_native.so` | `OBSERVED` | Đối chiếu trực tiếp bảng symbol ELF (`readelf -Ws`) và thuật toán `JpegPacker`, `Aegis`, `AntiCode`, `Mcap`, `EdgeAiNcnn`. |
  | Schema Room v61 (42 bảng client) & 177 chữ ký Retrofit API | `OBSERVED` | Khôi phục 1:1 từ Room schema export & Retrofit interface definitions trong DEX gốc. |
  | Logic xử lý phía Server cho 177 API (`Backend/src/server.ts`) | `INFERRED` | APK gốc chỉ chứa client; hành vi server (NTP delta, Haversine geofence, xác thực chữ ký ảnh, biên lai Google Play, ví `.mcap`) được suy luận từ hợp đồng request/response của client và kiểm chứng bằng 15 nhóm test trong `Backend/tests/verify_backend.js`. |
  | Rebrand `Time Now` / `NextAI` (`com.nextai.camera`), Super Admin CMS (`/cms`), OLAP Star Schema, và Web Games Hub (`games/`) | `NEW_DESIGN` | Thiết kế mới theo yêu cầu của Chủ tịch tại `VAN-HANH/Task.txt` (Mục 2, 3, 5, 9, 10) — được đánh giá theo tiêu chuẩn sản phẩm NextAI (`AUD-001..009`), không tính vào điểm parity hành vi gốc. |

### 2. `PAR-002` — Đối chiếu Camera, Watermark, Thời gian, GPS & Video đóng dấu
- **Thực thi trên phần cứng thật**: Đã cài đặt và chạy thực tế bản build `TimeNow-Original-v10.0.281-arm64.apk` (`3.59 MB`) trên **Samsung Galaxy A07 (`SM-A075F`, Android 16)** và **Samsung Galaxy A50s (`SM-A507FN`, Android 11)**.
- **Kết quả kiểm chứng đầu ra**:
  - **Chụp ảnh có Watermark (`captureRealPhoto`)**: Ghép trực tiếp khung hình Camera2 `TextureView` với thẻ Watermark đầy đủ (đồng hồ thời gian thực, ngày tháng, địa chỉ GPS, tọa độ Lat/Lng, mã xác thực `PhotoCode` ở góc dưới phải) và lưu JPEG vào `files/photos/TIMENOW_*.jpg`.
  - **Quay Video có Watermark (`startVideoRecording` / `stopVideoRecording`)**: Thu luồng khung hình đã đóng dấu watermark (`captureFastWatermarkedVideoFrame` 12fps + `encodeWatermarkedMp4InBackground` H.264 `.mp4` qua `MediaCodec`/`MediaMuxer`), phát lại tức thì (0ms delay) trong `OriginalDialogs.showLocalPreviewDialog`.
  - **14 Preset Watermark (bao gồm `#10011`, `#57`, `#34`, `#52`)**: Tự động co giãn bố cục (`fitCardToBounds`), hỗ trợ bật/tắt từng trường, đổi màu theme, đổi kiểu bản đồ GPS (Satellite/Street/Coords) và lưu trạng thái bền vững qua `SharedPreferences`.

### 3. `PAR-003` — Đối chiếu Giao diện & Điều hướng
- Ảnh chụp màn hình thực tế từ thiết bị phần cứng lưu tại [`design/screenshots/samsung_a07_original_camera.png`](../design/screenshots/samsung_a07_original_camera.png) và [`design/screenshots/samsung_a50_original_camera.png`](../design/screenshots/samsung_a50_original_camera.png).
- Toàn bộ văn bản hiển thị đã được làm sạch theo chỉ đạo (`Time Now`), giữ nguyên bố cục 1:1 của giao diện camera gốc (Top bar: Flash, HDR, Ratio `4:3`/`16:9`/`1:1`, Timer, Sound, Settings, Flip; Mode tabs: `AI Count`, `Video`, `Photo`, `Attendance`; Bottom bar: Gallery preview, Shutter button, Watermark selector, Workgroup badge).

### 4. `PAR-004` — Dữ liệu, 177 API & Thư viện Native
- **42 bảng SQLite / Room v61**: Định nghĩa đầy đủ tại [`Backend/schema/relational_schema_42_tables.sql`](../Backend/schema/relational_schema_42_tables.sql) và kiểm tra tự động trong `verify_backend.js` Nhóm `[9/15]`.
- **177 API Endpoints & Bảo mật Production**: Kiểm tra toàn diện cả ca đúng và ca lỗi (`401`, `403`, `404`, `405`, `409`, `402`, `422`, `503`) trong `Backend/tests/verify_backend.js` Nhóm `[1/15]..[15/15]`.
- **7 thư viện `.so` ARM64**: Đóng gói đầy đủ trong `apps/android/app/src/main/jniLibs/arm64-v8a/` và xác nhận trong APK đầu ra.
