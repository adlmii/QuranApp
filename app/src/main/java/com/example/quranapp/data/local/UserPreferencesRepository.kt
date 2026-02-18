package com.example.quranapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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

        // Per-prayer notification toggles
        private val PRAYER_NOTIFICATION_KEYS = mapOf(
            "Fajr ✨" to booleanPreferencesKey("notify_fajr"),
            "Dhuhr 🌤" to booleanPreferencesKey("notify_dhuhr"),
            "Asr 🌥" to booleanPreferencesKey("notify_asr"),
            "Maghrib 🌅" to booleanPreferencesKey("notify_maghrib"),
            "Isha'a 🌙" to booleanPreferencesKey("notify_isha")
        )
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
            0
        } else {
            prefs[KEY_TODAY_MINUTES] ?: 0
        }
    }

    /**
     * Observe per-prayer notification preferences as Map<PrayerName, Boolean>
     */
    val notificationPrefs: Flow<Map<String, Boolean>> = context.dataStore.data.map { prefs ->
        PRAYER_NOTIFICATION_KEYS.mapValues { (_, key) ->
            prefs[key] ?: true // Default: notifications ON
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

    /**
     * Toggle notification for a specific prayer
     */
    suspend fun setNotificationPref(prayerName: String, enabled: Boolean) {
        val key = PRAYER_NOTIFICATION_KEYS[prayerName] ?: return
        context.dataStore.edit { prefs ->
            prefs[key] = enabled
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
