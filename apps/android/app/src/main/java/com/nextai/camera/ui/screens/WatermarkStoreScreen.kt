package com.nextai.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.WatermarkPresetModel
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiDarkSurfaceElevated
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald
import com.nextai.camera.ui.theme.NextAiVipGoldStart

/**
 * Screen 2A: Watermark Store Screen (`WatermarkStoreScreen.kt`).
 * Displays PGC, UGC, and Team Watermark templates across industry categories
 * (`My/Recent`, `Team Watermarks`, `Time & Location`, `Construction & Engineering`,
 * `Attendance & Patrol`, `Logistics & Fleet`, `Shared UGC Code`).
 */
@Composable
fun WatermarkStoreScreen(
    state: NextAiUiState,
    onSelectPreset: (String) -> Unit,
    onEditPreset: () -> Unit,
    onBack: () -> Unit
) {
    val categories = listOf(
        "All Templates",
        "Time & Location",
        "Construction & Engineering",
        "Attendance & Patrol",
        "Logistics & Fleet",
        "Team Mandatory",
        "UGC Share Code"
    )
    var selectedCategory by remember { mutableStateOf("All Templates") }
    var ugcShareCodeQuery by remember { mutableStateOf("") }

    val filteredPresets = state.presets.filter {
        selectedCategory == "All Templates" || it.category.equals(selectedCategory, ignoreCase = true)
    }.ifEmpty { state.presets }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
            .padding(16.dp)
    ) {
        // Header
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
                        text = "NextAI Watermark Store",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "PGC Industry Templates • Team Mandates • UGC Share Codes",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Button(
                onClick = onEditPreset,
                colors = ButtonDefaults.buttonColors(containerColor = NextAiPrimaryBlue),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Text(" Customize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // UGC Share Code Lookup Bar
        OutlinedTextField(
            value = ugcShareCodeQuery,
            onValueChange = { ugcShareCodeQuery = it },
            label = { Text("Enter UGC Watermark Share Code or Keyword (e.g. NX-WM-2026)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                val active = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (active) NextAiPrimaryBlue else NextAiDarkSurfaceElevated)
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        color = if (active) Color.White else Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Template Grid (2 Columns, 174x132 dp cards)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredPresets) { preset ->
                WatermarkTemplateCard(
                    preset = preset,
                    isSelected = preset.id == state.activePresetId,
                    onSelect = { onSelectPreset(preset.id) },
                    onCustomize = {
                        onSelectPreset(preset.id)
                        onEditPreset()
                    }
                )
            }
        }
    }
}

@Composable
private fun WatermarkTemplateCard(
    preset: WatermarkPresetModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCustomize: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NextAiDarkSurface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) NextAiVerifiedEmerald else preset.accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(preset.accentColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "ID #${preset.id}",
                    color = preset.accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (preset.isVip) {
                Text(
                    text = "NEXTAI PLUS",
                    color = NextAiVipGoldStart,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = NextAiVerifiedEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = preset.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = preset.category,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        preset.fields.take(3).forEach { field ->
            Text(
                text = "• ${field.label}",
                color = Color(0xFFCBD5E1),
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isSelected) "ACTIVE" else "USE TEMPLATE",
                color = if (isSelected) NextAiVerifiedEmerald else NextAiPrimaryBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCustomize() }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Edit & Share",
                    tint = NextAiIndustrialAmber,
                    modifier = Modifier.height(14.dp)
                )
                Text(
                    text = " Edit",
                    color = NextAiIndustrialAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
