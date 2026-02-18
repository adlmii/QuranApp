package com.example.quranapp.ui.screens.home

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quranapp.data.repository.PrayerRepository
import com.example.quranapp.data.repository.PrayerSchedule
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

// Pastikan UiState tetap sama seperti kodemu
data class HomeUiState(
    val gregorianDate: String = "",
    val hijriDate: String = "",
    val matsuratType: String = "Pagi",
    val nextPrayerName: String = "--",
    val nextPrayerTime: String = "--:--",
    val timeToNextPrayer: String = "Calculating...",
    val location: String = "Locating...",
    val lat: Double = -6.2088, // Default Jakarta (lebih umum)
    val lon: Double = 106.8456,
    val quranCurrentMinutes: Int = 0,
    val quranTargetMinutes: Int = 25,
    val lastReadSurah: String? = null,
    val lastReadSurahArabic: String? = null,
    val lastReadNumber: Int = 1,
    val lastReadAyah: Int = 1
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        updateDateInfo()
        startPrayerCountdown()
        loadUserData()
    }

    fun updateLocation(latitude: Double, longitude: Double, context: Context) {
        viewModelScope.launch {
            // Update koordinat
            _uiState.value = _uiState.value.copy(lat = latitude, lon = longitude)

            // Recalculate prayer times immediately with new loc
            calculatePrayerTimes()

            // Get Address Name (Async)
            getAddressName(latitude, longitude, context)
        }
    }

    private suspend fun getAddressName(lat: Double, lon: Double, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                // Menggunakan Deprecated method untuk kompatibilitas luas,
                // atau gunakan versi listener untuk API 33+
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val district = address.subLocality ?: address.locality ?: "Unknown"

                    _uiState.value = _uiState.value.copy(location = district)
                }
            } catch (e: Exception) {
                // Handle error (misal tidak ada internet)
                _uiState.value = _uiState.value.copy(location = "Unknown Location")
            }
        }
    }

    private fun startPrayerCountdown() {
        viewModelScope.launch {
            while (isActive) {
                calculatePrayerTimes()
                delay(1000L) // Update setiap detik
            }
        }
    }

    private val prayerRepository = PrayerRepository()

    private fun calculatePrayerTimes() {
        try {
            // Use Repository
            val schedule = prayerRepository.calculatePrayerTimes(_uiState.value.lat, _uiState.value.lon)
            
            val nextPrayerName = prayerRepository.getPrayerName(schedule.nextPrayer)
            val targetTime = schedule.nextPrayerTime
            val now = Date()

            // Hitung Countdown
            val diffMillis = if (targetTime != null) targetTime.time - now.time else 0L

            val countdownText = if (diffMillis > 0) {
                val hours = diffMillis / (1000 * 60 * 60)
                val minutes = (diffMillis / (1000 * 60)) % 60
                val seconds = (diffMillis / 1000) % 60

                if (hours > 0) {
                    String.format("in %dh %dm", hours, minutes)
                } else {
                    String.format("in %dm %ds", minutes, seconds)
                }
            } else {
                "Now"
            }

            // Format waktu sholat (misal: 18:30)
            val timeFormatter = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = if (targetTime != null) timeFormatter.format(targetTime) else "--:--"

            _uiState.value = _uiState.value.copy(
                nextPrayerName = nextPrayerName,
                nextPrayerTime = formattedTime,
                timeToNextPrayer = countdownText
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback UI or log
            _uiState.value = _uiState.value.copy(
                nextPrayerName = "Error",
                timeToNextPrayer = "Retry..."
            )
        }
    }

    private fun updateDateInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today = LocalDate.now()
            val now = LocalTime.now()

            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())

            // HijrahDate (Perlu API Level 26+)
            val hijrahDate = HijrahDate.from(today)
            val hijriFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

            val isMorning = now.hour < 14
            val matsurat = if (isMorning) "Pagi" else "Petang"

            _uiState.value = _uiState.value.copy(
                gregorianDate = today.format(formatter),
                hijriDate = hijriFormatter.format(hijrahDate),
                matsuratType = matsurat
            )
        }
    }

    private fun loadUserData() {
        // Simulasi data user
        _uiState.value = _uiState.value.copy(
            quranCurrentMinutes = 15,
            quranTargetMinutes = 25,
            lastReadSurah = "Al-Kahf",
            lastReadSurahArabic = "الكهف",
            lastReadNumber = 18,
            lastReadAyah = 10
        )
    }
}