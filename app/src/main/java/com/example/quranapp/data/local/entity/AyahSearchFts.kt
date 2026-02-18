package com.example.quranapp.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "ayah_search_fts")
data class AyahSearchFts(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val textTranslation: String
)
