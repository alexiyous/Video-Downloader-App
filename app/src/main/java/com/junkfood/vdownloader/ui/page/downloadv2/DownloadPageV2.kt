package com.junkfood.vdownloader.ui.page.downloadv2

import android.content.Intent
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.download.DownloaderV2
import com.junkfood.vdownloader.download.Task
import com.junkfood.vdownloader.download.Task.DownloadState.Completed
import com.junkfood.vdownloader.download.Task.DownloadState.Error
import com.junkfood.vdownloader.download.Task.DownloadState.FetchingInfo
import com.junkfood.vdownloader.download.Task.DownloadState.Idle
import com.junkfood.vdownloader.download.Task.DownloadState.ReadyWithInfo
import com.junkfood.vdownloader.download.Task.DownloadState.Running
import com.junkfood.vdownloader.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.common.LocalWindowWidthState
import com.junkfood.vdownloader.ui.component.DownloaderModalBottomSheet
import com.junkfood.vdownloader.ui.page.downloadv2.configure.Config
import com.junkfood.vdownloader.ui.page.downloadv2.configure.DownloadDialog
import com.junkfood.vdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.vdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.junkfood.vdownloader.ui.page.downloadv2.configure.FormatPage
import com.junkfood.vdownloader.ui.page.downloadv2.configure.PlaylistSelectionPage
import com.junkfood.vdownloader.util.DownloadUtil
import com.junkfood.vdownloader.util.FileUtil
import com.junkfood.vdownloader.util.getErrorReport
import com.junkfood.vdownloader.util.makeToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val TAG = "DownloadPageV2"

enum class Filter {
    All,
    Downloading,
    Canceled,
    Finished,
    CanceledAndDownloading; // New combined filter

    @Composable
    @ReadOnlyComposable
    fun label(): String =
        when (this) {
            All -> stringResource(R.string.all)
            Downloading -> stringResource(R.string.status_downloading)
            Canceled -> stringResource(R.string.status_canceled)
            Finished -> stringResource(R.string.status_completed)
            CanceledAndDownloading ->
                stringResource(R.string.status_downloading) // Add this string resource
        }

    fun predict(entry: Pair<Task, Task.State>): Boolean {
        if (this == All) return true
        val state = entry.second.downloadState
        return when (this) {
            Downloading -> {
                when (state) {
                    is FetchingInfo,
                    Idle,
                    ReadyWithInfo,
                    is Running -> true
                    else -> false
                }
            }
            Canceled -> {
                state is Error || state is Task.DownloadState.Canceled
            }
            Finished -> {
                state is Completed
            }
            CanceledAndDownloading -> { // Logic for the combined filter
                state is FetchingInfo ||
                    state is Idle ||
                    state is ReadyWithInfo ||
                    state is Running ||
                    state is Error ||
                    state is Task.DownloadState.Canceled
            }
            else -> false
        }
    }
}

sealed interface UiAction {
    data class OpenFile(val filePath: String?) : UiAction

    data class ShareFile(val filePath: String?) : UiAction

    data class OpenThumbnailURL(val url: String) : UiAction

    data object CopyVideoURL : UiAction

    data class OpenVideoURL(val url: String) : UiAction

    data object Cancel : UiAction

    data object Delete : UiAction

    data object Resume : UiAction

    data class CopyErrorReport(val throwable: Throwable) : UiAction
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPageV2(
    modifier: Modifier = Modifier,
    dialogViewModel: DownloadDialogViewModel,
    onMenuOpen: (() -> Unit) = {},
    onActionMenu: () -> Unit,
    downloader: DownloaderV2 = koinInject(),
) {
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    DownloadPageImplV2(
        modifier = modifier,
        taskDownloadStateMap = downloader.getTaskStateMap(),
        downloadCallback = {
            view.slightHapticFeedback()
            dialogViewModel.postAction(Action.ShowSheet())
        },
        onMenuOpen = onMenuOpen,
        onActionMenu = onActionMenu,
    ) { task, action ->
        view.slightHapticFeedback()
        when (action) {
            UiAction.Cancel -> downloader.cancel(task)
            UiAction.Delete -> downloader.remove(task)
            UiAction.Resume -> downloader.restart(task)
            is UiAction.CopyErrorReport -> {
                clipboardManager.setText(
                    AnnotatedString(getErrorReport(action.throwable, task.url))
                )
                context.makeToast(R.string.error_copied)
            }
            UiAction.CopyVideoURL -> {
                clipboardManager.setText(AnnotatedString(task.url))
                context.makeToast(R.string.link_copied)
            }
            is UiAction.OpenFile -> {
                action.filePath?.let {
                    FileUtil.openFile(path = it) { context.makeToast(R.string.file_unavailable) }
                }
            }
            is UiAction.OpenThumbnailURL -> {
                uriHandler.openUri(action.url)
                uriHandler.openUri(action.url)
                uriHandler.openUri(action.url)
            }
            is UiAction.OpenVideoURL -> {
                uriHandler.openUri(action.url)
            }
            is UiAction.ShareFile -> {
                val shareTitle = context.getString(R.string.share)
                FileUtil.createIntentForSharingFile(action.filePath)?.let {
                    context.startActivity(Intent.createChooser(it, shareTitle))
                }
            }
        }
    }

    var preferences by remember {
        mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
    }
    val sheetValue by dialogViewModel.sheetValueFlow.collectAsStateWithLifecycle()
    val state by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()

    val selectionState = dialogViewModel.selectionStateFlow.collectAsStateWithLifecycle().value

    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sheetValue) {
        if (sheetValue == DownloadDialogViewModel.SheetValue.Expanded) {
            showDialog = true
        } else {
            launch { sheetState.hide() }.invokeOnCompletion { showDialog = false }
        }
    }

    if (showDialog) {

        DownloadDialog(
            state = state,
            sheetState = sheetState,
            config = Config(),
            preferences = preferences,
            onPreferencesUpdate = { preferences = it },
            onActionPost = { dialogViewModel.postAction(it) },
        )
    }
    when (selectionState) {
        is DownloadDialogViewModel.SelectionState.FormatSelection ->
            FormatPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )

        is DownloadDialogViewModel.SelectionState.PlaylistSelection -> {
            PlaylistSelectionPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )
        }

        DownloadDialogViewModel.SelectionState.Idle -> {}
    }
}

@Composable
private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        top = calculateTopPadding() + other.calculateTopPadding(),
        bottom = calculateBottomPadding() + other.calculateBottomPadding(),
        start =
            calculateStartPadding(layoutDirection) + other.calculateStartPadding(layoutDirection),
        end = calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
    )
}

private const val HeaderSpacingDp = 28

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPageImplV2(
    modifier: Modifier = Modifier,
    taskDownloadStateMap: SnapshotStateMap<Task, Task.State>,
    downloadCallback: () -> Unit = {},
    onMenuOpen: (() -> Unit) = {},
    onActionMenu: () -> Unit,
    onActionPost: (Task, UiAction) -> Unit,
) {
    var activeFilter by remember { mutableStateOf(Filter.CanceledAndDownloading) }
    val filteredMap by
        remember(activeFilter) {
            derivedStateOf { taskDownloadStateMap.filter { activeFilter.predict(it.toPair()) } }
        }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val view = LocalView.current

    fun showActionSheet(task: Task) {
        view.slightHapticFeedback()
        scope.launch {
            selectedTask = task
            delay(50)
            sheetState.show()
        }
    }

    LaunchedEffect(selectedTask, taskDownloadStateMap.size) {
        if (!taskDownloadStateMap.contains(selectedTask)) {
            selectedTask == null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        //        floatingActionButton = { FABs(modifier = Modifier, downloadCallback =
        // downloadCallback) },
    ) { windowInsetsPadding ->
        val lazyListState = rememberLazyGridState()
        val windowWidthSizeClass = LocalWindowWidthState.current
        val spacerHeight =
            with(LocalDensity.current) {
                if (windowWidthSizeClass != WindowWidthSizeClass.Compact) 0f
                else HeaderSpacingDp.dp.toPx()
            }
        var headerOffset by remember { mutableFloatStateOf(spacerHeight) }
        var isGridView by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .then(
                        if (windowWidthSizeClass != WindowWidthSizeClass.Compact) Modifier
                        else
                            Modifier.nestedScroll(
                                connection =
                                    TopBarNestedScrollConnection(
                                        maxOffset = spacerHeight,
                                        flingAnimationSpec = rememberSplineBasedDecay(),
                                        offset = { headerOffset },
                                        onOffsetUpdate = { headerOffset = it },
                                    )
                            )
                    )
        ) {
            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    //                    Spacer(Modifier.height(with(LocalDensity.current) {
                    // headerOffset.toDp() }))
                    Header(
                        onMenuOpen = onMenuOpen,
                        modifier = Modifier,
                        onActionMenu = onActionMenu,
                    )
                    if (headerOffset <= 0.1f && spacerHeight > 0f) {
                        HorizontalDivider(thickness = Dp.Hairline)
                    }
                }

                LazyVerticalGrid(
                    modifier = Modifier,
                    state = lazyListState,
                    columns = GridCells.Adaptive(240.dp),
                    contentPadding =
                        windowInsetsPadding +
                            PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    if (filteredMap.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            val videoCount =
                                filteredMap.count {
                                    !it.value.viewState.videoFormats.isNullOrEmpty()
                                }
                            SubHeader(
                                modifier = Modifier,
                                videoCount = videoCount,
                                audioCount = filteredMap.size - videoCount,
                                isGridView = isGridView,
                                onToggleView = { isGridView = !isGridView },
                                onShowMenu = { context.makeToast("Not implemented yet!") },
                            )
                        }
                    }

                    if (isGridView) {
                        items(
                            items =
                                filteredMap.toList().sortedBy { (_, state) -> state.downloadState },
                            key = { (task, _) -> task.id },
                        ) { (task, state) ->
                            with(state.viewState) {
                                VideoCardV2(
                                    modifier = Modifier.padding(bottom = 20.dp).padding(),
                                    viewState = this,
                                    actionButton = {
                                        ActionButton(
                                            modifier = Modifier,
                                            downloadState = state.downloadState,
                                        ) {
                                            onActionPost(task, it)
                                        }
                                    },
                                    stateIndicator = {
                                        CardStateIndicator(
                                            modifier = Modifier,
                                            downloadState = state.downloadState,
                                        )
                                    },
                                    onButtonClick = { showActionSheet(task) },
                                )
                            }
                        }
                    } else {
                        items(
                            items = filteredMap.toList().sortedBy { (_, state) -> state.downloadState },
                            key = { (task, state) -> "${task.id}_${state.downloadState}" }, // Key includes downloadState
                            span = { GridItemSpan(maxLineSpan) }
                        ) { (task, state) ->
                            // Variable to hold the more icon position
                            var moreIconPosition by remember { mutableStateOf(0) }
                            var expanded by remember { mutableStateOf(false) }  // Move expanded state here

                            VideoListItem(
                                modifier = Modifier
                                    .padding(bottom = 16.dp), // Ensure the padding is as per your layout
                                state = state, // Pass the task state to VideoListItem
                                stateIndicator = {
                                    ListItemStateText(
                                        modifier = Modifier.padding(top = 3.dp), // Modify as needed for visual spacing
                                        downloadState = state.downloadState,
                                        onStopClick = { onActionPost(task, UiAction.Cancel) },
                                        onPlayClick = { onActionPost(task, UiAction.Resume) },
                                    )
                                },
                                onRemoveClick = { onActionPost(task, UiAction.Delete) }, // Handle remove action
                                onButtonClick = { showActionSheet(task) }, // Show action sheet when button is clicked
                            )
                        }
                    }
                }
            }
        }
        if (filteredMap.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                DownloadQueuePlaceholder(
                    modifier =
                        Modifier.fillMaxHeight(0.4f).widthIn(max = 360.dp).align(Alignment.Center)
                )
            }
        }
    }
    if (selectedTask != null) {
        val task = selectedTask!!
        val (downloadState, _, viewState) = taskDownloadStateMap[task] ?: return
        DownloaderModalBottomSheet(
            sheetState = sheetState,
            contentPadding = PaddingValues(),
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { selectedTask = null }
            },
        ) {
            SheetContent(
                task = task,
                downloadState = downloadState,
                viewState = viewState,
                onDismissRequest = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedTask = null }
                },
                onActionPost = onActionPost,
            )
        }
    }
}

@Composable
fun Header(modifier: Modifier = Modifier, onMenuOpen: () -> Unit = {}, onActionMenu: () -> Unit) {
    val windowWidthSizeClass = LocalWindowWidthState.current
    when (windowWidthSizeClass) {
        WindowWidthSizeClass.Expanded -> {
            HeaderExpanded(modifier = modifier)
        }
        else -> {
            HeaderCompact(modifier = modifier, onMenuOpen = onMenuOpen, onActionMenu = onActionMenu)
        }
    }
}

@Composable
private fun HeaderCompact(
    modifier: Modifier = Modifier,
    onMenuOpen: () -> Unit,
    onActionMenu: () -> Unit,
) {
    Row(modifier = modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(5.dp))
        IconButton(onClick = onMenuOpen, modifier = Modifier) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = stringResource(R.string.show_navigation_drawer),
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            stringResource(R.string.video_progress),
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                ),
        )
        Spacer(modifier = Modifier.weight(1f))

        Box {
            IconButton(onClick = { onActionMenu() }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(id = R.string.show_more_actions),
                    tint = MaterialTheme.colorScheme.onBackground, // Change color as needed
                )
            }
        }
    }
}

@Composable
private fun HeaderExpanded(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            stringResource(R.string.download_progress),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
        )
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
fun SubHeader(
    modifier: Modifier = Modifier,
    containerColor: Color =
        MaterialTheme.colorScheme.run {
            if (LocalDarkTheme.current.isDarkTheme()) background else background
        },
    videoCount: Int = 0,
    audioCount: Int = 0,
    isGridView: Boolean = true,
    onToggleView: () -> Unit,
    onShowMenu: () -> Unit,
) {
    //        if (videoCount > 0) {
    //            append(pluralStringResource(R.plurals.video_count, videoCount).format(videoCount))
    //            if (audioCount > 0) {
    //                append(", ")
    //            }
    //        }
    //        if (audioCount > 0) {
    //            append(pluralStringResource(R.plurals.downloading_count, ).format(audioCount +
    // videoCount))
    //        }
    // }
    Row(
        modifier = modifier.padding(top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.status_downloading),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier =
                    Modifier.size(23.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary, // Background color
                            shape = CircleShape, // Border radius
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (videoCount + audioCount).toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White, // Text color for contrastbackground
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
@Preview
private fun DownloadQueuePlaceholder(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        ConstraintLayout {
            val (image, text) = createRefs()
            val showImage =
                with(LocalDensity.current) {
                    this@BoxWithConstraints.constraints.maxHeight >= 240.dp.toPx()
                }

            // Base container for the image
            Box(
                modifier =
                    Modifier.fillMaxHeight(0.5f).widthIn(max = 240.dp).constrainAs(image) {
                        top.linkTo(parent.top) // Add margin for top spacin g
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                contentAlignment = Alignment.BottomCenter, // Center-align the content within the box
            ) {
                // Smaller image inside the box
                Image(
                    painter =
                        painterResource(
                            id = R.drawable.img_progress_opps
                        ), // Replace with your smaller image
                    contentDescription = null,
                    modifier =
                        Modifier.size(81.dp, 104.dp), // Use the actual size of your smaller image
                )
            }

            Column(
                modifier =
                    Modifier.constrainAs(text) {
                        top.linkTo(image.bottom, margin = 25.dp) // Set distance to the text
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.oops_title),
                    modifier = Modifier.padding(horizontal = 30.dp),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontSize = 30.sp, // Set font size to 30.sp
                            fontWeight = FontWeight.Bold, // Make text bold
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.oops_content),
                    modifier = Modifier.padding(top = 4.dp).padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
