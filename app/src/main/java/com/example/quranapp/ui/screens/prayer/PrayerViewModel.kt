package com.example.quranapp.ui.screens.prayer

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.example.quranapp.data.repository.PrayerRepository
import com.example.quranapp.data.local.QuranAppDatabase
import com.example.quranapp.data.local.UserPreferencesRepository
import com.example.quranapp.data.local.entity.PrayerStatusEntity
import com.example.quranapp.notification.PrayerAlarmScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class PrayerItemState(
    val name: String,
    val time: String,
    val isPrayed: Boolean = false,
    val isNotificationOn: Boolean = true,
    val isNext: Boolean = false,
    val isNow: Boolean = false,
    val isPassed: Boolean = false,
    val countdown: String = ""
)

data class PrayerUiState(
    val prayerList: List<PrayerItemState> = emptyList(),
    val prayedCount: Int = 0,
    val totalPrayer: Int = 5,
    val nextPrayerName: String = "--",
    val nextPrayerTime: String = "--:--",
    val timeToNextPrayer: String = "",
    val isCurrentPrayerNow: Boolean = false,
    val currentPrayerLabel: String = "",
    val imsakTime: String = "--:--",
    val sunriseTime: String = "--:--",
    val gregorianDate: String = "",
    val hijriDate: String = "",
    val location: String = "Jakarta",
    val canMarkAll: Boolean = false
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private var lat: Double = -6.1753
    private var lon: Double = 106.8312

    private val prayerRepository = PrayerRepository()
    private val prayerStatusDao = QuranAppDatabase.getInstance(application).prayerStatusDao()
    private val userPrefs = UserPreferencesRepository(application)
    private val alarmScheduler = PrayerAlarmScheduler(application)

    // In-memory cache of prayer statuses loaded from DB
    private val prayedStatusMap = mutableMapOf<String, Boolean>()
    // In-memory cache of notification prefs
    private val notificationPrefsMap = mutableMapOf<String, Boolean>()
    // Flag to avoid scheduling alarms every second
    private var alarmsScheduled = false

    init {
        loadSavedLocation()
        updateDateInfo()
        loadPrayerStatuses()
        observeNotificationPrefs()
        startPrayerCountdown()
    }

    private fun loadSavedLocation() {
        viewModelScope.launch {
            userPrefs.lastLocation.collect { (savedLat, savedLon) ->
                lat = savedLat
                lon = savedLon
                return@collect
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, context: Context) {
        lat = latitude
        lon = longitude
        alarmsScheduled = false // Reschedule on location change
        viewModelScope.launch {
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

    /**
     * Load prayer statuses for today from Room and observe reactively
     */
    private fun loadPrayerStatuses() {
        viewModelScope.launch {
            prayerStatusDao.getPrayerStatusByDate(getTodayString()).collect { statusList ->
                prayedStatusMap.clear()
                statusList.forEach { entity ->
                    prayedStatusMap[entity.prayerName] = entity.isPrayed
                }
                calculatePrayerTimes()
            }
        }
    }

    /**
     * Observe notification preferences from DataStore
     */
    private fun observeNotificationPrefs() {
        viewModelScope.launch {
            userPrefs.notificationPrefs.collect { prefs ->
                notificationPrefsMap.clear()
                notificationPrefsMap.putAll(prefs)
                alarmsScheduled = false // Reschedule with updated prefs
                calculatePrayerTimes()
            }
        }
    }

    fun togglePrayed(index: Int) {
        val currentList = _uiState.value.prayerList.toMutableList()
        val item = currentList[index]
        
        // Allow checklist if prayer is passed or currently in "Now" state
        if (item.isPassed || item.isNow) {
            val newStatus = !item.isPrayed
            currentList[index] = item.copy(isPrayed = newStatus)
            val newCount = currentList.count { it.isPrayed }

            _uiState.value = _uiState.value.copy(
                prayerList = currentList,
                prayedCount = newCount,
                canMarkAll = currentList.any { (it.isPassed || it.isNow) && !it.isPrayed }
            )

            // Persist to Room
            viewModelScope.launch {
                prayerStatusDao.upsertPrayerStatus(
                    PrayerStatusEntity(
                        date = getTodayString(),
                        prayerName = item.name,
                        isPrayed = newStatus
                    )
                )
            }
        }
    }

    fun toggleNotification(index: Int) {
        val currentList = _uiState.value.prayerList.toMutableList()
        val item = currentList[index]
        val newStatus = !item.isNotificationOn

        currentList[index] = item.copy(isNotificationOn = newStatus)
        _uiState.value = _uiState.value.copy(prayerList = currentList)

        viewModelScope.launch {
            userPrefs.setNotificationPref(item.name, newStatus)
        }
    }

    fun markAllPrayed() {
        val currentList = _uiState.value.prayerList.map { 
            if (it.isPassed || it.isNow) it.copy(isPrayed = true) else it
        }
        _uiState.value = _uiState.value.copy(
            prayerList = currentList,
            prayedCount = currentList.count { it.isPrayed },
            canMarkAll = false
        )

        // Persist all to Room
        viewModelScope.launch {
            prayerStatusDao.markAllPrayed(getTodayString())
        }
    }

    private fun updateDateInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
            val hijrahDate = HijrahDate.from(today)
            val hijriFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

            _uiState.value = _uiState.value.copy(
                gregorianDate = today.format(formatter),
                hijriDate = hijriFormatter.format(hijrahDate)
            )
        }
    }

    private fun calculatePrayerTimes() {
        try {
            val schedule = prayerRepository.calculatePrayerTimes(lat, lon)
            
            val nextPrayer = schedule.nextPrayer
            val nextPrayerTimeDate = schedule.nextPrayerTime
            val now = System.currentTimeMillis()

            // Countdown to next prayer
            val diffMillis = if (nextPrayerTimeDate != null) nextPrayerTimeDate.time - now else 0L
            val countdownText = if (diffMillis > 0) {
                val hours = diffMillis / (1000 * 60 * 60)
                val minutes = (diffMillis / (1000 * 60)) % 60
                val seconds = (diffMillis / 1000) % 60

                if (hours > 0) getApplication<Application>().getString(com.example.quranapp.R.string.countdown_hours_mins, hours, minutes) 
                else getApplication<Application>().getString(com.example.quranapp.R.string.countdown_mins_secs, minutes, seconds)
            } else {
                getApplication<Application>().getString(com.example.quranapp.R.string.label_now)
            }

            val formatter = SimpleDateFormat("h:mm a", Locale.ENGLISH)
            val imsakTime = schedule.imsak
            val sunriseTime = schedule.sunrise

            val prayerTimes = schedule.prayerTimes

            val rawList = listOf(
                Triple("Fajr", prayerTimes.fajr, Prayer.FAJR),
                Triple("Dzuhur", prayerTimes.dhuhr, Prayer.DHUHR),
                Triple("Asr", prayerTimes.asr, Prayer.ASR),
                Triple("Maghrib", prayerTimes.maghrib, Prayer.MAGHRIB),
                Triple("Isha'a", prayerTimes.isha, Prayer.ISHA)
            )

            val uiList = rawList.map { (_, timeDate, type) ->
                val prayerName = getPrayerNameString(type)
                val isPassed = now >= timeDate.time
                val isNow = schedule.isInGracePeriod && schedule.currentPrayer == type
                val wasPrayed = prayedStatusMap[prayerName] ?: false
                val isNotifOn = notificationPrefsMap[prayerName] ?: true

                PrayerItemState(
                    name = prayerName,
                    time = formatter.format(timeDate).lowercase(),
                    isNext = (nextPrayer == type && !isNow),
                    isNow = isNow,
                    isPrayed = wasPrayed,
                    isPassed = isPassed && !isNow,
                    isNotificationOn = isNotifOn,
                    countdown = when {
                        isNow -> getApplication<Application>().getString(com.example.quranapp.R.string.label_now)
                        nextPrayer == type -> countdownText
                        else -> ""
                    }
                )
            }

            val formattedNextTime = if (nextPrayerTimeDate != null) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(nextPrayerTimeDate)
            } else "--:--"

            // Determine card label for PrayerCard
            val isNowActive = schedule.isInGracePeriod && schedule.currentPrayer != null
            val currentLabel = if (isNowActive && schedule.currentPrayer != null) {
                getPrayerNameString(schedule.currentPrayer)
            } else ""

            // When in grace period, show current prayer's time on the card
            val cardTime = if (isNowActive && schedule.currentPrayer != null) {
                val currentPrayerDate = prayerTimes.timeForPrayer(schedule.currentPrayer)
                if (currentPrayerDate != null) {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentPrayerDate)
                } else formattedNextTime
            } else formattedNextTime

            val cardName = if (isNowActive) currentLabel
                else getPrayerNameString(nextPrayer)

            _uiState.value = _uiState.value.copy(
                prayerList = uiList,
                nextPrayerName = cardName,
                nextPrayerTime = cardTime,
                timeToNextPrayer = countdownText,
                isCurrentPrayerNow = isNowActive,
                currentPrayerLabel = currentLabel,
                imsakTime = formatter.format(imsakTime).lowercase(),
                sunriseTime = formatter.format(sunriseTime).lowercase(),
                prayedCount = uiList.count { it.isPrayed },
                canMarkAll = uiList.any { (it.isPassed || it.isNow) && !it.isPrayed }
            )

            // Schedule notification alarms (only once, not every second)
            if (!alarmsScheduled) {
                scheduleAlarms(schedule)
                alarmsScheduled = true
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleAlarms(schedule: com.example.quranapp.data.repository.PrayerSchedule) {
        val prayerTimes = schedule.prayerTimes
        // Triple: (Prayer, Date, skipPreReminder)
        val prayers = listOf(
            Triple(Prayer.FAJR, prayerTimes.fajr, false),
            Triple(Prayer.SUNRISE, prayerTimes.sunrise, true), // Sunrise: only exact time, no pre-reminder
            Triple(Prayer.DHUHR, prayerTimes.dhuhr, false),
            Triple(Prayer.ASR, prayerTimes.asr, false),
            Triple(Prayer.MAGHRIB, prayerTimes.maghrib, false),
            Triple(Prayer.ISHA, prayerTimes.isha, false)
        )

        val enabledPrayers = prayers.filter { (type, _, _) ->
            val name = prayerRepository.getPrayerName(type)
            notificationPrefsMap[name] ?: true
        }.map { (type, time, skipPre) ->
            Triple(prayerRepository.getPrayerNameClean(type), time, skipPre)
        }

        alarmScheduler.scheduleAll(enabledPrayers)
        alarmScheduler.scheduleExtras(
            fajrTime = prayerTimes.fajr,
            asrTime = prayerTimes.asr,
            maghribTime = prayerTimes.maghrib
        )
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

    private fun getTodayString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}