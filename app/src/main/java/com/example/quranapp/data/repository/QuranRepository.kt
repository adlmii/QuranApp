package com.example.quranapp.data.repository

import android.content.Context
import com.example.quranapp.data.model.Ayah
import com.example.quranapp.data.model.AyahSearchResult
import com.example.quranapp.data.model.SurahDetail
import com.example.quranapp.data.local.QuranAppDatabase
import com.example.quranapp.data.local.QuranContentDatabase
import com.example.quranapp.data.local.entity.AyahEntity
import com.example.quranapp.data.local.entity.AyahSearchFts
import com.example.quranapp.data.local.entity.AyahWithSurahName
import com.example.quranapp.data.local.entity.ManualBookmarkEntity
import com.example.quranapp.data.local.entity.RecentQuranEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class QuranRepository(private val context: Context) {

    // User state database (bookmarks, prayer, recent, FTS search)
    private val appDb = QuranAppDatabase.getInstance(context)
    private val recentQuranDao = appDb.recentQuranDao()
    private val ayahSearchDao = appDb.ayahSearchDao()
    private val bookmarkDao = appDb.manualBookmarkDao()

    // Quran content database (read-only, pre-populated from quran.db)
    private val contentDb = QuranContentDatabase.getInstance(context)
    private val surahDao = contentDb.surahDao()
    private val quranDao = contentDb.quranDao()

    // ── Mode Halaman (Mushaf Page View) ──

    /**
     * Get all ayahs on a specific page as a reactive Flow.
     * Used by the Mushaf pager screen.
     */
    fun getAyahsByPage(pageNumber: Int): Flow<List<AyahWithSurahName>> {
        return quranDao.getAyahsByPage(pageNumber)
    }

    suspend fun getSurahDetail(surahNumber: Int): SurahDetail? = withContext(Dispatchers.IO) {
        try {
            val surah = surahDao.getById(surahNumber) ?: return@withContext null
            val ayahEntities = quranDao.getAyahsBySurah(surahNumber)

            val ayahs = ayahEntities.map { entity ->
                Ayah(
                    number = entity.verseNumber,
                    arabic = entity.textUthmani,
                    translation = entity.translationId,
                    page = entity.pageNumber,
                    juz = entity.juzNumber,
                    manzil = 0,
                    hizbQuarter = 0
                )
            }

            SurahDetail(
                number = surah.id,
                name = surah.nameSimple,
                arabicName = surah.nameArabic,
                englishName = surah.nameSimple,
                ayahCount = surah.versesCount,
                type = surah.revelationPlace,
                ayahs = ayahs
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSurahList(): List<com.example.quranapp.data.model.Surah> = withContext(Dispatchers.IO) {
        try {
            val entities = surahDao.getAll()
            entities.map { entity ->
                com.example.quranapp.data.model.Surah(
                    number = entity.id,
                    name = entity.nameSimple,
                    englishName = entity.nameSimple,
                    arabicName = entity.nameArabic,
                    ayahCount = entity.versesCount,
                    type = entity.revelationPlace
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getJuzList(): List<com.example.quranapp.data.model.Juz> = withContext(Dispatchers.IO) {
        try {
            val juzNumbers = quranDao.getDistinctJuzNumbers()
            val juzList = ArrayList<com.example.quranapp.data.model.Juz>()

            for (juzNum in juzNumbers) {
                val firstAyahs = quranDao.getFirstAyahPerSurahInJuz(juzNum)
                val lastAyahs = quranDao.getLastAyahPerSurahInJuz(juzNum)

                // Build a map of surahId -> last verse number in this juz
                val lastVerseMap = lastAyahs.associate { it.surahId to it.verseNumber }

                val surahEntries = ArrayList<com.example.quranapp.data.model.JuzSurahEntry>()
                for (firstAyah in firstAyahs) {
                    val surah = surahDao.getById(firstAyah.surahId)
                    val lastVerse = lastVerseMap[firstAyah.surahId] ?: firstAyah.verseNumber
                    val totalVerses = surah?.versesCount ?: lastVerse

                    // Determine ayah range string
                    val ayahRange = if (firstAyah.verseNumber == 1 && lastVerse == totalVerses) {
                        "All"
                    } else {
                        "${firstAyah.verseNumber}-${lastVerse}"
                    }

                    surahEntries.add(
                        com.example.quranapp.data.model.JuzSurahEntry(
                            surahNumber = firstAyah.surahId,
                            surahName = surah?.nameSimple ?: "Surah ${firstAyah.surahId}",
                            arabicName = surah?.nameArabic ?: "",
                            ayahRange = ayahRange
                        )
                    )
                }

                juzList.add(
                    com.example.quranapp.data.model.Juz(
                        number = juzNum,
                        surahs = surahEntries
                    )
                )
            }
            juzList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ── Recent Quran (Last Read) — Room Database ──

    /**
     * Reactive Flow untuk observe perubahan last read secara real-time.
     * Dipakai oleh HomeViewModel.
     */
    fun getLastReadFlow(): Flow<RecentQuranEntity?> {
        return recentQuranDao.getLastRead()
    }

    /**
     * Simpan posisi terakhir baca ke Room Database.
     */
    suspend fun saveLastRead(
        surahNumber: Int,
        ayahNumber: Int,
        surahName: String,
        isPageMode: Boolean = false,
        pageNumber: Int? = null
    ) {
        recentQuranDao.saveLastRead(
            RecentQuranEntity(
                id = 1,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName,
                isPageMode = isPageMode,
                pageNumber = pageNumber
            )
        )
    }

    // ── Manual Bookmark ──

    fun getBookmarkFlow(): Flow<ManualBookmarkEntity?> {
        return bookmarkDao.getBookmark()
    }

    suspend fun saveBookmark(surahNumber: Int, ayahNumber: Int, surahName: String) {
        bookmarkDao.saveBookmark(
            ManualBookmarkEntity(
                id = 1,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName
            )
        )
    }

    suspend fun deleteBookmark() {
        bookmarkDao.deleteBookmark()
    }

    /**
     * Public wrapper to get surah name by number (for flow navigation)
     */
    suspend fun getSurahNameByNumber(surahNumber: Int): String? {
        return surahDao.getById(surahNumber)?.nameSimple
    }

    // ── Ayah Search (FTS) ──

    /**
     * Populate FTS index from Quran content database on first launch.
     * Skips if already populated.
     */
    suspend fun populateSearchIndex() = withContext(Dispatchers.IO) {
        try {
            if (ayahSearchDao.count() > 0) return@withContext

            val surahEntities = surahDao.getAll()
            val surahNameMap = surahEntities.associate { it.id to it.nameSimple }

            // Load all ayahs from content database
            val entries = ArrayList<AyahSearchFts>(6236)
            for (surah in surahEntities) {
                val ayahs = quranDao.getAyahsBySurah(surah.id)
                for (ayah in ayahs) {
                    entries.add(
                        AyahSearchFts(
                            surahNumber = ayah.surahId,
                            ayahNumber = ayah.verseNumber,
                            surahName = surahNameMap[ayah.surahId] ?: "Surah ${ayah.surahId}",
                            textTranslation = ayah.translationId
                        )
                    )
                }
            }

            // Insert in batches
            entries.chunked(500).forEach { batch ->
                ayahSearchDao.insertAll(batch)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Search ayahs by text in translation using FTS.
     */
    suspend fun searchAyahs(query: String): List<AyahSearchResult> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) return@withContext emptyList()

            val ftsQuery = query.trim() + "*"
            val results = ayahSearchDao.search(ftsQuery)

            results.map { fts ->
                val text = fts.textTranslation
                val snippet = if (text.length > 120) {
                    val idx = text.indexOf(query, ignoreCase = true).coerceAtLeast(0)
                    val start = (idx - 30).coerceAtLeast(0)
                    val end = (start + 120).coerceAtMost(text.length)
                    (if (start > 0) "..." else "") + text.substring(start, end) + (if (end < text.length) "..." else "")
                } else {
                    text
                }

                AyahSearchResult(
                    surahNumber = fts.surahNumber,
                    ayahNumber = fts.ayahNumber,
                    surahName = fts.surahName,
                    snippet = snippet
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
