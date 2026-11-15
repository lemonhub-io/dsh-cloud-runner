package com.yubai.notes.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yubai.notes.data.NoteEntity
import com.yubai.notes.data.NotesDatabase
import com.yubai.notes.data.NotesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class NoteFilter { ALL, PINNED }

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun from(value: String?): ThemeMode =
            values().firstOrNull { it.name == value } ?: SYSTEM
    }
}

data class HomeUiState(
    val notes: List<NoteEntity> = emptyList(),
    val query: String = "",
    val filter: NoteFilter = NoteFilter.ALL,
    val totalCount: Int = 0,
    val pinnedCount: Int = 0,
    val isLoading: Boolean = true,
)

data class EditorUiState(
    val id: String,
    val title: String,
    val body: String,
    val colorIndex: Int,
    val pinned: Boolean,
    val createdAt: Long,
    val lastSavedAt: Long?,
    val isNew: Boolean,
)

sealed interface NoteEvent {
    data class Deleted(val note: NoteEntity) : NoteEvent
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotesRepository(NotesDatabase.get(application).noteDao())
    private val preferences =
        application.getSharedPreferences("yubai_preferences", Context.MODE_PRIVATE)

    private val _query = MutableStateFlow("")
    private val _filter = MutableStateFlow(NoteFilter.ALL)

    val home: StateFlow<HomeUiState> = combine(
        repository.notes,
        _query,
        _filter,
    ) { notes, query, filter ->
        val normalizedQuery = query.trim()
        val searched = if (normalizedQuery.isEmpty()) {
            notes
        } else {
            notes.filter { note ->
                note.title.contains(normalizedQuery, ignoreCase = true) ||
                    note.body.contains(normalizedQuery, ignoreCase = true)
            }
        }
        HomeUiState(
            notes = if (filter == NoteFilter.PINNED) searched.filter { it.pinned } else searched,
            query = query,
            filter = filter,
            totalCount = notes.size,
            pinnedCount = notes.count { it.pinned },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    private val _editor = MutableStateFlow<EditorUiState?>(null)
    val editor: StateFlow<EditorUiState?> = _editor.asStateFlow()

    private val _themeMode =
        MutableStateFlow(ThemeMode.from(preferences.getString(KEY_THEME_MODE, null)))
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _events = MutableSharedFlow<NoteEvent>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    private var saveJob: Job? = null

    fun setQuery(query: String) {
        _query.value = query
    }

    fun setFilter(filter: NoteFilter) {
        _filter.value = filter
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun openNewNote() {
        saveJob?.cancel()
        val now = System.currentTimeMillis()
        _editor.value = EditorUiState(
            id = UUID.randomUUID().toString(),
            title = "",
            body = "",
            colorIndex = 0,
            pinned = false,
            createdAt = now,
            lastSavedAt = null,
            isNew = true,
        )
    }

    fun openNote(note: NoteEntity) {
        saveJob?.cancel()
        _editor.value = EditorUiState(
            id = note.id,
            title = note.title,
            body = note.body,
            colorIndex = note.colorIndex,
            pinned = note.pinned,
            createdAt = note.createdAt,
            lastSavedAt = note.updatedAt,
            isNew = false,
        )
    }

    fun updateTitle(title: String) {
        _editor.update { it?.copy(title = title) }
        scheduleSave()
    }

    fun updateBody(body: String) {
        _editor.update { it?.copy(body = body) }
        scheduleSave()
    }

    fun updateColor(colorIndex: Int) {
        _editor.update { it?.copy(colorIndex = colorIndex) }
        scheduleSave()
    }

    fun toggleEditorPin() {
        _editor.update { it?.copy(pinned = !it.pinned) }
        scheduleSave()
    }

    fun closeEditor() {
        val closing = _editor.value ?: return
        saveJob?.cancel()
        _editor.value = null
        viewModelScope.launch {
            if (closing.isBlank()) {
                if (!closing.isNew) {
                    repository.deleteById(closing.id)
                    _events.emit(NoteEvent.Deleted(closing.toEntity()))
                }
            } else {
                persist(closing)
            }
        }
    }

    /** Called when the app leaves the foreground so the final keystrokes are not lost. */
    fun flushEditor() {
        val current = _editor.value ?: return
        saveJob?.cancel()
        if (current.isBlank()) return
        viewModelScope.launch { persist(current) }
    }

    fun deleteEditorNote() {
        val current = _editor.value ?: return
        saveJob?.cancel()
        _editor.value = null
        // A draft that was never persisted has nothing to delete or undo.
        if (current.isNew) return
        viewModelScope.launch {
            repository.deleteById(current.id)
            _events.emit(NoteEvent.Deleted(current.toEntity()))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.delete(note)
            _events.emit(NoteEvent.Deleted(note))
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.save(note.copy(pinned = !note.pinned))
        }
    }

    fun restoreNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.save(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    private fun scheduleSave() {
        val current = _editor.value ?: return
        if (current.isBlank() && current.isNew) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(AUTOSAVE_DELAY_MS)
            val latest = _editor.value ?: return@launch
            persist(latest)
        }
    }

    private suspend fun persist(state: EditorUiState) {
        if (state.isBlank()) return
        val now = System.currentTimeMillis()
        repository.save(state.toEntity(updatedAt = now))
        if (_editor.value?.id == state.id) {
            _editor.update {
                it?.copy(isNew = false, lastSavedAt = now)
            }
        }
    }

    private fun EditorUiState.isBlank(): Boolean =
        title.isBlank() && body.isBlank()

    private fun EditorUiState.toEntity(updatedAt: Long = System.currentTimeMillis()) = NoteEntity(
        id = id,
        title = title,
        body = body,
        colorIndex = colorIndex,
        pinned = pinned,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
        const val AUTOSAVE_DELAY_MS = 320L
    }
}
