package com.yubai.notes.data

import kotlinx.coroutines.flow.Flow

class NotesRepository(private val dao: NoteDao) {

    val notes: Flow<List<NoteEntity>> = dao.observeAll()

    suspend fun save(note: NoteEntity) = dao.upsert(note)

    suspend fun delete(note: NoteEntity) = dao.delete(note)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}
