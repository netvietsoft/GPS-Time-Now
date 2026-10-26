# NextAI Camera Enterprise Backend, Super Admin CMS & OLAP Data Warehouse

Self-contained TypeScript + Node.js (`v20.20.2`) Backend Gateway, 7-Module Super Admin CMS, Truth H5 Verification Portal, Teamspace Web SaaS Portal, 42-Table Relational Schema, and ClickHouse/StarRocks/DuckDB OLAP Analytics Schema.

## Quick Start

```bash
# Start server on default port 4580 (auto-falls back to free port in 1000..10000)
node dist/server.js

# Run automated verification test suite (177 APIs + Haversine + HMAC + AdMob Test IDs + Brand Audit)
node tests/verify_backend.js
```

## Built-in Web Interfaces
- **`GET /cms`** — NextAI Super Admin CMS Dashboard (7 Modules: Watermark Studio, Teamspace & PostGIS Geofence, Anti-Fake & Aegis Truth Center, Billing & Plus VIP, AdX Test Key Console, Embodied AI `.mcap` & Earn, OLAP Warehouse & AI Gateway).
- **`GET /truth`** — NextAI Photo Truth Verification H5 Portal (`NX9X-*` cryptographic verification).
- **`GET /teamspace`** — NextAI Teamspace Enterprise Web Portal & Stripe Checkout Simulator.
- **`GET /api/catalog`** — Complete JSON catalog of all 177 API endpoints.
- **`GET /health`** — Live health check, uptime, active port, and schema telemetry.

## Directory Structure
- `src/server.ts` & `dist/server.js` — Core HTTP/REST gateway supporting both `/nextai/v1/<path>` and `/next/<path>` prefixes with standard `BaseResponse<T>` envelopes.
- `public/` — `cms.html`, `truth.html`, `teamspace.html`.
- `schema/relational_schema_42_tables.sql` — 42-table relational DDL & seed records (`nextai_camera_db`).
- `schema/olap_analytics_schema.sql` — Star-schema OLAP warehouse (`nextai_olap_warehouse`) with 5 Fact tables, 3 Dimension tables, and 2 real-time Materialized Views.
- `data/seed/` — Sanitized SQLite database (`nextai_camera.db`), CSVs, and JSON seed payloads.
- `tests/verify_backend.js` — Automated end-to-end verification suite.
