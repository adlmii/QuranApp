package com.example.quranapp.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.quranapp.ui.theme.TajweedColors
import java.util.regex.Pattern

object TajweedHelper {

    // Regex Universal: Mendukung 3 Format sekaligus:
    // 1. Sandwich: [n]teks[n]
    // 2. Standard: [n]teks[/n]
    // 3. Bracket:  [n[teks] (Format yang ditemukan di file quran_tajweed_full.json)
    //
    // Penjelasan Regex:
    // \[([a-zA-Z0-9:]+)      -> Group 1: Kode tag (n, h:1, dll)
    // (?:                    -> Non-capturing group untuk Alternation (Pilihan)
    //   \](.*?)\[/?\1\]      -> Pilihan A: Format 1 & 2 (Tutup dengan ] lalu konten lalu [/?kode]) -> Content di Group 2
    // |                      -> ATAU
    //   \[(.*?)\]            -> Pilihan B: Format 3 (Buka dengan [ lalu konten lalu ])        -> Content di Group 3
    // )
    private val PATTERN = Pattern.compile(
        """\[([a-zA-Z0-9:]+)(?:\](.*?)\[/?\1\]|\[(.*?)\])""", 
        Pattern.DOTALL
    )

    fun parseTajweed(text: String): AnnotatedString {
        return buildAnnotatedString {
            val matcher = PATTERN.matcher(text)
            var lastIndex = 0

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()

                // 1. Teks Biasa (sebelum tajwid)
                if (start > lastIndex) {
                    append(text.substring(lastIndex, start))
                }

                // 2. Ambil Kode & Isi
                val code = matcher.group(1) ?: ""
                // Content ada di Group 2 (Format A) atau Group 3 (Format B)
                val content = matcher.group(2) ?: matcher.group(3) ?: "" 

                // 3. Mapping Warna berdasarkan kode user
                val color = when (code) {
                    "n" -> TajweedColors.Ghunnah   // Ghunnah
                    "p" -> TajweedColors.Qalqalah  // Pantulan
                    "h:1" -> TajweedColors.Allah   // Lafadz Allah (Tebal)
                    "h:2", "h:3" -> TajweedColors.MadWajib // Mad Panjang
                    "h:4", "h:5" -> TajweedColors.MadJaiz  // Mad Lainnya
                    "f" -> TajweedColors.Ikhfa     // Ikhfa (jika ada kode f)
                    "w" -> TajweedColors.Ghunnah   // Idgham (jika ada kode w mappings ke Ghunnah/Idgham)
                    "a", "g" -> TajweedColors.Ghunnah
                    "m", "o" -> TajweedColors.MadWajib
                    else -> TajweedColors.DefaultHighlight // Default
                }

                // 4. Render Teks Berwarna
                withStyle(style = SpanStyle(color = color)) {
                    append(content)
                }

                lastIndex = end
            }

            // 5. Sisa Teks Akhir
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }
}
