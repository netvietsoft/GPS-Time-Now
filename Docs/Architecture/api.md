# API Architecture — 177 `/next/*` Endpoints & Service Contracts

> **Owner**: `Agent 1 - ARCHITECT` & `Agent 4 - INTEGRATION/API`  
> **Canonical OpenAPI Spec**: [`Docs/Contracts/openapi.yaml`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Contracts/openapi.yaml)  
> **Base URL (Local/LAN)**: `http://localhost:4580/next/` | **Production**: `https://api.nextai.technology/next/`

---

## 1. Standard Wire Envelope (`BaseResponse<T>`)

Every `/next/*` endpoint returns the deterministic JSON envelope expected by the Android Retrofit client:

```json
{
  "code": 200,
  "msg": "success",
  "status": 0,
  "data": { ... }
}
```

---

## 2. The 5 Service Domains (177 Total Endpoints)

| Service Interface | Route Count | Key Endpoint Prefixes | Core Responsibilities |
|---|---:|---|---|
| **1. `NextAITodayApiService`** | **68** | `/next/android/config`, `/next/realtime`, `/next/weather/*`, `/next/v2/place/*`, `/next/watermark/*`, `/next/logo/*`, `/next/truthful/*`, `/next/ai/*`, `/next/center/app/*`, `/next/member/vip/*`, `/next/goods/*`, `/next/order/*`, `/next/app/adx/*`, `/next/attend/*` | Atomic NTP clock sync, GPS/Nominatim reverse geocoding, PGC/UGC watermarks, AI logo cutout, LLM note generation, `VerifyAuth` batch queries, OAuth login, and VIP billing. |
| **2. `NextAIClusterApiService`** | **91** | `/next/app/group/*`, `/next/app/photo/*`, `/next/app/checklist/*`, `/next/app/project/file/*`, `/next/app/attendance/*`, `/next/app/group/timeclock/geofence/*`, `/next/business/subscription/*`, `/next/web/teamspace/*` | Multi-tenant Teamspace creation, RBAC roles, project geofences, field photo stream, OCR search, safety checklists, security patrol checkpoints, and OneDrive/Drive webhooks. |
| **3. `NextAIEarnApiService`** | **9** | `/next/app/reward/video/task/*`, `/next/app/reward/video/submit`, `/next/app/reward/video/balance`, `/next/app/reward/video/withdrawal/*` | Embodied AI `.mcap` task listing, S3 upload submission, reward wallet balance, and Pix (Brazil BRL) / SPEI (Mexico MXN) bank withdrawals. |
| **4. `NextAIDualModeApiService`** | **7** | `/next/antiFakeCode/generate`, `/next/workgroup/photo/watermark/remove/check`, `/next/app/user/event/add`, `/next/app/user/event/count` | Pre-allocating offline anti-fake codes, authorizing `JpegPacker` lossless watermark removal, and tracking free-trial feature usage counts. |
| **5. `NextAIStorageStsApiService`** | **2** | `/next/workgroup/v4/s3/token`, `/next/workgroup/v4/oss/token` | Issuing short-lived STS credentials (`accessKeyId`, `secretAccessKey`, `sessionToken`, `expiration`) for cloud media uploads. |

---

## 3. Additional Analytical & Platform Routes

- `GET /health` — Server liveness, uptime, port `4580` status, and OLTP/OLAP table counts.
- `GET /next/analytics/olap/summary` — Real-time OLAP star-schema aggregations for the Admin CMS.
- `GET /cms` — Enterprise Admin CMS web application.
- `GET /games` — Interactive HTML5 Web Games Hub.
