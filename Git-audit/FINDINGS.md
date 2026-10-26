# Các phát hiện cần sửa hoặc chứng minh

Baseline: `4ba4cbe`. Mọi đường dẫn và số dòng là tại baseline. `OPEN` cho đến khi auditor xác nhận PR và bằng chứng.

## AUD-001 — P0 — API không kiểm tra xác thực và phân quyền

- **Quan sát:** `Backend/src/server.ts:1116-1248` tiếp nhận request rồi gọi `handleApiRoute` mà không kiểm tra `Authorization`; `/cms` được phục vụ tại `:1133-1137`. `Access-Control-Allow-Origin: *` tại `:1119`. `NextAiNetworkConfig.kt:35` dùng token mặc định trong app nhưng server không xác thực nó.
- **Tác động:** Client không đăng nhập vẫn truy cập chức năng và dữ liệu nghiệp vụ. CORS rộng làm tăng rủi ro khi có phiên trình duyệt; bản thân CORS không thay thế auth.
- **Yêu cầu:** Xác thực server, phân quyền theo tenant/role cho mọi API và CMS; từ chối mặc định; giới hạn CORS theo origin thực tế; audit log hành động quản trị.
- **Chấp nhận:** Test tự động chứng minh request thiếu/sai/hết hạn token trả 401, sai vai trò hoặc tenant trả 403; người dùng hợp lệ chỉ thấy dữ liệu của mình; CMS có phiên và quyền admin.

## AUD-002 — P0 — Xác minh thanh toán luôn thành công

- **Quan sát:** `Backend/src/server.ts:898-906` trả `receiptVerified: true`, `vipActivated: true` từ `orderNo`/`productId` do request cung cấp, không đối chiếu với Google Play.
- **Tác động:** Có thể cấp VIP bằng payload tự tạo.
- **Yêu cầu:** Xác minh purchase token qua nhà cung cấp, đối chiếu package/SKU/account/trạng thái, chống replay và ghi entitlement bền vững.
- **Chấp nhận:** Test với token giả, token của tài khoản khác, hoàn tiền, hết hạn, lặp token đều thất bại; sandbox purchase hợp lệ cấp quyền đúng một lần.

## AUD-003 — P0 — Chữ ký chống giả dùng khóa cố định trong source

- **Quan sát:** `Backend/src/server.ts:243-251` chứa `HMAC_SECRET` hằng số. Bất kỳ ai đọc repo public có thể tái tạo chữ ký. `/health` báo `brandSanitized`/schema bằng hằng số, không phải phép kiểm thực tế (`:1151-1168`).
- **Yêu cầu:** Thay khóa bị lộ, quản lý bằng secret manager, tách khóa theo môi trường, có phiên bản và kế hoạch xoay khóa; thiết kế xác minh chống phát hành mã giả.
- **Chấp nhận:** Khóa production không nằm trong repo/artifact/log; test chữ ký giả, chỉnh ảnh và khóa cũ bị từ chối theo chính sách; tài liệu xoay khóa và bằng chứng thử nghiệm.

## AUD-004 — P0 — Endpoint tiền thưởng và rút tiền đang mô phỏng thành công

- **Quan sát:** `Backend/src/server.ts:1040-1074` cộng `1500` Fen khi submit `.mcap`, trả `qaStatus: APPROVED`; rút tiền trả `COMPLETED` theo giá trị request, không thấy kiểm tra số dư, danh tính, hạn mức hoặc giao dịch nhà cung cấp.
- **Yêu cầu:** Cô lập simulator khỏi production; luồng duyệt, ledger bất biến, idempotency, kiểm tra số dư, hạn mức, KYC/anti abuse phù hợp và callback có chữ ký trước khi ghi hoàn tất.
- **Chấp nhận:** Request không đủ tiền, lặp, giả callback, sai tenant, `.mcap` rỗng/không hợp lệ đều thất bại; thử nghiệm sandbox có đối soát số dư.

## AUD-005 — P1 — Backend dùng trạng thái RAM và route trả dữ liệu mẫu

- **Quan sát:** `Backend/src/server.ts:253` tạo `dbState` trong RAM; `:1100-1112` trả catch-all HTTP 200 với `requestEcho`; `:898-906` và `:1064-1074` mô phỏng nghiệp vụ. DDL nằm trong `Backend/schema/` nhưng chưa chứng minh persistence được nối vào luồng request.
- **Yêu cầu:** Lưu nghiệp vụ trong DB có migration, transaction và ràng buộc tenant; endpoint chưa triển khai phải trả lỗi rõ, không trả thành công giả.
- **Chấp nhận:** Dữ liệu tồn tại sau restart; API lạ trả 404; mỗi endpoint quan trọng có test hành vi và lỗi, không chỉ kiểm tra envelope 200.

## AUD-006 — P1 — HTTP thường và cấu hình LAN trong Android

- **Quan sát:** `apps/android/app/src/main/AndroidManifest.xml:32-33` cho phép cleartext; `res/xml/network_security_config.xml:3` cho phép HTTP toàn bộ; `NextAiNetworkConfig.kt:18-20,35` chứa địa chỉ HTTP LAN và token dev mặc định.
- **Yêu cầu:** Release chỉ dùng HTTPS/TLS đến endpoint production cấu hình theo môi trường; hạn chế HTTP cho debug; lưu token bằng cơ chế an toàn và refresh thực.
- **Chấp nhận:** APK/AAB release không cho cleartext, không chứa token dev hoặc IP LAN; kiểm tra MITM/certificate và đăng nhập thành công trên môi trường staging.

## AUD-007 — P1 — Quality gate chưa bao phủ build và test sản phẩm

- **Quan sát:** `scripts/verify.ps1` chỉ kiểm tra tài liệu/JSON/brand trong design. `scripts/test.ps1:26-37` bỏ qua backend test khi thiếu `node_modules`, `:53-56` vẫn in thành công. Không có `.github/workflows/`; không thấy `apps/android/app/src/test` hoặc `src/androidTest` trong baseline.
- **Yêu cầu:** CI bắt buộc build Android debug/release, unit/instrumentation cần thiết, backend build/test, lint/typecheck, secret/dependency scan; bất kỳ gate bị bỏ qua phải báo FAIL hoặc SKIPPED có lý do và chặn release.
- **Chấp nhận:** PR cố tình làm hỏng build/test bị CI từ chối; báo cáo gồm commit SHA, phiên bản tool, lệnh, exit code và artifact.

## AUD-008 — P1 — Chưa có chuỗi phát hành và vận hành production được chứng minh

- **Quan sát:** `.ai/project.yaml` ghi `status: development`; [Tasksrequiring.md](../Tasksrequiring.md) còn 14 nhóm cấu hình/khóa cần xử lý; `Backend/package.json` không có lockfile trong baseline; chưa thấy pipeline deploy/rollback trong `.github/`.
- **Yêu cầu:** Tạo môi trường staging/prod, release signing bảo mật, cấu hình ngoài source, migration/backup/restore, giám sát và cảnh báo, rollback, quy trình duyệt phát hành.
- **Chấp nhận:** Dry run từ tag đến staging và rollback có log; backup restore thử được; crash/error/latency và thanh toán có cảnh báo; AAB release được ký bằng khóa production trong kho bảo mật.

## AUD-009 — P2 — Tài liệu kết quả dễ bị hiểu là chứng nhận production

- **Quan sát:** `.ai/state.json` ghi tất cả health `pass`; `UPDATETODOS.md` ghi nhiều hạng mục `COMPLETED`, trong khi các hành vi mô phỏng ở trên còn tồn tại. `Backend/tests/verify_backend.js:129-153` dựa trên kết quả thành công của simulator.
- **Yêu cầu:** Phân biệt demo, local test, staging, production trong báo cáo; liên kết mỗi khẳng định với artifact/commit/log kiểm chứng.
- **Chấp nhận:** Không còn mục `production-ready` chỉ dựa trên HTTP 200 hoặc trạng thái tự khai báo; dashboard hiển thị kết quả gate thật và giới hạn của chúng.
