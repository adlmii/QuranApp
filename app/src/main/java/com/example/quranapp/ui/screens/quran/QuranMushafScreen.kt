package com.example.quranapp.ui.screens.quran

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quranapp.ui.theme.UthmaniHafs
import com.example.quranapp.ui.theme.HeadlineQuran
import com.example.quranapp.ui.theme.DeepEmerald
import com.example.quranapp.ui.theme.TextBlack
import com.example.quranapp.util.QuranTextUtil
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranMushafScreen(viewModel: QuranViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Total 604 halaman dalam Mushaf Uthmani
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 604 }
    )

    HorizontalPager(
        state = pagerState,
        reverseLayout = true, // KUNCI: Swipe dari kanan ke kiri (khas Arab)
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        // pageIndex mulai dari 0, halaman Quran mulai dari 1
        val actualPage = pageIndex + 1

        // Tarik data per halaman dari ViewModel
        val ayahsOnPage by viewModel.getPageData(actualPage).collectAsState()

        if (ayahsOnPage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .graphicsLayer {
                        // 3D Paper Flip Effect
                        val pageOffset = (
                            (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                        ).absoluteValue

                        cameraDistance = 8 * density
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        rotationY = lerp(
                            start = 0f,
                            stop = 90f,
                            fraction = pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header for the physical page (Juz & Primary Surah)
                    val firstAyah = ayahsOnPage.first()
                    val primarySurah = firstAyah.surahNameArabic
                    MushafPageHeader(
                        juzNumber = firstAyah.ayah.juzNumber, 
                        surahName = primarySurah,
                        isBookmarked = uiState.bookmarkSurah == firstAyah.ayah.surahId && uiState.bookmarkAyah == firstAyah.ayah.verseNumber,
                        onBookmarkClick = { 
                            viewModel.saveBookmark(
                                surahNumber = firstAyah.ayah.surahId,
                                ayahNumber = firstAyah.ayah.verseNumber,
                                surahName = firstAyah.surahNameSimple
                            ) 
                        }
                    )

                    // Group ayahs on this page by their Surah
                    val ayahsBySurah = ayahsOnPage.groupBy { it.ayah.surahId }

                    ayahsBySurah.forEach { (surahId, ayahsInSurah) ->
                        val isFirstAyah = ayahsInSurah.any { it.ayah.verseNumber == 1 }

                        // Draw Header & Basmalah if this is the start of the Surah
                        if (isFirstAyah) {
                            val surahName = ayahsInSurah.first().surahNameArabic
                            MushafSurahHeader(surahName = surahName)

                            // Draw Basmalah for all Surahs except Al-Fatihah (1) and At-Tawbah (9)
                            // Al-Fatihah already has Basmalah as its first verse in the API.
                            if (surahId != 1 && surahId != 9) {
                                MushafBasmalah()
                            }
                        }

                        // Combine texts for this Surah
                        val combinedText = ayahsInSurah.joinToString(separator = "") { item ->
                            val cleanText = item.ayah.textUthmani.replace("\u06DD", "").trimEnd()
                            cleanText + QuranTextUtil.formatAyahNumber(item.ayah.verseNumber)
                        }

                        Text(
                            text = combinedText,
                            style = HeadlineQuran.copy(textDirection = TextDirection.Rtl),
                            textAlign = TextAlign.Justify,
                            color = TextBlack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Loading indicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun MushafSurahHeader(surahName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(
                width = 1.dp,
                color = DeepEmerald,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "سُورَة $surahName",
            fontFamily = UthmaniHafs,
            fontSize = 24.sp,
            color = DeepEmerald
        )
    }
}

@Composable
fun MushafBasmalah() {
    Text(
        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
        fontFamily = UthmaniHafs,
        fontSize = 24.sp,
        color = TextBlack,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun MushafPageHeader(
    juzNumber: Int, 
    surahName: String,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Juz Information (Left side)
            Text(
                text = "Juz $juzNumber",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald.copy(alpha = 0.7f)
            )
            // Right side: Surah Name & Bookmark
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "سُورَة $surahName",
                    fontFamily = UthmaniHafs,
                    fontSize = 18.sp,
                    color = DeepEmerald.copy(alpha = 0.7f),
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark Page",
                        tint = if (isBookmarked) DeepEmerald else DeepEmerald.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        HorizontalDivider(
            color = DeepEmerald.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
