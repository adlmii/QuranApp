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
import com.example.quranapp.data.local.entity.PrayerStatusEntity
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
    val isPassed: Boolean = false,
    val countdown: String = ""
)

data class PrayerUiState(
    val prayerList: List<PrayerItemState> = emptyList(),
    val prayedCount: Int = 0,
    val totalPrayer: Int = 5,
    val nextPrayerName: String = "--",
    val nextPrayerTime: String = "--:--",
    val timeToNextPrayer: String = "Calculating...",
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

    // In-memory cache of prayer statuses loaded from DB
    private val prayedStatusMap = mutableMapOf<String, Boolean>()

    init {
        updateDateInfo()
        loadPrayerStatuses()
        startPrayerCountdown()
    }

    fun updateLocation(latitude: Double, longitude: Double, context: Context) {
        lat = latitude
        lon = longitude
        viewModelScope.launch {
            getAddressName(latitude, longitude, context)
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
                _uiState.value = _uiState.value.copy(location = "Unknown")
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
                // Re-calculate to merge the loaded statuses with prayer times
                calculatePrayerTimes()
            }
        }
    }

    fun togglePrayed(index: Int) {
        val currentList = _uiState.value.prayerList.toMutableList()
        val item = currentList[index]
        
        // Hanya bisa checklist jika waktu sholat sudah lewat
        if (item.isPassed) {
            val newStatus = !item.isPrayed
            currentList[index] = item.copy(isPrayed = newStatus)
            val newCount = currentList.count { it.isPrayed }

            _uiState.value = _uiState.value.copy(
                prayerList = currentList,
                prayedCount = newCount,
                canMarkAll = currentList.any { it.isPassed && !it.isPrayed }
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

    fun markAllPrayed() {
        val currentList = _uiState.value.prayerList.map { 
            if (it.isPassed) it.copy(isPrayed = true) else it
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

            // Hitung Countdown
            val diffMillis = if (nextPrayerTimeDate != null) nextPrayerTimeDate.time - now else 0L
            val countdownText = if (diffMillis > 0) {
                val hours = diffMillis / (1000 * 60 * 60)
                val minutes = (diffMillis / (1000 * 60)) % 60
                val seconds = (diffMillis / 1000) % 60

                if (hours > 0) "in ${hours}h ${minutes}m" else "in ${minutes}m ${seconds}s"
            } else {
                "Now"
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
                val prayerName = prayerRepository.getPrayerName(type)
                val isPassed = now >= timeDate.time
                // Check persisted status from Room
                val wasPrayed = prayedStatusMap[prayerName] ?: false

                PrayerItemState(
                    name = prayerName,
                    time = formatter.format(timeDate).lowercase(),
                    isNext = (nextPrayer == type),
                    isPrayed = wasPrayed,
                    isPassed = isPassed,
                    countdown = if (nextPrayer == type) countdownText else ""
                )
            }

            val formattedNextTime = if (nextPrayerTimeDate != null) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(nextPrayerTimeDate)
            } else "--:--"

            _uiState.value = _uiState.value.copy(
                prayerList = uiList,
                nextPrayerName = prayerRepository.getPrayerName(nextPrayer),
                nextPrayerTime = formattedNextTime,
                timeToNextPrayer = countdownText,
                imsakTime = formatter.format(imsakTime).lowercase(),
                sunriseTime = formatter.format(sunriseTime).lowercase(),
                prayedCount = uiList.count { it.isPrayed },
                canMarkAll = uiList.any { it.isPassed && !it.isPrayed }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTodayString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }
}