package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode(
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color
) {
    CYBER_NEON(
        displayName = "Cyber Neon (Default)",
        description = "Gaya Slate Gelap dengan aksen Cyan & Ungu futuristik",
        primaryColor = PrimaryCyan,
        secondaryColor = AccentViolet,
        backgroundColor = BackgroundDark
    ),
    DAYLIGHT_PRO(
        displayName = "Daylight Pro (Terang)",
        description = "Tema latar putih cerah elegan dengan kontras tinggi & aksen Biru Safir",
        primaryColor = Color(0xFF0284C7),
        secondaryColor = Color(0xFF6366F1),
        backgroundColor = Color(0xFFF8FAFC)
    ),
    MIDNIGHT_OBSIDIAN(
        displayName = "Midnight Obsidian",
        description = "Kontras tinggi AMOLED Hitam dengan aksen Pink & Fuchsia",
        primaryColor = Color(0xFFF43F5E),
        secondaryColor = Color(0xFFA855F7),
        backgroundColor = Color(0xFF030712)
    ),
    TACTICAL_FIELD(
        displayName = "Tactical Field",
        description = "Nuansa Navy Militer dengan aksen Hijau Emerald & Amber",
        primaryColor = AccentEmerald,
        secondaryColor = AccentAmber,
        backgroundColor = Color(0xFF0B132B)
    ),
    CLEAN_SLATE(
        displayName = "Clean Modern Slate",
        description = "Tampilan profesional bersih dengan aksen Biru Langit",
        primaryColor = Color(0xFF0EA5E9),
        secondaryColor = Color(0xFF6366F1),
        backgroundColor = Color(0xFF0B0F19)
    )
}

private fun getColorSchemeForMode(mode: AppThemeMode): ColorScheme {
    return when (mode) {
        AppThemeMode.CYBER_NEON -> darkColorScheme(
            primary = PrimaryCyan,
            onPrimary = Color(0xFF04060C),
            primaryContainer = Color(0xFF0A364E),
            onPrimaryContainer = Color(0xFFE0F7FA),
            secondary = AccentViolet,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF3B1869),
            onSecondaryContainer = Color(0xFFF3E8FF),
            tertiary = AccentPink,
            onTertiary = Color.White,
            background = BackgroundDark,
            onBackground = TextPrimary,
            surface = SurfaceDark,
            onSurface = TextPrimary,
            surfaceVariant = SurfaceDarkElevated,
            onSurfaceVariant = TextSecondary,
            outline = SurfaceCardBorder
        )
        AppThemeMode.DAYLIGHT_PRO -> lightColorScheme(
            primary = Color(0xFF0284C7),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0F2FE),
            onPrimaryContainer = Color(0xFF0369A1),
            secondary = Color(0xFF6366F1),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEEF2FF),
            onSecondaryContainer = Color(0xFF3730A3),
            tertiary = Color(0xFF0D9488),
            onTertiary = Color.White,
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1)
        )
        AppThemeMode.MIDNIGHT_OBSIDIAN -> darkColorScheme(
            primary = Color(0xFFF43F5E),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF4C0519),
            onPrimaryContainer = Color(0xFFFFE4E6),
            secondary = Color(0xFFA855F7),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF3B0764),
            onSecondaryContainer = Color(0xFFFAF5FF),
            tertiary = PrimaryCyan,
            onTertiary = Color.White,
            background = Color(0xFF030712),
            onBackground = Color(0xFFF9FAFB),
            surface = Color(0xFF111827),
            onSurface = Color(0xFFF9FAFB),
            surfaceVariant = Color(0xFF1F2937),
            onSurfaceVariant = Color(0xFF9CA3AF),
            outline = Color(0xFF374151)
        )
        AppThemeMode.TACTICAL_FIELD -> darkColorScheme(
            primary = AccentEmerald,
            onPrimary = Color(0xFF022C22),
            primaryContainer = Color(0xFF064E3B),
            onPrimaryContainer = Color(0xFFD1FAE5),
            secondary = AccentAmber,
            onSecondary = Color(0xFF451A03),
            secondaryContainer = Color(0xFF78350F),
            onSecondaryContainer = Color(0xFFFEF3C7),
            tertiary = PrimaryCyan,
            onTertiary = Color.White,
            background = Color(0xFF0B132B),
            onBackground = Color(0xFFF0FDF4),
            surface = Color(0xFF1C2541),
            onSurface = Color(0xFFF0FDF4),
            surfaceVariant = Color(0xFF3A506B),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF3A506B)
        )
        AppThemeMode.CLEAN_SLATE -> darkColorScheme(
            primary = Color(0xFF0EA5E9),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF0369A1),
            onPrimaryContainer = Color(0xFFE0F2FE),
            secondary = Color(0xFF6366F1),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF312E81),
            onSecondaryContainer = Color(0xFFEEF2FF),
            tertiary = Color(0xFF14B8A6),
            onTertiary = Color.White,
            background = Color(0xFF0B0F19),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF161F30),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF243048),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155)
        )
    }
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.CYBER_NEON,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colorScheme = getColorSchemeForMode(themeMode)
    val statusBarColor = colorScheme.background
    val isLight = (themeMode == AppThemeMode.DAYLIGHT_PRO)

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = statusBarColor.toArgb()
                window.navigationBarColor = statusBarColor.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
