package com.example.quranapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surah")
data class SurahEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name_simple") val nameSimple: String,
    @ColumnInfo(name = "name_arabic") val nameArabic: String,
    @ColumnInfo(name = "revelation_place") val revelationPlace: String,
    @ColumnInfo(name = "verses_count") val versesCount: Int
)
