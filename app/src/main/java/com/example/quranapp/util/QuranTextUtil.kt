package com.example.quranapp.util

/**
 * Utility functions for formatting Quran text display.
 */
object QuranTextUtil {

    private val ARABIC_DIGITS = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")

    /**
     * Converts a Latin ayah number (e.g. 1, 2, 3) to Arabic-Indic numerals (١, ٢, ٣)
     * and wraps it with the End of Ayah ornament (U+06DD ۝).
     *
     * In Uthmanic Hafs font, U+06DD renders as the ornamental circle
     * and the following Arabic-Indic digits are composited inside it.
     */
    fun formatAyahNumber(number: Int): String {
        val arabicNumberString = number.toString().map { char ->
            if (char.isDigit()) ARABIC_DIGITS[char.toString().toInt()] else char
        }.joinToString("")

        // UthmaniHafs font renders Arabic-Indic digits with their own ornamental circle
        // No need for explicit U+06DD which causes a double circle
        return " $arabicNumberString "
    }
}

