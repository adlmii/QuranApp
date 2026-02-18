package com.example.quranapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.quranapp.data.repository.PrayerRepository
import java.util.Date

class PrayerAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        // Request code offsets: prayer index * 10 + alert type
        // Alert type: 0 = pre-reminder, 1 = adzan
        private const val PRE_REMINDER = 0
        private const val ADZAN = 1
    }

    /**
     * Schedule pre-reminder and adzan alarms for each enabled prayer.
     * @param prayers List of Triple(cleanName, time, skipPreReminder)
     */
    fun scheduleAll(prayers: List<Triple<String, Date, Boolean>>) {
        val now = System.currentTimeMillis()

        prayers.forEachIndexed { index, (name, time, skipPreReminder) ->
            val prayerTimeMs = time.time

            // Pre-reminder (10 min before) — skip for Sunrise
            if (!skipPreReminder) {
                val preReminderMs = prayerTimeMs - PrayerRepository.PRE_REMINDER_MS
                if (preReminderMs > now) {
                    scheduleAlarm(
                        triggerTime = preReminderMs,
                        requestCode = index * 10 + PRE_REMINDER,
                        prayerName = name,
                        isPreReminder = true
                    )
                }
            }

            // Exact time notification
            if (prayerTimeMs > now) {
                scheduleAlarm(
                    triggerTime = prayerTimeMs,
                    requestCode = index * 10 + ADZAN,
                    prayerName = name,
                    isPreReminder = false
                )
            }
        }
    }

    private fun scheduleAlarm(
        triggerTime: Long,
        requestCode: Int,
        prayerName: String,
        isPreReminder: Boolean
    ) {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra("prayer_name", prayerName)
            putExtra("is_pre_reminder", isPreReminder)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback to inexact alarm if exact alarm permission denied
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Cancel all existing alarms (useful before rescheduling)
     */
    fun cancelAll() {
        for (i in 0 until 50) { // Max 5 prayers * 10 offset range
            val intent = Intent(context, PrayerNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}
