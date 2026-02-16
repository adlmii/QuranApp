package com.example.quranapp.data

import android.content.Context
import com.example.quranapp.model.AlMatsurat
import com.example.quranapp.model.MatsuratType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AlMatsuratRepository(private val context: Context) {

    suspend fun getMatsurat(type: MatsuratType): List<AlMatsurat> = withContext(Dispatchers.IO) {
        val listType = object : TypeToken<List<AlMatsurat>>() {}.type

        if (type == MatsuratType.EVENING) {
            val eveningJsonString = getJsonDataFromAsset(context, "dzikir_evening.json")
            if (eveningJsonString != null) {
                return@withContext Gson().fromJson(eveningJsonString, listType)
            }
        }
        
        // Default to Morning (dzikir.json)
        val jsonString = getJsonDataFromAsset(context, "dzikir.json") ?: return@withContext emptyList()
        Gson().fromJson(jsonString, listType)
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
