package com.nextai.camera.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Offline Anti-Fake Code Pre-Allocation, Lossless Watermark Removal Authorization,
 * Favorite POI Sync, and Cloud S3/OSS STS Token Service (`NextAiDualModeApiService`).
 */
interface NextAiDualModeApiService {
    @POST("antiFakeCode/generate")
    suspend fun generateOfflineAntiFakeCodes(@Body request: AntiFakeCodeBatchRequest): NextAiBaseResponse<AntiFakeCodeBatchResponse>

    @POST("workgroup/photo/watermark/remove/check")
    suspend fun checkWatermarkRemovePermission(@Body request: WatermarkRemoveCheckRequest): NextAiBaseResponse<WatermarkRemoveCheckResponse>

    @POST("favorite/address/list")
    suspend fun listFavoriteAddresses(@Body body: Map<String, String> = emptyMap()): NextAiBaseResponse<List<PlaceDto>>

    @POST("workgroup/v4/s3/token")
    suspend fun fetchS3TemporaryCredentials(@Body body: Map<String, String> = emptyMap()): NextAiBaseResponse<Map<String, String>>
}
