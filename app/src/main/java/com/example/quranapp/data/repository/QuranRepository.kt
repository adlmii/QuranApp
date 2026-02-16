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
            // 1. Load Arabic Text (with Inline Tajweed tags)
            // Structure: { data: { surahs: [ { number: 1, ayahs: [ { number: 1, text: "..." } ... ] } ... ] } }
            // Note: This loads ~6MB. Optimization: Stream parser or Database.
            val tajweedString = context.assets.open("quran_tajweed_full.json").bufferedReader().use { it.readText() }
            val tajweedRoot = JSONObject(tajweedString)
            val tajweedSurahs = tajweedRoot.getJSONObject("data").getJSONArray("surahs")
            
            // Find Surah (Array is 0-indexed, but usually ordered by number. We iterate to be safe)
            var surahJson: JSONObject? = null
            for (i in 0 until tajweedSurahs.length()) {
                val s = tajweedSurahs.getJSONObject(i)
                if (s.getInt("number") == surahNumber) {
                    surahJson = s
                    break
                }
            }
            if (surahJson == null) return@withContext null

            // 2. Load Translation
            // Structure: { data: { surahs: [ ... ] } } matching structure
            val transString = context.assets.open("quran_translation_full.json").bufferedReader().use { it.readText() }
            val transRoot = JSONObject(transString)
            val transSurahs = transRoot.getJSONObject("data").getJSONArray("surahs")
            
            var transSurahJson: JSONObject? = null
             for (i in 0 until transSurahs.length()) {
                val s = transSurahs.getJSONObject(i)
                if (s.getInt("number") == surahNumber) {
                    transSurahJson = s
                    break
                }
            }

            // 3. Map Data
            val ayahs = ArrayList<Ayah>()
            val ayahsJson = surahJson.getJSONArray("ayahs")
            val transAyahsJson = transSurahJson?.getJSONArray("ayahs")

            for (j in 0 until ayahsJson.length()) {
                val ayahObj = ayahsJson.getJSONObject(j)
                val ayahNum = ayahObj.getInt("numberInSurah")
                val textWithTags = ayahObj.getString("text")
                
                // Get translation if available
                var translationText = ""
                if (transAyahsJson != null && j < transAyahsJson.length()) {
                    // Assume same order. Verify numberInSurah if strictly needed.
                    val tObj = transAyahsJson.getJSONObject(j)
                    if (tObj.getInt("numberInSurah") == ayahNum) {
                        translationText = tObj.getString("text")
                    }
                }

                ayahs.add(
                    Ayah(
                        number = ayahNum,
                        arabic = textWithTags, // Contains [n]...[/n]
                        translation = translationText,
                        page = ayahObj.optInt("page", 0), // API usually provides page
                        juz = ayahObj.optInt("juz", 0),
                        manzil = ayahObj.optInt("manzil", 0),
                        hizbQuarter = ayahObj.optInt("hizbQuarter", 0)
                    )
                )
            }

            return@withContext SurahDetail(
                number = surahNumber,
                name = surahJson.getString("englishName"), // "Al-Faatiha"
                arabicName = surahJson.getString("name"), // "سُورَةُ ٱلْفَاتِحَةِ"
                englishName = surahJson.getString("englishNameTranslation"), // "The Opening"
                ayahCount = surahJson.getJSONArray("ayahs").length(),
                type = surahJson.getString("revelationType"), // "Meccan"
                ayahs = ayahs
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
