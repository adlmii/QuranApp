package com.example.quranapp.ui.screens.quran.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.quranapp.R
import com.example.quranapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSettingsSheet(
    isPageMode: Boolean,
    arabicFontSize: Float,
    totalAyahs: Int,
    isKeepScreenOn: Boolean = false,
    onToggleMode: () -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit = {},
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
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp, top = 24.dp)
        ) {
            // ── Top Bar (Settings + X) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepEmerald
                )
                Surface(
                    shape = CircleShape,
                    color = DividerColor.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onDismiss() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Display Section Header ──
            SectionHeader(title = stringResource(R.string.settings_section_display))

            // ── Jump to Ayah Pill ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, DividerColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(start = 24.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = jumpText,
                        onValueChange = { jumpText = it.filter { c -> c.isDigit() } },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.hint_go_to_ayah, totalAyahs),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextGray.copy(alpha = 0.6f)
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
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = DeepEmerald,
                            focusedTextColor = TextBlack,
                            unfocusedTextColor = TextBlack
                        )
                    )
                    
                    // Elegant Circular Action Button
                    Surface(
                        shape = CircleShape,
                        color = DeepEmerald,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                val num = jumpText.toIntOrNull()
                                if (num != null && num in 1..totalAyahs) {
                                    onJumpToAyah(num)
                                    focusManager.clearFocus()
                                    jumpText = ""
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Go",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Font Size Pill ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, DividerColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dynamic Arabic Text Preview
                    Text(
                        text = "بِسْمِ ٱللَّهِ",
                        style = HeadlineQuran.copy(
                            fontSize = arabicFontSize.sp
                        ),
                        color = DeepEmerald,
                        modifier = Modifier.heightIn(min = 60.dp), // Prevent layout jumping
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "T",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )
                        
                        @OptIn(ExperimentalMaterial3Api::class)
                        Slider(
                            value = arabicFontSize,
                            onValueChange = onFontSizeChange,
                            valueRange = 20f..44f,
                            steps = 11,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = DeepEmerald,
                                activeTrackColor = DeepEmerald,
                                inactiveTrackColor = LightEmerald,
                                inactiveTickColor = Color.Transparent,
                                activeTickColor = Color.Transparent
                            ),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(DeepEmerald)
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }
                        )
                        
                        Text(
                            text = "T",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Reading Mode Section Header ──
            SectionHeader(title = stringResource(R.string.settings_section_reading_mode))

            // ── Segmented Control (List vs Page) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFF0F4F4))
                    .padding(6.dp)
            ) {
                // Smooth sliding animation
                val offsetFraction by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isPageMode) 1f else 0f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    ),
                    label = "TabIndicator"
                )

                // The Sliding Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = size.width * offsetFraction
                        }
                        .clip(RoundedCornerShape(50))
                        .background(brush = DeepEmeraldGradient)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(50), clip = false)
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    // List Option
                    SettingsTabButton(
                        text = stringResource(R.string.settings_list),
                        icon = Icons.Rounded.Menu,
                        isSelected = !isPageMode,
                        modifier = Modifier.weight(1f),
                        onClick = { if (isPageMode) onToggleMode() }
                    )
                    
                    // Page Option
                    SettingsTabButton(
                        text = stringResource(R.string.settings_page),
                        icon = Icons.Rounded.MenuBook,
                        isSelected = isPageMode,
                        modifier = Modifier.weight(1f),
                        onClick = { if (!isPageMode) onToggleMode() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Keep Screen On Pill ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, DividerColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_keep_screen_on),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextBlack
                    )
                    Switch(
                        checked = isKeepScreenOn,
                        onCheckedChange = onToggleKeepScreenOn,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DeepEmerald,
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = DividerColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = DeepEmerald.copy(alpha = 0.8f),
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
fun SettingsTabButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextGray,
        animationSpec = tween(300), label = ""
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Disable ripple for smooth transition
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
