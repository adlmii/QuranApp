package com.example.quranapp.ui.screens.qibla

import kotlin.math.*

object QiblaUtils {
    // Kaaba Coordinates
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    /**
     * Calculates the Qibla bearing from the given location.
     * Returns the bearing in degrees (0-360), where 0 is North.
     */
    fun calculateQiblaDirection(lat: Double, lng: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LNG - lng)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        var theta = atan2(y, x)
        theta = Math.toDegrees(theta)

        // Normalize to 0-360
        return (theta + 360) % 360
    }
}
