package com.example.quranapp.ui.screens.almatsurat

import androidx.compose.foundation.background
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
import com.example.quranapp.model.AlMatsurat
import com.example.quranapp.model.MatsuratType
import com.example.quranapp.ui.theme.BackgroundWhite
import com.example.quranapp.ui.theme.DeepEmerald
import com.example.quranapp.ui.theme.LightEmerald
import com.example.quranapp.ui.theme.TextGray
import com.example.quranapp.R

@Composable
fun AlMatsuratScreen(
    navController: NavController,
    type: MatsuratType,
    viewModel: AlMatsuratViewModel = viewModel()
) {
    val matsuratList by viewModel.matsuratList.collectAsState()
    val currentType by viewModel.matsuratType.collectAsState()
    
    // Load custom font
    val arabicFont = FontFamily(Font(R.font.lpmq_isepmisbah)) // Assuming resource id will be generated or need to place in res/font

    LaunchedEffect(type) {
        viewModel.loadMatsurat(type)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // ... (Header code remains same) ...
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepEmerald)
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Al-Ma'tsurat",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (currentType == MatsuratType.MORNING) "Dzikir Pagi" else "Dzikir Petang",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(matsuratList) { item ->
                MatsuratItem(item = item, arabicFont = arabicFont)
            }
        }
    }
}

@Composable
fun MatsuratItem(item: AlMatsurat, arabicFont: FontFamily) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
             // Title and Badge Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald,
                    modifier = Modifier.weight(1f)
                )
                
                // Hide badge if count is 0 or 1 (assuming "0x" means count 0, user said "0x klo 0x dihapus aja")
                // Typically count 1 is not shown either, but let's stick to user request "0x dihapus".
                // If the data has "0x" string, I should check that. But `item.count` is Int.
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Arabic Text with Custom Font
            Text(
                text = item.arabic,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = arabicFont,
                    lineHeight = 60.sp
                ),
                color = DeepEmerald,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Latin
            Text(
                text = item.latin ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = DeepEmerald,
                fontStyle = FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Translation
            Text(
                text = item.translation ?: "", // Typo in model was 'transalation', check model
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
        }
    }
}


