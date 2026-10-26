package com.nextai.camera

/**
 * Official Google AdMob Test Configuration (`Task.txt` #4).
 * Replaces all legacy commercial mediation keys with official Google AdMob test identifiers.
 */
object NextAiAdConfig {
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val APP_OPEN_TEST_ID = "ca-app-pub-3940256099942544/9257395921"
    const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_VIDEO_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    const val NATIVE_ADVANCED_TEST_ID = "ca-app-pub-3940256099942544/2247696110"

    const val AD_FREE_PASS_REWARD_HOURS = 24

    data class AdUnitPlacement(
        val name: String,
        val format: String,
        val unitId: String
    )

    val allTestPlacements: List<AdUnitPlacement> = listOf(
        AdUnitPlacement("App Open Cold/Warm Start", "APP_OPEN", APP_OPEN_TEST_ID),
        AdUnitPlacement("Gallery & Settings Banner", "BANNER", BANNER_TEST_ID),
        AdUnitPlacement("Batch Export Interstitial", "INTERSTITIAL", INTERSTITIAL_TEST_ID),
        AdUnitPlacement("Unlock 24h AdFreePass / Lossless Unpack", "REWARDED_VIDEO", REWARDED_VIDEO_TEST_ID),
        AdUnitPlacement("Watermark Store Native Card", "NATIVE_ADVANCED", NATIVE_ADVANCED_TEST_ID)
    )
}
