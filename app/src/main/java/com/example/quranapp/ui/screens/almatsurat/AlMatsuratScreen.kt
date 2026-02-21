package com.example.quranapp.ui.screens.almatsurat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.R
import com.example.quranapp.data.model.AlMatsurat
import com.example.quranapp.data.model.MatsuratType
import com.example.quranapp.ui.components.AppCard
import com.example.quranapp.ui.components.AppHeader
import com.example.quranapp.ui.theme.CreamBackground
import com.example.quranapp.ui.theme.DeepEmerald
import com.example.quranapp.ui.theme.HeadlineQuran
import com.example.quranapp.ui.theme.LightEmerald
import com.example.quranapp.ui.theme.TextGray
import com.example.quranapp.ui.theme.White
import com.example.quranapp.ui.theme.TextBlack
import com.example.quranapp.ui.theme.GoldAccent
import com.example.quranapp.ui.theme.SetStatusBarColor

@Composable
fun AlMatsuratScreen(
    navController: NavController,
    type: MatsuratType,
    viewModel: AlMatsuratViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(type) {
        viewModel.loadMatsurat(type)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        SetStatusBarColor(CreamBackground)

        AppHeader(
            title = stringResource(R.string.title_almatsurat),
            subtitle = if (type == MatsuratType.MORNING) stringResource(R.string.subtitle_dzikir_pagi) else stringResource(R.string.subtitle_dzikir_petang),
            onBackClick = { navController.popBackStack() },
            backgroundColor = CreamBackground,
            contentColor = DeepEmerald
        )

        // List
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepEmerald)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.matsuratList) { item ->
                    MatsuratItem(item = item)
                }
            }
        }
    }
}

@Composable
fun MatsuratItem(item: AlMatsurat) {
    if (item.isQuran) {
        SlidingMatsuratCard(item)
    } else {
        StaticMatsuratCard(item)
    }
}

@Composable
fun SlidingMatsuratCard(item: AlMatsurat) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = White,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            MatsuratHeader(item)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Sliding Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (page == 0) {
                        // Arabic View
                        Text(
                            text = item.arabic,
                            style = HeadlineQuran.copy(lineHeight = 48.sp),
                            color = TextBlack,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        )
                    } else {
                        // Translation View
                         Box(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.translation ?: stringResource(R.string.label_no_translation),
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                                color = TextGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dots Indicator
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { iteration ->
                    val color = if (pagerState.currentPage == iteration) DeepEmerald else LightEmerald
                    val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                            .size(width = width, height = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StaticMatsuratCard(item: AlMatsurat) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = White,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            MatsuratHeader(item)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = item.arabic,
                style = HeadlineQuran.copy(lineHeight = 48.sp),
                color = TextBlack,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!item.translation.isNullOrEmpty()) {
                Text(
                    text = item.translation,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                    color = TextGray
                )
            }
        }
    }
}

@Composable
fun MatsuratHeader(item: AlMatsurat) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepEmerald,
            modifier = Modifier.weight(1f)
        )

        if (item.count > 1) {
            Surface(
                shape = RoundedCornerShape(50),
                color = GoldAccent.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(
                        text = "${item.count}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD4AF37), // Darker Gold for text
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
