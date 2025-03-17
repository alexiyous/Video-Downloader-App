package com.junkfood.vdownloader.ui.page.videolist

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.database.objects.DownloadedVideoInfo
import com.junkfood.vdownloader.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.vdownloader.ui.component.ClearButton
import com.junkfood.vdownloader.ui.component.DownloaderModalBottomSheetM2
import com.junkfood.vdownloader.ui.component.FilledTonalButtonWithIcon
import com.junkfood.vdownloader.ui.component.OutlinedButtonWithIcon
import com.junkfood.vdownloader.ui.theme.DownloaderTheme
import com.junkfood.vdownloader.util.findURLsFromString

@Composable
fun VideoEditDrawer(
    sheetState: ModalBottomSheetState,
    info: DownloadedVideoInfo,
    isFileAvailable: Boolean = true,
    onDismissRequest: () -> Unit = {},
    onSave: (String) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val view = LocalView.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    val urlList = remember { mutableStateListOf<String>() }

    BackHandler(sheetState.targetValue == ModalBottomSheetValue.Expanded) { onDismissRequest() }

    LaunchedEffect(Unit) {
        clipboardManager.getText()?.let {
            urlList.clear()
            urlList.addAll(findURLsFromString(it.toString()).toSet())
        }
    }

    with(info) {
        VideoEditDrawerImpl(
            sheetState = sheetState,
            title = videoTitle,
            urlListFromClipboard = urlList,
            isFileAvailable = isFileAvailable,
            onSave = { newTitle -> onSave(newTitle) }, // Pass onSave as a lambda function
            onCancel = {
                view.slightHapticFeedback()
                onDismissRequest()
            }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DrawerPreview() {
    DownloaderTheme {
        VideoEditDrawerImpl(
            sheetState = ModalBottomSheetState(
                ModalBottomSheetValue.Expanded,
                density = LocalDensity.current,
            ),
            urlListFromClipboard = listOf("https://www.example.com"),
            onSave = { }, // Provide a lambda for onSave
            onCancel = { }, // Provide a lambda for onCancel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditDrawerImpl(
    sheetState: ModalBottomSheetState =
        ModalBottomSheetState(ModalBottomSheetValue.Hidden, density = LocalDensity.current),
    title: String = stringResource(id = R.string.video_title_sample_text),
    urlListFromClipboard: List<String>,
    isFileAvailable: Boolean = true,
    onSave: (String) -> Unit = {}, // Modify onSave to accept a String argument
    onCancel: () -> Unit = {},
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var newTitle by remember { mutableStateOf(title) }

    DownloaderModalBottomSheetM2(
        sheetState = sheetState,
        contentPadding = PaddingValues(horizontal = 20.dp),
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text(stringResource(R.string.video_url)) },
                    maxLines = 3,
                    trailingIcon = {
                        if (newTitle.isNotEmpty()) {
                            ClearButton { newTitle = "" }
                        }
                    },
                )

                LazyRow(
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp),
                ) {
                    if (urlListFromClipboard.isNotEmpty()) {
                        item(key = "paste url") {
                            SuggestionChip(
                                modifier = Modifier.animateItem(),
                                onClick = { newTitle = urlListFromClipboard.first() },
                                label = { Text(stringResource(R.string.paste)) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentPaste,
                                        contentDescription = null,
                                        modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                                    )
                                },
                            )
                        }
                    }
                }
            }

            Row(
                modifier =
                Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 24.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButtonWithIcon(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    onClick = onCancel,
                    icon = Icons.Outlined.Cancel,
                    text = stringResource(R.string.cancel),
                )

                FilledTonalButtonWithIcon(
                    onClick = { onSave(newTitle) }, // Pass newTitle to onSave when the button is clicked
                    icon = Icons.Outlined.Save,
                    text = stringResource(R.string.save),
                )
            }
        },
    )
}
