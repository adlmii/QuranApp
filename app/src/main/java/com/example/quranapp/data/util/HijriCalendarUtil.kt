package com.example.quranapp.data.util

import android.os.Build
import com.example.quranapp.data.model.IslamicEvent
import com.example.quranapp.data.model.islamicEvents
import java.time.LocalDate
import java.time.YearMonth

/**
 * Hijri date info container.
 */
data class HijriDateInfo(
    val year: Int,
    val month: Int,
    val day: Int,
    val monthName: String
)

/**
 * Hijri month names in Indonesian/Arabic.
 */
val hijriMonthNames = listOf(
    "Muharram", "Safar", "Rabi'ul Awal", "Rabi'ul Akhir",
    "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban",
    "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
)

/**
 * Special months with gradient highlighting.
 * Muharram(1), Rajab(7), Ramadhan(9), Dzulhijjah(12)
 */
val specialHijriMonths = setOf(1, 7, 9, 12)

/**
 * Convert a Gregorian LocalDate to HijriDateInfo.
 * Uses java.time.chrono.HijrahDate (API 26+).
 * @param correction +1 or -1 day adjustment for rukyat differences
 */
fun toHijriDate(date: LocalDate, correction: Int = 0): HijriDateInfo {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val adjusted = date.plusDays(correction.toLong())
        val hijri = java.time.chrono.HijrahDate.from(adjusted)
        val year = hijri.get(java.time.temporal.ChronoField.YEAR)
        val month = hijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        val day = hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
        return HijriDateInfo(
            year = year,
            month = month,
            day = day,
            monthName = hijriMonthNames.getOrElse(month - 1) { "Unknown" }
        )
    }
    // Fallback for API < 26 (approximate Kuwaiti algorithm)
    return approximateHijri(date, correction)
}

/**
 * Get total days in a Hijri month using HijrahDate.
 */
fun getHijriMonthLength(hijriYear: Int, hijriMonth: Int): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return try {
            val hijriDate = java.time.chrono.HijrahDate.of(hijriYear, hijriMonth, 1)
            hijriDate.lengthOfMonth()
        } catch (e: Exception) {
            30 // fallback
        }
    }
    return if (hijriMonth % 2 == 1) 30 else 29 // approximate
}

/**
 * Get Islamic events for a specific Hijri month.
 */
fun getEventsForHijriMonth(hijriMonth: Int): List<IslamicEvent> {
    return islamicEvents.filter { it.hijriMonth == hijriMonth }
}

/**
 * Check if a Hijri month is "special" (deserves gradient highlight).
 */
fun isSpecialMonth(hijriMonth: Int): Boolean {
    return hijriMonth in specialHijriMonths
}

/**
 * Get Hijri month name.
 */
fun getHijriMonthName(month: Int): String {
    return hijriMonthNames.getOrElse(month - 1) { "Unknown" }
}

// ────────────────────────────────────────
// Approximate Hijri conversion (for API < 26)
// Based on simplified Kuwaiti algorithm
// ────────────────────────────────────────

private fun approximateHijri(date: LocalDate, correction: Int): HijriDateInfo {
    val adjusted = date.plusDays(correction.toLong())
    val jd = gregorianToJulianDay(adjusted.year, adjusted.monthValue, adjusted.dayOfMonth)
    return julianDayToHijri(jd)
}

private fun gregorianToJulianDay(year: Int, month: Int, day: Int): Double {
    val a = (14 - month) / 12
    val y = year + 4800 - a
    val m = month + 12 * a - 3
    return day + (153 * m + 2) / 5.0 + 365 * y + y / 4.0 - y / 100.0 + y / 400.0 - 32045
}

private fun julianDayToHijri(jd: Double): HijriDateInfo {
    val l = Math.floor(jd - 1948439.5 + 10632).toInt()
    val n = Math.floor((l - 1) / 10631.0).toInt()
    val lRem = l - 10631 * n + 354
    val j = (Math.floor((10985 - lRem) / 5316.0) * Math.floor((50 * lRem) / 17719.0) +
            Math.floor(lRem / 5670.0) * Math.floor((43 * lRem) / 15238.0)).toInt()
    val lFinal = lRem - Math.floor((30 - j) / 15.0 * (17719 + j) / 2.0 + j * (15238 - j) / 2.0 / 43.0).toInt() + 29
    val month = Math.floor((24 * lFinal) / 709.0).toInt()
    val day = lFinal - Math.floor((709 * month) / 24.0).toInt()
    val year = 30 * n + j - 30

    val clampedMonth = month.coerceIn(1, 12)
    return HijriDateInfo(
        year = year,
        month = clampedMonth,
        day = day.coerceIn(1, 30),
        monthName = hijriMonthNames.getOrElse(clampedMonth - 1) { "Unknown" }
    )
}
