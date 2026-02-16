package com.example.quranapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Prayers : Screen("prayers", "Prayers", Icons.Filled.Mosque)
    object Quran : Screen("quran", "Quran", Icons.Filled.MenuBook)
    object QuranDetail : Screen("quran_detail/{surahNumber}", "Detail", Icons.Filled.MenuBook)
    object AlMatsurat : Screen("almatsurat/{type}", "Al-Ma'tsurat", Icons.Filled.MenuBook)
    object Qibla : Screen("qibla", "Qibla", Icons.Filled.Explore)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}