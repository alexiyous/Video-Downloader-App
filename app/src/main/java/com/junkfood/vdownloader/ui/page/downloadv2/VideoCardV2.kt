package com.junkfood.vdownloader.ui.page.downloadv2

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.junkfood.vdownloader.ui.common.AsyncImageImpl
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.common.LocalFixedColorRoles
import com.junkfood.vdownloader.ui.common.motion.materialSharedAxisY
import com.junkfood.vdownloader.ui.theme.DownloaderTheme
import com.junkfood.vdownloader.util.toDurationText
import com.junkfood.vdownloader.util.toFileSizeText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val IconButtonSize = 64.dp
private val IconSize = 36.dp
private val ActionButtonContainerColor: Color
    @Composable get() = LocalFixedColorRoles.current.onSecondaryFixed.copy(alpha = 0.68f)
private val ActionButtonContentColor: Color
    @Composable get() = LocalFixedColorRoles.current.secondaryFixed
private val LabelContainerColor: Color = Color.Black.copy(alpha = 0.68f)

@Composable
fun VideoCardV2(
    modifier: Modifier = Modifier,
    viewState: Task.ViewState,
    stateIndicator: @Composable (BoxScope.() -> Unit)? = null,
    actionButton: @Composable (BoxScope.() -> Unit)? = null,
    onButtonClick: () -> Unit,
) {
    with(viewState) {
        VideoCardV2(
            modifier = modifier,
            thumbnailModel = thumbnailUrl,
            title = title,
            uploader = uploader,
            duration = duration,
            fileSizeApprox = fileSizeApprox,
            stateIndicator = stateIndicator,
            actionButton = actionButton,
            onButtonClick = onButtonClick,
        )
    }
}

@Composable
fun VideoListItem(
    modifier: Modifier = Modifier,
    state: Task.State,
    stateIndicator: @Composable (() -> Unit)? = null,
    onRemoveClick: () -> Unit,
    onButtonClick: () -> Unit,
) {
    val downloadState = state.downloadState
    val viewState = state.viewState
    val context = LocalContext.current
    val density = LocalDensity.current.density
    var expanded by remember { mutableStateOf(false) }  // Move expanded state here

    var menuPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .width(175.dp)
            ) {
                ListItemImage(
                    modifier = Modifier.fillMaxSize(),
                    thumbnailModel = viewState.thumbnailUrl
                )
                VideoInfoLabel(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    duration = viewState.duration,
                    fileSizeApprox = viewState.fileSizeApprox
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                TitleText(
                    title = viewState.title,
                    uploader = viewState.uploader,
                    contentPadding = PaddingValues()
                )
                stateIndicator?.invoke()
            }
        }
        AnimatedContent(
            targetState = downloadState,
            transitionSpec = {
                materialSharedAxisY(initialOffsetY = { it / 5 }, targetOffsetY = { -it / 5 })
            },
        ) { currState ->
            if (currState is Completed || currState is Error) {
                Box(
                    modifier =
                    Modifier.fillMaxSize() // Ensure the Box takes full size
                        .offset(x = 25.dp, y = 20.dp) // Adjust position
                ) {
                    // Adjusted IconButton visibility and placement
                    IconButton(
                        onClick = { expanded = true },
                        modifier =
                        Modifier.align(Alignment.BottomEnd)
                            .padding(24.dp) // Use padding to avoid overlap with other elements
                            .clearAndSetSemantics {}
                            .onGloballyPositioned { layoutCoordinates ->
                                menuPosition = layoutCoordinates.positionInRoot()
                            },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.show_more_actions),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onBackground, // Change color as needed
                        )
                    }
                }

                if (expanded) {
                    Dialog(
                        onDismissRequest = { expanded = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false),
                    ) {
                        Box(
                            modifier =
                            Modifier.fillMaxSize()
                                .clickable(onClick = { expanded = false })
                        ) {
                            Column(
                                modifier =
                                Modifier.let {
                                    with(density) {
                                        it.offset(
                                            x = (menuPosition.x / density - 125).dp,
                                            y = (menuPosition.y / density + 20).dp
                                        )
                                    }
                                }
                                    .width(175.dp)
                                    .background(
                                        if (isSystemInDarkTheme()) Color.Gray else Color.White,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {} // Prevents accidental dismiss
                            ) {
                                DropdownMenuItem(
                                    modifier = Modifier.height(60.dp),
                                    onClick = {
                                        expanded = false
                                        onButtonClick()
                                    },
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier =
                                            Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 10.dp,
                                            ),
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ico_info),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(text = stringResource(id = R.string.information))
                                        }
                                    },
                                )
                                DropdownMenuItem(
                                    modifier = Modifier.height(60.dp),
                                    onClick = {
                                        expanded = false
                                        onRemoveClick()
                                    },
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier =
                                            Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 10.dp,
                                            ),
                                        ) {
                                            Icon(
                                                painter =
                                                painterResource(id = R.drawable.ico_delete),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(text = stringResource(id = R.string.delete))
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            } else {}
        }
    }
}



@Preview
@Composable
private fun VideoListItemPreview() {
    DownloaderTheme {
        // Set the downloadState directly to Error state
        val downloadState = Task.DownloadState.Error(action = Task.RestartableAction.Download)

        val viewState = Task.ViewState(
            url = "https://www.example.com",
            title = "Sample Video",
            uploader = "Uploader",
            duration = 120,
            fileSizeApprox = 10.0,
            thumbnailUrl = "https://www.example.com/thumbnail.jpg"
        )

        // Construct Task.State with the Error downloadState
        val taskState = Task.State(
            downloadState = downloadState,
            videoInfo = null,
            viewState = viewState
        )

        Surface {
            // Pass Task.State to VideoListItem
            VideoListItem(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                state = taskState, // Pass Task.State
                stateIndicator = {
                    ListItemStateText(
                        modifier = Modifier.padding(top = 3.dp),
                        downloadState = downloadState,
                        onStopClick = {},
                        onPlayClick = {},
                    )
                },
                onRemoveClick = {},
                onButtonClick = {},
            )
        }
    }
}


@Composable
fun VideoCardV2(
    modifier: Modifier = Modifier,
    thumbnailModel: Any? = null,
    title: String = "",
    uploader: String = "",
    duration: Int = 0,
    fileSizeApprox: Double = .0,
    stateIndicator: @Composable (BoxScope.() -> Unit)? = null,
    actionButton: @Composable (BoxScope.() -> Unit)? = null,
    onButtonClick: () -> Unit,
) {
    val containerColor =
        MaterialTheme.colorScheme.run {
            if (LocalDarkTheme.current.isDarkTheme()) surfaceContainer else surfaceContainerLowest
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column {
            Box(Modifier.fillMaxWidth()) {
                CardImage(modifier = Modifier, thumbnailModel = thumbnailModel)
                Box(Modifier.align(Alignment.TopStart)) { stateIndicator?.invoke(this) }
                Box(Modifier.align(Alignment.Center)) { actionButton?.invoke(this) }
                VideoInfoLabel(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    duration = duration,
                    fileSizeApprox = fileSizeApprox,
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleText(modifier = Modifier.weight(1f), title = title, uploader = uploader)
                IconButton(onButtonClick, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.show_more_actions),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
@Preview
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun VideoCardV2Preview() {
    DownloaderTheme {
        val downloadState = Error(throwable = Throwable(), action = RestartableAction.Download)
        VideoCardV2(
            thumbnailModel = R.drawable.sample3,
            title = stringResource(R.string.video_title_sample_text),
            uploader = stringResource(R.string.video_creator_sample_text),
            actionButton = { ActionButton(modifier = Modifier, downloadState = downloadState) {} },
            stateIndicator = {
                CardStateIndicator(modifier = Modifier, downloadState = downloadState)
            },
        ) {}
    }
}

@Composable
private fun CardImage(modifier: Modifier = Modifier, thumbnailModel: Any? = null) {
    if (thumbnailModel != null) {
        AsyncImageImpl(
            modifier =
                modifier
                    .padding()
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
            model = thumbnailModel,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    } else {
        Surface(
            modifier =
                modifier
                    .padding()
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {}
    }
}

@Composable
private fun ListItemImage(modifier: Modifier = Modifier, thumbnailModel: Any? = null) {
    Box(
        modifier =
        modifier
            .width(175.dp)
            .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)
            .clip(RoundedCornerShape(12.dp)) // Set desired corner radius
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {}
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
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 3.dp),
            text = if (uploader.isNullOrBlank() || uploader == "null") "" else uploader,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VideoInfoLabel(modifier: Modifier = Modifier, duration: Int, fileSizeApprox: Double) {
    val isDarkTheme = isSystemInDarkTheme() // Automatically checks the theme

    // Choose colors based on the current theme
    val textColor = if (isDarkTheme) Color.White else Color.Gray
    val iconTint = if (isDarkTheme) Color.White else Color.Gray

    Surface(
        modifier = modifier,
        color = Color.Transparent, // Transparent background
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        val fileSizeText = fileSizeApprox.toFileSizeText()
        val durationText = duration.toDurationText()
        Row(
            modifier = Modifier.padding(bottom = 6.dp, end = 18.dp), // Adds internal padding to content
            verticalAlignment = Alignment.CenterVertically, // Align icon and text vertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ico_sm_video),
                contentDescription = stringResource(R.string.video),
                modifier = Modifier.size(12.dp),
                tint = iconTint, // Dynamic icon tint
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = durationText,
                color = textColor, // Dynamic text color based on theme
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ), // Set font size to 11sp
            )
        }
    }
}

@Composable
fun CardStateIndicator(modifier: Modifier = Modifier, downloadState: Task.DownloadState) {
    Surface(
        modifier = modifier.padding(vertical = 12.dp, horizontal = 8.dp),
        color = LabelContainerColor,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        CardItemStateText(
            modifier = Modifier.padding(horizontal = 4.dp),
            downloadState = downloadState,
        )
    }
}

@Composable
fun ListItemStateText(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = LocalDarkTheme.current.isDarkTheme(),
    downloadState: Task.DownloadState,
    onStopClick: () -> Unit,
    onPlayClick: () -> Unit,
) {
    val progressBackground =
        if (isDarkTheme) Color(0xFF3B4047) else Color(0XFFD8E8E2) // Dark background for dark theme
    val progressColor = Color(0xFF00B06D) // Always use #00B06D for the progress color

    var iconYPosition by remember { mutableStateOf(0) } // Store position state

    AnimatedContent(
        downloadState,
        transitionSpec = {
            materialSharedAxisY(initialOffsetY = { it / 5 }, targetOffsetY = { -it / 5 })
        },
        contentKey = { it::class.simpleName }
    ) { downloadState ->
        val text = when (downloadState) {
            is Canceled -> stringResource(R.string.status_canceled)
            is Completed -> stringResource(R.string.status_downloaded)
            is Error -> stringResource(R.string.status_error)
            is FetchingInfo -> stringResource(R.string.status_fetching_video_info)
            Idle -> stringResource(R.string.status_enqueued)
            ReadyWithInfo -> stringResource(R.string.status_enqueued)
            is Running -> {
                val progress = downloadState.progress
                if (progress >= 0) {
                    "%.1f %%".format(progress * 100)
                } else {
                    stringResource(R.string.status_downloading)
                }
            }
        }

        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (downloadState) {
                    is Canceled, is Completed -> {
                        LinearProgressIndicator(
                            modifier = Modifier.weight(1f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(50)),
                            color = progressColor,
                            backgroundColor = progressBackground
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onPlayClick, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.Gray.copy(alpha = 0.8f)
                            )
                        }
                    }
                    is FetchingInfo,
                    Idle,
                    ReadyWithInfo -> {
                        Column {
                            LinearProgressIndicator(
                                modifier =
                                Modifier.weight(1f)
                                    .height(12.dp) // Thicker Progress Bar
                                    .clip(RoundedCornerShape(50)), // Rounded corners on both sides
                                color = progressColor,
                                backgroundColor = progressBackground,
                            )

                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    is Error -> {
//                            Spacer(Modifier.weight(1f)) // Push icon to the right
//                            Box(
//                                modifier = Modifier
//                                    .size(24.dp)
//                                    .onGloballyPositioned { layoutCoordinates ->
//                                        iconYPosition = layoutCoordinates.positionInRoot().y.roundToInt()
//                                    }
//                            ) {
//                                IconButton(onClick = { onMoreIconPosition(iconYPosition) }) {
//                                    Icon(
//                                        imageVector = Icons.Outlined.MoreVert,
//                                        contentDescription = stringResource(R.string.show_more_actions),
//                                        tint = MaterialTheme.colorScheme.onBackground
//                                    )
//                                }
//                            }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    is Running -> {
                        val progress = downloadState.progress
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.weight(1f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(50)),
                            color = progressColor,
                            backgroundColor = progressBackground
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onStopClick, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Stop",
                                tint = Color.Gray.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Handle missing states
//                    is FetchingInfo, Idle, ReadyWithInfo, is Error -> {
//                        Text(
//                            text = text,
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    }
                }
            }
        }
    }
}



@Composable
private fun CardItemStateText(modifier: Modifier = Modifier, downloadState: Task.DownloadState) {
    val errorColor =
        MaterialTheme.colorScheme.run {
            if (LocalDarkTheme.current.isDarkTheme()) error else errorContainer
        }
    val textStyle = MaterialTheme.typography.labelSmall
    val contentColor = Color.White

    val text =
        when (downloadState) {
            is Canceled -> R.string.status_canceled
            is Completed -> R.string.status_downloaded
            is Error -> R.string.status_error
            is FetchingInfo -> R.string.status_fetching_video_info
            Idle -> R.string.status_enqueued
            ReadyWithInfo -> R.string.status_enqueued
            is Running -> R.string.status_downloading
        }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (downloadState is Error) {
            Icon(
                imageVector = Icons.Rounded.Error,
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = stringResource(id = text),
            modifier = Modifier,
            style = textStyle,
            color = contentColor,
        )
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    downloadState: Task.DownloadState,
    onActionPost: (UiAction) -> Unit,
) =
    when (downloadState) {
        is Error -> {
            RestartButton(modifier = modifier) { onActionPost(UiAction.Resume) }
        }
        is Canceled -> {
            ResumeButton(modifier = modifier, downloadState.progress) {
                onActionPost(UiAction.Resume)
            }
        }
        is Completed -> {
            PlayVideoButton(modifier = modifier) {
                onActionPost(UiAction.OpenFile(downloadState.filePath))
            }
        }
        is FetchingInfo,
        ReadyWithInfo,
        Idle -> {
            ProgressButton(modifier = modifier, progress = -1f) { onActionPost(UiAction.Cancel) }
        }
        is Running -> {
            ProgressButton(modifier = modifier, progress = downloadState.progress) {
                onActionPost(UiAction.Cancel)
            }
        }
    }

@Composable
private fun ResumeButton(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    onClick: () -> Unit,
) {
    val background = ActionButtonContainerColor

    Box(
        modifier =
            modifier
                .size(IconButtonSize)
                .clip(CircleShape)
                .drawBehind { drawCircle(background) }
                .clickable(onClickLabel = stringResource(R.string.cancel), onClick = onClick)
    ) {
        if (progress != null) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(IconButtonSize).align(Alignment.Center),
                color = ActionButtonContentColor,
                trackColor = Color.Transparent,
                gapSize = 0.dp,
            )
        }
        Icon(
            imageVector = Icons.Rounded.Download,
            contentDescription = stringResource(R.string.restart),
            modifier = Modifier.size(IconSize).align(Alignment.Center),
            tint = ActionButtonContentColor,
        )
    }
}

@Composable
fun RestartButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val background = ActionButtonContainerColor

    Box(
        modifier =
            modifier
                .size(IconButtonSize)
                .clip(CircleShape)
                .drawBehind { drawCircle(background) }
                .clickable(onClickLabel = stringResource(R.string.cancel), onClick = onClick)
    ) {
        Icon(
            imageVector = Icons.Rounded.RestartAlt,
            contentDescription = stringResource(R.string.restart),
            modifier = Modifier.size(IconSize).align(Alignment.Center),
            tint = ActionButtonContentColor,
        )
    }
}

@Composable
private fun PlayVideoButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(IconButtonSize),
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = ActionButtonContainerColor,
                contentColor = ActionButtonContentColor,
            ),
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = stringResource(R.string.open_file),
            modifier = Modifier.size(IconSize),
        )
    }
}

@Composable
private fun ProgressButton(modifier: Modifier = Modifier, progress: Float, onClick: () -> Unit) {
    val animatedProgress by
        animateFloatAsState(
            progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "progress",
        )
    val background = ActionButtonContainerColor

    Box(
        modifier =
            modifier
                .size(IconButtonSize)
                .clip(CircleShape)
                .drawBehind { drawCircle(background) }
                .clickable(onClickLabel = stringResource(R.string.cancel), onClick = onClick)
    ) {
        if (progress < 0) {
            CircularProgressIndicator(
                modifier = Modifier.size(IconButtonSize).align(Alignment.Center),
                color = ActionButtonContentColor,
                trackColor = Color.Transparent,
            )
        } else {
            CircularProgressIndicator(
                { animatedProgress },
                modifier = Modifier.size(IconButtonSize).align(Alignment.Center),
                color = ActionButtonContentColor,
                gapSize = 0.dp,
                trackColor = Color.Transparent,
            )
        }
        Icon(
            imageVector = Icons.Rounded.Pause,
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(IconSize),
            tint = ActionButtonContentColor,
        )
    }
}
