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

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository(application)

    private val _surahList = MutableStateFlow<List<Surah>>(emptyList())
    val surahList: StateFlow<List<Surah>> = _surahList.asStateFlow()

    private val _juzList = MutableStateFlow<List<Juz>>(emptyList())
    val juzList: StateFlow<List<Juz>> = _juzList.asStateFlow()

    init {
        loadSurahData()
        loadJuzData()
    }

    private fun loadSurahData() {
        viewModelScope.launch {
            _surahList.value = repository.getSurahList()
        }
    }

    private fun loadJuzData() {
        viewModelScope.launch {
            _juzList.value = repository.getJuzList()
        }
    }
}