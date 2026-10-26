package com.nextai.camera.data.remote

/**
 * Unified facade providing access to all NextAI internal backend Retrofit services:
 * - [NextAiCoreApiService]
 * - [NextAiClusterApiService]
 * - [NextAiEarnApiService]
 * - [NextAiDualModeApiService]
 */
object NextAiApiServices {
    val core: NextAiCoreApiService
        get() = NextAiNetworkConfig.coreApi

    val cluster: NextAiClusterApiService
        get() = NextAiNetworkConfig.clusterApi

    val earn: NextAiEarnApiService
        get() = NextAiNetworkConfig.earnApi

    val dualMode: NextAiDualModeApiService
        get() = NextAiNetworkConfig.dualModeApi
}
