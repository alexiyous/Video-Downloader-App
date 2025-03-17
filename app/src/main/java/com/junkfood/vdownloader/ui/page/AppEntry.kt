package com.junkfood.vdownloader.ui.page

import DownloaderPage
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.junkfood.vdownloader.App
import com.junkfood.vdownloader.App.Companion.context
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.common.LocalWindowWidthState
import com.junkfood.vdownloader.ui.common.Route
import com.junkfood.vdownloader.ui.common.animatedComposable
import com.junkfood.vdownloader.ui.common.animatedComposableVariant
import com.junkfood.vdownloader.ui.common.arg
import com.junkfood.vdownloader.ui.common.id
import com.junkfood.vdownloader.ui.common.slideInVerticallyComposable
import com.junkfood.vdownloader.ui.page.command.TaskListPage
import com.junkfood.vdownloader.ui.page.command.TaskLogPage
import com.junkfood.vdownloader.ui.page.downloadv2.DownloadPageV2
import com.junkfood.vdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.vdownloader.ui.page.help.MainHelpPage
import com.junkfood.vdownloader.ui.page.history.HistoryPage
import com.junkfood.vdownloader.ui.page.settings.appearance.LanguagePage
import com.junkfood.vdownloader.ui.page.settings.network.CookiesViewModel
import com.junkfood.vdownloader.ui.page.tab.TabListPage
import com.junkfood.vdownloader.ui.page.videolist.VideoListPage
import com.junkfood.vdownloader.ui.page.whatsapp.WhatsAppMedia
import com.junkfood.vdownloader.ui.page.whatsapp.WhatsappImagePage
import com.junkfood.vdownloader.ui.page.whatsapp.WhatsappImageView
import com.junkfood.vdownloader.ui.page.whatsapp.WhatsappSavePage
import com.junkfood.vdownloader.ui.page.whatsapp.WhatsappVideoPage
import com.junkfood.vdownloader.ui.page.whatsapp.WhatsappVideoViewer
import com.junkfood.vdownloader.ui.theme.bottomNavigationDark
import com.junkfood.vdownloader.util.PreferenceUtil
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.text.set

private const val TAG = "HomeEntry"


@Composable
fun MainScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    dialogViewModel: DownloadDialogViewModel,
    drawerState: DrawerState,
    cookiesViewModel: CookiesViewModel,
    currentRoute: String?,
    showPermissionDialog: MutableState<Boolean>,
    onRequestPermission: () -> Unit,
    onCancelPermission: () -> Unit,
    isStoragePermissionGranted: () -> Boolean, // Add the function parameter
    startDestination: String,
    view: View, // Pass the startDestination dynamically
) {
    val scope = rememberCoroutineScope()

    val bottomNavRoutes = listOf(Route.HOME, Route.PROGRESS, Route.DOWNLOADS)
    val bottomWhatsappNavRoutes =
        listOf(Route.WHATSAPP_IMAGE, Route.WHATSAPP_VIDEO, Route.WHATSAPP_SAVE)
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value

    // State to track whether the menu is open

    var tabCount by remember { mutableStateOf(1) } // Track tab count

    LaunchedEffect(currentRoute) {
        if (currentRoute == Route.HOME) {
            showPermissionDialog.value = !isStoragePermissionGranted()
        }
    }

    val onSocialEvent: (String) -> Unit = { strType ->
        if (strType == "Whatsapp") {
            navController.navigate(Route.WHATSAPP_IMAGE)
        } else {}
    }

    val onRightMenuAction: (Int) -> Unit = { iType ->
        if (iType == 0) // Open new tab
        {
            //            Toast.makeText(context, "Show open new tab ", Toast.LENGTH_SHORT).show()
            navController.navigate("${Route.TAB_LIST}/$tabCount") // Pass tab count
        } else if (iType == 1) // Open
         navController.navigate(Route.History)
        else if (iType == 2) navController.navigate(Route.Help)
    }

    var expanded by remember { mutableStateOf(false) }

    // Entire screen layout
    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes && !showPermissionDialog.value) {
                BottomNavigationBar(navController = navController, strType = "main")
            } else if (currentRoute in bottomWhatsappNavRoutes && !showPermissionDialog.value) {
                BottomNavigationBar(navController = navController, strType = "whatsapp")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Remove the extra space
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = startDestination) {
                animatedComposable(Route.HOME) {
                    DownloaderPage(
                        dialogViewModel = dialogViewModel,
                        onMenuOpen = { scope.launch { drawerState.open() } },
                        onActionMenu = { expanded = true }, // Pass the callback to
                        onShowHelpPage = { navController.navigate(Route.Help) },
                        onSocialEvent = onSocialEvent,
                    )
                }
                animatedComposable(Route.TOS) {
                    TosPage(
                        onContinue = {
                            PreferenceUtil.setVisited(context)
                            navController.navigate(Route.LANGUAGES) {
                                popUpTo(Route.TOS) { inclusive = true }
                            }
                        }
                    )
                }
                animatedComposable(Route.LANGUAGES) {
                    LanguagePage { navController.navigate(Route.HOME) }
                }

                animatedComposable(Route.PROGRESS) {
                    DownloadPageV2(
                        dialogViewModel = dialogViewModel,
                        onMenuOpen = {
                            view.slightHapticFeedback()
                            scope.launch { drawerState.open() }
                        },
                        onActionMenu = { expanded = true }, // Pass the callback to
                    )
                }
                animatedComposable(Route.DOWNLOADS) {
                    VideoListPage(
                        onMenuOpen = { scope.launch { drawerState.open() } },
                        onActionMenu = { expanded = true }, // Pass the callback to
                    )
                }
                animatedComposable(Route.History) {
                    HistoryPage(
                        onNavigateBack = {
                            navController.popBackStack() // Navigates back to the previous route
                        },
                        onMenuOpen = {
                            onRightMenuAction(0)
                        }
                    )
                }
                animatedComposable("${Route.TAB_LIST}/{tabNumber}") { backStackEntry ->
                    val tabNumber =
                        backStackEntry.arguments?.getString("tabNumber")?.toIntOrNull() ?: 1

                    TabListPage(
                        tabNumber = tabNumber,
                        onNavigateBack = {
                            if (tabCount > 1) tabCount-- // Decrease count only if more than 1
                            navController.popBackStack() // Navigate back
                        },
                        onAction = {
                            tabCount++ // Increase tab count
                            navController.navigate("${Route.TAB_LIST}/$tabCount") // Na
                        },
                    )
                }
                animatedComposable(Route.WHATSAPP_IMAGE) {
                    WhatsappImagePage(
                        onNavigateBack = onNavigateBack,
                        onImageSelected = { imageList, selectedIndex ->
                            navController.navigate(Route.WHATSAPP_VIEW + "/$selectedIndex") {
                                currentBackStackEntry?.savedStateHandle?.set("images", imageList)
                            }
                        },
                    )
                }
                animatedComposable(Route.WHATSAPP_VIDEO) {
                    WhatsappVideoPage(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToVideoViewer = { videos, initialIndex ->
                            // Set the data before navigating
                            navController.currentBackStackEntry?.savedStateHandle?.set("videos", videos)
                            navController.navigate(Route.WHATSAPP_VIDEO_VIEWER + "/$initialIndex")
                        }
                    )
                }

                animatedComposable(
                    "${Route.WHATSAPP_VIDEO_VIEWER}/{initialIndex}",
                    arguments = listOf(navArgument("initialIndex") { type = NavType.IntType }),
                ) {
                    val initialIndex = it.arguments?.getInt("initialIndex") ?: 0
                    val videos = navController.previousBackStackEntry?.savedStateHandle?.get<List<WhatsAppMedia>>("videos") ?: emptyList()

                    Log.d(TAG, "Videos: $videos")

                    if (videos == null) {
                        // If videos are null, pop back
                        LaunchedEffect(Unit) {
                            navController.popBackStack()
                        }
                        return@animatedComposable
                    }

                    WhatsappVideoViewer(
                        videos = videos,
                        initialVideoIndex = initialIndex,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                animatedComposable(Route.WHATSAPP_SAVE) {
                    WhatsappSavePage(onNavigateBack = onNavigateBack)
                }
                animatedComposable(
                    "${Route.WHATSAPP_VIEW}/{selectedIndex}",
                    arguments = listOf(navArgument("selectedIndex") { type = NavType.IntType }),
                    ) { backStackEntry ->
                    val selectedIndex = backStackEntry.arguments?.getInt("selectedIndex") ?: 0
                    // Retrieve the image list from SavedStateHandle
                    val imageList =  navController.previousBackStackEntry?.savedStateHandle?.get<List<WhatsAppMedia>>("images") ?: emptyList()

                    WhatsappImageView(
                        images = imageList,
                        initialImageIndex = selectedIndex,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                animatedComposable(Route.Help) {
                    MainHelpPage(onDismiss = { navController.navigate(Route.HOME) })
                }
                animatedComposableVariant(Route.TASK_LIST) {
                    TaskListPage(
                        onNavigateBack = onNavigateBack,
                        onNavigateToDetail = { navController.navigate(Route.TASK_LOG id it) },
                    )
                }
                slideInVerticallyComposable(
                    Route.TASK_LOG arg Route.TASK_HASHCODE,
                    arguments = listOf(navArgument(Route.TASK_HASHCODE) { type = NavType.IntType }),
                ) {
                    TaskLogPage(
                        onNavigateBack = onNavigateBack,
                        taskHashCode = it.arguments?.getInt(Route.TASK_HASHCODE) ?: -1,
                    )
                }

                settingsGraph(
                    onNavigateBack = onNavigateBack,
                    onNavigateTo = { route ->
                        navController.navigate(route = route) { launchSingleTop = true }
                    },
                    cookiesViewModel = cookiesViewModel,
                )
            }
        }
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
                DialogProperties(usePlatformDefaultWidth = false), // Ensures full-screen coverage
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
                        Modifier.align(Alignment.TopEnd)
                            .offset(x = -10.dp, y = 50.dp) // Moves menu to top-right
                            .width(200.dp)
                            .background(backgroundColor, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {} // Prevents accidental dismiss
                    // when clicking inside the menu
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.height(60.dp), // Increased height
                        onClick = {
                            onRightMenuAction(0)
                            expanded = false
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
                                    painter = painterResource(id = R.drawable.ico_open_new_tab),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = stringResource(id = R.string.open_new_tab))
                            }
                        },
                    )

                    DropdownMenuItem(
                        modifier = Modifier.height(60.dp), // Increased height
                        onClick = {
                            onRightMenuAction(1)
                            expanded = false
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
                                    painter = painterResource(id = R.drawable.ico_history),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = stringResource(id = R.string.history))
                            }
                        },
                    )
                }
            }
        }
    }

    if (showPermissionDialog.value) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        // Consume all touch events
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    }
                    .zIndex(1f)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).zIndex(2f),
                factory = { context ->
                    LayoutInflater.from(context)
                        .inflate(R.layout.dialog_permission_custom, null)
                        .apply {
                            val btnAllow = findViewById<Button>(R.id.btnAllow)
                            val btnCancel = findViewById<ImageView>(R.id.btnCancel)

                            btnAllow.setOnClickListener { onRequestPermission() }
                            btnCancel.setOnClickListener { onCancelPermission() }
                        }
                },
            )
        }
    }
}

@Composable
fun AppEntry(
    dialogViewModel: DownloadDialogViewModel,
    showPermissionDialog: MutableState<Boolean>,
    onRequestPermission: () -> Unit,
    onCancelPermission: () -> Unit,
    isStoragePermissionGranted: () -> Boolean,
) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val view = LocalView.current
    val windowWidth = LocalWindowWidthState.current
    val sheetState by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()
    val cookiesViewModel: CookiesViewModel = koinViewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val versionReport = App.packageInfo.versionName.toString()
    val appName = stringResource(R.string.app_name)
    val scope = rememberCoroutineScope()

    val onNavigateBack: () -> Unit = {
        with(navController) {
            if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                popBackStack()
            }
        }
    }

    val startDestination = remember {
        if (PreferenceUtil.hasVisited(context)) Route.HOME else Route.TOS
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavigationDrawer(
            windowWidth = windowWidth,
            drawerState = drawerState,
            currentRoute = currentRoute,
            currentTopDestination = startDestination,
            showQuickSettings = false,
            gesturesEnabled = true,
            onDismissRequest = { drawerState.close() },
            onNavigateToRoute = {
                if (currentRoute != it) {
                    navController.navigate(it) {
                        launchSingleTop = true
                        popUpTo(route = Route.HOME)
                    }
                }
            },
        ) {
            MainScreen(
                navController = navController,
                onNavigateBack = onNavigateBack,
                dialogViewModel = dialogViewModel,
                drawerState = drawerState,
                cookiesViewModel = cookiesViewModel,
                currentRoute = currentRoute,
                showPermissionDialog = showPermissionDialog,
                onRequestPermission = onRequestPermission,
                onCancelPermission = onCancelPermission,
                isStoragePermissionGranted = isStoragePermissionGranted,
                startDestination = startDestination,
                view = view,
            )

            AppUpdater()
            YtdlpUpdater()
        }
    }
}

fun NavGraphBuilder.settingsGraph(
    onNavigateBack: () -> Unit,
    onNavigateTo: (route: String) -> Unit,
    cookiesViewModel: CookiesViewModel,
) {
    navigation(startDestination = Route.SETTINGS_PAGE, route = Route.SETTINGS) {
        animatedComposable(Route.LANGUAGES) { LanguagePage { onNavigateBack() } }
    }
}
