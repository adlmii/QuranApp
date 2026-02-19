package com.example.quranapp.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.example.quranapp.ui.theme.UthmaniHafs
import com.example.quranapp.ui.theme.CreamBackground
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
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
import com.example.quranapp.ui.screens.quran.components.QuranSettingsSheet
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import com.example.quranapp.ui.theme.*
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuranDetailScreen(
    navController: NavController,
    surahNumber: Int,
    initialAyah: Int = 1,
    viewModel: QuranDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(surahNumber) {
        viewModel.loadSurah(surahNumber)
    }

    val arabicFont = UthmaniHafs

    // Scroll tracking state
    val listState = rememberLazyListState()

    // Scroll to initial ayah after content loads
    LaunchedEffect(uiState.surahDetail) {
        val ayahs = uiState.surahDetail?.ayahs ?: return@LaunchedEffect
        if (initialAyah > 1 && ayahs.isNotEmpty()) {
            val hasBasmalah = surahNumber != 1 && surahNumber != 9
            val headerOffset = if (hasBasmalah) 1 else 0
            val targetIndex = (initialAyah - 1 + headerOffset).coerceIn(0, ayahs.size - 1 + headerOffset)
            listState.scrollToItem(targetIndex)
        }
    }

    // Auto-save scroll position with debounce
    val firstVisibleItem by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisibleItem) {
        // Debounce: wait 500ms before saving to avoid excessive writes
        delay(500)
        val ayahs = uiState.surahDetail?.ayahs ?: return@LaunchedEffect
        // firstVisibleItemIndex accounts for header items (basmalah = 1 item offset for most surahs)
        val hasBasmalah = surahNumber != 1 && surahNumber != 9
        val headerOffset = if (hasBasmalah) 1 else 0
        val ayahIndex = (firstVisibleItem - headerOffset).coerceIn(0, ayahs.size - 1)
        if (ayahs.isNotEmpty()) {
            viewModel.saveLastRead(ayahs[ayahIndex].number)
        }
    }

    // Settings Bottom Sheet
    if (showSettings) {
        QuranSettingsSheet(
            isPageMode = uiState.isPageMode,
            arabicFontSize = uiState.arabicFontSize,
            totalAyahs = uiState.surahDetail?.ayahs?.size ?: 0,
            onToggleMode = { viewModel.toggleViewMode() },
            onJumpToAyah = { ayahNum ->
                val ayahs = uiState.surahDetail?.ayahs ?: return@QuranSettingsSheet
                val hasBasmalah = surahNumber != 1 && surahNumber != 9
                val headerOffset = if (hasBasmalah) 1 else 0
                val index = (ayahNum - 1 + headerOffset).coerceIn(0, ayahs.size - 1 + headerOffset)
                coroutineScope.launch {
                    listState.animateScrollToItem(index)
                }
                showSettings = false
            },
            onFontSizeChange = { viewModel.updateFontSize(it) },
            onDismiss = { showSettings = false }
        )
    }

    Scaffold(
        topBar = {
            DetailHeader(
                title = uiState.surahDetail?.name ?: "Loading...",
                onBack = { navController.popBackStack() },
                isPageMode = uiState.isPageMode,
                onToggleMode = { viewModel.toggleViewMode() },
                sessionProgress = uiState.sessionProgress,
                targetMinutes = uiState.targetMinutes,
                onSettingsClick = { showSettings = true }
            )
            SetStatusBarColor(CreamBackground)
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
                        progress = { uiState.sessionProgress / uiState.targetMinutes.toFloat() },
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
                            // Page Mode — Placeholder
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = null,
                                        tint = DeepEmerald.copy(alpha = 0.3f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Mode Halaman",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepEmerald
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Masih dalam Pengembangan",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextGray
                                    )
                                }
                            }
                        } else {
                            // Ayah Mode (with scroll tracking)
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // ── Previous Surah Card ──
                                if (surahNumber > 1 && uiState.prevSurahName != null) {
                                    item {
                                        SurahNavigationCard(
                                            label = "Surah Sebelumnya",
                                            surahName = uiState.prevSurahName!!,
                                            onClick = {
                                                navController.navigate("quran_detail/${surahNumber - 1}") {
                                                    popUpTo("quran_detail/${surahNumber}") { inclusive = true }
                                                }
                                            }
                                        )
                                    }
                                }

                                // Basmalah except for Surah 1 (it's part of ayahs) and Surah 9
                                if (surahNumber != 1 && surahNumber != 9) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
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
                                    val isBookmarked = uiState.bookmarkSurah == surahNumber &&
                                            uiState.bookmarkAyah == ayah.number
                                    AyahItem(
                                        ayah = ayah,
                                        surahNumber = surahNumber,
                                        arabicFont = arabicFont,
                                        arabicFontSize = uiState.arabicFontSize,
                                        isBookmarked = isBookmarked,
                                        onBookmarkClick = { viewModel.saveBookmark(ayah.number) }
                                    )
                                }

                                // ── Next Surah Card ──
                                if (surahNumber < 114 && uiState.nextSurahName != null) {
                                    item {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SurahNavigationCard(
                                            label = "Lanjut ke",
                                            surahName = uiState.nextSurahName!!,
                                            onClick = {
                                                navController.navigate("quran_detail/${surahNumber + 1}") {
                                                    popUpTo("quran_detail/${surahNumber}") { inclusive = true }
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
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
    onToggleMode: () -> Unit,
    sessionProgress: Int = 0,
    targetMinutes: Int = 5,
    onSettingsClick: () -> Unit = {}
) {
    AppHeader(
        title = title,
        onBackClick = onBack,
        backgroundColor = CreamBackground,
        contentColor = DeepEmerald,
        actions = {
            // Session Indicator
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(end = 8.dp)) {
                CircularProgressIndicator(
                    progress = { sessionProgress / targetMinutes.toFloat() },
                    modifier = Modifier.size(24.dp),
                    color = GoldAccent,
                    trackColor = DeepEmerald.copy(alpha = 0.1f),
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

            // Settings Button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = DeepEmerald
                )
            }
        }
    )
}

@Composable
fun AyahItem(
    ayah: Ayah,
    surahNumber: Int,
    arabicFont: FontFamily,
    arabicFontSize: Float = 28f,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Badge Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = LightEmerald.copy(alpha = 0.5f),
                modifier = Modifier.wrapContentSize(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MediumEmerald.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$surahNumber:${ayah.number}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            // Bookmark icon
            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) DeepEmerald else TextGray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Arabic Text (Right Aligned)
        Text(
            text = ayah.arabic,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = arabicFont,
                fontSize = arabicFontSize.sp,
                lineHeight = (arabicFontSize * 1.8f).sp
            ),
            color = TextBlack,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Translation
        Text(
            text = ayah.translation,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp,
                fontWeight = FontWeight.Normal
            ),
            color = TextGray,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    HorizontalDivider(
        color = DividerColor, 
        thickness = 1.dp, 
        modifier = Modifier.padding(top = 24.dp)
    )
}

@Composable
fun SurahNavigationCard(
    label: String,
    surahName: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = LightEmerald.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = androidx.compose.foundation.BorderStroke(1.dp, MediumEmerald.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$label ",
                style = MaterialTheme.typography.bodyMedium,
                color = DeepEmerald
            )
            Text(
                text = surahName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = DeepEmerald
            )
        }
    }
}

// buildTajweedText function removed in favor of TajweedHelper
