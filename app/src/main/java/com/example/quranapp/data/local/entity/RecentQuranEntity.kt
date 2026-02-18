package com.example.quranapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_quran")
data class RecentQuranEntity(
    @PrimaryKey val id: Int = 1,  // Selalu timpa baris yang sama
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val isPageMode: Boolean = false,
    val pageNumber: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
