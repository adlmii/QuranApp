package com.example.quranapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quranapp.data.local.entity.ManualBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ManualBookmarkDao {
    @Query("SELECT * FROM manual_bookmark WHERE id = 1")
    fun getBookmark(): Flow<ManualBookmarkEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBookmark(entity: ManualBookmarkEntity)
}
