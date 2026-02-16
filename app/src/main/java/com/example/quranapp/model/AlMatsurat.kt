package com.example.quranapp.model

data class AlMatsurat(
    val title: String,
    val arabic: String,
    val latin: String? = null,
    val translation: String? = null,
    val count: Int = 1
)

enum class MatsuratType(val title: String) {
    MORNING("Pagi"),
    EVENING("Petang")
}
