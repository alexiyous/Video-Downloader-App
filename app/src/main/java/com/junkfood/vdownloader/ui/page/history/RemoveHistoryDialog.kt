package com.junkfood.vdownloader.ui.page.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.database.objects.DownloadedVideoInfo
import com.junkfood.vdownloader.ui.component.DownloaderDialog

@Composable
fun RemoveHistoryDialog(
    deleteFile: Boolean = false,
    onDeleteFileToggled: (Boolean) -> Unit = {},
    info: DownloadedVideoInfo,
    onRemoveConfirm: (Boolean) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Set dialog width based on the screen width (for mobile, tablet, or other)
    val dialogWidth =
        when {
            screenWidth < 600.dp -> screenWidth - 20.dp // Mobile devices (less than 600dp)
            screenWidth in 600.dp..1200.dp ->
                screenWidth - 60.dp // Tablets (between 600dp and 1200dp)
            else -> screenWidth - 100.dp // Larger devices (above 1200dp)
        }
    DownloaderDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                modifier = Modifier.padding(top = 15.dp, bottom = 25.dp),
                text = stringResource(R.string.delete_info),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
            )
        },
        icon = {
            Box(
                contentAlignment = Alignment.TopEnd, // Aligns the icon to the top-right
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.dismiss),
                    modifier =
                        Modifier // Reduced padding for the close icon
                            .clickable { onDismissRequest() },
                )
            }
        },
        text = {
            Column(
                modifier = Modifier // Reduced padding for the text
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 35.dp),
                    textAlign = TextAlign.Center, // Ensure the text aligns centrally within its container
                    text = stringResource(R.string.delete_dlg_content),
                    fontSize = 16.sp // Set the font size to 18sp
                )
            }
        },
        confirmButton = {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(60.dp) // Adjust the height as needed
                        .clip(RoundedCornerShape(30.dp)) // Apply border radius
                        .background(Color(0xFF00B06D)) // Set background color
                        .clickable {
                            onRemoveConfirm(deleteFile) // Call the function when clicking "Clear"
                            onDismissRequest() // Close the dialog after confirming
                        },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // Text on the right
                    Text(
                        text = stringResource(R.string.clear_bg),
                        fontSize = 22.sp,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color =
                            MaterialTheme.colorScheme
                                .onPrimary, // Ensure text color contrasts with background
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun RemoveHistoryDialogPreview() {
    RemoveHistoryDialog(
        deleteFile = true,
        onDeleteFileToggled = {},
        info =
            DownloadedVideoInfo(
                id = 1,
                videoTitle = "Sample Video",
                videoAuthor = "Sample Author",
                videoUrl = "https://example.com/video.mp4",
                thumbnailUrl = "https://example.com/thumbnail.jpg",
                videoPath = "/storage/emulated/0/Download/sample_video.mp4",
                extractor = "YouTube",
            ),
        onRemoveConfirm = { deleteFile ->
            // Mock confirmation action for preview
            println("Delete file confirmed: $deleteFile")
        },
        onDismissRequest = {
            // Mock dismiss action for preview
            println("Dialog dismissed")
        },
    )
}
