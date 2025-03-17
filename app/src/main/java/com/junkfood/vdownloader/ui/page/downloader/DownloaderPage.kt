import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.download.DownloaderV2
import com.junkfood.vdownloader.download.Task
import com.junkfood.vdownloader.ui.component.MenuButton
import com.junkfood.vdownloader.ui.page.downloadv2.Filter
import com.junkfood.vdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.vdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.junkfood.vdownloader.ui.theme.DownloaderTheme
import com.junkfood.vdownloader.util.DownloadUtil
import org.koin.compose.koinInject

@Composable
fun InputBox(dialogViewModel: DownloadDialogViewModel) {
    val textState = remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current // Access ClipboardManager
    fun startConcurrentDownloads() {
        if (textState.value.isNotBlank()) {
            val url = textState.value

            // Post the first action for proceeding with URLs
            dialogViewModel.postAction(Action.ProceedWithURLs(listOf(url)))

            // Retrieve preferences without using `remember`, as this is not a composable function
            val preferences = DownloadUtil.DownloadPreferences.createFromPreferences()

            // Post the second action for downloading with preferences
            dialogViewModel.postAction(
                Action.DownloadWithPreset(
                    urlList = listOf(url),
                    preferences =
                        preferences.copy(
                            extractAudio = false // DownloadType.Video == DownloadType.Audio
                        ),
                )
            )

            textState.value = ""
        }
    }

    fun pasteClipboardContent() {
        val clipboardText = clipboardManager.getText()?.text ?: ""
        if (clipboardText.isNotBlank()) {
            textState.value = clipboardText
        }
    }
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    ) // Set background color with alpha
            ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(164.dp),
    ) {
        Box(
            modifier =
                Modifier.padding(20.dp)
                    .clip(RoundedCornerShape(52.dp))
                    .border(
                        BorderStroke(2.dp, Color(0xFFE5E6E9)),
                        shape = RoundedCornerShape(100.dp),
                    )
                    .background(MaterialTheme.colorScheme.background)
                    .align(Alignment.CenterHorizontally)
                    .height(52.dp) // Adjust the height as needed
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                // Link icon on the left
                Image(
                    painter = painterResource(id = R.drawable.link),
                    contentDescription = "link(1) 1",
                    colorFilter = ColorFilter.tint(Color(0xff8b8b8b)),
                    modifier = Modifier.size(35.dp).padding(start = 12.dp),
                )

                // Input field taking the remaining space
                TextField(
                    value = textState.value,
                    onValueChange = { newValue -> textState.value = newValue },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_or_url),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = TextStyle(fontSize = 12.sp),
                        )
                    },
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor =
                                Color.Transparent, // Make focused line transparent
                            unfocusedIndicatorColor =
                                Color.Transparent, // Make unfocused line transparent
                        ),
                    textStyle =
                        TextStyle(
                            fontSize = 16.sp // Adjust font size as needed
                        ),
                    //                    singleLine = true, // Ensure the TextField is a single
                    // line
                    modifier =
                        Modifier.weight(1f) // Use weight to fill the remaining space
                            .padding(horizontal = 5.dp), // Add horizontal padding
                )

                IconButton(
                    onClick = { startConcurrentDownloads() },
                    modifier =
                        Modifier.size(46.dp) // Size of the circular button
                            .clip(CircleShape) // Make it circular
                            .background(Color.Transparent) // Background color (can be changed)
                            .padding(end = 6.dp),
                ) {
                    Box(
                        modifier =
                            Modifier.size(40.dp).clip(CircleShape).background(Color(0xffc8efe1))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.turn_back),
                            contentDescription = "turn-back 1",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.align(Alignment.Center).size(20.dp),
                        )
                    }
                }
            }
        }

        Button(
            onClick = { pasteClipboardContent() },
            modifier = Modifier.padding(horizontal = 40.dp).fillMaxWidth().height(60.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            shape = RoundedCornerShape(16.dp), // Control the border radius
        ) {
            Text(
                text = stringResource(R.string.url_paste),
                fontSize = 22.sp, // Set the font size to 22sp
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color.White,
            )
        }
    }
}

@Composable
fun HelpBox(modifier: Modifier, onShowHelpPage: () -> Unit) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ),
        modifier = modifier.fillMaxWidth().padding(start = 20.dp).clip(RoundedCornerShape(100.dp)),
        onClick = { onShowHelpPage() },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp), // Adjust the height as needed
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Link icon on the left
                Image(
                    painter = painterResource(id = R.drawable.ico_right),
                    contentDescription = "link(1) 1",
                    modifier = Modifier.size(30.dp),
                )

                // Text on the right
                Text(
                    text = stringResource(R.string.how_to_download),
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun GridItem(iconRes: Int, text: String, onItemClick: () -> Unit) {
    Box(
        modifier =
            Modifier.padding(8.dp) // Padding around each grid item
                .clickable { onItemClick() } // Handle item click
                .fillMaxWidth() // Ensure item fills available space
    ) {
        // Vertical layout with icon at the top and text at the bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Icon inside circular button
            Box(
                modifier =
                    Modifier.size(44.dp) // Size of the circular button
                        .background(Color.Transparent) // Background color (can be changed)
                        .padding(top = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Icon",
                    modifier = Modifier.align(Alignment.Center).size(44.dp),
                )
            }

            // Text part at the bottom
            Text(
                text = text,
                modifier = Modifier.padding(top = 1.dp), // Padding between icon and text
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
            )
        }
    }
}

@Composable
fun SocialListBox(onSocialEvent: (String) -> Unit) {
    val lstSocialBoxs =
        listOf(
            Pair(R.drawable.ico_facebook, "Facebook"),
            Pair(R.drawable.ico_whatsapp, "Whatsapp"),
            Pair(R.drawable.ico_instagram, "Instagram"),
            Pair(R.drawable.ico_twitter, "Twitter"),
            Pair(R.drawable.pinterest, "Pinterest"),
            Pair(R.drawable.ico_ticktok, "Tiktok"),
            Pair(R.drawable.ico_vimeo, "Vimeo"),
            Pair(R.drawable.ico_buzzvideo, "BuzzVideo"),
        )

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    ) // Set background color with alpha
            ),
        modifier = Modifier.fillMaxWidth().padding(20.dp).height(186.dp),
    ) {
        Box(Modifier.fillMaxSize().heightIn(max = 186.dp)) { // Ensure Box takes available space
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // 2 columns
                modifier =
                    Modifier.fillMaxSize() // Ensure LazyVerticalGrid fills the parent
                        .padding(10.dp),
            ) {
                items(lstSocialBoxs) { item ->
                    GridItem(
                        iconRes = item.first,
                        text = item.second,
                        onItemClick = { onSocialEvent(item.second) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderPage(
    dialogViewModel: DownloadDialogViewModel,
    onMenuOpen: () -> Unit,
    onActionMenu: () -> Unit,
    onShowHelpPage: () -> Unit,
    onSocialEvent: (String) -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(
                initialHeightOffset = rememberTopAppBarState().heightOffsetLimit,
                initialContentOffset = 0f,
            ),
            canScroll = { true },
        )

    val hostState = remember { SnackbarHostState() }

    val downloader: DownloaderV2 = koinInject() // Inject DownloaderV2
    val taskDownloadStateMap = downloader.getTaskStateMap()
    var activeFilter by remember { mutableStateOf(Filter.CanceledAndDownloading) }
    val filteredMap by
        remember(activeFilter) {
            derivedStateOf { taskDownloadStateMap.filter { activeFilter.predict(it.toPair()) } }
        }

    var videoCount by remember { mutableStateOf(0) }

    LaunchedEffect(filteredMap) { videoCount = filteredMap.size }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.video_downloader)) },
                navigationIcon = { MenuButton { onMenuOpen() } },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = RectangleShape,
                                    )
                                    .wrapContentSize(Alignment.CenterEnd)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = videoCount.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            // Icon Button to trigger dropdown menu
                            IconButton(onClick = { onActionMenu() }) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription =
                                        stringResource(id = R.string.show_more_actions),
                                    tint =
                                        MaterialTheme.colorScheme
                                            .onBackground, // Change color as needed
                                )
                            }
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
        snackbarHost = { SnackbarHost(hostState = hostState) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Content of the page
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .padding(innerPadding) // Apply inner padding for scaffold content
            ) {
                InputBox(dialogViewModel = dialogViewModel)
                SocialListBox(onSocialEvent)

                Spacer(modifier = Modifier.weight(1f))

                HelpBox(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    onShowHelpPage = onShowHelpPage,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun DownloaderPagePreview() {
    // Create a fake DownloaderV2 implementation with mock methods
    val fakeDownloader =
        object : DownloaderV2 {
            override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
                return SnapshotStateMap() // Return an empty map
            }

            override fun cancel(task: Task): Boolean {
                return false // Always return false for preview
            }

            override fun restart(task: Task) {
                // No-op for preview
            }

            override fun enqueue(task: Task) {
                // No-op for preview
            }

            override fun enqueue(task: Task, state: Task.State) {
                // No-op for preview
            }

            override fun remove(task: Task): Boolean {
                return false // Always return false for preview
            }
        }

    // Create a mock ViewModel with the fake downloader
    val mockViewModel = remember { DownloadDialogViewModel(fakeDownloader) }

    DownloaderTheme {
        DownloaderPage(
            dialogViewModel = mockViewModel,
            onMenuOpen = {},
            onActionMenu = {},
            onShowHelpPage = {},
            onSocialEvent = {},
        )
    }
}
