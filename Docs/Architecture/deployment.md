# Deployment & Real Device Verification Runbook

> **Owner**: `Agent 10 - DEVOPS` & `Agent 6 - TESTER`  
> **Backend Port**: `4580`  
> **Real Test Device (`VAN-HANH/Task.txt` #8)**: Samsung Galaxy A50 (`192.168.1.2:35977`, Pairing Port `28721`, Pairing Code `741742`)

---

## 1. Starting the Self-Hosted Backend, CMS & Web Games Hub (Port `4580`)

```powershell
cd E:\Decompiler\APP\IMAGE\Timemark\CONVERT\Backend
npm install
npm run build
npm start
```

Verify all endpoints are live:
- `GET http://localhost:4580/health` -> `200 OK`
- `POST http://localhost:4580/next/realtime` -> Atomic server time in `BaseResponse<RealTimeInfo>`
- `GET http://localhost:4580/cms` -> 7-Module Enterprise Admin CMS
- `GET http://localhost:4580/games` -> HTML5 Web Games Hub (5 playable games)

---

## 2. Building & Deploying to Samsung Galaxy A50 (`192.168.1.2:35977`)

Per `VAN-HANH/Task.txt` Requirement #8:
- **Device**: Samsung Galaxy A50 (`SM-A505F`)
- **Wireless ADB Connect Address**: `192.168.1.2:35977`
- **Wireless ADB Pairing Port**: `28721`
- **Wireless ADB Pairing Code**: `741742`

Execute via automated script [`scripts/test.ps1`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/scripts/test.ps1) or manually:
```powershell
adb pair 192.168.1.2:28721 741742
adb connect 192.168.1.2:35977
adb -s 192.168.1.2:35977 reverse tcp:4580 tcp:4580
cd E:\Decompiler\APP\IMAGE\Timemark\CONVERT\apps\android
.\gradlew.bat installDebug
adb -s 192.168.1.2:35977 shell am start -n com.nextai.camera/.MainActivity
```
Using `adb reverse tcp:4580 tcp:4580` routes `http://localhost:4580/next/` on the physical Samsung Galaxy A50 directly to the local Node.js Backend running on port `4580`.
