package com.example.quranapp.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.quranapp.data.model.IslamicEvent
import com.example.quranapp.data.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Single cell in the calendar grid.
 */
data class CalendarDay(
    val gregorianDay: Int,
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriYear: Int,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val isSpecialHijriMonth: Boolean,
    val events: List<IslamicEvent>,
    val date: LocalDate
)

data class CalendarUiState(
    val displayYear: Int = 0,
    val displayMonth: Int = 0, // 1-12
    val displayMonthName: String = "",
    val hijriMonthLabel: String = "",
    val hijriYearLabel: String = "",
    val calendarDays: List<CalendarDay?> = emptyList(), // null = empty cell
    val events: List<IslamicEvent> = emptyList(),
    val todayLabel: String = ""
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var currentYearMonth: YearMonth = YearMonth.now()

    init {
        buildCalendar()
    }

    fun goToPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1)
        buildCalendar()
    }

    fun goToNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1)
        buildCalendar()
    }

    private fun buildCalendar() {
        val year = currentYearMonth.year
        val month = currentYearMonth.monthValue
        val daysInMonth = currentYearMonth.lengthOfMonth()
        val firstDayOfMonth = currentYearMonth.atDay(1)
        val today = LocalDate.now()

        // Monday = 1 (ISO), we want Mon as first column
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=Mon ... 7=Sun

        // Leading empty cells (days before the 1st)
        val leadingBlanks = firstDayOfWeek - 1 // Mon-based

        val cells = mutableListOf<CalendarDay?>()

        // Add leading nulls
        repeat(leadingBlanks) { cells.add(null) }

        // Track which Hijri months appear in this Gregorian month
        val hijriMonthsInView = mutableSetOf<Int>()
        var firstHijriInfo: HijriDateInfo? = null
        var lastHijriInfo: HijriDateInfo? = null

        // Build day cells
        for (day in 1..daysInMonth) {
            val date = currentYearMonth.atDay(day)
            val hijri = toHijriDate(date)

            if (day == 1) firstHijriInfo = hijri
            if (day == daysInMonth) lastHijriInfo = hijri
            hijriMonthsInView.add(hijri.month)

            val dayEvents = getEventsForHijriMonth(hijri.month).filter { it.hijriDay == hijri.day }

            cells.add(
                CalendarDay(
                    gregorianDay = day,
                    hijriDay = hijri.day,
                    hijriMonth = hijri.month,
                    hijriYear = hijri.year,
                    isToday = date == today,
                    isCurrentMonth = true,
                    isSpecialHijriMonth = isSpecialMonth(hijri.month),
                    events = dayEvents,
                    date = date
                )
            )
        }

        // Collect all events for the visible Hijri months
        val allEvents = hijriMonthsInView.flatMap { getEventsForHijriMonth(it) }
            .sortedWith(compareBy({ it.hijriMonth }, { it.hijriDay }))

        // Hijri label: show range if spanning 2 months
        val hijriLabel = if (firstHijriInfo != null && lastHijriInfo != null) {
            if (firstHijriInfo!!.month == lastHijriInfo!!.month) {
                firstHijriInfo!!.monthName
            } else {
                "${firstHijriInfo!!.monthName} – ${lastHijriInfo!!.monthName}"
            }
        } else ""

        val hijriYearLabel = if (firstHijriInfo != null && lastHijriInfo != null) {
            if (firstHijriInfo!!.year == lastHijriInfo!!.year) {
                "${firstHijriInfo!!.year} H"
            } else {
                "${firstHijriInfo!!.year}–${lastHijriInfo!!.year} H"
            }
        } else ""

        val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale("id", "ID"))

        // Today label
        val todayHijri = toHijriDate(today)
        val todayLabel = "${today.dayOfMonth} ${today.month.getDisplayName(TextStyle.FULL, Locale("id"))} ${today.year} / ${todayHijri.day} ${todayHijri.monthName} ${todayHijri.year} H"

        _uiState.value = CalendarUiState(
            displayYear = year,
            displayMonth = month,
            displayMonthName = monthName.replaceFirstChar { it.uppercase() },
            hijriMonthLabel = hijriLabel,
            hijriYearLabel = hijriYearLabel,
            calendarDays = cells,
            events = allEvents,
            todayLabel = todayLabel
        )
    }
}
