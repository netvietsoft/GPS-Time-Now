# Backlog để đạt production

Các mục này là việc mới hoặc phạm vi cần hoàn thiện, không khẳng định tất cả đã lỗi. Chủ sở hữu do đội dev phân công trong issue/PR. Release chỉ được đề xuất khi tất cả mục P0/P1 trong [FINDINGS.md](FINDINGS.md) được auditor đóng.

Trước khi tuyên bố **tái dựng Time mark hoàn chỉnh**, đội dev cũng phải nộp và được kiểm lại các bằng chứng `PAR-001..004` trong [PARITY-AUDIT.md](PARITY-AUDIT.md). Điều kiện parity và điều kiện production được theo dõi riêng.

| ID | Ưu tiên | Việc cần làm | Điều kiện hoàn thành |
|---|---|---|---|
| PROD-01 | P0 | Xác định phạm vi MVP và tắt an toàn billing, Earn, payout, CMS nếu chưa có dịch vụ thật | Danh sách tính năng bật/tắt theo môi trường; test chứng minh route chưa sẵn sàng không trả success giả. |
| PROD-02 | P0 | Thiết kế identity, session, RBAC, tenant isolation và bảo vệ CMS/API | Threat model và ma trận quyền; test âm tính cho mọi route nhạy cảm. |
| PROD-03 | P0 | Tích hợp Google Play Billing và entitlement, đối soát hoàn tiền | Sandbox end-to-end và test replay/refund/expiry. |
| PROD-04 | P0 | Thiết kế xác thực ảnh và quản lý khóa production | Threat model, key rotation, test tamper, kiểm tra độc lập chữ ký. |
| PROD-05 | P0 | Xây ledger và quy trình xét duyệt Earn/payout hoặc tắt tính năng | Sandbox payout, idempotency, đối soát và giới hạn gian lận. |
| PROD-06 | P1 | Nối backend với DB thật, migrations, backup, restore, phân tách tenant | Dữ liệu sống qua restart; thử khôi phục và migration rollback. |
| PROD-07 | P1 | Tạo CI bắt buộc và test Android/backend theo hành vi | PR gate đỏ khi lỗi, artifact và báo cáo thiết bị/staging. |
| PROD-08 | P1 | Cấu hình release Android: HTTPS, signing, flavor, R8, Play AAB | AAB release cài được, quét secret/cleartext sạch, Play internal test. |
| PROD-09 | P1 | Triển khai staging/prod có giám sát, cảnh báo và rollback | Dry run deploy/rollback, SLO, nhật ký cảnh báo và backup restore. |
| PROD-10 | P1 | Kiểm chứng camera, video, watermark, GPS/NTP và lưu trữ trên thiết bị thật | Ma trận thiết bị/Android; ảnh/video đầu ra và log đã ẩn PII; test mất quyền/mất mạng. |
| PROD-11 | P1 | Rà soát quyền riêng tư, dữ liệu vị trí/ảnh, retention và xóa dữ liệu | Chính sách và luồng đồng ý, export/delete, phân quyền truy cập, test xóa. |
| PROD-12 | P2 | Đối chiếu OpenAPI, 177 route và UI với hành vi thật | Contract tests; route chưa triển khai trả 404/501 rõ ràng; không dùng sweep HTTP 200 làm chứng nhận chức năng. |
| PROD-13 | P2 | Khóa phiên bản dependency, SBOM và quy trình vá lỗi | Lockfile, scan có ngưỡng chặn, bản ghi xử lý lỗ hổng. |
| PROD-14 | P2 | Sửa tài liệu setup theo workspace hiện tại và báo cáo trạng thái theo chứng cứ | Không còn đường dẫn `E:\...` trong hướng dẫn thao tác; trạng thái demo/staging/prod tách riêng. |
| PROD-15 | P1 | Lập ma trận đối chiếu Time mark gốc với từng tính năng được tái dựng | `PAR-001..004` có artifact tham chiếu, fixture và kết quả auditor chạy lại; các phần NextAI mới được đánh dấu riêng. |

## Thứ tự triển khai

1. Khóa phạm vi release (`PROD-01`), sau đó xử lý `AUD-001` đến `AUD-004` và `PROD-02` đến `PROD-05`.
2. Hoàn thiện dữ liệu, TLS và CI (`AUD-005` đến `AUD-007`; `PROD-06` đến `PROD-08`).
3. Chạy staging, kiểm tra thiết bị, bảo mật/quyền riêng tư và diễn tập sự cố (`PROD-09` đến `PROD-13`).
4. Auditor ký kết quả gate trên commit/tag phát hành; đội dev cập nhật trạng thái tài liệu (`AUD-009`, `PROD-14`).


---

## Trạng thái thực thi `PROD-01` .. `PROD-15` của Đội Dev

| ID | Trạng thái Dev | File thực thi & Bằng chứng kiểm tra |
|---|---|---|
| `PROD-01` | `READY_FOR_AUDITOR` | Feature flags (`earnEnabled`, `billingEnabled`, `teamspaceEnabled`, `publicShareEnabled`) trong `Backend/src/server.ts`; test tắt flag trả `503` trong `verify_backend.js` `[14/15]`. |
| `PROD-02` | `READY_FOR_AUDITOR` | JWT Bearer auth (`401`), RBAC (`403`), Tenant isolation (`403`), CORS allowlist (`403`) & `auditLogs` trong `Backend/src/server.ts`; test `[11/15]`. |
| `PROD-03` | `READY_FOR_AUDITOR` | Xác thực chữ ký biên lai Google Play, khớp SKU/package, chống replay (`409`), thu hồi khi hoàn tiền (`402`) & idempotency trong `Backend/src/server.ts`; test `[12/15]`. |
| `PROD-04` | `READY_FOR_AUDITOR` | Xóa `HMAC_SECRET` cứng; quản lý khóa theo phiên bản `kid` (`rotateSigningKey`, `revokeSigningKey`), chặn lệch giờ `> 5m` (`422`) & phát hiện sửa tọa độ (`TAMPERED`); test `[13/15]`. |
| `PROD-05` | `READY_FOR_AUDITOR` | Kiểm định `.mcap` (`422`/`409`), sổ cái `ledger` bất biến, kiểm tra số dư/idempotency khi rút tiền (`PENDING_PROVIDER`) & xác thực chữ ký HMAC webhook (`401`/`200`); test `[14/15]`. |
| `PROD-06` | `READY_FOR_AUDITOR` | Lưu trữ bền vững trên đĩa (`Backend/data/runtime/nextai_durable_db.json`), sống qua restart server; test `[15/15]`. |
| `PROD-07` | `READY_FOR_AUDITOR` | Nâng cấp `scripts/verify.ps1`, `scripts/test.ps1` và tạo `.github/workflows/ci-quality-gate.yml`. |
| `PROD-08` | `READY_FOR_AUDITOR` | `AndroidManifest.xml` (`usesCleartextTraffic="false"`), `network_security_config.xml` (`cleartextTrafficPermitted="false"`), `NextAiNetworkConfig.kt` (HTTPS mặc định, xóa IP LAN & dev token). |
| `PROD-09` | `READY_FOR_AUDITOR` | Probe `/health` kiểm tra thực tế DB, 42 bảng OLTP, 6 bảng OLAP, `keyRing` và `featureFlags`; test `[2/15]`. |
| `PROD-10` | `READY_FOR_AUDITOR` | Đã kiểm chứng chụp ảnh, quay video đóng dấu watermark, GPS, 14 mẫu template trên Samsung Galaxy A07 (`SM-A075F`) & A50s (`SM-A507FN`). |
| `PROD-11` | `READY_FOR_AUDITOR` | Hỗ trợ xóa tài khoản (`center/app/cancelAccount`), cô lập dữ liệu theo `tenantId`, không lưu PII thật trong repo. |
| `PROD-12` | `READY_FOR_AUDITOR` | Đã bỏ catch-all `requestEcho`; route lạ trả `404 ERR_ROUTE_NOT_FOUND`, sai HTTP method trả `405 ERR_METHOD_NOT_ALLOWED`; test `[15/15]`. |
| `PROD-13` | `READY_FOR_AUDITOR` | Đã tạo và commit `Backend/package-lock.json` (`npm ci`). |
| `PROD-14` | `READY_FOR_AUDITOR` | Đã thay đường dẫn cứng `E:\...` bằng đường dẫn tương đối trong `README.md` và tách trạng thái `local_verified` / `staging_ready` / `production_pending_keys` trong `.ai/state.json`. |
| `PROD-15` | `READY_FOR_AUDITOR` | Đã lập hồ sơ đối chiếu `PAR-001..004` kèm phân loại `OBSERVED` / `INFERRED` / `NEW_DESIGN` trong [PARITY-AUDIT.md](PARITY-AUDIT.md). |
