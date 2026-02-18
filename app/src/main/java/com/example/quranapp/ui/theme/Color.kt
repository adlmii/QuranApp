package com.example.quranapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────
// Primary Colors (Deep Emerald & Teal)
// ─────────────────────────────────────────────
val DeepEmerald = Color(0xFF004D40)
val DeepEmeraldDark = Color(0xFF00251A)
val MediumEmerald = Color(0xFF4DB6AC)
val LightEmerald = Color(0xFFE0F2F1)
val BrightEmerald = Color(0xFF26A69A)
val EmeraldText = Color(0xFF00695C)
val DeepEmeraldHeader = Color(0xFF005C4B) // Matches gradient start

// ─────────────────────────────────────────────
// Accents (Gold & Sand)
// ─────────────────────────────────────────────
val GoldAccent = Color(0xFFD4AF37)
val GoldLight = Color(0xFFFFE082)
val SandBackground = Color(0xFFFDFBF7)

// ─────────────────────────────────────────────
// Neutrals
// ─────────────────────────────────────────────
val CreamBackground = Color(0xFFFAF8EF)
val White = Color(0xFFFFFFFF)
val TextBlack = Color(0xFF212121)
val TextGray = Color(0xFF757575)
val DividerColor = Color(0xFFEEEEEE)

// ─────────────────────────────────────────────
// Gradients
// ─────────────────────────────────────────────
val DeepEmeraldGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF005C4B), Color(0xFF00251A))
)

val GoldenHourGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFD54F), Color(0xFFF57F17))
)

val SoftSurfaceGradient = Brush.verticalGradient(
    colors = listOf(White, Color(0xFFF5F5F5))
)

val MidnightGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
)

val SageGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4da0b0), Color(0xFFd39d38)) // Artistic teal to gold
)

// Qibla specific
val QiblaActive = GoldAccent
val CompassShadow = Color(0x40000000)