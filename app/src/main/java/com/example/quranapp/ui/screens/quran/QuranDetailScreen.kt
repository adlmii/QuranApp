package com.example.quranapp.ui.screens.quran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.example.quranapp.ui.theme.UthmaniHafs
import com.example.quranapp.ui.theme.CreamBackground
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.data.model.Ayah
import com.example.quranapp.R
import com.example.quranapp.ui.components.AppHeader
import androidx.compose.ui.res.stringResource
import com.example.quranapp.ui.theme.*
import androidx.compose.ui.text.font.FontFamily
import com.example.quranapp.util.QuranTextUtil
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import com.example.quranapp.ui.screens.quran.components.QuranSettingsSheet
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
    var currentMushafSurahName by remember { mutableStateOf<String?>(null) }
    var currentMushafAyah by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val arabicFont = UthmaniHafs

    // Scroll tracking state — declared early so toggle lambda can reference it
    val listState = rememberLazyListState()

    val onToggleModeAction: () -> Unit = {
        if (uiState.isPageMode) {
            // Page → List: pass first ayah of current page so List scrolls to it at top
            viewModel.toggleViewMode(currentMushafAyah)
        } else {
            // List → Page: find the page containing the first visible ayah
            val ayahs = uiState.surahDetail?.ayahs
            if (ayahs != null && ayahs.isNotEmpty()) {
                val hasPrevCard = surahNumber > 1 && uiState.prevSurahName != null
                val hasBasmalah = surahNumber != 1 && surahNumber != 9
                val headerOffset = (if (hasPrevCard) 1 else 0) + (if (hasBasmalah) 1 else 0)
                val ayahIndex = (listState.firstVisibleItemIndex - headerOffset).coerceIn(0, ayahs.size - 1)
                val currentAyah = ayahs[ayahIndex]
                coroutineScope.launch {
                    val pageNum = viewModel.getPageForAyah(surahNumber, currentAyah.number)
                    if (pageNum != null) {
                        viewModel.toggleViewMode() // switch to page mode
                        // targetPageJump will be set to navigate pager
                        viewModel.jumpToAyahInPageMode(surahNumber, currentAyah.number)
                    } else {
                        viewModel.toggleViewMode()
                    }
                }
            } else {
                viewModel.toggleViewMode()
            }
        }
    }

    LaunchedEffect(surahNumber) {
        viewModel.loadSurah(surahNumber)
    }

    // Guard: prevent auto-save before initial scroll completes
    var hasInitiallyScrolled by remember { mutableStateOf(false) }

    // Scroll to initial ayah after content loads
    LaunchedEffect(uiState.surahDetail) {
        val ayahs = uiState.surahDetail?.ayahs ?: return@LaunchedEffect
        if (initialAyah > 1 && ayahs.isNotEmpty()) {
            val hasPrevCard = surahNumber > 1 && uiState.prevSurahName != null
            val hasBasmalah = surahNumber != 1 && surahNumber != 9
            val headerOffset = (if (hasPrevCard) 1 else 0) + (if (hasBasmalah) 1 else 0)
            val targetIndex = (initialAyah - 1 + headerOffset).coerceIn(0, ayahs.size - 1 + headerOffset)
            listState.scrollToItem(targetIndex, scrollOffset = 0)
        }
        hasInitiallyScrolled = true
    }

    // Auto-save scroll position with debounce (only after initial scroll)
    val firstVisibleItem by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisibleItem) {
        if (!hasInitiallyScrolled) return@LaunchedEffect
        // Debounce: wait 500ms before saving to avoid excessive writes
        delay(500)
        val ayahs = uiState.surahDetail?.ayahs ?: return@LaunchedEffect
        // firstVisibleItemIndex accounts for header items (prev surah card + basmalah)
        val hasPrevCard = surahNumber > 1 && uiState.prevSurahName != null
        val hasBasmalah = surahNumber != 1 && surahNumber != 9
        val headerOffset = (if (hasPrevCard) 1 else 0) + (if (hasBasmalah) 1 else 0)
        val ayahIndex = (firstVisibleItem - headerOffset).coerceIn(0, ayahs.size - 1)
        if (ayahs.isNotEmpty()) {
            viewModel.saveLastRead(ayahs[ayahIndex].number)
        }
    }

    // Sync scroll from Mushaf mode to List mode
    LaunchedEffect(uiState.targetScrollAyah, uiState.isPageMode) {
        val target = uiState.targetScrollAyah
        val ayahs = uiState.surahDetail?.ayahs
        if (target != null && ayahs != null && ayahs.isNotEmpty() && !uiState.isPageMode) {
            val ayahIndexInList = ayahs.indexOfFirst { it.number == target }
            if (ayahIndexInList != -1) {
                val hasPrevCard = surahNumber > 1 && uiState.prevSurahName != null
                val hasBasmalah = surahNumber != 1 && surahNumber != 9
                val headerOffset = (if (hasPrevCard) 1 else 0) + (if (hasBasmalah) 1 else 0)
                val targetIndex = ayahIndexInList + headerOffset
                listState.scrollToItem(targetIndex, scrollOffset = 0)
            }
            viewModel.consumeTargetScrollAyah()
        }
    }

    // Settings Bottom Sheet
    if (showSettings) {
        val sheetTotalAyahs = if (uiState.isPageMode && uiState.activeMushafSurahTotalAyahs > 0) {
            uiState.activeMushafSurahTotalAyahs
        } else {
            uiState.surahDetail?.ayahs?.size ?: 0
        }
        
        val sheetSurahId = if (uiState.isPageMode && uiState.activeMushafSurahId > 0) {
            uiState.activeMushafSurahId
        } else {
            surahNumber
        }

        QuranSettingsSheet(
            isPageMode = uiState.isPageMode,
            arabicFontSize = uiState.arabicFontSize,
            totalAyahs = sheetTotalAyahs,
            onToggleMode = onToggleModeAction,
            onJumpToAyah = { ayahNum ->
                if (uiState.isPageMode) {
                    viewModel.jumpToAyahInPageMode(sheetSurahId, ayahNum)
                } else {
                    val ayahs = uiState.surahDetail?.ayahs ?: return@QuranSettingsSheet
                    val hasPrevCard = surahNumber > 1 && uiState.prevSurahName != null
                    val hasBasmalah = surahNumber != 1 && surahNumber != 9
                    val headerOffset = (if (hasPrevCard) 1 else 0) + (if (hasBasmalah) 1 else 0)
                    val index = (ayahNum - 1 + headerOffset).coerceIn(0, ayahs.size - 1 + headerOffset)
                    coroutineScope.launch {
                        listState.animateScrollToItem(index)
                    }
                }
                showSettings = false
            },
            onFontSizeChange = { size -> viewModel.updateFontSize(size) },
            onDismiss = { showSettings = false }
        )
    }

    Scaffold(
        topBar = {
            DetailHeader(
                title = if (uiState.isPageMode && currentMushafSurahName != null) currentMushafSurahName!! else (uiState.surahDetail?.name ?: stringResource(R.string.label_loading)),
                onBack = { navController.popBackStack() },
                isPageMode = uiState.isPageMode,
                onToggleMode = onToggleModeAction,
                sessionProgress = uiState.sessionProgress,
                targetMinutes = uiState.targetMinutes,
                onSettingsClick = { showSettings = true }
            )
            SetStatusBarColor(MaterialTheme.colorScheme.background)
        },
        containerColor = MaterialTheme.colorScheme.background
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
                        color = MaterialTheme.colorScheme.secondary, // Gold
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.label_error, uiState.error!!),
                            color = Color.Red
                        )
                    }
                } else {
                    uiState.surahDetail?.let { detail ->
                        if (uiState.isPageMode) {
                            // Page Mode — Mushaf HorizontalPager
                            // Find the page containing the requested ayah, or fallback to the start of the Surah
                            val startPage = detail.ayahs.find { it.number == initialAyah }?.page 
                                ?: detail.ayahs.firstOrNull()?.page ?: 1
                            val pagerState = rememberPagerState(
                                initialPage = startPage - 1, // 0-indexed
                                pageCount = { 604 }
                            )

                            // Jump listener for jumping across Mushaf pages via Settings Sheet
                            LaunchedEffect(uiState.targetPageJump) {
                                uiState.targetPageJump?.let { targetPage ->
                                    pagerState.animateScrollToPage(targetPage - 1)
                                    viewModel.consumeTargetPageJump()
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                reverseLayout = true, // Swipe dari kanan ke kiri (khas Arab)
                                modifier = Modifier.fillMaxSize()
                            ) { pageIndex ->
                                val actualPage = pageIndex + 1
                                val ayahsOnPage by viewModel.getPageData(actualPage).collectAsState()

                                // Update current mushaf surah name strictly for the active page
                                LaunchedEffect(pagerState.currentPage, ayahsOnPage) {
                                    if (pageIndex == pagerState.currentPage && ayahsOnPage.isNotEmpty()) {
                                        val firstAyahItem = ayahsOnPage.first()
                                        val firstVerse = firstAyahItem.ayah.verseNumber
                                        val surahId = firstAyahItem.ayah.surahId
                                        
                                        currentMushafSurahName = firstAyahItem.surahNameSimple
                                        currentMushafAyah = firstVerse
                                        
                                        viewModel.updateActiveMushafSurah(surahId)
                                        
                                        // Save to Last Read history so the Home Screen card works for Mushaf reading
                                        viewModel.saveLastRead(firstVerse)
                                    }
                                }

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

                                                // Put the camera farther away for a less distorted 3D effect
                                                cameraDistance = 8 * density
                                                
                                                // Opacity fades out slightly at the extreme edges of the flip
                                                alpha = lerp(
                                                    start = 0.5f,
                                                    stop = 1f,
                                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                                )
                                                
                                                // Rotate along the Y-axis to simulate page turning
                                                // 90 degrees means perfectly edge-on (invisible)
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
                                            val firstAyahItem = ayahsOnPage.first()
                                            val firstAyah = firstAyahItem.ayah
                                            val primarySurah = firstAyahItem.surahNameArabic
                                            MushafPageHeader(
                                                juzNumber = firstAyah.juzNumber, 
                                                surahName = primarySurah,
                                                isBookmarked = uiState.bookmarkSurah == firstAyah.surahId && uiState.bookmarkAyah == firstAyah.verseNumber,
                                                onBookmarkClick = { 
                                                    viewModel.saveBookmark(
                                                        surahNumber = firstAyah.surahId,
                                                        ayahNumber = firstAyah.verseNumber,
                                                        surahName = primarySurah
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
                                                    style = HeadlineQuran.copy(
                                                        fontSize = uiState.arabicFontSize.sp,
                                                        lineHeight = (uiState.arabicFontSize * 1.8f).sp,
                                                        textDirection = TextDirection.Rtl
                                                    ),
                                                    textAlign = TextAlign.Justify,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    }
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
                                            label = stringResource(id = R.string.nav_surah_prev),
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
                                                color = MaterialTheme.colorScheme.primary
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
                                            label = stringResource(id = R.string.nav_surah_next),
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
    subtitle: String? = null,
    onBack: () -> Unit,
    isPageMode: Boolean,
    onToggleMode: () -> Unit,
    sessionProgress: Int = 0,
    targetMinutes: Int = 5,
    onSettingsClick: () -> Unit = {}
) {
    AppHeader(
        title = title,
        subtitle = subtitle,
        onBackClick = onBack,
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        actions = {
            // Session Indicator
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(end = 8.dp)) {
                CircularProgressIndicator(
                    progress = { sessionProgress / targetMinutes.toFloat() },
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeWidth = 3.dp
                )
                Text(
                    text = "$sessionProgress",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            // Settings Button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
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
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.wrapContentSize(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$surahNumber:${ayah.number}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
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
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Arabic Text (Right Aligned)
        Text(
            text = ayah.arabic,
            style = HeadlineQuran.copy(
                fontSize = arabicFontSize.sp,
                lineHeight = (arabicFontSize * 1.8f).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant, 
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
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
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
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = surahName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// buildTajweedText function removed in favor of TajweedHelper
