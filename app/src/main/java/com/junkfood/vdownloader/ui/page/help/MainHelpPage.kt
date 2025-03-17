package com.junkfood.vdownloader.ui.page.help

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.theme.DownloaderTheme

data class GalleryItem(
    val number: String, // Unique number or identifier
    val title: String, // Title of the main image
    val mainImage: Int, // Resource ID for the main image
    val followImage: Int, // Resource ID for the follow image (bottom image)
)

@Composable
fun ExplainContent(modifier: Modifier, items: List<GalleryItem>, onDismiss: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }

    Box(modifier = modifier) {
        // Content of the page
        Column(
            modifier = Modifier.fillMaxSize().padding() // Apply inner padding for scaffold content
        ) {
            // First section (1 part)
            Box(
                modifier = Modifier.fillMaxWidth().weight(2f),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(), // Optional: Fill available space
                    horizontalAlignment =
                        Alignment.CenterHorizontally, // Centers children horizontally
                    verticalArrangement = Arrangement.Center, // Centers children vertically
                ) {
                    Box(
                        modifier =
                            Modifier.size(50.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary, // Background color
                                    shape = CircleShape, // Border radius
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = items[currentIndex].number,
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 25.sp // Set your desired font size here
                                ),
                            color = Color.White, // Text color for contrast
                        )
                    }
                    Text(
                        modifier = Modifier.padding(20.dp),
                        text = items[currentIndex].title,
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        color = MaterialTheme.colorScheme.onBackground, // Text color for contrast
                    )
                }
            }

            // Second section (2 parts)
            Box(
                modifier = Modifier.fillMaxWidth().weight(5f).padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Gallery of images
                Column(
                    modifier = Modifier.fillMaxSize(), // Fill available space
                    horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
                    verticalArrangement = Arrangement.Center, // Space items vertically
                ) {
                    // Main image
                    Image(
                        modifier =
                            Modifier.fillMaxWidth()
                                .weight(1f) // Take most of the space
                                .padding(bottom = 15.dp),
                        painter =
                            painterResource(
                                id = items[currentIndex].mainImage
                            ), // Dynamic image resource
                        contentDescription = "Main image",
                    )

                    // Bottom image with fixed height
                    Image(
                        modifier =
                            Modifier.fillMaxWidth() // Stretch horizontally
                                .height(15.dp) // Fixed height for the bottom image
                                .padding(top = 8.dp),
                        painter =
                            painterResource(
                                id = items[currentIndex].followImage
                            ), // Replace with your smaller image resource
                        contentDescription = "Gallery image",
                    )
                }
            }

            // Third section (1 part)
            Box(
                modifier = Modifier.fillMaxWidth().weight(2f),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    modifier =
                        Modifier.fillMaxWidth().padding(20.dp).clip(RoundedCornerShape(100.dp)),
                    onClick = {
                        if (currentIndex == items.size - 1) {
                            onDismiss() // Call onDismiss when it's the last item
                        } else {
                            currentIndex = (currentIndex + 1) % items.size
                        }
                    },
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth().height(60.dp), // Adjust the height as needed
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {

                            // Text on the right
                            Text(
                                text = stringResource(R.string.next),
                                fontSize = 22.sp,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHelpPage(onDismiss: () -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(
                initialHeightOffset = rememberTopAppBarState().heightOffsetLimit,
                initialContentOffset = 0f,
            ),
            canScroll = { true },
        )

    val hostState = remember { SnackbarHostState() }

    val items =
        listOf(
            GalleryItem(
                number = "1",
                title = stringResource(R.string.go_to_website),
                mainImage = R.drawable.img_help_website,
                followImage = R.drawable.img_slide_1,
            ),
            GalleryItem(
                number = "2",
                title = stringResource(R.string.play_the_video),
                mainImage = R.drawable.img_help_video,
                followImage = R.drawable.img_slide_2,
            ),
            GalleryItem(
                number = "3",
                title = stringResource(R.string.click_download),
                mainImage = R.drawable.img_help_download,
                followImage = R.drawable.img_slide_3,
            ),
        )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {},
                //                navigationIcon = { BackButton { onNavigateBack() } },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically // Ensures vertical centering
                    ) {
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(modifier = Modifier.size(30.dp), onClick = { onDismiss() }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.dismiss),
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
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
        ExplainContent(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            items = items,
            onDismiss = onDismiss,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun MainHelpPagePreview() {
    DownloaderTheme {
        // Create a mock instance of DownloadDialogViewModel
        MainHelpPage(onDismiss = {})
    }
}
