package com.example.quranapp.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.ui.screens.quran.components.JuzSurahCard
import com.example.quranapp.ui.screens.quran.components.QuranTabSelector
import com.example.quranapp.ui.screens.quran.components.SurahItem
import com.example.quranapp.ui.theme.*
import com.example.quranapp.ui.components.AppHeader

@Composable
fun QuranScreen(
    navController: NavController,
    viewModel: QuranViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        AppHeader(
            title = "Al-Qur'an",
            onBackClick = { navController.popBackStack() },
            backgroundColor = CreamBackground,
            contentColor = DeepEmerald,
            actions = {
                IconButton(onClick = { /* TODO: Open search */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = DeepEmerald
                    )
                }
            }
        )

        // ── Tab Selector ──
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            QuranTabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Content ──
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepEmerald)
            }
        } else if (uiState.error != null) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}", color = Color.Red)
            }
        } else {
            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.surahList) { surah ->
                        SurahItem(
                            surah = surah,
                            onClick = { navController.navigate("quran_detail/${surah.number}") }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    uiState.juzList.forEach { juz ->
                        item {
                            Text(
                                text = "Juz ${juz.number}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(juz.surahs) { entry ->
                            JuzSurahCard(
                                entry = entry,
                                onClick = { navController.navigate("quran_detail/${entry.surahNumber}") }
                            )
                        }
                    }
                }
            }
        }
    }
}