package com.example.quranapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.quranapp.R
import com.example.quranapp.data.repository.PrayerRepository
import java.util.Calendar
import java.util.Date

class PrayerAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val PRE_REMINDER = 0
        private const val ADZAN = 1
        private const val POST_PRAYER = 2
        private const val RC_MATSURAT_PAGI = 100
        private const val RC_MATSURAT_PETANG = 101
        private const val RC_DAILY_GOAL = 102
        private const val RC_HIJRI_CHANGE = 103
        private const val RC_ALKAHFI_REMINDER = 104
        private const val ALKAHFI_DELAY_MINUTES = 30
    }

    
    private fun getPrayerDisplayName(originalName: String, prayerTime: Date): String {
        val cal = Calendar.getInstance().apply { time = prayerTime }
        val isFriday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        return if (isFriday && originalName == "Dhuhr") {
            context.getString(R.string.prayer_jumat)
        } else {
            originalName
        }
    }

    /**
     * Schedule pre-reminder and adzan alarms for each enabled prayer.
     * @param prayers List of Triple(cleanName, time, skipPreReminder)
     */
    fun scheduleAll(prayers: List<Triple<String, Date, Boolean>>) {
        val now = System.currentTimeMillis()

        prayers.forEachIndexed { index, (name, time, skipPreReminder) ->
            val prayerTimeMs = time.time
            val displayName = getPrayerDisplayName(name, time)

            // Pre-reminder (10 min before) — skip for Sunrise
            if (!skipPreReminder) {
                val preReminderMs = prayerTimeMs - PrayerRepository.PRE_REMINDER_MS
                if (preReminderMs > now) {
                    scheduleAlarm(
                        triggerTime = preReminderMs,
                        requestCode = index * 10 + PRE_REMINDER,
                        prayerName = displayName,
                        isPreReminder = true
                    )
                }
            }

            // Exact time notification
            if (prayerTimeMs > now) {
                scheduleAlarm(
                    triggerTime = prayerTimeMs,
                    requestCode = index * 10 + ADZAN,
                    prayerName = displayName,
                    isPreReminder = false
                )
            }

            // Post-prayer check (15 min after) — skip for Sunrise
            if (!skipPreReminder) {
                val postPrayerMs = prayerTimeMs + 15 * 60 * 1000L
                if (postPrayerMs > now) {
                    scheduleTypedAlarm(
                        triggerTime = postPrayerMs,
                        requestCode = index * 10 + POST_PRAYER,
                        notificationType = "post_prayer",
                        extraData = displayName
                    )
                }
            }
        }
    }

    /**
     * Schedule extra contextual notifications:
     * - Ma'tsurat Pagi (Fajr + 30m)
     * - Ma'tsurat Petang (Asr + 30m)
     * - Daily Quran Goal (20:00)
     * - Hijri Date Change (Maghrib)
     */
    fun scheduleExtras(fajrTime: Date, asrTime: Date, maghribTime: Date) {
        val now = System.currentTimeMillis()

        // Ma'tsurat Pagi: Fajr + 30 min
        val matsuratPagiMs = fajrTime.time + 30 * 60 * 1000L
        if (matsuratPagiMs > now) {
            scheduleTypedAlarm(
                triggerTime = matsuratPagiMs,
                requestCode = RC_MATSURAT_PAGI,
                notificationType = "matsurat_pagi"
            )
        }

        // Ma'tsurat Petang: Asr + 30 min
        val matsuratPetangMs = asrTime.time + 30 * 60 * 1000L
        if (matsuratPetangMs > now) {
            scheduleTypedAlarm(
                triggerTime = matsuratPetangMs,
                requestCode = RC_MATSURAT_PETANG,
                notificationType = "matsurat_petang"
            )
        }

        // Daily Quran Goal: 20:00 today
        val goalCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (goalCalendar.timeInMillis > now) {
            scheduleTypedAlarm(
                triggerTime = goalCalendar.timeInMillis,
                requestCode = RC_DAILY_GOAL,
                notificationType = "quran_goal"
            )
        }

        // Hijri Date Change: at Maghrib
        if (maghribTime.time > now) {
            scheduleTypedAlarm(
                triggerTime = maghribTime.time,
                requestCode = RC_HIJRI_CHANGE,
                notificationType = "hijri_change"
            )
        }

        // Al-Kahfi Reminder: Thursday night, 30 min after Maghrib (malam Jumat = Kamis setelah Maghrib)
        val maghribCal = Calendar.getInstance().apply { time = maghribTime }
        val isThursday = maghribCal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY
        val alkahfiMs = maghribTime.time + ALKAHFI_DELAY_MINUTES * 60 * 1000L
        if (isThursday && alkahfiMs > now) {
            scheduleTypedAlarm(
                triggerTime = alkahfiMs,
                requestCode = RC_ALKAHFI_REMINDER,
                notificationType = "alkahfi_reminder"
            )
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
        setExactAlarm(triggerTime, requestCode, intent)
    }

    private fun scheduleTypedAlarm(
        triggerTime: Long,
        requestCode: Int,
        notificationType: String,
        extraData: String = ""
    ) {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra("notification_type", notificationType)
            if (extraData.isNotEmpty()) putExtra("extra_data", extraData)
        }
        setExactAlarm(triggerTime, requestCode, intent)
    }

    private fun setExactAlarm(triggerTime: Long, requestCode: Int, intent: Intent) {
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
        // Prayer alarms: 0..59 (6 prayers * 10 offset range)
        // Extra alarms: 100..104
        val codes = (0 until 60) + listOf(RC_MATSURAT_PAGI, RC_MATSURAT_PETANG, RC_DAILY_GOAL, RC_HIJRI_CHANGE, RC_ALKAHFI_REMINDER)
        for (i in codes) {
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
