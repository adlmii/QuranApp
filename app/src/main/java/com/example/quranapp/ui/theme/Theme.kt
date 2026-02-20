package com.example.quranapp.ui.theme

import androidx.compose.ui.graphics.Color

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.quranapp.ui.theme.CreamBackground

private val LightColorScheme = lightColorScheme(
    primary = DeepEmerald,
    onPrimary = White,
    primaryContainer = LightEmerald,
    onPrimaryContainer = DeepEmeraldDark,

    secondary = GoldAccent,
    onSecondary = White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = Color(0xFF5D4037),

    tertiary = MediumEmerald,
    onTertiary = White,

    background = CreamBackground,
    onBackground = TextBlack,

    surface = White,
    onSurface = TextBlack,
    surfaceVariant = SandBackground,
    onSurfaceVariant = TextGray
)

private val DarkColorScheme = darkColorScheme(
    primary = MediumEmerald,
    onPrimary = DeepEmeraldDark,
    primaryContainer = DeepEmerald,
    onPrimaryContainer = LightEmerald,

    secondary = GoldAccent,
    onSecondary = DeepEmeraldDark,
    secondaryContainer = Color(0xFF8D6E63),
    onSecondaryContainer = GoldLight,

    tertiary = BrightEmerald,
    onTertiary = DeepEmeraldDark,

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0)
)

@Composable
fun QuranAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color dimatikan dulu agar warna Deep Emerald kita tidak tertimpa warna sistem Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            if (darkTheme) {
                window.statusBarColor = Color(0xFF121212).toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            } else {
                window.statusBarColor = CreamBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun SetStatusBarColor(color: Color) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = color.toArgb()
            
            // If color is DeepEmerald (dark), use light icons (isAppearanceLightStatusBars = false).
            val isLight = androidx.core.graphics.ColorUtils.calculateLuminance(color.toArgb()) > 0.5
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
        }
    }
}