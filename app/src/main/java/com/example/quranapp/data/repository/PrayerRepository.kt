package com.example.quranapp.data.repository

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Date

data class PrayerSchedule(
    val prayerTimes: PrayerTimes,
    val nextPrayer: Prayer,
    val nextPrayerTime: Date?,
    val currentPrayer: Prayer?,
    val isInGracePeriod: Boolean,
    val imsak: Date,
    val sunrise: Date
)

class PrayerRepository {

    companion object {
        const val GRACE_PERIOD_MS = 10 * 60 * 1000L // 10 minutes
        const val PRE_REMINDER_MS = 10 * 60 * 1000L // 10 minutes before
    }

    fun calculatePrayerTimes(lat: Double, lon: Double): PrayerSchedule {
        val coordinates = Coordinates(lat, lon)
        val params = CalculationMethod.SINGAPORE.parameters
        params.madhab = Madhab.SHAFI

        val now = Date()
        val todayComponents = DateComponents.from(now)
        val prayerTimes = PrayerTimes(coordinates, todayComponents, params)

        // Determine the current prayer (most recently entered)
        val activePrayer = prayerTimes.currentPrayer()
        val activePrayerTime = if (activePrayer != null && activePrayer != Prayer.NONE) {
            prayerTimes.timeForPrayer(activePrayer)
        } else null

        // Check grace period: within 10 minutes of the active prayer's start
        val isInGracePeriod = if (activePrayerTime != null) {
            (now.time - activePrayerTime.time) <= GRACE_PERIOD_MS
        } else false

        val currentPrayer = if (isInGracePeriod) activePrayer else null

        var nextPrayer = prayerTimes.nextPrayer()
        var nextPrayerTime = prayerTimes.timeForPrayer(nextPrayer)

        // Handle case where all prayers for today are passed (next is Fajr tomorrow)
        if (nextPrayer == Prayer.NONE || nextPrayerTime == null) {
            val tomorrow = Date(now.time + 86400000) // + 1 day
            val tomorrowComponents = DateComponents.from(tomorrow)
            val tomorrowPrayerTimes = PrayerTimes(coordinates, tomorrowComponents, params)

            nextPrayer = Prayer.FAJR
            nextPrayerTime = tomorrowPrayerTimes.fajr
        }

        // Imsak is typically 10 minutes before Fajr
        val imsak = Date(prayerTimes.fajr.time - (10 * 60 * 1000))
        val sunrise = prayerTimes.sunrise

        return PrayerSchedule(
            prayerTimes = prayerTimes,
            nextPrayer = nextPrayer,
            nextPrayerTime = nextPrayerTime,
            currentPrayer = currentPrayer,
            isInGracePeriod = isInGracePeriod,
            imsak = imsak,
            sunrise = sunrise
        )
    }

    // Helper to map Prayer enum to Display Name
    fun getPrayerName(prayer: Prayer): String {
        return when(prayer) {
            Prayer.FAJR -> "Fajr ✨"
            Prayer.SUNRISE -> "Syuruq ☀"
            Prayer.DHUHR -> "Dhuhr 🌤"
            Prayer.ASR -> "Asr 🌥"
            Prayer.MAGHRIB -> "Maghrib 🌅"
            Prayer.ISHA -> "Isha'a 🌙"
            else -> "--"
        }
    }

    // Clean name without emoji (for notifications)
    fun getPrayerNameClean(prayer: Prayer): String {
        return when(prayer) {
            Prayer.FAJR -> "Fajr"
            Prayer.SUNRISE -> "Syuruq"
            Prayer.DHUHR -> "Dhuhr"
            Prayer.ASR -> "Asr"
            Prayer.MAGHRIB -> "Maghrib"
            Prayer.ISHA -> "Isha'a"
            else -> "--"
        }
    }
}
