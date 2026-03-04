package com.example.quranapp.util

import android.content.Context
import com.example.quranapp.data.local.UserPreferencesRepository
import com.example.quranapp.data.util.toHijriDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtil {

    /**
     * Returns the app-locale-aware Gregorian date string (e.g. "Kamis, 5 Maret").
     * Falls back to system locale if no context is provided.
     */
    fun getGregorianDate(context: Context? = null): String {
        val locale = getAppLocale(context)
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
        return today.format(formatter)
    }

    fun getHijriDate(): String {
        val today = LocalDate.now()
        val hijri = toHijriDate(today)

        return "${hijri.day} ${hijri.monthName} ${hijri.year} H"
    }

    /**
     * Resolve the app's saved language preference to a Locale.
     * If no context or no preference, falls back to Locale.getDefault().
     */
    private fun getAppLocale(context: Context?): Locale {
        if (context == null) return Locale.getDefault()
        return try {
            val languageCode = runBlocking {
                UserPreferencesRepository(context).language.first()
            }
            when {
                languageCode == "in" -> Locale("in")
                languageCode == "en" -> Locale.ENGLISH
                languageCode.isNotBlank() -> Locale(languageCode)
                else -> Locale.getDefault()
            }
        } catch (e: Exception) {
            Locale.getDefault()
        }
    }
}
