package com.example.quranapp.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.model.Juz
import com.example.quranapp.data.model.JuzSurahEntry
import com.example.quranapp.data.model.Surah
import com.example.quranapp.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class QuranHomeUiState(
    val surahList: List<Surah> = emptyList(),
    val juzList: List<Juz> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository(application)

    private val _uiState = MutableStateFlow(QuranHomeUiState())
    val uiState: StateFlow<QuranHomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Load both in parallel if needed, or sequentially
                val surahs = repository.getSurahList()
                val juzs = repository.getJuzList()
                _uiState.value = _uiState.value.copy(
                    surahList = surahs,
                    juzList = juzs,
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
}
