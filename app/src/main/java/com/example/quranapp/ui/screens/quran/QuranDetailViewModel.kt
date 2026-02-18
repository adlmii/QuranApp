package com.example.quranapp.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.model.SurahDetail
import com.example.quranapp.data.model.Ayah
import com.example.quranapp.data.repository.QuranRepository
import com.example.quranapp.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


data class QuranDetailUiState(
    val surahDetail: SurahDetail? = null,
    val pages: List<List<Ayah>> = emptyList(),
    val isPageMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val sessionProgress: Int = 0,
    val showReward: Boolean = false
)

class QuranDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepository(application)
    private val userPrefs = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(QuranDetailUiState())
    val uiState: StateFlow<QuranDetailUiState> = _uiState.asStateFlow()

    // Track surah info for scroll-based save
    private var currentSurahNumber: Int = 0
    private var currentSurahName: String = ""

    init {
        startSessionTimer()
    }

    private fun startSessionTimer() {
        viewModelScope.launch {
            while (true) {
                delay(60000) // 1 minute
                val newProgress = _uiState.value.sessionProgress + 1
                _uiState.value = _uiState.value.copy(sessionProgress = newProgress)

                // Persist to DataStore
                userPrefs.addMinute()
            }
        }
    }

    fun loadSurah(number: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val detail = repository.getSurahDetail(number)
                val pages = detail?.ayahs?.groupBy { it.page }?.values?.toList() ?: emptyList()
                
                if (detail != null) {
                    currentSurahNumber = detail.number
                    currentSurahName = detail.name

                    // Save initial last read position to Room
                    repository.saveLastRead(
                        surahNumber = detail.number,
                        ayahNumber = 1,
                        surahName = detail.name,
                        isPageMode = _uiState.value.isPageMode
                    )
                }

                _uiState.value = _uiState.value.copy(
                    surahDetail = detail,
                    pages = pages,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )
            }
        }
    }

    /**
     * Called from QuranDetailScreen scroll tracking.
     * Saves the current visible ayah number.
     */
    fun saveLastRead(ayahNumber: Int) {
        if (currentSurahNumber == 0) return
        viewModelScope.launch {
            repository.saveLastRead(
                surahNumber = currentSurahNumber,
                ayahNumber = ayahNumber,
                surahName = currentSurahName,
                isPageMode = _uiState.value.isPageMode
            )
        }
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isPageMode = !_uiState.value.isPageMode)
    }
}
