package com.yubai.notes.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yubai.notes.data.NoteEntity
import com.yubai.notes.ui.theme.LocalNotePalettes
import com.yubai.notes.ui.theme.NotePalette
import com.yubai.notes.util.formatHeaderDate
import com.yubai.notes.util.formatListTime
import com.yubai.notes.util.greetingNow
import com.yubai.notes.util.noteBodyPreview
import com.yubai.notes.util.noteDisplayTitle
import com.yubai.notes.util.shareNote

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    themeMode: ThemeMode,
    isDark: Boolean,
    onQueryChange: (String) -> Unit,
    onFilterChange: (NoteFilter) -> Unit,
    onNewNote: () -> Unit,
    onOpenNote: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    var actionNote by remember { mutableStateOf<NoteEntity?>(null) }
    var showThemeSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewNote,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 5.dp,
                    pressedElevation = 1.dp,
                    focusedElevation = 5.dp,
                    hoveredElevation = 7.dp,
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp),
                    )
                },
                text = {
                    Text(
                        text = "新建笔记",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        },
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 4.dp,
                bottom = 120.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                HomeHeader(
                    state = state,
                    themeMode = themeMode,
                    isDark = isDark,
                    onQueryChange = onQueryChange,
                    onFilterChange = onFilterChange,
                    onThemeClick = { showThemeSheet = true },
                )
            }

            when {
                state.isLoading -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }

                state.notes.isEmpty() -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        EmptyNotes(
                            query = state.query,
                            filter = state.filter,
                            onClearSearch = { onQueryChange("") },
                            onShowAll = { onFilterChange(NoteFilter.ALL) },
                        )
                    }
                }

                else -> {
                    items(
                        items = state.notes,
                        key = { it.id },
                    ) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onOpenNote(note) },
                            onLongClick = { actionNote = note },
                        )
                    }
                }
            }
        }
    }

    actionNote?.let { note ->
        NoteActionsSheet(
            note = note,
            onDismiss = { actionNote = null },
            onTogglePin = {
                onTogglePin(note)
                actionNote = null
            },
            onDelete = {
                onDeleteNote(note)
                actionNote = null
            },
        )
    }

    if (showThemeSheet) {
        ThemeModeSheet(
            current = themeMode,
            onDismiss = { showThemeSheet = false },
            onSelect = onThemeModeChange,
        )
    }
}

@Composable
private fun HomeHeader(
    state: HomeUiState,
    themeMode: ThemeMode,
    isDark: Boolean,
    onQueryChange: (String) -> Unit,
    onFilterChange: (NoteFilter) -> Unit,
    onThemeClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark()
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "余白",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "留一点空白，安放思绪",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
            ThemeButton(
                themeMode = themeMode,
                isDark = isDark,
                onClick = onThemeClick,
            )
        }

        Spacer(Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "${greetingNow()}。",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatHeaderDate(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "把此刻的想法，留在余白里。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        )

        Spacer(Modifier.height(22.dp))

        SearchField(
            query = state.query,
            onQueryChange = onQueryChange,
        )

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterPill(
                label = "全部",
                count = state.totalCount,
                selected = state.filter == NoteFilter.ALL,
                onClick = { onFilterChange(NoteFilter.ALL) },
            )
            FilterPill(
                label = "置顶",
                count = state.pinnedCount,
                selected = state.filter == NoteFilter.PINNED,
                icon = Icons.Rounded.PushPin,
                onClick = { onFilterChange(NoteFilter.PINNED) },
            )
        }

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (state.filter == NoteFilter.PINNED) "置顶笔记" else "全部笔记",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (state.query.isBlank()) {
                    "${state.notes.size} 篇"
                } else {
                    "找到 ${state.notes.size} 篇"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }

        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun BrandMark() {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val secondary = MaterialTheme.colorScheme.secondary
    Canvas(
        modifier = Modifier
            .size(38.dp)
            .semantics { contentDescription = "余白" },
    ) {
        drawRoundRect(
            color = primary,
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        )
        val stroke = 1.9.dp.toPx()
        val startX = size.width * 0.29f
        val endX = size.width * 0.71f
        drawLine(
            color = onPrimary,
            start = Offset(startX, size.height * 0.32f),
            end = Offset(endX, size.height * 0.32f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = onPrimary,
            start = Offset(startX, size.height * 0.48f),
            end = Offset(endX, size.height * 0.48f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = onPrimary.copy(alpha = 0.72f),
            start = Offset(startX, size.height * 0.64f),
            end = Offset(size.width * 0.55f, size.height * 0.64f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = secondary,
            radius = 2.4.dp.toPx(),
            center = Offset(size.width * 0.66f, size.height * 0.71f),
        )
    }
}

@Composable
private fun ThemeButton(
    themeMode: ThemeMode,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val icon = when (themeMode) {
        ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
        ThemeMode.LIGHT -> Icons.Rounded.LightMode
        ThemeMode.DARK -> Icons.Rounded.DarkMode
    }
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = if (isDark) "当前为深色外观，点击切换" else "当前为浅色外观，点击切换",
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(11.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() },
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "搜索标题或内容",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "清空搜索",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val background by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        },
        label = "filterBackground",
    )
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = background,
        contentColor = contentColor,
        border = if (selected) {
            null
        } else {
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
            )
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val palettes = LocalNotePalettes.current
    val palette = palettes[note.colorIndex.coerceIn(0, palettes.lastIndex)]
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(22.dp)
    val title = noteDisplayTitle(note.title, note.body)
    val preview = noteBodyPreview(note.title, note.body)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
        shape = shape,
        color = palette.container,
        border = if (note.pinned) {
            BorderStroke(1.dp, palette.accent.copy(alpha = 0.26f))
        } else {
            null
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 25.sp,
                    ),
                    color = palette.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (note.pinned) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "已置顶",
                        tint = palette.accent,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(13.dp),
                    )
                }
            }
            if (preview.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 20.sp,
                    ),
                    color = palette.content.copy(alpha = 0.7f),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatListTime(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.content.copy(alpha = 0.46f),
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(palette.accent.copy(alpha = 0.55f)),
                )
            }
        }
    }
}

@Composable
private fun EmptyNotes(
    query: String,
    filter: NoteFilter,
    onClearSearch: () -> Unit,
    onShowAll: () -> Unit,
) {
    val searching = query.isNotBlank()
    val pinnedEmpty = filter == NoteFilter.PINNED && !searching

    val title = when {
        searching -> "没有找到相关笔记"
        pinnedEmpty -> "还没有置顶笔记"
        else -> "写下第一段思绪"
    }
    val subtitle = when {
        searching -> "换个关键词试试，或清空搜索条件。"
        pinnedEmpty -> "长按任意笔记，即可把它置顶在这里。"
        else -> "不必写得完整。一句话、一个念头，\n都值得被好好留下。"
    }
    val actionLabel = when {
        searching -> "清空搜索"
        pinnedEmpty -> "查看全部笔记"
        else -> null
    }
    val action = when {
        searching -> onClearSearch
        pinnedEmpty -> onShowAll
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 34.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NoteStackIllustration()
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(9.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 260.dp),
        )
        if (actionLabel != null && action != null) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = action) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun NoteStackIllustration() {
    val backCard = MaterialTheme.colorScheme.secondaryContainer
    val frontCard = MaterialTheme.colorScheme.surfaceVariant
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val accent = MaterialTheme.colorScheme.secondary
    val border = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = Modifier.size(width = 158.dp, height = 128.dp)) {
        val cardWidth = size.width * 0.5f
        val cardHeight = size.height * 0.7f
        val left = (size.width - cardWidth) / 2f
        val top = (size.height - cardHeight) / 2f

        rotate(degrees = -9f, pivot = Offset(size.width / 2f, size.height / 2f)) {
            drawRoundRect(
                color = backCard,
                topLeft = Offset(left - 8.dp.toPx(), top + 3.dp.toPx()),
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
            )
        }
        rotate(degrees = 6f, pivot = Offset(size.width / 2f, size.height / 2f)) {
            drawRoundRect(
                color = frontCard,
                topLeft = Offset(left + 10.dp.toPx(), top - 1.dp.toPx()),
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
            )
            drawRoundRect(
                color = border,
                topLeft = Offset(left + 10.dp.toPx(), top - 1.dp.toPx()),
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                style = Stroke(width = 1.dp.toPx()),
            )
        }

        val frontLeft = left + 10.dp.toPx()
        val contentLeft = frontLeft + 15.dp.toPx()
        val contentTop = top + 19.dp.toPx()
        val lineWidth = cardWidth - 30.dp.toPx()
        drawLine(
            color = lineColor,
            start = Offset(contentLeft, contentTop),
            end = Offset(contentLeft + lineWidth * 0.72f, contentTop),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        repeat(3) { index ->
            val y = contentTop + (21.dp.toPx() * (index + 1))
            drawLine(
                color = lineColor.copy(alpha = lineColor.alpha * (0.75f - index * 0.12f)),
                start = Offset(contentLeft, y),
                end = Offset(contentLeft + lineWidth * if (index == 2) 0.48f else 0.86f, y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        drawCircle(
            color = accent.copy(alpha = 0.8f),
            radius = 4.dp.toPx(),
            center = Offset(frontLeft + cardWidth - 18.dp.toPx(), top + cardHeight - 5.dp.toPx()),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteActionsSheet(
    note: NoteEntity,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val palettes = LocalNotePalettes.current
    val palette = palettes[note.colorIndex.coerceIn(0, palettes.lastIndex)]
    val title = noteDisplayTitle(note.title, note.body)
    val preview = noteBodyPreview(note.title, note.body)

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(palette.accent),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            if (preview.isNotBlank()) {
                Spacer(Modifier.height(7.dp))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            SheetAction(
                icon = Icons.Rounded.PushPin,
                label = if (note.pinned) "取消置顶" else "置顶笔记",
                onClick = onTogglePin,
            )
            SheetAction(
                icon = Icons.Rounded.Share,
                label = "分享笔记",
                onClick = {
                    shareNote(context, note.title, note.body)
                    onDismiss()
                },
            )
            SheetAction(
                icon = Icons.Rounded.DeleteOutline,
                label = "删除笔记",
                destructive = true,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun SheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val color = if (destructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.82f),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(15.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeSheet(
    current: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = "外观",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "选择一个适合此刻的阅读氛围。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            ThemeMode.values().forEach { mode ->
                ThemeOption(
                    mode = mode,
                    selected = mode == current,
                    onClick = {
                        onSelect(mode)
                        onDismiss()
                    },
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val icon = when (mode) {
        ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
        ThemeMode.LIGHT -> Icons.Rounded.LightMode
        ThemeMode.DARK -> Icons.Rounded.DarkMode
    }
    val label = when (mode) {
        ThemeMode.SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色"
        ThemeMode.DARK -> "深色"
    }
    val description = when (mode) {
        ThemeMode.SYSTEM -> "随手机的显示模式自动切换"
        ThemeMode.LIGHT -> "温暖纸张般的明亮界面"
        ThemeMode.DARK -> "适合夜晚的柔和深色界面"
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        } else {
            Color.Transparent
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(21.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}
