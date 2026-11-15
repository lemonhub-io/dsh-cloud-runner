package com.yubai.notes.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val Paper = Color(0xFFF7F3EC)
internal val PaperElevated = Color(0xFFFFFCF6)
internal val Ink = Color(0xFF20251F)
internal val InkMuted = Color(0xFF6B7168)
internal val Forest = Color(0xFF315C4C)
internal val ForestSoft = Color(0xFFD7E6DC)
internal val Terracotta = Color(0xFF9B5F44)
internal val TerracottaSoft = Color(0xFFF2DCCF)
internal val OutlineWarm = Color(0xFFDDD5C8)

internal val Night = Color(0xFF121513)
internal val NightElevated = Color(0xFF1C211D)
internal val NightInk = Color(0xFFE9E7DF)
internal val NightMuted = Color(0xFFA3AAA0)
internal val Mint = Color(0xFFA8D5BF)
internal val MintSoft = Color(0xFF2A4A3D)
internal val Clay = Color(0xFFE2A88A)

@Immutable
data class NotePalette(
    val name: String,
    val container: Color,
    val content: Color,
    val accent: Color,
)

val LightNotePalettes = listOf(
    NotePalette("奶油", Color(0xFFFAF0D8), Color(0xFF40351F), Color(0xFFB27D2E)),
    NotePalette("鼠尾草", Color(0xFFE8F0E4), Color(0xFF2D4233), Color(0xFF5E8067)),
    NotePalette("陶土", Color(0xFFF7E7E1), Color(0xFF463029), Color(0xFFAF6A57)),
    NotePalette("雾蓝", Color(0xFFE5EDF2), Color(0xFF293E47), Color(0xFF5B8293)),
    NotePalette("丁香", Color(0xFFEEE9F5), Color(0xFF372F45), Color(0xFF806B9B)),
    NotePalette("亚麻", Color(0xFFF0EAE0), Color(0xFF40362A), Color(0xFF907858)),
)

val DarkNotePalettes = listOf(
    NotePalette("奶油", Color(0xFF2B2519), Color(0xFFF0E5CD), Color(0xFFD8B26B)),
    NotePalette("鼠尾草", Color(0xFF1E2A21), Color(0xFFDCEBDA), Color(0xFF8FB79A)),
    NotePalette("陶土", Color(0xFF2D211E), Color(0xFFF1DCD5), Color(0xFFD79A86)),
    NotePalette("雾蓝", Color(0xFF1C2830), Color(0xFFD8E8EF), Color(0xFF8BB5C8)),
    NotePalette("丁香", Color(0xFF272330), Color(0xFFE7DDF3), Color(0xFFB6A0CF)),
    NotePalette("亚麻", Color(0xFF28231D), Color(0xFFEEE3D3), Color(0xFFC6A983)),
)

val LocalNotePalettes = staticCompositionLocalOf { LightNotePalettes }
