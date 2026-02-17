package com.example.quranapp.ui.screens.prayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quranapp.ui.theme.*

// ─────────────────────────────────────────────
// Prayer Progress Card
// ─────────────────────────────────────────────

@Composable
fun PrayerProgressCard(
    count: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) count.toFloat() / total.toFloat() else 0f

    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepEmerald)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(120.dp),
                color = White.copy(alpha = 0.12f),
                trackColor = White.copy(alpha = 0.12f),
                strokeWidth = 14.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                gapSize = 0.dp
            )

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(120.dp),
                color = MediumEmerald,
                trackColor = androidx.compose.ui.graphics.Color.Transparent,
                strokeWidth = 14.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                gapSize = 0.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$count/$total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "prayed",
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Imsak & Sunrise Info Bar
// ─────────────────────────────────────────────

@Composable
fun ImsakSunriseBar(
    imsakTime: String,
    sunriseTime: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(LightEmerald)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Imsak $imsakTime  |  Sunrise $sunriseTime",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = DeepEmerald,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────
// Prayer List Item (dark themed)
// ─────────────────────────────────────────────

@Composable
fun PrayerItem(
    name: String,
    time: String,
    isPrayed: Boolean,
    isNext: Boolean = false,
    isPassed: Boolean = false,
    countdown: String = "",
    onCheckClick: () -> Unit
) {
    val containerColor = if (isNext) DeepEmerald else DeepEmerald
    // User requested 0.7f alpha for prayed items
    val contentAlpha = if (isPrayed) 0.7f else if (isPassed || isNext) 1f else 0.35f 

    val borderStroke = if (isNext) {
        androidx.compose.foundation.BorderStroke(2.dp, MediumEmerald) // Thicker, solid border for Next
    } else {
        null
    }

    Card(
        modifier = Modifier.fillMaxWidth().run {
             // Optional: apply alpha to the whole card if prayed?
             // User said "buat kartunya sedikit lebih transparan"
             if(isPrayed) this.alpha(0.7f) else this
        },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPrayed) MediumEmerald else White.copy(alpha = if (isPassed) 0.12f else 0.05f)
                    )
                    .clickable(enabled = isPassed || isNext) { onCheckClick() }, // Allow checking if passed or next
                contentAlignment = Alignment.Center
            ) {
                if (isPrayed) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Prayer name
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = White.copy(alpha = if (isNext) 1f else contentAlpha),
                modifier = Modifier.weight(1f)
            )

            // Countdown badge (for next prayer)
            if (isNext && countdown.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MediumEmerald.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = countdown,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MediumEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Time
            // User: "ganti warna teks jam-nya jadi abu-abu" if prayed
            val timeColor = if (isPrayed) TextGray else White.copy(alpha = if (isNext) 1f else contentAlpha * 0.7f)
            
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = timeColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Notification icon
            Icon(
                imageVector = if (isPrayed || isNext) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = "Notification",
                tint = White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// Mark All as Prayed Button
// ─────────────────────────────────────────────

@Composable
fun MarkAllPrayedButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val containerColor = if (enabled) BrightEmerald else DeepEmerald.copy(alpha = 0.5f)
    val contentColor = if (enabled) White else White.copy(alpha = 0.5f)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = DeepEmerald.copy(alpha = 0.5f),
            contentColor = contentColor,
            disabledContentColor = White.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = if (enabled) "Mark all as prayed" else "Track upcoming prayer soon",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}