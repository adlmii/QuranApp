package com.example.quranapp.data.model

data class AlMatsurat(
    val title: String,
    val arabic: String,
    val latin: String? = null,
    val translation: String? = null,
    val count: Int = 1
) {
    val isQuran: Boolean
        get() = title.matches(Regex(".*: \\d+-\\d+"))
}

enum class MatsuratType(val title: String) {
    MORNING("Pagi"),
    EVENING("Petang")
}
