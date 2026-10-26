package com.nextai.global

/**
 * NextAI Global Region, Localization & Multi-Currency Settlement Registry
 * for Embodied AI ("Earn") and Global Watermark Presets.
 */
object NextAiGlobalRegistry {
    const val APP_DISPLAY_NAME = "NextAI Camera"
    const val APP_FULL_NAME = "NextAI: Time & GPS Camera"
    const val GLOBAL_PRESET_REALTIME_ID = "10011"
    const val GLOBAL_PRESET_STEEL_COUNT_ID = "57"
    const val GLOBAL_PRESET_ATTENDANCE_ID = "34"
    const val GLOBAL_PRESET_VLPR_ID = "52"
    const val GLOBAL_PRESET_FACE_COUNT_ID = "54"

    val SUPPORTED_SETTLEMENT_METHODS = listOf(
        "BRL_PIX" to "Brazil Pix Instant Payout (BRL)",
        "MXN_SPEI" to "Mexico SPEI Bank Transfer (MXN)",
        "VIP_EXCHANGE" to "NextAI Plus VIP Days Exchange"
    )
}
