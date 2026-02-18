package com.example.quranapp.ui.screens.quran.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.quranapp.data.model.Surah
import com.example.quranapp.ui.theme.*
import com.example.quranapp.R
import com.example.quranapp.ui.components.AppCard
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import kotlin.math.*
import kotlin.random.Random

// ─────────────────────────────────────────────
// Tab Selector (Surah / Juz)
// ─────────────────────────────────────────────

@Composable
fun QuranTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFF0F4F4)) // Very light emerald/gray
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / 2
        
        // Smooth sliding animation
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedTab == 0) 0.dp else tabWidth,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "TabIndicator"
        )

        // The Sliding Indicator (Gradient background)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(brush = DeepEmeraldGradient)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), clip = false)
        )

        // The Clickable Text Layers
        Row(modifier = Modifier.fillMaxSize()) {
            TabButton(
                text = "Surah",
                isSelected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(0) }
            )
            TabButton(
                text = "Juz",
                isSelected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(1) }
            )
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) White else TextGray,
        animationSpec = tween(durationMillis = 300), 
        label = "TabTextColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null // No ripple, just slide
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

// ─────────────────────────────────────────────
// Surah List Item (dark themed)
// ─────────────────────────────────────────────

@Composable
fun SurahItem(
    surah: Surah,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        backgroundColor = White,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number in a Star/Polygon shape or Circle with Border
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, LightEmerald, CircleShape)
                    .background(CreamBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = surah.number.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Latin name + Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = surah.englishName, 
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Arabic name
            Text(
                text = surah.arabicName.replace("سُورَةُ", "").trim(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = UthmaniHafs,
                    color = DeepEmerald
                ),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─────────────────────────────────────────────
// Juz Surah Card (light themed, individual card per surah entry)
// ─────────────────────────────────────────────

@Composable
fun JuzSurahCard(
    entry: com.example.quranapp.data.model.JuzSurahEntry,
    onClick: () -> Unit = {}
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        backgroundColor = White,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arabic name (right side usually for Quran, but following design)
            // Let's swap to match SurahItem logic (Latin Left, Arabic Right)

             // Surah name + ayah range
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.surahName, // Assuming this is Latin name
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ayah ${entry.ayahRange}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Arabic name
            Text(
                text = entry.arabicName.replace("سُورَةُ", "").trim(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = UthmaniHafs,
                    color = DeepEmerald
                ),
                textAlign = TextAlign.End
            )
        }
    }
}

// ─────────────────────────────────────────────
// Fireworks Animation
// ─────────────────────────────────────────────

data class Particle(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    val color: Color
)

@Composable
fun Fireworks(modifier: Modifier = Modifier) {
    val particles = remember { mutableStateListOf<Particle>() }
    val lastFrameTime = remember { mutableLongStateOf(0L) }
    
    // Add new particles periodically
    LaunchedEffect(Unit) {
        while (true) {
            val centerX = Random.nextFloat() * 1000f // Random X (will be scaled in draw)
            val centerY = Random.nextFloat() * 500f + 500f // Start lower
            val color = listOf(Color.Red, Color.Yellow, Color.Blue, Color.Green, Color.Magenta, Color(0xFFD4AF37)).random()
            
            repeat(20) { i ->
                val angle = Random.nextFloat() * 2 * PI.toFloat()
                val speed = Random.nextFloat() * 10f + 5f
                particles.add(
                    Particle(
                        id = Random.nextInt(),
                        x = centerX,
                        y = centerY,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        alpha = 1f,
                        size = Random.nextFloat() * 10f + 5f,
                        color = color
                    )
                )
            }
            delay(500) // New explosion every 0.5s
        }
    }

    // Animation Loop
    LaunchedEffect(Unit) {
        val startTime = withFrameNanos { it }
        lastFrameTime.longValue = startTime
        
        while (true) {
            withFrameNanos { time ->
                val dt = (time - lastFrameTime.longValue) / 1_000_000f // ms
                lastFrameTime.longValue = time
                
                // Update particles
                val iterator = particles.listIterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 0.2f // Gravity
                    p.alpha -= 0.01f // Fade out
                    
                    if (p.alpha <= 0f) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
             // For this simple effect, let's assume we map 1000x1000 virtual space to canvas
             val drawX = (p.x / 1000f) * size.width
             val drawY = (p.y / 1500f) * size.height 
             
             if (p.alpha > 0) {
                 drawCircle(
                     color = p.color.copy(alpha = p.alpha),
                     radius = p.size,
                     center = androidx.compose.ui.geometry.Offset(drawX, drawY)
                 )
             }
        }
    }
}