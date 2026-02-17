package com.example.quranapp.ui.screens.quran.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.quranapp.data.model.Surah
import com.example.quranapp.ui.theme.*
import com.example.quranapp.R
import com.example.quranapp.ui.components.AppCard

// ─────────────────────────────────────────────
// Tab Selector (Surah / Juz)
// ─────────────────────────────────────────────

@Composable
fun QuranTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LightEmerald),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabButton("Surah", selectedTab == 0, Modifier.weight(1f)) { onTabSelected(0) }
        TabButton("Juz", selectedTab == 1, Modifier.weight(1f)) { onTabSelected(1) }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DeepEmerald else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) White else DeepEmerald
        )
    }
}

// ─────────────────────────────────────────────
// Surah List Item (dark themed)
// ─────────────────────────────────────────────

@Composable
fun SurahItem(
    surah: Surah,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = LightEmerald
    ) {
        val arabicFont = UthmaniHafs

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number
            Text(
                text = surah.number.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald.copy(alpha = 0.5f),
                modifier = Modifier.width(30.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Arabic name
            Text(
                text = surah.arabicName.replace("سُورَةُ", "").trim(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = arabicFont,
                    fontSize = 24.sp,
                    letterSpacing = 0.sp
                ),
                fontWeight = FontWeight.Bold,
                color = DeepEmerald.copy(alpha = 0.7f),
                modifier = Modifier.width(80.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Latin name + Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = surah.englishName, // Removed Ayat count
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Juz Surah Card (light themed, individual card per surah entry)
// ─────────────────────────────────────────────

@Composable
fun JuzSurahCard(
    entry: com.example.quranapp.data.model.JuzSurahEntry,
    onClick: () -> Unit = {}
) {
    val arabicFont = UthmaniHafs

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = LightEmerald
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arabic name (left side)
            Text(
                text = entry.arabicName.replace("سُورَةُ", "").trim(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = arabicFont,
                    fontSize = 24.sp,
                    letterSpacing = 0.sp
                ),
                fontWeight = FontWeight.Bold,
                color = DeepEmerald.copy(alpha = 0.7f),
                modifier = Modifier.width(80.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Surah name + ayah range
            Column {
                Text(
                    text = entry.surahName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Aya ${entry.ayahRange}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    }
}