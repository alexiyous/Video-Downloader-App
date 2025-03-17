package com.junkfood.vdownloader.ui.page.videolist

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.vdownloader.App
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.database.backup.BackupUtil
import com.junkfood.vdownloader.database.backup.BackupUtil.toJsonString
import com.junkfood.vdownloader.database.backup.BackupUtil.toURLListString
import com.junkfood.vdownloader.database.objects.DownloadedVideoInfo
import com.junkfood.vdownloader.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.vdownloader.ui.common.LocalWindowWidthState
import com.junkfood.vdownloader.ui.component.CheckBoxItem
import com.junkfood.vdownloader.ui.component.ConfirmButton
import com.junkfood.vdownloader.ui.component.DismissButton
import com.junkfood.vdownloader.ui.component.MediaListItem
import com.junkfood.vdownloader.ui.component.MenuButton
import com.junkfood.vdownloader.ui.component.DownloaderDialog
import com.junkfood.vdownloader.util.AUDIO_REGEX
import com.junkfood.vdownloader.util.FileUtil
import com.junkfood.vdownloader.util.ToastUtil
import com.junkfood.vdownloader.util.toFileSizeText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

fun DownloadedVideoInfo.filterByType(
    videoFilter: Boolean = false,
    audioFilter: Boolean = true,
): Boolean {
    return if (!(videoFilter || audioFilter)) true
    else if (audioFilter) this.videoPath.contains(Regex(AUDIO_REGEX))
    else !this.videoPath.contains(Regex(AUDIO_REGEX))
}

fun DownloadedVideoInfo.filterSort(
    viewState: VideoListViewModel.VideoListViewState,
    filterSet: Set<String>,
): Boolean {
    return filterByType(videoFilter = viewState.videoFilter, audioFilter = viewState.audioFilter) &&
        filterByExtractor(filterSet.elementAtOrNull(viewState.activeFilterIndex))
}

fun DownloadedVideoInfo.filterByExtractor(extractor: String?): Boolean {
    return extractor.isNullOrEmpty() || (this.extractor == extractor)
}

private const val TAG = "VideoListPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListPage(
    viewModel: VideoListViewModel = koinViewModel(),
    onMenuOpen: () -> Unit,
    onActionMenu: () -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(
                initialHeightOffset = rememberTopAppBarState().heightOffsetLimit,
                initialContentOffset = 0f,
            ),
            canScroll = { true },
        )

    val softKeyboardController = LocalSoftwareKeyboardController.current

    val viewState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val fullVideoList by viewModel.videoListFlow.collectAsStateWithLifecycle(emptyList())
    val searchedVideoList by
        viewModel.searchedVideoListFlow.collectAsStateWithLifecycle(emptyList())

    val videoList = if (viewState.isSearching) searchedVideoList else fullVideoList
    val filterSet by viewModel.filterSetFlow.collectAsState(mutableSetOf())

    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val fileSizeMap by
        viewModel.fileSizeMapFlow.collectAsStateWithLifecycle(initialValue = emptyMap())
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val hostState = remember { SnackbarHostState() }

    var currentVideoInfo by remember { mutableStateOf(DownloadedVideoInfo()) }

    var isSelectEnabled by remember { mutableStateOf(false) }
    var showRemoveMultipleItemsDialog by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    val selectedItemIds = remember(videoList, viewState) { mutableStateListOf<Int>() }

    LaunchedEffect(isSelectEnabled) {
        if (!isSelectEnabled) {
            delay(200)
            selectedItemIds.clear()
        }
    }

    val selectedVideoCount =
        remember(selectedItemIds.size) {
            mutableIntStateOf(
                videoList.count { info ->
                    selectedItemIds.contains(info.id) &&
                        info.filterByType(videoFilter = true, audioFilter = false)
                }
            )
        }
    val selectedAudioCount =
        remember(selectedItemIds.size) {
            mutableIntStateOf(
                videoList.count { info ->
                    selectedItemIds.contains(info.id) &&
                        info.filterByType(videoFilter = false, audioFilter = true)
                }
            )
        }

    val selectedFileSizeSum by
        remember(selectedItemIds.size) {
            derivedStateOf {
                selectedItemIds.fold(0L) { acc: Long, id: Int ->
                    acc + fileSizeMap.getOrElse(id) { 0L }
                }
            }
        }

    val visibleItemCount =
        remember(videoList, viewState) {
            mutableIntStateOf(videoList.count { it.filterSort(viewState, filterSet) })
        }

    val checkBoxState by
        remember(selectedItemIds, visibleItemCount) {
            derivedStateOf {
                if (selectedItemIds.isEmpty()) ToggleableState.Off
                else if (
                    selectedItemIds.size == visibleItemCount.intValue &&
                        selectedItemIds.isNotEmpty()
                )
                    ToggleableState.On
                else ToggleableState.Indeterminate
            }
        }

    var showRemoveDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showBottomEditSheet by remember { mutableStateOf(false) }

    BackHandler(isSelectEnabled || viewState.isSearching) {
        if (isSelectEnabled) {
            isSelectEnabled = false
        } else {
            viewModel.toggleSearch(false)
        }
    }

    LaunchedEffect(sheetState.targetValue, isSelectEnabled) {
        if (showBottomSheet || isSelectEnabled) {
            softKeyboardController?.hide()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                title = { Text(text = stringResource(R.string.downloads_history)) },
                navigationIcon = { MenuButton { onMenuOpen() } },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically // Ensures vertical centering
                    ) {
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            //                            IconButton(onClick = { onActionMenu() }) {
                            //                                Icon(
                            //                                    imageVector =
                            // Icons.Outlined.MoreVert,
                            //                                    contentDescription =
                            //                                    stringResource(id =
                            // R.string.show_more_actions),
                            //                                    tint =
                            // MaterialTheme.colorScheme.onBackground, // Change color as needed
                            //                                )
                            //                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors =
                    TopAppBarDefaults.largeTopAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
            )
        },
        bottomBar = {
            AnimatedVisibility(
                isSelectEnabled,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                BottomAppBar(modifier = Modifier) {
                    val selectAllText = stringResource(R.string.select_all)
                    TriStateCheckbox(
                        modifier = Modifier.semantics { this.contentDescription = selectAllText },
                        state = checkBoxState,
                        onClick = {
                            view.slightHapticFeedback()
                            when (checkBoxState) {
                                ToggleableState.On -> selectedItemIds.clear()
                                else -> {
                                    for (item in videoList) {
                                        if (
                                            !selectedItemIds.contains(item.id) &&
                                                item.filterSort(viewState, filterSet)
                                        ) {
                                            selectedItemIds.add(item.id)
                                        }
                                    }
                                }
                            }
                        },
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text =
                            stringResource(R.string.multiselect_item_count)
                                .format(selectedVideoCount.intValue, selectedAudioCount.intValue),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    IconButton(
                        onClick = {
                            view.slightHapticFeedback()
                            showRemoveMultipleItemsDialog = true
                        },
                        enabled = selectedItemIds.isNotEmpty(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = stringResource(id = R.string.remove),
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = hostState) },
    ) { innerPadding ->
        if (fullVideoList.isEmpty())
            Box(modifier = Modifier.fillMaxSize()) {
                val painter =
                    painterResource(
                        id = R.drawable.img_progress_opps
                    ) // Replace with your drawable resource ID
                Column(
                    modifier = Modifier.align(Alignment.Center).widthIn(max = 360.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Image Box for controlling position and alignment
                    Box(
                        modifier =
                            Modifier.padding(
                                    bottom = 25.dp
                                ) // Adjust this padding to set distance from text
                                .fillMaxWidth(0.5f)
                                .height(120.dp) // Specify height for consistency
                                .widthIn(max = 240.dp),
                        contentAlignment = Alignment.BottomCenter, // Keeps the image at the bottom
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.size(81.dp, 104.dp), // Adjust size for the new image
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

        val cellCount =
            when (LocalWindowWidthState.current) {
                WindowWidthSizeClass.Expanded -> 2
                else -> 1
            }
        val span: (LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(cellCount) }
        val shareTitle = stringResource(id = R.string.share)

        LazyColumn(modifier = Modifier, state = lazyListState, contentPadding = innerPadding) {
            for (info in videoList) {
                item(key = info.id, contentType = { info.videoPath.contains(AUDIO_REGEX) }) {
                    with(info) {
                        AnimatedVisibility(
                            modifier = Modifier,
                            visible = info.filterSort(viewState, filterSet),
                            exit = shrinkVertically() + fadeOut(),
                            enter = expandVertically() + fadeIn(),
                        ) {
                            MediaListItem(
                                modifier = Modifier,
                                title = videoTitle,
                                author = videoAuthor,
                                thumbnailUrl = thumbnailUrl,
                                videoPath = videoPath,
                                videoFileSize = fileSizeMap.getOrElse(id) { 0L },
                                videoUrl = videoUrl,
                                isSelectEnabled = { isSelectEnabled },
                                isSelected = { selectedItemIds.contains(id) },
                                onSelect = {
                                    if (selectedItemIds.contains(id)) selectedItemIds.remove(id)
                                    else selectedItemIds.add(id)
                                },
                                onClick = {
                                    FileUtil.openFile(path = videoPath) {
                                        ToastUtil.makeToastSuspend(
                                            App.context.getString(R.string.file_unavailable)
                                        )
                                    }
                                },
                                onLongClick = {
                                    isSelectEnabled = true
                                    selectedItemIds.add(id)
                                },
                                onShowContextMenu = {
                                    view.slightHapticFeedback()
                                    currentVideoInfo = info
                                    scope.launch {
                                        showBottomSheet = true
                                        delay(50)
                                        sheetState.show()
                                    }
                                },
                                onMenuAction = { strType ->
                                    if (strType == "share") {
                                        view.slightHapticFeedback()
                                        FileUtil.createIntentForSharingFile(videoPath)
                                            ?.runCatching {
                                                context.startActivity(
                                                    Intent.createChooser(this, shareTitle)
                                                )
                                            }
                                    } else if (strType == "rename") {
                                        view.slightHapticFeedback()
                                        currentVideoInfo = info
                                        scope.launch {
                                            showBottomEditSheet = true
                                            delay(50)
                                            sheetState.show()
                                        }
                                    } else if (
                                        strType == "delete"
                                    ) {
                                        showRemoveDialog = true
                                    } else if (strType == "info") {
                                        view.slightHapticFeedback()
                                        currentVideoInfo = info
                                        scope.launch {
                                            showBottomSheet = true
                                            delay(50)
                                            sheetState.show()
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        val isFileAvailable = fileSizeMap[currentVideoInfo.id] != 0L
        VideoDetailDrawer(
            sheetState = sheetState,
            info = currentVideoInfo,
            isFileAvailable = isFileAvailable,
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomSheet = false }
            },
            onDelete = { showRemoveDialog = true },
        )
    }

    if (showBottomEditSheet) {
        val isFileAvailable = fileSizeMap[currentVideoInfo.id] != 0L
        VideoEditDrawer(
            sheetState = sheetState,
            info = currentVideoInfo,
            isFileAvailable = isFileAvailable,
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { showBottomEditSheet = false }
            },
            onSave = { newTitle ->
                viewModel.updateVideoTitle(currentVideoInfo, newTitle)
                showBottomEditSheet = false
            },
        )
    }

    var deleteFile by remember { mutableStateOf(false) }

    if (showRemoveDialog) {
        RemoveItemDialog(
            info = currentVideoInfo,
            deleteFile = deleteFile,
            onDeleteFileToggled = { deleteFile = it },
            onRemoveConfirm = {
                viewModel.deleteDownloadHistory(listOf(currentVideoInfo), deleteFile = deleteFile)
            },
            onDismissRequest = { showRemoveDialog = false },
        )
    }

    if (showRemoveMultipleItemsDialog) {
        DownloaderDialog(
            onDismissRequest = { showRemoveMultipleItemsDialog = false },
            title = { Text(stringResource(R.string.delete_info)) },
            text = {
                Column {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        text =
                            stringResource(R.string.delete_multiple_items_msg)
                                .format(selectedItemIds.size),
                    )
                    CheckBoxItem(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        text =
                            stringResource(R.string.delete_file) +
                                " (${selectedFileSizeSum.toFileSizeText()})",
                        checked = deleteFile,
                    ) {
                        deleteFile = !deleteFile
                    }
                }
            },
            confirmButton = {
                ConfirmButton {
                    viewModel.deleteDownloadHistory(
                        infoList = videoList.filter { selectedItemIds.contains(it.id) },
                        deleteFile = deleteFile,
                    )
                    showRemoveMultipleItemsDialog = false
                    isSelectEnabled = false
                }
            },
            dismissButton = { DismissButton { showRemoveMultipleItemsDialog = false } },
        )
    }

    var backupString by remember { mutableStateOf("") }
}

private fun List<DownloadedVideoInfo>.backupToString(type: BackupUtil.BackupType): String {
    return when (type) {
        BackupUtil.BackupType.DownloadHistory -> reversed().toJsonString()
        BackupUtil.BackupType.URLList -> toURLListString()
        else -> throw IllegalArgumentException()
    }
}
