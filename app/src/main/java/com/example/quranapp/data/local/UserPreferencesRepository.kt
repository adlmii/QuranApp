package com.example.quranapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Singleton DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_TARGET_MINUTES = intPreferencesKey("quran_target_minutes")
        private val KEY_TODAY_MINUTES = intPreferencesKey("quran_today_minutes")
        private val KEY_LAST_SAVED_DATE = stringPreferencesKey("last_saved_date")
    }

    /**
     * Observe target menit baca harian (default 25)
     */
    val targetMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TARGET_MINUTES] ?: 25
    }

    /**
     * Observe total menit baca hari ini (auto-reset jika hari berbeda)
     */
    val todayMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        val savedDate = prefs[KEY_LAST_SAVED_DATE] ?: ""
        val today = getTodayString()
        if (savedDate != today) {
            0 // Hari berbeda → belum ada progres
        } else {
            prefs[KEY_TODAY_MINUTES] ?: 0
        }
    }

    /**
     * Tambah 1 menit ke tracking hari ini.
     * Otomatis reset jika hari sudah berganti.
     */
    suspend fun addMinute() {
        context.dataStore.edit { prefs ->
            val today = getTodayString()
            val savedDate = prefs[KEY_LAST_SAVED_DATE] ?: ""

            if (savedDate != today) {
                // Hari berganti → reset progres
                prefs[KEY_TODAY_MINUTES] = 1
                prefs[KEY_LAST_SAVED_DATE] = today
            } else {
                val current = prefs[KEY_TODAY_MINUTES] ?: 0
                prefs[KEY_TODAY_MINUTES] = current + 1
            }
        }
    }

    /**
     * Set target menit baca harian
     */
    suspend fun setTarget(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TARGET_MINUTES] = minutes
        }
    }

    private fun getTodayString(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            sdf.format(java.util.Date())
        }
    }
}
