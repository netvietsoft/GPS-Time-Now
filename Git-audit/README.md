# Git audit — NextAI Camera

Audit ID: `AUDIT-20261026-001`

Baseline: `4ba4cbe` (`main`, 2026-10-26)
Status: **Chưa đủ điều kiện production**. Các kết luận dưới đây dựa trên mã nguồn và kiểm tra tại commit baseline; không coi tài liệu tiến độ là bằng chứng vận hành.

## Cách đội dev làm việc với audit

1. Chọn một mục `AUD-*` trong [FINDINGS.md](FINDINGS.md), tạo issue/PR và ghi ID vào tiêu đề.
2. Trong PR, nêu thay đổi, cách tái hiện trước sửa, kết quả sau sửa, và liên kết bằng chứng theo [EVIDENCE.md](EVIDENCE.md).
3. Auditor kiểm tra độc lập trên commit PR. Chỉ đóng mục khi tiêu chí chấp nhận đạt và bằng chứng có thể chạy lại.
4. Nếu có tính năng mới hoặc thay đổi phạm vi, bổ sung mục vào [PRODUCTION-BACKLOG.md](PRODUCTION-BACKLOG.md) và liên kết PR.

Mức ưu tiên: **P0** chặn phát hành và có nguy cơ bảo mật/tài chính; **P1** chặn production hoặc độ tin cậy cốt lõi; **P2** chất lượng, khả năng vận hành. `OPEN` nghĩa là chưa xác nhận đã sửa. Các mục cần bằng chứng là yêu cầu kiểm chứng, không mặc định là lỗi.

## Kết quả kiểm tra tại baseline

| Kiểm tra | Kết quả | Giới hạn |
|---|---|---|
| `scripts/verify.ps1` | PASS | Kiểm tra file quản trị, JSON, brand trong design; không chạy build Android, auth, thanh toán hay bảo mật API. |
| `Backend`: `npm install --no-package-lock --ignore-scripts`, `npm run build`, `npm test` | PASS, 10/10 nhóm | Test xác nhận nhiều endpoint trả HTTP 200 và dữ liệu mẫu; không chứng minh an toàn production. |
| Android Gradle và thiết bị thật | Chưa chạy trong audit này | Cần CI và báo cáo thiết bị theo EVIDENCE.md. |

Repo có [Tasksrequiring.md](../Tasksrequiring.md) liệt kê khóa production cần cấp. Không đưa khóa thật, dữ liệu cá nhân, token hoặc ảnh khách hàng vào PR/bằng chứng.
