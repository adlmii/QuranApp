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
    val imsak: Date,
    val sunrise: Date
)

class PrayerRepository {

    fun calculatePrayerTimes(lat: Double, lon: Double): PrayerSchedule {
        val coordinates = Coordinates(lat, lon)
        // Use Singapore parameters as default/placeholder similar to previous code
        val params = CalculationMethod.SINGAPORE.parameters 
        params.madhab = Madhab.SHAFI

        val now = Date()
        val todayComponents = DateComponents.from(now)
        val prayerTimes = PrayerTimes(coordinates, todayComponents, params)

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
            imsak = imsak,
            sunrise = sunrise
        )
    }

    // Helper to map Prayer enum to Display Name
    fun getPrayerName(prayer: Prayer): String {
        return when(prayer) {
            Prayer.FAJR -> "Fajr \u2728"
            Prayer.SUNRISE -> "Syuruq \u2600"
            Prayer.DHUHR -> "Dhuhr \uD83C\uDF24"
            Prayer.ASR -> "Asr \uD83C\uDF25"
            Prayer.MAGHRIB -> "Maghrib \uD83C\uDF05"
            Prayer.ISHA -> "Isha'a \uD83C\uDF19"
            else -> "--"
        }
    }
}
