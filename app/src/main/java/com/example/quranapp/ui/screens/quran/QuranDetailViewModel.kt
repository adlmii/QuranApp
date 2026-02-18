package com.example.quranapp.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.model.SurahDetail
import com.example.quranapp.data.model.Ayah
import com.example.quranapp.data.repository.QuranRepository
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

    private val _uiState = MutableStateFlow(QuranDetailUiState())
    val uiState: StateFlow<QuranDetailUiState> = _uiState.asStateFlow()

    init {
        startSessionTimer()
    }

    private fun startSessionTimer() {
        viewModelScope.launch {
            while (_uiState.value.sessionProgress < 5) {
                delay(60000) // 1 minute
                val newProgress = _uiState.value.sessionProgress + 1
                _uiState.value = _uiState.value.copy(sessionProgress = newProgress)
                
                if (newProgress >= 5) {
                    // Reached goal, no animation
                }
            }
        }
    }

    fun loadSurah(number: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val detail = repository.getSurahDetail(number)
                val pages = detail?.ayahs?.groupBy { it.page }?.values?.toList() ?: emptyList()
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

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isPageMode = !_uiState.value.isPageMode)
    }
}

