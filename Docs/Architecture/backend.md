# Backend & CMS Architecture — Node.js + TypeScript Server on Port 4580

> **Owner**: `Agent 1 - ARCHITECT` & `Agent 2 - BACKEND`  
> **Location**: [`CONVERT/Backend/`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Backend)  
> **Port**: `4580` (`http://localhost:4580`)  
> **Related ADR**: [`ADR-002`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/ADR/ADR-002-nodejs-typescript-backend.md)

---

## 1. Unified Multi-Service Architecture on Port 4580

Per `VAN-HANH/Task.txt` Requirement #5, the Backend is self-hosted in `CONVERT/Backend/` using **TypeScript + Node.js** on port `4580` and consolidates 5 logical service domains plus the Admin CMS and HTML5 Web Game Hub:

```text
CONVERT/Backend/
├── package.json                       # Scripts: start, dev, build, test
├── tsconfig.json                      # Strict TypeScript 5 configuration
├── src/
│   ├── server.ts                      # HTTP/Express bootstrap on Port 4580 + CORS + Request Logger
│   ├── database/
│   │   ├── schema.ts                  # 42-Table OLTP Room Mirror + Server SaaS Tables + OLAP Star Schema
│   │   └── seed.ts                    # Rich realistic sample data (Metro Line 1, Watermarks #10011/#57/#34/#52, SKUs, Earn tasks)
│   ├── routes/
│   │   ├── todayApi.ts                # 68 Core Camera, NTP Realtime, Weather, POI, Watermark, AI, Auth & VIP endpoints
│   │   ├── clusterApi.ts              # 91 Teamspace SaaS, Projects, Photos, Geofence, Checklists & Business endpoints
│   │   ├── earnApi.ts                 # 9 Embodied AI (.mcap) tasks, balance, submit & Pix/SPEI withdrawal endpoints
│   │   ├── dualModeApi.ts             # 7 AntiFakeCode generation, JpegPacker remove check & user event count endpoints
│   │   ├── storageApi.ts              # S3 / OSS STS temporary credential issuer & local object storage upload/download
│   │   └── olapApi.ts                 # OLAP multidimensional analytics endpoints for CMS BI dashboards
│   └── utils/
│       └── response.ts                # Canonical BaseResponse<T> envelope ({ code: 200, msg: "success", status: 0, data })
└── cms/
    └── index.html                     # 7-Module Super Admin CMS Single-Page Application (/cms)
```

---

## 2. 7-Module Enterprise Admin CMS (`http://localhost:4580/cms`)

1. **OLAP Executive BI Dashboard**: Real-time charts of daily photo captures, active Teamspaces, MRR/ARR subscription revenue, and `.mcap` robotics dataset volume.
2. **Watermark Template & Skin Studio**: CRUD management for Official (`#10011`), PGC (`#57` Rebar AI, `#34` Geofence, `#52` VLPR), and UGC community watermarks.
3. **VerifyAuth & Anti-Fake Audit Center**: Live lookup and verification of `AntiFakeCode` (`NEXT9X-...`), `PhotoCode`, NTP time drift, and cheat flags.
4. **Teamspace SaaS & Geofence Manager**: Manage enterprise teams, project geofences, member quotas, S3 storage limits, and attendance logs.
5. **Monetization, SKUs & AdX Controller**: Configure Google Play / Stripe SKUs (`00000001`, `plus_1year_onetime`), `AdFreePass` rules, and Google Test AdMob placements.
6. **Embodied AI ("Earn") Task & Payout Console**: Publish robotics collection tasks, inspect uploaded `.mcap` hand-tracking metadata, and approve BRL Pix / MXN SPEI payouts.
7. **System Health & 42-Table Database Explorer**: Inspect live row counts and query any of the 42 OLTP tables or 8 OLAP analytical tables directly from the browser.
