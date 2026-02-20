package com.example.quranapp.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    /**
     * Apply saved language to context.
     * @param context base context
     * @param languageCode "in" | "en" | "" for system default
     */
    fun wrapContext(context: Context, languageCode: String): Context {
        if (languageCode.isBlank()) return context

        val locale = when (languageCode) {
            "in" -> Locale("in")
            "en" -> Locale.ENGLISH
            else -> Locale.getDefault()
        }

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return context.createConfigurationContext(config)
    }
}
