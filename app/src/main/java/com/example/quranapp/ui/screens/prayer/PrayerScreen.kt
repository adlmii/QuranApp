package com.example.quranapp.ui.screens.prayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quranapp.ui.components.AppHeader
import com.example.quranapp.ui.components.PrayerCard
import com.example.quranapp.ui.screens.prayer.components.ImsakSunriseBar
import com.example.quranapp.ui.screens.prayer.components.MarkAllPrayedButton
import com.example.quranapp.ui.screens.prayer.components.PrayerItem
import com.example.quranapp.ui.screens.prayer.components.PrayerProgressCard
import com.example.quranapp.ui.theme.CreamBackground
import com.google.android.gms.location.LocationServices

@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val getLocation = {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        viewModel.updateLocation(it.latitude, it.longitude, context)
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLocation()
        }
    }

    // Request notification permission on Android 13+
    val requestNotifPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 20.dp)
    ) {
        com.example.quranapp.ui.theme.SetStatusBarColor(CreamBackground)

        // ── Sticky Header ──
        Spacer(modifier = Modifier.height(24.dp))
        AppHeader(
            gregorianDate = uiState.gregorianDate,
            hijriDate = uiState.hijriDate,
            location = uiState.location
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ── Scrollable Content ──
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Dashboard Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrayerCard(
                        prayerName = uiState.nextPrayerName,
                        prayerTime = uiState.nextPrayerTime,
                        countDown = uiState.timeToNextPrayer.asString(),
                        isNow = uiState.isCurrentPrayerNow,
                        nowLabel = uiState.currentPrayerLabel,
                        modifier = Modifier.weight(1f)
                    )

                    PrayerProgressCard(
                        count = uiState.prayedCount,
                        total = uiState.totalPrayer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Imsak & Sunrise Bar
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ImsakSunriseBar(
                        imsakTime = uiState.imsakTime,
                        sunriseTime = uiState.sunriseTime
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Prayer List
            itemsIndexed(uiState.prayerList) { index, prayer ->
                PrayerItem(
                    name = prayer.name,
                    time = prayer.time,
                    isPrayed = prayer.isPrayed,
                    isNext = prayer.isNext,
                    isNow = prayer.isNow,
                    isPassed = prayer.isPassed,
                    isNotificationOn = prayer.isNotificationOn,
                    countdown = prayer.countdown.asString(),
                    onCheckClick = {
                        viewModel.togglePrayed(index)
                    },
                    onNotificationToggle = {
                        viewModel.toggleNotification(index)
                    }
                )
            }

            // Bottom Text
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    MarkAllPrayedButton(
                        onClick = { viewModel.markAllPrayed() },
                        enabled = uiState.canMarkAll
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}