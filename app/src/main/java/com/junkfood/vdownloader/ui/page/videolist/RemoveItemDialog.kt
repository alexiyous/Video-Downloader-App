package com.junkfood.vdownloader.ui.page.videolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.database.objects.DownloadedVideoInfo
import com.junkfood.vdownloader.ui.component.CheckBoxItem
import com.junkfood.vdownloader.ui.component.DownloaderDialog

@Composable
fun RemoveItemDialog(
    deleteFile: Boolean = false,
    onDeleteFileToggled: (Boolean) -> Unit = {},
    info: DownloadedVideoInfo,
    onRemoveConfirm: (Boolean) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    DownloaderDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.delete_info)) },
        text = {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    text = stringResource(R.string.delete_info_msg).format(info.videoTitle),
                )
                CheckBoxItem(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = stringResource(R.string.delete_file),
                    checked = deleteFile,
                    onValueChange = onDeleteFileToggled,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    onRemoveConfirm(deleteFile)
                }
            ) {
                Text(text = stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(text = stringResource(R.string.dismiss)) }
        },
    )
}
