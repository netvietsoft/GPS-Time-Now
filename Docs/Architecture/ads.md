# Advertising & Mediation Architecture (Official Test Keys Strategy)

> **Owner**: `Agent 4 - INTEGRATION/API` & `Agent 5 - PAYMENT`  
> **Requirement**: `VAN-HANH/Task.txt` #4 ("Thay thế các key quảng cáo bằng các key quảng cáo test")  
> **Production Key Checklist**: [`Tasksrequiring.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md)

---

## 1. Mandatory Google Official Test Ad Keys

All production ad keys from the original APK have been stripped and replaced with **Google's Official Test AdMob IDs** so developers and QA testers can safely trigger and inspect ads on emulators and physical devices (Samsung Galaxy A50 `192.168.1.2:35977`) without policy violations:

| Placement / Config | Google Official Test ID Configured in `CONVERT/` | Where Configured |
|---|---|---|
| **AdMob Application ID** | `ca-app-pub-3940256099942544~3347511713` | `AndroidManifest.xml` & `.env.example` |
| **Splash / App Open Ad** | `ca-app-pub-3940256099942544/9257395921` | `AdConfig.kt` & `/next/app/adx/global_fetch_exposure_config` |
| **After-Save / Export Interstitial** | `ca-app-pub-3940256099942544/1033173712` | `AdConfig.kt` & `/next/app/adx/global_fetch_exposure_config` |
| **Watermark Store Native Banner** | `ca-app-pub-3940256099942544/2247696110` | `AdConfig.kt` & `/next/app/adx/global_fetch_exposure_config` |
| **Rewarded Video (`AdFreePass` / Single Unpack)** | `ca-app-pub-3940256099942544/5224354917` | `AdConfig.kt` & `/next/app/adx/global_fetch_exposure_config` |

---

## 2. NextAI First-Party AdX Exposure Controller

On startup, the Android client queries `POST /next/app/adx/global_fetch_exposure_config` on Port `4580`:
- If the user holds an active **NextAI Plus** subscription (`vipStatus == 1`) or an active **AdFreePass** (`AdFreePassState` timer > 0 from watching a Rewarded Ad or completing an **Earn** `.mcap` task), the server returns `adsEnabled: false` and suppresses all ad placements.
- Otherwise, the server returns frequency-capped placement rules using the Google Official Test Unit IDs above.
