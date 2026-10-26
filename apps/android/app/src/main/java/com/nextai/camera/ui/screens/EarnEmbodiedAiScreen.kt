package com.nextai.camera.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.camera.engine.McapRoboticsBridge
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiEarnCyan
import com.nextai.camera.ui.theme.NextAiEarnPurple
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald
import com.nextai.camera.ui.theme.NextAiVipGoldStart

/**
 * Screen 7: Embodied AI Robotics Data Crowdsourcing & AR Collect Screen (`EarnEmbodiedAiScreen.kt`).
 * Implements:
 * - Wallet Balance Card with Brazil `Pix (BRL)`, Mexico `SPEI (MXN)`, and `NextAI Plus VIP Days` exchange
 * - Live AR Collect Viewfinder (`[16, 180, 358, 280]`) rendering 21-Keypoint 3D Hand Skeletons
 *   (`HandOverlayView` left & right hands)
 * - 4-Sensor IMU 200Hz Telemetry Bar (`Accelerometer`, `Gyroscope`, `Gravity`, `48kHz PCM Mic`)
 * - ROS2 / Foxglove `.mcap` Container Multiplexer & Task Hall Queue
 */
@Composable
fun EarnEmbodiedAiScreen(
    state: NextAiUiState,
    onToggleRecordMcap: (Long) -> Unit,
    onExchangeVipDays: () -> Unit,
    onBack: () -> Unit
) {
    val leftHand = McapRoboticsBridge.generateSimulated21HandKeypoints(
        phaseRad = state.mcapElapsedSeconds * 0.45f,
        isRightHand = false
    )
    val rightHand = McapRoboticsBridge.generateSimulated21HandKeypoints(
        phaseRad = state.mcapElapsedSeconds * 0.45f + 0.8f,
        isRightHand = true
    )

    val tasks = listOf(
        Triple(901L, "Bimanual Desktop Cable & Tool Sorting", "R$ 15.00 BRL / MX$ 52.00 • Min 15s • 2 Hands"),
        Triple(902L, "Precision Screw Fastening & Assembly", "R$ 22.50 BRL / MX$ 78.00 • Min 20s • 2 Hands"),
        Triple(903L, "Kitchen Pouring & Utensil Manipulation", "R$ 18.00 BRL / MX$ 64.00 • Min 15s • 2 Hands")
    )

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
                    text = "NextAI Earn • Embodied AI Robotics",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "First-Person HEVC + 21-Keypoint Hand Pose + 200Hz IMU -> .mcap",
                    color = NextAiEarnCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Wallet Balance & Pix/SPEI Card [16, 56, 358, 112]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(NextAiEarnPurple.copy(alpha = 0.35f), NextAiEarnCyan.copy(alpha = 0.25f))
                    )
                )
                .border(1.dp, NextAiEarnCyan, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = NextAiEarnCyan)
                        Text(
                            text = "R$ ${"%.2f".format(state.earnBalanceBrl)} BRL  |  MX$ ${"%.2f".format(state.earnBalanceMxn)} MXN",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "Instant Payout via Brazil Pix / Mexico SPEI • VIP Credits: ${state.earnVipDaysAccumulated} Days",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = onExchangeVipDays,
                    colors = ButtonDefaults.buttonColors(containerColor = NextAiVipGoldStart)
                ) {
                    Text("Redeem VIP", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AR Viewfinder + 21 Hand Landmarks Overlay [16, 180, 358, 280]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.5.dp, NextAiEarnPurple, RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                fun drawHandSkeleton(points: List<McapRoboticsBridge.HandKeypoint2D>, boneColor: Color) {
                    McapRoboticsBridge.HAND_CONNECTIONS.forEach { (startIdx, endIdx) ->
                        val p1 = points[startIdx]
                        val p2 = points[endIdx]
                        drawLine(
                            color = boneColor,
                            start = Offset(p1.normalizedX * size.width, p1.normalizedY * size.height),
                            end = Offset(p2.normalizedX * size.width, p2.normalizedY * size.height),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                    points.forEach { kp ->
                        drawCircle(
                            color = Color.White,
                            radius = if (kp.index in listOf(4, 8, 12, 16, 20)) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = Offset(kp.normalizedX * size.width, kp.normalizedY * size.height)
                        )
                    }
                }
                drawHandSkeleton(leftHand, NextAiEarnCyan)
                drawHandSkeleton(rightHand, NextAiVerifiedEmerald)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "21-KEYPOINT HAND TRACKING: 98.4% CONFIDENCE",
                    color = NextAiVerifiedEmerald,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (state.isRecordingMcap) "REC ● ${state.mcapElapsedSeconds}s (.mcap)" else "READY (.mcap)",
                    color = if (state.isRecordingMcap) Color(0xFFEF4444) else NextAiEarnCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Button(
                onClick = { onToggleRecordMcap(901L) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isRecordingMcap) Color(0xFFEF4444) else NextAiEarnPurple
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = if (state.isRecordingMcap) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = null
                )
                Text(
                    text = if (state.isRecordingMcap) " Stop & Multiplex .MCAP" else " Start AR Sensor Recording (.MCAP)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // IMU 4-Sensor Telemetry Bar [16, 472, 358, 64]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(NextAiDarkSurface)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ACCEL: 0.12, 9.78, 0.41 m/s²",
                color = NextAiEarnCyan,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "GYRO: 0.02, -0.01 rad/s",
                color = NextAiIndustrialAmber,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "VAD MIC: 48kHz",
                color = NextAiVerifiedEmerald,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        state.lastMcapSummary?.let { summary ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Last .MCAP: ${summary.videoFramesWritten} HEVC frames + ${summary.imuSamplesWritten} IMU samples (${summary.fileSizeBytes / 1024} KB)",
                color = NextAiVerifiedEmerald,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Task Hall Queue [16, 548, 358, 240]
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(tasks) { (id, title, reward) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NextAiDarkSurface)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "#$id • $title", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = reward, color = NextAiEarnCyan, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onToggleRecordMcap(id) },
                        colors = ButtonDefaults.buttonColors(containerColor = NextAiEarnPurple)
                    ) {
                        Text("Shoot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
