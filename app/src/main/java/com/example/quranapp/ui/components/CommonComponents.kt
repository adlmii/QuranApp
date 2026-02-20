package com.example.quranapp.ui.components

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quranapp.R
import com.example.quranapp.ui.theme.*
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Timer

@Composable
fun AppHeader(
    gregorianDate: String,
    hijriDate: String,
    location: String = "",
    onDateClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Date section (clickable → Calendar)
        Column(
            modifier = if (onDateClick != null) {
                Modifier.clickable { onDateClick() }
            } else Modifier
        ) {
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
                .clip(RoundedCornerShape(50))
                .background(LightEmerald)
                .padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = stringResource(R.string.content_desc_location),
                tint = DeepEmerald,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = location.ifEmpty { stringResource(R.string.label_locating) },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
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
    isNow: Boolean = false,
    nowLabel: String = "",
    modifier: Modifier = Modifier
) {
    // Pulse animation for "Now" state
    val pulseAlpha: Float = if (isNow) {
        val infiniteTransition = rememberInfiniteTransition(label = "nowPulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        alpha
    } else 1f

    Card(
        modifier = modifier
            .height(180.dp),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = DeepEmeraldGradient)
        ) {
            // Decorative Circle
            Box(
                modifier = Modifier
                    .offset(x = 80.dp, y = (-20).dp)
                    .size(150.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(White.copy(alpha = 0.05f))
            )

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
                        .background(
                            if (isNow) GoldAccent.copy(alpha = pulseAlpha)
                            else White.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isNow) stringResource(R.string.badge_now) else stringResource(R.string.badge_next),
                        color = if (isNow) DeepEmerald else White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Prayer name + time
                Column {
                    Text(
                        text = if (isNow && nowLabel.isNotEmpty()) nowLabel else prayerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prayerTime,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        fontSize = 32.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isNow) stringResource(R.string.label_ongoing) else countDown,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = GoldAccent
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Generic Progress Card (Shared)
// ─────────────────────────────────────────────

@Composable
fun GenericProgressCard(
    progress: Float,
    mainText: String,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = DeepEmeraldGradient),
            contentAlignment = Alignment.Center
        ) {
            // Track (flat ends) - Single layer only
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(120.dp),
                color = White.copy(alpha = 0.1f),
                trackColor = androidx.compose.ui.graphics.Color.Transparent, // Avoid double opacity
                strokeWidth = 12.dp,
                gapSize = 0.dp
            )

            // Progress (rounded ends) - Only if > 0
            if (progress > 0.001f) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    color = GoldAccent,
                    trackColor = androidx.compose.ui.graphics.Color.Transparent,
                    strokeWidth = 12.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    gapSize = 0.dp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = White.copy(alpha = 0.7f)
                )
            }
        }
    }
}