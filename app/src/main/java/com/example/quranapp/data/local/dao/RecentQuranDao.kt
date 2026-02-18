package com.example.quranapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quranapp.data.local.entity.RecentQuranEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentQuranDao {
    @Query("SELECT * FROM recent_quran WHERE id = 1")
    fun getLastRead(): Flow<RecentQuranEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLastRead(entity: RecentQuranEntity)
}
