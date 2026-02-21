package com.example.quranapp.util

import com.example.quranapp.data.util.toHijriDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtil {

    fun getGregorianDate(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
        return today.format(formatter)
    }

    fun getHijriDate(): String {
        val today = LocalDate.now()
        val hijri = toHijriDate(today)

        return "${hijri.day} ${hijri.monthName} ${hijri.year} H"
    }
}