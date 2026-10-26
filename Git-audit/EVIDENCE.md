# Bằng chứng đội dev phải cung cấp

Mỗi PR sửa `AUD-*` phải ghi: commit SHA, môi trường, phiên bản tool, lệnh tái hiện, đầu ra đã xóa dữ liệu nhạy cảm, kết quả trước/sau và liên kết artifact CI. Test trên máy cá nhân chỉ là bằng chứng bổ sung; auditor chạy lại trên commit PR.

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
