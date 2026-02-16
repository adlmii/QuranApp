package com.example.quranapp.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quranapp.ui.theme.*
import com.example.quranapp.ui.theme.CalligraphyIcon

// ─────────────────────────────────────────────
// Quran Progress Card
// ─────────────────────────────────────────────

@Composable
fun QuranProgressCard(
    currentMinutes: Int,
    targetMinutes: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (targetMinutes > 0) currentMinutes.toFloat() / targetMinutes.toFloat() else 0f

    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepEmerald)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Track (flat ends)
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(120.dp),
                color = White.copy(alpha = 0.12f),
                trackColor = White.copy(alpha = 0.12f),
                strokeWidth = 14.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                gapSize = 0.dp
            )

            // Progress (rounded ends)
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
                    text = "$currentMinutes/$targetMinutes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "min",
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Al-Ma'tsurat Card
// ─────────────────────────────────────────────

@Composable
fun AlMatsuratCard(
    type: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepEmerald),
        onClick = { onClick(if (type == "Pagi") "MORNING" else "EVENING") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title + type badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Al-Ma'tsurat",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MediumEmerald.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = White
                    )
                }
            }

            // Arrow circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Open",
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Recent Surah Item
// ─────────────────────────────────────────────

@Composable
fun RecentSurahItem(
    number: Int,
    title: String,
    arabicName: String,
    ayah: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightEmerald),
        onClick = { /* Navigate to surah */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah number circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DeepEmerald),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Arabic Name
            Text(
                text = arabicName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald.copy(alpha = 0.8f),
                modifier = Modifier.padding(end = 8.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Surah info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ayah $ayah",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            // Last Read badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepEmerald)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Last Read",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}