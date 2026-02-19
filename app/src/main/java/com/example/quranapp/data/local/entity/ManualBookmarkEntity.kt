package com.example.quranapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manual_bookmark")
data class ManualBookmarkEntity(
    @PrimaryKey val id: Int = 1,  // Singleton: selalu timpa baris yang sama
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val timestamp: Long = System.currentTimeMillis()
)
