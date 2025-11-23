package com.suplz.avitoassignment.presentation.util

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
fun ForceStatusBarAppearance(isDarkTheme: Boolean) {
    val context = LocalContext.current
    val systemIsDark = isSystemInDarkTheme()

    DisposableEffect(isDarkTheme) {
        val window = (context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)


        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme

        onDispose {
            insetsController.isAppearanceLightStatusBars = !systemIsDark
            insetsController.isAppearanceLightNavigationBars = !systemIsDark
        }
    }
}