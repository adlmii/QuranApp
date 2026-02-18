package com.example.quranapp.data.repository

import android.content.Context
import com.example.quranapp.data.model.Ayah
import com.example.quranapp.data.model.AyahSearchResult
import com.example.quranapp.data.model.SurahDetail
import com.example.quranapp.data.local.QuranAppDatabase
import com.example.quranapp.data.local.entity.AyahSearchFts
import com.example.quranapp.data.local.entity.RecentQuranEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class QuranRepository(private val context: Context) {

    private val db = QuranAppDatabase.getInstance(context)
    private val recentQuranDao = db.recentQuranDao()
    private val ayahSearchDao = db.ayahSearchDao()

    suspend fun getSurahDetail(surahNumber: Int): SurahDetail? = withContext(Dispatchers.IO) {
        try {
            // 1. Load Arabic Text (Uthmani Hafs)
            // Structure: { "quran": [ { "chapter": 1, "verse": 1, "text": "..." }, ... ] }
            val arabicString = context.assets.open("quran_uthmani.json").bufferedReader().use { it.readText() }
            val arabicRoot = JSONObject(arabicString)
            val arabicVerses = arabicRoot.getJSONArray("quran")
            
            // 2. Load Translation (Indonesian Ministry)
            // Structure: { "quran": [ { "chapter": 1, "verse": 1, "text": "..." }, ... ] }
            val transString = context.assets.open("quran_translation_indo.json").bufferedReader().use { it.readText() }
            val transRoot = JSONObject(transString)
            val transVerses = transRoot.getJSONArray("quran")

            // 3. Filter and Map Data
            val ayahs = ArrayList<Ayah>()
            
            // Map Translation for O(1) lookup
            val translationMap =  HashMap<Int, String>() // Verse Number -> Text
            for (i in 0 until transVerses.length()) {
                val tObj = transVerses.getJSONObject(i)
                if (tObj.getInt("chapter") == surahNumber) {
                    translationMap[tObj.getInt("verse")] = tObj.getString("text")
                }
            }

            for (i in 0 until arabicVerses.length()) {
                val aObj = arabicVerses.getJSONObject(i)
                if (aObj.getInt("chapter") == surahNumber) {
                    val verseNum = aObj.getInt("verse")
                    val arabicText = aObj.getString("text")
                    val translationText = translationMap[verseNum] ?: ""
                    
                    ayahs.add(
                        Ayah(
                            number = verseNum,
                            arabic = arabicText,
                            translation = translationText,
                            page = 0,
                            juz = 0,
                            manzil = 0,
                            hizbQuarter = 0
                        )
                    )
                }
            }

            // Get Metadata for Surah objects
            val surahMetadata = getSurahMetadata(surahNumber)

            return@withContext SurahDetail(
                number = surahNumber,
                name = surahMetadata?.name ?: "",
                arabicName = surahMetadata?.arabicName ?: "",
                englishName = surahMetadata?.englishName ?: "",
                ayahCount = ayahs.size,
                type = surahMetadata?.type ?: "",
                ayahs = ayahs
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getSurahMetadata(surahNumber: Int): com.example.quranapp.data.model.Surah? {
        val list = getSurahList()
        return list.find { it.number == surahNumber }
    }

    suspend fun getSurahList(): List<com.example.quranapp.data.model.Surah> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("surah_list_metadata.json").bufferedReader().use { it.readText() }
            val surahsArray = org.json.JSONArray(jsonString)
            val surahList = ArrayList<com.example.quranapp.data.model.Surah>()

            for (i in 0 until surahsArray.length()) {
                val obj = surahsArray.getJSONObject(i)
                surahList.add(
                    com.example.quranapp.data.model.Surah(
                        number = obj.getInt("number"),
                        name = obj.getString("englishName"),
                        englishName = obj.getString("englishNameTranslation"),
                        arabicName = obj.getString("name"),
                        ayahCount = obj.getInt("numberOfAyahs"),
                        type = obj.getString("revelationType")
                    )
                )
            }
            surahList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getJuzList(): List<com.example.quranapp.data.model.Juz> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("juz_list_metadata.json").bufferedReader().use { it.readText() }
            val juzArray = org.json.JSONArray(jsonString)
            val juzList = ArrayList<com.example.quranapp.data.model.Juz>()

            for (i in 0 until juzArray.length()) {
                val jObj = juzArray.getJSONObject(i)
                val sArray = jObj.getJSONArray("surahs")
                val surahEntries = ArrayList<com.example.quranapp.data.model.JuzSurahEntry>()
                
                for (k in 0 until sArray.length()) {
                    val sObj = sArray.getJSONObject(k)
                    surahEntries.add(
                        com.example.quranapp.data.model.JuzSurahEntry(
                            surahNumber = sObj.getInt("surahNumber"),
                            surahName = sObj.getString("surahName"),
                            arabicName = sObj.getString("arabicName"),
                            ayahRange = sObj.getString("ayahRange")
                        )
                    )
                }
                
                juzList.add(
                    com.example.quranapp.data.model.Juz(
                        number = jObj.getInt("number"),
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

    // ── Ayah Search (FTS) ──

    /**
     * Populate FTS index from JSON assets on first launch.
     * Skips if already populated.
     */
    suspend fun populateSearchIndex() = withContext(Dispatchers.IO) {
        try {
            if (ayahSearchDao.count() > 0) return@withContext

            // Load surah metadata for name lookup
            val surahMetaString = context.assets.open("surah_list_metadata.json").bufferedReader().use { it.readText() }
            val surahMetaArray = org.json.JSONArray(surahMetaString)
            val surahNameMap = HashMap<Int, String>()
            for (i in 0 until surahMetaArray.length()) {
                val obj = surahMetaArray.getJSONObject(i)
                surahNameMap[obj.getInt("number")] = obj.getString("englishName")
            }

            // Load translations
            val transString = context.assets.open("quran_translation_indo.json").bufferedReader().use { it.readText() }
            val transRoot = JSONObject(transString)
            val transVerses = transRoot.getJSONArray("quran")

            val entries = ArrayList<AyahSearchFts>(6236)
            for (i in 0 until transVerses.length()) {
                val obj = transVerses.getJSONObject(i)
                val chapter = obj.getInt("chapter")
                entries.add(
                    AyahSearchFts(
                        surahNumber = chapter,
                        ayahNumber = obj.getInt("verse"),
                        surahName = surahNameMap[chapter] ?: "Surah $chapter",
                        textTranslation = obj.getString("text")
                    )
                )
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

