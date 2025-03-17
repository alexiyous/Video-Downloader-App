package com.junkfood.vdownloader.ui.page

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.common.LocalWindowWidthState
import com.junkfood.vdownloader.ui.common.Route
import com.junkfood.vdownloader.ui.component.PreferenceThemeSettingItem
import com.junkfood.vdownloader.ui.page.downloadv2.DownloadPageImplV2
import com.junkfood.vdownloader.util.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.junkfood.vdownloader.util.DarkThemePreference.Companion.OFF
import com.junkfood.vdownloader.util.DarkThemePreference.Companion.ON
import com.junkfood.vdownloader.util.PreferenceUtil
import com.junkfood.vdownloader.util.toDisplayName
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    windowWidth: WindowWidthSizeClass = LocalWindowWidthState.current,
    currentRoute: String? = null,
    currentTopDestination: String? = null,
    showQuickSettings: Boolean = true,
    onNavigateToRoute: (String) -> Unit,
    onDismissRequest: suspend () -> Unit,
    gesturesEnabled: Boolean = true,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidth =
        when (windowWidth) {
            WindowWidthSizeClass.Compact,
            WindowWidthSizeClass.Medium -> screenWidth * 3 / 4 // For phone, 3/4 of the screen width
            WindowWidthSizeClass.Expanded ->
                screenWidth * 2 / 5 // For tablet, 5/2 of the screen width
            else ->
                screenWidth * 1 /
                    4 // Default case, using phone width if the windowWidth is not recognized
        }

    // Add BackHandler to handle back button press
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    when (windowWidth) {
        WindowWidthSizeClass.Compact,
        WindowWidthSizeClass.Medium -> {
            ModalNavigationDrawer(
                gesturesEnabled = gesturesEnabled,
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = modifier.width(drawerWidth).padding(0.dp),
                        windowInsets = WindowInsets(0), // Remove default insets
                    ) {
                        NavigationDrawerSheetContent(
                            modifier = Modifier,
                            currentRoute = currentRoute,
                            showQuickSettings = showQuickSettings,
                            onNavigateToRoute = onNavigateToRoute,
                            onDismissRequest = onDismissRequest,
                            footer = footer,
                        )
                    }
                },
                content = content,
            )
        }
        WindowWidthSizeClass.Expanded -> {
            ModalNavigationDrawer(
                gesturesEnabled = drawerState.isOpen,
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerState = drawerState,
                        modifier = modifier.width(drawerWidth),
                    ) {
                        NavigationDrawerSheetContent(
                            modifier = Modifier,
                            currentRoute = currentRoute,
                            showQuickSettings = showQuickSettings,
                            onNavigateToRoute = onNavigateToRoute,
                            onDismissRequest = onDismissRequest,
                            footer = footer,
                        )
                    }
                },
            ) {
                Row {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.zIndex(1f),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight().systemBarsPadding().width(92.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(Modifier.height(8.dp))
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            ) {
                                Icon(Icons.Outlined.Menu, null)
                            }
                            Spacer(Modifier.weight(1f))
                            NavigationRailContent(
                                modifier = Modifier,
                                currentTopDestination = currentTopDestination,
                                onNavigateToRoute = onNavigateToRoute,
                            )
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    content()
                }
            }
        }
    }
}

@Composable
fun NavigationDrawerSheetContent(
    modifier: Modifier = Modifier,
    currentRoute: String? = null,
    showQuickSettings: Boolean = true,
    onNavigateToRoute: (String) -> Unit,
    onDismissRequest: suspend () -> Unit,
    footer: @Composable (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    val darkThemePreference = LocalDarkTheme.current

    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        DrawerHeader()
        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            Column(modifier = Modifier.padding(16.dp)) {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.down_progress)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ico_download_progress),
                            modifier = Modifier.size(25.dp),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        scope.launch {
                            onDismissRequest()
                            onNavigateToRoute(Route.PROGRESS)
                        }
                    },
                    selected = currentRoute == Route.PROGRESS,
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.downloads_history)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ico_video_download),
                            modifier = Modifier.size(25.dp),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        scope.launch {
                            onDismissRequest()
                            onNavigateToRoute(Route.DOWNLOADS)
                        }
                    },
                    selected = currentRoute == Route.DOWNLOADS,
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.sel_language)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ico_select_language),
                            modifier = Modifier.size(25.dp),
                            contentDescription = Locale.getDefault().toDisplayName(),
                        )
                    },
                    onClick = {
                        scope.launch {
                            onDismissRequest()
                            onNavigateToRoute(Route.LANGUAGES)
                        }
                    },
                    selected = false,
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.app_themes)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ico_app_themes),
                            modifier = Modifier.size(25.dp),
                            contentDescription = null,
                        )
                    },
                    onClick = {},
                    selected = false,
                    shape = RectangleShape,
                )
                val darkThemePreference = LocalDarkTheme.current
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                    PreferenceThemeSettingItem(
                        text = stringResource(R.string.system_theme),
                        selected = darkThemePreference.darkThemeValue == FOLLOW_SYSTEM,
                    ) {
                        PreferenceUtil.modifyDarkThemePreference(FOLLOW_SYSTEM)
                    }

                    PreferenceThemeSettingItem(
                        text = stringResource(R.string.dark_mode),
                        selected = darkThemePreference.darkThemeValue == ON,
                    ) {
                        PreferenceUtil.modifyDarkThemePreference(ON)
                    }

                    PreferenceThemeSettingItem(
                        text = stringResource(R.string.right_mode),
                        selected = darkThemePreference.darkThemeValue == OFF,
                    ) {
                        PreferenceUtil.modifyDarkThemePreference(OFF)
                    }
                }

                //                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.share_app)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ico_share_app),
                            contentDescription = Locale.getDefault().toDisplayName(),
                            modifier = Modifier.size(25.dp),
                        )
                    },
                    onClick = {

                    },
                    selected = false,
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.rate_us)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ico_rate_us),
                            modifier = Modifier.size(25.dp),
                            contentDescription = Locale.getDefault().toDisplayName(),
                        )
                    },
                    onClick = { showDialog = true },
                    selected = false,
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.privacy_policy)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ico_privacy_policy),
                            modifier = Modifier.size(25.dp),
                            contentDescription = Locale.getDefault().toDisplayName(),
                        )
                    },
                    onClick = {

                    },
                    selected = false,
                    //                    selected = currentRoute == Route.COOKIE_PROFILE,
                )

                if (showDialog) {
                    RateUsDialog(
                        onDismiss = {
                            showDialog = false // Close the dialog when dismissed
                        },
                        onConfirm = {
                            // Handle confirmation logic (like saving rating or submitting feedback)
                            showDialog = false // Close the dialog after confirmation
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationRailItemVariant(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit),
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.large)
                .background(
                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                    else Color.Transparent
                )
                .selectable(selected = selected, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides
                if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            icon()
        }
    }
}

@Composable
fun NavigationRailContent(
    modifier: Modifier = Modifier,
    currentTopDestination: String? = null,
    onNavigateToRoute: (String) -> Unit,
) {
    Column(
        modifier = modifier.selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val scope = rememberCoroutineScope()
        NavigationRailItemVariant(
            icon = {
                Image(
                    painter =
                        painterResource(
                            id =
                                if (currentTopDestination == Route.HOME) R.drawable.ico_home_active
                                else R.drawable.ico_home_inactive
                        ),
                    contentDescription = stringResource(R.string.download_progress),
                    modifier = Modifier.size(24.dp), // Adjust the size as needed
                )
            },
            modifier = Modifier,
            selected = currentTopDestination == Route.HOME,
            onClick = { onNavigateToRoute(Route.HOME) },
        )

        NavigationRailItemVariant(
            icon = {
                Image(
                    painter =
                        painterResource(
                            id =
                                if (currentTopDestination == Route.PROGRESS)
                                    R.drawable.ico_progress_active
                                else R.drawable.ico_progress_inactive
                        ),
                    contentDescription = stringResource(R.string.custom_command),
                    modifier = Modifier.size(24.dp), // Adjust the size as needed
                )
            },
            modifier = Modifier,
            selected = currentTopDestination == Route.PROGRESS,
            onClick = { onNavigateToRoute(Route.PROGRESS) },
        )

        NavigationRailItemVariant(
            icon = {
                Image(
                    painter =
                        painterResource(
                            id =
                                if (currentTopDestination == Route.DOWNLOADS)
                                    R.drawable.ico_download_active
                                else R.drawable.ico_download_inactive
                        ),
                    contentDescription = stringResource(R.string.downloads_history),
                    modifier = Modifier.size(24.dp), // Adjust the size as needed
                )
            },
            modifier = Modifier,
            selected = currentTopDestination == Route.DOWNLOADS,
            onClick = { onNavigateToRoute(Route.DOWNLOADS) },
        )

        NavigationRailItemVariant(
            icon = {
                Image(
                    painter =
                        painterResource(
                            id =
                                if (currentTopDestination == Route.LANGUAGES)
                                    R.drawable.ico_filled_select_language
                                else R.drawable.ico_select_language
                        ),
                    contentDescription = stringResource(R.string.settings),
                    modifier = Modifier.size(24.dp), // Adjust the size as needed
                )
            },
            modifier = Modifier,
            selected = currentTopDestination == Route.LANGUAGES,
            onClick = { onNavigateToRoute(Route.LANGUAGES) },
        )
    }
}

@Composable
private fun DrawerHeader(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .height(141.dp)
                .padding(20.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_no_fill),
                contentDescription = "Tasks header image",
                modifier = Modifier.width(60.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = modifier) {
                Text(
                    text = stringResource(R.string.video_downloader),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )

                Text(
                    text = "(V.1.0)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.alpha(0.6f),
                )
            }
        }
    }
}

@Preview(device = "spec:width=400dp,height=841dp")
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun ExpandedPreview() {
    val widthDp = LocalConfiguration.current.screenWidthDp
    var currentRoute = remember { mutableStateOf(Route.HOME) }

    CompositionLocalProvider(
        LocalWindowWidthState provides
            if (widthDp > 480) WindowWidthSizeClass.Expanded
            else if (widthDp > 360) WindowWidthSizeClass.Medium else WindowWidthSizeClass.Compact
    ) {
        Row {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            NavigationDrawer(
                currentRoute = currentRoute.value,
                currentTopDestination = currentRoute.value,
                drawerState = drawerState,
                onNavigateToRoute = { currentRoute.value = it },
                onDismissRequest = {},
            ) {
                DownloadPageImplV2(
                    taskDownloadStateMap = remember { mutableStateMapOf() },
                    onActionMenu = {},
                ) { _, _ ->
                }
            }
        }
    }
}
