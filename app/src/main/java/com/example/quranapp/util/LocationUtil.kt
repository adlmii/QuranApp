package com.example.quranapp.util

import android.content.Context
import android.location.Geocoder
import com.example.quranapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationUtil {

    suspend fun getAddressName(lat: Double, lon: Double, context: Context): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    address.subLocality ?: address.locality ?: "Unknown"
                } else {
                    context.getString(R.string.location_unknown)
                }
            } catch (e: Exception) {
                context.getString(R.string.location_unknown)
            }
        }
    }
}