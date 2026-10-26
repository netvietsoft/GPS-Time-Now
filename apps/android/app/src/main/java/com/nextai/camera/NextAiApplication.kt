package com.nextai.camera

import android.app.Application
import com.nextai.camera.data.local.NextAiDatabase
import com.nextai.camera.data.remote.NextAiNetworkConfig

/**
 * Main Application entrypoint for NextAI Camera (`com.nextai.camera`).
 * Replaces legacy wrapper and initializes the 42-table Room database,
 * internal backend Retrofit client, and hardware anti-spoofing bridge.
 */
class NextAiApplication : Application() {

    val database: NextAiDatabase by lazy {
        NextAiDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        NextAiNetworkConfig.initialize(isEmulator = isRunningOnEmulator())
    }

    private fun isRunningOnEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT ?: ""
        val model = android.os.Build.MODEL ?: ""
        return fingerprint.startsWith("generic") ||
            fingerprint.lowercase().contains("emulator") ||
            model.contains("Emulator") ||
            model.contains("Android SDK built for")
    }

    companion object {
        @Volatile
        lateinit var instance: NextAiApplication
            private set
    }
}
