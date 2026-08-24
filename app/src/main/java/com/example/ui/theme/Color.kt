package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// LocStamp Clean Professional Modern Palette
val PrimaryCyan = Color(0xFF06B6D4) // Modern Cyan 500
val PrimaryCyanDark = Color(0xFF0891B2) // Cyan 600
val AccentEmerald = Color(0xFF10B981) // Emerald 500
val AccentViolet = Color(0xFF8B5CF6) // Violet 500
val AccentAmber = Color(0xFFF59E0B) // Amber 500
val AccentPink = Color(0xFFEC4899) // Pink 500
val AccentBlue = Color(0xFF3B82F6) // Blue 500

// Aliases for compatibility
val NeonCyan = PrimaryCyan
val NeonPurple = AccentViolet
val NeonPink = AccentPink
val NeonMagenta = AccentPink
val NeonEmerald = AccentEmerald
val NeonAmber = AccentAmber
val NeonBlue = AccentBlue

// Clean Modern Dark Theme Canvas & Surfaces (Slate Palette)
val BackgroundDark = Color(0xFF0F172A) // Slate 900
val BackgroundDeep = Color(0xFF020617) // Slate 950
val SurfaceDark = Color(0xFF1E293B) // Slate 800
val SurfaceDarkElevated = Color(0xFF334155) // Slate 700
val SurfaceGlass = Color(0xEE1E293B)
val SurfaceCard = Color(0xFF1E293B)
val SurfaceCardBorder = Color(0xFF334155)

// Text Colors
val TextPrimary = Color(0xFFF8FAFC) // Slate 50
val TextSecondary = Color(0xFF94A3B8) // Slate 400
val TextMuted = Color(0xFF64748B) // Slate 500

// Gradients
val CyberGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryCyan, AccentEmerald)
)

val CyanPurpleGradient = Brush.linearGradient(
    colors = listOf(PrimaryCyan, AccentViolet)
)

val PurplePinkGradient = Brush.linearGradient(
    colors = listOf(AccentViolet, AccentPink)
)

val DarkSurfaceGradient = Brush.verticalGradient(
    colors = listOf(SurfaceDarkElevated, SurfaceDark)
)

