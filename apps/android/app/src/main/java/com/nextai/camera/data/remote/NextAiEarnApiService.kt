package com.nextai.camera.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Embodied AI First-Person Robotics Crowdsourcing ("Earn") Service (`NextAiEarnApiService`).
 * Handles task hall discovery, `.mcap` sensor bundle submission, and BRL Pix / MXN SPEI / VIP payouts.
 */
interface NextAiEarnApiService {
    @POST("app/reward/video/balance")
    suspend fun getWalletBalance(@Body body: Map<String, String> = emptyMap()): NextAiBaseResponse<EarnBalanceDto>

    @POST("app/reward/video/task/list")
    suspend fun getRoboticsTaskList(@Body body: Map<String, Any>): NextAiBaseResponse<List<EarnTaskDto>>

    @POST("app/reward/video/submit")
    suspend fun submitMcapRecording(@Body request: EarnSubmitMcapRequest): NextAiBaseResponse<Map<String, Any>>

    @POST("app/reward/video/withdrawal/apply")
    suspend fun applyWithdrawal(@Body request: EarnWithdrawRequest): NextAiBaseResponse<Map<String, Any>>
}
