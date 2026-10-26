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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald

/**
 * Screen 10 (Report 09 #8): Dedicated Geofence Timeclock & Inspection Checklist Screen (`GeofenceAttendanceScreen.kt`).
 * Coordinates `[16, 108, 358, 200]` Geofence Map Circle, `[16, 320, 358, 140]` Shift Clock-In/Out Card,
 * and `[16, 476, 358, 280]` Inspection Checklist Items.
 */
@Composable
fun GeofenceAttendanceScreen(
    state: NextAiUiState,
    onPunchClockWithCamera: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column {
                Text(
                    text = "Geofence Shift Timeclock & Checklist",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Radius 150m • Distance ${state.geofenceDistanceMeters}m • NTP Verified",
                    color = NextAiVerifiedEmerald,
                    fontSize = 11.sp
                )
            }
        }

        // Geofence Map Circle [16, 108, 358, 200]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF064E3B))
                .border(1.5.dp, NextAiVerifiedEmerald, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = NextAiVerifiedEmerald.copy(alpha = 0.22f),
                    radius = 74.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = NextAiVerifiedEmerald,
                    radius = 74.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = NextAiPrimaryBlue,
                    radius = 7.dp.toPx(),
                    center = Offset(center.x + 14.dp.toPx(), center.y - 10.dp.toPx())
                )
            }
            Text(
                text = "INSIDE VALID GEOFENCE (${state.geofenceDistanceMeters}m / 150m)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            )
        }

        // Shift Clock-In/Out Card [16, 320, 358, 140]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(NextAiDarkSurface)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Current Shift: Morning Construction Shift (07:30 - 17:00)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Atomic Server Time: ${state.liveDateWeekday} ${state.liveClockText} • AntiFakeCode ${state.activeAntiFakeCode}",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            Button(
                onClick = onPunchClockWithCamera,
                colors = ButtonDefaults.buttonColors(containerColor = NextAiVerifiedEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Capture Verified Shift Attendance Photo", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Inspection Checklist Items [16, 476, 358, 280]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(NextAiDarkSurface)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Mandatory Site Inspection Checklist",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            listOf(
                "1. Scaffolding Anchor & Safety Net Verification",
                "2. Electrical Distribution Board Lockout Tag",
                "3. Concrete Slump Test Photo with GPS Watermark"
            ).forEach { title ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, color = Color(0xFFE2E8F0), fontSize = 12.sp)
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NextAiVerifiedEmerald)
                }
            }
        }
    }
}
