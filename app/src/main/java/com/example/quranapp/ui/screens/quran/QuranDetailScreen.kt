package com.example.quranapp.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.example.quranapp.ui.theme.UthmaniHafs
import com.example.quranapp.ui.theme.CreamBackground
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
import com.example.quranapp.R
import com.example.quranapp.ui.components.AppHeader
import com.example.quranapp.ui.screens.quran.components.Fireworks
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import com.example.quranapp.ui.theme.*
import androidx.compose.ui.text.font.FontFamily

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

    val arabicFont = UthmaniHafs

    Scaffold(
        topBar = {
            DetailHeader(
                title = uiState.surahDetail?.name ?: "Loading...",
                onBack = { navController.popBackStack() },
                isPageMode = uiState.isPageMode,
                onToggleMode = { viewModel.toggleViewMode() },
                sessionProgress = uiState.sessionProgress
            )
        },
        containerColor = CreamBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Session Progress Bar
                if (uiState.sessionProgress > 0) {
                     LinearProgressIndicator(
                        progress = { uiState.sessionProgress / 5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = Color(0xFFD4AF37), // Gold
                        trackColor = Color(0xFFE0E0E0)
                    )
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepEmerald)
                    }
                } else if (uiState.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = Color.Red
                        )
                    }
                } else {
                    uiState.surahDetail?.let { detail ->
                        if (uiState.isPageMode) {
                            // Page Mode
                            if (uiState.pages.isNotEmpty()) {
                                val pagerState = rememberPagerState(pageCount = { uiState.pages.size })
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.weight(1f)
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
                                modifier = Modifier.weight(1f),
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
            
            // Reward Overlay Removed
        }
    }
}

@Composable
fun DetailHeader(
    title: String,
    onBack: () -> Unit,
    isPageMode: Boolean,
    onToggleMode: () -> Unit,
    sessionProgress: Int = 0 // Add progress param
) {
    AppHeader(
        title = title,
        onBackClick = onBack,
        backgroundColor = CreamBackground,
        contentColor = DeepEmerald,
        actions = {
            // Session Indicator
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(end = 16.dp)) {
                CircularProgressIndicator(
                    progress = { sessionProgress / 5f },
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFD4AF37), // Gold
                    trackColor = Color(0xFFE0E0E0),
                    strokeWidth = 3.dp
                )
                Text(
                    text = "$sessionProgress",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            // View Mode Toggle
            IconButton(onClick = onToggleMode) {
                Icon(
                    imageVector = if (isPageMode) Icons.Default.Menu else Icons.Default.List,
                    contentDescription = "Toggle View",
                    tint = DeepEmerald
                )
            }
        }
    )
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
                color = MediumEmerald.copy(
                    alpha = 0.2f
                ), // Light gray like screenshot
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$surahNumber:${ayah.number}",
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
        // New data is Uthmani Hafs (Standard Unicode), so no need for TajweedHelper parsing
        
        Text(
            text = ayah.arabic,
            style = HeadlineQuran,
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
