-- ============================================================================
-- NEXTAI ENTERPRISE OLAP ANALYTICS SCHEMA (nextai_olap_warehouse)
-- Compatible with ClickHouse 24.x / StarRocks 3.x / DuckDB 1.x
-- Purpose: Real-time B2B Teamspace Telemetry, Anti-Fake Cryptographic Audit,
--          Embodied AI Robotics (.mcap) Quality Scoring, AdX & Subscription Funnel
-- Domain Ecosystem: nextai.global | nextai.solution | nextai.technology
-- ============================================================================

CREATE DATABASE IF NOT EXISTS nextai_olap_warehouse;

-- ----------------------------------------------------------------------------
-- 1. DIMENSION TABLES (Star Schema Dimensions)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nextai_olap_warehouse.dim_teams (
    team_id String,
    team_name String,
    country_code FixedString(2),
    industry_vertical String,
    plan_tier Enum8('FREE' = 0, 'PLUS' = 1, 'BUSINESS_PRO' = 2, 'ENTERPRISE' = 3),
    member_quota UInt32,
    storage_limit_bytes UInt64,
    force_live_capture UInt8,
    block_mock_gps UInt8,
    created_at DateTime64(3, 'UTC'),
    updated_at DateTime64(3, 'UTC')
) ENGINE = ReplacingMergeTree(updated_at)
ORDER BY (team_id);

CREATE TABLE IF NOT EXISTS nextai_olap_warehouse.dim_watermarks (
    watermark_id String,
    base_id String,
    watermark_name String,
    category_id String,
    category_name String,
    cv_model_id Int32,
    is_pgc UInt8,
    vip_required UInt8,
    anti_fake_enabled UInt8,
    updated_at DateTime64(3, 'UTC')
) ENGINE = ReplacingMergeTree(updated_at)
ORDER BY (watermark_id, base_id);

CREATE TABLE IF NOT EXISTS nextai_olap_warehouse.dim_geofences (
    geofence_id String,
    team_id String,
    project_id String,
    geofence_name String,
    center_lat Float64,
    center_lng Float64,
    radius_meters UInt32,
    require_live_capture UInt8,
    updated_at DateTime64(3, 'UTC')
) ENGINE = ReplacingMergeTree(updated_at)
ORDER BY (team_id, geofence_id);

-- ----------------------------------------------------------------------------
-- 2. FACT TABLE: PHOTO CAPTURES & FIELD TELEMETRY
-- Partitioned by Month, Ordered by (team_id, project_id, capture_time)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nextai_olap_warehouse.fact_photo_captures (
    capture_id UUID,
    photo_code String,
    anti_fake_code String,
    user_id String,
    device_id String,
    team_id String,
    project_id String,
    watermark_id String,
    watermark_base_id String,
    cv_model_id Int32,
    cv_detected_count UInt32,
    capture_time DateTime64(3, 'UTC'),
    server_ntp_time DateTime64(3, 'UTC'),
    ntp_delta_ms Int64,
    lat Float64,
    lng Float64,
    altitude_m Float32,
    azimuth_deg Float32,
    geofence_id String,
    geofence_distance_m Float32,
    inside_geofence UInt8,
    weather_temp_c Float32,
    weather_aqi UInt16,
    jpeg_packer_reversible UInt8,
    file_size_bytes UInt64,
    upload_latency_ms UInt32,
    country_code FixedString(2)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(capture_time)
ORDER BY (team_id, project_id, capture_time);

-- ----------------------------------------------------------------------------
-- 3. FACT TABLE: ANTI-FAKE CRYPTOGRAPHIC VERIFICATIONS & TAMPER AUDITS
-- Tracks Mock GPS, System Clock Manipulation, Developer Mode & Aegis Nonce
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nextai_olap_warehouse.fact_anti_fake_verifications (
    verification_id UUID,
    photo_code String,
    anti_fake_code String,
    verify_time DateTime64(3, 'UTC'),
    verifier_channel Enum8('APP_SCANNER' = 1, 'WEB_H5_TRUTH_PORTAL' = 2, 'API_BATCH_VERIFY' = 3, 'CMS_AUDITOR' = 4),
    user_id String,
    device_id String,
    is_mock_gps_detected UInt8,
    is_clock_tampered UInt8,
    ntp_drift_ms Int64,
    is_developer_mode_on UInt8,
    aegis_hardware_nonce String,
    aegis_signature_valid UInt8,
    ocr_text_match_score Float32,
    jpeg_tail_integrity_valid UInt8,
    verdict Enum8('AUTHENTIC' = 1, 'SUSPICIOUS_CLOCK_DRIFT' = 2, 'SPOOFED_GPS' = 3, 'TAMPERED_PIXELS' = 4)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(verify_time)
ORDER BY (verify_time, photo_code, verdict);

-- ----------------------------------------------------------------------------
-- 4. FACT TABLE: EMBODIED AI ROBOTICS (.MCAP) CROWDSOURCING SUBMISSIONS
-- Tracks 21-Hand-Landmark Ratio, 4-IMU Sensor Streams & Multi-Currency Payouts
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nextai_olap_warehouse.fact_embodied_mcap_submissions (
    submission_id UUID,
    task_id UInt64,
    task_title String,
    user_id String,
    device_model String,
    country_code FixedString(2),
    submitted_at DateTime64(3, 'UTC'),
    mcap_s3_url String,
    duration_ms UInt32,
    video_fps Float32,
    hand_landmark_21_ratio Float32,
    bimanual_visibility_score Float32,
    imu_accel_hz Float32,
    imu_gyro_hz Float32,
    imu_gravity_hz Float32,
    imu_rotvec_hz Float32,
    audio_vad_clean_ratio Float32,
    qa_status Enum8('PENDING' = 0, 'APPROVED' = 1, 'REJECTED_LOW_HAND_RATIO' = 2, 'REJECTED_IMU_DROP' = 3),
    reward_amount_fen UInt64,
    currency FixedString(3),
    payout_channel Enum8('WALLET' = 0, 'PIX_BRAZIL' = 1, 'SPEI_MEXICO' = 2, 'NEXTAI_PLUS_VIP_DAYS' = 3)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(submitted_at)
ORDER BY (task_id, country_code, submitted_at);

-- ----------------------------------------------------------------------------
-- 5. FACT TABLE: FIRST-PARTY ADX IMPRESSIONS & GOOGLE ADMOB TEST TELEMETRY
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nextai_olap_warehouse.fact_ad_impressions_and_revenue (
    impression_id UUID,
    event_time DateTime64(3, 'UTC'),
    user_id String,
    device_id String,
    country_code FixedString(2),
    ad_format Enum8('BANNER' = 1, 'INTERSTITIAL' = 2, 'REWARDED_FREE_PASS' = 3, 'NATIVE' = 4, 'APP_OPEN' = 5),
    ad_unit_id String,
    placement_scene String,
    is_test_ad UInt8 DEFAULT 1,
    ecpm_usd Decimal(10, 4),
    revenue_usd Decimal(10, 6),
    reward_free_pass_minutes UInt16
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_time)
ORDER BY (event_time, ad_format, country_code);

-- ----------------------------------------------------------------------------
-- 6. FACT TABLE: SUBSCRIPTION CONVERSIONS & PAYWALL EXIT SURVEY FUNNEL
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nextai_olap_warehouse.fact_subscription_conversions (
    conversion_id UUID,
    event_time DateTime64(3, 'UTC'),
    user_id String,
    team_id String,
    product_line Enum8('NEXTAI_PLUS_INDIVIDUAL' = 1, 'TEAMSPACE_BUSINESS_PLAN' = 2),
    sku_id String,
    payment_gateway Enum8('GOOGLE_PLAY_BILLING' = 1, 'STRIPE_WEB_CHECKOUT' = 2, 'COUPON_TRIAL' = 3),
    funnel_stage Enum8('PAYWALL_VIEW' = 1, 'ORDER_CREATED' = 2, 'RECEIPT_VERIFIED' = 3, 'PAYWALL_EXIT_SURVEY' = 4),
    exit_survey_reason String,
    gross_amount_usd Decimal(10, 2),
    country_code FixedString(2)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_time)
ORDER BY (product_line, funnel_stage, event_time);

-- ----------------------------------------------------------------------------
-- 7. MATERIALIZED VIEWS FOR REAL-TIME B2B ATTENDANCE & ESG REPORTING
-- ----------------------------------------------------------------------------

CREATE MATERIALIZED VIEW IF NOT EXISTS nextai_olap_warehouse.mv_teamspace_attendance_realtime
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(capture_day)
ORDER BY (team_id, project_id, capture_day)
AS SELECT
    team_id,
    project_id,
    toStartOfDay(capture_time) AS capture_day,
    count() AS total_captures,
    countIf(inside_geofence = 1) AS verified_geofence_captures,
    countIf(abs(ntp_delta_ms) < 2000) AS ntp_compliant_captures,
    sum(cv_detected_count) AS total_ai_counted_objects,
    sum(file_size_bytes) AS total_storage_bytes_added
FROM nextai_olap_warehouse.fact_photo_captures
GROUP BY team_id, project_id, capture_day;

CREATE MATERIALIZED VIEW IF NOT EXISTS nextai_olap_warehouse.mv_esg_construction_compliance
ENGINE = AggregatingMergeTree()
ORDER BY (team_id, watermark_base_id)
AS SELECT
    team_id,
    watermark_base_id,
    countState() AS total_inspections,
    avgState(geofence_distance_m) AS avg_geofence_deviation_m,
    maxState(capture_time) AS last_inspection_time
FROM nextai_olap_warehouse.fact_photo_captures
GROUP BY team_id, watermark_base_id;
