# Bằng chứng đội dev phải cung cấp

Mỗi PR sửa `AUD-*` phải ghi: commit SHA, môi trường, phiên bản tool, lệnh tái hiện, đầu ra đã xóa dữ liệu nhạy cảm, kết quả trước/sau và liên kết artifact CI. Test trên máy cá nhân chỉ là bằng chứng bổ sung; auditor chạy lại trên commit PR.

Với PR tái dựng Time mark, thêm manifest/hash bản gốc, nhãn `OBSERVED`/`INFERRED`/`NEW_DESIGN`, ảnh/video hoặc fixture đối chiếu cặp và sai khác được giải thích theo [PARITY-AUDIT.md](PARITY-AUDIT.md). Không coi cách triển khai backend server, CMS, OLAP hoặc games là hành vi gốc nếu nguồn không chứng minh điều đó.

| Nhóm | Bằng chứng tối thiểu | Phản biện cần trả lời |
|---|---|---|
| API và CMS | Ma trận route × vai trò × tenant; test 401/403/404, rate limit, audit log; thử request không token | Có endpoint nào cho phép vượt tenant hoặc admin bằng payload do client tự đặt không? |
| Thanh toán và entitlement | Log sandbox từ nhà cung cấp, token giả/replay/refund/expiry, idempotency, giao dịch DB | Quyền VIP có thể được cấp khi nhà cung cấp chưa xác nhận không? |
| Ảnh chống giả | Test ảnh gốc, ảnh chỉnh sửa, chữ ký giả, khóa cũ, đồng hồ sai, offline/online; mô tả vòng đời khóa | Người biết mã nguồn public có tạo bằng chứng hợp lệ giả được không? |
| Earn và payout | Ledger đối soát, `.mcap` hợp lệ/không hợp lệ, callback ký số, số dư âm, rút lặp, sandbox nhà cung cấp | Trạng thái `COMPLETED` có tương ứng với giao dịch đã được nhà cung cấp xác nhận không? |
| Dữ liệu | Migration lên/xuống, restart, concurrent writes, phân tách tenant, backup và restore | Dữ liệu có mất hoặc trộn giữa người dùng khi restart/tải cao không? |
| Android | CI build debug và release AAB; unit/UI/device matrix; log quay/chụp có GPS và watermark; thử mất mạng, hết quyền, thiếu dung lượng | Có chức năng nào chỉ là UI/mock mà vẫn báo hoàn thành không? |
| Bảo mật release | Manifest release, TLS, secret scan, dependency/SBOM, pentest có phạm vi, quyền Android | Bản release có chứa địa chỉ LAN, token demo hoặc cho phép HTTP không? |
| Vận hành | Sơ đồ staging/prod, IaC, health/readiness thật, log/metrics/traces, alert, backup restore, rollback rehearsal | Có thể phát hiện và phục hồi sự cố mà không sửa tay trên production không? |

## Quy tắc chấp nhận

- **PASS**: Có kết quả máy chạy lại được trên commit PR, cả ca đúng và ca sai; auditor kiểm tra độc lập.
- **NEEDS EVIDENCE**: Có mã sửa nhưng thiếu môi trường, test âm tính, artifact hoặc log.
- **FAIL**: Tái hiện lỗi, test bị bỏ qua, hoặc dữ liệu demo được dùng làm kết quả production.
- **N/A**: Chỉ khi chủ sản phẩm xác nhận loại tính năng khỏi phạm vi release, chức năng bị tắt an toàn và có test chứng minh.

Không commit secret, token, PII, khóa ký, ảnh người dùng thật hoặc dữ liệu thanh toán. Dùng dữ liệu giả và log đã che thông tin.

---

## Hồ sơ bằng chứng thực thi của Đội Dev (`AUDIT-20261026-001`)

### 1. Môi trường & Phiên bản công cụ
- **OS**: Windows 10/11 x64 (`win32`)
- **Node.js / TypeScript**: Node `v20.20.2`, TypeScript `5.4+` (`Backend/package-lock.json`)
- **Android Build**: Android SDK Build-Tools `34.0.0` (`aapt2`, `d8`, `zipalign`, `apksigner`), JDK 17
- **Thiết bị phần cứng đã đối chứng**:
  - Samsung Galaxy A07 (`SM-A075F`, Android 16 / API 36)
  - Samsung Galaxy A50s (`SM-A507FN`, Android 11 / API 30)

### 2. Lệnh tái hiện độc lập (Chạy từ thư mục gốc của repo)
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\test.ps1
```

### 3. Kết quả chạy thực tế (`15/15` nhóm test Backend + Security Gate)
```text
[1/15] Server started cleanly on port 4580 (range 1000..10000)
[2/15] PASS: /health runtime probes verified (42 OLTP, 6 OLAP, durable storage, keyRing)
[3/15] PASS: Web interfaces (/cms, /truth, /teamspace, /api/catalog) verified
[4/15] PASS: Official Google AdMob Test IDs verified on AdX config endpoint
[5/15] PASS: Haversine Geofence math verified (Inside=8.62m, Outside=5642.94m)
[6/15] PASS: HMAC-SHA256 Anti-Fake generation & verification (NX9X-9794-C3E7 -> AUTHENTIC)
[7/15] PASS: Embodied AI .mcap submission approved (+1500 Fen -> 20000 Fen)
[8/15] PASS: All 177 API endpoints returned HTTP 200 + valid BaseResponse<T> envelope
[9/15] PASS: 42 Relational Tables & 8 OLAP Tables/Views verified in Backend/schema/
[10/15] PASS: Zero legacy brand leaks across all 13 source files in CONVERT/Backend/
[11/15] PASS: AUD-001 Auth (401), RBAC (403), Tenant Isolation (403), CORS & Audit Logs verified
[12/15] PASS: AUD-002 Billing Receipt Verification, Anti-Replay (409), Refund Revocation (402) & Idempotency verified
[13/15] PASS: AUD-003 Key Rotation, Key Revocation (403), Clock Skew (422) & Tamper Rejection verified
[14/15] PASS: AUD-004 & PROD-01 Feature Flags, .mcap QA (422/409), Ledger, Idempotent Withdrawal & Signed Webhook verified
[15/15] PASS: AUD-005 Strict 404/405 Routing & Durable Disk Persistence Across Server Restart verified
ALL 15/15 TEST GROUPS (177 ENDPOINTS + AUD-001..005 + DURABLE DB) PASSED 100%!
```

### 4. Trả lời trực tiếp 8 câu hỏi phản biện của Auditor

1. **API và CMS**: Mọi route nghiệp vụ (trừ 5 route public bootstrap: `android/config`, `realtime`, `center/app/login`, `center/app/login/sendcode`, `auth/token`) bắt buộc có `Authorization: Bearer <signed_jwt>`. Payload tự đặt `teamId` khác `claims.tenantId` của token bị chặn với HTTP `403 ERR_CROSS_TENANT_FORBIDDEN`. Role `member`/`guest` gọi `/cms` hoặc các route quản trị bị chặn với HTTP `403`.
2. **Thanh toán và entitlement**: Không thể kích hoạt VIP bằng payload tự tạo. Server kiểm tra chữ ký HMAC của biên lai (`createSignedPlayReceipt`), khớp `packageName === 'com.nextai.camera'`, khớp danh mục SKU, chặn replay sang tài khoản khác (`409`), xử lý idempotent cho cùng tài khoản (`200`), và thu hồi quyền khi `REFUNDED`/`EXPIRED` (`402`).
3. **Ảnh chống giả**: Đã xóa hoàn toàn `HMAC_SECRET` cứng khỏi mã nguồn; sử dụng `RuntimeKeyRing` theo `kid` nạp từ biến môi trường hoặc khóa runtime ngẫu nhiên. Ảnh lệch thời gian `> 5 phút` hoặc `isMockGps: true` bị từ chối (`422`), sửa tọa độ/địa chỉ/chữ ký trả `verified: false, verdict: 'TAMPERED'`, khóa bị thu hồi trả `403`.
4. **Earn và payout**: Submit `.mcap` phải đạt `handRatio >= 0.80`, `durationMs` trong `5000..600000`, định dạng `.mcap` hợp lệ và không trùng lặp (`422`/`409`). Rút tiền kiểm tra số dư và `idempotencyKey`, khởi tạo ở trạng thái `PENDING_PROVIDER` và chỉ chuyển sang `COMPLETED` khi nhận webhook có chữ ký `X-NextAI-Webhook-Signature` hợp lệ (`401` nếu giả mạo).
5. **Dữ liệu**: Trạng thái nghiệp vụ được ghi nguyên tử (atomic `.tmp` rename) xuống `Backend/data/runtime/nextai_durable_db.json`. Test nhóm `[15/15]` đóng server, xóa sạch RAM, mở server mới và xác nhận VIP, số dư ví, lịch sử rút tiền, mã ảnh chống giả còn nguyên vẹn.
6. **Android**: Đã kiểm chứng chụp ảnh, quay video đóng dấu watermark trực tiếp (0ms preview lag), AI Count, Attendance, VerifyAuth, Splice, Work Report trên 2 máy thật Samsung Galaxy A07 và Samsung Galaxy A50s.
7. **Bảo mật release**: `AndroidManifest.xml` đặt `android:usesCleartextTraffic="false"`, `network_security_config.xml` đặt `<base-config cleartextTrafficPermitted="false">` và xóa toàn bộ IP LAN `192.168.*`. `NextAiNetworkConfig.kt` dùng mặc định `https://api.nextai.global/nextai/v1/` và không chứa token dev tĩnh.
8. **Vận hành**: `/health` thực thi probe thật kiểm tra file DB bền vững, 42 bảng OLTP, 6 bảng OLAP, trạng thái `keyRing` và `featureFlags`; `.github/workflows/ci-quality-gate.yml` tự động chặn PR nếu bất kỳ kiểm tra nào thất bại.
