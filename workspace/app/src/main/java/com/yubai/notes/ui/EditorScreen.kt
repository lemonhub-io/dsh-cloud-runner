package com.yubai.notes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yubai.notes.ui.theme.LocalNotePalettes
import com.yubai.notes.util.formatSavedTime
import com.yubai.notes.util.noteCharacterCount
import com.yubai.notes.util.shareNote
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: EditorUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onColorSelected: (Int) -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
) {
    val palettes = LocalNotePalettes.current
    val palette = palettes[state.colorIndex.coerceIn(0, palettes.lastIndex)]
    val targetBackground = lerp(
        MaterialTheme.colorScheme.background,
        palette.container,
        0.55f,
    )
    val background by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(420),
        label = "editorBackground",
    )
    val textColor = MaterialTheme.colorScheme.onBackground
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = palette.accent

    var showPaletteSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val titleFocus = remember { FocusRequester() }
    val bodyFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    BackHandler(onBack = onBack)

    LaunchedEffect(state.id) {
        if (state.isNew) {
            delay(360)
            titleFocus.requestFocus()
            keyboard?.show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            EditorTopBar(
                state = state,
                accent = accent,
                onBack = onBack,
                onTogglePin = onTogglePin,
                onPaletteClick = {
                    keyboard?.hide()
                    showPaletteSheet = true
                },
                onMoreClick = {
                    keyboard?.hide()
                    showMenu = true
                },
                menuExpanded = showMenu,
                onMenuDismiss = { showMenu = false },
                onShare = {
                    showMenu = false
                    shareNote(context, state.title, state.body)
                },
                onDeleteRequest = {
                    showMenu = false
                    showDeleteDialog = true
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(10.dp))

                BasicTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocus),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = textColor,
                        fontSize = 30.sp,
                        lineHeight = 39.sp,
                    ),
                    cursorBrush = SolidColor(accent),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { bodyFocus.requestFocus() },
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (state.title.isEmpty()) {
                                Text(
                                    text = "标题",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        color = textColor.copy(alpha = 0.24f),
                                        fontSize = 30.sp,
                                        lineHeight = 39.sp,
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                Box(
                    modifier = Modifier
                        .padding(top = 15.dp, bottom = 16.dp)
                        .width(34.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent.copy(alpha = 0.85f)),
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    BasicTextField(
                        value = state.body,
                        onValueChange = onBodyChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(bodyFocus),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = textColor.copy(alpha = 0.92f),
                            fontSize = 17.sp,
                            lineHeight = 29.sp,
                        ),
                        cursorBrush = SolidColor(accent),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.body.isEmpty()) {
                                    Column {
                                        Text(
                                            text = "开始记录…",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = textColor.copy(alpha = 0.24f),
                                                fontSize = 17.sp,
                                                lineHeight = 29.sp,
                                            ),
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = "不必完美，先把念头留下。\n你的文字会自动保存。",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = mutedColor.copy(alpha = 0.48f),
                                                lineHeight = 21.sp,
                                            ),
                                        )
                                    }
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                EditorFooter(state = state, accent = accent)
            }
        }
    }

    if (showPaletteSheet) {
        PaletteSheet(
            selectedIndex = state.colorIndex,
            onDismiss = { showPaletteSheet = false },
            onSelect = { index ->
                onColorSelected(index)
                showPaletteSheet = false
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "删除这篇笔记？",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = "删除后仍可在首页底部撤销。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text(
                        text = "删除",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            },
            shape = RoundedCornerShape(26.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun EditorTopBar(
    state: EditorUiState,
    accent: Color,
    onBack: () -> Unit,
    onTogglePin: () -> Unit,
    onPaletteClick: () -> Unit,
    onMoreClick: () -> Unit,
    menuExpanded: Boolean,
    onMenuDismiss: () -> Unit,
    onShare: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f),
                modifier = Modifier.size(22.dp),
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = state.lastSavedAt?.let { "已保存 ${formatSavedTime(it)}" } ?: "新笔记",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
            )
        }

        EditorIconButton(
            icon = Icons.Rounded.PushPin,
            contentDescription = if (state.pinned) "取消置顶" else "置顶笔记",
            tint = if (state.pinned) {
                accent
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            },
            active = state.pinned,
            activeColor = accent,
            onClick = onTogglePin,
        )
        EditorIconButton(
            icon = Icons.Rounded.Palette,
            contentDescription = "更改笔记颜色",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
            onClick = onPaletteClick,
        )
        Box {
            EditorIconButton(
                icon = Icons.Rounded.MoreHoriz,
                contentDescription = "更多操作",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                onClick = onMoreClick,
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = onMenuDismiss,
            ) {
                DropdownMenuItem(
                    text = { Text("分享笔记") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    onClick = onShare,
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "删除笔记",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    onClick = onDeleteRequest,
                )
            }
        }
    }
}

@Composable
private fun EditorIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    active: Boolean = false,
    activeColor: Color = tint,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(42.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (active) activeColor.copy(alpha = 0.13f) else Color.Transparent,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun EditorFooter(
    state: EditorUiState,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${noteCharacterCount(state.body)} 字",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)),
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = accent.copy(alpha = 0.7f),
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "自动保存",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaletteSheet(
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val palettes = LocalNotePalettes.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 30.dp),
        ) {
            Text(
                text = "笔记颜色",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "为此刻的心情，挑一种底色。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                palettes.forEachIndexed { index, palette ->
                    val isSelected = index == selectedIndex
                    val description = if (isSelected) {
                        "${palette.name}，已选择"
                    } else {
                        palette.name
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(palette.container)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) {
                                    palette.accent
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape,
                            )
                            .clickable { onSelect(index) }
                            .semantics {
                                contentDescription = description
                                selected = isSelected
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "当前：${palettes[selectedIndex.coerceIn(0, palettes.lastIndex)].name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
