# Security Architecture — Aegis Anti-Spoofing, JpegPacker & Secret Hygiene

> **Owner**: `Agent 9 - SECURITY` & `Agent 1 - ARCHITECT`  
> **Related Knowledge**: [`ACQ-20261026-002`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/ACQUIREMENTS.md#acq-20261026-002--jpegpacker-tail-steganography-and-aegis-anti-tamper-verification-architecture)

---

## 1. 4-Layer Anti-Fake Photo Authenticity (`VerifyAuth`)

**NextAI Camera** prevents time/location forgery by combining hardware, network, cryptographic, and steganographic controls:

1. **Atomic Network Time Lock (`/next/realtime`)**:
   - Computes monotonic `SystemClock.elapsedRealtime()` offset against the server's atomic clock so changing the phone's system clock cannot falsify the watermark timestamp.
2. **Hardware Camera & GPS Integrity (`AegisCaptureSession`)**:
   - Inspects Android camera HAL characteristics and location provider flags (`isFromMockProvider`, satellite SNR count) to reject virtual camera hooks and mock GPS apps, reporting anomalies to `/next/photo/datacenter/odd`.
3. **EOI Tail Steganography (`JpegPacker`)**:
   - Appends an encrypted container after JPEG `0xFFD9` (`PackPosition = 1`) holding the original unwatermarked pixel slices (`SliceDataInfo[]`) and an HMAC-SHA256 digest over the capture telemetry.
4. **Dual Verification (`VerifyAuth` + `PaddleOCR`)**:
   - `/next/truthful/Query/batch/detail` cross-checks the cryptographic tail signature, the server registry (`photo_code_entity`), and the visible text extracted via `PaddleOCR` to detect Photoshop pixel tampering.

---

## 2. Secret Hygiene & Workspace Guardrails

- All 24 sensitive keys identified during decompilation are documented and sanitized:
  - Test AdMob IDs are active by default.
  - Production keys required before store release are isolated in [`Tasksrequiring.md`](file:///E:/Decompiler/APP/IMAGE/Timemark/CONVERT/Tasksrequiring.md).
  - `scripts/validate-project-memory.ps1` scans `PROJECT_ERROR.md` and `ACQUIREMENTS.md` to ensure no live private keys or credentials are ever committed.
