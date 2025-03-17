package com.junkfood.vdownloader.ui.page.whatsapp

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.component.BackButton


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsappVideoViewer(
    videos: List<WhatsAppMedia>,
    initialVideoIndex: Int,
    onNavigateBack: () -> Unit
) {
    var isLandscape by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = LocalContext.current as ComponentActivity
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val player = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = false
            }
    }

    // Function to handle orientation change
    fun setOrientation(landscape: Boolean) {
        activity.requestedOrientation = if (landscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        isLandscape = landscape
    }

    // Initialize player with first video without playing
    LaunchedEffect(Unit) {
        if (videos.isNotEmpty()) {
            player.apply {
                setMediaItem(MediaItem.fromUri(videos[initialVideoIndex].uri))
                prepare()
                stop()
            }
        }
    }


    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(
            initialHeightOffset = rememberTopAppBarState().heightOffsetLimit,
            initialContentOffset = 0f,
        ),
        canScroll = { true },
    )

    var currentVideoIndex by remember { mutableStateOf(initialVideoIndex) }

    val pagerState = rememberPagerState(
        initialPage = initialVideoIndex,
        pageCount = { videos.size }
    )

    // Track page changes and update player
    LaunchedEffect(pagerState.currentPage) {
        currentVideoIndex = pagerState.currentPage
        // Update player with new media
        if (videos.isEmpty()) return@LaunchedEffect

        player.apply {
            stop() // Stop current playback
            clearMediaItems() // Clear previous items
            setMediaItem(MediaItem.fromUri(videos[currentVideoIndex].uri))
            prepare()
            pause()
            playWhenReady = false // Ensure it doesn't auto-play after preparing
        }
    }

    // Handle lifecycle
    DisposableEffect(lifecycle) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    player.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    player.play()
                }
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    BackHandler {
        setOrientation(false)
        player.release()
        onNavigateBack()
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
                navigationIcon = { BackButton {
                    player.release()
                    onNavigateBack()
                } },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    Row {
                        // Orientation toggle button
                        IconButton(
                            onClick = { setOrientation(!isLandscape) }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isLandscape) {
                                        R.drawable.ic_screen_rotation // Replace with your portrait icon
                                    } else {
                                        R.drawable.ic_screen_rotation // Replace with your landscape icon
                                    }
                                ),
                                contentDescription = if (isLandscape) "Switch to Portrait" else "Switch to Landscape",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(22.dp),
                            )
                        }

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
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (page == currentVideoIndex) {
                        AndroidView(
                            factory = { context ->
                                PlayerView(context).apply {
                                    this.player = player
                                    useController = true
                                    resizeMode = if (isLandscape) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    } else {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                    setShowNextButton(false)
                                    setShowPreviousButton(false)
                                }
                            },
                            // Update the view when orientation changes
                            update = { view ->
                                view.resizeMode = if (isLandscape) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FILL
                                } else {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Show thumbnail for non-current pages
                        var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

                        LaunchedEffect(Unit) {
                            thumbnail = createVideoThumb(context, Uri.parse(videos[page].uri))
                        }

                        thumbnail?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}