package com.example.quranapp.ui.screens.quran.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Pengaturan Baca",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepEmerald
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Display Mode Toggle ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mode Tampilan",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextBlack
                    )
                    Text(
                        text = if (isPageMode) "Mode Halaman" else "Mode Ayat",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
                Switch(
                    checked = isPageMode,
                    onCheckedChange = { onToggleMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CreamBackground,
                        checkedTrackColor = DeepEmerald
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = DividerColor
            )

            // ── Jump to Ayah ──
            Text(
                text = "Lompat ke Ayat",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = jumpText,
                    onValueChange = { jumpText = it.filter { c -> c.isDigit() } },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, DeepEmerald, RoundedCornerShape(50)),
                    placeholder = {
                        Text(
                            "1 – $totalAyahs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
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
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightEmerald.copy(alpha = 0.3f),
                        unfocusedContainerColor = LightEmerald.copy(alpha = 0.2f),
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
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
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "Go",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = DividerColor
            )

            // ── Font Size Slider ──
            Text(
                text = "Ukuran Teks Arab",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "بِسْمِ ٱللَّهِ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = UthmaniHafs,
                    fontSize = arabicFontSize.sp
                ),
                color = DeepEmerald
            )
            Slider(
                value = arabicFontSize,
                onValueChange = onFontSizeChange,
                valueRange = 20f..44f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = DeepEmerald,
                    activeTrackColor = DeepEmerald,
                    inactiveTrackColor = LightEmerald
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kecil", style = MaterialTheme.typography.labelSmall, color = TextGray)
                Text("Besar", style = MaterialTheme.typography.labelSmall, color = TextGray)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = DividerColor
            )

            // ── Tajwid (Coming Soon) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Warna Tajwid",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextBlack
                    )
                    Text(
                        text = "Segera Hadir",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
                Switch(
                    checked = false,
                    onCheckedChange = { /* Coming soon */ },
                    enabled = false,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CreamBackground,
                        checkedTrackColor = DeepEmerald
                    )
                )
            }
        }
    }
}
