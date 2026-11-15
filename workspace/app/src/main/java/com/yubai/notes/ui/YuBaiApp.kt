package com.yubai.notes.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yubai.notes.ui.theme.isDarkTheme
import com.yubai.notes.util.noteDisplayTitle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun YuBaiApp(viewModel: NotesViewModel) {
    val home by viewModel.home.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var cachedEditor by remember { mutableStateOf<EditorUiState?>(null) }
    LaunchedEffect(editor) {
        if (editor != null) cachedEditor = editor
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is NoteEvent.Deleted -> {
                    val title = noteDisplayTitle(event.note.title, event.note.body)
                    val result = snackbarHostState.showSnackbar(
                        message = "已删除「${title.take(14)}」",
                        actionLabel = "撤销",
                        withDismissAction = false,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreNote(event.note)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = editor != null,
            transitionSpec = {
                if (targetState) {
                    (
                        slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 7 } +
                            fadeIn(tween(220))
                        ) togetherWith (
                        slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 12 } +
                            fadeOut(tween(180))
                        )
                } else {
                    (
                        slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 12 } +
                            fadeIn(tween(220))
                        ) togetherWith (
                        slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 7 } +
                            fadeOut(tween(180))
                        )
                }
            },
            label = "screen",
        ) { editing ->
            if (editing) {
                val state = editor ?: cachedEditor
                if (state != null) {
                    EditorScreen(
                        state = state,
                        onBack = viewModel::closeEditor,
                        onTitleChange = viewModel::updateTitle,
                        onBodyChange = viewModel::updateBody,
                        onColorSelected = viewModel::updateColor,
                        onTogglePin = viewModel::toggleEditorPin,
                        onDelete = viewModel::deleteEditorNote,
                    )
                }
            } else {
                HomeScreen(
                    state = home,
                    themeMode = themeMode,
                    isDark = isDarkTheme(themeMode),
                    onQueryChange = viewModel::setQuery,
                    onFilterChange = viewModel::setFilter,
                    onNewNote = viewModel::openNewNote,
                    onOpenNote = viewModel::openNote,
                    onDeleteNote = viewModel::deleteNote,
                    onTogglePin = viewModel::togglePin,
                    onThemeModeChange = viewModel::setThemeMode,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 100.dp),
        )
    }
}
