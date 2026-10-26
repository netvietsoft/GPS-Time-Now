# Canonical Error Codes Catalog (`BaseResponse<T>`)

> **Wire Envelope**: `{"code": <int>, "msg": "<string>", "status": <int>, "data": <object|null>}`

| Code | Status | Constant Name | HTTP Status | Meaning & Client Recovery Action |
|---:|---:|---|---:|---|
| `200` | `0` | `OK_SUCCESS` | `200` | Request succeeded; parse `data`. |
| `400` | `1001` | `ERR_INVALID_PARAMS` | `400` | Missing or malformed request field; validate input payload. |
| `401` | `1002` | `ERR_AUTH_TOKEN_EXPIRED` | `401` | JWT expired or missing; invoke `/next/center/app/token/refresh`. |
| `403` | `1003` | `ERR_TEAMSPACE_RBAC_DENIED` | `403` | User role lacks permission for this Teamspace/Project operation. |
| `403` | `2001` | `ERR_VIP_REQUIRED_FOR_UNPACK` | `403` | `JpegPacker` watermark removal requires `NextAI Plus` or `AdFreePass`; open `PurchaseScreen`. |
| `409` | `3001` | `ERR_GEOFENCE_OUT_OF_RANGE` | `200` | Employee is outside project geofence radius (`distance > radius_meters`). |
| `422` | `4001` | `ERR_VERIFY_AUTH_TAMPERED` | `200` | Photo failed `Aegis` hardware signature, NTP delta, or `PaddleOCR` check. |
| `422` | `5001` | `ERR_MCAP_HAND_CONFIDENCE_LOW` | `200` | `.mcap` recording has `< 85%` 21-point hand landmark visibility or missing IMU stream. |
| `422` | `5002` | `ERR_WITHDRAWAL_BELOW_MIN` | `200` | Withdrawal amount is below minimum (`3000` fen / 30 BRL for Pix, `10000` fen / 100 MXN for SPEI). |
| `500` | `9999` | `ERR_INTERNAL_SERVER` | `500` | Unexpected server error; retry with exponential backoff. |
