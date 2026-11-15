package com.yubai.notes.util

import android.content.Context
import android.content.Intent

fun buildNoteShareText(title: String, body: String): String {
    val cleanTitle = title.trim()
    val cleanBody = body.trim()
    return when {
        cleanTitle.isNotEmpty() && cleanBody.isNotEmpty() -> "$cleanTitle\n\n$cleanBody"
        cleanTitle.isNotEmpty() -> cleanTitle
        else -> cleanBody
    }
}

fun shareNote(context: Context, title: String, body: String) {
    val text = buildNoteShareText(title, body)
    if (text.isBlank()) return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享笔记"))
}
