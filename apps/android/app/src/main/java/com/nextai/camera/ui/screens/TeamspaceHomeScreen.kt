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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.nextai.camera.ui.theme.NextAiDarkSurfaceElevated
import com.nextai.camera.ui.theme.NextAiIndustrialAmber
import com.nextai.camera.ui.theme.NextAiPrimaryBlue
import com.nextai.camera.ui.theme.NextAiVerifiedEmerald

/**
 * Screen 4 (Report 09 #3, #8, #9): Teamspace SaaS Home Screen (`TeamspaceHomeScreen.kt`).
 * Implements all 4 tabs of the B2B Teamspace ecosystem:
 * - Tab 0: Photo Stream & Multi-Dimensional OCR Search
 * - Tab 1: Construction Projects & Interactive Satellite Photo Map Clusters
 * - Tab 2: Geofence Timeclock Circle (`app/group/timeclock/geofence/*`) & Field Inspection Checklists
 * - Tab 3: Team Members ACL & Microsoft OneDrive / Google Drive Auto-Sync Integrations
 */
@Composable
fun TeamspaceHomeScreen(
    state: NextAiUiState,
    onOpenDedicatedGeofence: () -> Unit,
    onOpenDedicatedPhotoMap: () -> Unit,
    onPunchAttendancePhoto: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var ocrSearchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NextAiDarkBackground)
    ) {
        // 4.1 Team Header & Cloud S3 Storage Quota Bar [0, 44, 390, 72]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NextAiDarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
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
                            text = state.teamName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Teamspace Business Plan • 28 Members • S3 Real-Time Sync",
                            color = NextAiVerifiedEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val quotaProgress = (state.teamStorageUsedGb / state.teamStorageLimitGb).toFloat().coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cloud S3 Storage Quota",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Text(
                    text = "${state.teamStorageUsedGb} GB / ${state.teamStorageLimitGb} GB",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { quotaProgress },
                color = NextAiPrimaryBlue,
                trackColor = NextAiDarkSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }

        // Main Tab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> TeamspacePhotoFeedTab(
                    state = state,
                    ocrQuery = ocrSearchQuery,
                    onQueryChange = { ocrSearchQuery = it }
                )
                1 -> TeamspaceProjectsAndMapTab(
                    onOpenFullMap = onOpenDedicatedPhotoMap
                )
                2 -> TeamspaceGeofenceAndChecklistTab(
                    state = state,
                    onPunchAttendance = onPunchAttendancePhoto,
                    onOpenFullGeofence = onOpenDedicatedGeofence
                )
                3 -> TeamspaceMembersAndCloudSyncTab(state = state)
            }
        }

        // Bottom Navigation (4 Tabs) [0, 764, 390, 80]
        NavigationBar(containerColor = NextAiDarkSurface) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                label = { Text("Photo Feed", fontSize = 10.sp) }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.FolderSpecial, contentDescription = null) },
                label = { Text("Projects & Map", fontSize = 10.sp) }
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.FactCheck, contentDescription = null) },
                label = { Text("Geofence", fontSize = 10.sp) }
            )
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                label = { Text("Members & Sync", fontSize = 10.sp) }
            )
        }
    }
}

@Composable
private fun TeamspacePhotoFeedTab(
    state: NextAiUiState,
    ocrQuery: String,
    onQueryChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = ocrQuery,
            onValueChange = onQueryChange,
            label = { Text("Search by OCR Text on Photo, Project, Member, or AntiFakeCode") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        val filtered = state.capturedPhotos.filter {
            ocrQuery.isBlank() ||
                it.fileName.contains(ocrQuery, ignoreCase = true) ||
                it.projectTag.contains(ocrQuery, ignoreCase = true) ||
                it.antiFakeCode.contains(ocrQuery, ignoreCase = true)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { photo ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NextAiDarkSurface)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = photo.projectTag,
                            color = NextAiIndustrialAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SYNCED TO S3",
                            color = NextAiVerifiedEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${photo.fileName} • ${photo.watermarkTitle}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "📍 ${photo.address} (${photo.gpsDms})",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "🔒 AntiFakeCode: ${photo.antiFakeCode} • Nonce: ${photo.aegisNonceHex.take(12)}",
                        color = NextAiVerifiedEmerald,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamspaceProjectsAndMapTab(
    onOpenFullMap: () -> Unit
) {
    val projects = listOf(
        Triple("Metro Line 2 - Station B4", "150m GPS Auto-Tag Radius • 1,420 Photos", "ACTIVE"),
        Triple("Thu Thiem Riverfront Tower", "250m GPS Auto-Tag Radius • 890 Photos", "ACTIVE"),
        Triple("Long Thanh Airport Terminal T1", "500m GPS Auto-Tag Radius • 3,150 Photos", "ACTIVE")
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Interactive Satellite Photo Map Cluster Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF064E3B))
                .border(1.dp, NextAiVerifiedEmerald, RoundedCornerShape(14.dp))
                .clickable { onOpenFullMap() }
                .padding(14.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = NextAiPrimaryBlue.copy(alpha = 0.35f),
                    radius = 38.dp.toPx(),
                    center = Offset(size.width * 0.3f, size.height * 0.45f)
                )
                drawCircle(
                    color = NextAiIndustrialAmber.copy(alpha = 0.40f),
                    radius = 44.dp.toPx(),
                    center = Offset(size.width * 0.68f, size.height * 0.55f)
                )
            }
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(
                    text = "Satellite Photo Cluster Map (PostGIS)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to inspect 5,460 geo-clustered site photos by GPS bounding box",
                    color = Color(0xFFD1FAE5),
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = onOpenFullMap,
                colors = ButtonDefaults.buttonColors(containerColor = NextAiPrimaryBlue),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Text(" Open Full Photo Map", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        projects.forEach { (title, sub, status) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NextAiDarkSurface)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = sub, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                Text(
                    text = status,
                    color = NextAiVerifiedEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun TeamspaceGeofenceAndChecklistTab(
    state: NextAiUiState,
    onPunchAttendance: () -> Unit,
    onOpenFullGeofence: () -> Unit
) {
    val checklists = listOf(
        "Rebar Spacing & Concrete Cover Inspection (12/12 Verified)" to true,
        "Personal Protective Equipment (PPE Helmet & Harness) Check" to true,
        "Tower Crane Load Limit & Wind Speed Telemetry Audit" to false
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Geofence Map Circle [16, 108, 358, 200]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(NextAiDarkSurface)
                .border(1.dp, NextAiVerifiedEmerald, RoundedCornerShape(14.dp))
                .clickable { onOpenFullGeofence() }
                .padding(14.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = NextAiVerifiedEmerald.copy(alpha = 0.16f),
                    radius = 58.dp.toPx(),
                    center = Offset(size.width * 0.78f, size.height * 0.5f)
                )
                drawCircle(
                    color = NextAiVerifiedEmerald,
                    radius = 58.dp.toPx(),
                    center = Offset(size.width * 0.78f, size.height * 0.5f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = NextAiPrimaryBlue,
                    radius = 6.dp.toPx(),
                    center = Offset(size.width * 0.75f, size.height * 0.48f)
                )
            }

            Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                Text(
                    text = "Geofence Timeclock Zone",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (state.insideGeofence) {
                        "INSIDE VALID GEOFENCE (${state.geofenceDistanceMeters}m / 150m radius)"
                    } else {
                        "OUTSIDE GEOFENCE BOUNDARY"
                    },
                    color = NextAiVerifiedEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onPunchAttendance,
                    colors = ButtonDefaults.buttonColors(containerColor = NextAiVerifiedEmerald)
                ) {
                    Text(
                        text = "Punch Shift Clock-In Photo",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Text(
            text = "Field Inspection Checklists (Require Live Camera Proof)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        checklists.forEach { (itemTitle, completed) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NextAiDarkSurface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemTitle,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (completed) NextAiVerifiedEmerald else NextAiIndustrialAmber
                )
            }
        }
    }
}

@Composable
private fun TeamspaceMembersAndCloudSyncTab(state: NextAiUiState) {
    var oneDriveSync by remember { mutableStateOf(state.oneDriveSyncEnabled) }
    var googleDriveSync by remember { mutableStateOf(state.googleDriveSyncEnabled) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(NextAiDarkSurface)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = NextAiPrimaryBlue)
                Text(
                    text = "Enterprise Cloud Auto-Backup Integrations",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Microsoft OneDrive Enterprise (MSAL)", color = Color.White, fontSize = 13.sp)
                    Text("Auto-export photos & .xlsx reports to /NextAI_Teamspace", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                Switch(checked = oneDriveSync, onCheckedChange = { oneDriveSync = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Google Drive Workspace Sync", color = Color.White, fontSize = 13.sp)
                    Text("Real-time folder mirroring by Project & Date", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                Switch(checked = googleDriveSync, onCheckedChange = { googleDriveSync = it })
            }
        }

        val members = listOf(
            Triple("Tran Minh Duc", "OWNER • Chief Site Engineer", "482 Photos"),
            Triple("Nguyen Van An", "ADMIN • QA/QC Inspector", "319 Photos"),
            Triple("Le Hoang Nam", "MEMBER • Structural Supervisor", "275 Photos"),
            Triple("Pham Thi Mai", "MEMBER • Safety Officer", "194 Photos")
        )

        members.forEach { (name, role, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NextAiDarkSurface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NextAiPrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = role, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
                Text(text = count, color = NextAiVerifiedEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
