# NextAI Standalone HTML5 Games & Interactive Domain Simulators (`CONVERT/games/`)

This directory fulfills **Task #9** (`Đặt thư mục tổng hợp các game riêng`) and provides five self-contained, 100% playable HTML5 Canvas + JavaScript games themed around the core technologies of **NextAI Global** (`nextai.global`, `nextai.technology`, `nextai.solution`).

## Directory Structure

| Folder | Title | Core Domain & Mechanics |
|---|---|---|
| `01-steel-pipe-ai-counter/` | **NextAI Steel Detect Challenge** | NCNN FP16 Steel Pipe cross-section counter with bounding boxes, region box-select, decoy rejection, and Precision/Recall/F1 scoring. |
| `02-embodied-hand-mcap-sim/` | **NextAI Embodied Robotics Hand Lab** | 21-Keypoint MediaPipe Hand Landmark inverse kinematics + 4-Sensor IMU oscilloscope (`Accel`, `Gyro`, `Mag`, `RotVec`), `.mcap` chunk recorder, and `>=85%` FOV QA gatekeeper. |
| `03-patrol-geofence-runner/` | **NextAI Security Patrol & QR Geofence Runner** | Top-down tactical patrol map with Haversine GPS geofence circles, QR checkpoint verification, and Aegis EMP pulses against Mock-GPS spoofer drones. |
| `04-truth-forensics-detective/` | **NextAI Photo Truth Detective (VerifyAuth)** | Cryptographic forensics inspector featuring `JpegPacker` binary trailer unpacking (`NEXTAI_JPEG_PACK_END_2026`), `AntiFakeCode` Base62 HMAC-SHA256 verification, and NTP clock drift detection. |
| `05-field-collage-builder/` | **NextAI Construction Before/After Puzzle** | Fast-paced engineering QA/QC puzzle matching `BEFORE` and `AFTER` site cards by Stake (`K12+400`) and Azimuth (`045° NE`) to compile PDF/Excel/KMZ milestones. |

## Launching

- **Standalone**: Open any `<game-folder>/index.html` directly in any modern browser.
- **Unified Web Portal (`Task #10`)**: Open `../apps/web/index.html` to launch all 5 games inside the **NextAI Global Super-Portal & HTML5 Game Arcade** with live `postMessage` + `localStorage` leaderboards, the 7-Screen NextAI Camera Live Simulator, and the NextAI Super Admin CMS Connector.
