package com.example.quranapp.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quranapp.ui.components.AppHeader
import com.example.quranapp.ui.components.PrayerCard
import com.example.quranapp.ui.screens.home.components.*
import com.example.quranapp.ui.theme.CreamBackground
import com.example.quranapp.ui.theme.DeepEmerald
import com.example.quranapp.ui.theme.SetStatusBarColor
import com.example.quranapp.ui.theme.TextGray
import com.google.android.gms.location.LocationServices

@Composable
fun HomeScreen(
    onNavigateToMatsurat: (String) -> Unit,
    onNavigateToDetail: (Int, Int) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
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

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(horizontal = 20.dp)
    ) {
        SetStatusBarColor(CreamBackground)

        // ── Sticky Header ──
        Spacer(modifier = Modifier.height(24.dp))
        AppHeader(
            gregorianDate = uiState.gregorianDate,
            hijriDate = uiState.hijriDate,
            location = uiState.location
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ── Scrollable Content ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // ── Prayer + Progress Cards ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrayerCard(
                    prayerName = uiState.nextPrayerName,
                    prayerTime = uiState.nextPrayerTime,
                    countDown = uiState.timeToNextPrayer,
                    modifier = Modifier.weight(1f)
                )
                QuranProgressCard(
                    currentMinutes = uiState.quranCurrentMinutes,
                    targetMinutes = uiState.quranTargetMinutes,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Al-Ma'tsurat ──
            AlMatsuratCard(
                type = uiState.matsuratType,
                onClick = onNavigateToMatsurat 
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Recent Section ──
            Text(
                text = "Recents",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.lastReadSurah != null) {
                RecentSurahItem(
                    number = uiState.lastReadNumber,
                    title = uiState.lastReadSurah!!,
                    ayah = uiState.lastReadAyah,
                    onClick = { onNavigateToDetail(uiState.lastReadNumber, uiState.lastReadAyah) }
                )
            } else {
                Text(
                    text = "Belum ada riwayat bacaan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}