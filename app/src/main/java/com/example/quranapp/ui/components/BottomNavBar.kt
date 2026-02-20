package com.example.quranapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quranapp.R
import com.example.quranapp.ui.navigation.Screen
import com.example.quranapp.ui.theme.DeepEmerald
import com.example.quranapp.ui.theme.LightEmerald
import com.example.quranapp.ui.theme.White
import com.example.quranapp.ui.theme.GoldAccent

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Prayers,
        Screen.Quran,
        Screen.Qibla,
        Screen.Settings
    )

    // Add a shadow/elevation to the nav bar
    Surface(
        shadowElevation = 16.dp,
        color = Color.Transparent
    ) {
        NavigationBar(
            containerColor = DeepEmerald,
            contentColor = White,
            tonalElevation = 8.dp,
            windowInsets = NavigationBarDefaults.windowInsets
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val contentColor = if (isSelected) GoldAccent else White.copy(alpha = 0.6f)
                val titleResId = when (screen) {
                    Screen.Home -> R.string.nav_home
                    Screen.Prayers -> R.string.nav_prayers
                    Screen.Quran -> R.string.nav_quran
                    Screen.Qibla -> R.string.nav_qibla
                    Screen.Settings -> R.string.nav_settings
                    else -> R.string.nav_home
                }
                val titleText = stringResource(titleResId)

                NavigationBarItem(
                    icon = {
                        val iconSize = if (screen == Screen.Prayers) 22.dp else 24.dp
                        val modifier = if (screen == Screen.Prayers) {
                            Modifier.padding(top = 4.dp).size(iconSize)
                        } else {
                            Modifier.size(iconSize)
                        }

                        if (screen.iconRes != null) {
                            Icon(
                                painter = painterResource(id = screen.iconRes),
                                contentDescription = titleText,
                                tint = contentColor,
                                modifier = modifier
                            )
                        } else if (screen.icon != null) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = titleText,
                                tint = contentColor,
                                modifier = modifier
                            )
                        }
                    },
                    label = {
                        Text(
                            text = titleText,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = isSelected,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent, // No pill indicator
                        selectedIconColor = GoldAccent,
                        selectedTextColor = GoldAccent,
                        unselectedIconColor = White.copy(alpha = 0.6f),
                        unselectedTextColor = White.copy(alpha = 0.6f)
                    ),
                    alwaysShowLabel = true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}