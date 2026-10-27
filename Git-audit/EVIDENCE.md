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

### 3. Kết quả chạy thực tế (`16/16` nhóm test Backend + Canonical APK `aapt2 dump xmltree` + Concurrent Shared DB Gate)
```text
[1/16] Server started cleanly on port 4588 (range 1000..10000)
[2/16] PASS: /health runtime probes & fail-closed defaults verified (42 OLTP, 6 OLAP, durable storage, keyRing)
[3/16] PASS: Web interfaces (/cms, /truth, /teamspace, /api/catalog) verified
[4/16] PASS: Official Google AdMob Test IDs verified on AdX config endpoint
[5/16] PASS: Haversine Geofence math verified (Inside=8.62m, Outside=5642.94m)
[6/16] PASS: HMAC-SHA256 Anti-Fake generation & verification (NX9X-3822-DB27 -> AUTHENTIC)
[7/16] PASS: Embodied AI .mcap submission approved (+1500 Fen -> 20000 Fen)
[8/16] PASS: All 177 API endpoints returned HTTP 200 + valid BaseResponse<T> envelope
[9/16] PASS: 42 Relational Tables & 8 OLAP Tables/Views verified in Backend/schema/
[10/16] PASS: Zero legacy brand leaks across all 13 source files in CONVERT/Backend/
[11/16] PASS: REV-001 & AUD-001 Unknown Account (401), Role/Tenant Override (403), RBAC & Tenant Isolation verified
[12/16] PASS: REV-002 & AUD-002 Provider-Sourced Play Verification, Anti-Replay (409) & Refund Revocation (402) verified
[13/16] PASS: AUD-003 Key Rotation, Key Revocation (403), Clock Skew (422) & Tamper Rejection verified
[14/16] PASS: REV-005 & AUD-004 Provider Transfer Reconciliation (422->200), Feature Flags & Ledger verified
[15/16] PASS: AUD-005 Strict 404/405 Routing & Durable Disk Persistence Across Server Restart verified
[16/16] PASS: REV-004 Concurrent Multi-Process Writes (6x12), Interrupted-Write .bak Recovery, Backup/Restore & v1->v2 Migration verified
ALL 16/16 TEST GROUPS (177 ENDPOINTS + REV-001..005 + CONCURRENT DB) PASSED 100%!
```

### 4. Giải trình chi tiết 6 điểm phản biện trong `REVIEW-16502ec.md` (`REV-001` .. `REV-006`)

1. **`REV-001` (`AUD-001` — Chặn cấp quyền `super_admin` từ client input trên `auth/token` và `center/app/login`)**:
   - Đã bổ sung kho định danh tài khoản bền vững `dbState.accounts` lưu mật khẩu băm muối (`hashAccountCredential`).
   - Mọi request tới `center/app/login` hoặc `auth/token` với `userId` không tồn tại (ví dụ `{"userId":"audit_unknown_user","role":"super_admin","tenantId":"foreign_tenant"}`) bị từ chối ngay với HTTP `401 ERR_UNKNOWN_ACCOUNT`.
   - Nếu client gửi `role` hoặc `tenantId` khác với bản ghi server của tài khoản đó, server từ chối với HTTP `403 ERR_CLIENT_ROLE_OR_TENANT_OVERRIDE_FORBIDDEN`.
   - Sai hoặc thiếu mật khẩu bị từ chối với HTTP `401 ERR_INVALID_CREDENTIALS`. `role` và `tenantId` trong JWT luôn lấy 100% từ bản ghi tài khoản trên server (kiểm tra tự động tại Nhóm `[11/16]`).
2. **`REV-002` (`AUD-002` — Fail-closed mặc định cho Billing & Xác thực trạng thái từ Provider Ledger, xóa local HMAC receipt helper)**:
   - `featureFlags.billingEnabled` mặc định là `false` (trả về HTTP `503 ERR_BILLING_FEATURE_DISABLED` cho mọi request tạo đơn hoặc xác thực biên lai khi chưa bật).
   - Đã xóa hàm ký biên lai nội bộ `createSignedPlayReceipt` và `NEXTAI_PLAY_BILLING_SECRET`.
   - Khi bật chế độ sandbox kiểm thử, `order/google/receipts` và `business/subscription/gp/receipt/verify` từ chối mọi request cố tình truyền `purchaseState` hoặc `expiryTimeMs` từ phía client (`400 ERR_CLIENT_STATE_INJECTION_FORBIDDEN`), và đối chiếu trực tiếp với sổ cái nhà cung cấp `dbState.providerPlayPurchases` (`400 ERR_PROVIDER_PURCHASE_NOT_FOUND` nếu không tồn tại, `409` nếu khác tài khoản `obfuscatedExternalAccountId`, `402` và thu hồi VIP khi trạng thái nhà cung cấp chuyển sang `REFUNDED`).
3. **`REV-003` (`AUD-006`, `AUD-007` — Hợp nhất 1 đường build APK duy nhất từ `apps/android/app/src/main/AndroidManifest.xml` & Kiểm tra binary APK bằng `aapt2 dump xmltree`)**:
   - Đã xóa hoàn toàn đoạn sinh `AndroidManifest.xml` tạm có `usesCleartextTraffic="true"` và đường dẫn cứng `E:\...` trong `apps/android/build_and_install_apk.ps1`.
   - `apps/android/build_and_install_apk.ps1` liên kết trực tiếp với file chuẩn `apps/android/app/src/main/AndroidManifest.xml` (`usesCleartextTraffic="false"`, `networkSecurityConfig="@xml/network_security_config"`, `targetSdkVersion="34"`), xuất cả APK debug và release, và chạy `aapt2 dump xmltree` ngay trên file `.apk` đã đóng gói để xác nhận `usesCleartextTraffic(0x010104ec)=false`, `networkSecurityConfig(0x01010527)=@0x7f070000`, `targetSdkVersion(0x01010270)=34` (lưu kết quả tại `apps/android/app/build/outputs/apk/manifest-xmltree-audit.txt`).
   - `.github/workflows/ci-quality-gate.yml`, `scripts/verify.ps1`, và `scripts/test.ps1` đều tự động chạy `build_and_install_apk.ps1 -SkipDeploy` để tái tạo APK từ bản clone sạch.
4. **`REV-004` (`AUD-005`, `AUD-008` — Khóa file liên tiến trình, chống xung đột `EPERM` khi chạy song song, khôi phục từ `.bak`, backup/restore & migration v1->v2)**:
   - `saveDurableState()` và `loadDurableState()` sử dụng khóa liên tiến trình `withCrossProcessFileLock` (`${dbPath}.lock`), tên file `.tmp` duy nhất theo `PID + timestamp + random`, cơ chế retry `EPERM`/`EACCES`/`EBUSY` trên Windows, bản sao lưu giao dịch `${dbPath}.bak`, và hàm nâng cấp `migrateDurableStatePayload` (`schemaVersion: 2`).
   - Nhóm test `[16/16]` khởi chạy đồng thời 6 tiến trình con Node.js ghi 72 lần liên tiếp vào cùng 1 file DB chia sẻ (0 lỗi `EPERM`), kiểm tra khôi phục tự động từ `.bak` khi file chính bị hỏng giữa chừng, kiểm tra `backupDurableState`/`restoreDurableState`, và kiểm tra migration `schemaVersion: 1 -> 2`. Đã kiểm chứng chạy song song `npm test` và `scripts/verify.ps1` cùng lúc đạt 100% PASS.
5. **`REV-005` (`AUD-004` — Fail-closed mặc định cho Earn/Payout & Đối soát giao dịch nhà cung cấp trước khi tất toán Webhook)**:
   - `featureFlags.earnEnabled` và `featureFlags.payoutEnabled` mặc định là `false` (trả về HTTP `503` mặc định).
   - Khi bật sandbox kiểm thử, `app/reward/video/withdrawal/webhook` không chỉ kiểm tra chữ ký HMAC mà còn bắt buộc đối soát `withdrawalId`, `providerTxId`, `amountFen` và trạng thái chuyển khoản trong `dbState.providerPayoutTransfers` (`422 ERR_PROVIDER_TRANSFER_RECONCILIATION_FAILED` nếu chưa có xác nhận từ nhà cung cấp).
6. **`REV-006` (`PAR-001..004` — Manifest SHA-256 của bộ tham chiếu gốc `Git-audit/reference-manifest.json`)**:
   - Đã tạo [`Git-audit/reference-manifest.json`](reference-manifest.json) lưu kích thước byte và mã băm SHA-256 của `Timemark.apk` gốc (`2a7893bc8fada904126ff4aacd1d061b80e0f614057894601af6e05476fc1e9b`), `timemark.db` gốc (`160dde7c72c93e2265baa72e6ce5e38e7e1dfda166024bd132c7b3ab73fe03c2`), 7 thư viện ARM64 `.so`, `WatermarkDefaultList.json`, và các file schema SQL; được `scripts/verify.ps1` kiểm tra tự động.
