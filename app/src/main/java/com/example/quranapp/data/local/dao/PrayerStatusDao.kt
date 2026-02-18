package com.example.quranapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quranapp.data.local.entity.PrayerStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerStatusDao {
    @Query("SELECT * FROM prayer_status WHERE date = :date")
    fun getPrayerStatusByDate(date: String): Flow<List<PrayerStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrayerStatus(entity: PrayerStatusEntity)

    @Query("UPDATE prayer_status SET isPrayed = 1 WHERE date = :date")
    suspend fun markAllPrayed(date: String)
}
