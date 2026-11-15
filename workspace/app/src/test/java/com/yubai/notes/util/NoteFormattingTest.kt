package com.yubai.notes.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class NoteFormattingTest {

    private val zone = ZoneId.of("Asia/Shanghai")
    private val now = ZonedDateTime.of(2026, 11, 15, 15, 30, 0, 0, zone)
        .toInstant()
        .toEpochMilli()

    @Test
    fun displayTitlePrefersTitle() {
        assertEquals("旅行清单", noteDisplayTitle(" 旅行清单 ", "正文"))
    }

    @Test
    fun displayTitleFallsBackToFirstBodyLine() {
        assertEquals("去看海", noteDisplayTitle("", "\n  \n去看海\n带上相机"))
    }

    @Test
    fun displayTitleHasFriendlyFallback() {
        assertEquals("无标题笔记", noteDisplayTitle("  ", " \n "))
    }

    @Test
    fun previewDoesNotRepeatFallbackTitle() {
        val preview = noteBodyPreview("", "去看海\n带上相机\n买一束花")
        assertEquals("带上相机 买一束花", preview)
    }

    @Test
    fun previewCollapsesWhitespace() {
        assertEquals("第一行 第二行", notePreview("第一行\n\n  第二行  "))
    }

    @Test
    fun previewIsTruncated() {
        val preview = notePreview("一二三四五六", maxLength = 4)
        assertEquals("一二三四…", preview)
    }

    @Test
    fun characterCountIgnoresWhitespace() {
        assertEquals(6, noteCharacterCount("今天 天气\n很好"))
    }

    @Test
    fun listTimeShowsClockForToday() {
        val timestamp = ZonedDateTime.of(2026, 11, 15, 9, 5, 0, 0, zone)
            .toInstant()
            .toEpochMilli()
        assertEquals("09:05", formatListTime(timestamp, now, zone))
    }

    @Test
    fun listTimeShowsYesterday() {
        val timestamp = ZonedDateTime.of(2026, 11, 14, 23, 10, 0, 0, zone)
            .toInstant()
            .toEpochMilli()
        assertEquals("昨天", formatListTime(timestamp, now, zone))
    }

    @Test
    fun listTimeShowsMonthAndDayWithinYear() {
        val timestamp = ZonedDateTime.of(2026, 3, 2, 8, 0, 0, 0, zone)
            .toInstant()
            .toEpochMilli()
        assertEquals("3月2日", formatListTime(timestamp, now, zone))
    }

    @Test
    fun listTimeShowsYearForOlderNotes() {
        val timestamp = ZonedDateTime.of(2024, 12, 31, 8, 0, 0, 0, zone)
            .toInstant()
            .toEpochMilli()
        assertEquals("2024年12月31日", formatListTime(timestamp, now, zone))
    }

    @Test
    fun greetingCoversDayParts() {
        assertEquals("夜深了", greetingForHour(2))
        assertEquals("早上好", greetingForHour(7))
        assertEquals("上午好", greetingForHour(10))
        assertEquals("中午好", greetingForHour(12))
        assertEquals("下午好", greetingForHour(16))
        assertEquals("晚上好", greetingForHour(21))
    }

    @Test
    fun shareTextJoinsTitleAndBody() {
        assertEquals("标题\n\n正文", buildNoteShareText(" 标题 ", "\n正文 "))
        assertEquals("只有正文", buildNoteShareText("", "只有正文"))
        assertEquals("", buildNoteShareText("", ""))
    }
}
