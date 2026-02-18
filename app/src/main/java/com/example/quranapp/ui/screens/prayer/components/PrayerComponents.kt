package com.example.quranapp.ui.screens.prayer.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quranapp.ui.theme.*
import com.example.quranapp.ui.components.GenericProgressCard

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

    GenericProgressCard(
        progress = progress,
        mainText = "$count/$total",
        subText = "prayed",
        modifier = modifier
    )
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
    isNow: Boolean = false,
    isPassed: Boolean = false,
    isNotificationOn: Boolean = true,
    countdown: String = "",
    onCheckClick: () -> Unit,
    onNotificationToggle: () -> Unit = {}
) {
    val containerColor = DeepEmerald
    val contentAlpha = when {
        isNow -> 1f
        isPrayed -> 0.7f
        isPassed || isNext -> 1f
        else -> 0.35f
    }

    // Pulse animation for "Now" state
    val borderAlpha: Float = if (isNow) {
        val infiniteTransition = rememberInfiniteTransition(label = "nowItemPulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "borderPulse"
        )
        alpha
    } else 1f

    val borderStroke = when {
        isNow -> BorderStroke(2.5.dp, GoldAccent.copy(alpha = borderAlpha))
        isNext -> BorderStroke(2.dp, MediumEmerald)
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth().run {
             if(isPrayed) this.alpha(0.7f) else this
        },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = when {
            isNow -> 6.dp
            isNext -> 4.dp
            else -> 0.dp
        })
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
                        if (isPrayed) MediumEmerald
                        else White.copy(alpha = if (isPassed || isNow) 0.12f else 0.05f)
                    )
                    .clickable(enabled = isPassed || isNext || isNow) { onCheckClick() },
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
                color = White.copy(alpha = if (isNext || isNow) 1f else contentAlpha),
                modifier = Modifier.weight(1f)
            )

            // "Now" badge or countdown badge
            if (isNow && countdown.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Now",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            } else if (isNext && countdown.isNotEmpty()) {
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
            val timeColor = White.copy(alpha = if (isNext || isNow) 1f else contentAlpha * 0.7f)
            
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = timeColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Notification icon (clickable toggle)
            Icon(
                imageVector = if (isNotificationOn) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = "Toggle Notification",
                tint = if (isNotificationOn) GoldAccent.copy(alpha = 0.7f) else White.copy(alpha = 0.25f),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onNotificationToggle() }
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