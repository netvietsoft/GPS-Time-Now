package com.nextai.camera.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * NextAI Network & Gateway Configuration (AUD-006, PROD-08 & Play Policy Hardened).
 * - Production Default: `https://api.nextai.global/nextai/v1/` (HTTPS enforced, no cleartext)
 * - Secondary Gateway: `https://gateway.nextai.technology/nextai/v1/`
 * - Edge Gateway: `https://edge.nextai.solution/nextai/v1/`
 * No hardcoded LAN IPs, cleartext HTTP endpoints, or static development JWT tokens are shipped in source.
 */
object NextAiNetworkConfig {
    const val PRODUCTION_BASE_URL = "https://api.nextai.global/nextai/v1/"
    const val LOCALHOST_BASE_URL = "https://gateway.nextai.technology/nextai/v1/"
    const val EMULATOR_BASE_URL = "https://edge.nextai.solution/nextai/v1/"

    enum class EndpointProfile(val label: String, val baseUrl: String) {
        PRODUCTION("Production HTTPS Gateway (api.nextai.global)", PRODUCTION_BASE_URL),
        LOCALHOST("Secondary HTTPS Gateway (gateway.nextai.technology)", LOCALHOST_BASE_URL),
        EMULATOR("Edge HTTPS Gateway (edge.nextai.solution)", EMULATOR_BASE_URL)
    }

    @Volatile
    var activeProfile: EndpointProfile = EndpointProfile.PRODUCTION
        private set

    @Volatile
    var jwtAccessToken: String? = null
        private set

    fun initialize(isEmulator: Boolean, useLocalAdbReverse: Boolean = false) {
        activeProfile = when {
            isEmulator -> EndpointProfile.EMULATOR
            useLocalAdbReverse -> EndpointProfile.LOCALHOST
            else -> EndpointProfile.PRODUCTION
        }
    }

    fun switchEndpointProfile(profile: EndpointProfile) {
        activeProfile = profile
    }

    fun updateAccessToken(token: String?) {
        jwtAccessToken = token?.takeIf { it.isNotBlank() }
    }

    private val authHeaderInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("X-NextAI-Client", "NextAI-Camera-Android/10.0.281")
            .header("X-NextAI-Namespace", "com.nextai.camera")
        val currentToken = jwtAccessToken
        if (!currentToken.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $currentToken")
        }
        chain.proceed(builder.build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun buildRetrofit(baseUrl: String = activeProfile.baseUrl): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val coreApi: NextAiCoreApiService
        get() = buildRetrofit().create(NextAiCoreApiService::class.java)

    val clusterApi: NextAiClusterApiService
        get() = buildRetrofit().create(NextAiClusterApiService::class.java)

    val earnApi: NextAiEarnApiService
        get() = buildRetrofit().create(NextAiEarnApiService::class.java)

    val dualModeApi: NextAiDualModeApiService
        get() = buildRetrofit().create(NextAiDualModeApiService::class.java)
}
