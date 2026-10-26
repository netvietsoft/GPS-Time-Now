# Architecture Overview — NextAI Camera & Enterprise Field SaaS Platform

> **Package**: `com.nextai.camera`  
> **Version**: `10.0.281-nextai`  
> **Backend & CMS Port**: `http://localhost:4580`

---

## 1. End-to-End System Topology

```mermaid
graph TB
    subgraph ClientLayer["Android Client (com.nextai.camera) & Web Clients"]
        AndroidApp["NextAI Camera Android App<br/>(Kotlin 2.0 + Jetpack Compose M3)"]
        NativeEngine["Native C++17 & Edge AI Engine<br/>(7 ARM64 .so | NCNN | JpegPacker | Aegis | .mcap)"]
        RoomDB[("NextAICameraDB (Room v61)<br/>42 Local SQLite Tables")]
        WebCMS["Enterprise Admin CMS<br/>(http://localhost:4580/cms)"]
        WebGames["HTML5 Web Games Hub<br/>(http://localhost:4580/games)"]
    end

    subgraph GatewayLayer["Self-Hosted Node.js + TypeScript Backend (Port 4580)"]
        APIRouter["/next/* Unified API Gateway<br/>(177 Endpoints | BaseResponse<T>)"]
        TodaySvc["Core Camera, Watermark, AI & VIP<br/>(TodayApiService - 68 Routes)"]
        ClusterSvc["Teamspace SaaS, Geofence & Reports<br/>(ClusterApiService - 91 Routes)"]
        EarnSvc["Embodied AI (.mcap) & Pix/SPEI Payouts<br/>(EarnApiService - 9 Routes)"]
        DualSvc["AntiFakeCode & Unpack Guard<br/>(DualModeApiService - 7 Routes)"]
        StsSvc["S3 / R2 / OSS STS Token Issuer<br/>(2 Routes + Local Object Storage)"]
    end

    subgraph DataLayer["Dual-Plane Database Architecture"]
        OLTP[("Relational OLTP Plane<br/>(42 Client Mirror Tables + SaaS Entities)")]
        OLAP[("OLAP Analytical Star Schema<br/>(fact_photo_captures, fact_attendance_events,<br/>fact_revenue_transactions, fact_robotics_mcap_telemetry)")]
    end

    AndroidApp <--> NativeEngine
    AndroidApp <--> RoomDB
    AndroidApp -->|"HTTPS / HTTP :4580/next/*"| APIRouter
    WebCMS --> APIRouter
    WebGames --> APIRouter
    APIRouter --> TodaySvc & ClusterSvc & EarnSvc & DualSvc & StsSvc
    TodaySvc & ClusterSvc & EarnSvc & DualSvc --> OLTP
    OLTP -->|"ETL / Materialized Views"| OLAP
    OLAP --> WebCMS
```

---

## 2. Core Architectural Pillars

1. **Cryptographic Trust & Anti-Tamper Capture (`PreviewActivity` + `Aegis` + `JpegPacker`)**:
   - Every photo synchronizes atomic time via `/next/realtime` and locks GPS satellite coordinates (`lat`, `lng`, `azimuth`, `altitude`).
   - `AegisCaptureSession` signs the hardware frame buffer to reject virtual cameras and mock GPS injectors.
   - `JpegPacker` embeds the clean, unwatermarked bounding-box pixel slices (`SliceDataInfo[]`) and signed metadata after the JPEG `EOI` (`0xFFD9`) marker (`ACQ-20261026-002`).
2. **Offline-First Local Persistence (`NextAICameraDB` v61)**:
   - All 42 Room tables (`new_watermarks_table`, `stand_alone_photo_table`, `anti_code_entry`, `lat_long_poi_entity`, `team_space_entry`, etc.) cache watermarks, pre-allocated anti-fake codes, and 500m grid geocoding so construction workers can capture verified photos deep underground or offline.
3. **Edge AI Computer Vision (`libnextai_native.so`)**:
   - Runs on-device NCNN models for Rebar Counting (`SteelDetect` `#57`), License Plate Recognition (`VLPR` `#52`), Low-Light Enhancement (`ZeroDCE`), and `PaddleOCR` text verification.
4. **Enterprise Teamspace SaaS & 4-Pillar Monetization**:
   - Combines individual VIP (`NextAI Plus`), B2B Teamspace subscriptions, hybrid test-safe Ad Mediation, and Embodied AI `.mcap` robotics crowdsourcing (`NextAI Earn`).
