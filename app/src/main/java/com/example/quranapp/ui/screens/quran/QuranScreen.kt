package com.example.quranapp.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
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
    var isSearchActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        SetStatusBarColor(CreamBackground)
        
        if (isSearchActive) {
            // ── Search Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { 
                        viewModel.onSearchQueryChange(it) 
                        if (selectedTab != 0) selectedTab = 0 // Auto-switch to Surah tab
                    },
                    placeholder = { Text("Search Surah...", color = TextGray.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(50)) // Pill shape
                        .background(White),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = DeepEmerald,
                        focusedTextColor = TextBlack,
                        unfocusedTextColor = TextBlack
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = DeepEmerald)
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextGray)
                            }
                        }
                    },
                    shape = RoundedCornerShape(50)
                )
                
                TextButton(
                    onClick = { 
                        isSearchActive = false 
                        viewModel.onSearchQueryChange("") // Reset query on close
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        "Cancel", 
                        color = DeepEmerald, 
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        } else {
            // ── Standard Header ──
            AppHeader(
                title = "Al-Qur'an",
                onBackClick = { navController.popBackStack() },
                backgroundColor = CreamBackground,
                contentColor = DeepEmerald,
                actions = {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = DeepEmerald
                        )
                    }
                }
            )
        }

        // ── Tab Selector ──
        // Only show tabs if NOT searching? Or keep them? 
        // User said "Searching Juz is less common", so maybe just hide tabs or auto-select Surah.
        // Let's keep tabs but maybe disable/hide if search is active to keep it simple.
        if (!isSearchActive) {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                QuranTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
             Spacer(modifier = Modifier.height(8.dp))
        }

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
            if (selectedTab == 0 || isSearchActive) { // Force Surah view if searching
                val listToDisplay = if (isSearchActive) uiState.filteredSurahList else uiState.surahList

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (listToDisplay.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text("No surah found", color = TextGray)
                            }
                        }
                    } else {
                        items(listToDisplay) { surah ->
                            SurahItem(
                                surah = surah,
                                onClick = { navController.navigate("quran_detail/${surah.number}") }
                            )
                        }
                    }
                }
            } else {
                // Juz Tab (Unfiltered for now)
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