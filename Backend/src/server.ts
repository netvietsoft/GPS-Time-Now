import * as http from 'http';
import * as fs from 'fs';
import * as path from 'path';
import * as crypto from 'crypto';
import * as url from 'url';

export interface BaseResponse<T = any> {
  code: number;
  msg: string;
  status: number;
  data: T;
}

export interface EndpointDef {
  id: number;
  service: string;
  path: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  description: string;
}

// Official Google AdMob Test IDs (Task.txt #4)
export const GOOGLE_ADMOB_TEST_CONFIG = {
  appId: 'ca-app-pub-3940256099942544~3347511713',
  bannerAdUnitId: 'ca-app-pub-3940256099942544/6300978111',
  interstitialAdUnitId: 'ca-app-pub-3940256099942544/1033173712',
  rewardedAdUnitId: 'ca-app-pub-3940256099942544/5224354917',
  nativeAdUnitId: 'ca-app-pub-3940256099942544/2247696110',
  appOpenAdUnitId: 'ca-app-pub-3940256099942544/9257395921',
  isTestDevice: true,
  adFreePassRewardMinutes: 1440,
  exposureFrequencyCap: {
    interstitialIntervalSec: 180,
    maxDailySplashAds: 4,
    vipBypassAllAds: true
  }
};

// Complete 177 API Endpoints Catalog across the 5 Service Interfaces
export const ENDPOINT_CATALOG: EndpointDef[] = [
  // 1. TodayApiServiceKt (68 Endpoints: #1 - #68)
  { id: 1, service: 'TodayApiServiceKt', path: 'android/config', method: 'POST', description: 'System config, feature flags & Edge AI model manifests' },
  { id: 2, service: 'TodayApiServiceKt', path: 'realtime', method: 'POST', description: 'Atomic NTP server time synchronization against clock tampering' },
  { id: 3, service: 'TodayApiServiceKt', path: 'network/ip', method: 'POST', description: 'Public IP geolocation and country detection' },
  { id: 4, service: 'TodayApiServiceKt', path: 'weather/current/detail', method: 'POST', description: 'Real-time weather, temperature, wind & AQI at capture GPS' },
  { id: 5, service: 'TodayApiServiceKt', path: 'v2/place/list/group', method: 'POST', description: 'Nearby POI list and street address around GPS coordinates' },
  { id: 6, service: 'TodayApiServiceKt', path: 'v2/place/search/group', method: 'POST', description: 'Keyword search for POI locations' },
  { id: 7, service: 'TodayApiServiceKt', path: 'place/Nominatim/reverse', method: 'POST', description: 'OpenStreetMap Nominatim reverse geocoding' },
  { id: 8, service: 'TodayApiServiceKt', path: 'location/select/feedback', method: 'POST', description: 'User location selection telemetry feedback' },
  { id: 9, service: 'TodayApiServiceKt', path: 'config/watermarks', method: 'POST', description: 'Default watermark template list by category' },
  { id: 10, service: 'TodayApiServiceKt', path: 'watermark/ours/query', method: 'POST', description: 'Query official NextAI watermark templates' },
  { id: 11, service: 'TodayApiServiceKt', path: 'watermark/pgc/query', method: 'POST', description: 'Query industry PGC watermark templates' },
  { id: 12, service: 'TodayApiServiceKt', path: 'watermark/item/edit/history/upload', method: 'POST', description: 'Sync watermark field edit history to cloud' },
  { id: 13, service: 'TodayApiServiceKt', path: 'watermark/share/private/query', method: 'POST', description: 'Fetch private shared watermark by shareCode' },
  { id: 14, service: 'TodayApiServiceKt', path: 'watermark/share/public/register', method: 'POST', description: 'Publish user watermark template to public store' },
  { id: 15, service: 'TodayApiServiceKt', path: 'watermark/share/public/status/update', method: 'POST', description: 'Update visibility status of shared watermark' },
  { id: 16, service: 'TodayApiServiceKt', path: 'watermark/ugc/code/search', method: 'POST', description: 'Search community UGC watermark by code' },
  { id: 17, service: 'TodayApiServiceKt', path: 'watermark/ugc/google/search', method: 'POST', description: 'Smart keyword search across UGC watermarks' },
  { id: 18, service: 'TodayApiServiceKt', path: 'watermark/ugc/public/info/save', method: 'POST', description: 'Save public UGC watermark metadata' },
  { id: 19, service: 'TodayApiServiceKt', path: 'watermark/ugc/recommend/template', method: 'POST', description: 'Recommend watermark templates by onboarding industry & role' },
  { id: 20, service: 'TodayApiServiceKt', path: 'watermark/ugc/share/Report', method: 'POST', description: 'Report download/usage count of shared UGC watermark' },
  { id: 21, service: 'TodayApiServiceKt', path: 'watermark/Skin/list', method: 'POST', description: 'List color skins and themes for a base watermark ID' },
  { id: 22, service: 'TodayApiServiceKt', path: 'logo/search', method: 'POST', description: 'Search brand and company logos online' },
  { id: 23, service: 'TodayApiServiceKt', path: 'logo/cutout', method: 'POST', description: 'Cloud AI automatic background removal for company logos' },
  { id: 24, service: 'TodayApiServiceKt', path: 'logo/recommend/Update', method: 'POST', description: 'Update recommended logo layout for watermark' },
  { id: 25, service: 'TodayApiServiceKt', path: 'logo/report', method: 'POST', description: 'Record logo usage telemetry' },
  { id: 26, service: 'TodayApiServiceKt', path: 'truthful/create/batch', method: 'POST', description: 'Batch register anti-fake photo hashes, GPS & timestamps' },
  { id: 27, service: 'TodayApiServiceKt', path: 'truthful/Query/batch', method: 'POST', description: 'Batch query anti-fake verification status' },
  { id: 28, service: 'TodayApiServiceKt', path: 'truthful/Query/batch/detail', method: 'POST', description: 'Deep AI VerifyAuth comparing OCR text & cryptographic metadata' },
  { id: 29, service: 'TodayApiServiceKt', path: 'photo/code/save', method: 'POST', description: 'Save PhotoCode metadata for fast lookup' },
  { id: 30, service: 'TodayApiServiceKt', path: 'photo/code/get', method: 'POST', description: 'Retrieve original capture metadata by photoCode' },
  { id: 31, service: 'TodayApiServiceKt', path: 'photo/datacenter/odd', method: 'POST', description: 'Report suspicious Mock GPS or system clock tampering flags' },
  { id: 32, service: 'TodayApiServiceKt', path: 'overseas/understand/photo', method: 'POST', description: 'Cloud Vision VLM scene understanding & watermark suggestion' },
  { id: 33, service: 'TodayApiServiceKt', path: 'ai/generatePrompt', method: 'POST', description: 'Cloud LLM automated field inspection note & report generator' },
  { id: 34, service: 'TodayApiServiceKt', path: 'workgroup/v3/upload/toCloud', method: 'POST', description: 'Confirm media file uploaded to S3/OSS cloud storage' },
  { id: 35, service: 'TodayApiServiceKt', path: 'app/shortLink/create', method: 'POST', description: 'Create short URL for photo or watermark sharing' },
  { id: 36, service: 'TodayApiServiceKt', path: 'app/plan/shortLink/create', method: 'POST', description: 'Create Stripe Web Checkout short link for Teamspace Business' },
  { id: 37, service: 'TodayApiServiceKt', path: 'center/app/login', method: 'POST', description: 'Multi-provider login (Google, Microsoft MSAL, Email OTP, Apple)' },
  { id: 38, service: 'TodayApiServiceKt', path: 'center/app/login/sendcode', method: 'POST', description: 'Send Email OTP verification code' },
  { id: 39, service: 'TodayApiServiceKt', path: 'center/app/logout', method: 'POST', description: 'Terminate active user session' },
  { id: 40, service: 'TodayApiServiceKt', path: 'center/app/cancelAccount', method: 'POST', description: 'GDPR/Google Play compliant permanent account deletion' },
  { id: 41, service: 'TodayApiServiceKt', path: 'center/app/token/refresh', method: 'POST', description: 'Refresh JWT access token' },
  { id: 42, service: 'TodayApiServiceKt', path: 'center/app/user', method: 'GET', description: 'Get current authenticated user profile' },
  { id: 43, service: 'TodayApiServiceKt', path: 'center/app/user/update', method: 'PUT', description: 'Update user profile nickname, avatar & industry' },
  { id: 44, service: 'TodayApiServiceKt', path: 'center/app/bind', method: 'POST', description: 'Bind external OAuth identity provider' },
  { id: 45, service: 'TodayApiServiceKt', path: 'center/app/unbind', method: 'POST', description: 'Unbind social identity provider' },
  { id: 46, service: 'TodayApiServiceKt', path: 'center/app/cluster/list', method: 'GET', description: 'List regional backend server clusters' },
  { id: 47, service: 'TodayApiServiceKt', path: 'center/app/group/list', method: 'GET', description: 'List user Teamspace groups from Account Center' },
  { id: 48, service: 'TodayApiServiceKt', path: 'center/app/group/list/silence', method: 'POST', description: 'Mute push notifications for selected groups' },
  { id: 49, service: 'TodayApiServiceKt', path: 'member/vip/info', method: 'POST', description: 'Query NextAI Plus VIP subscription status' },
  { id: 50, service: 'TodayApiServiceKt', path: 'member/vip/info/extra', method: 'POST', description: 'Detailed VIP entitlements, trial expiration & AdFreePass state' },
  { id: 51, service: 'TodayApiServiceKt', path: 'goods/query', method: 'POST', description: 'Query Paywall SKU catalog for Google Play Billing' },
  { id: 52, service: 'TodayApiServiceKt', path: 'order/create', method: 'POST', description: 'Create internal billing order prior to Play Billing purchase' },
  { id: 53, service: 'TodayApiServiceKt', path: 'order/google/receipts', method: 'POST', description: 'Verify Google Play purchaseToken receipt and activate VIP' },
  { id: 54, service: 'TodayApiServiceKt', path: 'order/bind/contract', method: 'POST', description: 'Bind auto-renewing subscription contract' },
  { id: 55, service: 'TodayApiServiceKt', path: 'app/adx/global_fetch_exposure_config', method: 'POST', description: 'Fetch AdX exposure rules and official Google AdMob test unit IDs' },
  { id: 56, service: 'TodayApiServiceKt', path: 'promote/operation/config/get', method: 'POST', description: 'Fetch in-app marketing banners & campaign rules' },
  { id: 57, service: 'TodayApiServiceKt', path: 'user/Retention/feekback', method: 'POST', description: 'Submit Paywall exit retention survey feedback' },
  { id: 58, service: 'TodayApiServiceKt', path: 'feedback/list', method: 'POST', description: 'List user technical support tickets' },
  { id: 59, service: 'TodayApiServiceKt', path: 'feedback/unread/num', method: 'POST', description: 'Get unread support reply count' },
  { id: 60, service: 'TodayApiServiceKt', path: 'user/questionnaire/submit', method: 'POST', description: 'Submit React Native questionnaire bundle answers' },
  { id: 61, service: 'TodayApiServiceKt', path: 'workreport/templates/classifier', method: 'POST', description: 'Classify & recommend PDF/Excel report layouts' },
  { id: 62, service: 'TodayApiServiceKt', path: 'attend/take/record', method: 'POST', description: 'Record personal attendance clock-in/clock-out photo' },
  { id: 63, service: 'TodayApiServiceKt', path: 'attend/feedback/create', method: 'POST', description: 'Add remark/appeal to personal attendance record' },
  { id: 64, service: 'TodayApiServiceKt', path: 'attend/Export', method: 'POST', description: 'Export monthly personal attendance sheet to Excel/PDF' },
  { id: 65, service: 'TodayApiServiceKt', path: 'upgrade/info', method: 'POST', description: 'Check app version update availability' },
  { id: 66, service: 'TodayApiServiceKt', path: 'Simple/upload/log', method: 'POST', description: 'Register diagnostic log archive uploaded to S3' },
  { id: 67, service: 'TodayApiServiceKt', path: 'ab/query', method: 'POST', description: 'Fetch A/B testing experiment bucket assignments' },
  { id: 68, service: 'TodayApiServiceKt', path: 'push/token/upload', method: 'POST', description: 'Register FCM push notification token' },

  // 2. ClusterApiService (91 Endpoints: #69 - #159)
  { id: 69, service: 'ClusterApiService', path: 'app/group/create', method: 'POST', description: 'Create new B2B Teamspace workspace' },
  { id: 70, service: 'ClusterApiService', path: 'app/group/delete', method: 'POST', description: 'Delete Teamspace workspace' },
  { id: 71, service: 'ClusterApiService', path: 'app/group/rename', method: 'POST', description: 'Rename Teamspace workspace' },
  { id: 72, service: 'ClusterApiService', path: 'app/group/query', method: 'POST', description: 'Query detailed Teamspace metadata' },
  { id: 73, service: 'ClusterApiService', path: 'app/group/setting', method: 'POST', description: 'Get Teamspace capture rules & anti-tamper locks' },
  { id: 74, service: 'ClusterApiService', path: 'app/group/setting/update', method: 'POST', description: 'Update Teamspace live-capture & clock-lock settings' },
  { id: 75, service: 'ClusterApiService', path: 'app/group/onboarding/Update', method: 'POST', description: 'Mark Teamspace onboarding wizard completed' },
  { id: 76, service: 'ClusterApiService', path: 'app/group/user/info', method: 'POST', description: 'Get current member role & profile in Teamspace' },
  { id: 77, service: 'ClusterApiService', path: 'app/group/user/update', method: 'POST', description: 'Update member display name & title in Teamspace' },
  { id: 78, service: 'ClusterApiService', path: 'app/group/user/delete', method: 'POST', description: 'Remove member or leave Teamspace' },
  { id: 79, service: 'ClusterApiService', path: 'app/group/member/list', method: 'POST', description: 'List all members in Teamspace' },
  { id: 80, service: 'ClusterApiService', path: 'app/group/member/info', method: 'POST', description: 'Get member contribution statistics' },
  { id: 81, service: 'ClusterApiService', path: 'app/group/member/query', method: 'POST', description: 'Search members by name or email' },
  { id: 82, service: 'ClusterApiService', path: 'app/group/role/list', method: 'POST', description: 'List RBAC roles (Owner, Admin, Member, Guest)' },
  { id: 83, service: 'ClusterApiService', path: 'app/group/role/member/add', method: 'POST', description: 'Assign Admin/Manager role to member' },
  { id: 84, service: 'ClusterApiService', path: 'app/group/role/member/delete', method: 'POST', description: 'Revoke Admin/Manager role from member' },
  { id: 85, service: 'ClusterApiService', path: 'app/group/role/project/edit', method: 'POST', description: 'Configure project-level ACL permissions' },
  { id: 86, service: 'ClusterApiService', path: 'app/group/invite/email/send', method: 'POST', description: 'Send Teamspace email invitations' },
  { id: 87, service: 'ClusterApiService', path: 'app/group/invite/link/get', method: 'POST', description: 'Get Teamspace invite URL and QR code' },
  { id: 88, service: 'ClusterApiService', path: 'app/group/invite/link/join', method: 'POST', description: 'Join Teamspace via invite URL' },
  { id: 89, service: 'ClusterApiService', path: 'app/group/invite/user/status', method: 'POST', description: 'Check invitation acceptance status' },
  { id: 90, service: 'ClusterApiService', path: 'app/group/join', method: 'POST', description: 'Join Teamspace by 6-digit Team Code' },
  { id: 91, service: 'ClusterApiService', path: 'app/group/project/create', method: 'POST', description: 'Create construction/field project with GPS radius' },
  { id: 92, service: 'ClusterApiService', path: 'app/group/project/update', method: 'POST', description: 'Update project details and geofence radius' },
  { id: 93, service: 'ClusterApiService', path: 'app/group/project/delete', method: 'POST', description: 'Delete project from Teamspace' },
  { id: 94, service: 'ClusterApiService', path: 'app/group/project/list', method: 'POST', description: 'List projects in Teamspace' },
  { id: 95, service: 'ClusterApiService', path: 'app/group/project/search', method: 'POST', description: 'Search projects by name or nearest GPS' },
  { id: 96, service: 'ClusterApiService', path: 'app/group/project/detail/stats', method: 'POST', description: 'Get project photo, video & member metrics' },
  { id: 97, service: 'ClusterApiService', path: 'app/group/project/Settings', method: 'POST', description: 'Configure mandatory watermark rules for project' },
  { id: 98, service: 'ClusterApiService', path: 'app/group/watermark/add', method: 'POST', description: 'Add corporate watermark template to Teamspace' },
  { id: 99, service: 'ClusterApiService', path: 'app/group/watermark/update', method: 'POST', description: 'Update corporate watermark template' },
  { id: 100, service: 'ClusterApiService', path: 'app/group/watermark/delete', method: 'POST', description: 'Remove watermark template from Teamspace' },
  { id: 101, service: 'ClusterApiService', path: 'app/group/watermark/list', method: 'POST', description: 'List enforced watermarks in Teamspace' },
  { id: 102, service: 'ClusterApiService', path: 'app/group/watermark/move', method: 'POST', description: 'Reorder Teamspace watermark priority' },
  { id: 103, service: 'ClusterApiService', path: 'app/group/watermark/project/Update', method: 'POST', description: 'Bind default watermark to specific project' },
  { id: 104, service: 'ClusterApiService', path: 'app/photo/upload', method: 'POST', description: 'Upload field photo metadata to Teamspace cloud' },
  { id: 105, service: 'ClusterApiService', path: 'app/photo/delete', method: 'POST', description: 'Delete photo from Teamspace cloud' },
  { id: 106, service: 'ClusterApiService', path: 'app/photo/move', method: 'POST', description: 'Move photo between projects' },
  { id: 107, service: 'ClusterApiService', path: 'app/photo/list', method: 'POST', description: 'Paginated photo feed for Teamspace' },
  { id: 108, service: 'ClusterApiService', path: 'app/photo/search', method: 'POST', description: 'Multi-filter search across Teamspace photos (OCR, member, date)' },
  { id: 109, service: 'ClusterApiService', path: 'app/photo/search/options', method: 'POST', description: 'Get available filter facets for Teamspace photo search' },
  { id: 110, service: 'ClusterApiService', path: 'app/photo/detail/get', method: 'POST', description: 'Get full photo EXIF, GPS map & Anti-Fake audit detail' },
  { id: 111, service: 'ClusterApiService', path: 'app/photo/detail/update', method: 'POST', description: 'Update photo notes and tags' },
  { id: 112, service: 'ClusterApiService', path: 'app/photo/batch/share', method: 'POST', description: 'Create client share bundle for multiple photos' },
  { id: 113, service: 'ClusterApiService', path: 'app/photo/batch/share/detail', method: 'POST', description: 'View shared photo bundle metadata' },
  { id: 114, service: 'ClusterApiService', path: 'app/photo/export/task/create', method: 'POST', description: 'Create async cloud report export job (PDF, Excel POI, KMZ, ZIP)' },
  { id: 115, service: 'ClusterApiService', path: 'app/photo/export/task/status', method: 'POST', description: 'Poll cloud report export task status & download URL' },
  { id: 116, service: 'ClusterApiService', path: 'app/photo/export/template/list', method: 'POST', description: 'List PDF/Excel/KMZ report layout templates' },
  { id: 117, service: 'ClusterApiService', path: 'app/photo/map/markers', method: 'POST', description: 'Get photo GPS map markers within bounding box' },
  { id: 118, service: 'ClusterApiService', path: 'app/photo/map/cluster/photos', method: 'POST', description: 'List photos inside a map marker cluster' },
  { id: 119, service: 'ClusterApiService', path: 'app/checklist/create', method: 'POST', description: 'Create field inspection checklist template' },
  { id: 120, service: 'ClusterApiService', path: 'app/checklist/update', method: 'POST', description: 'Update inspection checklist template' },
  { id: 121, service: 'ClusterApiService', path: 'app/checklist/delete', method: 'POST', description: 'Delete inspection checklist template' },
  { id: 122, service: 'ClusterApiService', path: 'app/checklist/list', method: 'POST', description: 'List checklists in project/Teamspace' },
  { id: 123, service: 'ClusterApiService', path: 'app/checklist/detail', method: 'POST', description: 'Get checklist inspection items & requirements' },
  { id: 124, service: 'ClusterApiService', path: 'app/checklist/item/submit', method: 'POST', description: 'Submit photo evidence for a checklist item' },
  { id: 125, service: 'ClusterApiService', path: 'app/checklist/submission/list', method: 'POST', description: 'List completed checklist submissions' },
  { id: 126, service: 'ClusterApiService', path: 'app/checklist/export', method: 'POST', description: 'Export checklist inspection report to PDF' },
  { id: 127, service: 'ClusterApiService', path: 'app/project/file/create', method: 'POST', description: 'Upload CAD/PDF/Word engineering document to project' },
  { id: 128, service: 'ClusterApiService', path: 'app/project/file/delete', method: 'POST', description: 'Delete document from project' },
  { id: 129, service: 'ClusterApiService', path: 'app/project/file/list', method: 'POST', description: 'List engineering documents in project' },
  { id: 130, service: 'ClusterApiService', path: 'app/project/file/rename', method: 'POST', description: 'Rename project document' },
  { id: 131, service: 'ClusterApiService', path: 'app/attendance/rule/get', method: 'POST', description: 'Get Teamspace shift schedule & attendance rules' },
  { id: 132, service: 'ClusterApiService', path: 'app/attendance/rule/update', method: 'POST', description: 'Update Teamspace shift schedule & attendance rules' },
  { id: 133, service: 'ClusterApiService', path: 'app/attendance/punch', method: 'POST', description: 'Execute verified attendance clock-in/out punch' },
  { id: 134, service: 'ClusterApiService', path: 'app/attendance/status', method: 'POST', description: 'Check employee current shift punch status' },
  { id: 135, service: 'ClusterApiService', path: 'app/attendance/records/daily', method: 'POST', description: 'Query daily team attendance records' },
  { id: 136, service: 'ClusterApiService', path: 'app/attendance/records/monthly', method: 'POST', description: 'Query monthly attendance & overtime summary' },
  { id: 137, service: 'ClusterApiService', path: 'app/attendance/export', method: 'POST', description: 'Export team payroll & attendance Excel sheet' },
  { id: 138, service: 'ClusterApiService', path: 'app/group/timeclock/geofence/create', method: 'POST', description: 'Create GPS attendance geofence (center lat/lng + radius meters)' },
  { id: 139, service: 'ClusterApiService', path: 'app/group/timeclock/geofence/update', method: 'POST', description: 'Update GPS attendance geofence coordinates or radius' },
  { id: 140, service: 'ClusterApiService', path: 'app/group/timeclock/geofence/delete', method: 'POST', description: 'Delete GPS attendance geofence' },
  { id: 141, service: 'ClusterApiService', path: 'app/group/timeclock/geofence/list', method: 'POST', description: 'List active GPS attendance geofences for Teamspace' },
  { id: 142, service: 'ClusterApiService', path: 'app/group/timeclock/geofence/check', method: 'POST', description: 'Verify employee GPS against geofence radius using Haversine formula' },
  { id: 143, service: 'ClusterApiService', path: 'business/subscription/info', method: 'POST', description: 'Get Teamspace Business Plan subscription & storage quota info' },
  { id: 144, service: 'ClusterApiService', path: 'business/subscription/gp/goods/list', method: 'POST', description: 'List Teamspace Business SKUs for Google Play' },
  { id: 145, service: 'ClusterApiService', path: 'business/subscription/gp/order/create', method: 'POST', description: 'Create Teamspace Business upgrade order' },
  { id: 146, service: 'ClusterApiService', path: 'business/subscription/gp/receipt/verify', method: 'POST', description: 'Verify Google Play receipt for Teamspace Business Plan' },
  { id: 147, service: 'ClusterApiService', path: 'business/subscription/member/quota', method: 'POST', description: 'Check remaining member seat & S3 storage quota' },
  { id: 148, service: 'ClusterApiService', path: 'business/subscription/trial/apply', method: 'POST', description: 'Activate 14-day free trial for Teamspace Business' },
  { id: 149, service: 'ClusterApiService', path: 'web/teamspace/app/integration/list', method: 'POST', description: 'List 3rd-party integrations (OneDrive, Google Drive, Dropbox, Zapier)' },
  { id: 150, service: 'ClusterApiService', path: 'web/teamspace/app/integration/bind', method: 'POST', description: 'Bind OAuth2 3rd-party cloud drive auto-sync' },
  { id: 151, service: 'ClusterApiService', path: 'web/teamspace/app/integration/unbind', method: 'POST', description: 'Disconnect 3rd-party cloud integration' },
  { id: 152, service: 'ClusterApiService', path: 'web/teamspace/app/integration/status', method: 'POST', description: 'Check auto-sync status to external cloud storage' },
  { id: 153, service: 'ClusterApiService', path: 'app/share/link/info', method: 'POST', description: 'Get public share link landing page info' },
  { id: 154, service: 'ClusterApiService', path: 'app/share/link/photos', method: 'POST', description: 'List photos inside a public share link' },
  { id: 155, service: 'ClusterApiService', path: 'app/workgroup/home', method: 'POST', description: 'Fetch Teamspace home dashboard overview' },
  { id: 156, service: 'ClusterApiService', path: 'app/workgroup/summary/read', method: 'POST', description: 'Mark Teamspace activity summary as read' },
  { id: 157, service: 'ClusterApiService', path: 'app/workgroup/notification/list', method: 'POST', description: 'List Teamspace activity notifications' },
  { id: 158, service: 'ClusterApiService', path: 'app/workgroup/notification/read', method: 'POST', description: 'Mark Teamspace notifications as read' },
  { id: 159, service: 'ClusterApiService', path: 'app/workgroup/comment/add', method: 'POST', description: 'Add supervisor comment on field photo' },

  // 3. EarnApiService (9 Endpoints: #160 - #168)
  { id: 160, service: 'EarnApiService', path: 'app/reward/video/balance', method: 'POST', description: 'Get Embodied AI contributor wallet balance (BRL/MXN/USD)' },
  { id: 161, service: 'EarnApiService', path: 'app/reward/video/task/list', method: 'POST', description: 'List egocentric robotics video collection tasks' },
  { id: 162, service: 'EarnApiService', path: 'app/reward/video/apply/list', method: 'POST', description: 'List user applied robotics tasks & submission statuses' },
  { id: 163, service: 'EarnApiService', path: 'app/reward/video/details', method: 'POST', description: 'Get robotics task specification (21-hand landmarks + 4 IMU streams)' },
  { id: 164, service: 'EarnApiService', path: 'app/reward/video/recent/approved/list', method: 'POST', description: 'Social proof ticker of recent approved contributor payouts' },
  { id: 165, service: 'EarnApiService', path: 'app/reward/video/submit', method: 'POST', description: 'Submit .mcap sensor recording with handRatio & IMU telemetry' },
  { id: 166, service: 'EarnApiService', path: 'app/reward/video/withdrawal/apply', method: 'POST', description: 'Request payout via Brazil Pix (BRL), Mexico SPEI (MXN) or VIP days' },
  { id: 167, service: 'EarnApiService', path: 'app/reward/video/withdrawal/banks', method: 'POST', description: 'List supported withdrawal banks by country' },
  { id: 168, service: 'EarnApiService', path: 'app/reward/video/withdrawal/status', method: 'POST', description: 'Check bank payout transfer status' },

  // 4. DualModeApiService (7 Endpoints: #169 - #175)
  { id: 169, service: 'DualModeApiService', path: 'antiFakeCode/generate', method: 'POST', description: 'Pre-generate deterministic HMAC-SHA256 antiFakeCode batch for offline capture' },
  { id: 170, service: 'DualModeApiService', path: 'workgroup/photo/watermark/remove/check', method: 'POST', description: 'Check JpegPacker reversible watermark removal permission' },
  { id: 171, service: 'DualModeApiService', path: 'favorite/address/add', method: 'POST', description: 'Save GPS POI to user Favorite Addresses' },
  { id: 172, service: 'DualModeApiService', path: 'favorite/address/cancel', method: 'POST', description: 'Remove address from Favorites' },
  { id: 173, service: 'DualModeApiService', path: 'favorite/address/list', method: 'POST', description: 'List saved Favorite Addresses' },
  { id: 174, service: 'DualModeApiService', path: 'app/user/event/add', method: 'POST', description: 'Record user feature usage telemetry event' },
  { id: 175, service: 'DualModeApiService', path: 'app/user/event/count', method: 'POST', description: 'Query feature usage count for dynamic Paywall gating' },

  // 5. OSSApi & S3TokenApi (2 Endpoints: #176 - #177)
  { id: 176, service: 'OSSApi', path: 'workgroup/v4/oss/token', method: 'POST', description: 'Issue temporary Alibaba Cloud OSS STS credentials' },
  { id: 177, service: 'S3TokenApi', path: 'workgroup/v4/s3/token', method: 'POST', description: 'Issue temporary AWS S3 STS credentials for photos, reports & .mcap uploads' }
];

// Haversine formula in meters
export function calculateHaversineMeters(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371000; // Earth radius in meters
  const toRad = (deg: number) => (deg * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c * 100) / 100;
}

// Deterministic HMAC-SHA256 Anti-Fake Photo Code Generator
const HMAC_SECRET = 'nextai-enterprise-aegis-hmac-secret-2026';
export function generateDeterministicAntiFakeCode(seedInput: string): { code: string; signature: string } {
  const hmac = crypto.createHmac('sha256', HMAC_SECRET).update(seedInput).digest('hex').toUpperCase();
  const part1 = hmac.slice(0, 4);
  const part2 = hmac.slice(4, 8);
  return {
    code: `NX9X-${part1}-${part2}`,
    signature: hmac
  };
}

// In-Memory Relational + OLAP State initialized from sanitized seed data
export const dbState = {
  watermarks: [
    {
      id: 'wm_10011',
      name: 'Real-Time Official Anti-Tamper',
      base_id: '10011',
      category_id: 'cat_time_gps',
      type: 1,
      update_time: '1761108000000',
      version: '3.2',
      water_mark_content: '{"base_id":"10011","theme":"#2563EB","antiFake":true,"items":["time","date","latlng","address","weather","compass","antiCode"]}',
      is_pgc: 1,
      pgc_Order: 1
    },
    {
      id: 'wm_20',
      name: 'Engineering Construction Log',
      base_id: '20',
      category_id: 'cat_construction',
      type: 1,
      update_time: '1761108000000',
      version: '2.8',
      water_mark_content: '{"base_id":"20","theme":"#F59E0B","antiFake":true,"items":["project_name","construction_area","content","inspector","time","address"]}',
      is_pgc: 1,
      pgc_Order: 2
    },
    {
      id: 'wm_57',
      name: 'AI Steel Rebar Counter (CVModel=4)',
      base_id: '57',
      category_id: 'cat_construction',
      type: 2,
      update_time: '1761108000000',
      version: '4.0',
      water_mark_content: '{"base_id":"57","cvModelId":4,"theme":"#EF4444","items":["steel_count","batch_no","operator","time","latlng"]}',
      is_pgc: 1,
      pgc_Order: 3
    },
    {
      id: 'wm_34',
      name: 'Geofence Shift Attendance',
      base_id: '34',
      category_id: 'cat_attendance',
      type: 1,
      update_time: '1761108000000',
      version: '3.0',
      water_mark_content: '{"base_id":"34","theme":"#10B981","lockTime":true,"items":["shift_time","employee_name","geofence_name","company_logo","antiCode"]}',
      is_pgc: 1,
      pgc_Order: 4
    },
    {
      id: 'wm_52',
      name: 'AI License Plate Recognition (CVModel=1)',
      base_id: '52',
      category_id: 'cat_patrol_security',
      type: 2,
      update_time: '1761108000000',
      version: '2.1',
      water_mark_content: '{"base_id":"52","cvModelId":1,"theme":"#3B82F6","items":["plate_number","vehicle_type","gate_name","time"]}',
      is_pgc: 1,
      pgc_Order: 5
    }
  ],
  teams: [
    {
      team_id: 'team_vn_001',
      team_name: 'Coteccons Metro Line 1 Engineering',
      role: 1,
      member_count: 42,
      photo_count: 18450,
      plan_type: 2,
      storage_used_bytes: 68934205440,
      storage_limit_bytes: 107374182400,
      avatar_url: 'https://static.nextai.global/teams/metro1.png',
      settings_json: '{"forceLiveCapture":true,"blockMockGps":true,"onedriveSync":true}',
      updated_at: 1761109000000
    },
    {
      team_id: 'team_vn_002',
      team_name: 'Vinhomes Grand Park Facility Patrol',
      role: 2,
      member_count: 28,
      photo_count: 9320,
      plan_type: 2,
      storage_used_bytes: 31138512896,
      storage_limit_bytes: 107374182400,
      avatar_url: 'https://static.nextai.global/teams/vhgp.png',
      settings_json: '{"forceLiveCapture":true,"requireQrPatrol":true}',
      updated_at: 1761109200000
    },
    {
      team_id: 'team_br_003',
      team_name: 'Sao Paulo Solar Grid Installation',
      role: 1,
      member_count: 15,
      photo_count: 4120,
      plan_type: 1,
      storage_used_bytes: 8589934592,
      storage_limit_bytes: 53687091200,
      avatar_url: 'https://static.nextai.global/teams/solar_sp.png',
      settings_json: '{"forceLiveCapture":true,"autoExportPdf":true}',
      updated_at: 1761109500000
    }
  ],
  projects: [
    {
      project_id: 'proj_01',
      team_id: 'team_vn_001',
      project_name: 'Ga Ngầm Ba Son - Tầng B2',
      address: '2 Tôn Đức Thắng, Bến Nghé, Quận 1, TP.HCM',
      lat: 10.7815,
      lng: 106.7072,
      radius: 250,
      photo_count: 6420,
      status: 1,
      updated_at: 1761109100000
    },
    {
      project_id: 'proj_02',
      team_id: 'team_vn_001',
      project_name: 'Depot Long Bình - Khu Bảo Dưỡng',
      address: 'Long Bình, TP. Thủ Đức, TP.HCM',
      lat: 10.8754,
      lng: 106.8291,
      radius: 500,
      photo_count: 5180,
      status: 1,
      updated_at: 1761109150000
    },
    {
      project_id: 'proj_03',
      team_id: 'team_vn_002',
      project_name: 'Phân Khu The Origami - Tuần Tra Đêm',
      address: 'Nguyễn Xiển, Long Thạnh Mỹ, TP. Thủ Đức',
      lat: 10.8421,
      lng: 106.8365,
      radius: 300,
      photo_count: 4890,
      status: 1,
      updated_at: 1761109300000
    }
  ],
  geofences: [
    {
      geofenceId: 'geo_bason_01',
      teamId: 'team_vn_001',
      projectId: 'proj_01',
      name: 'Ga Ngầm Ba Son - Cổng Số 2',
      centerLat: 10.78152,
      centerLng: 106.70724,
      radiusMeters: 150,
      requireLiveCameraCapture: true
    },
    {
      geofenceId: 'geo_longbinh_02',
      teamId: 'team_vn_001',
      projectId: 'proj_02',
      name: 'Depot Long Bình - Trạm Kiểm Soát',
      centerLat: 10.87541,
      centerLng: 106.82915,
      radiusMeters: 300,
      requireLiveCameraCapture: true
    }
  ],
  photoCodes: new Map<string, any>([
    [
      'NX9X-84K2-V71Q',
      {
        photo_code: 'NX9X-84K2-V71Q',
        local_path: '/storage/emulated/0/DCIM/NextAI/NX_20261022_081512.jpg',
        oss_url: 'https://nextai-oversea.s3.us-west-1.amazonaws.com/photos/NX9X-84K2-V71Q.jpg',
        capture_time: 1761105312000,
        lat: 10.78152,
        lng: 106.70724,
        address: '2 Tôn Đức Thắng, Quận 1, TP.HCM',
        watermark_json: '{"ntpDeltaMs":42,"isMockGps":false,"aegisNonce":"9f8e7d6c5b4a","jpegPacked":true}',
        verify_status: 1,
        verdict: 'AUTHENTIC',
        created_at: 1761105313000
      }
    ],
    [
      'NX9X-92M4-P18Z',
      {
        photo_code: 'NX9X-92M4-P18Z',
        local_path: '/storage/emulated/0/DCIM/NextAI/NX_20261022_084230.jpg',
        oss_url: 'https://nextai-oversea.s3.us-west-1.amazonaws.com/photos/NX9X-92M4-P18Z.jpg',
        capture_time: 1761106950000,
        lat: 10.78158,
        lng: 106.70731,
        address: '2 Tôn Đức Thắng, Quận 1, TP.HCM',
        watermark_json: '{"ntpDeltaMs":35,"isMockGps":false,"steelCount":128,"jpegPacked":true}',
        verify_status: 1,
        verdict: 'AUTHENTIC',
        created_at: 1761106951000
      }
    ],
    [
      'NX9X-31L9-K55B',
      {
        photo_code: 'NX9X-31L9-K55B',
        local_path: '/storage/emulated/0/DCIM/NextAI/NX_20261022_091005.jpg',
        oss_url: 'https://nextai-oversea.s3.us-west-1.amazonaws.com/photos/NX9X-31L9-K55B.jpg',
        capture_time: 1761108605000,
        lat: 10.87541,
        lng: 106.82915,
        address: 'Depot Long Bình, TP. Thủ Đức',
        watermark_json: '{"ntpDeltaMs":51,"isMockGps":false,"inspector":"KS. Lê Hoàng Nam","jpegPacked":true}',
        verify_status: 1,
        verdict: 'AUTHENTIC',
        created_at: 1761108606000
      }
    ]
  ]),
  earnWallet: {
    walletBalanceFen: 18500,
    totalEarnedFen: 64500,
    currency: 'BRL',
    submissions: [] as any[],
    withdrawals: [] as any[]
  },
  favoriteAddresses: [
    {
      place_id: 'poi_bason_01',
      name: 'Ga Ngầm Ba Son - Cổng 2',
      address: '2 Tôn Đức Thắng, Quận 1, TP.HCM',
      lat: 10.78152,
      lng: 106.70724,
      use_count: 19,
      custom_alias: 'Công trường Ba Son'
    }
  ],
  userEvents: new Map<string, number>([
    ['remove_watermark_trial', 1],
    ['ai_logo_cutout_trial', 2]
  ])
};

function ok<T>(data: T): BaseResponse<T> {
  return {
    code: 200,
    msg: 'success',
    status: 0,
    data
  };
}

function normalizePhotoCodeInput(code: string): string {
  if (!code) return 'NX9X-84K2-V71Q';
  // Normalize legacy prefix if passed by old client test
  return code.replace(/^TM9X-/i, 'NX9X-');
}

function readPublicHtml(fileName: string, fallbackTitle: string): string {
  const candidates = [
    path.join(__dirname, '..', 'public', fileName),
    path.join(process.cwd(), 'public', fileName),
    path.join(process.cwd(), 'Backend', 'public', fileName)
  ];
  for (const p of candidates) {
    if (fs.existsSync(p)) {
      return fs.readFileSync(p, 'utf8');
    }
  }
  return `<!DOCTYPE html><html><head><title>${fallbackTitle}</title></head><body><h1>${fallbackTitle}</h1></body></html>`;
}

export function handleApiRoute(rawPath: string, method: string, body: any, activePort: number): BaseResponse<any> {
  // Strip /nextai/v1/ or /next/ or leading/trailing slashes
  const cleanPath = rawPath
    .replace(/^\/+/, '')
    .replace(/^nextai\/v1\//i, '')
    .replace(/^next\//i, '')
    .replace(/\s+/g, '')
    .replace(/\/+$/, '');

  const lower = cleanPath.toLowerCase();
  const now = Date.now();

  // 1. Core Config & Atomic Realtime
  if (lower === 'android/config') {
    return ok({
      brand: 'NextAI',
      packageName: 'com.nextai.camera',
      minVersionCode: 100028000,
      latestVersionCode: 100028100,
      forceUpdate: false,
      ntpSyncEnabled: true,
      aegisHardwareAttestation: true,
      jpegPackerSteganography: true,
      embodiedAiEarnEnabled: true,
      adMobTestMode: true,
      gatewayDomains: {
        coreApi: `http://127.0.0.1:${activePort}/nextai/v1/`,
        adxApi: `http://127.0.0.1:${activePort}/nextai/v1/`,
        teamspaceWeb: `http://127.0.0.1:${activePort}/teamspace`,
        truthH5: `http://127.0.0.1:${activePort}/truth`,
        cloudCdn: 'https://static.nextai.global/'
      },
      edgeAiModels: [
        { cvModelId: 1, name: 'VLPR License Plate Recognition', engine: 'NCNN', ready: true },
        { cvModelId: 2, name: 'FaceDetect Group Attendance Counter', engine: 'NCNN', ready: true },
        { cvModelId: 4, name: 'SteelDetect Rebar Counter', engine: 'NCNN', ready: true },
        { cvModelId: 9, name: 'MediaPipe 21 Hand Landmarks', engine: 'MediaPipe+NCNN', ready: true }
      ]
    });
  }

  if (lower === 'realtime') {
    const clientTime = Number(body?.clientTime || now);
    const deltaMs = now - clientTime;
    return ok({
      serverRealtimeMs: now,
      serverTimeUtc: new Date(now).toISOString(),
      clientTimeMs: clientTime,
      ntpDeltaMs: deltaMs,
      ntpVerified: Math.abs(deltaMs) < 300000,
      atomicStratum: 1,
      authority: 'ntp.nextai.global'
    });
  }

  if (lower === 'network/ip' || lower === 'network/benchmark') {
    return ok({
      ip: '203.113.148.22',
      countryCode: body?.country || 'VN',
      region: 'Ho Chi Minh City',
      isp: 'NextAI Global Edge Network',
      rttMs: 8,
      acceleratedEdge: true
    });
  }

  if (lower === 'weather/current/detail') {
    return ok({
      lat: body?.lat ?? 10.78152,
      lng: body?.lng ?? 106.70724,
      temperatureC: 29.5,
      temperatureF: 85.1,
      humidityPercent: 72,
      windSpeedKmh: 14.2,
      windDirection: 'SE',
      condition: 'Partly Cloudy',
      iconUrl: 'https://static.nextai.global/weather/partly_cloudy.png',
      aqi: 42,
      aqiLevel: 'Good',
      updatedAtMs: now
    });
  }

  if (lower === 'v2/place/list/group' || lower === 'v2/place/search/group' || lower === 'place/nominatim/reverse') {
    const lat = Number(body?.lat ?? 10.78152);
    const lng = Number(body?.lng ?? 106.70724);
    return ok({
      lat,
      lng,
      formattedAddress: '2 Tôn Đức Thắng, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh',
      city: 'TP. Hồ Chí Minh',
      district: 'Quận 1',
      country: 'Vietnam',
      poiList: [
        { placeId: 'poi_bason_01', name: 'Ga Ngầm Ba Son - Metro Line 1', address: '2 Tôn Đức Thắng, Quận 1', lat, lng, distanceMeters: 12 },
        { placeId: 'poi_bason_02', name: 'Tòa Nhà Saigon Marina IFC', address: '2 Tôn Đức Thắng, Quận 1', lat: lat + 0.0003, lng: lng + 0.0002, distanceMeters: 45 },
        { placeId: 'poi_vhgp_03', name: 'Vinhomes Grand Park - The Origami', address: 'Nguyễn Xiển, TP. Thủ Đức', lat: 10.8421, lng: 106.8365, distanceMeters: 14200 }
      ]
    });
  }

  // 2. Watermark Store & Templates
  if (
    lower === 'config/watermarks' ||
    lower === 'watermark/ours/query' ||
    lower === 'watermark/pgc/query' ||
    lower === 'watermark/ugc/recommend/template' ||
    lower === 'watermark/ugc/code/search' ||
    lower === 'watermark/ugc/google/search' ||
    lower === 'watermark/share/private/query'
  ) {
    return ok({
      total: dbState.watermarks.length,
      categories: [
        { id: 'cat_time_gps', name: 'Time & GPS Official', iconUrl: 'https://static.nextai.global/icons/cat_gps.png' },
        { id: 'cat_construction', name: 'Construction & Engineering', iconUrl: 'https://static.nextai.global/icons/cat_build.png' },
        { id: 'cat_attendance', name: 'Attendance & Shift Clock', iconUrl: 'https://static.nextai.global/icons/cat_clock.png' },
        { id: 'cat_patrol_security', name: 'Security Patrol & Property', iconUrl: 'https://static.nextai.global/icons/cat_patrol.png' }
      ],
      watermarks: dbState.watermarks
    });
  }

  if (lower === 'watermark/skin/list') {
    return ok({
      baseId: body?.baseId || '10011',
      skins: [
        { skinId: 'skin_blue', themeColor: '#2563EB', textColor: '#FFFFFF', alpha: 0.9 },
        { skinId: 'skin_amber', themeColor: '#F59E0B', textColor: '#000000', alpha: 0.95 },
        { skinId: 'skin_emerald', themeColor: '#10B981', textColor: '#FFFFFF', alpha: 0.9 }
      ]
    });
  }

  // 3. Cloud AI Logo Cutout, Vision & LLM
  if (lower === 'logo/search') {
    return ok({
      keyword: body?.keyword || 'NextAI',
      logos: [
        { logoId: 'lg_01', name: 'NextAI Global Official Logo', url: 'https://static.nextai.global/logos/nextai_official.png' },
        { logoId: 'lg_02', name: 'NextAI Construction Division', url: 'https://static.nextai.global/logos/nextai_build.png' }
      ]
    });
  }

  if (lower === 'logo/cutout') {
    return ok({
      originalUrl: body?.imageUrl || 'https://static.nextai.global/logos/input.jpg',
      cutoutPngUrl: 'https://static.nextai.global/logos/cutout_transparent_rgba.png',
      modelEngine: 'NextAI-RMBG-1.4-FP16',
      confidenceScore: 0.994,
      processingMs: 118
    });
  }

  if (lower === 'overseas/understand/photo') {
    return ok({
      sceneCategory: 'CONSTRUCTION_ENGINEERING',
      detectedObjects: ['steel_rebar', 'safety_helmet', 'concrete_column'],
      recommendedWatermarkBaseId: '57',
      suggestedTitle: 'Nghiệm Thu Cốt Thép Sàn B2',
      confidence: 0.96
    });
  }

  if (lower === 'ai/generateprompt') {
    const industry = body?.industry || 'Construction & Field Engineering';
    const context = body?.context || 'Daily site safety & progress inspection';
    return ok({
      industry,
      context,
      generatedTitle: `[NextAI Verified] ${industry} — Field Inspection Log`,
      generatedChecklistNotes: [
        '1. Verified GPS coordinates within active project geofence radius.',
        '2. Confirmed structural steel rebar count and PPE safety compliance.',
        '3. Cryptographic Aegis PhotoCode & NTP timestamp locked.'
      ],
      llmModel: 'nextai-enterprise-instruct-v4'
    });
  }

  // 4. Anti-Fake Cryptographic Engine & Truth Center
  if (lower === 'antifakecode/generate') {
    const count = Math.min(Math.max(Number(body?.count || 5), 1), 50);
    const deviceId = String(body?.deviceId || 'NX-DEVICE-001');
    const codes: Array<{ code: string; signature: string; expiresAtMs: number }> = [];

    for (let i = 0; i < count; i++) {
      const seed = `${deviceId}:${now}:${i}:${body?.seed || 'nextai'}`;
      const generated = generateDeterministicAntiFakeCode(seed);
      codes.push({
        code: generated.code,
        signature: generated.signature,
        expiresAtMs: now + 7 * 86400 * 1000
      });
      dbState.photoCodes.set(generated.code, {
        photo_code: generated.code,
        local_path: `/storage/emulated/0/DCIM/NextAI/${generated.code}.jpg`,
        oss_url: `https://nextai-oversea.s3.us-west-1.amazonaws.com/photos/${generated.code}.jpg`,
        capture_time: now,
        lat: Number(body?.lat ?? 10.78152),
        lng: Number(body?.lng ?? 106.70724),
        address: body?.address || '2 Tôn Đức Thắng, Quận 1, TP.HCM',
        watermark_json: JSON.stringify({
          ntpDeltaMs: 18,
          isMockGps: false,
          aegisNonce: generated.signature.slice(0, 16).toLowerCase(),
          hmacSignature: generated.signature,
          jpegPacked: true
        }),
        verify_status: 1,
        verdict: 'AUTHENTIC',
        created_at: now
      });
    }

    return ok({
      deviceId,
      algorithm: 'HMAC-SHA256',
      generatedCount: codes.length,
      antiFakeCodes: codes.map(c => c.code),
      details: codes
    });
  }

  if (lower === 'truthful/create/batch' || lower === 'photo/code/save') {
    const rawCode = normalizePhotoCodeInput(body?.photoCode || body?.photos?.[0]?.photoCode || `NX9X-${now.toString(36).slice(-4).toUpperCase()}-V99A`);
    const sig = generateDeterministicAntiFakeCode(rawCode).signature;
    const record = {
      photo_code: rawCode,
      local_path: body?.localPath || `/storage/emulated/0/DCIM/NextAI/${rawCode}.jpg`,
      oss_url: `https://nextai-oversea.s3.us-west-1.amazonaws.com/photos/${rawCode}.jpg`,
      capture_time: Number(body?.captureTime || now),
      lat: Number(body?.lat ?? 10.78152),
      lng: Number(body?.lng ?? 106.70724),
      address: body?.address || '2 Tôn Đức Thắng, Quận 1, TP.HCM',
      watermark_json: JSON.stringify({
        ntpDeltaMs: 24,
        isMockGps: false,
        aegisNonce: sig.slice(0, 16).toLowerCase(),
        hmacSignature: sig,
        jpegPacked: true
      }),
      verify_status: 1,
      verdict: 'AUTHENTIC',
      created_at: now
    };
    dbState.photoCodes.set(rawCode, record);
    return ok({ saved: true, photoCode: rawCode, record });
  }

  if (lower === 'photo/code/get' || lower === 'truthful/query/batch' || lower === 'truthful/query/batch/detail') {
    const requestedCodes: string[] = Array.isArray(body?.codes)
      ? body.codes.map(normalizePhotoCodeInput)
      : [normalizePhotoCodeInput(body?.photoCode || 'NX9X-84K2-V71Q')];

    const verifiedList = requestedCodes.map(c => {
      const existing = dbState.photoCodes.get(c);
      if (existing) return existing;
      const sig = generateDeterministicAntiFakeCode(c).signature;
      return {
        photo_code: c,
        local_path: `/storage/emulated/0/DCIM/NextAI/${c}.jpg`,
        oss_url: `https://nextai-oversea.s3.us-west-1.amazonaws.com/photos/${c}.jpg`,
        capture_time: now - 60000,
        lat: 10.78152,
        lng: 106.70724,
        address: '2 Tôn Đức Thắng, Quận 1, TP.HCM',
        watermark_json: JSON.stringify({
          ntpDeltaMs: 31,
          isMockGps: false,
          aegisNonce: sig.slice(0, 16).toLowerCase(),
          hmacSignature: sig,
          jpegPacked: true
        }),
        verify_status: 1,
        verdict: 'AUTHENTIC',
        created_at: now
      };
    });

    return ok({
      verified: true,
      verdict: 'AUTHENTIC',
      ocrConsistencyScore: 0.998,
      aegisHardwareAttested: true,
      jpegPackerSteganographyIntact: true,
      photoInfo: verifiedList[0],
      verifiedList
    });
  }

  // 5. Account Center, VIP Billing & Official AdMob Test Exposure Config
  if (lower === 'center/app/login') {
    return ok({
      userId: 'usr_nx_8801',
      email: body?.email || 'architect@nextai.global',
      nickname: 'NextAI Enterprise Architect',
      accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.nextai_access_token_2026',
      refreshToken: 'nx_refresh_tok_9988776655',
      expiresInSec: 604800,
      vipInfo: {
        isVip: true,
        planName: 'NextAI Plus Annual'
      }
    });
  }

  if (lower === 'center/app/user') {
    return ok({
      userId: 'usr_nx_8801',
      nickname: 'NextAI Field Lead',
      email: 'lead@nextai.global',
      industry: 'Construction & Engineering',
      avatar: 'https://static.nextai.global/avatars/default_pro.png',
      isVip: true
    });
  }

  if (lower === 'center/app/cluster/list') {
    return ok({
      clusters: [
        { clusterId: 'ap-southeast-1', name: 'NextAI Asia-Pacific (Singapore)', endpoint: `http://127.0.0.1:${activePort}/nextai/v1/` },
        { clusterId: 'us-west-1', name: 'NextAI Americas (California)', endpoint: `http://127.0.0.1:${activePort}/nextai/v1/` },
        { clusterId: 'sa-east-1', name: 'NextAI LATAM (Sao Paulo)', endpoint: `http://127.0.0.1:${activePort}/nextai/v1/` }
      ]
    });
  }

  if (lower === 'center/app/group/list' || lower === 'app/workgroup/home') {
    return ok({
      groups: dbState.teams,
      projects: dbState.projects,
      geofences: dbState.geofences
    });
  }

  if (lower === 'member/vip/info' || lower === 'member/vip/info/extra') {
    return ok({
      isVip: true,
      planName: 'NextAI Plus Annual',
      skuId: 'nextai_plus_annual_001',
      expireTimeMs: now + 365 * 86400 * 1000,
      adFreePassActive: true,
      entitlements: [
        'REMOVE_WATERMARK_JPEGPACKER',
        'AI_LOGO_CUTOUT_UNLIMITED',
        'EXPORT_EXCEL_APACHE_POI',
        'EXPORT_KMZ_GOOGLE_EARTH',
        'CUSTOM_BRAND_LOGO',
        'NO_ADS'
      ]
    });
  }

  if (lower === 'goods/query' || lower === 'business/subscription/gp/goods/list') {
    return ok({
      platform: 'google_play',
      currency: 'USD',
      skus: [
        { goodsId: 'g_plus_year', skuId: 'nextai_plus_annual_001', title: 'NextAI Plus Annual', priceUsd: 29.99, trialDays: 7 },
        { goodsId: 'g_plus_month', skuId: 'nextai_plus_monthly_002', title: 'NextAI Plus Monthly', priceUsd: 4.99, trialDays: 0 },
        { goodsId: 'g_biz_pro', skuId: 'nextai_teamspace_biz_pro', title: 'NextAI Teamspace Business Pro', priceUsd: 9.99, trialDays: 14 }
      ]
    });
  }

  if (lower === 'order/create' || lower === 'business/subscription/gp/order/create') {
    return ok({
      orderNo: `ORD-NX-${now}`,
      skuId: body?.skuId || 'nextai_plus_annual_001',
      status: 'CREATED',
      createdAtMs: now
    });
  }

  if (lower === 'order/google/receipts' || lower === 'business/subscription/gp/receipt/verify') {
    return ok({
      orderNo: body?.orderNo || `ORD-NX-${now}`,
      productId: body?.productId || 'nextai_plus_annual_001',
      receiptVerified: true,
      vipActivated: true,
      expireTimeMs: now + 365 * 86400 * 1000
    });
  }

  if (lower === 'app/adx/global_fetch_exposure_config') {
    return ok(GOOGLE_ADMOB_TEST_CONFIG);
  }

  // 6. Teamspace Geofence Haversine Endpoints
  if (lower === 'app/group/timeclock/geofence/list') {
    return ok({ geofences: dbState.geofences });
  }

  if (lower === 'app/group/timeclock/geofence/create') {
    const newGeo = {
      geofenceId: `geo_${now}`,
      teamId: body?.teamId || 'team_vn_001',
      projectId: body?.projectId || 'proj_01',
      name: body?.name || 'New Construction Geofence Zone',
      centerLat: Number(body?.centerLat ?? 10.78152),
      centerLng: Number(body?.centerLng ?? 106.70724),
      radiusMeters: Number(body?.radiusMeters ?? 150),
      requireLiveCameraCapture: true
    };
    dbState.geofences.push(newGeo);
    return ok({ created: true, geofence: newGeo });
  }

  if (lower === 'app/group/timeclock/geofence/check' || lower === 'app/attendance/punch') {
    const empLat = Number(body?.lat ?? 10.78152);
    const empLng = Number(body?.lng ?? 106.70724);
    const targetGeo =
      dbState.geofences.find(g => g.geofenceId === body?.geofenceId) ||
      {
        geofenceId: body?.geofenceId || 'geo_bason_01',
        teamId: body?.teamId || 'team_vn_001',
        projectId: 'proj_01',
        name: 'Ga Ngầm Ba Son - Cổng Số 2',
        centerLat: Number(body?.centerLat ?? 10.78152),
        centerLng: Number(body?.centerLng ?? 106.70724),
        radiusMeters: Number(body?.radiusMeters ?? 150),
        requireLiveCameraCapture: true
      };

    const distanceMeters = calculateHaversineMeters(empLat, empLng, targetGeo.centerLat, targetGeo.centerLng);
    const insideGeofence = distanceMeters <= targetGeo.radiusMeters;

    return ok({
      geofenceId: targetGeo.geofenceId,
      geofenceName: targetGeo.name,
      employeeLat: empLat,
      employeeLng: empLng,
      centerLat: targetGeo.centerLat,
      centerLng: targetGeo.centerLng,
      radiusMeters: targetGeo.radiusMeters,
      distanceMeters,
      insideGeofence,
      allowedToPunch: insideGeofence,
      punchStatus: insideGeofence ? 'CLOCK_IN_VERIFIED' : 'OUTSIDE_GEOFENCE_REJECTED',
      checkedAtUtc: new Date(now).toISOString()
    });
  }

  // 7. Teamspace Cloud Report Export & Short Links
  if (lower === 'app/plan/shortlink/create' || lower === 'app/shortlink/create') {
    const teamId = body?.teamId || 'team_vn_001';
    const planType = body?.planType || 'TEAMSPACE_BUSINESS_PRO';
    return ok({
      shortUrl: `http://127.0.0.1:${activePort}/teamspace?team-id=${teamId}&plan-id=${planType}`,
      stripeCheckoutSessionId: `cs_test_nextai_${now}`,
      expiresInSec: 86400
    });
  }

  if (lower === 'app/photo/export/task/create' || lower === 'app/photo/export/task/status') {
    const format = body?.format || 'EXCEL_APACHE_POI';
    return ok({
      taskId: body?.taskId || `exp_nx_${now}`,
      status: 'COMPLETED',
      progressPercent: 100,
      format,
      downloadUrl: `https://nextai-oversea.s3.us-west-1.amazonaws.com/exports/NextAI_Report_${format}_${now}.${format.includes('EXCEL') ? 'xlsx' : format.includes('KMZ') ? 'kmz' : 'pdf'}`
    });
  }

  // 8. Embodied AI ("Earn") Robotics .mcap Crowdsourcing
  if (lower === 'app/reward/video/balance') {
    return ok({
      balanceFen: dbState.earnWallet.walletBalanceFen,
      totalEarnedFen: dbState.earnWallet.totalEarnedFen,
      currency: dbState.earnWallet.currency,
      formattedBalance: `R$ ${(dbState.earnWallet.walletBalanceFen / 100).toFixed(2)} BRL`,
      payoutChannels: ['PIX_BRAZIL', 'SPEI_MEXICO', 'NEXTAI_PLUS_VIP_DAYS']
    });
  }

  if (lower === 'app/reward/video/task/list' || lower === 'app/reward/video/details') {
    return ok({
      tasks: [
        {
          taskId: 402,
          title: 'First-Person Hand Tool Assembly (Egocentric Video)',
          rewardAmountFen: 1500,
          currency: 'BRL',
          containerFormat: 'ROS2_MCAP',
          minDurationMs: 15000,
          maxDurationMs: 120000,
          minHandLandmarkRatio: 0.8,
          requiredStreams: [
            'HEVC_VIDEO_1080P_30FPS',
            'MEDIAPIPE_21_HAND_LANDMARKS',
            'IMU_4_SENSORS_ACCEL_GYRO_GRAVITY_ROTVEC',
            'PCM_AUDIO_48KHZ_VOICE_COMMUNICATION'
          ]
        },
        {
          taskId: 405,
          title: 'Kitchen Utensil Sorting & Bimanual Manipulation',
          rewardAmountFen: 1800,
          currency: 'BRL',
          containerFormat: 'ROS2_MCAP',
          minDurationMs: 20000,
          maxDurationMs: 180000,
          minHandLandmarkRatio: 0.85,
          requiredStreams: [
            'HEVC_VIDEO_1080P_30FPS',
            'MEDIAPIPE_21_HAND_LANDMARKS',
            'IMU_4_SENSORS_ACCEL_GYRO_GRAVITY_ROTVEC'
          ]
        }
      ]
    });
  }

  if (lower === 'app/reward/video/submit') {
    const handRatio = Number(body?.handRatio ?? 0.92);
    const durationMs = Number(body?.durationMs ?? 45000);
    const approved = handRatio >= 0.8 && durationMs >= 5000;
    const rewardFen = approved ? 1500 : 0;
    if (approved) {
      dbState.earnWallet.walletBalanceFen += rewardFen;
      dbState.earnWallet.totalEarnedFen += rewardFen;
    }
    const submission = {
      submissionId: `mcap_sub_${now}`,
      taskId: Number(body?.taskId || 402),
      mcapS3Url: body?.mcapS3Url || `s3://nextai-oversea/mcap/sub_${now}.mcap`,
      durationMs,
      handRatio,
      keypointsPerHand: 21,
      imuStreamsVerified: 4,
      qaStatus: approved ? 'APPROVED' : 'REJECTED_LOW_HAND_RATIO',
      rewardAmountFen: rewardFen,
      newWalletBalanceFen: dbState.earnWallet.walletBalanceFen,
      submittedAtUtc: new Date(now).toISOString()
    };
    dbState.earnWallet.submissions.push(submission);
    return ok(submission);
  }

  if (lower === 'app/reward/video/withdrawal/apply' || lower === 'app/reward/video/withdrawal/status') {
    const amountFen = Number(body?.amountFen || 3000);
    return ok({
      withdrawalId: body?.withdrawalId || `wd_pix_${now}`,
      amountFen,
      currency: body?.currency || 'BRL',
      channel: body?.pixKeyType ? 'PIX_BRAZIL' : 'SPEI_MEXICO',
      status: 'COMPLETED',
      processedAtUtc: new Date(now).toISOString()
    });
  }

  if (lower === 'app/reward/video/withdrawal/banks') {
    return ok({
      country: body?.country || 'BR',
      banks: [
        { bankCode: 'PIX_INSTANT', bankName: 'Banco Central do Brasil (Pix Instant)' },
        { bankCode: '260', bankName: 'Nu Pagamentos S.A. (Nubank)' },
        { bankCode: '341', bankName: 'Itaú Unibanco' },
        { bankCode: 'SPEI_CLABE', bankName: 'Banco de México (SPEI CLABE)' }
      ]
    });
  }

  // 9. S3 & OSS STS Temporary Credentials
  if (lower === 'workgroup/v4/s3/token' || lower === 'workgroup/v4/oss/token') {
    return ok({
      accessKeyId: 'ASIANEXTAISTSTOKEN2026',
      secretAccessKey: 'nextaiS3TemporarySecretKey998877665544332211',
      sessionToken: `nx_sts_session_${now}`,
      bucket: 'nextai-oversea',
      region: 'us-west-1',
      endpoint: 'https://s3.us-west-1.amazonaws.com',
      expirationMs: now + 3600 * 1000
    });
  }

  // 10. Dynamic Match from the 177 Endpoint Catalog or Catch-All
  const matchedEndpoint = ENDPOINT_CATALOG.find(e => e.path.replace(/\s+/g, '').toLowerCase() === lower);
  return ok({
    endpointId: matchedEndpoint?.id ?? 177,
    service: matchedEndpoint?.service ?? 'NextAIDynamicRouter',
    path: matchedEndpoint?.path ?? cleanPath,
    method,
    description: matchedEndpoint?.description ?? 'NextAI Enterprise Gateway Dynamic Handler',
    timestampMs: now,
    requestEcho: body || {},
    verifiedBy: 'nextai.global'
  });
}

export function createNextAIServer(getActivePort: () => number): http.Server {
  return http.createServer((req, res) => {
    // CORS headers for Web CMS, Truth H5, Teamspace Portal & Mobile Clients
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-NextAI-Device-Id');

    if (req.method === 'OPTIONS') {
      res.writeHead(204);
      res.end();
      return;
    }

    const parsedUrl = url.parse(req.url || '/', true);
    const pathname = parsedUrl.pathname || '/';
    const activePort = getActivePort();

    // Built-in Web & Telemetry Routes
    if (req.method === 'GET' && (pathname === '/' || pathname === '/cms')) {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      res.end(readPublicHtml('cms.html', 'NextAI Super Admin CMS'));
      return;
    }

    if (req.method === 'GET' && (pathname === '/truth' || pathname === '/cheatingReport')) {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      res.end(readPublicHtml('truth.html', 'NextAI Truth Verification Center'));
      return;
    }

    if (req.method === 'GET' && pathname === '/teamspace') {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      res.end(readPublicHtml('teamspace.html', 'NextAI Teamspace Enterprise Portal'));
      return;
    }

    if (req.method === 'GET' && pathname === '/health') {
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(
        JSON.stringify(
          ok({
            service: 'NextAI Camera Enterprise Backend & Super Admin CMS',
            version: '10.0.281',
            status: 'HEALTHY',
            port: activePort,
            uptimeSec: Math.round(process.uptime()),
            nodeVersion: process.version,
            totalEndpoints: ENDPOINT_CATALOG.length,
            relationalTables: 42,
            olapTables: 6,
            brandSanitized: true,
            adMobTestKeysActive: true
          })
        )
      );
      return;
    }

    if (req.method === 'GET' && pathname === '/api/catalog') {
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(
        JSON.stringify(
          ok({
            totalEndpoints: ENDPOINT_CATALOG.length,
            services: {
              TodayApiServiceKt: 68,
              ClusterApiService: 91,
              EarnApiService: 9,
              DualModeApiService: 7,
              OSSAndS3TokenApi: 2
            },
            endpoints: ENDPOINT_CATALOG
          })
        )
      );
      return;
    }

    if (req.method === 'GET' && pathname === '/api/olap/summary') {
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(
        JSON.stringify(
          ok({
            warehouse: 'nextai_olap_warehouse (ClickHouse / StarRocks / DuckDB)',
            relationalDatabase: 'nextai_camera_db (42 Tables)',
            factTables: [
              'fact_photo_captures',
              'fact_anti_fake_verifications',
              'fact_embodied_mcap_submissions',
              'fact_ad_impressions_and_revenue',
              'fact_subscription_conversions'
            ],
            materializedViews: ['mv_teamspace_attendance_realtime', 'mv_esg_construction_compliance'],
            metrics: {
              totalVerifiedPhotos: 31890,
              geofenceComplianceRate: 0.992,
              approvedMcapRecordings: 1420 + dbState.earnWallet.submissions.length
            }
          })
        )
      );
      return;
    }

    // Collect JSON body for API routes
    const chunks: Buffer[] = [];
    req.on('data', chunk => chunks.push(Buffer.from(chunk)));
    req.on('end', () => {
      let body: any = {};
      const rawBody = Buffer.concat(chunks).toString('utf8').trim();
      if (rawBody) {
        try {
          body = JSON.parse(rawBody);
        } catch {
          body = { raw: rawBody };
        }
      } else if (parsedUrl.query && Object.keys(parsedUrl.query).length > 0) {
        body = parsedUrl.query;
      }

      const responsePayload = handleApiRoute(pathname, req.method || 'POST', body, activePort);
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify(responsePayload));
    });
  });
}

// Automatic port binder in range 1000..10000 (defaults to 4580)
export async function startServer(preferredPort?: number): Promise<{ server: http.Server; port: number }> {
  const startPort = preferredPort ?? Number(process.env.PORT || 4580);
  let activePort = startPort;
  const server = createNextAIServer(() => activePort);

  const tryListen = (portToTry: number): Promise<number> => {
    return new Promise((resolve, reject) => {
      const onError = (err: any) => {
        server.removeListener('listening', onListening);
        if (err && (err.code === 'EADDRINUSE' || err.code === 'EACCES')) {
          const nextCandidate = portToTry >= 10000 ? 1000 : portToTry + 1;
          resolve(tryListen(nextCandidate));
        } else {
          reject(err);
        }
      };
      const onListening = () => {
        server.removeListener('error', onError);
        const addr = server.address();
        const boundPort = typeof addr === 'object' && addr ? addr.port : portToTry;
        resolve(boundPort);
      };
      server.once('error', onError);
      server.once('listening', onListening);
      server.listen(portToTry, '0.0.0.0');
    });
  };

  activePort = await tryListen(startPort);
  return { server, port: activePort };
}

if (require.main === module) {
  startServer().then(({ port }) => {
    console.log(`[NextAI Backend] Server listening on http://127.0.0.1:${port}`);
    console.log(`[NextAI CMS]    Super Admin CMS:  http://127.0.0.1:${port}/cms`);
    console.log(`[NextAI Truth]  Truth H5 Portal:  http://127.0.0.1:${port}/truth`);
    console.log(`[NextAI SaaS]   Teamspace Portal: http://127.0.0.1:${port}/teamspace`);
  });
}
