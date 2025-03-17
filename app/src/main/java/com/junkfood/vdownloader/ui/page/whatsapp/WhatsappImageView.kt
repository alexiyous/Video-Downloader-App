package com.junkfood.vdownloader.ui.page.whatsapp

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.component.BackButton
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsappImageView(
    images: List<WhatsAppMedia>,
    initialImageIndex: Int,
    onNavigateBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(
            initialHeightOffset = rememberTopAppBarState().heightOffsetLimit,
            initialContentOffset = 0f,
        ),
        canScroll = { true },
    )

    var currentImageIndex by remember { mutableStateOf(initialImageIndex) }

    val pagerState = rememberPagerState(
        initialPage = initialImageIndex,
        pageCount = { images.size }
    )

    val scope = rememberCoroutineScope()

    // Store previous page to detect page changes
    var previousPage by remember { mutableStateOf(pagerState.currentPage) }

    // Create zoom state with default specs
    val zoomableState = rememberZoomableState(
        zoomSpec = ZoomSpec(5f, 1f),
    )

    // Handle back press
    BackHandler {
        if (!zoomableState.isZoomedOut) {
            scope.launch {
                zoomableState.resetZoom()
            }
        } else {
            onNavigateBack()
        }
    }

    // Track page changes and update zoom state
    LaunchedEffect(pagerState.currentPage) {
        if (previousPage != pagerState.currentPage) {
            zoomableState.resetZoom()
            previousPage = pagerState.currentPage
        }
        currentImageIndex = pagerState.currentPage
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.whatsapp),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    Row {
                        IconButton(onClick = { /* Handle share */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ico_share_app),
                                contentDescription = "Share Icon",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        IconButton(onClick = { /* Handle delete */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ico_delete),
                                contentDescription = "Delete Icon",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = zoomableState.isZoomedOut,
            ) { page ->
                ZoomableImagePage(
                    imageUri = images[page].uri,
                    zoomableState = zoomableState,
                    isCurrentPage = page == pagerState.currentPage
                )
            }
        }
    }
}

@Composable
private fun ZoomableImagePage(
    imageUri: String,
    zoomableState: ZoomableState,
    isCurrentPage: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .size(coil.size.Size.ORIGINAL)
                .build()
        )

        // Update zoom state when image loads and is current page
        LaunchedEffect(painter.state, isCurrentPage) {
            if (painter.state is AsyncImagePainter.State.Success && isCurrentPage) {
                val intrinsicSize = painter.intrinsicSize
                if (!intrinsicSize.isUnspecified) {
                    zoomableState.resetZoom()
                }
            }
        }

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    state = zoomableState,
                    enabled = isCurrentPage,
                    onDoubleClick = DoubleClickToZoomListener.cycle(2.5f)
                )
        )
    }
}

private val ZoomableState.isZoomedOut
    get() = (zoomFraction ?: 0f) <= 0.0001f




@Preview(showBackground = true)
@Composable
fun WhatsappImageViewPreview() {
    val onNavigateBack = { /* Handle back navigation */ }
    WhatsappImageView(
        images = TODO(),
        initialImageIndex = TODO(),
        onNavigateBack = TODO()
    )
}
