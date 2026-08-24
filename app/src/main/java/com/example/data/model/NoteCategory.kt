package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.PrimaryCyan

data class CategoryItem(
    val id: String,
    val name: String,
    val hashtag: String,
    val icon: String,
    val description: String,
    val color: Color
)

object NoteCategoryRegistry {
    // User requested categories + professional field survey categories
    val allCategories: List<CategoryItem> = listOf(
        CategoryItem(
            id = "visit",
            name = "Visit",
            hashtag = "#visit",
            icon = "🤝",
            description = "Kunjungan Kerja / Tamu",
            color = PrimaryCyan
        ),
        CategoryItem(
            id = "tugas_luar",
            name = "Tugas Luar",
            hashtag = "#tugas luar",
            icon = "💼",
            description = "Dinas & Tugas Lapangan",
            color = AccentAmber
        ),
        CategoryItem(
            id = "liburan",
            name = "Liburan",
            hashtag = "#liburan",
            icon = "🌴",
            description = "Wisata, Liburan & Rekreasi",
            color = AccentEmerald
        ),
        CategoryItem(
            id = "santai",
            name = "Santai",
            hashtag = "#santai",
            icon = "☕",
            description = "Waktu Luang, Istirahat & Rileks",
            color = AccentPink
        ),
        CategoryItem(
            id = "nongkrong",
            name = "Nongkrong",
            hashtag = "#nongkrong",
            icon = "👥",
            description = "Kumpul Komunitas & Hangout",
            color = AccentViolet
        ),
        CategoryItem(
            id = "cafe",
            name = "Cafe",
            hashtag = "#cafe",
            icon = "🍰",
            description = "Cafe, Resto & Kuliner",
            color = AccentPink
        ),
        CategoryItem(
            id = "warkop",
            name = "Warkop",
            hashtag = "#warkop",
            icon = "🍵",
            description = "Warung Kopi & Ngopi Santai",
            color = AccentAmber
        ),
        CategoryItem(
            id = "survey",
            name = "Survey",
            hashtag = "#survey",
            icon = "📐",
            description = "Survey Geospasial & Pemetaan",
            color = PrimaryCyan
        ),
        CategoryItem(
            id = "proyek",
            name = "Proyek",
            hashtag = "#proyek",
            icon = "🏗️",
            description = "Konstruksi, Sipil & Infrastruktur",
            color = AccentAmber
        ),
        CategoryItem(
            id = "inspeksi",
            name = "Inspeksi",
            hashtag = "#inspeksi",
            icon = "🔍",
            description = "Pemeriksaan Fisik & QC",
            color = AccentEmerald
        ),
        CategoryItem(
            id = "audit",
            name = "Audit",
            hashtag = "#audit",
            icon = "📋",
            description = "Audit Kepatuhan & Inventaris",
            color = AccentViolet
        ),
        CategoryItem(
            id = "travel",
            name = "Travel",
            hashtag = "#travel",
            icon = "✈️",
            description = "Perjalanan Dinas & Ekspedisi",
            color = PrimaryCyan
        ),
        CategoryItem(
            id = "insiden",
            name = "Insiden",
            hashtag = "#insiden",
            icon = "⚠️",
            description = "Laporan Kerusakan & Bahaya",
            color = AccentPink
        )
    )

    // Quick hashtag list requested specifically
    val requestedHashtags: List<String> = listOf(
        "#visit",
        "#tugas luar",
        "#liburan",
        "#santai",
        "#nongkrong",
        "#cafe",
        "#warkop"
    )

    fun getCategoryNames(): List<String> = allCategories.map { it.name }

    fun findCategoryByName(name: String): CategoryItem? {
        return allCategories.firstOrNull { it.name.equals(name, ignoreCase = true) || it.hashtag.equals(name, ignoreCase = true) }
    }
}
