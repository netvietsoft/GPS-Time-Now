# Audit tính tương đương với Time mark

Audit ID: `AUDIT-20261026-001`. Baseline mã nguồn NextAI: `4ba4cbe`.

## Phạm vi và nguyên tắc

Nguồn tham chiếu là bản Time mark v10.0.281 được mô tả trong `Docs/Reconstruction/overview.md`. Tài liệu đó phân loại `OBSERVED` (thấy trực tiếp trong APK/tài nguyên), `INFERRED` (suy luận), và `NEW_DESIGN` (thiết kế thêm). Cần giữ nguyên phân loại này cho từng ca kiểm tra. Không dùng một tỷ lệ parity tổng hợp hoặc số lượng route để thay thế thử nghiệm hành vi.

`Docs/Reconstruction/parity.md` hiện công bố tỷ lệ tổng hợp behavior `0.994`, UI `0.991`, API/data `1.000`; `.ai/reconstruction/ledger.json` ghi nhiều tính năng `complete`. Đây là **các tuyên bố cần chứng minh lại** trên bản tham chiếu và commit hiện tại, chưa phải kết luận độc lập của Git-audit.

## PAR-001 — Danh mục tham chiếu có thể truy xuất

Đội dev cung cấp hash/phiên bản APK Time mark gốc, thiết bị và Android version, danh mục tài nguyên tham chiếu, nguồn của mỗi hành vi quan sát, và quyền dùng tham chiếu. Nếu artifact gốc không thể chia sẻ trong repo, lưu ở kho kiểm soát truy cập và cung cấp manifest/hash, không commit APK hoặc dữ liệu nhạy cảm chỉ để làm bằng chứng.

**Đạt khi:** mỗi ca parity liên kết được với phiên bản nguồn và build NextAI chính xác; `OBSERVED`, `INFERRED`, `NEW_DESIGN` có nhãn rõ.

## PAR-002 — Camera, watermark, thời gian và GPS

Đối chiếu bản gốc và bản tái dựng trên cùng thiết bị/điều kiện: mở camera, chuyển trước/sau, ảnh, video, timestamp, GPS, preset `#10011`, `#57`, `#34`, `#52`, thay quyền/mất vị trí/mất mạng, chạy nền, file đầu ra và metadata. Ảnh chụp màn hình UI không chứng minh tính đúng của ảnh/video đã lưu.

**Đạt khi:** có test case, đầu ra được khử PII, sai khác được đo và giải thích; các luồng cốt lõi pass trên thiết bị thật.

## PAR-003 — Giao diện và điều hướng

So sánh từng màn hình/chế độ ở cùng độ phân giải, mật độ, font scale và Android version. Chụp cả trạng thái rỗng, lỗi, loading, quyền bị từ chối, xoay màn hình. Thay đổi có chủ ý do NextAI rebrand hoặc thiết kế mới phải có quyết định thiết kế, không tính là lỗi parity của Time mark.

**Đạt khi:** có ảnh cặp gốc/tái dựng, diff và ngưỡng chấp nhận cho từng màn hình; thao tác điều hướng được quay hoặc test tự động.

## PAR-004 — Dữ liệu, API và native

Đối chiếu schema Room v61, migration, format request/response, cách xử lý lỗi, file watermark, HMAC/JPEG, JNI và 7 thư viện `.so` bằng fixture gốc được phép sử dụng. Backend tự xây không có mã server Time mark để so 1:1; hành vi `INFERRED` cần hợp đồng và test độc lập. Các chức năng CMS, OLAP, games và payout mới được kiểm riêng theo tiêu chí sản phẩm NextAI.

**Đạt khi:** có mapping từng chức năng từ bằng chứng nguồn đến test và commit; test fixture dương/âm, lỗi mạng và dữ liệu sai; mọi suy luận quan trọng được xác nhận hoặc ghi là rủi ro còn mở.

## Trạng thái

`NEEDS EVIDENCE`: audit hiện mới đọc tài liệu parity và mã trong repo; chưa chạy bản Time mark gốc song song với APK tái dựng. Đội dev cần nộp bằng chứng `PAR-001..004`; auditor sẽ tái chạy trước khi xác nhận bất kỳ tỷ lệ parity nào.
