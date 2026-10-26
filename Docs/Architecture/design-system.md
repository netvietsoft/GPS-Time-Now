# Design System Architecture — NextAI Industrial High-Contrast v2.1

> **Owner**: `Agent 11 - DESIGNER` & `Agent 1 - ARCHITECT`  
> **Canonical Specification**: [`Docs/Design/DESIGN_SYSTEM.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Docs/Design/DESIGN_SYSTEM.md)  
> **Machine-Readable Tokens**: [`design/approved/design_tokens.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/approved/design_tokens.json)

---

## 1. Outdoor Sunlight Readability Engineering

Field engineers, construction inspectors, and delivery personnel operate **NextAI Camera** under direct outdoor sunlight (>10,000 lux). The design system enforces:
- **Glassmorphic High-Contrast Surfaces**: `rgba(15, 23, 42, 0.78)` with `backdrop-filter: blur(10px)` and `1px solid rgba(255, 255, 255, 0.10)` borders so live watermark text remains legible over bright sky or concrete backgrounds.
- **Semantic Trust Color Tokens**:
  - **NextAI Brand Primary**: `#2563EB` (Hover `#1D4ED8`)
  - **Aegis Verified Emerald**: `#10B981` (authentic NTP + GPS satellite lock)
  - **Industrial Safety Amber**: `#F59E0B` (active camera mode, rebar detection `#57`, warnings)
  - **VIP Monetization Gold**: `#FBBF24` → `#D97706` gradient (`NextAI Plus`)
  - **Embodied AI Cyan/Purple**: `#06B6D4` / `#6366F1` (`NextAI Earn` robotics telemetry)

---

## 2. Standard Viewport & Coordinate Grid (`390 × 844 dp`)

All mobile screens are engineered against the canonical `390 × 844 dp` (`xxhdpi 3.0x`) coordinate grid defined in `design/approved/design_tokens.json`:
- **Status Bar**: `y: 0..44 dp` (with `Aegis Secure` indicator notch)
- **Camera Top Control Bar**: `y: 44..92 dp` (`h: 48 dp`)
- **Camera Viewfinder Canvas**: `y: 92..652 dp` (`h: 560 dp`)
  - `miniMapCompassWidget`: `x: 16, y: 108, w: 96, h: 96`
  - `liveWatermarkCard` (`#10011`): `x: 16, y: 468, w: 320, h: 168`
  - `antiFakeCodeStrip`: `x: 362, y: 468, w: 16, h: 168`
- **Mode Selector Tabs**: `y: 652..692 dp` (`h: 40 dp`)
- **Bottom Shutter & Action Bar**: `y: 692..810 dp` (`h: 118 dp`)
