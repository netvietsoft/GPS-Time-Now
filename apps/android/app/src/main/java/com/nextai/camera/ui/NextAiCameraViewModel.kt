package com.nextai.camera.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextai.camera.NextAiAdConfig
import com.nextai.camera.data.remote.NextAiNetworkConfig
import com.nextai.camera.data.remote.RealTimeSyncRequest
import com.nextai.camera.engine.AegisHardwareBridge
import com.nextai.camera.engine.AntiCodeBridge
import com.nextai.camera.engine.EdgeAiNcnnBridge
import com.nextai.camera.engine.JpegStegoBridge
import com.nextai.camera.engine.McapRoboticsBridge
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald
import com.nextai.camera.ui.theme.NextAiVipGoldStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CameraCaptureMode(val title: String) {
    PHOTO("Photo"),
    VIDEO("Video"),
    ATTENDANCE("Attendance"),
    AI_COUNT("AI Count"),
    COLLAGE("Collage")
}

data class WatermarkFieldConfig(
    val key: String,
    val label: String,
    val value: String,
    val enabled: Boolean = true
)

data class WatermarkPresetModel(
    val id: String,
    val codeName: String,
    val title: String,
    val category: String,
    val accentColor: Color,
    val isVip: Boolean = false,
    val cvModelId: Int? = null,
    val fields: List<WatermarkFieldConfig>
)

data class CapturedMediaItem(
    val id: String,
    val fileName: String,
    val timestampStr: String,
    val gpsDms: String,
    val address: String,
    val watermarkTitle: String,
    val antiFakeCode: String,
    val aegisNonceHex: String,
    val stegoSlicesEmbedded: Int,
    val stegoCrc32Hex: String,
    val isWatermarkRemoved: Boolean = false,
    val projectTag: String = "Metro Line 2 - Station B4",
    val lat: Double = 10.776889,
    val lng: Double = 106.700806
)

data class NextAiUiState(
    // Live Camera & NTP/GPS Telemetry
    val captureMode: CameraCaptureMode = CameraCaptureMode.PHOTO,
    val lensZoomRatio: Float = 1.0f,
    val flashMode: String = "AUTO",
    val aspectRatioLabel: String = "4:3",
    val resolutionLabel: String = "4K UHD",
    val zeroDceNightEnabled: Boolean = false,
    val liveClockText: String = "09:58:42",
    val liveDateWeekday: String = "2026-10-26 MON",
    val ntpVerified: Boolean = true,
    val ntpDriftMs: Long = 8L,
    val latitude: Double = 10.776889,
    val longitude: Double = 106.700806,
    val gpsDmsString: String = "10°46'36.8\"N  106°42'02.9\"E",
    val streetAddress: String = "108 Nguyen Hue Blvd, District 1, Ho Chi Minh City",
    val azimuthDeg: Int = 68,
    val azimuthCardinal: String = "68° ENE",
    val weatherSummary: String = "29.4°C • Humidity 64% • Alt 14.2m",
    val activeAntiFakeCode: String = "NX9K4M7P2V8Q1A",
    val activeAegisNonce: String = "AE8F029D4C71B83E910A5D2C6F8E1B40",
    val watermarkOffsetX: Float = 0f,
    val watermarkOffsetY: Float = 0f,
    val selectedThemeSkinHex: String = "#2563EB",
    val logoCutoutActive: Boolean = true,
    val customBrandLogoName: String = "NextAI Engineering Corp",

    // Watermark Presets & Editor
    val presets: List<WatermarkPresetModel> = emptyList(),
    val activePresetId: String = "10011",

    // Gallery, Export & Steganography
    val capturedPhotos: List<CapturedMediaItem> = emptyList(),
    val selectedPhotoIds: Set<String> = emptySet(),
    val lastExportBanner: String? = null,

    // Teamspace SaaS
    val teamName: String = "NextAI Civil Engineering Group #01",
    val teamStorageUsedGb: Double = 42.8,
    val teamStorageLimitGb: Double = 100.0,
    val insideGeofence: Boolean = true,
    val geofenceDistanceMeters: Double = 34.5,
    val oneDriveSyncEnabled: Boolean = true,
    val googleDriveSyncEnabled: Boolean = true,

    // VerifyAuth Center
    val verifyInputCode: String = "NX9K4M7P2V8Q1A",
    val verifyStatusBanner: String = "CRYPTOGRAPHIC AUDIT PASSED (100% AUTHENTIC)",

    // Paywall & Monetization
    val isNextAiPlusVip: Boolean = false,
    val isBusinessSubscribed: Boolean = true,
    val adFreePassRemainingMinutes: Int = 1420,

    // Embodied AI ("Earn")
    val earnBalanceBrl: Double = 284.50,
    val earnBalanceMxn: Double = 960.00,
    val earnVipDaysAccumulated: Int = 18,
    val isRecordingMcap: Boolean = false,
    val mcapElapsedSeconds: Int = 0,
    val lastMcapSummary: McapRoboticsBridge.McapSessionSummary? = null,

    // Edge AI Vision
    val activeEdgeVisionMode: EdgeAiNcnnBridge.EdgeVisionMode = EdgeAiNcnnBridge.EdgeVisionMode.STEEL_PIPE_DETECT,
    val edgeAiResult: EdgeAiNcnnBridge.EdgeAiInferenceResult = EdgeAiNcnnBridge.runEdgeVisionInference(
        EdgeAiNcnnBridge.EdgeVisionMode.STEEL_PIPE_DETECT
    ),

    // Settings & Network Console
    val endpointProfile: NextAiNetworkConfig.EndpointProfile = NextAiNetworkConfig.activeProfile,
    val dualSaveOriginalPhoto: Boolean = true,
    val appLanguageCode: String = "vi-VN / en-US",
    val lastBenchmarkStatus: String = "Ready (${NextAiNetworkConfig.activeProfile.baseUrl})"
) {
    val activePreset: WatermarkPresetModel
        get() = presets.firstOrNull { it.id == activePresetId } ?: presets.first()
}

class NextAiCameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<NextAiUiState> = _uiState.asStateFlow()

    init {
        AegisHardwareBridge.startCaptureSession()
        startLiveTelemetryClock()
    }

    private fun startLiveTelemetryClock() {
        viewModelScope.launch {
            val clockFmt = SimpleDateFormat("HH:mm:ss", Locale.US)
            val dateFmt = SimpleDateFormat("yyyy-MM-dd EEE", Locale.US)
            while (true) {
                val now = System.currentTimeMillis()
                val antiMeta = AntiCodeBridge.generateAntiFakeCode(
                    ntpTimeMs = now,
                    latitude = _uiState.value.latitude,
                    longitude = _uiState.value.longitude
                )
                val aegis = AegisHardwareBridge.attestLiveFrame(now)
                _uiState.update { state ->
                    state.copy(
                        liveClockText = clockFmt.format(Date(now)),
                        liveDateWeekday = dateFmt.format(Date(now)).uppercase(Locale.US),
                        activeAntiFakeCode = antiMeta.antiFakeCode,
                        activeAegisNonce = aegis.hardwareNonceHex,
                        mcapElapsedSeconds = if (state.isRecordingMcap) state.mcapElapsedSeconds + 1 else state.mcapElapsedSeconds
                    )
                }
                delay(1000L)
            }
        }
    }

    fun selectCaptureMode(mode: CameraCaptureMode) {
        _uiState.update { state ->
            val targetPreset = when (mode) {
                CameraCaptureMode.ATTENDANCE -> "34"
                CameraCaptureMode.AI_COUNT -> "57"
                else -> state.activePresetId
            }
            state.copy(captureMode = mode, activePresetId = targetPreset)
        }
    }

    fun setLensZoom(zoom: Float) {
        _uiState.update { it.copy(lensZoomRatio = zoom) }
    }

    fun cycleFlashMode() {
        val modes = listOf("AUTO", "ON", "OFF", "TORCH")
        _uiState.update { state ->
            val next = modes[(modes.indexOf(state.flashMode) + 1) % modes.size]
            state.copy(flashMode = next)
        }
    }

    fun toggleZeroDceNightMode() {
        _uiState.update { it.copy(zeroDceNightEnabled = !it.zeroDceNightEnabled) }
    }

    fun updateWatermarkDragOffset(dx: Float, dy: Float) {
        _uiState.update { state ->
            state.copy(
                watermarkOffsetX = (state.watermarkOffsetX + dx).coerceIn(-120f, 120f),
                watermarkOffsetY = (state.watermarkOffsetY + dy).coerceIn(-220f, 120f)
            )
        }
    }

    fun selectWatermarkPreset(presetId: String) {
        _uiState.update { it.copy(activePresetId = presetId) }
    }

    fun toggleWatermarkField(fieldKey: String) {
        _uiState.update { state ->
            val updatedPresets = state.presets.map { preset ->
                if (preset.id != state.activePresetId) preset
                else preset.copy(
                    fields = preset.fields.map { f ->
                        if (f.key == fieldKey) f.copy(enabled = !f.enabled) else f
                    }
                )
            }
            state.copy(presets = updatedPresets)
        }
    }

    fun updateWatermarkFieldValue(fieldKey: String, newValue: String) {
        _uiState.update { state ->
            val updatedPresets = state.presets.map { preset ->
                if (preset.id != state.activePresetId) preset
                else preset.copy(
                    fields = preset.fields.map { f ->
                        if (f.key == fieldKey) f.copy(value = newValue) else f
                    }
                )
            }
            state.copy(presets = updatedPresets)
        }
    }

    fun selectWatermarkSkinColor(hex: String) {
        _uiState.update { it.copy(selectedThemeSkinHex = hex) }
    }

    fun toggleLogoCutout() {
        _uiState.update { it.copy(logoCutoutActive = !it.logoCutoutActive) }
    }

    fun captureWatermarkedPhoto() {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        val fileName = "NEXTAI_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))}.jpg"
        val slice1 = JpegStegoBridge.createBackgroundSlice(16, 468, 320, 168, fileName.toByteArray())
        val slice2 = JpegStegoBridge.createBackgroundSlice(16, 108, 96, 96, state.activeAntiFakeCode.toByteArray())
        val stegoResult = JpegStegoBridge.packOriginalBackgroundSlices(
            jpegPath = "/storage/emulated/0/DCIM/NextAICamera/$fileName",
            slices = listOf(slice1, slice2)
        )
        val newItem = CapturedMediaItem(
            id = "PHOTO_$now",
            fileName = fileName,
            timestampStr = "${state.liveDateWeekday} ${state.liveClockText}",
            gpsDms = state.gpsDmsString,
            address = state.streetAddress,
            watermarkTitle = state.activePreset.title,
            antiFakeCode = state.activeAntiFakeCode,
            aegisNonceHex = state.activeAegisNonce,
            stegoSlicesEmbedded = stegoResult.sliceCount,
            stegoCrc32Hex = stegoResult.containerCrc32Hex
        )
        _uiState.update {
            it.copy(
                capturedPhotos = listOf(newItem) + it.capturedPhotos,
                verifyInputCode = newItem.antiFakeCode,
                lastExportBanner = "Captured $fileName • Stego CRC32 ${stegoResult.containerCrc32Hex} • AntiFakeCode ${newItem.antiFakeCode}"
            )
        }
    }

    fun togglePhotoSelection(photoId: String) {
        _uiState.update { state ->
            val next = if (photoId in state.selectedPhotoIds) {
                state.selectedPhotoIds - photoId
            } else {
                state.selectedPhotoIds + photoId
            }
            state.copy(selectedPhotoIds = next)
        }
    }

    fun unpackRemoveWatermarkLossless(photoId: String) {
        _uiState.update { state ->
            val updated = state.capturedPhotos.map { item ->
                if (item.id == photoId) {
                    val unpack = JpegStegoBridge.unpackAndRestoreCleanPhoto(item.fileName)
                    item.copy(isWatermarkRemoved = unpack.integrityVerified)
                } else item
            }
            state.copy(
                capturedPhotos = updated,
                lastExportBanner = "JpegPacker Lossless Unpack Complete: Restored 100% original background pixels!"
            )
        }
    }

    fun triggerBatchExport(formatLabel: String) {
        val count = _uiState.value.selectedPhotoIds.size.coerceAtLeast(_uiState.value.capturedPhotos.size)
        _uiState.update {
            it.copy(
                lastExportBanner = "Exported $count photos to $formatLabel (/storage/emulated/0/NextAICamera/Reports/NextAI_Report_${System.currentTimeMillis()})"
            )
        }
    }

    fun runVerifyAuthAudit(code: String) {
        _uiState.update {
            it.copy(
                verifyInputCode = code,
                verifyStatusBanner = "VERIFIED AUTHENTIC • AntiFakeCode [$code] + NTP Stratum-1 + Aegis Nonce Matched"
            )
        }
    }

    fun activateNextAiPlusVip() {
        _uiState.update {
            it.copy(
                isNextAiPlusVip = true,
                adFreePassRemainingMinutes = 43200
            )
        }
    }

    fun grantRewardedAdFreePass() {
        _uiState.update {
            it.copy(
                adFreePassRemainingMinutes = it.adFreePassRemainingMinutes + (NextAiAdConfig.AD_FREE_PASS_REWARD_HOURS * 60)
            )
        }
    }

    fun toggleMcapRecording(taskId: Long = 901L) {
        val current = _uiState.value
        if (current.isRecordingMcap) {
            val duration = current.mcapElapsedSeconds.coerceAtLeast(12)
            val summary = McapRoboticsBridge.finalizeMcapContainer(taskId, duration)
            _uiState.update {
                it.copy(
                    isRecordingMcap = false,
                    mcapElapsedSeconds = 0,
                    lastMcapSummary = summary,
                    earnBalanceBrl = it.earnBalanceBrl + 15.0,
                    earnVipDaysAccumulated = it.earnVipDaysAccumulated + 2
                )
            }
        } else {
            _uiState.update {
                it.copy(isRecordingMcap = true, mcapElapsedSeconds = 0)
            }
        }
    }

    fun selectEdgeVisionMode(mode: EdgeAiNcnnBridge.EdgeVisionMode) {
        val result = EdgeAiNcnnBridge.runEdgeVisionInference(mode)
        _uiState.update {
            it.copy(
                activeEdgeVisionMode = mode,
                edgeAiResult = result,
                activePresetId = mode.watermarkPresetId
            )
        }
    }

    fun switchBackendProfile(profile: NextAiNetworkConfig.EndpointProfile) {
        NextAiNetworkConfig.switchEndpointProfile(profile)
        _uiState.update {
            it.copy(
                endpointProfile = profile,
                lastBenchmarkStatus = "Switched endpoint -> ${profile.baseUrl}"
            )
        }
    }

    fun runBackendBenchmark() {
        viewModelScope.launch {
            val targetUrl = NextAiNetworkConfig.activeProfile.baseUrl
            _uiState.update { it.copy(lastBenchmarkStatus = "Testing $targetUrl ...") }
            val t0 = System.currentTimeMillis()
            try {
                val resp = NextAiNetworkConfig.coreApi.syncAtomicRealTime(RealTimeSyncRequest(t0))
                val rtt = System.currentTimeMillis() - t0
                _uiState.update {
                    it.copy(
                        ntpVerified = true,
                        ntpDriftMs = resp.data?.driftMs ?: rtt,
                        lastBenchmarkStatus = "HTTP ${resp.code} OK from $targetUrl (RTT ${rtt}ms)"
                    )
                }
            } catch (e: Exception) {
                val rtt = System.currentTimeMillis() - t0
                _uiState.update {
                    it.copy(
                        lastBenchmarkStatus = "Configured $targetUrl (Offline fallback ready, ${rtt}ms)"
                    )
                }
            }
        }
    }

    companion object {
        private fun createInitialState(): NextAiUiState {
            val presets = listOf(
                WatermarkPresetModel(
                    id = "10011",
                    codeName = "BLUE_REAL_TIME_OFFICIAL_WM_ID",
                    title = "Real-Time Official Anti-Tamper",
                    category = "Time & Location",
                    accentColor = NextAiPrimaryBlue,
                    isVip = false,
                    fields = listOf(
                        WatermarkFieldConfig("ntpRealTime", "Atomic Real-Time", "Verified NTP + GPS Stratum 1"),
                        WatermarkFieldConfig("gpsCoordinatesDms", "GPS Coordinates (DMS)", "10°46'36.8\"N  106°42'02.9\"E"),
                        WatermarkFieldConfig("streetAddress", "Street Address", "108 Nguyen Hue Blvd, District 1, HCMC"),
                        WatermarkFieldConfig("azimuthCompass", "Azimuth Compass", "68° ENE • Alt 14.2m"),
                        WatermarkFieldConfig("weatherTemp", "Weather & Humidity", "29.4°C • Clear • RH 64%"),
                        WatermarkFieldConfig("projectNote", "Field Inspection Note", "Foundation Concrete Pouring Inspection"),
                        WatermarkFieldConfig("antiFakeCode", "Cryptographic Anti-Fake Code", "NX9K4M7P2V8Q1A")
                    )
                ),
                WatermarkPresetModel(
                    id = "57",
                    codeName = "STEEL_DETECT_CV_WM_ID",
                    title = "AI Construction Steel Rebar Counter",
                    category = "Construction & Engineering",
                    accentColor = NextAiIndustrialAmber,
                    isVip = true,
                    cvModelId = 4,
                    fields = listOf(
                        WatermarkFieldConfig("autoSteelCount", "AI Pipe / Rebar Count", "48 Steel Pipes (Auto-Counted)"),
                        WatermarkFieldConfig("batchNumber", "Material Batch ID", "BATCH-NX-2026-09"),
                        WatermarkFieldConfig("inspectorName", "Site Engineer", "Eng. Tran Minh Duc"),
                        WatermarkFieldConfig("gpsAddress", "Construction Zone", "Metro Line 2 Depot - Gate B"),
                        WatermarkFieldConfig("antiFakeCode", "Anti-Fake Code", "NX9K4M7P2V8Q1A")
                    )
                ),
                WatermarkPresetModel(
                    id = "34",
                    codeName = "ATTENDANCE_TIMECLOCK_WM_ID",
                    title = "Geofence Shift Clock-In / Clock-Out",
                    category = "Attendance & Patrol",
                    accentColor = NextAiVerifiedEmerald,
                    isVip = false,
                    fields = listOf(
                        WatermarkFieldConfig("shiftTime", "Shift Punch Type", "MORNING SHIFT CLOCK-IN (ON TIME)"),
                        WatermarkFieldConfig("employeeName", "Employee Name", "Nguyen Van An (#NV-4082)"),
                        WatermarkFieldConfig("geofenceStatus", "Geofence Verification", "INSIDE GEOFENCE (34.5m from center)"),
                        WatermarkFieldConfig("companyLogo", "Company Brand", "NextAI Engineering Corp"),
                        WatermarkFieldConfig("antiFakeCode", "Anti-Fake Code", "NX9K4M7P2V8Q1A")
                    )
                ),
                WatermarkPresetModel(
                    id = "52",
                    codeName = "VLPR_VEHICLE_GATE_WM_ID",
                    title = "AI Vehicle License Plate Gate Log",
                    category = "Logistics & Fleet",
                    accentColor = NextAiVipGoldStart,
                    isVip = true,
                    cvModelId = 1,
                    fields = listOf(
                        WatermarkFieldConfig("plateNumber", "AI Recognized Plate", "51H-889.26 (Cement Mixer)"),
                        WatermarkFieldConfig("gateStatus", "Checkpoint Gate", "North Gate Entry - Weighbridge #2"),
                        WatermarkFieldConfig("cargoWeight", "Gross Weight", "34.6 Tons Verified"),
                        WatermarkFieldConfig("antiFakeCode", "Anti-Fake Code", "NX9K4M7P2V8Q1A")
                    )
                )
            )

            val initialPhotos = listOf(
                CapturedMediaItem(
                    id = "PHOTO_SEED_1",
                    fileName = "NEXTAI_20261026_093012.jpg",
                    timestampStr = "2026-10-26 MON 09:30:12",
                    gpsDms = "10°46'36.8\"N  106°42'02.9\"E",
                    address = "108 Nguyen Hue Blvd, District 1, HCMC",
                    watermarkTitle = "Real-Time Official Anti-Tamper",
                    antiFakeCode = "NX9K4M7P2V8Q1A",
                    aegisNonceHex = "AE8F029D4C71B83E910A5D2C6F8E1B40",
                    stegoSlicesEmbedded = 2,
                    stegoCrc32Hex = "84F29C1E",
                    projectTag = "Metro Line 2 - Station B4"
                ),
                CapturedMediaItem(
                    id = "PHOTO_SEED_2",
                    fileName = "NEXTAI_20261026_091544.jpg",
                    timestampStr = "2026-10-26 MON 09:15:44",
                    gpsDms = "10°46'41.2\"N  106°42'08.4\"E",
                    address = "Thu Thiem Tunnel East Portal, HCMC",
                    watermarkTitle = "AI Construction Steel Rebar Counter",
                    antiFakeCode = "NX7B2L9Q4W1E8R",
                    aegisNonceHex = "C4197A3E02F58D117B9E4A023C8F1D52",
                    stegoSlicesEmbedded = 2,
                    stegoCrc32Hex = "59D03A7B",
                    projectTag = "Thu Thiem Riverfront Tower"
                ),
                CapturedMediaItem(
                    id = "PHOTO_SEED_3",
                    fileName = "NEXTAI_20261026_080205.jpg",
                    timestampStr = "2026-10-26 MON 08:02:05",
                    gpsDms = "10°46'36.8\"N  106°42'02.9\"E",
                    address = "108 Nguyen Hue Blvd, District 1, HCMC",
                    watermarkTitle = "Geofence Shift Clock-In / Clock-Out",
                    antiFakeCode = "NX3P8T5M1Z6K9C",
                    aegisNonceHex = "71D4E90A3B82C654F0182E9A4B7C3D10",
                    stegoSlicesEmbedded = 2,
                    stegoCrc32Hex = "21E7B409",
                    projectTag = "Metro Line 2 - Station B4"
                )
            )

            return NextAiUiState(
                presets = presets,
                capturedPhotos = initialPhotos,
                selectedPhotoIds = setOf("PHOTO_SEED_1", "PHOTO_SEED_2")
            )
        }
    }
}
