package com.example.quranapp.data.model

data class Ayah(
    val number: Int,
    val arabic: String,
    val translation: String,
    val page: Int = 0,
    val juz: Int = 0,
    val manzil: Int = 0,
    val hizbQuarter: Int = 0
)

data class SurahDetail(
    val number: Int,
    val name: String, // Transliteration (Latin)
    val arabicName: String,
    val englishName: String, // Translation (Meaning)
    val ayahCount: Int,
    val type: String, // meccan/medinan
    val ayahs: List<Ayah>
)
