package com.junkfood.vdownloader.ui.page.whatsapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.component.BackButton
import com.junkfood.vdownloader.util.TestVideoInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsappSavePage(onNavigateBack: () -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(
                initialHeightOffset = rememberTopAppBarState().heightOffsetLimit,
                initialContentOffset = 0f,
            ),
            canScroll = { true },
        )

    val videoList =
        listOf(
            TestVideoInfo(
                1,
                "Video 1",
                "https://www.ezyodr.com/uploads/lands/land_1732819280.png",
                "https://www.ezyodr.com/uploads/lands/land_1732819280.png",
            ),
            TestVideoInfo(
                2,
                "Video 2",
                "https://www.ezyodr.com/uploads/lands/land_1734126657.png",
                "https://www.ezyodr.com/uploads/lands/land_1734126657.png",
            ),
            TestVideoInfo(
                3,
                "Video 3",
                "https://www.ezyodr.com/uploads/lands/land_1734126582.png",
                "https://www.ezyodr.com/uploads/lands/land_1734126582.png",
            ),
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.whatsapp)) },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
                colors =
                    TopAppBarDefaults.largeTopAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                actions = {
                    IconButton(onClick = { /* Handle delete */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ico_whatsapp_help),
                            contentDescription = "Delete Icon",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp),
                        )
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(videoList) { video ->
                            Image(
                                painter = rememberAsyncImagePainter(video.thumbnailUrl),
                                contentDescription = "Video Thumbnail",
                                contentScale = ContentScale.Crop, // Ensures image fills the square
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .aspectRatio(1f) // Ensures a square shape
                                        .clickable {
                                            // Call the callback to pass the image URL
                                            //
                                            // onImageSelected(video.imageUrl)
                                        },
                            )
                        }
                    }
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun WhatsappSavePagePreview() {
    val onNavigateBack = { /* Handle back navigation */ }
    WhatsappSavePage(onNavigateBack = onNavigateBack)
}
