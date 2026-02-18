package com.example.quranapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.quranapp.data.local.entity.AyahSearchFts

@Dao
interface AyahSearchDao {

    @Query("SELECT *, rowid FROM ayah_search_fts WHERE ayah_search_fts MATCH :query LIMIT 50")
    suspend fun search(query: String): List<AyahSearchFts>

    @Insert
    suspend fun insertAll(entries: List<AyahSearchFts>)

    @Query("SELECT COUNT(*) FROM ayah_search_fts")
    suspend fun count(): Int
}
