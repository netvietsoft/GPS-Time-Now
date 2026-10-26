package com.nextai.camera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.camera.engine.EdgeAiNcnnBridge
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiDarkSurfaceElevated
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald

/**
 * Screen 8 (Report 09 #10): Edge AI Computer Vision Counter & Night Enhancer (`AiEdgeCountScreen.kt`).
 * Runs NCNN on-device models via `libnnpackage-f99779bd.so`:
 * - `SteelDetect` (`CVModelId = 4`): Construction steel rebar / pipe counter
 * - `VLPR` (`CVModelId = 1`): Vehicle License Plate Recognition
 * - `FaceDetect` (`CVModelId = 2`): Group safety briefing crew counter
 * - `ZeroDCE` (`assets/algorithm/nightmode/`): Deep curve low-light enhancer
 */
@Composable
fun AiEdgeCountScreen(
    state: NextAiUiState,
    onSelectVisionMode: (EdgeAiNcnnBridge.EdgeVisionMode) -> Unit,
    onApplyCountToWatermarkAndSnap: () -> Unit,
    onBack: () -> Unit
) {
    val result = state.edgeAiResult

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Column {
                Text(
                    text = "NextAI Edge CV • NCNN Inference Studio",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CVModelId=${result.mode.cvModelId} • Latency ${result.inferenceTimeMs}ms • libnnpackage-f99779bd.so",
                    color = NextAiIndustrialAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mode Switcher Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EdgeAiNcnnBridge.EdgeVisionMode.entries.forEach { mode ->
                val active = mode == state.activeEdgeVisionMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) NextAiIndustrialAmber else NextAiDarkSurfaceElevated)
                        .clickable { onSelectVisionMode(mode) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.badgeLabel,
                        color = if (active) Color.Black else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Bounding Boxes Overlay Canvas [0, 92, 390, 560]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (result.mode == EdgeAiNcnnBridge.EdgeVisionMode.ZERO_DCE_NIGHT) {
                        Color(0xFF1E3A8A)
                    } else {
                        Color(0xFF0F172A)
                    }
                )
                .border(1.5.dp, NextAiIndustrialAmber, RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                result.detections.forEach { det ->
                    val cx = det.normalizedX * size.width
                    val cy = det.normalizedY * size.height
                    val r = det.normalizedRadius * size.minDimension
                    drawCircle(
                        color = NextAiIndustrialAmber.copy(alpha = 0.28f),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = NextAiVerifiedEmerald,
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // Auto Counter Badge [16, 580, 180, 56]
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NextAiDarkSurface.copy(alpha = 0.92f))
                    .border(1.dp, NextAiVerifiedEmerald, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = result.mode.title,
                    color = NextAiIndustrialAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.summaryText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Target Watermark Preset ID: #${result.mode.watermarkPresetId} • ZeroDCE Gain: ${result.zeroDceCurveGain}x",
                    color = NextAiVerifiedEmerald,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onApplyCountToWatermarkAndSnap,
            colors = ButtonDefaults.buttonColors(containerColor = NextAiPrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Text(
                text = " Stamp AI Result into Watermark & Capture Photo",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
