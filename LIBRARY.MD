# LIBRARY.MD — Approved Technology & Dependency Catalog

> **Governance**: Per `AGENTS.md` Rule #9 & #14, agents may use the approved libraries listed in this catalog without filing a new ADR. Adding any unlisted external runtime dependency requires justification and an ADR in `Docs/ADR/`.

---

## 1. Android Application (`CONVERT/apps/android/`)

| Category | Library / Coordinate | Approved Version | Purpose |
|---|---|---|---|
| **Language & Coroutines** | `org.jetbrains.kotlin:kotlin-stdlib`, `kotlinx-coroutines-android` | `2.0.20` / `1.8.1` | Core Kotlin language & async structured concurrency |
| **Jetpack Compose UI** | `androidx.compose:compose-bom` (`material3`, `ui`, `foundation`, `navigation-compose`) | `2024.09.00` | Declarative UI for all 8 NextAI Camera screens |
| **Camera & Media** | `androidx.camera:camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-video`, `camera-view` | `1.3.4` | Hardware camera viewfinder, photo capture, and HEVC video recording |
| **Local Database (42 Tables)** | `androidx.room:room-runtime`, `room-ktx`, `room-compiler` | `2.6.1` | Local SQLite persistence (`NextAICameraDB` v61 parity) |
| **Networking** | `com.squareup.retrofit2:retrofit`, `converter-gson`, `com.squareup.okhttp3:okhttp` | `2.11.0` / `4.12.0` | HTTP client for all 177 `/next/*` API endpoints |
| **Serialization** | `com.google.code.gson:gson`, `org.jetbrains.kotlinx:kotlinx-serialization-json` | `2.11.0` / `1.7.1` | JSON model parsing (`BaseResponse<T>`, watermark JSON) |
| **Image Loading** | `io.coil-kt:coil-compose` | `2.7.0` | Async image & watermark cover rendering |
| **Location & Maps** | `com.google.android.gms:play-services-location`, `play-services-maps` | `21.3.0` / `19.0.0` | GPS satellite telemetry, geofence distance, mini-map widget |
| **AdTech (Test Keys)** | `com.google.android.gms:play-services-ads` | `23.3.0` | Google AdMob integration configured with official Test IDs |
| **Billing** | `com.android.billingclient:billing-ktx` | `7.0.0` | Google Play Billing (`NextAI Plus` & `Teamspace Business`) |
| **Document Export** | `org.apache.poi:poi-ooxml` (or lightweight XML/ZIP builder), Android `PdfDocument` | `5.2.5` / SDK Built-in | Batch PDF, Excel `.xlsx`, and Google Earth `.kmz` report generation |
| **Native C++ / Edge AI** | NDK `C++17`, `libnextai_native.so` (`NCNN`, `PaddleOCR`, `JpegPacker`, `Aegis`, `McapMuxer`) | NDK `19`–`28` (`ld.lld`) | Rebar counting, VLPR, ZeroDCE, steganography, `.mcap` robotics capture |

---

## 2. Backend API, CMS & OLTP/OLAP Server (`CONVERT/Backend/`)

| Category | Package | Approved Version | Purpose |
|---|---|---|---|
| **Runtime & Language** | `node`, `typescript`, `ts-node` | `>=20.0.0` / `^5.6.2` | Server runtime and strict static typing |
| **HTTP Framework** | `express`, `cors` | `^4.21.0` / `^2.8.5` | `/next/*` API Gateway, `/cms` portal, `/games` static server on port `4580` |
| **Database Engine** | `better-sqlite3` or `sqlite3` / built-in `node:sqlite` + JSON WAL persistence | Latest | Zero-config local OLTP (42 tables) + OLAP Star Schema engine |
| **Security & Crypto** | Node.js built-in `node:crypto` | Built-in | JWT signing, HMAC-SHA256 `AntiFakeCode` verification, S3 STS token generation |
| **Testing** | Node.js built-in `node:test`, `node:assert` | Built-in | Fast zero-dependency unit & integration test execution |

---

## 3. Forbidden / Deprecated Libraries (Strictly Prohibited)

- **`moment.js`**: Deprecated; use ISO-8601 `Date` / `Intl.DateTimeFormat` or `dayjs`.
- **Legacy SenseTime `.lic` loader (`com.sensetime.stmobile.*`)**: Prohibited due to package-name lock (`ERR-20261026-004`); use OpenGL ES 3.0 LUT shaders (`ADR-005`).
- **SecNeo / Bangcle wrapper (`com.secneo.apkwrapper.*`)**: Prohibited packer stub; removed in clean Kotlin architecture.
