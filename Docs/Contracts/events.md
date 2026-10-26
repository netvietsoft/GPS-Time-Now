# Domain & Telemetry Events Contract

> **Owner**: `Agent 1 - ARCHITECT` & `Agent 4 - INTEGRATION/API`

---

## 1. Core Domain Events (`NextAI Event Bus`)

| Event Name | Producer | Consumers | Payload Schema |
|---|---|---|---|
| `photo.captured.aegis_signed` | Android `PreviewScreen` / `JpegPacker` | Room `stand_alone_photo_table`, `/next/truthful/create/batch`, OLAP `fact_photo_captures` | `{ photoCode, antiFakeCode, ntpTime, lat, lng, watermarkId, rebarCount, aegisSignature }` |
| `photo.unpacked.clean_restored` | Android `ExportUnpackScreen` | `/next/workgroup/photo/watermark/remove/check`, Audit Log | `{ photoCode, userId, vipStatus, sliceCount }` |
| `teamspace.attendance.punched` | Android `TeamSpaceScreen` | `/next/app/attendance/punch`, OLAP `fact_attendance_events` | `{ teamId, projectId, userId, lat, lng, distanceMeters, insideGeofence, shiftType }` |
| `verifyauth.audit.completed` | Backend `/next/truthful/Query/batch/detail` | `VerifyAuthScreen`, Admin CMS Audit Center | `{ antiFakeCode, authentic, aegisValid, ntpDeltaMs, gpsSatellites, blindWatermarkIntact }` |
| `billing.subscription.activated` | Backend `/next/order/google/receipts` | User VIP State, OLAP `fact_revenue_transactions` | `{ orderNo, userId, skuId, amountUsd, isTrial, expiresAt }` |
| `earn.mcap.submitted` | Android `EarnRoboticsScreen` | `/next/app/reward/video/submit`, OLAP `fact_robotics_mcap_telemetry` | `{ taskId, userId, ossKey, durationSec, handLandmarkConfidence, imuHz, rewardBrl }` |
| `games.score.recorded` | HTML5 Web Games Hub (`/games`) | LocalStorage & `/next/app/user/event/add` | `{ gameId, score, levelCompleted, bonusVipMinutes }` |
