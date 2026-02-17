package com.example.quranapp.data.repository

import android.content.Context
import com.example.quranapp.data.model.AlMatsurat
import com.example.quranapp.data.model.MatsuratType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


import com.example.quranapp.data.repository.QuranRepository

class AlMatsuratRepository(private val context: Context) {

    private val quranRepository = QuranRepository(context)

    // Manual mapping to ensure accuracy given the spelling differences
    private val surahNameMap = mapOf(
        "Al-Fatihah" to 1,
        "Al-Baqarah" to 2,
        "Ali-Imran" to 3,
        "Ash-Shaffat" to 37,
        "Al-Ikhlas" to 112,
        "Al-Falaq" to 113,
        "An-Nass" to 114
    )

    suspend fun getMatsurat(type: MatsuratType): List<AlMatsurat> = withContext(Dispatchers.IO) {
        val listType = object : TypeToken<List<AlMatsurat>>() {}.type
        
        val jsonFileName = if (type == MatsuratType.EVENING) "dzikir_evening.json" else "dzikir.json"
        
        val jsonString = getJsonDataFromAsset(context, jsonFileName) ?: return@withContext emptyList()
        val originalList: List<AlMatsurat> = Gson().fromJson(jsonString, listType)

        // Enrich with Real Quran Data
        originalList.map { item ->
            if (item.isQuran) {
                enrichWithQuranData(item)
            } else {
                item
            }
        }
    }

    private suspend fun enrichWithQuranData(item: AlMatsurat): AlMatsurat {
        return try {
            val parts = item.title.split(":")
            if (parts.size < 2) return item

            val surahName = parts[0].trim()
            val rangePart = parts[1].trim()
            
            val surahId = surahNameMap[surahName] ?: return item
            
            // Handle range "1-5" or single "255" (though regex expects range, be safe)
            val verseRange = parseVerseRange(rangePart) ?: return item
            
            val surahDetail = quranRepository.getSurahDetail(surahId) ?: return item
            
            val targetVerses = surahDetail.ayahs.filter { it.number in verseRange }
            
            if (targetVerses.isEmpty()) return item

            val arabicText = targetVerses.joinToString(" " + "\u06DD" + " ") { it.arabic } + " " + "\u06DD"
            val translationText = targetVerses.joinToString(" ") { 
                "${it.number}. ${it.translation}" 
            }

            item.copy(
                arabic = arabicText,
                translation = translationText
            )
        } catch (e: Exception) {
            e.printStackTrace()
            item
        }
    }

    private fun parseVerseRange(rangeStr: String): IntRange? {
        return try {
            if (rangeStr.contains("-")) {
                val parts = rangeStr.split("-")
                val start = parts[0].trim().toInt()
                val end = parts[1].trim().toInt()
                start..end
            } else {
                val single = rangeStr.trim().toInt()
                single..single
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}
