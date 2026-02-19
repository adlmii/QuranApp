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
            title = "⏰ Persiapan $prayerName"
            body = "Waktu $prayerName tinggal 10 menit lagi. Segera bersiap!"
        } else if (prayerName == "Syuruq") {
            title = "☀️ Waktu Terbit"
            body = "Matahari telah terbit. Waktu Dhuha sebentar lagi!"
        } else {
            title = "🕌 $prayerName"
            body = "Waktu $prayerName telah masuk. Ayo sholat!"
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
                    "🌿 Sudah Sholat $prayerName?",
                    "Jangan lupa catat progres sholatmu hari ini ya 🌿"
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
                        val formatter = java.time.format.DateTimeFormatter.ofPattern(
                            "d MMMM yyyy", java.util.Locale.getDefault()
                        )
                        val hijriStr = formatter.format(hijri)

                        showNotification(
                            context,
                            "hijri_change".hashCode(),
                            "🌙 Pergantian Hari Hijriah",
                            "Selamat memasuki malam $hijriStr. Semoga berkah! 🌙"
                        )
                    } catch (e: Exception) {
                        // Fallback if date conversion fails
                        showNotification(
                            context,
                            "hijri_change".hashCode(),
                            "🌙 Pergantian Hari Hijriah",
                            "Selamat memasuki malam hari baru Hijriah. Semoga berkah! 🌙"
                        )
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
