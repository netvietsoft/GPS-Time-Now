package com.nextai.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nextai.camera.ui.navigation.NextAiNavHost
import com.nextai.camera.ui.theme.NextAiCameraTheme
import com.nextai.camera.ui.theme.NextAiDarkBackground

/**
 * Main Launcher Activity (`com.nextai.camera.MainActivity`).
 * Hosts the Camera-First Jetpack Compose navigation graph (`NextAiNavHost`) across all 12 core screens.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NextAiCameraTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = NextAiDarkBackground
                ) {
                    NextAiNavHost()
                }
            }
        }
    }
}
