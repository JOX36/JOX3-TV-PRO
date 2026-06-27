package com.streamvault.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = DarkBackground,
    primaryContainer = CyanDark,
    onPrimaryContainer = CyanLight,
    secondary = ElectricBlue,
    onSecondary = DarkBackground,
    secondaryContainer = ElectricBlueDark,
    onSecondaryContainer = ElectricBlueLight,
    tertiary = NeonCyan,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF1A3A5C),
    error = ErrorRed,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDark,
    onPrimary = Color.White,
    primaryContainer = CyanLight,
    onPrimaryContainer = DarkBackground,
    secondary = ElectricBlueDark,
    onSecondary = Color.White,
    secondaryContainer = ElectricBlueLight,
    onSecondaryContainer = DarkBackground,
    background = Color(0xFFF5F9FF),
    onBackground = Color(0xFF0A1628),
    surface = Color.White,
    onSurface = Color(0xFF0A1628),
    surfaceVariant = Color(0xFFE8EEF5),
    onSurfaceVariant = Color(0xFF455A64),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun JOX3TVTheme(
    darkTheme: Boolean = true, // Default dark for IPTV app
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StreamVaultTypography,
        shapes = StreamVaultShapes,
        content = content
    )
}
