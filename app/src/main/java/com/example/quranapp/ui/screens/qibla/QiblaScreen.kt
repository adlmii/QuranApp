package com.example.quranapp.ui.screens.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.ui.theme.*
import com.example.quranapp.ui.components.AppHeader
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    navController: NavController,
    viewModel: QiblaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Calculate rotation:
    // We want the Compass dial to rotate so that North points to actual North.
    // -currentHeading
    // We want the Qibla Needle to point to Qibla Bearing RELATIVE to North.
    // So Needle Rotation relative to Screen Up = (QiblaBearing - CurrentHeading)
    // Wait, simpler approach:
    // Rotate the DIAL by -currentHeading.
    // Rotate the NEEDLE by (QiblaBearing - CurrentHeading)? No.
    // If Dial rotates, North is correct. Qibla Needle stays fixed at QiblaAngle relative to dial?
    // Let's rotate the whole Compass Frame by -heading.
    // Then Needle is drawn at QiblaBearing.
    
    val animatedHeading by animateFloatAsState(
        targetValue = -uiState.currentHeading,
        animationSpec = tween(durationMillis = 200), label = "CompassRotation"
    )

    // Haptic Feedback Logic
    val view = androidx.compose.ui.platform.LocalView.current
    val diff = kotlin.math.abs(uiState.currentHeading - uiState.qiblaBearing) % 360
    val minDiff = if (diff > 180) 360 - diff else diff
    val isAligned = minDiff < 2
    
    LaunchedEffect(isAligned) {
        if (isAligned) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
         // ── Header ──
        Spacer(modifier = Modifier.height(24.dp))
        AppHeader(
            title = "Qibla Finder",
            onBackClick = { navController.popBackStack() },
            backgroundColor = CreamBackground,
            contentColor = DeepEmerald
        )

        Spacer(modifier = Modifier.height(60.dp))

        if (!uiState.hasLocationPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Location Permission Required", color = TextGray)
                // TODO: Add Button to Request Permission
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Compass Container
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .rotate(animatedHeading), // Rotate everything to align North
                    contentAlignment = Alignment.Center
                ) {
                    // 1. Compass Dial (Background)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2
                        drawCircle(
                            color = LightEmerald.copy(alpha = 0.3f),
                            radius = radius,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        // Draw Ticks
                        for (i in 0 until 360 step 30) {
                            val angle = Math.toRadians(i.toDouble() - 90)
                            val startX = center.x + (radius - 20) * cos(angle).toFloat()
                            val startY = center.y + (radius - 20) * sin(angle).toFloat()
                            val endX = center.x + radius * cos(angle).toFloat()
                            val endY = center.y + radius * sin(angle).toFloat()
                            drawLine(
                                color = DeepEmerald,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (i % 90 == 0) 4f else 2f
                            )
                        }
                        
                        // North Text (Approximate placement)
                        // Note: Canvas text is hard in Compose 1.x without TextMeasurer, assuming M3.
                    }

                    // 2. North Indicator (Red Triangle)
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, // Placeholder for North
                        contentDescription = "North",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .rotate(90f)
                            .padding(top = 10.dp),
                        tint = Color.Red
                    )

                    // 3. Qibla Needle (The Goal)
                    // This needle stays fixed at 'qiblaBearing' degrees relative to North (which is at top of this rotated box)
                    // 3. Qibla Direction (Kaaba Icon)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(uiState.qiblaBearing.toFloat()),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // Draw Kaaba at the "Top" (which corresponds to Qibla Bearing)
                        // We offset it slightly so it sits on the compass rim
                        Canvas(modifier = Modifier.padding(top = 20.dp).size(24.dp)) {
                            // Cube Body
                            drawRect(color = Color.Black)
                            
                            // Gold Band
                            drawLine(
                                color = Color(0xFFD4AF37),
                                start = Offset(0f, size.height * 0.3f),
                                end = Offset(size.width, size.height * 0.3f),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        
                        // Direction Line
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(
                                color = Color(0xFFD4AF37).copy(alpha = 0.5f),
                                start = center,
                                end = Offset(center.x, 40f), // Point to Kaaba
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
                
                // Static Central Overlay (e.g. Kaaba Icon)
                // Text(text = "${qiblaBearing.toInt()}°", style = MaterialTheme.typography.headlineMedium, color = DeepEmerald)
            }
            
            // Info Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Qibla: ${uiState.qiblaBearing.toInt()}°",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
                Text(
                    text = "Current: ${uiState.currentHeading.toInt()}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            }
        }
    }
}
