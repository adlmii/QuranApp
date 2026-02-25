package com.example.quranapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quranapp.data.local.entity.SurahEntity

@Dao
interface SurahDao {

    @Query("SELECT * FROM surah ORDER BY id ASC")
    suspend fun getAll(): List<SurahEntity>

    @Query("SELECT * FROM surah WHERE id = :id")
    suspend fun getById(id: Int): SurahEntity?
}
