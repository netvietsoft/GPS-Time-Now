# CAU_TRUC_THU_MUC.md — Cấu Trúc Thư Mục Chuẩn (NextAI Camera Workspace V2.1.2)

> **Hồ sơ Workspace (`workspace_profile`)**: `RECONSTRUCTION` + `PRODUCT` (`Design-Gated: true`)  
> **Cổng Backend / CMS / Games Hub**: `4580`

---

## Cây Thư Mục Tổng Thể Tại `CONVERT/`

```text
CONVERT/
├── README.md                          # Tổng quan dự án, hướng dẫn cài đặt & chạy nhanh
├── AGENTS.md                          # Hiến pháp AI Agent (Start Protocol & 24 Luật Cứng)
├── PROJECT_ERROR.md                   # Bộ nhớ lỗi bền vững (ERR-20261026-001 .. 004)
├── ACQUIREMENTS.md                    # Kiến thức dự án đã chưng cất (ACQ-20261026-001 .. 005)
├── PROJECT_MEMORY.md                  # Bộ nhớ ngữ cảnh vận hành hiện hành
├── TASK_LOG.md                        # Nhật ký hoạt động của các Agent theo thời gian
├── UPDATETODOS.md                     # Bảng theo dõi 10 yêu cầu Task.txt & 20 Phase Reconstruction
├── Tasksrequiring.md                  # Danh mục 14 khóa Production cần Chủ tịch bổ sung trước khi lên Store
├── LIBRARY.MD                         # Danh mục thư viện được phê duyệt (Kotlin, Compose, Node.js, C++17)
├── CLAUDE.md                          # Adapter trỏ về AGENTS.md cho Claude Code
├── .clinerules                        # Adapter trỏ về AGENTS.md cho Cline / Roo Code
├── .gitignore                         # Quy tắc loại trừ file build, node_modules, secrets
├── .env.example                       # Mẫu cấu hình biến môi trường (Port 4580, Test Ad Keys, ADB)
│
├── .agents/                           # Adapter layer cho Antigravity (Section 53)
│   ├── rules/
│   │   ├── workspace-standard.md
│   │   └── design-gate.md
│   ├── agents/
│   │   └── ui-designer/agent.md
│   └── skills/
│       └── design-mode/SKILL.md
│
├── .ai/                               # Trạng thái vận hành & Schema máy đọc (Section 22-24, 43-44)
│   ├── project.yaml                   # Cấu hình dự án, port 4580, design gate, parity gate
│   ├── agents.yaml                    # Phân quyền 14 Agent (Agent 0 -> Agent 13)
│   ├── state.json                     # Trạng thái vận hành toàn cục
│   ├── locks.json                     # Quản lý khóa file (fencing_token, lease_id)
│   ├── schemas/                       # 8 JSON Schemas chuẩn V2.1.2
│   ├── tasks/                         # Hàng đợi task (ready/, done/)
│   ├── handoffs/                      # Hồ sơ bàn giao phiên làm việc
│   ├── design/approved/               # Hồ sơ phê duyệt Design Gate (DESIGN-0001.json)
│   └── reconstruction/ledger.json     # Sổ cái theo dõi 9 Feature cốt lõi
│
├── Docs/                              # Tài liệu Kiến trúc, Hợp đồng, ADR & Thiết kế cho con người
│   ├── README.md
│   ├── rules.md                       # Luật code, rebranding NextAI, test & bảo mật
│   ├── CAU_TRUC_THU_MUC.md            # Tài liệu giải thích cây thư mục này
│   ├── Architecture/                  # 12 tài liệu kiến trúc chuyên sâu
│   ├── ADR/                           # 5 Hồ sơ Quyết định Kiến trúc (ADR-001 .. ADR-005)
│   ├── Contracts/                     # openapi.yaml, database-schema.md, error-codes.md, events.md
│   ├── Design/DESIGN_SYSTEM.md        # Hệ thống thiết kế NextAI Industrial High-Contrast v2.1
│   ├── Reconstruction/                # overview.md & parity.md
│   └── Changelog/CHANGELOG.md         # Nhật ký thay đổi sản phẩm
│
├── design/                            # Artifact thiết kế đã rebrand sang NextAI Camera
│   ├── approved/
│   │   ├── design_tokens.json
│   │   └── preview_screens.html
│   └── prototypes/
│       ├── design_tokens.json
│       └── preview_screens.html
│
├── Backend/                           # Hệ thống Server API (/next/*), CMS (/cms) & DB OLTP/OLAP (Port 4580)
│   ├── package.json
│   ├── tsconfig.json
│   ├── src/                           # 177 API endpoints, 42-table OLTP + OLAP Star Schema, Seed Data
│   ├── cms/                           # Giao diện quản trị Super Admin CMS (7 phân hệ)
│   └── tests/                         # Kiểm thử tự động Backend API & OLAP
│
├── apps/
│   └── android/                       # Ứng dụng Android gốc (com.nextai.camera)
│       ├── build.gradle.kts
│       └── app/
│           └── src/main/
│               ├── AndroidManifest.xml
│               ├── java/com/nextai/camera/  # Kotlin + Jetpack Compose + Room 42 bảng + Retrofit
│               ├── cpp/                     # C++17 JNI (NCNN, JpegPacker, Aegis, PaddleOCR, .mcap)
│               └── assets/                  # Shaders LUT, NCNN models, fonts, watermark presets
│
├── games/                             # Thư mục tổng hợp các Mini-Game độc lập & Web Game Hub
│   ├── index.html                     # Cổng Web Game HTML5 tổng hợp tất cả các game
│   ├── rebar-tycoon/                  # Game 1: Đếm Thép AI Công Trường
│   ├── geo-patrol-defender/           # Game 2: Tuần Tra Hàng Rào Địa Lý Geofence
│   ├── truth-detective/               # Game 3: Giám Định Ảnh Thật/Giả VerifyAuth
│   ├── robo-hand-trainer/             # Game 4: Huấn Luyện Bàn Tay Robot 21 Khớp (.mcap)
│   └── tower-crane-builder/           # Game 5: Xây Dựng Tháp Cẩu Công Trường
│
└── scripts/                           # Kịch bản kiểm tra bộ nhớ dự án, verify & test thiết bị thật
    ├── validate-project-memory.ps1
    ├── validate-project-memory.sh
    ├── verify.ps1
    └── test.ps1
```
