package com.nextai.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.camera.NextAiAdConfig
import com.nextai.camera.data.remote.NextAiNetworkConfig
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald

/**
 * Screen 9 (Report 09 #12): Settings, Account Center, Internal Backend Switcher & AdMob Test Console (`SettingsScreen.kt`).
 * Displays:
 * - Account Profile & `AdFreePass` Status
 * - Internal Backend Endpoint Switcher (`https://api.nextai.global/nextai/v1/`) + Network Benchmark (`network/benchmark`)
 * - Camera Dual-Save & Localization (`values/strings.xml` + `values-vi/strings.xml`)
 * - Official Google AdMob Test Ad Units Verification Console (`Task.txt` #4)
 */
@Composable
fun SettingsScreen(
    state: NextAiUiState,
    onSwitchEndpointProfile: (NextAiNetworkConfig.EndpointProfile) -> Unit,
    onRunNetworkBenchmark: () -> Unit,
    onBack: () -> Unit
) {
    var dualSaveOriginal by remember { mutableStateOf(state.dualSaveOriginalPhoto) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
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
                        text = "Settings, Backend & AdMob Console",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "NextAI Technology & Solution • com.nextai.camera v10.0.281",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Internal Backend API Connection Switcher (Task.txt #5)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NextAiDarkSurface)
                    .border(1.dp, NextAiPrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Internal Backend API Gateway (Port 4580)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                NextAiNetworkConfig.EndpointProfile.entries.forEach { profile ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSwitchEndpointProfile(profile) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.endpointProfile == profile,
                            onClick = { onSwitchEndpointProfile(profile) }
                        )
                        Column {
                            Text(text = profile.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = profile.baseUrl,
                                color = NextAiVerifiedEmerald,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Text(
                    text = "Benchmark Status: ${state.lastBenchmarkStatus}",
                    color = NextAiIndustrialAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Button(
                    onClick = onRunNetworkBenchmark,
                    colors = ButtonDefaults.buttonColors(containerColor = NextAiPrimaryBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null)
                    Text(" Run Internal Backend & NTP Benchmark", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Camera & Localization Preferences
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NextAiDarkSurface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Camera, Steganography & Localization",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dual-Save Original & JpegPacker Stego Slice", color = Color.White, fontSize = 13.sp)
                        Text("Embed lossless background slices into JPEG tail", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(checked = dualSaveOriginal, onCheckedChange = { dualSaveOriginal = it })
                }
                Text(
                    text = "Active Localization: English (values/strings.xml) & Tiếng Việt (values-vi/strings.xml)",
                    color = NextAiVerifiedEmerald,
                    fontSize = 11.sp
                )
            }
        }

        // Official Google AdMob Test Console (Task.txt #4)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NextAiDarkSurface)
                    .border(1.dp, NextAiVerifiedEmerald.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Official Google AdMob Test Configuration",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "App ID: ${NextAiAdConfig.ADMOB_APP_ID}",
                    color = NextAiVerifiedEmerald,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(NextAiAdConfig.allTestPlacements) { placement ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NextAiDarkSurface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "${placement.format} • ${placement.name}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = placement.unitId,
                        color = NextAiVerifiedEmerald,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "TEST KEY",
                    color = NextAiIndustrialAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
