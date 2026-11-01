package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkRydGreenPrimary,
    onPrimary = Color(0xFF02361D),
    primaryContainer = RydGreenDark,
    onPrimaryContainer = RydGreenContainer,
    secondary = RydGoldLight,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = RydGold,
    background = DarkRydSurface,
    surface = DarkRydSurfaceCard,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E2E4A),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkRydBorder
)

private val LightColorScheme = lightColorScheme(
    primary = RydGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = RydGreenContainer,
    onPrimaryContainer = RydOnGreenContainer,
    secondary = RydGold,
    onSecondary = Color.White,
    secondaryContainer = RydGoldContainer,
    onSecondaryContainer = RydOnGoldContainer,
    background = RydSurfaceLight,
    surface = RydSurfaceCard,
    onBackground = RydTextPrimary,
    onSurface = RydTextPrimary,
    surfaceVariant = RydSurfaceVariant,
    onSurfaceVariant = RydTextSecondary,
    outline = RydOutline,
    error = RydError,
    onError = Color.White
)

@Composable
fun RydRoverTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
