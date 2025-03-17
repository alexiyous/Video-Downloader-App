package com.junkfood.vdownloader.ui.page.history

import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.junkfood.vdownloader.App.Companion.context
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.download.Task
import com.junkfood.vdownloader.download.Task.DownloadState.Canceled
import com.junkfood.vdownloader.download.Task.DownloadState.Completed
import com.junkfood.vdownloader.download.Task.DownloadState.Error
import com.junkfood.vdownloader.download.Task.DownloadState.FetchingInfo
import com.junkfood.vdownloader.download.Task.DownloadState.Idle
import com.junkfood.vdownloader.download.Task.DownloadState.ReadyWithInfo
import com.junkfood.vdownloader.download.Task.DownloadState.Running
import com.junkfood.vdownloader.download.Task.RestartableAction
import com.junkfood.vdownloader.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.theme.DownloaderTheme
import com.junkfood.vdownloader.ui.theme.bottomNavigationDark
import com.junkfood.vdownloader.util.FileUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HistoryListItem(
    modifier: Modifier = Modifier,
    title: String = "",
    uploader: String = "",
    onShowContextMenu: () -> Unit = {},
    onButtonClick: (String) -> Unit = {},
    onMenuAction: (String) -> Unit = {},

) {
    var expanded by remember { mutableStateOf(false) }
    var menuPosition by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    Box(
        modifier =
            Modifier.clip(RoundedCornerShape(12.dp)) // Apply rounded corners
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) // Optional background color
                .padding(10.dp) // Padding around the content
    ) {
        Row(
            modifier = modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically // Align items in the center
        ) {
            Box(modifier = Modifier) {
                Image(
                    painter = painterResource(id = R.drawable.ico_his_google),
                    contentDescription = stringResource(R.string.video),
                    modifier = Modifier.size(50.dp),
                )
            }

            Box {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                    TitleText(
                        modifier = Modifier,
                        title = title,
                        uploader = uploader,
                        contentPadding = PaddingValues(),
                    )
                }

                Box(modifier = Modifier.align(Alignment.CenterEnd).clearAndSetSemantics {}
                    .onGloballyPositioned { layoutCoordinates ->
                        menuPosition = layoutCoordinates.positionInRoot()
                    },) {
                    IconButton(
                        onClick = {
                            expanded = true
                            onShowContextMenu
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.show_more_actions),
                            modifier = Modifier.size(25.dp),
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
                                    Modifier
                                        .let {
                                            with(density) {
                                                it.offset(
                                                    x = menuPosition.x.toDp() - 130.dp,
                                                    y = menuPosition.y.toDp() + 25.dp,
                                                )
                                            }
                                        }
                                        .width(175.dp)
                                        .background(
                                            backgroundColor,
                                            shape = RoundedCornerShape(20.dp),
                                        )
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {} // Prevents accidental dismiss
                                ) {
                                    DropdownMenuItem(
                                        modifier = Modifier.height(60.dp), // Increased height
                                        onClick = {
                                            expanded = false
                                            onMenuAction("new_tab")
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier =
                                                Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 10.dp,
                                                ), // Adds left padding
                                            ) {
                                                Text(
                                                    text =
                                                    stringResource(id = R.string.open_new_tab)
                                                )
                                            }
                                        },
                                    )

                                    DropdownMenuItem(
                                        modifier = Modifier.height(60.dp), // Increased height
                                        onClick = {
                                            expanded = false
                                            onMenuAction("copy_link")
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier =
                                                Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 10.dp,
                                                ), // Adds left padding
                                            ) {
                                                Text(text = stringResource(id = R.string.copy_link))
                                            }
                                        },
                                    )

                                    DropdownMenuItem(
                                        modifier = Modifier.height(60.dp), // Increased height
                                        onClick = {
                                            expanded = false
                                            onMenuAction("share")
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier =
                                                Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 10.dp,
                                                ), // Adds left padding
                                            ) {
                                                Text(text = stringResource(id = R.string.share))
                                            }
                                        },
                                    )

                                    DropdownMenuItem(
                                        modifier = Modifier.height(60.dp), // Increased height
                                        onClick = {
                                            expanded = false
                                            onMenuAction("delete")
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier =
                                                Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 10.dp,
                                                ), // Adds left padding
                                            ) {
                                                Text(text = stringResource(id = R.string.delete))
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleText(
    modifier: Modifier = Modifier,
    title: String,
    uploader: String,
    contentPadding: PaddingValues = PaddingValues(12.dp),
) {
    Column(
        modifier = modifier.padding(contentPadding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Box(modifier = Modifier.fillMaxWidth().padding(end = 20.dp)) {
            Text(
                text = uploader,
                modifier = Modifier.padding(top = 3.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun HistoryListItemPreview() {
    DownloaderTheme {
        val fakeStateList =
            listOf(
                Running(Job(), "", 0.58f),
                Error(throwable = Throwable(), RestartableAction.Download),
                FetchingInfo(Job(), ""),
                Canceled(RestartableAction.Download),
                ReadyWithInfo,
                Idle,
                Completed(null),
            )

        var downloadState: Task.DownloadState by remember { mutableStateOf(Idle) }

        LaunchedEffect(Unit) {
            fakeStateList.forEach {
                delay(2000)
                downloadState = it
            }
        }

        Surface {
            HistoryListItem(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
                title = stringResource(R.string.video_title_sample_text),
                uploader = stringResource(R.string.video_creator_sample_text),
                onButtonClick = { menuType -> },
                onMenuAction = {}
            )
        }

//        Surface {
//            HistoryParentListItem(
//                modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
//                title = stringResource(R.string.video_title_sample_text),
//                onActionSelected = {},
//            )
//        }
    }
}

@Composable
fun HistoryParentListItem(
    modifier: Modifier = Modifier,
    title: String = "",
    onActionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) } // Track dropdown menu state

    Box(
        modifier =
            Modifier.clip(RoundedCornerShape(12.dp)) // Apply border radius (rounded corners)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) // Optional background color
                .padding(12.dp) // Padding around the content
    ) {
        Row(
            modifier = modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier) {
                Image(
                    painter = painterResource(id = R.drawable.ico_his_google),
                    contentDescription = stringResource(R.string.video),
                    modifier = Modifier.size(50.dp),
                )
            }

            Box {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Center, // Center the content vertically
                    horizontalAlignment = Alignment.Start, // Align text to the start horizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(
                        onClick = { expanded = true }, // Expand the dropdown menu
                        modifier = Modifier.align(Alignment.CenterEnd), // Align to the end-center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.show_more_actions),
                            modifier = Modifier.size(25.dp),
                        )
                    }

                    if (expanded) {
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
                                            .background(
                                                if (isSystemInDarkTheme()) Color.Gray
                                                else Color.White,
                                                shape = RoundedCornerShape(20.dp),
                                            )
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable {} // Prevents accidental dismiss when
                                    // clicking inside the menu
                                ) {
                                    DropdownMenuItem(
                                        modifier = Modifier.height(60.dp), // Increased height
                                        onClick = {
                                            onActionSelected("open_new_tab") // Handle action 0
                                            expanded = false
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier =
                                                    Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 10.dp,
                                                    ), // Adds left padding
                                            ) {
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text =
                                                        stringResource(id = R.string.open_new_tab)
                                                )
                                            }
                                        },
                                    )

                                    DropdownMenuItem(
                                        modifier = Modifier.height(60.dp), // Increased height
                                        onClick = {
                                            onActionSelected("copy_link") // Handle action 1
                                            expanded = false
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier =
                                                    Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 10.dp,
                                                    ), // Adds left padding
                                            ) {
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(text = stringResource(id = R.string.history))
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun HistoryParentListItemPreview() {
//    DownloaderTheme {
//        val fakeStateList =
//            listOf(
//                Running(Job(), "", 0.58f),
//                Error(throwable = Throwable(), RestartableAction.Download),
//                FetchingInfo(Job(), ""),
//                Canceled(RestartableAction.Download),
//                ReadyWithInfo,
//                Idle,
//                Completed(null),
//            )
//
//        var downloadState: Task.DownloadState by remember { mutableStateOf(Idle) }
//
//        LaunchedEffect(Unit) {
//            fakeStateList.forEach {
//                delay(2000)
//                downloadState = it
//            }
//        }
//    }
}
