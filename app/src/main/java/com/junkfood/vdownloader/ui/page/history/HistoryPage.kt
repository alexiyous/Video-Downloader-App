package com.junkfood.vdownloader.ui.page.history

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.vdownloader.App.Companion.context
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.database.objects.DownloadedVideoInfo
import com.junkfood.vdownloader.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.component.BackButton
import com.junkfood.vdownloader.ui.page.videolist.RemoveItemDialog
import com.junkfood.vdownloader.ui.page.videolist.VideoDetailDrawer
import com.junkfood.vdownloader.ui.page.videolist.VideoListViewModel
import com.junkfood.vdownloader.ui.theme.bottomNavigationDark
import com.junkfood.vdownloader.util.FileUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(
    onNavigateBack: () -> Unit,
    onMenuOpen: () -> Unit
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

    var showRemoveDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var deleteFile by remember { mutableStateOf(false) }
    var selectedItemIds = remember { mutableStateListOf<Int>() }
    var selectedVideoId by remember { mutableStateOf(0) }
    var currentVideoInfo by remember { mutableStateOf(DownloadedVideoInfo()) }

    val viewModel: VideoListViewModel = koinViewModel()
    val viewState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val fullVideoList by viewModel.videoListFlow.collectAsStateWithLifecycle(emptyList())
    val searchedVideoList by
        viewModel.searchedVideoListFlow.collectAsStateWithLifecycle(emptyList())

    val videoList = if (viewState.isSearching) searchedVideoList else fullVideoList

    val density = LocalDensity.current
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun handleAllDeleteClick() {
        selectedItemIds.clear()
        selectedItemIds.addAll(videoList.map { it.id })
        showDialog = true
    }

    fun handleMenuClick(menuType: String, videoId: Int, videoExtractor: String) {
        if (menuType == "delete") {
            selectedVideoId = videoId
            showRemoveDialog = true
        }
    }

    var expanded by remember { mutableStateOf(false) } // Track dropdown menu state
    var menuPosition by remember { mutableStateOf(Offset.Zero) }
    val clipboardManager = LocalClipboardManager.current
    var isSelectEnabled by remember { mutableStateOf(false) }

    val sheetState =
        androidx.compose.material.rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val hostState = remember { SnackbarHostState() }
    var showBottomSheet by remember { mutableStateOf(false) }
    val fileSizeMap by
    viewModel.fileSizeMapFlow.collectAsStateWithLifecycle(initialValue = emptyMap())

    val shareTitle = stringResource(id = R.string.share)

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
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    // Row to place title next to back button
                    Text(modifier = Modifier, text = stringResource(R.string.history))
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
                colors =
                    TopAppBarDefaults.largeTopAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                actions = {
                    IconButton(
                        onClick = {
                            if (videoList.isEmpty()) {
                                Toast.makeText(
                                        context.applicationContext,
                                        "No items to delete",
                                        Toast.LENGTH_SHORT,
                                    )
                                    .show()
                            } else {
                                handleAllDeleteClick()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ico_delete),
                            contentDescription = "Delete Icon",
                            tint = MaterialTheme.colorScheme.onBackground, // Change color as needed
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (videoList.isEmpty()) {
                    // Show Empty State
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_progress_opps),
                            contentDescription = "No History",
                            modifier = Modifier.size(100.dp),
                        )
                        Text(
                            text = stringResource(R.string.oops_title),
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.oops_content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        videoList.forEach { video ->
                            item {
                                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp).clearAndSetSemantics {}
                                    .onGloballyPositioned { layoutCoordinates ->
                                        menuPosition = layoutCoordinates.positionInRoot()
                                    },) {
                                    HistoryListItem(
                                        title = video.extractor,
                                        uploader = video.videoUrl,
                                        onShowContextMenu = {
                                            view.slightHapticFeedback()
                                            currentVideoInfo = video
                                            scope.launch {
                                                showBottomSheet = true
                                                delay(50)
                                                sheetState.show()
                                            }
                                        },
                                        onButtonClick = { menuType ->
                                            currentVideoInfo = video
                                            handleMenuClick(menuType, video.id, video.extractor)
                                        },
                                        onMenuAction = { strType ->
                                            if (strType == "new_tab") {
                                                onMenuOpen()
                                            } else if (strType == "copy_link") {
                                                clipboardManager.setText(AnnotatedString(video.videoPath))
                                                Toast.makeText(
                                                    context,
                                                    "Link copied to clipboard",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else if (strType == "share") {
                                                currentVideoInfo = video
                                                view.slightHapticFeedback()
                                                FileUtil.createIntentForSharingFile(video.videoPath)
                                                    ?.runCatching {
                                                        context.startActivity(
                                                            Intent.createChooser(this, shareTitle)
                                                        )
                                                    }
                                            } else if (strType == "delete") {
                                                currentVideoInfo = video
                                                showRemoveDialog = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    )

    if (showBottomSheet) {
//        val isFileAvailable = fileSizeMap[currentVideoInfo.id] != 0L
        val isFileAvailable = true
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

    if (showDialog) {
        RemoveHistoryDialog(
            deleteFile = false,
            onDeleteFileToggled = { deleteFile = it },
            info = DownloadedVideoInfo(),
            onRemoveConfirm = {
                viewModel.deleteDownloadHistory(
                    infoList = videoList.filter { selectedItemIds.contains(it.id) },
                    deleteFile = deleteFile,
                )
                showDialog = false
            },
            onDismissRequest = { showDialog = false },
        )
    }

    if (showRemoveDialog) {
        RemoveItemDialog(
            info = currentVideoInfo,
            deleteFile = deleteFile,
            onDeleteFileToggled = { deleteFile = it },
            onRemoveConfirm = {
                viewModel.deleteDownloadHistory(listOf(currentVideoInfo), deleteFile)
                showRemoveDialog = false
            },
            onDismissRequest = { showRemoveDialog = false },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryPagePreview() {
    // Sample data for preview purposes
    val onNavigateBack = { /* handle navigation back */ }

    HistoryPage(onNavigateBack = onNavigateBack, onMenuOpen = {})
}
