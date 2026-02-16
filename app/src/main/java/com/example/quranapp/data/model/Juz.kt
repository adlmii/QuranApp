package com.example.quranapp.data.model

data class JuzSurahEntry(
    val surahNumber: Int,
    val surahName: String,
    val arabicName: String,
    val ayahRange: String // e.g. "1-141" or "All"
)

data class Juz(
    val number: Int,
    val surahs: List<JuzSurahEntry>
)
