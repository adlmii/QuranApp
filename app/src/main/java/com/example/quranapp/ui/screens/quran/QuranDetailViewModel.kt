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
    val targetMinutes: Int = 5,
    val showReward: Boolean = false,
    // Bookmark
    val bookmarkSurah: Int = 0,
    val bookmarkAyah: Int = 0,
    // Font size
    val arabicFontSize: Float = 28f,
    // Adjacent surah names for flow navigation
    val nextSurahName: String? = null,
    val prevSurahName: String? = null
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
        observeProgress()
        observeBookmark()
        observeFontSize()
        startSessionTimer()
    }

    private fun observeProgress() {
        viewModelScope.launch {
            userPrefs.todayMinutes.collect { minutes ->
                _uiState.value = _uiState.value.copy(sessionProgress = minutes)
            }
        }
        viewModelScope.launch {
            userPrefs.targetMinutes.collect { target ->
                _uiState.value = _uiState.value.copy(targetMinutes = target)
            }
        }
    }

    private fun observeBookmark() {
        viewModelScope.launch {
            repository.getBookmarkFlow().collect { bookmark ->
                _uiState.value = _uiState.value.copy(
                    bookmarkSurah = bookmark?.surahNumber ?: 0,
                    bookmarkAyah = bookmark?.ayahNumber ?: 0
                )
            }
        }
    }

    private fun observeFontSize() {
        viewModelScope.launch {
            userPrefs.arabicFontSize.collect { size ->
                _uiState.value = _uiState.value.copy(arabicFontSize = size)
            }
        }
    }

    private fun startSessionTimer() {
        viewModelScope.launch {
            while (true) {
                delay(60000) // 1 minute
                // Persist to DataStore (observer will update sessionProgress)
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

                // Load adjacent surah names for flow navigation
                val nextName = if (number < 114) repository.getSurahNameByNumber(number + 1) else null
                val prevName = if (number > 1) repository.getSurahNameByNumber(number - 1) else null

                _uiState.value = _uiState.value.copy(
                    surahDetail = detail,
                    pages = pages,
                    isLoading = false,
                    nextSurahName = nextName,
                    prevSurahName = prevName
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

    fun saveBookmark(ayahNumber: Int) {
        if (currentSurahNumber == 0) return
        viewModelScope.launch {
            repository.saveBookmark(
                surahNumber = currentSurahNumber,
                ayahNumber = ayahNumber,
                surahName = currentSurahName
            )
        }
    }

    fun updateFontSize(size: Float) {
        viewModelScope.launch {
            userPrefs.saveArabicFontSize(size)
        }
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isPageMode = !_uiState.value.isPageMode)
    }
}
