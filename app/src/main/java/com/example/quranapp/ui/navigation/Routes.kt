package com.example.quranapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

import com.example.quranapp.R

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null, val iconRes: Int? = null) {
    object Home : Screen("home", "Home", icon = Icons.Filled.Home)
    object Prayers : Screen("prayers", "Prayers", iconRes = R.drawable.ic_prayers_custom)
    object Quran : Screen("quran", "Quran", icon = Icons.Filled.MenuBook)
    object QuranDetail : Screen("quran_detail/{surahNumber}?ayahNumber={ayahNumber}", "Detail", icon = Icons.Filled.MenuBook)
    object AlMatsurat : Screen("almatsurat/{type}", "Al-Ma'tsurat", icon = Icons.Filled.MenuBook)
    object Qibla : Screen("qibla", "Qibla", icon = Icons.Filled.Explore)
    object Calendar : Screen("calendar", "Calendar", icon = Icons.Filled.DateRange)
    object Settings : Screen("settings", "Settings", icon = Icons.Filled.Settings)
}