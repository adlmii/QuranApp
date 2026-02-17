package com.example.quranapp.ui.theme

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
    onPrimary = White, // Teks di atas warna primary (misal: teks di tombol login)

    secondary = LightEmerald,
    onSecondary = DeepEmerald, // Teks di atas warna secondary

    background = CreamBackground,
    onBackground = TextBlack,

    surface = White, // Warna kartu/card
    onSurface = TextBlack
)

@Composable
fun QuranAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color dimatikan dulu agar warna Deep Emerald kita tidak tertimpa warna sistem Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    // Note: Nanti bisa kita tambahkan logika DarkMode di sini jika perlu

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Mengubah warna Status Bar agar selaras dengan background Cream
            window.statusBarColor = CreamBackground.toArgb()
            // Mengatur ikon status bar menjadi gelap (karena backgroundnya terang/cream)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}