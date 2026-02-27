package com.example.quranapp.ui.screens.quran.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quranapp.R
import com.example.quranapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSettingsSheet(
    isPageMode: Boolean,
    arabicFontSize: Float,
    totalAyahs: Int,
    onToggleMode: () -> Unit,
    onJumpToAyah: (Int) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var jumpText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CreamBackground,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle(color = DeepEmerald.copy(alpha = 0.4f))
            }
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Sheet Title
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = DeepEmerald,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // ── Card 1: Display Mode ──
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LightEmerald.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPageMode) Icons.Rounded.AutoStories else Icons.Rounded.MenuBook,
                            contentDescription = null,
                            tint = DeepEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_display_mode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )
                        Text(
                            text = if (isPageMode) stringResource(R.string.mode_halaman_title) else stringResource(R.string.settings_mode_ayah),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }

                    Switch(
                        checked = isPageMode,
                        onCheckedChange = { onToggleMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DeepEmerald,
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = DividerColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card 2: Jump to Ayah ──
            SettingsCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = DeepEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_jump_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = jumpText,
                            onValueChange = { jumpText = it.filter { c -> c.isDigit() } },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .border(1.dp, LightEmerald, RoundedCornerShape(12.dp)),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.hint_ayah_range, totalAyahs),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextGray.copy(alpha = 0.8f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    val num = jumpText.toIntOrNull()
                                    if (num != null && num in 1..totalAyahs) {
                                        onJumpToAyah(num)
                                        focusManager.clearFocus()
                                        jumpText = ""
                                    }
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = DeepEmerald,
                                focusedTextColor = TextBlack,
                                unfocusedTextColor = TextBlack
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Button(
                            onClick = {
                                val num = jumpText.toIntOrNull()
                                if (num != null && num in 1..totalAyahs) {
                                    onJumpToAyah(num)
                                    focusManager.clearFocus()
                                    jumpText = ""
                                }
                            },
                            modifier = Modifier.height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepEmerald),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "Go",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card 3: Font Size ──
            SettingsCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FormatSize,
                                contentDescription = null,
                                tint = DeepEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_font_size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald
                            )
                        }
                        
                        // Preview inside the header!
                        Text(
                            text = "بِسْمِ ٱللَّهِ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = UthmaniHafs,
                                fontSize = arabicFontSize.sp
                            ),
                            color = DeepEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TextFields,
                            contentDescription = "Small Font",
                            tint = TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Slider(
                            value = arabicFontSize,
                            onValueChange = onFontSizeChange,
                            valueRange = 20f..44f,
                            steps = 11,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = DeepEmerald,
                                activeTrackColor = DeepEmerald,
                                inactiveTrackColor = LightEmerald
                            )
                        )
                        
                        Icon(
                            imageVector = Icons.Rounded.TextFields,
                            contentDescription = "Large Font",
                            tint = TextGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card 4: Tajwid (Coming Soon) ──
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DividerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ColorLens,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_tajwid),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                        Text(
                            text = stringResource(R.string.label_coming_soon),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray.copy(alpha = 0.7f)
                        )
                    }

                    Switch(
                        checked = false,
                        onCheckedChange = { /* Coming soon */ },
                        enabled = false,
                        colors = SwitchDefaults.colors(
                            disabledCheckedThumbColor = CreamBackground,
                            disabledCheckedTrackColor = LightEmerald,
                            disabledUncheckedThumbColor = CreamBackground,
                            disabledUncheckedTrackColor = DividerColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, DividerColor.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
