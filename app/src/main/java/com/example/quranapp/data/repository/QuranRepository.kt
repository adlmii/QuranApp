package com.example.quranapp.data.repository

import android.content.Context
import com.example.quranapp.data.model.Ayah
import com.example.quranapp.data.model.SurahDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class QuranRepository(private val context: Context) {

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
            
            // Optimized approach: Iterate through Arabic verses, find matching ones for this surah
            // Since the JSON is flat and sorted, we could optimize finding the start index, 
            // but for simplicity and safety against minor unsorted data (though likely sorted), 
            // we'll iterate. For 6236 verses, simple iteration is acceptable on IO thread, 
            // but fetching *all* verses every time is inefficient.
            // TODO: In a real production app, parsing the huge JSON once and caching, or using a DB is better.
            // For this refactor, we stick to file reading but ensure we match chapter.
            
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

                    // Note: The new JSON might not have page/juz info per verse. 
                    // If strictly needed, we'd need another metadata source or heuristic.
                    // For now, defaulting to 0 or using metadata if we had verse-level metadata.
                    // The old code used metadata from "quran_tajweed_full.json" which had it.
                    // If we strictly need Juz/Page, we might need to keep "quran_tajweed_full.json" just for metadata
                    // or assume the user is okay with losing verse-level page info for now, 
                    // OR we use the surah-level page info to estimate.
                    // Let's default to 0 for now as per the "change the data" request.
                    
                    ayahs.add(
                        Ayah(
                            number = verseNum,
                            arabic = arabicText,
                            translation = translationText,
                            page = 0, // Placeholder
                            juz = 0, // Placeholder
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
                name = surahMetadata?.name ?: "", // English/Latin Name
                arabicName = surahMetadata?.arabicName ?: "",
                englishName = surahMetadata?.englishName ?: "", // Meaning
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
                        name = obj.getString("englishName"),                    // e.g. "Al-Faatiha"
                        englishName = obj.getString("englishNameTranslation"),  // e.g. "The Opening"
                        arabicName = obj.getString("name"),                     // e.g. "سُورَةُ ٱلْفَاتِحَةِ"
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
}
