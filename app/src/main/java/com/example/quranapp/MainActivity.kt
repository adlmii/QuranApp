package com.example.quranapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.quranapp.data.local.UserPreferencesRepository
import com.example.quranapp.ui.navigation.AppNavigation
import com.example.quranapp.ui.theme.QuranAppTheme
import com.example.quranapp.util.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = UserPreferencesRepository(newBase)
        val lang = runBlocking { prefs.language.first() }
        super.attachBaseContext(LocaleHelper.wrapContext(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userPrefs = UserPreferencesRepository(this)
        
        setContent {
            var themeMode by remember { mutableStateOf(0) }
            val systemDarkTheme = isSystemInDarkTheme()
            
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    themeMode = userPrefs.themeMode.first()
                }
            }
            
            // Observe theme mode changes reactively
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    userPrefs.themeMode.collect { mode ->
                        themeMode = mode
                    }
                }
            }
            
            val isDarkTheme = when (themeMode) {
                1 -> false // Light
                2 -> true  // Dark
                else -> systemDarkTheme // System
            }
            
            QuranAppTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QuranAppTheme {
        Greeting("Android")
    }
}