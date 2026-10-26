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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.nextai.camera.ui.theme.NextAiEarnPurple
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald
import com.nextai.camera.ui.theme.NextAiVipGoldStart

/**
 * Screen 2B: Watermark Field Editor, Skin Selector & AI Logo Cutout (`WatermarkEditorScreen.kt`).
 * Allows toggling individual watermark fields (`ntpRealTime`, `gpsCoordinatesDms`, `azimuthCompass`,
 * `weatherTemp`, `projectNote`, `antiFakeCode`), selecting theme skins (`watermark/Skin/list`),
 * and invoking Cloud AI Logo Background Removal (`POST /nextai/v1/logo/cutout`).
 */
@Composable
fun WatermarkEditorScreen(
    state: NextAiUiState,
    onToggleField: (String) -> Unit,
    onUpdateFieldValue: (String, String) -> Unit,
    onSelectSkinHex: (String) -> Unit,
    onToggleLogoCutout: () -> Unit,
    onOpenLogoCutoutStudio: () -> Unit,
    onBack: () -> Unit
) {
    val activePreset = state.activePreset
    val skinPalette = listOf(
        "#2563EB" to NextAiPrimaryBlue,
        "#F59E0B" to NextAiIndustrialAmber,
        "#10B981" to NextAiVerifiedEmerald,
        "#6366F1" to NextAiEarnPurple,
        "#FBBF24" to NextAiVipGoldStart
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Watermark Editor • ${activePreset.title}",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Preset Code: ${activePreset.codeName}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Theme / Skin Color Selector (`watermark/Skin/list`)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NextAiDarkSurface)
                .padding(12.dp)
        ) {
            Text(
                text = "Watermark Theme Skin & Brand Cutout",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    skinPalette.forEach { (hex, color) ->
                        val selected = state.selectedThemeSkinHex == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onSelectSkinHex(hex) }
                        )
                    }
                }

                Button(
                    onClick = onOpenLogoCutoutStudio,
                    colors = ButtonDefaults.buttonColors(containerColor = NextAiIndustrialAmber)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.Black)
                    Text(
                        text = " AI Logo Cutout",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Display AI Transparent Brand Logo (${state.customBrandLogoName})",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )
                Switch(
                    checked = state.logoCutoutActive,
                    onCheckedChange = { onToggleLogoCutout() }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Field Toggles & Inline Value Editor
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(activePreset.fields) { field ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NextAiDarkSurface)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = field.label,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Field Key: ${field.key}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = field.enabled,
                            onCheckedChange = { onToggleField(field.key) }
                        )
                    }

                    if (field.enabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = field.value,
                            onValueChange = { onUpdateFieldValue(field.key, it) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
