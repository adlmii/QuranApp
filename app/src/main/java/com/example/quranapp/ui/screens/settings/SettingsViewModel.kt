package com.example.quranapp.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val targetMinutes: Int = 5,
    val themeMode: Int = 0, // 0: System, 1: Light, 2: Dark
    val languageCode: String = "" // "" | "in" | "en"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userPrefs = UserPreferencesRepository(application)
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeTarget()
        observeThemeMode()
        observeLanguage()
    }

    private fun observeTarget() {
        viewModelScope.launch {
            userPrefs.targetMinutes.collect { minutes ->
                _uiState.value = _uiState.value.copy(targetMinutes = minutes)
            }
        }
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            userPrefs.themeMode.collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            userPrefs.language.collect { code ->
                _uiState.value = _uiState.value.copy(languageCode = code)
            }
        }
    }

    fun setTarget(minutes: Int) {
        viewModelScope.launch {
            userPrefs.setTarget(minutes)
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            userPrefs.saveThemeMode(mode)
        }
    }

    fun setLanguage(code: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            userPrefs.saveLanguage(code)
            onSaved()
        }
    }
}
