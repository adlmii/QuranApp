package com.example.quranapp.ui.screens.quran

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.local.entity.AyahEntity
import com.example.quranapp.data.local.entity.AyahWithSurahName
import com.example.quranapp.data.model.AyahSearchResult
import com.example.quranapp.data.model.Juz
import com.example.quranapp.data.model.JuzSurahEntry
import com.example.quranapp.data.model.Surah
import com.example.quranapp.data.repository.QuranRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap


data class QuranHomeUiState(
    val surahList: List<Surah> = emptyList(),
    val filteredSurahList: List<Surah> = emptyList(),
    val juzList: List<Juz> = emptyList(),
    val searchQuery: String = "",
    // Smart Jump
    val smartJumpSurah: Int? = null,
    val smartJumpAyah: Int? = null,
    val smartJumpSurahName: String? = null,
    // Ayah text search
    val ayahSearchResults: List<AyahSearchResult> = emptyList(),
    val isSearchingAyahs: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Bookmark
    val bookmarkSurah: Int = 0,
    val bookmarkAyah: Int = 0
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository(application)

    private val _uiState = MutableStateFlow(QuranHomeUiState())
    val uiState: StateFlow<QuranHomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadData()
        // Populate FTS search index in background (no-op if already populated)
        viewModelScope.launch {
            repository.populateSearchIndex()
        }
        observeBookmark()
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

    // ── Mode Halaman (Mushaf Page View) ──

    // Cache StateFlows per page to prevent recreation on recomposition (prevents flicker)
    private val pageDataCache = ConcurrentHashMap<Int, StateFlow<List<AyahWithSurahName>>>()

    /**
     * Get all ayahs on a specific Quran page as a StateFlow.
     * Used by QuranMushafScreen's HorizontalPager.
     */
    fun getPageData(pageNumber: Int): StateFlow<List<AyahWithSurahName>> {
        return pageDataCache.getOrPut(pageNumber) {
            repository.getAyahsByPage(pageNumber)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }
    }

    fun saveBookmark(surahNumber: Int, ayahNumber: Int, surahName: String) {
        viewModelScope.launch {
            repository.saveBookmark(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName
            )
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val surahs = repository.getSurahList()
                val juzs = repository.getJuzList()
                _uiState.value = _uiState.value.copy(
                    surahList = surahs,
                    filteredSurahList = surahs,
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

    fun onSearchQueryChange(query: String) {
        val currentList = _uiState.value.surahList

        // Cancel any pending search
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                filteredSurahList = currentList,
                smartJumpSurah = null,
                smartJumpAyah = null,
                smartJumpSurahName = null,
                ayahSearchResults = emptyList(),
                isSearchingAyahs = false
            )
            return
        }

        // ── Smart Jump: detect "surah:ayah" pattern ──
        if (query.contains(":")) {
            val parts = query.split(":")
            val surahNum = parts.getOrNull(0)?.trim()?.toIntOrNull()
            val ayahNum = parts.getOrNull(1)?.trim()?.toIntOrNull()

            if (surahNum != null && ayahNum != null && surahNum in 1..114) {
                val surah = currentList.find { it.number == surahNum }
                if (surah != null && ayahNum in 1..surah.ayahCount) {
                    _uiState.value = _uiState.value.copy(
                        searchQuery = query,
                        smartJumpSurah = surahNum,
                        smartJumpAyah = ayahNum,
                        smartJumpSurahName = surah.name,
                        filteredSurahList = emptyList(),
                        ayahSearchResults = emptyList(),
                        isSearchingAyahs = false
                    )
                    return
                }
            }

            // Invalid pattern — clear smart jump
            _uiState.value = _uiState.value.copy(
                searchQuery = query,
                smartJumpSurah = null,
                smartJumpAyah = null,
                smartJumpSurahName = null,
                filteredSurahList = emptyList(),
                ayahSearchResults = emptyList(),
                isSearchingAyahs = false
            )
            return
        }

        // ── Surah name filter (always instant) ──
        val filtered = currentList.filter { surah ->
            surah.name.contains(query, ignoreCase = true) ||
            surah.englishName.contains(query, ignoreCase = true) ||
            surah.number.toString().contains(query)
        }

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredSurahList = filtered,
            smartJumpSurah = null,
            smartJumpAyah = null,
            smartJumpSurahName = null,
            ayahSearchResults = if (query.length < 3) emptyList() else _uiState.value.ayahSearchResults,
            isSearchingAyahs = query.length >= 3
        )

        // ── Ayah text search (debounced, min 3 chars) ──
        if (query.length >= 3) {
            searchJob = viewModelScope.launch {
                delay(400) // debounce 400ms
                try {
                    val results = repository.searchAyahs(query)
                    _uiState.value = _uiState.value.copy(
                        ayahSearchResults = results,
                        isSearchingAyahs = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        ayahSearchResults = emptyList(),
                        isSearchingAyahs = false
                    )
                }
            }
        }
    }
}
