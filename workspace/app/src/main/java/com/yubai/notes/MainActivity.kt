package com.yubai.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yubai.notes.ui.NotesViewModel
import com.yubai.notes.ui.YuBaiApp
import com.yubai.notes.ui.theme.YuBaiTheme
import com.yubai.notes.ui.theme.isDarkTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = isDarkTheme(themeMode)

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
                window.isNavigationBarContrastEnforced = false
            }

            YuBaiTheme(themeMode = themeMode) {
                YuBaiApp(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        viewModel.flushEditor()
        super.onStop()
    }
}
