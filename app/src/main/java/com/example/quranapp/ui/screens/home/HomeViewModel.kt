package com.example.quranapp.ui.screens.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.repository.PrayerRepository
import com.example.quranapp.data.repository.QuranRepository
import com.example.quranapp.data.local.UserPreferencesRepository
import com.example.quranapp.util.UiText
import com.batoulapps.adhan.Prayer
import com.example.quranapp.R
import com.example.quranapp.util.DateUtil
import com.example.quranapp.util.PrayerFormatUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class HomeUiState(
    val gregorianDate: String = "",
    val hijriDate: String = "",
    val matsuratType: String = "",
    val matsuratKey: String = "MORNING",
    val nextPrayerName: String = "--",
    val nextPrayerTime: String = "--:--",
    val timeToNextPrayer: UiText = UiText.DynamicString(""),
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
            timeToNextPrayer = UiText.StringResource(R.string.label_calculating),
            matsuratType = application.getString(R.string.matsurat_pagi)
        )
        loadSavedLocation()

        val calendar = java.util.Calendar.getInstance()
        val nowHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val isMorning = nowHour < 14
        _uiState.value = _uiState.value.copy(
            gregorianDate = DateUtil.getGregorianDate(),
            hijriDate = DateUtil.getHijriDate(),
            matsuratType = application.getString(if (isMorning) R.string.matsurat_pagi else R.string.matsurat_petang),
            matsuratKey = if (isMorning) "MORNING" else "EVENING",
            timeToNextPrayer = UiText.StringResource(R.string.label_calculating)
        )

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
                val district = com.example.quranapp.util.LocationUtil.getAddressName(lat, lon, getApplication())

                _uiState.value = _uiState.value.copy(lat = lat, lon = lon, location = district)
                calculatePrayerTimes()
                return@collect
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, context: Context) {
        viewModelScope.launch {
            val district = com.example.quranapp.util.LocationUtil.getAddressName(latitude, longitude, context)

            _uiState.value = _uiState.value.copy(lat = latitude, lon = longitude, location = district)
            calculatePrayerTimes()
            userPrefs.saveLastLocation(latitude, longitude)
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

            val nextPrayerNameUiText = PrayerFormatUtil.getPrayerName(schedule.nextPrayer)
            val countdownText = PrayerFormatUtil.getCountdownText(schedule.nextPrayerTime)

            val targetTime = schedule.nextPrayerTime
            val timeFormatter = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = if (targetTime != null) timeFormatter.format(targetTime) else "--:--"

            val isNowActive = schedule.isInGracePeriod && schedule.currentPrayer != null
            val currentLabelUiText = if (isNowActive && schedule.currentPrayer != null) {
                PrayerFormatUtil.getPrayerName(schedule.currentPrayer)
            } else UiText.DynamicString("")

            val cardTime = if (isNowActive && schedule.currentPrayer != null) {
                val currentPrayerDate = schedule.prayerTimes.timeForPrayer(schedule.currentPrayer)
                if (currentPrayerDate != null) {
                    timeFormatter.format(currentPrayerDate)
                } else formattedTime
            } else formattedTime

            val cardName = if (isNowActive) currentLabelUiText.asString(getApplication())
            else nextPrayerNameUiText.asString(getApplication())

            _uiState.value = _uiState.value.copy(
                nextPrayerName = cardName,
                nextPrayerTime = cardTime,
                timeToNextPrayer = countdownText,
                isCurrentPrayerNow = isNowActive,
                currentPrayerLabel = currentLabelUiText.asString(getApplication())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(
                nextPrayerName = getApplication<Application>().getString(R.string.error_generic),
                timeToNextPrayer = UiText.StringResource(R.string.error_retry)
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
            Prayer.FAJR -> R.string.prayer_fajr
            Prayer.SUNRISE -> R.string.prayer_syuruq
            Prayer.DHUHR -> R.string.prayer_dhuhr
            Prayer.ASR -> R.string.prayer_asr
            Prayer.MAGHRIB -> R.string.prayer_maghrib
            Prayer.ISHA -> R.string.prayer_isha
            else -> return "--"
        }
        return getApplication<Application>().getString(resId)
    }
}