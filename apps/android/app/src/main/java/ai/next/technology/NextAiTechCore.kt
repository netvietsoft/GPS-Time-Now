package ai.next.technology

/**
 * NextAI Technology Core Metadata & Industrial Telemetry Constants.
 * Provides hardware-verified time synchronization, anti-tamper cryptography constants,
 * and edge AI vision configuration for NextAI Camera.
 */
object NextAiTechCore {
    const val VENDOR_NAME = "NextAI Technology & Solution"
    const val ENGINE_VERSION = "10.0.281-NextAI"
    const val TECHNOLOGY_NAMESPACE = "ai.next.technology"
    const val SOLUTION_NAMESPACE = "com.nextai.solution"
    const val GLOBAL_NAMESPACE = "com.nextai.global"

    const val DEFAULT_NTP_TOLERANCE_MS = 1500L
    const val AEGIS_NONCE_SIZE_BYTES = 32
    const val MCAP_IMU_SAMPLE_RATE_HZ = 200
    const val MCAP_AUDIO_SAMPLE_RATE_HZ = 48000
    const val HAND_LANDMARK_KEYPOINTS = 21
}
