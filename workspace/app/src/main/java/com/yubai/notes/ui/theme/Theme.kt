package com.yubai.notes.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yubai.notes.ui.ThemeMode

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Color(0xFFFFFBF4),
    primaryContainer = ForestSoft,
    onPrimaryContainer = Color(0xFF15382C),
    secondary = Terracotta,
    onSecondary = Color(0xFFFFFBF4),
    secondaryContainer = TerracottaSoft,
    onSecondaryContainer = Color(0xFF43251A),
    tertiary = Color(0xFF756440),
    onTertiary = Color(0xFFFFFBF4),
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDE7DC),
    onSurfaceVariant = InkMuted,
    outline = OutlineWarm,
    outlineVariant = Color(0xFFEBE4D8),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Color(0xFF2E332D),
    inverseOnSurface = Color(0xFFF2F1EA),
    inversePrimary = Mint,
    scrim = Color(0xFF000000),
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Color(0xFF123428),
    primaryContainer = MintSoft,
    onPrimaryContainer = Color(0xFFD6F2E3),
    secondary = Clay,
    onSecondary = Color(0xFF3C2117),
    secondaryContainer = Color(0xFF4B3127),
    onSecondaryContainer = Color(0xFFF8DDD1),
    tertiary = Color(0xFFD8C49B),
    onTertiary = Color(0xFF3A2F17),
    background = Night,
    onBackground = NightInk,
    surface = Night,
    onSurface = NightInk,
    surfaceVariant = Color(0xFF2A302A),
    onSurfaceVariant = NightMuted,
    outline = Color(0xFF3B423B),
    outlineVariant = Color(0xFF2C322C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE4E3DC),
    inverseOnSurface = Color(0xFF2D322D),
    inversePrimary = Forest,
    scrim = Color(0xFF000000),
)

private val YuBaiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun YuBaiTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palettes = remember(darkTheme) {
        if (darkTheme) DarkNotePalettes else LightNotePalettes
    }

    CompositionLocalProvider(LocalNotePalettes provides palettes) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = YuBaiTypography,
            shapes = YuBaiShapes,
            content = content,
        )
    }
}
