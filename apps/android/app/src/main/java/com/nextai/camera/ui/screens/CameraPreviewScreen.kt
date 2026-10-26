package com.nextai.camera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.camera.ui.CameraCaptureMode
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiDarkSurfaceElevated
import com.nextai.camera.ui.theme.NextAiEarnCyan
import com.nextai.camera.ui.theme.NextAiEarnPurple
import com.nextai.camera.ui.theme.NextAiGlassOverlayDark
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald
import com.nextai.camera.ui.theme.NextAiVipGoldStart
import kotlin.math.roundToInt

/**
 * Screen 1: Camera & Live Watermark Viewfinder (`CameraPreviewScreen.kt`).
 * Implements the 390x844 dp industrial layout:
 * - Top Control Bar (Flash, Ratio, 4K HD, ZeroDCE Night AI, Lens 0.5x/1x/2x, Settings)
 * - Live Viewfinder Canvas with Rule-of-Thirds Grid & Leveler
 * - Mini-Map & Azimuth Compass Widget (`[16, 108, 96, 96]`)
 * - Interactive Draggable Live Watermark Overlay (`[16, 468, 320, 168]`) with Segment-7 Digital Clock
 *   and `NTP & GPS Verified by NextAI` emerald badge
 * - Vertical Cryptographic `Anti-Fake Code` Strip (`[362, 468, 16, 168]`)
 * - 5 Camera Mode Selector Tabs (`Photo`, `Video`, `Attendance`, `AI Count`, `Collage`)
 * - Bottom Shutter Action Bar with Gallery, Watermark Store, Shutter, Teamspace & Earn shortcuts
 */
@Composable
fun CameraPreviewScreen(
    state: NextAiUiState,
    onCycleFlash: () -> Unit,
    onToggleZeroDce: () -> Unit,
    onSelectZoom: (Float) -> Unit,
    onDragWatermark: (Float, Float) -> Unit,
    onSelectMode: (CameraCaptureMode) -> Unit,
    onCapturePhoto: () -> Unit,
    onOpenWatermarkStore: () -> Unit,
    onOpenWatermarkEditor: () -> Unit,
    onOpenGalleryExport: () -> Unit,
    onOpenTeamspace: () -> Unit,
    onOpenVerifyAuth: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenEarnRobotics: () -> Unit,
    onOpenAiEdgeCount: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
    ) {
        // 1.1 Top Control Bar [0, 44, 390, 48]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NextAiDarkSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ControlPillButton(
                    text = "⚡ ${state.flashMode}",
                    active = state.flashMode != "OFF",
                    onClick = onCycleFlash
                )
                ControlPillButton(
                    text = state.aspectRatioLabel,
                    active = false,
                    onClick = {}
                )
                ControlPillButton(
                    text = state.resolutionLabel,
                    active = true,
                    onClick = {}
                )
                ControlPillButton(
                    text = if (state.zeroDceNightEnabled) "ZeroDCE ON" else "Night AI",
                    active = state.zeroDceNightEnabled,
                    activeColor = NextAiIndustrialAmber,
                    onClick = onToggleZeroDce
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onOpenVerifyAuth, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "VerifyAuth Center",
                        tint = NextAiVerifiedEmerald
                    )
                }
                IconButton(onClick = onOpenPaywall, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "NextAI Plus VIP",
                        tint = NextAiVipGoldStart
                    )
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
        }

        // 1.2 Live Viewfinder & Interactive Watermark Overlay [0, 92, 390, 560]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    Brush.verticalGradient(
                        colors = if (state.zeroDceNightEnabled) {
                            listOf(Color(0xFF172554), Color(0xFF1E3A8A), Color(0xFF0F172A))
                        } else {
                            listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF090D16))
                        }
                    )
                )
        ) {
            // Simulated Camera Viewfinder Grid + Construction Horizon Leveler
            Canvas(modifier = Modifier.fillMaxSize()) {
                val thirdW = size.width / 3f
                val thirdH = size.height / 3f
                val gridColor = Color.White.copy(alpha = 0.14f)
                drawLine(gridColor, Offset(thirdW, 0f), Offset(thirdW, size.height), strokeWidth = 1f)
                drawLine(gridColor, Offset(thirdW * 2, 0f), Offset(thirdW * 2, size.height), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, thirdH), Offset(size.width, thirdH), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, thirdH * 2), Offset(size.width, thirdH * 2), strokeWidth = 1f)

                // Center Electronic Leveler Reticle
                drawCircle(
                    color = NextAiVerifiedEmerald.copy(alpha = 0.45f),
                    radius = 28.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Top-Left Mini-Map & Azimuth Compass Widget [16, 108, 96, 96]
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .width(116.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NextAiGlassOverlayDark)
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Compass",
                        tint = NextAiIndustrialAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = state.azimuthCardinal,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF064E3B))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = NextAiVerifiedEmerald.copy(alpha = 0.35f),
                            radius = size.minDimension * 0.42f,
                            center = center
                        )
                        drawCircle(
                            color = NextAiPrimaryBlue,
                            radius = 5.dp.toPx(),
                            center = center
                        )
                    }
                    Text(
                        text = "SAT GPS LOCK",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 3.dp)
                    )
                }
            }

            // Top-Right Hardware Anti-Spoofing (Aegis) & Lens Switcher
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NextAiVerifiedEmerald.copy(alpha = 0.18f))
                        .border(1.dp, NextAiVerifiedEmerald, RoundedCornerShape(20.dp))
                        .clickable { onOpenVerifyAuth() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = NextAiVerifiedEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AEGIS NONCE: ${state.activeAegisNonce.take(8)}",
                        color = NextAiVerifiedEmerald,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Lens Zoom Switcher (0.5x / 1x / 2x)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NextAiGlassOverlayDark)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0.5f, 1.0f, 2.0f).forEach { zoom ->
                        val selected = state.lensZoomRatio == zoom
                        Text(
                            text = "${zoom}x",
                            color = if (selected) NextAiDarkBackground else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) NextAiIndustrialAmber else Color.Transparent)
                                .clickable { onSelectZoom(zoom) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Quick Shortcut to Edge AI Vision Screen
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NextAiIndustrialAmber.copy(alpha = 0.2f))
                        .border(1.dp, NextAiIndustrialAmber, RoundedCornerShape(20.dp))
                        .clickable { onOpenAiEdgeCount() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PrecisionManufacturing,
                        contentDescription = null,
                        tint = NextAiIndustrialAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = state.edgeAiResult.mode.badgeLabel,
                        color = NextAiIndustrialAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Draggable Live Watermark Card [16, 468, 320, 168]
            val preset = state.activePreset
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 18.dp, end = 40.dp)
                    .offset {
                        IntOffset(
                            state.watermarkOffsetX.roundToInt(),
                            state.watermarkOffsetY.roundToInt()
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDragWatermark(dragAmount.x, dragAmount.y)
                        }
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(NextAiGlassOverlayDark)
                    .border(1.5.dp, preset.accentColor.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                    .clickable { onOpenWatermarkEditor() }
                    .padding(12.dp)
            ) {
                // Header Row: Brand Logo + Segment-7 LED Clock + Verified Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(preset.accentColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NEXTAI OFFICIAL",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            if (state.logoCutoutActive) {
                                Text(
                                    text = state.customBrandLogoName,
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Segment-7 LED Digital Clock Display
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.liveClockText,
                                style = MaterialTheme.typography.displayLarge,
                                color = Color.White
                            )
                            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                                Text(
                                    text = state.liveDateWeekday,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "NTP + GPS VERIFIED (±${state.ntpDriftMs}ms)",
                                    color = NextAiVerifiedEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Enabled Watermark Fields
                preset.fields.filter { it.enabled }.forEach { field ->
                    val dynamicValue = when (field.key) {
                        "ntpRealTime" -> "${state.liveDateWeekday} ${state.liveClockText} (Stratum-1)"
                        "gpsCoordinatesDms" -> state.gpsDmsString
                        "streetAddress" -> state.streetAddress
                        "azimuthCompass" -> "${state.azimuthCardinal} • ${state.weatherSummary}"
                        "antiFakeCode" -> state.activeAntiFakeCode
                        else -> field.value
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${field.label}: ",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = dynamicValue,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Vertical Anti-Fake Photo Code Strip [362, 468, 16, 168]
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 18.dp)
                    .width(24.dp)
                    .height(172.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .border(1.dp, NextAiVerifiedEmerald.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .clickable { onOpenVerifyAuth() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    state.activeAntiFakeCode.forEach { ch ->
                        Text(
                            text = ch.toString(),
                            color = NextAiVerifiedEmerald,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 9.sp
                        )
                    }
                }
            }
        }

        // 1.3 Camera Mode Selector Tabs [0, 652, 390, 40]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NextAiDarkSurface)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraCaptureMode.entries.forEach { mode ->
                val isSelected = state.captureMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) NextAiPrimaryBlue else Color.Transparent)
                        .clickable {
                            onSelectMode(mode)
                            if (mode == CameraCaptureMode.AI_COUNT) {
                                onOpenAiEdgeCount()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = mode.title,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // 1.4 Bottom Action Bar [0, 692, 390, 118]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NextAiDarkBackground)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Internal Gallery & Export Hub
            BottomActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Gallery",
                        tint = Color.White
                    )
                },
                label = "Gallery (${state.capturedPhotos.size})",
                onClick = onOpenGalleryExport
            )

            // Mid-Left: Watermark Store
            BottomActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = "Watermarks",
                        tint = NextAiIndustrialAmber
                    )
                },
                label = "Watermarks",
                onClick = onOpenWatermarkStore
            )

            // Center: Industrial Shutter Button (with Attendance / Standard Ring)
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = if (state.captureMode == CameraCaptureMode.ATTENDANCE) NextAiVerifiedEmerald else NextAiPrimaryBlue,
                        shape = CircleShape
                    )
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.captureMode == CameraCaptureMode.ATTENDANCE) NextAiVerifiedEmerald else Color.White
                    )
                    .clickable { onCapturePhoto() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.captureMode == CameraCaptureMode.ATTENDANCE) "PUNCH" else "SNAP",
                    color = NextAiDarkBackground,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Mid-Right: Teamspace SaaS
            BottomActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Teamspace",
                        tint = NextAiPrimaryBlue
                    )
                },
                label = "Teamspace",
                onClick = onOpenTeamspace
            )

            // Far-Right: Embodied AI ("Earn" .mcap)
            BottomActionItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Earn AI",
                        tint = NextAiEarnCyan
                    )
                },
                label = "Earn .mcap",
                onClick = onOpenEarnRobotics
            )
        }
    }
}

@Composable
private fun ControlPillButton(
    text: String,
    active: Boolean,
    activeColor: Color = NextAiPrimaryBlue,
    onClick: () -> Unit
) {
    Surface(
        color = if (active) activeColor.copy(alpha = 0.22f) else NextAiDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (active) activeColor else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (active) Color.White else Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun BottomActionItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NextAiDarkSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
