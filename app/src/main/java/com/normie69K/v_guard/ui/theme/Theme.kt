package com.normie69K.v_guard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VGuardDarkColorScheme = darkColorScheme(
    primary              = VGuardBlue,
    onPrimary            = Color.White,
    primaryContainer     = VGuardBlueDark,
    onPrimaryContainer   = VGuardBlueLight,
    secondary            = SafeGreenLight,
    onSecondary          = Color.White,
    tertiary             = WarningAmber,
    background           = DarkBg,
    surface              = DarkSurface,
    surfaceVariant       = DarkCard,
    onBackground         = Color.White,
    onSurface            = Color.White,
    onSurfaceVariant     = SubtleText,
    outline              = DarkOutline,
    outlineVariant       = Color(0xFF2C2C2C),
    error                = EmergencyRed,
    errorContainer       = Color(0xFF3B0000),
    onError              = Color.White,
    onErrorContainer     = EmergencyRedLight
)

private val VGuardLightColorScheme = lightColorScheme(
    primary              = VGuardBlue,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFDDE8FF),
    onPrimaryContainer   = VGuardBlueDark,
    secondary            = SafeGreen,
    onSecondary          = Color.White,
    tertiary             = WarningAmber,
    background           = Color(0xFFF2F4F8),
    surface              = Color.White,
    surfaceVariant       = Color(0xFFECEFF5),
    onBackground         = Color(0xFF1A1A1A),
    onSurface            = Color(0xFF1A1A1A),
    onSurfaceVariant     = Color(0xFF555555),
    outline              = Color(0xFFCCCCCC),
    error                = EmergencyRed,
    errorContainer       = Color(0xFFFFDAD6),
    onError              = Color.White,
    onErrorContainer     = Color(0xFF410002)
)

@Composable
fun VguardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VGuardDarkColorScheme else VGuardLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // 1. Make the status and navigation bars completely transparent
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            // 2. Ensure icons (battery, wifi, etc.) are visible against the background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}