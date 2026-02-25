package com.example.quranapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ayah",
    indices = [
        Index(value = ["surah_id"]),
        Index(value = ["page_number"]),
        Index(value = ["juz_number"])
    ]
)
data class AyahEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "surah_id") val surahId: Int,
    @ColumnInfo(name = "verse_number") val verseNumber: Int,
    @ColumnInfo(name = "page_number") val pageNumber: Int,
    @ColumnInfo(name = "juz_number") val juzNumber: Int,
    @ColumnInfo(name = "text_uthmani") val textUthmani: String,
    @ColumnInfo(name = "translation_id") val translationId: String
)
