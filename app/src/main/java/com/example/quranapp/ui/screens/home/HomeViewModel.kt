package com.example.quranapp.ui.screens.home

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.repository.PrayerRepository
import com.example.quranapp.data.repository.QuranRepository
import com.example.quranapp.data.local.UserPreferencesRepository
import com.batoulapps.adhan.Prayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val gregorianDate: String = "",
    val hijriDate: String = "",
    val matsuratType: String = "", // Localized Label
    val matsuratKey: String = "MORNING", // Nav Key
    val nextPrayerName: String = "--",
    val nextPrayerTime: String = "--:--",
    val timeToNextPrayer: String = "",
    val isCurrentPrayerNow: Boolean = false,
    val currentPrayerLabel: String = "",
    val location: String = "Jakarta",
    val lat: Double = -6.1753,
    val lon: Double = 106.8312,
    val quranCurrentMinutes: Int = 0,
    val quranTargetMinutes: Int = 5,
    val lastReadSurah: String? = null,
    val lastReadNumber: Int = 1,
    val lastReadAyah: Int = 1,
    // Manual Bookmark
    val bookmarkSurah: String? = null,
    val bookmarkSurahNumber: Int = 0,
    val bookmarkAyah: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val prayerRepository = PrayerRepository()
    private val quranRepository = QuranRepository(application)
    private val userPrefs = UserPreferencesRepository(application)

    init {
        // Set default values from resources
        _uiState.value = _uiState.value.copy(
            timeToNextPrayer = application.getString(com.example.quranapp.R.string.label_calculating),
            matsuratType = application.getString(com.example.quranapp.R.string.matsurat_pagi)
        )
        loadSavedLocation()
        updateDateInfo()
        startPrayerCountdown()
        observeLastRead()
        observeGoals()
        observeBookmark()
    }

    private fun observeBookmark() {
        viewModelScope.launch {
            quranRepository.getBookmarkFlow().collect { bookmark ->
                _uiState.value = _uiState.value.copy(
                    bookmarkSurah = bookmark?.surahName,
                    bookmarkSurahNumber = bookmark?.surahNumber ?: 0,
                    bookmarkAyah = bookmark?.ayahNumber ?: 0
                )
            }
        }
    }

    private fun loadSavedLocation() {
        viewModelScope.launch {
            userPrefs.lastLocation.collect { (lat, lon) ->
                _uiState.value = _uiState.value.copy(lat = lat, lon = lon)
                calculatePrayerTimes()
                return@collect
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(lat = latitude, lon = longitude)
            calculatePrayerTimes()
            getAddressName(latitude, longitude, context)
            userPrefs.saveLastLocation(latitude, longitude)
        }
    }

    private suspend fun getAddressName(lat: Double, lon: Double, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val district = address.subLocality ?: address.locality ?: "Unknown"
                    _uiState.value = _uiState.value.copy(location = district)
                }
            } catch (e: Exception) {
                val unknown = getApplication<Application>().getString(com.example.quranapp.R.string.location_unknown)
                _uiState.value = _uiState.value.copy(location = unknown)
            }
        }
    }

    private fun startPrayerCountdown() {
        viewModelScope.launch {
            while (isActive) {
                calculatePrayerTimes()
                delay(1000L)
            }
        }
    }

    private fun calculatePrayerTimes() {
        try {
            val schedule = prayerRepository.calculatePrayerTimes(_uiState.value.lat, _uiState.value.lon)
            
            val nextPrayerName = getPrayerNameString(schedule.nextPrayer)
            val targetTime = schedule.nextPrayerTime
            val now = Date()

            val diffMillis = if (targetTime != null) targetTime.time - now.time else 0L

            val countdownText = if (diffMillis > 0) {
                val hours = diffMillis / (1000 * 60 * 60)
                val minutes = (diffMillis / (1000 * 60)) % 60
                val seconds = (diffMillis / 1000) % 60

                if (hours > 0) {
                    getApplication<Application>().getString(com.example.quranapp.R.string.countdown_hours_mins, hours, minutes)
                } else {
                    getApplication<Application>().getString(com.example.quranapp.R.string.countdown_mins_secs, minutes, seconds)
                }
            } else {
                getApplication<Application>().getString(com.example.quranapp.R.string.label_now)
            }

            val timeFormatter = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = if (targetTime != null) timeFormatter.format(targetTime) else "--:--"

            // Grace period awareness
            val isNowActive = schedule.isInGracePeriod && schedule.currentPrayer != null
            val currentLabel = if (isNowActive && schedule.currentPrayer != null) {
                getPrayerNameString(schedule.currentPrayer)
            } else ""

            // When in grace period, show current prayer's time on the card
            val cardTime = if (isNowActive && schedule.currentPrayer != null) {
                val currentPrayerDate = schedule.prayerTimes.timeForPrayer(schedule.currentPrayer)
                if (currentPrayerDate != null) {
                    timeFormatter.format(currentPrayerDate)
                } else formattedTime
            } else formattedTime

            val cardName = if (isNowActive) currentLabel
                else nextPrayerName

            _uiState.value = _uiState.value.copy(
                nextPrayerName = cardName,
                nextPrayerTime = cardTime,
                timeToNextPrayer = countdownText,
                isCurrentPrayerNow = isNowActive,
                currentPrayerLabel = currentLabel
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                nextPrayerName = getApplication<Application>().getString(com.example.quranapp.R.string.error_generic),
                timeToNextPrayer = getApplication<Application>().getString(com.example.quranapp.R.string.error_retry)
            )
        }
    }

    private fun updateDateInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today = LocalDate.now()
            val now = LocalTime.now()

            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())

            val hijrahDate = HijrahDate.from(today)
            val hijriFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

            val isMorning = now.hour < 14
            val matsuratLabel = if (isMorning) 
                getApplication<Application>().getString(com.example.quranapp.R.string.matsurat_pagi) 
            else 
                getApplication<Application>().getString(com.example.quranapp.R.string.matsurat_petang)
            
            val matsuratKey = if (isMorning) "MORNING" else "EVENING"

            _uiState.value = _uiState.value.copy(
                gregorianDate = today.format(formatter),
                hijriDate = hijriFormatter.format(hijrahDate),
                matsuratType = matsuratLabel,
                matsuratKey = matsuratKey
            )
        }
    }

    /**
     * Observe last read dari Room — real-time reactive update
     */
    private fun observeLastRead() {
        viewModelScope.launch {
            quranRepository.getLastReadFlow().collect { entity ->
                if (entity != null) {
                    _uiState.value = _uiState.value.copy(
                        lastReadSurah = entity.surahName,
                        lastReadNumber = entity.surahNumber,
                        lastReadAyah = entity.ayahNumber
                    )
                } else {
                    // Belum ada data — biarkan null supaya UI tampil empty state
                    _uiState.value = _uiState.value.copy(
                        lastReadSurah = null
                    )
                }
            }
        }
    }

    /**
     * Observe goals dari DataStore — real-time reactive update
     */
    private fun observeGoals() {
        viewModelScope.launch {
            userPrefs.todayMinutes.collect { minutes ->
                _uiState.value = _uiState.value.copy(quranCurrentMinutes = minutes)
            }
        }
        viewModelScope.launch {
            userPrefs.targetMinutes.collect { target ->
                _uiState.value = _uiState.value.copy(quranTargetMinutes = target)
            }
        }
    }

    private fun getPrayerNameString(prayer: Prayer): String {
        val resId = when(prayer) {
            Prayer.FAJR -> com.example.quranapp.R.string.prayer_fajr
            Prayer.SUNRISE -> com.example.quranapp.R.string.prayer_syuruq
            Prayer.DHUHR -> com.example.quranapp.R.string.prayer_dhuhr
            Prayer.ASR -> com.example.quranapp.R.string.prayer_asr
            Prayer.MAGHRIB -> com.example.quranapp.R.string.prayer_maghrib
            Prayer.ISHA -> com.example.quranapp.R.string.prayer_isha
            else -> return "--"
        }
        return getApplication<Application>().getString(resId)
    }
}