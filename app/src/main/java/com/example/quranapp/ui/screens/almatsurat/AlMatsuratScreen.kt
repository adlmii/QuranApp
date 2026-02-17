package com.example.quranapp.ui.screens.almatsurat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
        AppHeader(
            title = "Al-Ma'tsurat",
            subtitle = if (uiState.matsuratType == MatsuratType.MORNING) "Dzikir Pagi" else "Dzikir Petang",
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
    // Page 0: Arabic, Page 1: Translation
    val pagerState = rememberPagerState(pageCount = { 2 })

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                            style = HeadlineQuran,
                            color = DeepEmerald,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
                    } else {
                        // Translation View
                         Box(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), // Min height to match arabic approx
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.translation ?: "Tidak ada terjemahan",
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                                color = TextGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dots Indicator
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { iteration ->
                    val color = if (pagerState.currentPage == iteration) DeepEmerald else LightEmerald
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(6.dp)
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
        shape = RoundedCornerShape(16.dp),
        backgroundColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            MatsuratHeader(item)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = item.arabic,
                style = HeadlineQuran,
                color = DeepEmerald,
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightEmerald)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${item.count}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
