# ADR-003: Dual-Plane Database: 42-Table Relational OLTP Mirror & OLAP Star Schema

- **Status**: `ACCEPTED`
- **Date**: `2026-10-26`
- **Owner**: `Agent 1 - ARCHITECT` & `Agent 2 - BACKEND`

## Context
Requirement 5 of `VAN-HANH/Task.txt` explicitly requires both **Relational & OLAP Schema** in `CONVERT/Backend/` with realistic sample data, while the Android client requires 100% compatibility with the 42 tables of `TodayCameraDB` (Room schema v61, rebranded as `NextAICameraDB`).

## Decision
Implement a dual-plane database architecture in `CONVERT/Backend/src/database/`:
1. **Relational OLTP Plane**: All 42 tables matching the exact column names, primary keys, and indexes of `NextAICameraDB` v61 (`new_watermarks_table` through `baby_info_table`) plus server-side multi-tenant SaaS tables.
2. **OLAP Analytical Plane**: Star-schema dimension tables (`dim_date`, `dim_geography`, `dim_teamspace`, `dim_watermark_template`) and fact tables (`fact_photo_captures`, `fact_attendance_events`, `fact_revenue_transactions`, `fact_robotics_mcap_telemetry`).

## Reason
- Ensures 100% lossless bidirectional synchronization between the Android Room database and the Backend OLTP store.
- Enables sub-millisecond multidimensional BI queries in the Admin CMS (`/cms` and `/next/analytics/olap/*`) without scanning transactional tables.

## Rejected Alternatives
- OLTP-only schema without analytical fact/dimension tables (fails Requirement 5).

## Impact
- Documented in [`Docs/Contracts/database-schema.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Contracts/database-schema.md) and distilled in [`ACQ-20261026-005`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/ACQUIREMENTS.md#acq-20261026-005--hybrid-oltp-42-table-mirror-and-olap-star-schema-analytics-on-single-port-4580).
