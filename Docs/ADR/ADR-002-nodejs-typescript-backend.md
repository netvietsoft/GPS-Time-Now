# ADR-002: Self-Hosted Node.js + TypeScript Backend & CMS on Port 4580

- **Status**: `ACCEPTED`
- **Date**: `2026-10-26`
- **Owner**: `Agent 1 - ARCHITECT` & `Agent 2 - BACKEND`

## Context
Requirement 5 of `VAN-HANH/Task.txt` specifies replacing all external APIs with a self-hosted internal API in `CONVERT/Backend/` comprising API, CMS, Server, and Relational & OLAP Schema, implemented in **TypeScript + Node.js**, populated with sample data, and running on an available port in the range `1000..10000`.

## Decision
Implement the unified Backend server, 177 `/next/*` API endpoints, 7-module Admin CMS (`/cms`), S3/OSS STS emulator (`/s3`), and HTML5 Web Games Hub (`/games`) in **Node.js 20+ and TypeScript 5.x** listening on **Port `4580`**.

## Reason
- Port `4580` is verified free within `1000..10000`.
- Single unified server process allows instant startup (`npm start`) without complex multi-container orchestration during local and ADB physical device testing.
- Preserves the exact `BaseResponse<T>` JSON wire contract across all 177 endpoints.

## Rejected Alternatives
- Relying on external third-party cloud APIs (violates Requirement 5).
- Splitting the 5 Retrofit service groups across 5 separate ports.

## Impact
- Android client connects to `http://localhost:4580/next/` (via `adb reverse tcp:4580 tcp:4580` on Samsung Galaxy A50) or `http://10.0.2.2:4580/next/` on emulators.
