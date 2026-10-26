# Authentication & Authorization Architecture

> **Owner**: `Agent 1 - ARCHITECT` & `Agent 9 - SECURITY`  
> **Endpoints**: `/next/center/app/login`, `/next/center/app/login/sendcode`, `/next/center/app/token/refresh`, `/next/center/app/user`, `/next/center/app/bind`

---

## 1. Multi-Provider Authentication Flow

**NextAI Camera** (`com.nextai.camera`) supports 4 authentication providers unified under `/next/center/app/login`:
1. **Email OTP (`loginType = "email_code"`)**: Sends a 6-digit verification code via `/next/center/app/login/sendcode` and issues a signed JWT access token + refresh token.
2. **Google Sign-In (`loginType = "google"`)**: Validates the Google OAuth 2.0 ID token (`GOOGLE_WEB_CLIENT_ID`).
3. **Microsoft Azure AD / MSAL (`loginType = "microsoft"`)**: Authenticates enterprise users via PKCE (`res/raw/auth_config_single_account.json`) and enables automatic photo syncing to Microsoft OneDrive / SharePoint (`/next/web/teamspace/app/integration/*`).
4. **Guest / Offline Device Mode**: Allows immediate camera capture upon installation using a hardware-bound `deviceId` and pre-allocated offline `anti_code_entry` tokens.

---

## 2. Teamspace Role-Based Access Control (RBAC)

Within **NextAI Teamspace** (`/next/app/group/role/*`), permissions are enforced at both the Team level and Project level:
- **Owner (`role = 1`)**: Full billing, team deletion, S3 quota management, and global watermark lock settings.
- **Admin / Site Manager (`role = 2`)**: Create projects, configure geofence timeclocks (`lat`, `lng`, `radius`), lock watermark fields (`lock_time_location = 1`), and export batch PDF/Excel/KMZ reports.
- **Field Member (`role = 3`)**: Capture verified site photos, clock in/out within geofence radius, and submit safety checklists.
- **External Auditor / Guest (`role = 4`)**: Read-only access to assigned project photo streams and `VerifyAuth` certificates.
