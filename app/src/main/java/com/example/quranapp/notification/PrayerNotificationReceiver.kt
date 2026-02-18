package com.example.quranapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.quranapp.R

class PrayerNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "prayer_notifications"
        const val CHANNEL_NAME = "Prayer Notifications"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val isPreReminder = intent.getBooleanExtra("is_pre_reminder", false)

        createNotificationChannel(context)

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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
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

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
