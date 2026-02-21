package com.example.quranapp.util

import com.batoulapps.adhan.Prayer
import com.example.quranapp.R
import java.util.Date

object PrayerFormatUtil {

    // logika translasi Nama Sholat
    fun getPrayerName(prayer: Prayer): UiText {
        val resId = when(prayer) {
            Prayer.FAJR -> R.string.prayer_fajr
            Prayer.SUNRISE -> R.string.prayer_syuruq
            Prayer.DHUHR -> R.string.prayer_dhuhr
            Prayer.ASR -> R.string.prayer_asr
            Prayer.MAGHRIB -> R.string.prayer_maghrib
            Prayer.ISHA -> R.string.prayer_isha
            else -> return UiText.DynamicString("--")
        }
        return UiText.StringResource(resId)
    }

    // Hitung Mundur (Countdown)
    fun getCountdownText(targetTime: Date?): UiText {
        val now = System.currentTimeMillis()
        val diffMillis = if (targetTime != null) targetTime.time - now else 0L

        return if (diffMillis > 0) {
            val hours = diffMillis / (1000 * 60 * 60)
            val minutes = (diffMillis / (1000 * 60)) % 60
            val seconds = (diffMillis / 1000) % 60

            if (hours > 0) {
                UiText.StringResource(R.string.countdown_hours_mins, hours, minutes)
            } else {
                UiText.StringResource(R.string.countdown_mins_secs, minutes, seconds)
            }
        } else {
            UiText.StringResource(R.string.label_now)
        }
    }
}