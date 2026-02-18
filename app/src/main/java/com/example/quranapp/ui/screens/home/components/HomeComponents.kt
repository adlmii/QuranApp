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
import com.example.quranapp.ui.components.GenericProgressCard
import com.example.quranapp.ui.theme.CalligraphyIcon
import androidx.compose.ui.unit.sp

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

    GenericProgressCard(
        progress = progress,
        mainText = "$currentMinutes/$targetMinutes",
        subText = "min",
        modifier = modifier
    )
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
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp), // Restored to compact banner size
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        onClick = { onClick(if (type == "Pagi") "MORNING" else "EVENING") }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = MidnightGradient),
            contentAlignment = Alignment.Center // Center content vertically & horizontally
        ) {
            // Decorative background pattern/shape
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.05f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title + type badge
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Al-Ma'tsurat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent.copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepEmeraldDark
                        )
                    }
                }

                // Arrow circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open",
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SandBackground), // Warmer background
        elevation = CardDefaults.cardElevation(2.dp),
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
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(DeepEmerald.copy(alpha = 0.1f)), // Softer circle
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Surah info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ayah $ayah",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Last Read",
                            style = MaterialTheme.typography.labelSmall,
                            color = androidx.compose.ui.graphics.Color(0xFF8D6E63), // Brownish gold
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            // Arabic Name
            Text(
                text = arabicName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Normal,
                color = DeepEmerald,
                fontFamily = UthmaniHafs // Ensure this is available or use default
            )
        }
    }
}