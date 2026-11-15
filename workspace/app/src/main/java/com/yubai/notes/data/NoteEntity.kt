package com.yubai.notes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single note. [colorIndex] indexes into the pastel palettes defined in the UI layer so the
 * database stays independent from Compose colors.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val colorIndex: Int = 0,
    val pinned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
