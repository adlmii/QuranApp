package com.example.quranapp.data.model

data class LastReadData(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String, // Latin
    val surahArabicName: String,
    val surahEnglishName: String
)
