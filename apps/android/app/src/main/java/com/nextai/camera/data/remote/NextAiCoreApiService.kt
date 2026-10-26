package com.nextai.camera.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Core Camera, NTP Real-Time, Weather, POI, Watermark Store, AI Logo Cutout,
 * VerifyAuth Truth Center, and VIP Billing Service (`NextAiCoreApiService`).
 */
interface NextAiCoreApiService {
    @POST("realtime")
    suspend fun syncAtomicRealTime(@Body request: RealTimeSyncRequest): NextAiBaseResponse<RealTimeInfo>

    @POST("weather/current/detail")
    suspend fun getCurrentWeather(@Body request: WeatherRequest): NextAiBaseResponse<WeatherInfo>

    @POST("v2/place/list/group")
    suspend fun getNearbyPlaces(@Body request: PlaceSearchRequest): NextAiBaseResponse<List<PlaceDto>>

    @POST("v2/place/search/group")
    suspend fun searchPlaces(@Body request: PlaceSearchRequest): NextAiBaseResponse<List<PlaceDto>>

    @POST("logo/cutout")
    suspend fun cutoutBrandLogo(@Body request: LogoCutoutRequest): NextAiBaseResponse<LogoCutoutResponse>

    @POST("ai/generatePrompt")
    suspend fun generateFieldPrompt(@Body request: AiPromptGenerateRequest): NextAiBaseResponse<AiPromptGenerateResponse>

    @POST("truthful/create/batch")
    suspend fun registerTruthfulBatch(@Body request: TruthfulBatchCreateRequest): NextAiBaseResponse<Map<String, Any>>

    @POST("truthful/Query/batch/detail")
    suspend fun verifyPhotoAuthenticity(@Body request: VerifyAuthDetailRequest): NextAiBaseResponse<VerifyAuthAuditResult>

    @GET("network/benchmark")
    suspend fun runNetworkBenchmark(): NextAiBaseResponse<Map<String, Any>>
}
