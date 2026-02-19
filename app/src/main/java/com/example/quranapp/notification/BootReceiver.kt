package com.example.quranapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.quranapp.data.local.UserPreferencesRepository
import com.example.quranapp.data.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Reschedules all notification alarms after device reboot.
 * Android clears all AlarmManager alarms on shutdown.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load saved location from DataStore
                val userPrefs = UserPreferencesRepository(context)
                val (lat, lon) = userPrefs.lastLocation.first()

                // Calculate prayer times for today
                val prayerRepository = PrayerRepository()
                val schedule = prayerRepository.calculatePrayerTimes(lat, lon)
                val prayerTimes = schedule.prayerTimes

                // Build prayer list for scheduling (same as PrayerViewModel)
                val prayers = listOf(
                    Triple(prayerRepository.getPrayerNameClean(com.batoulapps.adhan.Prayer.FAJR), prayerTimes.fajr, false),
                    Triple(prayerRepository.getPrayerNameClean(com.batoulapps.adhan.Prayer.SUNRISE), prayerTimes.sunrise, true),
                    Triple(prayerRepository.getPrayerNameClean(com.batoulapps.adhan.Prayer.DHUHR), prayerTimes.dhuhr, false),
                    Triple(prayerRepository.getPrayerNameClean(com.batoulapps.adhan.Prayer.ASR), prayerTimes.asr, false),
                    Triple(prayerRepository.getPrayerNameClean(com.batoulapps.adhan.Prayer.MAGHRIB), prayerTimes.maghrib, false),
                    Triple(prayerRepository.getPrayerNameClean(com.batoulapps.adhan.Prayer.ISHA), prayerTimes.isha, false)
                )

                val scheduler = PrayerAlarmScheduler(context)
                scheduler.cancelAll()
                scheduler.scheduleAll(prayers)
                scheduler.scheduleExtras(
                    fajrTime = prayerTimes.fajr,
                    asrTime = prayerTimes.asr,
                    maghribTime = prayerTimes.maghrib
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
