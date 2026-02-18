package com.example.quranapp.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.ui.components.AppHeader
import com.example.quranapp.ui.screens.calendar.components.*
import com.example.quranapp.ui.theme.*

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Kalender Islam",
                onBackClick = { navController.popBackStack() },
                backgroundColor = CreamBackground,
                contentColor = DeepEmerald
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        SetStatusBarColor(CreamBackground)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Month navigator
            item {
                MonthNavigator(
                    monthName = uiState.displayMonthName,
                    year = uiState.displayYear,
                    hijriLabel = uiState.hijriMonthLabel,
                    hijriYear = uiState.hijriYearLabel,
                    onPrevious = { viewModel.goToPreviousMonth() },
                    onNext = { viewModel.goToNextMonth() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Calendar grid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    CalendarGrid(
                        days = uiState.calendarDays,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Event items
            if (uiState.events.isNotEmpty()) {
                items(uiState.events) { event ->
                    EventItem(event = event)
                }
            }

            // Bottom spacer
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
