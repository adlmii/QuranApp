package com.example.quranapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.R
import com.example.quranapp.ui.components.AppCard
import com.example.quranapp.ui.components.AppHeader
import com.example.quranapp.ui.theme.*

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        SetStatusBarColor(CreamBackground)

        // Header
        AppHeader(
            title = stringResource(R.string.title_settings_global),
            onBackClick = { navController.popBackStack() },
            backgroundColor = CreamBackground,
            contentColor = DeepEmerald
        )

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Target Ibadah Section ──
            SettingsCategoryCard(
                title = stringResource(R.string.cat_worship_goals)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_quran_target),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Text(
                        text = stringResource(R.string.desc_quran_target),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                    
                    // Target Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 15, 30, 60).forEach { minutes ->
                            FilterChip(
                                selected = uiState.targetMinutes == minutes,
                                onClick = { viewModel.setTarget(minutes) },
                                label = {
                                    Text(
                                        text = stringResource(R.string.goal_value, minutes),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepEmerald,
                                    selectedLabelColor = White,
                                    containerColor = LightEmerald.copy(alpha = 0.5f),
                                    labelColor = TextGray
                                )
                            )
                        }
                    }
                }
            }

            // ── Bahasa Aplikasi Section ──
            SettingsCategoryCard(
                title = stringResource(R.string.cat_language)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_app_language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageOption(
                            label = stringResource(R.string.lang_system),
                            selected = uiState.languageCode.isEmpty(),
                            onClick = {
                                viewModel.setLanguage("") {
                                    (context as? android.app.Activity)?.recreate()
                                }
                            }
                        )
                        LanguageOption(
                            label = stringResource(R.string.lang_indonesian),
                            selected = uiState.languageCode == "in",
                            onClick = {
                                viewModel.setLanguage("in") {
                                    (context as? android.app.Activity)?.recreate()
                                }
                            }
                        )
                        LanguageOption(
                            label = stringResource(R.string.lang_english),
                            selected = uiState.languageCode == "en",
                            onClick = {
                                viewModel.setLanguage("en") {
                                    (context as? android.app.Activity)?.recreate()
                                }
                            }
                        )
                    }
                }
            }

            // ── Tema Aplikasi Section ──
            SettingsCategoryCard(
                title = stringResource(R.string.cat_appearance_global)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_app_theme),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    // Theme Options
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOption(
                            label = stringResource(R.string.theme_system),
                            selected = uiState.themeMode == 0,
                            onClick = { viewModel.setThemeMode(0) }
                        )
                        ThemeOption(
                            label = stringResource(R.string.theme_light),
                            selected = uiState.themeMode == 1,
                            onClick = { viewModel.setThemeMode(1) }
                        )
                        ThemeOption(
                            label = stringResource(R.string.theme_dark),
                            selected = uiState.themeMode == 2,
                            onClick = { viewModel.setThemeMode(2) }
                        )
                    }
                }
            }

            // ── Informasi Aplikasi Section ──
            SettingsCategoryCard(
                title = stringResource(R.string.label_version)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextBlack
                    )
                    Text(
                        text = stringResource(R.string.app_version_val),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DeepEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = White,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald
            )
            HorizontalDivider(color = DividerColor)
            content()
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) LightEmerald.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) DeepEmerald else TextBlack,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = DeepEmerald
            )
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) LightEmerald.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) DeepEmerald else TextBlack,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = DeepEmerald
            )
        )
    }
}
