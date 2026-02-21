package com.example.quranapp.ui.screens.prayer

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.Prayer
import com.example.quranapp.data.repository.PrayerRepository
import com.example.quranapp.data.local.QuranAppDatabase
import com.example.quranapp.data.local.UserPreferencesRepository
import com.example.quranapp.data.local.entity.PrayerStatusEntity
import com.example.quranapp.notification.PrayerAlarmScheduler
import com.example.quranapp.util.DateUtil
import com.example.quranapp.util.LocationUtil
import com.example.quranapp.util.PrayerFormatUtil
import com.example.quranapp.util.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
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
    val countdown: UiText = UiText.DynamicString("")
)

data class PrayerUiState(
    val prayerList: List<PrayerItemState> = emptyList(),
    val prayedCount: Int = 0,
    val totalPrayer: Int = 5,
    val nextPrayerName: String = "--",
    val nextPrayerTime: String = "--:--",
    val timeToNextPrayer: UiText = UiText.DynamicString(""),
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

    private val prayedStatusMap = mutableMapOf<String, Boolean>()
    private val notificationPrefsMap = mutableMapOf<String, Boolean>()
    private var alarmsScheduled = false

    init {
        _uiState.value = _uiState.value.copy(
            gregorianDate = DateUtil.getGregorianDate(),
            hijriDate = DateUtil.getHijriDate()
        )

        loadSavedLocation()
        loadPrayerStatuses()
        observeNotificationPrefs()
        startPrayerCountdown()
    }

    private fun loadSavedLocation() {
        viewModelScope.launch {
            userPrefs.lastLocation.collect { (savedLat, savedLon) ->
                lat = savedLat
                lon = savedLon

                val district = LocationUtil.getAddressName(savedLat, savedLon, getApplication())
                _uiState.value = _uiState.value.copy(location = district)

                return@collect
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double, context: Context) {
        lat = latitude
        lon = longitude
        alarmsScheduled = false
        viewModelScope.launch {
            val district = LocationUtil.getAddressName(latitude, longitude, context)
            _uiState.value = _uiState.value.copy(location = district)
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

    private fun observeNotificationPrefs() {
        viewModelScope.launch {
            userPrefs.notificationPrefs.collect { prefs ->
                notificationPrefsMap.clear()
                notificationPrefsMap.putAll(prefs)
                alarmsScheduled = false
                calculatePrayerTimes()
            }
        }
    }

    fun togglePrayed(index: Int) {
        val currentList = _uiState.value.prayerList.toMutableList()
        val item = currentList[index]

        if (item.isPassed || item.isNow) {
            val newStatus = !item.isPrayed
            currentList[index] = item.copy(isPrayed = newStatus)
            val newCount = currentList.count { it.isPrayed }

            _uiState.value = _uiState.value.copy(
                prayerList = currentList,
                prayedCount = newCount,
                canMarkAll = currentList.any { (it.isPassed || it.isNow) && !it.isPrayed }
            )

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

        viewModelScope.launch {
            prayerStatusDao.markAllPrayed(getTodayString())
        }
    }

    private fun calculatePrayerTimes() {
        try {
            val schedule = prayerRepository.calculatePrayerTimes(lat, lon)

            val nextPrayer = schedule.nextPrayer
            val nextPrayerTimeDate = schedule.nextPrayerTime
            val now = System.currentTimeMillis()

            val countdownText = PrayerFormatUtil.getCountdownText(nextPrayerTimeDate)

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
                val prayerNameUiText = PrayerFormatUtil.getPrayerName(type)
                val prayerNameStr = prayerNameUiText.asString(getApplication())

                val isPassed = now >= timeDate.time
                val isNow = schedule.isInGracePeriod && schedule.currentPrayer == type
                val wasPrayed = prayedStatusMap[prayerNameStr] ?: false
                val isNotifOn = notificationPrefsMap[prayerNameStr] ?: true

                PrayerItemState(
                    name = prayerNameStr,
                    time = formatter.format(timeDate).lowercase(),
                    isNext = (nextPrayer == type && !isNow),
                    isNow = isNow,
                    isPrayed = wasPrayed,
                    isPassed = isPassed && !isNow,
                    isNotificationOn = isNotifOn,
                    countdown = when {
                        isNow -> UiText.StringResource(com.example.quranapp.R.string.label_now)
                        nextPrayer == type -> countdownText
                        else -> UiText.DynamicString("")
                    }
                )
            }

            val formattedNextTime = if (nextPrayerTimeDate != null) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(nextPrayerTimeDate)
            } else "--:--"

            val isNowActive = schedule.isInGracePeriod && schedule.currentPrayer != null
            val currentLabelUiText = if (isNowActive && schedule.currentPrayer != null) {
                PrayerFormatUtil.getPrayerName(schedule.currentPrayer)
            } else UiText.DynamicString("")

            val cardTime = if (isNowActive && schedule.currentPrayer != null) {
                val currentPrayerDate = prayerTimes.timeForPrayer(schedule.currentPrayer)
                if (currentPrayerDate != null) {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentPrayerDate)
                } else formattedNextTime
            } else formattedNextTime

            val cardName = if (isNowActive) currentLabelUiText.asString(getApplication())
            else PrayerFormatUtil.getPrayerName(nextPrayer).asString(getApplication())

            _uiState.value = _uiState.value.copy(
                prayerList = uiList,
                nextPrayerName = cardName,
                nextPrayerTime = cardTime,
                timeToNextPrayer = countdownText,
                isCurrentPrayerNow = isNowActive,
                currentPrayerLabel = currentLabelUiText.asString(getApplication()),
                imsakTime = formatter.format(imsakTime).lowercase(),
                sunriseTime = formatter.format(sunriseTime).lowercase(),
                prayedCount = uiList.count { it.isPrayed },
                canMarkAll = uiList.any { (it.isPassed || it.isNow) && !it.isPrayed }
            )

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
        val prayers = listOf(
            Triple(Prayer.FAJR, prayerTimes.fajr, false),
            Triple(Prayer.SUNRISE, prayerTimes.sunrise, true),
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

    private fun getTodayString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}