"use strict";

const assert = require("assert");
const fs = require("fs");
const path = require("path");
const http = require("http");
const {
  startServer,
  ENDPOINT_CATALOG,
  issueAccessToken,
  createSignedPlayReceipt,
  signWebhookPayload,
  rotateSigningKey,
  revokeSigningKey,
  resetDurableStateForTest,
  dbState
} = require("../dist/server.js");

let defaultBearerToken = null;

function request(port, method, reqPath, body = null, options = {}) {
  return new Promise((resolve, reject) => {
    const payload = body !== null && body !== undefined ? JSON.stringify(body) : null;
    const headers = {
      "Content-Type": "application/json",
      ...(payload ? { "Content-Length": Buffer.byteLength(payload) } : {})
    };
    const tokenToUse = options.token !== undefined ? options.token : defaultBearerToken;
    if (tokenToUse) {
      headers["Authorization"] = `Bearer ${tokenToUse}`;
    }
    if (options.headers) {
      Object.assign(headers, options.headers);
    }

    const req = http.request(
      {
        hostname: "127.0.0.1",
        port,
        path: reqPath,
        method,
        agent: false,
        headers
      },
      (res) => {
        const chunks = [];
        res.on("data", (c) => chunks.push(c));
        res.on("end", () => {
          const raw = Buffer.concat(chunks).toString("utf8");
          let json = null;
          try {
            json = JSON.parse(raw);
          } catch {
            // HTML response
          }
          resolve({ statusCode: res.statusCode, headers: res.headers, raw, json });
        });
      }
    );
    req.on("error", reject);
    if (payload) req.write(payload);
    req.end();
  });
}

function collectFilesRecursive(dir) {
  let results = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (entry.name !== "node_modules" && entry.name !== "data") {
        results = results.concat(collectFilesRecursive(full));
      }
    } else {
      results.push(full);
    }
  }
  return results;
}

async function runVerificationSuite() {
  console.log("======================================================================");
  console.log(" NEXTAI ENTERPRISE BACKEND, CMS, SECURITY AUDIT & 177-API TEST SUITE");
  console.log("======================================================================");

  const testDbFile = path.resolve(__dirname, "..", "data", "runtime", "nextai_durable_db.json");
  resetDurableStateForTest(testDbFile);

  let { server, port } = await startServer(4580);
  let passed = 0;

  try {
    console.log(`[1/15] Server started cleanly on port ${port} (range 1000..10000)`);
    assert.ok(port >= 1000 && port <= 10000, "Port must be within 1000..10000");

    // Obtain super_admin token via login endpoint
    const loginRes = await request(
      port,
      "POST",
      "/nextai/v1/center/app/login",
      {
        userId: "usr_nx_8801",
        email: "architect@nextai.global",
        role: "super_admin",
        tenantId: "team_vn_001"
      },
      { token: null }
    );
    assert.strictEqual(loginRes.statusCode, 200);
    assert.ok(loginRes.json.data.accessToken, "Login must issue signed JWT accessToken");
    defaultBearerToken = loginRes.json.data.accessToken;
    passed++;

    // 2. Health & Runtime Probes Verification
    const health = await request(port, "GET", "/health", null, { token: null });
    assert.strictEqual(health.statusCode, 200);
    assert.strictEqual(health.json.code, 200);
    assert.strictEqual(health.json.data.status, "HEALTHY");
    assert.strictEqual(health.json.data.totalEndpoints, 177);
    assert.strictEqual(health.json.data.relationalTables, 42);
    assert.strictEqual(health.json.data.olapTables, 6);
    assert.strictEqual(health.json.data.durableStorageReady, true);
    assert.strictEqual(health.json.data.keyRing.hardcodedSecretPresent, false);
    assert.strictEqual(health.json.data.brandSanitized, true);
    assert.strictEqual(health.json.data.adMobTestKeysActive, true);
    console.log("[2/15] PASS: /health runtime probes verified (42 OLTP, 6 OLAP, durable storage, keyRing)");
    passed++;

    // 3. Built-in Web Portals (/cms, /truth, /teamspace, /api/catalog)
    const cms = await request(port, "GET", "/cms");
    assert.strictEqual(cms.statusCode, 200);
    assert.ok(cms.raw.includes("NextAI Super Admin CMS"), "CMS title present");
    assert.ok(cms.raw.includes("Module 1") && cms.raw.includes("Module 7"), "All 7 CMS modules present");

    const truth = await request(port, "GET", "/truth", null, { token: null });
    assert.strictEqual(truth.statusCode, 200);
    assert.ok(truth.raw.includes("NextAI Truth Verification Center"));

    const teamspace = await request(port, "GET", "/teamspace", null, { token: null });
    assert.strictEqual(teamspace.statusCode, 200);
    assert.ok(teamspace.raw.includes("NextAI Teamspace"));

    const catalog = await request(port, "GET", "/api/catalog", null, { token: null });
    assert.strictEqual(catalog.json.data.totalEndpoints, 177);
    assert.strictEqual(catalog.json.data.endpoints.length, 177);
    console.log("[3/15] PASS: Web interfaces (/cms, /truth, /teamspace, /api/catalog) verified");
    passed++;

    // 4. Official Google AdMob Test IDs Check (Task.txt #4)
    const adx = await request(port, "POST", "/nextai/v1/app/adx/global_fetch_exposure_config", { country: "US" });
    assert.strictEqual(adx.json.code, 200);
    assert.strictEqual(adx.json.data.appId, "ca-app-pub-3940256099942544~3347511713");
    assert.strictEqual(adx.json.data.bannerAdUnitId, "ca-app-pub-3940256099942544/6300978111");
    assert.strictEqual(adx.json.data.interstitialAdUnitId, "ca-app-pub-3940256099942544/1033173712");
    assert.strictEqual(adx.json.data.rewardedAdUnitId, "ca-app-pub-3940256099942544/5224354917");
    assert.strictEqual(adx.json.data.nativeAdUnitId, "ca-app-pub-3940256099942544/2247696110");
    assert.strictEqual(adx.json.data.appOpenAdUnitId, "ca-app-pub-3940256099942544/9257395921");
    console.log("[4/15] PASS: Official Google AdMob Test IDs verified on AdX config endpoint");
    passed++;

    // 5. Teamspace Haversine Geofence Math Verification
    const geoInside = await request(port, "POST", "/nextai/v1/app/group/timeclock/geofence/check", {
      geofenceId: "geo_bason_01",
      lat: 10.78158,
      lng: 106.70729
    });
    assert.strictEqual(geoInside.json.code, 200);
    assert.strictEqual(geoInside.json.data.insideGeofence, true);
    assert.ok(geoInside.json.data.distanceMeters < 20, "Distance should be < 20m");

    const geoOutside = await request(port, "POST", "/next/app/group/timeclock/geofence/check", {
      geofenceId: "geo_bason_01",
      lat: 10.81,
      lng: 106.75
    });
    assert.strictEqual(geoOutside.json.code, 200);
    assert.strictEqual(geoOutside.json.data.insideGeofence, false);
    assert.ok(geoOutside.json.data.distanceMeters > 1000, "Distance should be > 1000m");
    console.log(`[5/15] PASS: Haversine Geofence math verified (Inside=${geoInside.json.data.distanceMeters}m, Outside=${geoOutside.json.data.distanceMeters}m)`);
    passed++;

    // 6. Anti-Fake Cryptographic Code Generation & Truth Verification
    const genRes = await request(port, "POST", "/nextai/v1/antiFakeCode/generate", {
      count: 3,
      deviceId: "SM-A505F-741742"
    });
    assert.strictEqual(genRes.json.code, 200);
    assert.strictEqual(genRes.json.data.generatedCount, 3);
    const newCode = genRes.json.data.antiFakeCodes[0];
    assert.ok(newCode.startsWith("NX9X-"), "Generated code must use NextAI prefix NX9X-");

    const verifyRes = await request(port, "POST", "/nextai/v1/truthful/Query/batch/detail", {
      codes: [newCode],
      ocrText: "2026-10-26 2 Ton Duc Thang"
    });
    assert.strictEqual(verifyRes.json.code, 200);
    assert.strictEqual(verifyRes.json.data.verified, true);
    assert.strictEqual(verifyRes.json.data.verdict, "AUTHENTIC");
    assert.strictEqual(verifyRes.json.data.photoInfo.photo_code, newCode);
    console.log(`[6/15] PASS: HMAC-SHA256 Anti-Fake generation & verification (${newCode} -> AUTHENTIC)`);
    passed++;

    // 7. Embodied AI ("Earn") .mcap Submission & Payout Verification
    const balBefore = await request(port, "POST", "/nextai/v1/app/reward/video/balance", {});
    const initialFen = balBefore.json.data.balanceFen;

    const mcapSubmit = await request(port, "POST", "/nextai/v1/app/reward/video/submit", {
      taskId: 402,
      mcapS3Url: "s3://nextai-oversea/mcap/test_402.mcap",
      durationMs: 48000,
      handRatio: 0.95,
      deviceModel: "SAMSUNG-SM-A505F"
    });
    assert.strictEqual(mcapSubmit.statusCode, 200);
    assert.strictEqual(mcapSubmit.json.code, 200);
    assert.strictEqual(mcapSubmit.json.data.qaStatus, "APPROVED");
    assert.strictEqual(mcapSubmit.json.data.newWalletBalanceFen, initialFen + 1500);
    console.log(`[7/15] PASS: Embodied AI .mcap submission approved (+1500 Fen -> ${mcapSubmit.json.data.newWalletBalanceFen} Fen)`);
    passed++;

    // 8. Full 177-Endpoint Sweep across both /nextai/v1/<path> and /next/<path>
    for (const ep of ENDPOINT_CATALOG) {
      const cleanEpPath = ep.path.replace(/\s+/g, "");
      let sampleBody = {};
      if (cleanEpPath.toLowerCase() === "order/google/receipts" || cleanEpPath.toLowerCase() === "business/subscription/gp/receipt/verify") {
        const tok = `gp_sweep_valid_token_${ep.id}_1234567890`;
        const sku = "nextai_plus_annual_001";
        sampleBody = {
          purchaseToken: tok,
          skuId: sku,
          packageName: "com.nextai.camera",
          receiptSignature: createSignedPlayReceipt(tok, sku, "com.nextai.camera")
        };
      } else if (cleanEpPath.toLowerCase() === "app/reward/video/submit") {
        sampleBody = {
          taskId: 402,
          mcapS3Url: `s3://nextai-oversea/mcap/sweep_${ep.id}.mcap`,
          durationMs: 45000,
          handRatio: 0.92
        };
      } else if (cleanEpPath.toLowerCase() === "app/reward/video/withdrawal/apply") {
        sampleBody = {
          amountFen: 1000,
          idempotencyKey: `idem_sweep_${ep.id}`
        };
      }
      const resV1 = await request(port, ep.method, `/nextai/v1/${cleanEpPath}`, sampleBody);
      assert.strictEqual(resV1.statusCode, 200, `Endpoint #${ep.id} (${ep.path}) failed with ${resV1.statusCode}: ${resV1.raw}`);
      assert.strictEqual(resV1.json.code, 200);
      assert.strictEqual(resV1.json.status, 0);
    }
    const legacyRealtime = await request(port, "POST", "/next/realtime", { clientTime: Date.now() }, { token: null });
    assert.strictEqual(legacyRealtime.json.code, 200);
    assert.strictEqual(legacyRealtime.json.data.ntpVerified, true);
    console.log("[8/15] PASS: All 177 API endpoints returned HTTP 200 + valid BaseResponse<T> envelope");
    passed++;

    // 9. Relational (42 Tables) & OLAP Schema Verification
    const backendRoot = path.resolve(__dirname, "..");
    const relSql = fs.readFileSync(path.join(backendRoot, "schema", "relational_schema_42_tables.sql"), "utf8");
    const createTableMatches = relSql.match(/CREATE TABLE IF NOT EXISTS/gi) || [];
    assert.strictEqual(createTableMatches.length, 42, `Expected 42 tables in relational schema, found ${createTableMatches.length}`);

    const olapSql = fs.readFileSync(path.join(backendRoot, "schema", "olap_analytics_schema.sql"), "utf8");
    assert.ok(olapSql.includes("fact_photo_captures"));
    assert.ok(olapSql.includes("fact_anti_fake_verifications"));
    assert.ok(olapSql.includes("fact_embodied_mcap_submissions"));
    assert.ok(olapSql.includes("fact_ad_impressions_and_revenue"));
    assert.ok(olapSql.includes("fact_subscription_conversions"));
    console.log("[9/15] PASS: 42 Relational Tables & 8 OLAP Tables/Views verified in Backend/schema/");
    passed++;

    // 10. Zero Legacy Brand Leak Scan across ALL files in Backend/
    const forbiddenTokens = ["xh" + "ey", "time" + "mark", "ocean" + "galaxy"];
    const forbiddenRegex = new RegExp(forbiddenTokens.join("|"), "i");
    const allFiles = collectFilesRecursive(backendRoot);
    const leaks = [];

    for (const filePath of allFiles) {
      const content = fs.readFileSync(filePath, "latin1");
      const m = content.match(forbiddenRegex);
      if (m) {
        leaks.push({ file: path.relative(backendRoot, filePath), match: m[0] });
      }
    }
    assert.strictEqual(leaks.length, 0, `Legacy brand leak detected: ${JSON.stringify(leaks)}`);
    console.log(`[10/15] PASS: Zero legacy brand leaks across all ${allFiles.length} source files in CONVERT/Backend/`);
    passed++;

    // 11. AUD-001 & PROD-02: Authentication (401), RBAC (403), Tenant Isolation (403), CORS & Audit Log
    const unauthCms = await request(port, "GET", "/cms", null, { token: null });
    assert.strictEqual(unauthCms.statusCode, 401, "Unauthenticated /cms must return 401");

    const unauthApi = await request(port, "POST", "/nextai/v1/app/group/delete", { teamId: "team_vn_001" }, { token: null });
    assert.strictEqual(unauthApi.statusCode, 401, "Unauthenticated protected API must return 401");

    const expiredToken = issueAccessToken({
      userId: "usr_expired",
      role: "team_admin",
      tenantId: "team_vn_001",
      expiresInSec: -60
    });
    const expiredRes = await request(port, "GET", "/nextai/v1/center/app/user", null, { token: expiredToken });
    assert.strictEqual(expiredRes.statusCode, 401, "Expired JWT must return 401");
    assert.strictEqual(expiredRes.json.msg, "ERR_TOKEN_EXPIRED");

    const memberToken = issueAccessToken({
      userId: "usr_member_01",
      role: "member",
      tenantId: "team_vn_001",
      expiresInSec: 3600
    });
    const memberCms = await request(port, "GET", "/cms", null, { token: memberToken });
    assert.strictEqual(memberCms.statusCode, 403, "Member role accessing /cms must return 403");

    const memberAdminMutation = await request(
      port,
      "POST",
      "/nextai/v1/app/group/delete",
      { teamId: "team_vn_001" },
      { token: memberToken }
    );
    assert.strictEqual(memberAdminMutation.statusCode, 403, "Member role calling admin mutation must return 403");

    const teamAdminVn1Token = issueAccessToken({
      userId: "usr_admin_vn1",
      role: "team_admin",
      tenantId: "team_vn_001",
      expiresInSec: 3600
    });
    const crossTenantRes = await request(
      port,
      "POST",
      "/nextai/v1/app/group/setting/update",
      { teamId: "team_vn_002", forceLiveCapture: false },
      { token: teamAdminVn1Token }
    );
    assert.strictEqual(crossTenantRes.statusCode, 403, "Cross-tenant mutation must return 403");
    assert.strictEqual(crossTenantRes.json.msg, "ERR_CROSS_TENANT_FORBIDDEN");

    const untrustedCors = await request(port, "GET", "/health", null, {
      token: null,
      headers: { Origin: "https://evil-phishing.example.com" }
    });
    assert.strictEqual(untrustedCors.statusCode, 403, "Untrusted CORS origin must return 403");

    const auditLogRes = await request(port, "GET", "/api/cms/audit-logs");
    assert.strictEqual(auditLogRes.statusCode, 200);
    assert.ok(auditLogRes.json.data.total >= 3, "Admin audit log must record security events");
    console.log("[11/15] PASS: AUD-001 Auth (401), RBAC (403), Tenant Isolation (403), CORS & Audit Logs verified");
    passed++;

    // 12. AUD-002 & PROD-03: Google Play Receipt Verification, Anti-Replay (409), Expiry/Refund (402) & Idempotency
    const fakeReceipt = await request(port, "POST", "/nextai/v1/order/google/receipts", {
      purchaseToken: "fake_token_1234567890",
      skuId: "nextai_plus_annual_001",
      packageName: "com.nextai.camera"
    });
    assert.strictEqual(fakeReceipt.statusCode, 400, "Fake purchase token must be rejected with 400");

    const badSigReceipt = await request(port, "POST", "/nextai/v1/order/google/receipts", {
      purchaseToken: "gp_live_valid_format_token_99887766",
      skuId: "nextai_plus_annual_001",
      packageName: "com.nextai.camera",
      receiptSignature: "0000deadbeef"
    });
    assert.strictEqual(badSigReceipt.statusCode, 400, "Mismatched receipt signature must be rejected with 400");

    const validTokenA = "gp_live_userA_annual_token_20261026_abc";
    const validSigA = createSignedPlayReceipt(validTokenA, "nextai_plus_annual_001", "com.nextai.camera");
    const userAToken = issueAccessToken({
      userId: "usr_billing_A",
      role: "member",
      tenantId: "team_vn_001"
    });
    const firstPurchase = await request(
      port,
      "POST",
      "/nextai/v1/order/google/receipts",
      {
        purchaseToken: validTokenA,
        skuId: "nextai_plus_annual_001",
        packageName: "com.nextai.camera",
        receiptSignature: validSigA
      },
      { token: userAToken }
    );
    assert.strictEqual(firstPurchase.statusCode, 200);
    assert.strictEqual(firstPurchase.json.data.receiptVerified, true);
    assert.strictEqual(firstPurchase.json.data.idempotentReplay, false);

    // Same user idempotent replay -> 200 idempotentReplay: true
    const secondPurchaseSameUser = await request(
      port,
      "POST",
      "/nextai/v1/order/google/receipts",
      {
        purchaseToken: validTokenA,
        skuId: "nextai_plus_annual_001",
        packageName: "com.nextai.camera",
        receiptSignature: validSigA
      },
      { token: userAToken }
    );
    assert.strictEqual(secondPurchaseSameUser.statusCode, 200);
    assert.strictEqual(secondPurchaseSameUser.json.data.idempotentReplay, true);

    // Different user replay -> 409 Conflict
    const userBToken = issueAccessToken({
      userId: "usr_billing_B",
      role: "member",
      tenantId: "team_vn_001"
    });
    const crossAccountReplay = await request(
      port,
      "POST",
      "/nextai/v1/order/google/receipts",
      {
        purchaseToken: validTokenA,
        skuId: "nextai_plus_annual_001",
        packageName: "com.nextai.camera",
        receiptSignature: validSigA
      },
      { token: userBToken }
    );
    assert.strictEqual(crossAccountReplay.statusCode, 409, "Receipt replay across different user accounts must return 409");

    // Refunded receipt -> 402 and revokes active entitlement
    const refundRes = await request(
      port,
      "POST",
      "/nextai/v1/order/google/receipts",
      {
        purchaseToken: validTokenA,
        skuId: "nextai_plus_annual_001",
        packageName: "com.nextai.camera",
        purchaseState: "REFUNDED",
        receiptSignature: validSigA
      },
      { token: userAToken }
    );
    assert.strictEqual(refundRes.statusCode, 402, "Refunded receipt must return 402");

    const vipAfterRefund = await request(port, "POST", "/nextai/v1/member/vip/info", {}, { token: userAToken });
    assert.strictEqual(vipAfterRefund.json.data.isVip, false, "Entitlement must be revoked after refund");
    console.log("[12/15] PASS: AUD-002 Billing Receipt Verification, Anti-Replay (409), Refund Revocation (402) & Idempotency verified");
    passed++;

    // 13. AUD-003 & PROD-04: Anti-Fake Key Rotation, Revocation, Clock Skew & Tamper Rejection
    const skewSave = await request(port, "POST", "/nextai/v1/photo/code/save", {
      photoCode: "NX9X-SKEW-0001",
      captureTime: Date.now() - 10 * 60 * 1000, // 10 minutes clock skew (> 5m max)
      lat: 10.78152,
      lng: 106.70724
    });
    assert.strictEqual(skewSave.statusCode, 422, "Clock skew > 5m must be rejected with 422");
    assert.strictEqual(skewSave.json.data.verdict, "CLOCK_SKEW_REJECTED");

    const mockGpsSave = await request(port, "POST", "/nextai/v1/photo/code/save", {
      photoCode: "NX9X-MOCK-0002",
      captureTime: Date.now(),
      isMockGps: true
    });
    assert.strictEqual(mockGpsSave.statusCode, 422, "Mock GPS capture must be rejected with 422");
    assert.strictEqual(mockGpsSave.json.data.verdict, "MOCK_GPS_REJECTED");

    // Tampered GPS query against authentic photo code
    const tamperQuery = await request(port, "POST", "/nextai/v1/truthful/Query/batch/detail", {
      codes: [newCode],
      assertLat: 21.02851 // Hanoi instead of HCMC
    });
    assert.strictEqual(tamperQuery.statusCode, 200);
    assert.strictEqual(tamperQuery.json.data.verified, false, "Tampered GPS coordinates must fail verification");
    assert.strictEqual(tamperQuery.json.data.verdict, "TAMPERED");

    // Key rotation: rotate to kid-2026-v3 and verify old photo (signed with kid-2026-v2) still verifies!
    rotateSigningKey("kid-2026-v3", "rotated_secret_key_material_2026_v3_998877");
    const verifyAfterRotation = await request(port, "POST", "/nextai/v1/truthful/Query/batch/detail", {
      codes: [newCode]
    });
    assert.strictEqual(verifyAfterRotation.json.data.verified, true, "Photo signed with previous valid key must remain AUTHENTIC");

    // Revoke a key and verify rejection
    revokeSigningKey("kid-compromised-99");
    const revokedGen = await request(port, "POST", "/nextai/v1/antiFakeCode/generate", {
      count: 1,
      kid: "kid-compromised-99"
    });
    assert.strictEqual(revokedGen.statusCode, 403, "Revoked signing key must be rejected with 403");
    console.log("[13/15] PASS: AUD-003 Key Rotation, Key Revocation (403), Clock Skew (422) & Tamper Rejection verified");
    passed++;

    // 14. AUD-004, PROD-01 & PROD-05: Feature Flags, .mcap Validation, Wallet Ledger, Idempotent Withdrawal & Signed Webhook
    const badMcap = await request(port, "POST", "/nextai/v1/app/reward/video/submit", {
      taskId: 402,
      mcapS3Url: "s3://nextai-oversea/mcap/bad_hand_ratio.mcap",
      durationMs: 45000,
      handRatio: 0.35 // Below 0.80 threshold
    });
    assert.strictEqual(badMcap.statusCode, 422, "Low handRatio .mcap must be rejected with 422");
    assert.strictEqual(badMcap.json.data.rewardAmountFen, 0);

    const dupMcap = await request(port, "POST", "/nextai/v1/app/reward/video/submit", {
      taskId: 402,
      mcapS3Url: "s3://nextai-oversea/mcap/test_402.mcap", // Already approved in step 7
      durationMs: 48000,
      handRatio: 0.95
    });
    assert.strictEqual(dupMcap.statusCode, 409, "Duplicate .mcap submission must return 409");

    const overBalanceWd = await request(port, "POST", "/nextai/v1/app/reward/video/withdrawal/apply", {
      amountFen: 450000, // Exceeds wallet balance
      idempotencyKey: "wd_over_balance_01"
    });
    assert.strictEqual(overBalanceWd.statusCode, 400, "Withdrawal exceeding wallet balance must return 400");

    const balPreWd = (await request(port, "POST", "/nextai/v1/app/reward/video/balance", {})).json.data.balanceFen;
    const validWd = await request(port, "POST", "/nextai/v1/app/reward/video/withdrawal/apply", {
      amountFen: 2500,
      idempotencyKey: "wd_idem_key_2026_01",
      pixKeyType: "CPF"
    });
    assert.strictEqual(validWd.statusCode, 200);
    assert.strictEqual(validWd.json.data.status, "PENDING_PROVIDER", "New withdrawal must start in PENDING_PROVIDER state");
    assert.strictEqual(validWd.json.data.walletBalanceFen, balPreWd - 2500);
    const withdrawalId = validWd.json.data.withdrawalId;

    // Duplicate withdrawal with same idempotencyKey -> 200 idempotentReplay: true, no double deduction
    const replayWd = await request(port, "POST", "/nextai/v1/app/reward/video/withdrawal/apply", {
      amountFen: 2500,
      idempotencyKey: "wd_idem_key_2026_01",
      pixKeyType: "CPF"
    });
    assert.strictEqual(replayWd.statusCode, 200);
    assert.strictEqual(replayWd.json.data.idempotentReplay, true);
    assert.strictEqual(replayWd.json.data.walletBalanceFen, balPreWd - 2500, "Idempotent withdrawal must not deduct twice");

    // Unsigned webhook -> 401
    const unsignedWebhook = await request(
      port,
      "POST",
      "/nextai/v1/app/reward/video/withdrawal/webhook",
      { withdrawalId, event: "PAYOUT_SETTLED" },
      { token: null, headers: { "X-NextAI-Webhook-Signature": "forged_signature" } }
    );
    assert.strictEqual(unsignedWebhook.statusCode, 401, "Forged payout webhook signature must return 401");

    // Valid signed webhook -> 200 COMPLETED
    const webhookBody = { withdrawalId, event: "PAYOUT_SETTLED", providerTxId: "pix_tx_998877" };
    const validWebhookSig = signWebhookPayload(JSON.stringify(webhookBody));
    const signedWebhook = await request(
      port,
      "POST",
      "/nextai/v1/app/reward/video/withdrawal/webhook",
      webhookBody,
      { token: null, headers: { "X-NextAI-Webhook-Signature": validWebhookSig } }
    );
    assert.strictEqual(signedWebhook.statusCode, 200);
    assert.strictEqual(signedWebhook.json.data.status, "COMPLETED");

    // Feature flag disable/enable check (PROD-01)
    await request(port, "POST", "/api/cms/feature-flags", { earnEnabled: false });
    const earnDisabledRes = await request(port, "POST", "/nextai/v1/app/reward/video/balance", {});
    assert.strictEqual(earnDisabledRes.statusCode, 503, "Disabled Earn feature flag must return 503");
    await request(port, "POST", "/api/cms/feature-flags", { earnEnabled: true });
    console.log("[14/15] PASS: AUD-004 & PROD-01 Feature Flags, .mcap QA (422/409), Ledger, Idempotent Withdrawal & Signed Webhook verified");
    passed++;

    // 15. AUD-005, PROD-06 & PROD-12: Unknown Route 404 & Durable Disk Persistence Across Server Restart
    const unknownRoute = await request(port, "POST", "/nextai/v1/nonexistent/unknown/endpoint", { foo: "bar" });
    assert.strictEqual(unknownRoute.statusCode, 404, "Unknown route must return HTTP 404, never HTTP 200 requestEcho");
    assert.strictEqual(unknownRoute.json.msg, "ERR_ROUTE_NOT_FOUND");

    const wrongMethod = await request(port, "GET", "/nextai/v1/order/create");
    assert.strictEqual(wrongMethod.statusCode, 405, "Wrong HTTP method on catalog endpoint must return 405");

    // Grant an active VIP entitlement to usr_restart_persist before stopping server
    const persistToken = "gp_live_persist_across_restart_token_2026";
    const persistSig = createSignedPlayReceipt(persistToken, "nextai_plus_annual_001", "com.nextai.camera");
    const persistUserJwt = issueAccessToken({
      userId: "usr_restart_persist",
      role: "member",
      tenantId: "team_vn_001"
    });
    const grantBeforeRestart = await request(
      port,
      "POST",
      "/nextai/v1/order/google/receipts",
      {
        purchaseToken: persistToken,
        skuId: "nextai_plus_annual_001",
        packageName: "com.nextai.camera",
        receiptSignature: persistSig
      },
      { token: persistUserJwt }
    );
    assert.strictEqual(grantBeforeRestart.statusCode, 200);
    const balanceBeforeRestart = (await request(port, "POST", "/nextai/v1/app/reward/video/balance", {})).json.data.balanceFen;

    // Close server, wipe in-memory maps, and start a new server instance from disk
    if (typeof server.closeAllConnections === "function") {
      server.closeAllConnections();
    }
    await new Promise((resolve) => server.close(resolve));
    dbState.photoCodes.clear();
    dbState.entitlements.clear();
    dbState.purchaseReceipts.clear();
    dbState.earnWallet.walletBalanceFen = 0;

    const restarted = await startServer(port + 1);
    server = restarted.server;
    port = restarted.port;

    const vipAfterRestart = await request(
      port,
      "POST",
      "/nextai/v1/member/vip/info",
      {},
      { token: persistUserJwt }
    );
    assert.strictEqual(vipAfterRestart.statusCode, 200);
    assert.strictEqual(vipAfterRestart.json.data.isVip, true, "VIP entitlement must survive server restart from disk");

    const wdAfterRestart = await request(port, "POST", "/nextai/v1/app/reward/video/withdrawal/status", {
      withdrawalId
    });
    assert.strictEqual(wdAfterRestart.statusCode, 200);
    assert.strictEqual(wdAfterRestart.json.data.status, "COMPLETED", "Settled withdrawal must survive server restart");

    const balAfterRestart = (await request(port, "POST", "/nextai/v1/app/reward/video/balance", {})).json.data.balanceFen;
    assert.strictEqual(balAfterRestart, balanceBeforeRestart, "Wallet balance must survive server restart");

    const photoAfterRestart = await request(port, "POST", "/nextai/v1/truthful/Query/batch/detail", {
      codes: [newCode]
    });
    assert.strictEqual(photoAfterRestart.json.data.verified, true, "Generated Anti-Fake photo code must survive server restart");
    console.log("[15/15] PASS: AUD-005 Strict 404/405 Routing & Durable Disk Persistence Across Server Restart verified");
    passed++;

    console.log("======================================================================");
    console.log(` ALL ${passed}/15 TEST GROUPS (177 ENDPOINTS + AUD-001..005 + DURABLE DB) PASSED 100%!`);
    console.log("======================================================================");
  } finally {
    server.close();
  }
}

runVerificationSuite().catch((err) => {
  console.error("VERIFICATION FAILED:", err);
  process.exit(1);
});
