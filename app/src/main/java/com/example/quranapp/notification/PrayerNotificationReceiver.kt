package com.example.quranapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.quranapp.R
import com.example.quranapp.data.local.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "prayer_notifications"
        const val CHANNEL_NAME = "Prayer Notifications"
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val notificationType = intent.getStringExtra("notification_type")

        if (notificationType != null) {
            handleTypedNotification(context, notificationType, intent)
        } else {
            handlePrayerNotification(context, intent)
        }
    }

    private fun handlePrayerNotification(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val isPreReminder = intent.getBooleanExtra("is_pre_reminder", false)

        val notificationId = prayerName.hashCode() + if (isPreReminder) 0 else 1000

        val title: String
        val body: String

        if (isPreReminder) {
            title = context.getString(R.string.notif_pre_title, prayerName)
            body = context.getString(R.string.notif_pre_body, prayerName)
        } else if (prayerName == "Syuruq") {
            title = context.getString(R.string.notif_syuruq_title)
            body = context.getString(R.string.notif_syuruq_body)
        } else {
            title = context.getString(R.string.notif_prayer_title, prayerName)
            body = context.getString(R.string.notif_prayer_body, prayerName)
        }

        showNotification(context, notificationId, title, body)
    }

    private fun handleTypedNotification(context: Context, type: String, intent: Intent) {
        when (type) {
            "post_prayer" -> {
                val prayerName = intent.getStringExtra("extra_data") ?: "Sholat"
                showNotification(
                    context,
                    "post_prayer_$prayerName".hashCode(),
                    context.getString(R.string.notif_post_title, prayerName),
                    context.getString(R.string.notif_post_body)
                )
            }

            "matsurat_pagi" -> {
                showNotification(
                    context,
                    "matsurat_pagi".hashCode(),
                    "📖 Dzikir Pagi",
                    "Waktu yang tepat untuk Dzikir Pagi. Yuk, baca Al-Ma'tsurat sekarang 📖"
                )
            }

            "matsurat_petang" -> {
                showNotification(
                    context,
                    "matsurat_petang".hashCode(),
                    "📖 Dzikir Petang",
                    "Waktu yang tepat untuk Dzikir Petang. Yuk, baca Al-Ma'tsurat sekarang 📖"
                )
            }

            "quran_goal" -> {
                // Conditional: only show if today's minutes < target
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val userPrefs = UserPreferencesRepository(context)
                        val todayMinutes = userPrefs.todayMinutes.first()
                        val targetMinutes = userPrefs.targetMinutes.first()

                        if (todayMinutes < targetMinutes) {
                            showNotification(
                                context,
                                "quran_goal".hashCode(),
                                "✨ Sedikit Lagi!",
                                "Baca Quran sebentar lagi yuk untuk mencapai target harimu ✨"
                            )
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            "hijri_change" -> {
                // Compute the new Hijri date (tomorrow in Hijri since Maghrib = new day)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        val tomorrow = java.time.LocalDate.now().plusDays(1)
                        val hijri = java.time.chrono.HijrahDate.from(tomorrow)
                        
                        // Check if it is the start of a new Hijri month
                        val dayOfMonth = hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                        
                        if (dayOfMonth == 1) {
                            val formatter = java.time.format.DateTimeFormatter.ofPattern(
                                "MMMM yyyy", java.util.Locale.getDefault()
                            )
                            val monthStr = formatter.format(hijri)

                            showNotification(
                                context,
                                "hijri_change".hashCode(),
                                context.getString(R.string.notif_hijri_month_title),
                                context.getString(R.string.notif_hijri_month_body, monthStr)
                            )
                        }
                    } catch (e: Exception) {
                        // Fallback or error handling if needed
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, id: Int, title: String, body: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi waktu sholat dan pengingat"
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
