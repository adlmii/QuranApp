package com.example.quranapp.data.model

/**
 * Represents a major Islamic event tied to a Hijri date.
 */
data class IslamicEvent(
    val hijriMonth: Int,   // 1-12
    val hijriDay: Int,     // 1-30
    val name: String,
    val description: String,
    val emoji: String = "🌙"
)

/**
 * Master list of important Islamic dates.
 * Hijri months: 1=Muharram, 2=Safar, 3=Rabi'ul Awal, 4=Rabi'ul Akhir,
 * 5=Jumadil Awal, 6=Jumadil Akhir, 7=Rajab, 8=Sya'ban, 9=Ramadhan,
 * 10=Syawal, 11=Dzulqa'dah, 12=Dzulhijjah
 */
val islamicEvents = listOf(
    // ── Muharram (1) ──
    IslamicEvent(1, 1, "Tahun Baru Hijriah", "Awal tahun dalam kalender Islam", "🎉"),
    IslamicEvent(1, 9, "Tasu'a", "Puasa sunnah sehari sebelum Asyura", "🤲"),
    IslamicEvent(1, 10, "Asyura", "Hari puasa sunnah Asyura, mengenang peristiwa Nabi Musa", "🤲"),

    // ── Safar (2) ──
    IslamicEvent(2, 1, "Awal Bulan Safar", "Bulan kedua dalam kalender Hijriah", "📅"),

    // ── Rabi'ul Awal (3) ──
    IslamicEvent(3, 12, "Maulid Nabi Muhammad ﷺ", "Peringatan kelahiran Nabi Muhammad SAW", "🕌"),

    // ── Rabi'ul Akhir (4) ──
    IslamicEvent(4, 1, "Awal Rabi'ul Akhir", "Bulan ke-4 kalender Hijriah", "📅"),

    // ── Jumadil Awal (5) ──
    IslamicEvent(5, 1, "Awal Jumadil Awal", "Bulan ke-5 kalender Hijriah", "📅"),

    // ── Jumadil Akhir (6) ──
    IslamicEvent(6, 1, "Awal Jumadil Akhir", "Bulan ke-6 kalender Hijriah", "📅"),

    // ── Rajab (7) ──
    IslamicEvent(7, 1, "Awal Bulan Rajab", "Bulan mulia, salah satu bulan haram", "⭐"),
    IslamicEvent(7, 27, "Isra Mi'raj", "Perjalanan malam Nabi Muhammad SAW ke Sidratul Muntaha", "✨"),

    // ── Sya'ban (8) ──
    IslamicEvent(8, 1, "Awal Bulan Sya'ban", "Bulan ke-8 kalender Hijriah", "📅"),
    IslamicEvent(8, 15, "Nisfu Sya'ban", "Malam pertengahan Sya'ban, malam pengampunan", "🌕"),

    // ── Ramadhan (9) ──
    IslamicEvent(9, 1, "Awal Ramadhan", "Bulan suci puasa dimulai", "🌙"),
    IslamicEvent(9, 17, "Nuzulul Quran", "Peringatan turunnya Al-Quran pertama kali", "📖"),
    IslamicEvent(9, 21, "Lailatul Qadr", "Potensi malam Lailatul Qadr", "🌟"),
    IslamicEvent(9, 23, "Lailatul Qadr", "Potensi malam Lailatul Qadr", "🌟"),
    IslamicEvent(9, 25, "Lailatul Qadr", "Potensi malam Lailatul Qadr", "🌟"),
    IslamicEvent(9, 27, "Lailatul Qadr", "Potensi malam Lailatul Qadr", "🌟"),
    IslamicEvent(9, 29, "Lailatul Qadr", "Potensi malam Lailatul Qadr", "🌟"),

    // ── Syawal (10) ──
    IslamicEvent(10, 1, "Awal Bulan Syawal", "Bulan ke-10 kalender Hijriah", "📅"),
    IslamicEvent(10, 1, "Idul Fitri", "Hari raya setelah sebulan berpuasa", "🎊"),
    IslamicEvent(10, 2, "Idul Fitri", "Hari kedua perayaan Idul Fitri", "🎊"),

    // ── Dzulqa'dah (11) ──
    IslamicEvent(11, 1, "Awal Dzulqa'dah", "Bulan haram, persiapan menuju haji", "📅"),

    // ── Dzulhijjah (12) ──
    IslamicEvent(12, 1, "Awal Dzulhijjah", "Bulan ke-12 kalender Hijriah", "📅"),
    IslamicEvent(12, 8, "Hari Tarwiyah", "Hari pertama rangkaian ibadah haji", "🕋"),
    IslamicEvent(12, 9, "Wukuf di Arafah", "Puncak ibadah haji, puasa sunnah Arafah", "🤲"),
    IslamicEvent(12, 10, "Idul Adha", "Hari raya kurban", "🐪"),
    IslamicEvent(12, 11, "Hari Tasyrik 1", "Hari-hari makan dan minum serta dzikir", "📿"),
    IslamicEvent(12, 12, "Hari Tasyrik 2", "Lanjutan hari Tasyrik", "📿"),
    IslamicEvent(12, 13, "Hari Tasyrik 3", "Hari terakhir Tasyrik", "📿")
)
