package com.example.quranapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.batoulapps.adhan.Prayer
import com.example.quranapp.MainActivity
import com.example.quranapp.R
import com.example.quranapp.data.local.UserPreferencesRepository
import com.example.quranapp.data.repository.PrayerRepository
import com.example.quranapp.util.LocaleHelper
import com.example.quranapp.util.LocationUtil
import com.example.quranapp.util.PrayerFormatUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 4×2 Prayer Widget — shows next/current prayer + all 5 prayer times
 */
class PrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val localizedContext = LocaleHelper.getLocalizedContext(context)
        for (widgetId in appWidgetIds) {
            updateWidget(localizedContext, context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        localizedContext: Context,
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        try {
            val (lat, lon) = runBlocking {
                UserPreferencesRepository(context).lastLocation.first()
            }

            val repo = PrayerRepository()
            val schedule = repo.calculatePrayerTimes(lat, lon)
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

            val isNow = schedule.isInGracePeriod && schedule.currentPrayer != null
            val displayPrayer = if (isNow) schedule.currentPrayer!! else schedule.nextPrayer

            val prayerName = PrayerFormatUtil.getPrayerName(displayPrayer).asString(localizedContext)
            val prayerTime = if (isNow && schedule.currentPrayer != null) {
                schedule.prayerTimes.timeForPrayer(schedule.currentPrayer)
            } else schedule.nextPrayerTime

            val timeStr = if (prayerTime != null) fmt.format(prayerTime) else "--:--"
            val countdownStr = formatCountdown(localizedContext, isNow, prayerTime)
            val label = if (isNow) {
                localizedContext.getString(R.string.label_now).uppercase()
            } else {
                localizedContext.getString(R.string.widget_next_prayer)
            }
            val location = runBlocking { LocationUtil.getAddressName(lat, lon, context) }

            val views = RemoteViews(context.packageName, R.layout.widget_prayer_large)
            views.setTextViewText(R.id.tv_widget_label, label)
            views.setTextViewText(R.id.tv_widget_prayer_name, prayerName)
            views.setTextViewText(R.id.tv_widget_time, timeStr)
            views.setTextViewText(R.id.tv_widget_countdown, countdownStr)
            views.setTextViewText(R.id.tv_widget_location, "📍 $location")

            // All 5 prayer times
            views.setTextViewText(R.id.tv_fajr_time, fmt.format(schedule.prayerTimes.fajr))
            views.setTextViewText(R.id.tv_dhuhr_time, fmt.format(schedule.prayerTimes.dhuhr))
            views.setTextViewText(R.id.tv_asr_time, fmt.format(schedule.prayerTimes.asr))
            views.setTextViewText(R.id.tv_maghrib_time, fmt.format(schedule.prayerTimes.maghrib))
            views.setTextViewText(R.id.tv_isha_time, fmt.format(schedule.prayerTimes.isha))

            // Localized prayer names
            views.setTextViewText(R.id.tv_fajr_name, PrayerFormatUtil.getPrayerName(Prayer.FAJR).asString(localizedContext))
            views.setTextViewText(R.id.tv_dhuhr_name, PrayerFormatUtil.getPrayerName(Prayer.DHUHR).asString(localizedContext))
            views.setTextViewText(R.id.tv_asr_name, PrayerFormatUtil.getPrayerName(Prayer.ASR).asString(localizedContext))
            views.setTextViewText(R.id.tv_maghrib_name, PrayerFormatUtil.getPrayerName(Prayer.MAGHRIB).asString(localizedContext))
            views.setTextViewText(R.id.tv_isha_name, PrayerFormatUtil.getPrayerName(Prayer.ISHA).asString(localizedContext))

            // Highlight CURRENT prayer pill (the one happening right now) with gold outline
            val pillMap = mapOf(
                Prayer.FAJR to R.id.pill_fajr,
                Prayer.DHUHR to R.id.pill_dhuhr,
                Prayer.ASR to R.id.pill_asr,
                Prayer.MAGHRIB to R.id.pill_maghrib,
                Prayer.ISHA to R.id.pill_isha
            )
            pillMap.values.forEach {
                views.setInt(it, "setBackgroundResource", R.drawable.widget_prayer_pill)
            }
            // Highlight the prayer whose time slot we're currently in
            // (e.g. between Dhuhr and Asr → highlight Dhuhr)
            val activePrayer = schedule.prayerTimes.currentPrayer()
            if (activePrayer != null && activePrayer != Prayer.NONE) {
                pillMap[activePrayer]?.let {
                    views.setInt(it, "setBackgroundResource", R.drawable.widget_prayer_pill_active)
                }
            }

            views.setOnClickPendingIntent(R.id.widget_root, createOpenAppIntent(context))
            appWidgetManager.updateAppWidget(widgetId, views)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: at least show the layout with click handler
            val views = RemoteViews(context.packageName, R.layout.widget_prayer_large)
            views.setOnClickPendingIntent(R.id.widget_root, createOpenAppIntent(context))
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            updateProviderWidgets(context, PrayerWidgetProvider::class.java)
        }

        internal fun updateProviderWidgets(context: Context, providerClass: Class<*>) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, providerClass))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, providerClass).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }

        internal fun createOpenAppIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("target_route", "prayers")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        internal fun formatCountdown(context: Context, isNow: Boolean, prayerTime: java.util.Date?): String {
            if (isNow) return context.getString(R.string.label_now)
            if (prayerTime == null) return ""
            val diff = prayerTime.time - System.currentTimeMillis()
            if (diff <= 0) return context.getString(R.string.label_now)
            val h = diff / (1000 * 60 * 60)
            val m = (diff / (1000 * 60)) % 60
            return if (h > 0) "${h}j ${m}m lagi" else "${m} menit lagi"
        }
    }
}
