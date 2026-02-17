package com.example.quranapp.ui.screens.almatsurat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.data.model.AlMatsurat
import com.example.quranapp.data.model.MatsuratType
import com.example.quranapp.ui.theme.BackgroundWhite
import com.example.quranapp.ui.theme.DeepEmerald
import com.example.quranapp.ui.theme.LightEmerald
import com.example.quranapp.ui.theme.TextGray
import com.example.quranapp.ui.theme.UthmaniHafs
import com.example.quranapp.ui.theme.HeadlineQuran
import androidx.compose.ui.graphics.graphicsLayer
import com.example.quranapp.R
import com.example.quranapp.ui.components.AppCard
import com.example.quranapp.ui.components.AppHeader
import com.example.quranapp.ui.theme.HeadlineQuran

@Composable
fun AlMatsuratScreen(
    navController: NavController,
    type: MatsuratType,
    viewModel: AlMatsuratViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load custom font


    LaunchedEffect(type) {
        viewModel.loadMatsurat(type)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        AppHeader(
            title = "Al-Ma'tsurat",
            subtitle = if (uiState.matsuratType == MatsuratType.MORNING) "Dzikir Pagi" else "Dzikir Petang",
            onBackClick = { navController.popBackStack() }
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
        FlipMatsuratCard(item)
    } else {
        StaticMatsuratCard(item)
    }
}

@Composable
fun FlipMatsuratCard(item: AlMatsurat) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = androidx.compose.animation.core.tween(500)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AppCard(
            onClick = { isFlipped = !isFlipped },
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                if (rotation <= 90f) {
                    // Front Face
                    Column(modifier = Modifier.fillMaxWidth()) {
                        MatsuratHeader(item)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = item.arabic,
                            style = HeadlineQuran,
                            color = DeepEmerald,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        

                    }
                } else {
                    // Back Face
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { rotationY = 180f },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.translation ?: "Tidak ada terjemahan",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DeepEmerald,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StaticMatsuratCard(item: AlMatsurat) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
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

            Text(
                text = item.translation ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
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


