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

class QuranDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepository(application)

    private val _surahDetail = MutableStateFlow<SurahDetail?>(null)
    val surahDetail: StateFlow<SurahDetail?> = _surahDetail.asStateFlow()

    private val _pages = MutableStateFlow<List<List<Ayah>>>(emptyList())
    val pages: StateFlow<List<List<Ayah>>> = _pages.asStateFlow()

    fun loadSurah(number: Int) {
        viewModelScope.launch {
            val detail = repository.getSurahDetail(number)
            _surahDetail.value = detail
            _pages.value = detail?.ayahs?.groupBy { it.page }?.values?.toList() ?: emptyList()
        }
    }
}
