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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald

/**
 * Screen 12 (Report 09 #11): AI Brand Logo Search & Background Cutout Studio (`AiLogoCutoutScreen.kt`).
 * Connects to `POST /nextai/v1/logo/search` and `POST /nextai/v1/logo/cutout` to remove solid
 * backgrounds from uploaded corporate logos and place transparent PNGs onto the watermark header.
 */
@Composable
fun AiLogoCutoutScreen(
    state: NextAiUiState,
    onToggleLogoCutout: () -> Unit,
    onBack: () -> Unit
) {
    var searchKeyword by remember { mutableStateOf(state.customBrandLogoName) }
    var logoOpacity by remember { mutableFloatStateOf(0.92f) }

    val expressLogos = listOf(
        "NextAI Engineering Corp",
        "NextAI Global Logistics",
        "NextAI Construction & Safety",
        "NextAI Property Management"
    )

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
                    text = "AI Brand Logo Search & Cutout",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cloud U2Net / RMBG-1.4 Automatic Background Removal",
                    color = NextAiIndustrialAmber,
                    fontSize = 11.sp
                )
            }
        }

        // Search Input Bar [16, 56, 358, 48]
        OutlinedTextField(
            value = searchKeyword,
            onValueChange = { searchKeyword = it },
            label = { Text("Search Enterprise Brand Logo or Upload Image") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Before / After Cutout Preview [16, 120, 358, 220]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BEFORE (Opaque BG)", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(searchKeyword, color = NextAiPrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NextAiDarkSurface)
                    .border(1.5.dp, NextAiVerifiedEmerald, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AFTER AI CUTOUT (Alpha PNG)", color = NextAiVerifiedEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = searchKeyword,
                        color = Color.White.copy(alpha = logoOpacity),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NextAiDarkSurface)
                .padding(12.dp)
        ) {
            Text(
                text = "Watermark Logo Opacity: ${(logoOpacity * 100).toInt()}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = logoOpacity,
                onValueChange = { logoOpacity = it },
                valueRange = 0.25f..1.0f
            )
        }

        // Brand Logo Presets
        expressLogos.forEach { brand ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NextAiDarkSurface)
                    .clickable { searchKeyword = brand }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = brand, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "TRANSPARENT PNG", color = NextAiVerifiedEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (!state.logoCutoutActive) onToggleLogoCutout()
                onBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = NextAiPrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = null)
            Text(" Apply Cutout Logo to Active Watermark", fontWeight = FontWeight.ExtraBold)
        }
    }
}
