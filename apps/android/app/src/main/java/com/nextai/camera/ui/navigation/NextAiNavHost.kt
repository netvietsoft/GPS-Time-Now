package com.nextai.camera.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nextai.camera.ui.CameraCaptureMode
import com.nextai.camera.ui.NextAiCameraViewModel
import com.nextai.camera.ui.screens.AiEdgeCountScreen
import com.nextai.camera.ui.screens.AiLogoCutoutScreen
import com.nextai.camera.ui.screens.CameraPreviewScreen
import com.nextai.camera.ui.screens.EarnEmbodiedAiScreen
import com.nextai.camera.ui.screens.GeofenceAttendanceScreen
import com.nextai.camera.ui.screens.LocalGalleryAndExportScreen
import com.nextai.camera.ui.screens.PaywallScreen
import com.nextai.camera.ui.screens.PhotoMapClusterScreen
import com.nextai.camera.ui.screens.SettingsScreen
import com.nextai.camera.ui.screens.TeamspaceHomeScreen
import com.nextai.camera.ui.screens.VerifyAuthScreen
import com.nextai.camera.ui.screens.WatermarkEditorScreen
import com.nextai.camera.ui.screens.WatermarkStoreScreen

object NextAiRoutes {
    const val CAMERA_PREVIEW = "camera_preview"
    const val WATERMARK_STORE = "watermark_store"
    const val WATERMARK_EDITOR = "watermark_editor"
    const val AI_LOGO_CUTOUT = "ai_logo_cutout"
    const val LOCAL_GALLERY_EXPORT = "local_gallery_export"
    const val TEAMSPACE_HOME = "teamspace_home"
    const val GEOFENCE_ATTENDANCE = "geofence_attendance"
    const val PHOTO_MAP_CLUSTER = "photo_map_cluster"
    const val VERIFY_AUTH = "verify_auth"
    const val PAYWALL = "paywall"
    const val EARN_EMBODIED_AI = "earn_embodied_ai"
    const val AI_EDGE_COUNT = "ai_edge_count"
    const val SETTINGS = "settings"
}

@Composable
fun NextAiNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: NextAiCameraViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NextAiRoutes.CAMERA_PREVIEW
    ) {
        composable(NextAiRoutes.CAMERA_PREVIEW) {
            CameraPreviewScreen(
                state = state,
                onCycleFlash = viewModel::cycleFlashMode,
                onToggleZeroDce = viewModel::toggleZeroDceNightMode,
                onSelectZoom = viewModel::setLensZoom,
                onDragWatermark = viewModel::updateWatermarkDragOffset,
                onSelectMode = viewModel::selectCaptureMode,
                onCapturePhoto = viewModel::captureWatermarkedPhoto,
                onOpenWatermarkStore = { navController.navigate(NextAiRoutes.WATERMARK_STORE) },
                onOpenWatermarkEditor = { navController.navigate(NextAiRoutes.WATERMARK_EDITOR) },
                onOpenGalleryExport = { navController.navigate(NextAiRoutes.LOCAL_GALLERY_EXPORT) },
                onOpenTeamspace = { navController.navigate(NextAiRoutes.TEAMSPACE_HOME) },
                onOpenVerifyAuth = { navController.navigate(NextAiRoutes.VERIFY_AUTH) },
                onOpenPaywall = { navController.navigate(NextAiRoutes.PAYWALL) },
                onOpenEarnRobotics = { navController.navigate(NextAiRoutes.EARN_EMBODIED_AI) },
                onOpenAiEdgeCount = { navController.navigate(NextAiRoutes.AI_EDGE_COUNT) },
                onOpenSettings = { navController.navigate(NextAiRoutes.SETTINGS) }
            )
        }

        composable(NextAiRoutes.WATERMARK_STORE) {
            WatermarkStoreScreen(
                state = state,
                onSelectPreset = viewModel::selectWatermarkPreset,
                onEditPreset = { navController.navigate(NextAiRoutes.WATERMARK_EDITOR) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.WATERMARK_EDITOR) {
            WatermarkEditorScreen(
                state = state,
                onToggleField = viewModel::toggleWatermarkField,
                onUpdateFieldValue = viewModel::updateWatermarkFieldValue,
                onSelectSkinHex = viewModel::selectWatermarkSkinColor,
                onToggleLogoCutout = viewModel::toggleLogoCutout,
                onOpenLogoCutoutStudio = { navController.navigate(NextAiRoutes.AI_LOGO_CUTOUT) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.AI_LOGO_CUTOUT) {
            AiLogoCutoutScreen(
                state = state,
                onToggleLogoCutout = viewModel::toggleLogoCutout,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.LOCAL_GALLERY_EXPORT) {
            LocalGalleryAndExportScreen(
                state = state,
                onToggleSelectPhoto = viewModel::togglePhotoSelection,
                onUnpackCleanPhoto = viewModel::unpackRemoveWatermarkLossless,
                onExportBatch = viewModel::triggerBatchExport,
                onVerifyPhotoCode = { code ->
                    viewModel.runVerifyAuthAudit(code)
                    navController.navigate(NextAiRoutes.VERIFY_AUTH)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.TEAMSPACE_HOME) {
            TeamspaceHomeScreen(
                state = state,
                onOpenDedicatedGeofence = { navController.navigate(NextAiRoutes.GEOFENCE_ATTENDANCE) },
                onOpenDedicatedPhotoMap = { navController.navigate(NextAiRoutes.PHOTO_MAP_CLUSTER) },
                onPunchAttendancePhoto = {
                    viewModel.selectCaptureMode(CameraCaptureMode.ATTENDANCE)
                    viewModel.captureWatermarkedPhoto()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.GEOFENCE_ATTENDANCE) {
            GeofenceAttendanceScreen(
                state = state,
                onPunchClockWithCamera = {
                    viewModel.selectCaptureMode(CameraCaptureMode.ATTENDANCE)
                    viewModel.captureWatermarkedPhoto()
                    navController.navigate(NextAiRoutes.CAMERA_PREVIEW) {
                        popUpTo(NextAiRoutes.CAMERA_PREVIEW) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.PHOTO_MAP_CLUSTER) {
            PhotoMapClusterScreen(
                state = state,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.VERIFY_AUTH) {
            VerifyAuthScreen(
                state = state,
                onRunVerifyAudit = viewModel::runVerifyAuthAudit,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.PAYWALL) {
            PaywallScreen(
                state = state,
                onActivateVip = viewModel::activateNextAiPlusVip,
                onWatchRewardedAdFreePass = viewModel::grantRewardedAdFreePass,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.EARN_EMBODIED_AI) {
            EarnEmbodiedAiScreen(
                state = state,
                onToggleRecordMcap = viewModel::toggleMcapRecording,
                onExchangeVipDays = viewModel::activateNextAiPlusVip,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.AI_EDGE_COUNT) {
            AiEdgeCountScreen(
                state = state,
                onSelectVisionMode = viewModel::selectEdgeVisionMode,
                onApplyCountToWatermarkAndSnap = {
                    viewModel.captureWatermarkedPhoto()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NextAiRoutes.SETTINGS) {
            SettingsScreen(
                state = state,
                onSwitchEndpointProfile = viewModel::switchBackendProfile,
                onRunNetworkBenchmark = viewModel::runBackendBenchmark,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
