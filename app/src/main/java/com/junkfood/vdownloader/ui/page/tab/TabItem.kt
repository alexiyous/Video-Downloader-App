package com.junkfood.vdownloader.ui.page.tab

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.junkfood.vdownloader.ui.theme.DownloaderTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

@Composable
fun TabItem(
    modifier: Modifier = Modifier,
    tabId: String = "",
    title: String = "",
    uploader: String = "",
    onButtonClick: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) } // Track dropdown menu state
    var iconPosition by remember { mutableStateOf(Offset.Zero) } // Store icon position

    val iconRes =
        when (tabId) {
            "2" -> R.drawable.ico_tab_youtube
            "3" -> R.drawable.ico_tab_facebook
            "4" -> R.drawable.ico_tab_instagram // Default icon
            else -> R.drawable.ico_tab_web // Replace with your drawable
        }

    Box(modifier = Modifier) {
        Row(modifier = modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier =
                    Modifier.size(width = 100.dp, height = 67.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(R.string.video),
                    modifier = Modifier.size(30.dp),
                )
            }

            Box {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                ) {
                    TitleText(
                        modifier = Modifier,
                        tabIcon = iconRes,
                        title = title,
                        uploader = uploader,
                        contentPadding = PaddingValues(),
                    )
                }

                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(
                        onClick = { expanded = true },
                        modifier =
                            Modifier.align(Alignment.CenterEnd).onGloballyPositioned { coordinates
                                ->
                                val localPos = coordinates.positionInRoot() // Get position
                                iconPosition =
                                    Offset(
                                        localPos.x,
                                        localPos.y + coordinates.size.height,
                                    ) // Position below
                            },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ico_tab_close),
                            contentDescription = "remove_item",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleText(
    modifier: Modifier = Modifier,
    tabIcon: Int,
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
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontSize = 19.sp, // Set font size to 30.sp
                    fontWeight = FontWeight.Bold, // Make text bold
                ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Box(modifier = Modifier.fillMaxWidth().padding(end = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = tabIcon), // Replace with your icon
                    contentDescription = null, // Decorative icon
                    modifier = Modifier.size(12.dp), // 12x12 size
                )

                Spacer(modifier = Modifier.width(4.dp)) // Small space between icon & text

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
}

@Preview
@Composable
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun TabItemPreview() {
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
            TabItem(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
                tabId = "",
                title = stringResource(R.string.video_title_sample_text),
                uploader = stringResource(R.string.video_creator_sample_text),
                onButtonClick = { menuType -> },
            )
        }
    }
}
