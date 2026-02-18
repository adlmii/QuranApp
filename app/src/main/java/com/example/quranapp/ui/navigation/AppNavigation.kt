package com.example.quranapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quranapp.ui.components.BottomNavBar
import com.example.quranapp.ui.screens.home.HomeScreen
import com.example.quranapp.ui.screens.prayer.PrayerScreen
import com.example.quranapp.ui.screens.quran.QuranScreen
import com.example.quranapp.ui.screens.quran.QuranDetailScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Cek rute saat ini
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show BottomBar only on top-level screens
    val topLevelRoutes = listOf(
        Screen.Home.route,
        Screen.Prayers.route,
        Screen.Quran.route,
        Screen.Qibla.route,
        Screen.Settings.route
    )
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    onNavigateToMatsurat = { type ->
                         navController.navigate(Screen.AlMatsurat.route.replace("{type}", type))
                    },
                    onNavigateToDetail = { surahNumber, ayahNumber ->
                        navController.navigate("quran_detail/$surahNumber?ayahNumber=$ayahNumber")
                    },
                    onNavigateToCalendar = {
                        navController.navigate(Screen.Calendar.route)
                    }
                ) 
            }
            composable(Screen.Prayers.route) { PrayerScreen() }
            composable(Screen.Quran.route) {
                QuranScreen(navController = navController)
            }
            composable(
                route = Screen.QuranDetail.route,
                arguments = listOf(
                    navArgument("surahNumber") { type = NavType.IntType },
                    navArgument("ayahNumber") { type = NavType.IntType; defaultValue = 1 }
                )
            ) { backStackEntry ->
                val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                val ayahNumber = backStackEntry.arguments?.getInt("ayahNumber") ?: 1
                QuranDetailScreen(navController = navController, surahNumber = surahNumber, initialAyah = ayahNumber)
            }
            composable(
                route = Screen.AlMatsurat.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val typeStr = backStackEntry.arguments?.getString("type") ?: "MORNING"
                val type = try {
                    com.example.quranapp.data.model.MatsuratType.valueOf(typeStr)
                } catch (e: Exception) {
                    com.example.quranapp.data.model.MatsuratType.MORNING
                }
                com.example.quranapp.ui.screens.almatsurat.AlMatsuratScreen(navController = navController, type = type)
            }
            composable(Screen.Qibla.route) { 
                com.example.quranapp.ui.screens.qibla.QiblaScreen(navController = navController)
            }
            composable(Screen.Calendar.route) {
                com.example.quranapp.ui.screens.calendar.CalendarScreen(navController = navController)
            }
            composable(Screen.Settings.route) { androidx.compose.material3.Text("Settings") }
        }
    }
}