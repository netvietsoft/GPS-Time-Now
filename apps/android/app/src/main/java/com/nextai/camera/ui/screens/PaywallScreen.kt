package com.nextai.camera.ui.screens

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
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.camera.NextAiAdConfig
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.theme.NextAiBusinessBlueEnd
import com.nextai.camera.ui.theme.NextAiBusinessBlueStart
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiDarkSurfaceElevated
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald
import com.nextai.camera.ui.theme.NextAiVipGoldEnd
import com.nextai.camera.ui.theme.NextAiVipGoldStart

/**
 * Screen 6: Dual-Tab Paywall Screen (`PaywallScreen.kt`).
 * Implements:
 * - Tab 0: `NextAI Plus` (Individual Gold Theme: Lossless Watermark Unpack, AI Steel/Plate Count,
 *   Custom Brand Logo Cutout, Ad-Free Experience)
 * - Tab 1: `NextAI Teamspace Business` (Enterprise Blue Theme: 100GB+ S3 Cloud, Geofence Timeclock,
 *   OneDrive/Google Drive Auto-Sync, Unlimited Excel/KMZ Exports)
 * - `AdFreePass` Countdown & Official AdMob Test Rewarded Video Unlock (`ca-app-pub-3940256099942544/5224354917`)
 * - Exit Retention Survey Dialog (`POST /nextai/v1/user/Retention/feekback`)
 */
@Composable
fun PaywallScreen(
    state: NextAiUiState,
    onActivateVip: () -> Unit,
    onWatchRewardedAdFreePass: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSkuIndex by remember { mutableIntStateOf(0) }
    var showExitRetentionDialog by remember { mutableStateOf(false) }

    val skus = if (selectedTab == 0) {
        listOf(
            Triple("Annual NextAI Plus", "$29.99 / year", "BEST VALUE • 7-Day Free Trial"),
            Triple("Monthly NextAI Plus", "$6.99 / month", "Flexible Monthly Billing"),
            Triple("1-Year One-Time Pass", "$34.99 one-time", "No Auto-Renewal")
        )
    } else {
        listOf(
            Triple("Teamspace Business Annual", "$9.99 / seat / mo", "100 GB S3 + Geofence + OneDrive Sync"),
            Triple("Teamspace Enterprise 50 Seats", "$399.00 / year", "1 TB S3 + Custom API + PostGIS Map"),
            Triple("14-Day Business Free Trial", "$0.00 today", "Full Teamspace SaaS Evaluation")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
            .padding(16.dp)
    ) {
        // Header + Dual Mode Switcher [45, 56, 300, 40]
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { showExitRetentionDialog = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NextAiDarkSurface)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedTab == 0) NextAiVipGoldStart else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "NextAI Plus",
                        color = if (selectedTab == 0) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedTab == 1) NextAiBusinessBlueStart else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Teamspace Business",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Feature Comparison Hero [16, 112, 358, 260]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = if (selectedTab == 0) {
                            listOf(NextAiVipGoldStart.copy(alpha = 0.25f), NextAiVipGoldEnd.copy(alpha = 0.12f))
                        } else {
                            listOf(NextAiBusinessBlueStart.copy(alpha = 0.30f), NextAiBusinessBlueEnd.copy(alpha = 0.15f))
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (selectedTab == 0) NextAiVipGoldStart else NextAiBusinessBlueStart,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = if (selectedTab == 0) NextAiVipGoldStart else NextAiBusinessBlueStart
                )
                Text(
                    text = if (selectedTab == 0) "Unlock NextAI Plus Pro Camera" else "NextAI Teamspace B2B Suite",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            val benefits = if (selectedTab == 0) {
                listOf(
                    "✓ Lossless Watermark Removal via JpegPacker Steganography",
                    "✓ On-Device Edge AI: Steel Pipe Counter (CVModelId=4) & License Plate OCR",
                    "✓ Unlimited Cloud AI Brand Logo Background Cutout",
                    "✓ 100% Ad-Free Experience + VIP Golden Watermark Skins"
                )
            } else {
                listOf(
                    "✓ 100 GB AWS S3 Real-Time Photo & Video Cloud Archive",
                    "✓ Geofence Shift Timeclock & Mandatory Project Watermark Locks",
                    "✓ Automated PDF, Apache POI Excel (.xlsx) & Google Earth (.kmz) Reports",
                    "✓ Direct Auto-Sync to Microsoft OneDrive & Google Drive"
                )
            }
            benefits.forEach { line ->
                Text(text = line, color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SKU Pricing Cards (3 Plans) [16, 388, 358, 260]
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            skus.forEachIndexed { index, (title, price, badge) ->
                val isSelected = selectedSkuIndex == index
                val accent = if (selectedTab == 0) NextAiVipGoldStart else NextAiBusinessBlueStart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NextAiDarkSurface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) accent else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedSkuIndex = index }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = badge, color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = price, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AdFreePass Countdown & Official AdMob Test Rewarded Video Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NextAiDarkSurfaceElevated)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AdFreePass Countdown: ${state.adFreePassRemainingMinutes / 60}h ${state.adFreePassRemainingMinutes % 60}m",
                    color = NextAiVerifiedEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AdMob Rewarded Test ID: ${NextAiAdConfig.REWARDED_VIDEO_TEST_ID}",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            }
            Button(
                onClick = onWatchRewardedAdFreePass,
                colors = ButtonDefaults.buttonColors(containerColor = NextAiVerifiedEmerald)
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Black)
                Text(" +24h Pass", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Subscribe CTA & Stripe Web Checkout Footer [16, 664, 358, 120]
        Button(
            onClick = onActivateVip,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == 0) NextAiVipGoldStart else NextAiBusinessBlueStart
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (state.isNextAiPlusVip) "NEXTAI PLUS VIP ACTIVE ✓" else "Start Free Trial / Activate Plan",
                color = if (selectedTab == 0) Color.Black else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }

    if (showExitRetentionDialog) {
        AlertDialog(
            onDismissRequest = { showExitRetentionDialog = false },
            title = { Text("Wait! Before You Leave NextAI Plus") },
            text = {
                Text(
                    "Tell us why you didn't upgrade today (Exit Retention Survey) or claim an instant 24-hour AdFreePass via our official Google AdMob Test Rewarded Video."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onWatchRewardedAdFreePass()
                        showExitRetentionDialog = false
                        onBack()
                    }
                ) {
                    Text("Claim 24h AdFreePass & Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitRetentionDialog = false
                        onBack()
                    }
                ) {
                    Text("Skip Survey")
                }
            }
        )
    }
}
