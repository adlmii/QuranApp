package com.example.quranapp.ui.screens.qibla

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quranapp.R
import com.example.quranapp.ui.components.AppHeader
import com.example.quranapp.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    navController: NavController,
    viewModel: QiblaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current

    // Smooth rotation for the compass card
    val animatedHeading by animateFloatAsState(
        targetValue = -uiState.currentHeading,
        animationSpec = tween(durationMillis = 300), 
        label = "CompassRotation"
    )

    // Calculate alignment
    val diff = abs(uiState.currentHeading - uiState.qiblaBearing) % 360
    val minDiff = if (diff > 180) 360 - diff else diff
    val isAligned = minDiff < 3 // 3 degrees tolerance

    // Haptic feedback
    LaunchedEffect(isAligned) {
        if (isAligned) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(R.string.title_qibla_finder),
                onBackClick = { navController.popBackStack() },
                backgroundColor = CreamBackground,
                contentColor = DeepEmerald
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        SetStatusBarColor(CreamBackground)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CreamBackground),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Location Status
            if (!uiState.hasLocationPermission) {
                PermissionRequestUI()
            } else {
                Text(
                    text = if (isAligned) stringResource(R.string.qibla_aligned) else stringResource(R.string.qibla_rotate_phone),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isAligned) DeepEmerald else TextGray,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                // COMPASS CONTAINER
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(320.dp)
                ) {
                    // 1. Static Outer Ring (Does NOT rotate) - Decoration
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = LightEmerald,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = CreamBackground,
                            radius = size.minDimension / 2 - 10.dp.toPx()
                        )
                    }

                    // 2. Rotating Compass Card
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .rotate(animatedHeading),
                        contentAlignment = Alignment.Center
                    ) {
                        CompassDial(
                            primaryColor = DeepEmerald,
                            secondaryColor = GoldAccent
                        )
                        
                        // 3. Qibla Indicator (Kaaba)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(uiState.qiblaBearing.toFloat()),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Gold Beam
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 30.dp)
                            ) {
                                KaabaIcon(size = 36.dp)
                                if (isAligned) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(80.dp)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(GoldAccent, Color.Transparent)
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // 4. Center Axis / Needle Base
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(DeepEmerald)
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer Info Card
                InfoCard(
                    qiblaBearing = uiState.qiblaBearing,
                    currentHeading = uiState.currentHeading,
                    isAligned = isAligned
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CompassDial(
    primaryColor: Color,
    secondaryColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Draw Ticks
        for (i in 0 until 360 step 2) {
            val isMajor = i % 90 == 0
            val isMinor = i % 30 == 0
            
            val angleRad = Math.toRadians((i - 90).toDouble())
            val lineLength = when {
                isMajor -> 20.dp.toPx()
                isMinor -> 12.dp.toPx()
                else -> 6.dp.toPx()
            }
            
            val color = when {
                isMajor -> primaryColor
                isMinor -> primaryColor.copy(alpha = 0.6f)
                else -> primaryColor.copy(alpha = 0.3f)
            }
            
            val strokeWidth = if (isMajor) 3.dp.toPx() else 1.dp.toPx()

            val start = Offset(
                x = center.x + (radius - lineLength) * cos(angleRad).toFloat(),
                y = center.y + (radius - lineLength) * sin(angleRad).toFloat()
            )
            val end = Offset(
                x = center.x + radius * cos(angleRad).toFloat(),
                y = center.y + radius * sin(angleRad).toFloat()
            )

            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
        
        // North Indicator (Triangle)
        val northTrianglePath = Path().apply {
            moveTo(center.x, center.y - radius + 30.dp.toPx())
            lineTo(center.x - 10.dp.toPx(), center.y - radius + 50.dp.toPx())
            lineTo(center.x + 10.dp.toPx(), center.y - radius + 50.dp.toPx())
            close()
        }
        drawPath(path = northTrianglePath, color = Color.Red)
    }
}

@Composable
fun KaabaIcon(size: Dp) {
    Box(modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.toPx()
            val h = size.toPx()
            
            // Perspective Cube (Kaaba)
            val path = Path().apply {
                // Front Face
                moveTo(w * 0.2f, h * 0.3f)
                lineTo(w * 0.8f, h * 0.3f)
                lineTo(w * 0.8f, h * 0.9f)
                lineTo(w * 0.2f, h * 0.9f)
                close()
                
                // Top Face
                moveTo(w * 0.2f, h * 0.3f)
                lineTo(w * 0.4f, h * 0.1f) // Perspective shift
                lineTo(w * 0.9f, h * 0.1f) // Perspective shift
                lineTo(w * 0.8f, h * 0.3f)
                close()
                
                // Side Face
                moveTo(w * 0.8f, h * 0.3f)
                lineTo(w * 0.9f, h * 0.1f)
                lineTo(w * 0.9f, h * 0.7f)
                lineTo(w * 0.8f, h * 0.9f)
                close()
            }
            
            drawPath(path = path, color = Color.Black)
            
            // Gold Band (Kiswah)
            val bandY = h * 0.45f
            val bandHeight = h * 0.1f
            
            // Front band
            drawRect(
                color = GoldAccent,
                topLeft = Offset(w * 0.2f, bandY),
                size = androidx.compose.ui.geometry.Size(w * 0.6f, bandHeight)
            )
            
            // Side band (perspective)
            val sidePath = Path().apply {
                moveTo(w * 0.8f, bandY)
                lineTo(w * 0.9f, bandY - (h * 0.05f)) // Perspective up
                lineTo(w * 0.9f, bandY - (h * 0.05f) + bandHeight)
                lineTo(w * 0.8f, bandY + bandHeight)
                close()
            }
            drawPath(path = sidePath, color = GoldAccent)
        }
    }
}

@Composable
fun InfoCard(
    qiblaBearing: Double,
    currentHeading: Float,
    isAligned: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(26.dp),
        border = if (isAligned) androidx.compose.foundation.BorderStroke(2.dp, GoldAccent) else null
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.label_qibla_direction),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${qiblaBearing.toInt()}${stringResource(R.string.unit_degree)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isAligned) GoldAccent else DeepEmerald,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(LightEmerald)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.label_current_heading),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${currentHeading.toInt()}${stringResource(R.string.unit_degree)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DeepEmerald,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun PermissionRequestUI() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.permission_location_required), color = TextGray)
        }
    }
}
