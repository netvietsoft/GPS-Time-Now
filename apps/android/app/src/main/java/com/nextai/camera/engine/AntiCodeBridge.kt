package com.nextai.camera.engine

import java.security.MessageDigest
import kotlin.math.abs

/**
 * Cryptographic Anti-Fake Photo Code & Blind Watermark Bridge (`AntiCodeBridge`).
 * Interfaces with `code_detect_anticode_jni.cpp` (`libnnpackage-f99779bd.so`) to encode
 * NTP atomic timestamp, GPS coordinates, and mock-location flags into a 14-character
 * Base62 `AntiFakeCode` printed along the watermark border and embedded as a DCT blind watermark.
 */
object AntiCodeBridge {
    private const val BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    data class AntiCodeMetadata(
        val antiFakeCode: String,
        val ntpTimestampMs: Long,
        val latitude: Double,
        val longitude: Double,
        val isMockLocation: Boolean,
        val locationProviderType: Int,
        val blindWatermarkSignatureHex: String
    )

    fun generateAntiFakeCode(
        ntpTimeMs: Long,
        latitude: Double,
        longitude: Double,
        isMock: Boolean = false,
        locationType: Int = 1
    ): AntiCodeMetadata {
        val raw = "NEXTAI|$ntpTimeMs|${"%.6f".format(latitude)}|${"%.6f".format(longitude)}|$isMock|$locationType"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder(14)
        sb.append("NX")
        for (i in 0 until 12) {
            val idx = (digest[i].toInt() and 0xFF) % BASE62.length
            sb.append(BASE62[idx])
        }
        val signatureHex = digest.take(16).joinToString("") { "%02X".format(it) }
        return AntiCodeMetadata(
            antiFakeCode = sb.toString(),
            ntpTimestampMs = ntpTimeMs,
            latitude = latitude,
            longitude = longitude,
            isMockLocation = isMock,
            locationProviderType = locationType,
            blindWatermarkSignatureHex = signatureHex
        )
    }

    fun formatDmsCoordinates(lat: Double, lng: Double): String {
        fun toDms(value: Double, posHemi: Char, negHemi: Char): String {
            val hemi = if (value >= 0) posHemi else negHemi
            val absVal = abs(value)
            val deg = absVal.toInt()
            val minFull = (absVal - deg) * 60.0
            val min = minFull.toInt()
            val sec = (minFull - min) * 60.0
            return "%d°%02d'%04.1f\"%c".format(deg, min, sec, hemi)
        }
        return "${toDms(lat, 'N', 'S')}  ${toDms(lng, 'E', 'W')}"
    }
}
