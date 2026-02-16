package com.example.quranapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quranapp.ui.theme.*

@Composable
fun AppHeader(
    gregorianDate: String,
    hijriDate: String,
    location: String = "Locating..."
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Date section
        Column {
            Text(
                text = gregorianDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hijriDate,
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }

        // Location badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(LightEmerald)
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = DeepEmerald,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = location,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = DeepEmerald
            )
        }
    }
}

// ─────────────────────────────────────────────
// Prayer Card (shared between Home & Prayer)
// ─────────────────────────────────────────────

@Composable
fun PrayerCard(
    prayerName: String,
    prayerTime: String,
    countDown: String,
    modifier: Modifier = Modifier
) {
    val isNow = countDown == "Now"

    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepEmerald)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Now / Next badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isNow) MediumEmerald else White.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isNow) "Now" else "Next",
                    color = White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Prayer name + time
            Column {
                Text(
                    text = prayerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = prayerTime,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }

            // Countdown
            Text(
                text = countDown,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = White
            )
        }
    }
}