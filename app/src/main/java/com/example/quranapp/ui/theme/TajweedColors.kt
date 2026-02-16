package com.example.quranapp.ui.theme

import androidx.compose.ui.graphics.Color

object TajweedColors {
    val TextBlack = Color.Black

    // Palette Khusus (Tema Soft/Material)
    object Palette {
        val SoftGreen = Color(0xFF66BB6A)   // Mad Jaiz / Ghunnah
        val SoftBlue = Color(0xFF42A5F5)    // Qalqalah
        val SoftRed = Color(0xFFEF5350)     // Mad / Allah
        val SoftPurple = Color(0xFFAB47BC)  // Mad Wajib
        val SoftOrange = Color(0xFFFF7043)  // Ghunnah / Ikhfa
        val SoftGray = Color(0xFFBDBDBD)    // Idgham / Silent
        val DeepRed = Color(0xFFC62828)     // Lafadz Allah (Tebal)
        val DeepTeal = Color(0xFF00796B)    // Iqlab / Idgham Bighunnah
    }

    // Mapping Implementation
    // [n] = Gunnah (Dengung) -> Orange Soft
    val Ghunnah = Palette.SoftOrange 

    // [p] = Qalqalah (Pantulan) -> Blue Soft
    val Qalqalah = Palette.SoftBlue

    // [h:1] = Lafadz Allah -> Deep Red
    val Allah = Palette.DeepRed

    // [h:2, h:3] = Mad Wajib (Panjang) -> Purple
    val MadWajib = Palette.SoftPurple

    // [h:4, h:5] = Mad Jaiz (Sedang) -> Green
    val MadJaiz = Palette.SoftGreen

    // [f] = Ikhfa (Samar) -> Deep Teal (Contrast with Orange) or Red
    val Ikhfa = Palette.DeepTeal
    
    // Default
    val DefaultHighlight = Palette.SoftBlue
}
