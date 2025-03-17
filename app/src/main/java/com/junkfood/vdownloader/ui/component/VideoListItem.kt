package com.junkfood.vdownloader.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.common.AsyncImageImpl
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.theme.bottomNavigationDark
import com.junkfood.vdownloader.util.toFileSizeText

private const val AUDIO_REGEX = "\\.(mp3|aac|opus|m4a|flac|wav)"

@Composable
@Preview
fun MediaListItemPreview() {
    MaterialTheme() {
        Surface() {
            MediaListItem(
                title = stringResource(id = R.string.video_title_sample_text),
                author = stringResource(id = (R.string.video_creator_sample_text)),
                videoFileSize = 5678 * 1024 * 1024L,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaListItem(
    modifier: Modifier = Modifier,
    title: String = "",
    author: String = "",
    thumbnailUrl: String = "",
    videoPath: String = "",
    videoUrl: String = "",
    videoFileSize: Long = 0L,
    isSelectEnabled: () -> Boolean = { false },
    isSelected: () -> Boolean = { false },
    onSelect: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onShowContextMenu: () -> Unit = {},
    onMenuAction: (String) -> Unit = {},
) {
    val isAudio = videoPath.contains(Regex(AUDIO_REGEX))
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val isFileAvailable = videoFileSize != 0L
    val fileSizeText = videoFileSize.toFileSizeText()

    var expanded by remember { mutableStateOf(false) }
    var menuPosition by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    Box(
        modifier =
            with(modifier) {
                    if (!isSelectEnabled())
                        combinedClickable(
                            enabled = true,
                            onClick = { onClick() },
                            onClickLabel = stringResource(R.string.open_file),
                            onLongClick = {
                                onLongClick()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onLongClickLabel = stringResource(R.string.multiselect_mode),
                        )
                    else selectable(selected = isSelected(), onClick = onSelect)
                }
                .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp)) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.CenterVertically),
                visible = isSelectEnabled(),
            ) {
                Checkbox(
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp),
                    checked = isSelected(),
                    onCheckedChange = null,
                )
            }
            MediaImage(modifier = Modifier, imageModel = thumbnailUrl, isAudio = isAudio)
            Column(
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (author != "null")
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter =
                            painterResource(
                                R.drawable.ico_thm_video
                            ), // You can choose another icon here if needed
                        contentDescription = "File size",
                        modifier = Modifier.size(12.dp), // Adjust size as needed
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Space between icon and text
                    Text(
                        text =
                            if (isFileAvailable) fileSizeText
                            else stringResource(R.string.unavailable),
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            with(MaterialTheme.colorScheme) {
                                if (isFileAvailable) onSurfaceVariant else error
                            },
                        maxLines = 1,
                    )
                }
            }
        }
        AnimatedVisibility(
            modifier =
                Modifier.align(Alignment.BottomEnd)
                    .offset(x = -10.dp, y = -10.dp), // Apply offset here to adjust position
            visible = !isSelectEnabled(),
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(100)),
        ) {
            IconButton(
                modifier =
                    Modifier.clearAndSetSemantics {}
                        .onGloballyPositioned { layoutCoordinates ->
                            menuPosition = layoutCoordinates.positionInRoot()
                        },
                onClick = {
                    expanded = true
                    onShowContextMenu
                },
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(id = R.string.show_more_actions),
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
                                Modifier.let {
                                        with(density) {
                                            it.offset(
                                                x = menuPosition.x.toDp() - 130.dp,
                                                y = menuPosition.y.toDp() + 25.dp,
                                            )
                                        }
                                    }
                                    .width(175.dp)
                                    .background(backgroundColor, shape = RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {} // Prevents accidental dismiss
                        ) {
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
                                        Icon(
                                            painter =
                                                painterResource(id = R.drawable.ico_send_share),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = stringResource(id = R.string.share))
                                    }
                                },
                            )

                            DropdownMenuItem(
                                modifier = Modifier.height(60.dp), // Increased height
                                onClick = {
                                    expanded = false
                                    onMenuAction("rename")
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
                                        Icon(
                                            painter = painterResource(id = R.drawable.ico_rename),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = stringResource(id = R.string.rename))
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
                                        Icon(
                                            painter = painterResource(id = R.drawable.ico_delete),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = stringResource(id = R.string.delete))
                                    }
                                },
                            )

                            DropdownMenuItem(
                                modifier = Modifier.height(60.dp), // Increased height
                                onClick = {
                                    expanded = false
                                    onMenuAction("info")
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaImage(
    modifier: Modifier = Modifier,
    imageModel: String,
    isAudio: Boolean = false,
    contentDescription: String? = null,
) {
    AsyncImageImpl(
        modifier =
            modifier
                .height(90.dp)
                .aspectRatio(if (!isAudio) 16f / 9f else 1f, matchHeightConstraintsFirst = true)
                .clip(MaterialTheme.shapes.extraSmall),
        model = imageModel,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
    )
}
