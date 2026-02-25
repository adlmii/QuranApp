package com.example.quranapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quranapp.data.local.entity.AyahEntity
import com.example.quranapp.data.local.entity.AyahWithSurahName
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    /** Mode Per Ayat: get all ayahs for a surah, ordered by verse number */
    @Query("SELECT * FROM ayah WHERE surah_id = :surahId ORDER BY verse_number ASC")
    suspend fun getAyahsBySurah(surahId: Int): List<AyahEntity>

    /** Mode Per Halaman (Mushaf View): get all ayahs on a specific page with Surah metadata */
    @Query("""
        SELECT a.*, s.name_arabic, s.name_simple 
        FROM ayah a 
        INNER JOIN surah s ON a.surah_id = s.id 
        WHERE a.page_number = :pageNumber 
        ORDER BY a.surah_id ASC, a.verse_number ASC
    """)
    fun getAyahsByPage(pageNumber: Int): Flow<List<AyahWithSurahName>>

    /** Get all ayahs in a specific juz */
    @Query("SELECT * FROM ayah WHERE juz_number = :juzNumber ORDER BY surah_id ASC, verse_number ASC")
    suspend fun getAyahsByJuz(juzNumber: Int): List<AyahEntity>

    /** Get all distinct juz numbers (1-30) */
    @Query("SELECT DISTINCT juz_number FROM ayah ORDER BY juz_number ASC")
    suspend fun getDistinctJuzNumbers(): List<Int>

    /** Get the first ayah of each surah in a given juz (for building juz-surah entry list) */
    @Query("""
        SELECT a.* FROM ayah a
        INNER JOIN (
            SELECT surah_id, MIN(verse_number) as min_verse
            FROM ayah WHERE juz_number = :juzNumber
            GROUP BY surah_id
        ) b ON a.surah_id = b.surah_id AND a.verse_number = b.min_verse
        WHERE a.juz_number = :juzNumber
        ORDER BY a.surah_id ASC
    """)
    suspend fun getFirstAyahPerSurahInJuz(juzNumber: Int): List<AyahEntity>

    /** Get the last ayah of each surah in a given juz */
    @Query("""
        SELECT a.* FROM ayah a
        INNER JOIN (
            SELECT surah_id, MAX(verse_number) as max_verse
            FROM ayah WHERE juz_number = :juzNumber
            GROUP BY surah_id
        ) b ON a.surah_id = b.surah_id AND a.verse_number = b.max_verse
        WHERE a.juz_number = :juzNumber
        ORDER BY a.surah_id ASC
    """)
    suspend fun getLastAyahPerSurahInJuz(juzNumber: Int): List<AyahEntity>

    /** Total ayah count (for verification) */
    @Query("SELECT COUNT(*) FROM ayah")
    suspend fun count(): Int
}
