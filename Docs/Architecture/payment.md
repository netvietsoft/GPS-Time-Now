# Payment, Dual Paywall & Embodied AI Payout Architecture

> **Owner**: `Agent 5 - PAYMENT`  
> **Production Key Checklist**: [`Tasksrequiring.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md)

---

## 1. Dual-Mode Paywall (`PurchaseScreen` / `PurchaseActivity`)

The payment architecture implements a **Dual-Tab Paywall** backed by `/next/goods/query`, `/next/order/create`, and `/next/order/google/receipts`:

### Tab 1: `NextAI Plus` (B2C Individual Pro)
- **SKUs**:
  - `00000001` (`SUBS`): Annual Plan — **$29.99 / year** with 3-Day Free Trial (Save 64%).
  - `0000000220240807` (`SUBS`): Retention Exit Promo Annual Plan — **$19.99 / year** (triggered by `PaywallExitSurvey` when user taps Close).
  - `plus_1year_onetime` (`INAPP`): 1-Year One-Time Purchase — **$34.99** (non-recurring).
  - `pro-monthly-non-stripe` (`SUBS`): Monthly Plan — **$6.99 / month**.
- **Unlocked Entitlements**:
  - `JpegPacker` Lossless Watermark Removal (`/next/workgroup/photo/watermark/remove/check`)
  - Unlimited AI Logo Cutout (`/next/logo/cutout`) & LLM Note Generation (`/next/ai/generatePrompt`)
  - Batch Excel (`.xlsx`) & Google Earth (`.kmz`) Report Export
  - 100% Ad-Free Experience

### Tab 2: `Teamspace Business Plan` (B2B Multi-Seat SaaS)
- **Channels**: Google Play seat subscriptions (`/next/business/subscription/gp/goods/list`) or **Stripe Web Checkout** (`/next/app/plan/shortLink/create` -> `?plan-id=`).
- **Unlocked Entitlements**: Expanded AWS S3/R2 cloud storage (`100 GB+`), Geofence Timeclock (`app/group/timeclock/geofence/*`), OneDrive/Google Drive real-time sync, and Project-level RBAC.

---

## 2. Embodied AI ("Earn") Wallet & Pix / SPEI Payouts

Users who record first-person robotics training videos (`.mcap` with 21 hand landmarks + 4-IMU sensors) earn cash rewards credited to `/next/app/reward/video/balance`:
- **Brazil Payouts (Pix)**: Currency `BRL`, minimum withdrawal `R$ 30.00` (`minAmountFen = 3000`), maximum `R$ 5,000.00`.
- **Mexico Payouts (SPEI)**: Currency `MXN`, minimum withdrawal `$100.00 MXN` (`minAmountFen = 10000`).
- **VIP Exchange**: Users can also redeem approved `.mcap` submissions for 30 days of free **NextAI Plus** (`key_earn_vip_status`).
