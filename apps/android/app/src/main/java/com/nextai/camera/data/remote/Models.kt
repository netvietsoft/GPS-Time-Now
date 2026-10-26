package com.nextai.camera.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Standard envelope returned by all NextAI Internal Backend endpoints (`/nextai/v1/*`).
 */
data class NextAiBaseResponse<T>(
    @SerializedName("code") val code: Int = 200,
    @SerializedName("msg") val msg: String = "success",
    @SerializedName("status") val status: Int = 0,
    @SerializedName("data") val data: T? = null
)

// Core & NTP / Weather / POI Models
data class RealTimeSyncRequest(
    @SerializedName("clientTime") val clientTime: Long
)

data class RealTimeInfo(
    @SerializedName("serverTimeMs") val serverTimeMs: Long,
    @SerializedName("ntpStratum") val ntpStratum: Int = 1,
    @SerializedName("driftMs") val driftMs: Long = 12,
    @SerializedName("verified") val verified: Boolean = true
)

data class WeatherRequest(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("lang") val lang: String = "en",
    @SerializedName("unit") val unit: String = "C"
)

data class WeatherInfo(
    @SerializedName("tempC") val tempC: Double,
    @SerializedName("condition") val condition: String,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("windSpeedKmh") val windSpeedKmh: Double,
    @SerializedName("altitudeM") val altitudeM: Double
)

data class PlaceSearchRequest(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("keyword") val keyword: String? = null
)

data class PlaceDto(
    @SerializedName("placeId") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("distanceMeters") val distanceMeters: Double
)

// Anti-Fake & VerifyAuth Models
data class TruthfulBatchCreateRequest(
    @SerializedName("photos") val photos: List<TruthfulPhotoRecord>
)

data class TruthfulPhotoRecord(
    @SerializedName("photoCode") val photoCode: String,
    @SerializedName("antiFakeCode") val antiFakeCode: String,
    @SerializedName("aegisNonceHex") val aegisNonceHex: String,
    @SerializedName("ntpTimestampMs") val ntpTimestampMs: Long,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("isMockGps") val isMockGps: Boolean,
    @SerializedName("sha256Hex") val sha256Hex: String
)

data class VerifyAuthDetailRequest(
    @SerializedName("codes") val codes: List<String>,
    @SerializedName("ocrText") val ocrText: String
)

data class VerifyAuthAuditResult(
    @SerializedName("photoCode") val photoCode: String,
    @SerializedName("authentic") val authentic: Boolean,
    @SerializedName("antiFakeCodeValid") val antiFakeCodeValid: Boolean,
    @SerializedName("ntpTimeValid") val ntpTimeValid: Boolean,
    @SerializedName("gpsMockClean") val gpsMockClean: Boolean,
    @SerializedName("aegisHardwareNonceValid") val aegisHardwareNonceValid: Boolean,
    @SerializedName("serverRecordTime") val serverRecordTime: String,
    @SerializedName("serverRecordAddress") val serverRecordAddress: String,
    @SerializedName("confidenceScore") val confidenceScore: Double
)

// Logo Cutout & Cloud AI Models
data class LogoCutoutRequest(
    @SerializedName("imageUrl") val imageUrl: String
)

data class LogoCutoutResponse(
    @SerializedName("cutoutPngUrl") val cutoutPngUrl: String,
    @SerializedName("foregroundAlpha") val foregroundAlpha: Float = 1.0f
)

data class AiPromptGenerateRequest(
    @SerializedName("industry") val industry: String,
    @SerializedName("context") val context: String,
    @SerializedName("keywords") val keywords: List<String>
)

data class AiPromptGenerateResponse(
    @SerializedName("suggestedTitle") val suggestedTitle: String,
    @SerializedName("suggestedNote") val suggestedNote: String,
    @SerializedName("recommendedWatermarkId") val recommendedWatermarkId: String
)

// Teamspace SaaS Models
data class TeamspaceSummaryDto(
    @SerializedName("teamId") val teamId: String,
    @SerializedName("teamName") val teamName: String,
    @SerializedName("memberCount") val memberCount: Int,
    @SerializedName("photoCount") val photoCount: Int,
    @SerializedName("storageUsedGb") val storageUsedGb: Double,
    @SerializedName("storageLimitGb") val storageLimitGb: Double,
    @SerializedName("planName") val planName: String
)

data class GeofencePunchRequest(
    @SerializedName("teamId") val teamId: String,
    @SerializedName("projectId") val projectId: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("shiftType") val shiftType: String,
    @SerializedName("photoCode") val photoCode: String
)

data class GeofencePunchResult(
    @SerializedName("punchId") val punchId: String,
    @SerializedName("insideGeofence") val insideGeofence: Boolean,
    @SerializedName("distanceFromCenterMeters") val distanceFromCenterMeters: Double,
    @SerializedName("verifiedServerTime") val verifiedServerTime: String
)

// Embodied AI ("Earn") Models
data class EarnBalanceDto(
    @SerializedName("balanceFen") val balanceFen: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("vipDaysEarned") val vipDaysEarned: Int,
    @SerializedName("pixAccountBound") val pixAccountBound: Boolean,
    @SerializedName("speiAccountBound") val speiAccountBound: Boolean
)

data class EarnTaskDto(
    @SerializedName("taskId") val taskId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("rewardAmount") val rewardAmount: String,
    @SerializedName("minDurationSec") val minDurationSec: Int,
    @SerializedName("requiredHandRatio") val requiredHandRatio: Float,
    @SerializedName("sampleVideoUrl") val sampleVideoUrl: String
)

data class EarnSubmitMcapRequest(
    @SerializedName("taskId") val taskId: Long,
    @SerializedName("mcapS3Url") val mcapS3Url: String,
    @SerializedName("durationMs") val durationMs: Long,
    @SerializedName("handRatio") val handRatio: Float,
    @SerializedName("imuSampleCount") val imuSampleCount: Int,
    @SerializedName("deviceModel") val deviceModel: String
)

data class EarnWithdrawRequest(
    @SerializedName("amountFen") val amountFen: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("method") val method: String,
    @SerializedName("accountIdentifier") val accountIdentifier: String
)

// DualMode & STS Credentials Models
data class AntiFakeCodeBatchRequest(
    @SerializedName("count") val count: Int = 20,
    @SerializedName("deviceId") val deviceId: String
)

data class AntiFakeCodeBatchResponse(
    @SerializedName("codes") val codes: List<String>,
    @SerializedName("expiresAt") val expiresAt: Long
)

data class WatermarkRemoveCheckRequest(
    @SerializedName("photoCode") val photoCode: String,
    @SerializedName("hasVipOrAdPass") val hasVipOrAdPass: Boolean
)

data class WatermarkRemoveCheckResponse(
    @SerializedName("allowed") val allowed: Boolean,
    @SerializedName("reason") val reason: String
)
