package com.junkfood.vdownloader.ui.page

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.common.Route
import com.junkfood.vdownloader.ui.theme.bottomNavigationDark

sealed class BottomNavItem(
    val route: String,
    val activeIcon: Int,
    val inactiveIcon: Int,
    val title: @Composable () -> String,
) {
    data object Home :
        BottomNavItem(
            Route.HOME,
            R.drawable.ico_home_active,
            R.drawable.ico_home_inactive,
            { stringResource(id = R.string.home) },
        )

    data object Progress :
        BottomNavItem(
            Route.PROGRESS,
            R.drawable.ico_progress_active,
            R.drawable.ico_progress_inactive,
            { stringResource(id = R.string.progress) },
        )

    data object Download :
        BottomNavItem(
            Route.DOWNLOADS,
            R.drawable.ico_download_active,
            R.drawable.ico_download_inactive,
            { stringResource(id = R.string.download) },
        )
}

sealed class BottomWhatsappNavItem(
    val route: String,
    val activeIcon: Int,
    val inactiveIcon: Int,
    val title: @Composable () -> String,
) {
    data object Whatsapp_image :
        BottomNavItem(
            Route.WHATSAPP_IMAGE,
            R.drawable.ico_w_img_active,
            R.drawable.ico_w_img_inactive,
            { stringResource(id = R.string.whatsapp_image) },
        )

    data object Whatsapp_video :
        BottomNavItem(
            Route.WHATSAPP_VIDEO,
            R.drawable.ico_w_video_active,
            R.drawable.ico_w_video_inactive,
            { stringResource(id = R.string.whatsapp_video) },
        )

    data object Whatsapp_saved :
        BottomNavItem(
            Route.WHATSAPP_SAVE,
            R.drawable.ico_w_save_active,
            R.drawable.ico_w_save_inactive,
            { stringResource(id = R.string.whatsapp_save) },
        )
}

@Composable
fun BottomNavigationBar(navController: NavController, strType: String) {
    val items =
        if (strType == "main") {
            listOf(BottomNavItem.Home, BottomNavItem.Progress, BottomNavItem.Download)
        } else {
            listOf(
                BottomWhatsappNavItem.Whatsapp_image,
                BottomWhatsappNavItem.Whatsapp_video,
                BottomWhatsappNavItem.Whatsapp_saved,
            )
        }

    // Determine background color based on theme
    val backgroundColor =
        if (LocalDarkTheme.current.isDarkTheme()) {
            bottomNavigationDark
        } else {
            MaterialTheme.colorScheme.background // Light theme background
        }

    BottomNavigation(
        backgroundColor = backgroundColor,
        elevation = 50.dp, // Stronger elevation for more prominent shadow
        modifier =
            Modifier.shadow(
                    50.dp,
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    clip = false,
                ) // Stronger shadow
                .graphicsLayer {
                    // Increase shadowElevation to make it more pronounced
                    shadowElevation = 50.dp.value // Higher elevation for stronger shadow
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    clip = false
                }
                .padding(top = 6.dp), // Slight padding to allow shadow visibility
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = {
                    val iconRes =
                        if (currentRoute == item.route) item.activeIcon else item.inactiveIcon

                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.title(),
                        modifier = Modifier.size(25.dp).align(Alignment.CenterVertically),
                    )
                },
                modifier = Modifier.align(Alignment.CenterVertically).padding(10.dp),
                label = {
                    Text(
                        item.title(),
                        modifier = Modifier.align(Alignment.CenterVertically).padding(top = 5.dp),
                    )
                },
                selected = currentRoute == item.route,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    if (item.route == Route.HOME) {
                        navController.popBackStack(Route.HOME, inclusive = false)
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    val navController = rememberNavController()
    // Provide mock values for preview
    BottomNavigationBar(navController = navController, "main")
}
