package com.example.quranapp.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quranapp.R
import com.example.quranapp.data.model.IslamicEvent
import com.example.quranapp.ui.screens.calendar.CalendarDay
import com.example.quranapp.ui.theme.*

// ─────────────────────────────────────────────
// Month Navigator (with today label above)
// ─────────────────────────────────────────────

@Composable
fun MonthNavigator(
    monthName: String,
    year: Int,
    hijriLabel: String,
    hijriYear: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Month nav row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.content_desc_prev_month),
                    tint = DeepEmerald,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$monthName $year",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
                Text(
                    text = "$hijriLabel $hijriYear",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.content_desc_next_month),
                    tint = DeepEmerald,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Day Headers (Sen - Ahd)
// ─────────────────────────────────────────────

@Composable
private fun getDayHeaders(): List<String> = listOf(
    stringResource(R.string.day_mon),
    stringResource(R.string.day_tue),
    stringResource(R.string.day_wed),
    stringResource(R.string.day_thu),
    stringResource(R.string.day_fri),
    stringResource(R.string.day_sat),
    stringResource(R.string.day_sun)
)

@Composable
fun DayHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        getDayHeaders().forEach { day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Calendar Grid
// ─────────────────────────────────────────────

@Composable
fun CalendarGrid(
    days: List<CalendarDay?>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        DayHeaderRow()
        Spacer(modifier = Modifier.height(6.dp))

        // Build rows of 7
        val rows = days.chunked(7)
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0 until 7) {
                    val day = row.getOrNull(i)
                    Box(modifier = Modifier.weight(1f)) {
                        if (day != null) {
                            CalendarCell(day = day)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Calendar Cell (dual date)
// ─────────────────────────────────────────────

@Composable
fun CalendarCell(day: CalendarDay) {
    val bgModifier = when {
        day.isToday -> Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, GoldAccent, RoundedCornerShape(12.dp))
            .background(GoldAccent.copy(alpha = 0.08f))
        else -> Modifier
            .clip(RoundedCornerShape(12.dp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp)
            .then(bgModifier)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gregorian day (bold, prominent)
        Text(
            text = day.gregorianDay.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (day.isToday) GoldAccent else DeepEmerald,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        // Hijri day (smaller, below)
        Text(
            text = day.hijriDay.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (day.isSpecialHijriMonth) GoldAccent.copy(alpha = 0.8f) else TextGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        // Event dot indicator
        if (day.events.isNotEmpty()) {
            Spacer(modifier = Modifier.height(1.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(GoldAccent)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Event Item Card (compact: name + date badge only)
// ─────────────────────────────────────────────

@Composable
fun EventItem(
    event: IslamicEvent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightEmerald),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Event name only
            Text(
                text = event.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DeepEmerald,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Hijri date badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${event.hijriDay}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }
        }
    }
}
