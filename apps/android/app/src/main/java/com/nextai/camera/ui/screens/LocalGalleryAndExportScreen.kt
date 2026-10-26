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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextai.solution.NextAiEnterpriseSolution
import com.nextai.camera.ui.CapturedMediaItem
import com.nextai.camera.ui.NextAiUiState
import com.nextai.camera.ui.theme.NextAiDarkBackground
import com.nextai.camera.ui.theme.NextAiDarkSurface
import com.nextai.camera.ui.theme.NextAiDarkSurfaceElevated
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald
import com.nextai.camera.ui.theme.NextAiVipGoldStart

/**
 * Screen 3 (Report 09 #4): Internal Gallery, Batch Report Export Hub & JpegPacker Lossless
 * Watermark Removal (`LocalGalleryAndExportScreen.kt`).
 * Supports:
 * - Batch export to PDF Work Report (1/2/4/6 photos per page), Apache POI Excel `.xlsx`,
 *   Google Earth `.kmz` with GPS pins, and `.zip` archive
 * - Single-tap `JpegPacker` Steganography Unpack (`startDecode`) to restore 100% original pixels
 */
@Composable
fun LocalGalleryAndExportScreen(
    state: NextAiUiState,
    onToggleSelectPhoto: (String) -> Unit,
    onUnpackCleanPhoto: (String) -> Unit,
    onExportBatch: (String) -> Unit,
    onVerifyPhotoCode: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
            .padding(16.dp)
    ) {
        // Header Toolbar & Batch Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                        text = "Internal Album & Batch Export Hub",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.selectedPhotoIds.size} selected of ${state.capturedPhotos.size} verified photos",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        state.lastExportBanner?.let { banner ->
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NextAiVerifiedEmerald.copy(alpha = 0.18f))
                    .border(1.dp, NextAiVerifiedEmerald, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = banner,
                    color = NextAiVerifiedEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Photo Multi-Select List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.capturedPhotos) { item ->
                GalleryPhotoCard(
                    item = item,
                    selected = item.id in state.selectedPhotoIds,
                    onToggleSelect = { onToggleSelectPhoto(item.id) },
                    onUnpackLossless = { onUnpackCleanPhoto(item.id) },
                    onVerify = { onVerifyPhotoCode(item.antiFakeCode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Export Format Selector Sheet [0, 616, 390, 228]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NextAiDarkSurface)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "One-Tap Batch Report Generator (Selected: ${state.selectedPhotoIds.size})",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormatTile(
                    modifier = Modifier.weight(1f),
                    title = "PDF Work Report",
                    subtitle = "1/2/4/6 Photos/Page",
                    accentColor = NextAiPrimaryBlue,
                    icon = { Icon(Icons.Default.Description, null, tint = NextAiPrimaryBlue) },
                    onClick = { onExportBatch(NextAiEnterpriseSolution.ExportFormat.PDF_REPORT.label) }
                )
                ExportFormatTile(
                    modifier = Modifier.weight(1f),
                    title = "Excel .xlsx",
                    subtitle = "Apache POI Auto-Columns",
                    accentColor = NextAiVerifiedEmerald,
                    icon = { Icon(Icons.Default.TableChart, null, tint = NextAiVerifiedEmerald) },
                    onClick = { onExportBatch(NextAiEnterpriseSolution.ExportFormat.EXCEL_SHEET.label) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormatTile(
                    modifier = Modifier.weight(1f),
                    title = "Google Earth .KMZ",
                    subtitle = "3D GPS Pin Overlay",
                    accentColor = NextAiIndustrialAmber,
                    icon = { Icon(Icons.Default.Public, null, tint = NextAiIndustrialAmber) },
                    onClick = { onExportBatch(NextAiEnterpriseSolution.ExportFormat.GOOGLE_EARTH_KMZ.label) }
                )
                ExportFormatTile(
                    modifier = Modifier.weight(1f),
                    title = "ZIP Bundle",
                    subtitle = "Original + EXIF Archive",
                    accentColor = NextAiVipGoldStart,
                    icon = { Icon(Icons.Default.FolderZip, null, tint = NextAiVipGoldStart) },
                    onClick = { onExportBatch(NextAiEnterpriseSolution.ExportFormat.ZIP_ARCHIVE.label) }
                )
            }
        }
    }
}

@Composable
private fun GalleryPhotoCard(
    item: CapturedMediaItem,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onUnpackLossless: () -> Unit,
    onVerify: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NextAiDarkSurface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) NextAiPrimaryBlue else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onToggleSelect() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (selected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = null,
                    tint = if (selected) NextAiPrimaryBlue else Color(0xFF94A3B8)
                )
                Column {
                    Text(
                        text = item.fileName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.timestampStr} • ${item.projectTag}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (item.isWatermarkRemoved) NextAiVipGoldStart.copy(alpha = 0.2f)
                        else NextAiVerifiedEmerald.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (item.isWatermarkRemoved) "CLEAN RESTORED" else "STEGO PACKED",
                    color = if (item.isWatermarkRemoved) NextAiVipGoldStart else NextAiVerifiedEmerald,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "GPS: ${item.gpsDms} • ${item.address}",
            color = Color(0xFFCBD5E1),
            fontSize = 11.sp
        )
        Text(
            text = "AntiFakeCode: ${item.antiFakeCode} • JpegPacker CRC32: ${item.stegoCrc32Hex} (${item.stegoSlicesEmbedded} slices)",
            color = NextAiVerifiedEmerald,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onUnpackLossless,
                colors = ButtonDefaults.buttonColors(containerColor = NextAiVipGoldStart),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AutoFixNormal, contentDescription = null, tint = Color.Black)
                Text(
                    text = if (item.isWatermarkRemoved) " Restored (Lossless)" else " Remove Watermark",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onVerify,
                colors = ButtonDefaults.buttonColors(containerColor = NextAiDarkSurfaceElevated),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NextAiVerifiedEmerald)
                Text(
                    text = " Verify PhotoCode",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExportFormatTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NextAiDarkSurfaceElevated)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }
    }
}
