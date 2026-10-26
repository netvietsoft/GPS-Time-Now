# Storage Architecture — S3 / R2 STS Credentials & Offline Room Sync

> **Owner**: `Agent 2 - BACKEND` & `Agent 4 - INTEGRATION/API`  
> **Endpoints**: `/next/workgroup/v4/s3/token`, `/next/workgroup/v4/oss/token`, `/next/workgroup/v3/upload/toCloud`

---

## 1. Zero-Hardcoded-Key STS Token Flow

Neither the Android APK nor the web client embeds static cloud storage secret keys. All uploads follow the **Security Token Service (STS)** pattern:
1. Client requests temporary credentials from `POST /next/workgroup/v4/s3/token` (or `/next/workgroup/v4/oss/token`).
2. Server validates the user's session and returns short-lived credentials (`accessKeyId`, `secretAccessKey`, `sessionToken`, `expiration`, `bucketName`, `endpoint`).
3. Client caches the token in Room table #15 (`oss_data_table`) until `expiration`.
4. In local development on Port `4580`, `S3_ENDPOINT` points to the built-in S3-compatible object storage emulator (`http://localhost:4580/s3`), persisting uploaded JPEGs, PDF/Excel/KMZ reports, and `.mcap` robotics files under `CONVERT/Backend/storage/` and `CONVERT/Backend/public/cdn/`.

---

## 2. Offline-First Resumable Upload Queue

Field photos captured without cellular coverage are stored in:
- `stand_alone_photo_table` (Table #9) & `picture_up_table` (Table #10) for individual mode.
- `upload_progress_task_entry` (Table #23) & `doc_upload_task_entry` (Table #24) for Teamspace mode, tracking `uploaded_bytes` vs `file_size` for automatic background retry when connectivity returns.
