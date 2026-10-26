# DESIGN_SYSTEM.md — NextAI Industrial High-Contrast Design System v2.1

> **Canonical Source**: Section 52 of `Development Workspace Standard V2.1.2`  
> **Approved Record**: [`.ai/design/approved/DESIGN-0001.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/.ai/design/approved/DESIGN-0001.json)  
> **Machine-Readable Tokens**: [`design/approved/design_tokens.json`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/approved/design_tokens.json)  
> **Interactive Prototype**: [`design/approved/preview_screens.html`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/design/approved/preview_screens.html)

---

## 1. Brand Principles (`NextAI Camera` — `com.nextai.camera`)

1. **Outdoor Sunlight Legibility First**: High-luminance text (`#F8FAFC`) over deep slate surfaces (`#090D16` / `#111827`) and frosted glass overlays (`rgba(15, 23, 42, 0.78)` with `blur(10px)`) ensures coordinates and timestamps remain readable on construction sites under direct sun.
2. **Visible Cryptographic Trust**: Every camera and verification screen surfaces live trust indicators (`Aegis Secure`, satellite count `18 Sats`, `✓ NTP & GPS Verified`, and the vertical `CODE: NEXT9X-...` strip).
3. **Industrial Precision**: Monospace telemetry (`JetBrains Mono` / `Roboto Mono`) for clocks, DMS coordinates, and anti-fake codes paired with clean `Inter` / `Roboto` UI typography.

---

## 2. Color Tokens

| Token Group | Token Name | Hex / Value | Usage |
|---|---|---|---|
| **Brand** | `brand.primary` | `#2563EB` | Primary CTA buttons, official watermark `#10011` accent border |
| **Brand** | `brand.primaryHover` | `#1D4ED8` | Active/pressed button gradient end |
| **Brand** | `brand.industrialAmber` | `#F59E0B` | Active camera mode underline, `#57` AI Rebar Counter accent |
| **Semantic** | `semantic.verifiedEmerald` | `#10B981` | `Aegis Secure` badge, Geofence in-range pill, `AUTHENTIC` status |
| **Semantic** | `semantic.warningAmber` | `#F59E0B` | Trial badge, steganography indicator |
| **Semantic** | `semantic.dangerRed` | `#EF4444` | Tamper alert, out-of-geofence warning |
| **Semantic** | `semantic.infoCyan` | `#06B6D4` | Anti-fake code strip text, `.mcap` hand landmark overlay |
| **Monetization** | `monetization.vipGoldStart` / `End` | `#FBBF24` → `#D97706` | `NextAI Plus` individual VIP paywall & `JpegPacker` unpack CTA |
| **Monetization** | `monetization.earnPurple` / `Cyan` | `#6366F1` → `#06B6D4` | `NextAI Earn` Embodied AI wallet & `.mcap` recorder cards |
| **Dark Theme** | `darkTheme.background` / `surface` | `#090D16` / `#111827` | Primary app background and elevated cards |
| **Light Theme** | `lightTheme.background` / `surface` | `#F8FAFC` / `#FFFFFF` | High-ambient daylight mode |

---

## 3. Typography Scale

- **Clock Display (`clockDisplay`)**: `34px` (`26px` compact card), Weight `800`, Line-height `1.05`, `JetBrains Mono`
- **Heading 1 (`h1`)**: `22px`, Weight `700`, Line-height `1.25`
- **Heading 2 (`h2`)**: `18px`, Weight `700`, Line-height `1.30`
- **Heading 3 (`h3`)**: `15px`, Weight `600`, Line-height `1.35`
- **Body (`body`)**: `13px`, Weight `500`, Line-height `1.45`
- **Caption (`caption`)**: `11px`, Weight `500`, Line-height `1.35`
- **Anti-Fake Strip (`antiFakeCode`)**: `10px`, Weight `700`, Letter-spacing `0.08em`, Vertical-RL

---

## 4. Viewport, Spacing & Component States

- **Mobile Reference Viewport**: `390 × 844 dp` (`xxhdpi 3.0x`)
- **Desktop CMS & Games Hub Breakpoint**: `1440px` max-width (`980px` tablet/mobile collapse)
- **Border Radius Scale**: `6px` (tags), `10px` (watermark card), `12px` (buttons/inputs), `14px` (section cards), `44px` (device bezel preview)
- **Component Interaction States**: Every interactive element defines `default`, `hover`, `active`, `focus-visible` (`2px solid #3B82F6`), `disabled` (`opacity: 0.48`), `loading`, `empty`, and `error` states.
- **Forbidden UI Patterns**:
  - Low-contrast gray text over raw camera viewfinder without a `backdrop-filter` or dark scrim.
  - Any legacy brand string (`Timemark`, `XHey`, `OceanGalaxy`) in any user-facing label.
