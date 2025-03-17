package com.junkfood.vdownloader.ui.page.tab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.component.BackButton
import com.junkfood.vdownloader.ui.page.videolist.VideoListViewModel
import com.junkfood.vdownloader.ui.theme.bottomNavigationDark
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabListPage(
    tabNumber: Int, // Receive the tab number
    onNavigateBack: () -> Unit,
    onAction: () -> Unit,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(
                initialHeightOffset = rememberTopAppBarState().heightOffsetLimit,
                initialContentOffset = 0f,
            ),
            canScroll = { true },
        )

    val viewModel: VideoListViewModel = koinViewModel()
    viewModel.searchedVideoListFlow.collectAsStateWithLifecycle(emptyList())

    val videoList =
        listOf(
            DownloadedVideoInfo(id = "1", extractor = "HomePage", videoUrl = "about:blank"),
            DownloadedVideoInfo(
                id = "2",
                extractor = "YouTube",
                videoUrl = "https://m.youtube.com",
            ),
            DownloadedVideoInfo(
                id = "3",
                extractor = "Facebook",
                videoUrl = "https://www.facebook.com",
            ),
            DownloadedVideoInfo(
                id = "4",
                extractor = "Instagram",
                videoUrl = "https://www.instagram.com",
            ),
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = "Tab ($tabNumber tab)") }, // Dynamic title
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
                colors =
                    TopAppBarDefaults.largeTopAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                actions = {
                    var expanded by remember { mutableStateOf(false) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically // Ensures vertical centering
                    ) {
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription =
                                        stringResource(id = R.string.show_more_actions),
                                    tint =
                                        MaterialTheme.colorScheme
                                            .onBackground, // Change color as needed
                                )
                            }

                            if (expanded) {
                                val backgroundColor =
                                    if (LocalDarkTheme.current.isDarkTheme()) {
                                        bottomNavigationDark
                                    } else {
                                        MaterialTheme.colorScheme.background // Light theme background
                                    }

                                Dialog(
                                    onDismissRequest = { expanded = false },
                                    properties =
                                        DialogProperties(
                                            usePlatformDefaultWidth = false
                                        ), // Ensures full-screen coverage
                                ) {
                                    Box(
                                        modifier =
                                            Modifier.fillMaxSize()
                                                .clickable(
                                                    onClick = { expanded = false }
                                                ) // Clicking outside closes the dialog
                                    ) {
                                        Column(
                                            modifier =
                                                Modifier.align(Alignment.TopEnd)
                                                    .offset(
                                                        x = -10.dp,
                                                        y = 50.dp,
                                                    ) // Moves menu to top-right
                                                    .width(200.dp)
                                                    .background(backgroundColor, shape = RoundedCornerShape(20.dp))
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .clickable {} // Prevents accidental dismiss
                                            // when clicking inside the menu
                                        ) {
                                            DropdownMenuItem(
                                                modifier =
                                                    Modifier.height(60.dp), // Increased height
                                                onClick = {
                                                    expanded = false
                                                    onAction()
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment =
                                                            Alignment.CenterVertically,
                                                        modifier =
                                                            Modifier.padding(
                                                                horizontal = 10.dp,
                                                                vertical = 10.dp,
                                                            ), // Adds left padding
                                                    ) {
                                                        Icon(
                                                            painter =
                                                                painterResource(
                                                                    id = R.drawable.ico_open_new_tab
                                                                ),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(24.dp),
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text =
                                                                stringResource(
                                                                    id = R.string.open_new_tab
                                                                )
                                                        )
                                                    }
                                                },
                                            )

                                            DropdownMenuItem(
                                                modifier =
                                                    Modifier.height(60.dp), // Increased height
                                                onClick = { expanded = false },
                                                text = {
                                                    Row(
                                                        verticalAlignment =
                                                            Alignment.CenterVertically,
                                                        modifier =
                                                            Modifier.padding(
                                                                horizontal = 10.dp,
                                                                vertical = 10.dp,
                                                            ), // Adds left padding
                                                    ) {
                                                        Icon(
                                                            painter =
                                                                painterResource(
                                                                    id = R.drawable.ico_delete
                                                                ),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(24.dp),
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text =
                                                                stringResource(
                                                                    id = R.string.clear_all
                                                                )
                                                        )
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                                Box(modifier = Modifier.padding(10.dp)) {
                                    TabItem(
                                        tabId = video.id,
                                        title = video.extractor,
                                        uploader = video.videoUrl,
                                        onButtonClick = {},
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}

data class DownloadedVideoInfo(val id: String, val extractor: String, val videoUrl: String)

@Preview(showBackground = true)
@Composable
fun TabListPagePreview() {
    // Sample data for preview purposes
    val onNavigateBack = { /* handle navigation back */ }

    TabListPage(tabNumber = 1, onNavigateBack = onNavigateBack, onAction = {})
}
