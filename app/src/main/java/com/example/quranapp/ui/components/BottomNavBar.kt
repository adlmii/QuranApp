package com.example.quranapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quranapp.ui.navigation.Screen
import com.example.quranapp.ui.theme.DeepEmerald
import com.example.quranapp.ui.theme.LightEmerald
import com.example.quranapp.ui.theme.White
import androidx.compose.ui.res.painterResource

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Prayers,
        Screen.Quran,
        Screen.Qibla,
        Screen.Settings
    )

    NavigationBar(
        containerColor = DeepEmerald,
        contentColor = White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val contentColor = if (isSelected) LightEmerald else White.copy(alpha = 0.5f)

            NavigationBarItem(

                icon = {
                    val iconSize = if (screen == Screen.Prayers) 22.dp else 26.dp
                    // Shift Prayer icon down slightly to center it visually if the custom asset feels "high"
                    val modifier = if (screen == Screen.Prayers) {
                        Modifier.padding(top = 4.dp).size(iconSize)
                    } else {
                        Modifier.size(iconSize)
                    }

                    if (screen.iconRes != null) {
                        Icon(
                            painter = painterResource(id = screen.iconRes),
                            contentDescription = screen.title,
                            tint = contentColor,
                            modifier = modifier
                        )
                    } else if (screen.icon != null) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = contentColor,
                            modifier = modifier
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.title,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = contentColor
                    )
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = LightEmerald,
                    selectedTextColor = LightEmerald,
                    unselectedIconColor = White.copy(alpha = 0.5f),
                    unselectedTextColor = White.copy(alpha = 0.5f)
                ),
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