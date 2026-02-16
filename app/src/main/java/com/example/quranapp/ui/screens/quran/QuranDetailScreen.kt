package com.example.quranapp.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.data.model.Ayah
import com.example.quranapp.data.model.TajweedRule
import com.example.quranapp.ui.theme.*
import com.example.quranapp.util.TajweedUtils
import com.example.quranapp.util.TajweedHelper
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import com.example.quranapp.R

@Composable
fun QuranDetailScreen(
    navController: NavController,
    surahNumber: Int,
    viewModel: QuranDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(surahNumber) {
        viewModel.loadSurah(surahNumber)
    }

    val arabicFont = FontFamily(Font(R.font.lpmq_isepmisbah))

    Scaffold(
        topBar = {
            DetailHeader(
                title = uiState.surahDetail?.name ?: "Loading...",
                onBack = { navController.popBackStack() },
                isPageMode = uiState.isPageMode,
                onToggleMode = { viewModel.toggleViewMode() }
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = DeepEmerald
                )
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                uiState.surahDetail?.let { detail ->
                    if (uiState.isPageMode) {
                        // Page Mode
                        if (uiState.pages.isNotEmpty()) {
                            val pagerState = rememberPagerState(pageCount = { uiState.pages.size })
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { pageIndex ->
                                val pageAyahs = uiState.pages[pageIndex]
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "Page ${pageAyahs.firstOrNull()?.page ?: "-"}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextGray,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    items(pageAyahs) { ayah ->
                                        AyahItem(ayah = ayah, surahNumber = surahNumber, arabicFont = arabicFont)
                                    }
                                }
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("No pages available")
                            }
                        }
                    } else {
                        // Ayah Mode
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Basmalah except for Surah 1 (it's part of ayahs) and Surah 9
                            if (surahNumber != 1 && surahNumber != 9) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontFamily = arabicFont,
                                                letterSpacing = 3.sp
                                            ), 
                                            textAlign = TextAlign.Center,
                                            color = DeepEmerald
                                        )
                                    }
                                }
                            }

                            items(detail.ayahs) { ayah ->
                                AyahItem(ayah = ayah, surahNumber = surahNumber, arabicFont = arabicFont)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailHeader(
    title: String,
    onBack: () -> Unit,
    isPageMode: Boolean,
    onToggleMode: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LightEmerald)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = DeepEmerald,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DeepEmerald,
            modifier = Modifier.weight(1f)
        )

        // Toggle Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(LightEmerald)
                .clickable { onToggleMode() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isPageMode) "Ayah View" else "Page View",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald
            )
        }
    }
}

@Composable
fun AyahItem(ayah: Ayah, surahNumber: Int, arabicFont: FontFamily) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Badge Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFE0E0E0), // Light gray like screenshot
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aya $surahNumber:${ayah.number}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "More",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            // Action icons can go here
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Arabic Text (Right Aligned)
        // Use TajweedHelper to parse inline tags [n]text[/n]
        val tajweedText = remember(ayah.arabic) {
            TajweedHelper.parseTajweed(ayah.arabic)
        }
        
        Text(
            text = tajweedText,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = arabicFont,
                lineHeight = 60.sp,
                letterSpacing = 3.sp
            ),
            color = DeepEmerald,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Translation
        Text(
            text = ayah.translation,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = TextGray,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    HorizontalDivider(color = LightEmerald.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(top = 16.dp))
}

// buildTajweedText function removed in favor of TajweedHelper
