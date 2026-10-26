package com.nextai.camera.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Internal Backend API Connection Configuration (`Task.txt` #5).
 * Connects to the internal NextAI Node.js + TypeScript Backend on port 4580:
 * - Emulator: `http://10.0.2.2:4580/nextai/v1/`
 * - Physical LAN Device: `http://192.168.1.100:4580/nextai/v1/`
 * - Localhost / ADB Reverse: `http://localhost:4580/nextai/v1/`
 */
object NextAiNetworkConfig {
    const val EMULATOR_BASE_URL = "http://10.0.2.2:4580/nextai/v1/"
    const val LAN_DEVICE_BASE_URL = "http://192.168.1.100:4580/nextai/v1/"
    const val LOCALHOST_BASE_URL = "http://localhost:4580/nextai/v1/"

    enum class EndpointProfile(val label: String, val baseUrl: String) {
        EMULATOR("Android Emulator (10.0.2.2:4580)", EMULATOR_BASE_URL),
        LAN_DEVICE("LAN Physical Device (192.168.1.100:4580)", LAN_DEVICE_BASE_URL),
        LOCALHOST("Localhost / ADB Reverse (localhost:4580)", LOCALHOST_BASE_URL)
    }

    @Volatile
    var activeProfile: EndpointProfile = EndpointProfile.EMULATOR
        private set

    @Volatile
    var jwtAccessToken: String = "nextai-dev-jwt-token-v1"

    fun initialize(isEmulator: Boolean) {
        activeProfile = if (isEmulator) EndpointProfile.EMULATOR else EndpointProfile.LAN_DEVICE
    }

    fun switchEndpointProfile(profile: EndpointProfile) {
        activeProfile = profile
    }

    private val authHeaderInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("Authorization", "Bearer $jwtAccessToken")
            .header("X-NextAI-Client", "NextAI-Camera-Android/10.0.281")
            .header("X-NextAI-Namespace", "com.nextai.camera")
            .build()
        chain.proceed(request)
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
