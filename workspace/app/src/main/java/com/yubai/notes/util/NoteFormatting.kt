package com.yubai.notes.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
private val fullDateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA)
private val weekdayFormatter = DateTimeFormatter.ofPattern("E", Locale.CHINA)
private val savedTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)

/** Title shown in cards; falls back to the first meaningful line of the body. */
fun noteDisplayTitle(title: String, body: String): String {
    val trimmedTitle = title.trim()
    if (trimmedTitle.isNotEmpty()) return trimmedTitle
    val firstLine = body.lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }
        .orEmpty()
    return firstLine.ifEmpty { "无标题笔记" }
}

/** A single-space preview with the leading/trailing noise removed. */
fun notePreview(body: String, maxLength: Int = 180): String {
    val cleaned = body
        .replace('\n', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
    return if (cleaned.length > maxLength) cleaned.take(maxLength).trimEnd() + "…" else cleaned
}

/**
 * Preview for a card. When the title is empty, the first body line becomes the title, so the
 * preview starts after it to avoid showing the same sentence twice.
 */
fun noteBodyPreview(title: String, body: String, maxLength: Int = 180): String {
    val previewSource = if (title.isBlank()) {
        val lines = body.lines()
        val firstLine = lines.indexOfFirst { it.isNotBlank() }
        if (firstLine >= 0) lines.drop(firstLine + 1).joinToString("\n") else ""
    } else {
        body
    }
    return notePreview(previewSource, maxLength)
}

/** Counts non-whitespace characters, which feels natural for Chinese notes. */
fun noteCharacterCount(body: String): Int = body.count { !it.isWhitespace() }

fun formatListTime(
    epochMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val dateTime = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return when {
        dateTime.toLocalDate() == today -> timeFormatter.format(dateTime)
        dateTime.toLocalDate() == today.minusDays(1) -> "昨天"
        dateTime.year == today.year -> monthDayFormatter.format(dateTime)
        else -> fullDateFormatter.format(dateTime)
    }
}

fun formatSavedTime(
    epochMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): String = savedTimeFormatter.format(Instant.ofEpochMilli(epochMillis).atZone(zone))

fun formatHeaderDate(
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val today: LocalDate = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return "${monthDayFormatter.format(today)} · ${weekdayFormatter.format(today)}"
}

fun greetingForHour(hour: Int): String = when (hour) {
    in 0..4 -> "夜深了"
    in 5..8 -> "早上好"
    in 9..11 -> "上午好"
    in 12..13 -> "中午好"
    in 14..17 -> "下午好"
    else -> "晚上好"
}

fun greetingNow(
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): String = greetingForHour(Instant.ofEpochMilli(nowMillis).atZone(zone).hour)
