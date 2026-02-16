package com.example.quranapp.util

import androidx.compose.ui.graphics.Color

object TajweedUtils {
    fun getRuleColor(rule: String): Color {
        return when {
            rule.contains("ghunnah", ignoreCase = true) -> Color(0xFF4CAF50) // Green
            rule.contains("qalqalah", ignoreCase = true) -> Color(0xFF2196F3) // Blue
            rule.contains("idgham", ignoreCase = true) -> Color(0xFF9E9E9E) // Gray
            rule.contains("ikhfa", ignoreCase = true) -> Color(0xFF00BCD4) // Cyan
            rule.contains("madd", ignoreCase = true) -> Color(0xFFF44336) // Red
            rule.contains("hamzat_wasl", ignoreCase = true) -> Color(0xFFB0BEC5) // Light Gray
            rule.contains("lam_shamsiyyah", ignoreCase = true) -> Color(0xFFB0BEC5) // Light Gray
            else -> Color.Black // Default
        }
    }
}
