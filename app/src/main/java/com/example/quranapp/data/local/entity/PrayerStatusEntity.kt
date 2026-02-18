package com.example.quranapp.data.local.entity

import androidx.room.Entity

@Entity(tableName = "prayer_status", primaryKeys = ["date", "prayerName"])
data class PrayerStatusEntity(
    val date: String,        // Format: "yyyy-MM-dd"
    val prayerName: String,  // "Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"
    val isPrayed: Boolean = false
)
