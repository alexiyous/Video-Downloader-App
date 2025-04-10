package com.junkfood.vdownloader.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.vdownloader.Downloader
import com.junkfood.vdownloader.util.PreferenceUtil
import com.junkfood.vdownloader.util.PreferenceUtil.getBoolean
import com.junkfood.vdownloader.util.PreferenceUtil.getLong
import com.junkfood.vdownloader.util.PreferenceUtil.getString
import com.junkfood.vdownloader.util.UpdateUtil
import com.junkfood.vdownloader.util.YT_DLP_AUTO_UPDATE
import com.junkfood.vdownloader.util.YT_DLP_UPDATE_INTERVAL
import com.junkfood.vdownloader.util.YT_DLP_UPDATE_TIME
import com.junkfood.vdownloader.util.YT_DLP_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun YtdlpUpdater() {

    val downloaderState by Downloader.downloaderState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (downloaderState !is Downloader.State.Idle) return@LaunchedEffect

        if (!YT_DLP_AUTO_UPDATE.getBoolean() && YT_DLP_VERSION.getString().isNotEmpty())
            return@LaunchedEffect

        if (!PreferenceUtil.isNetworkAvailableForDownload()) {
            return@LaunchedEffect
        }

        val lastUpdateTime = YT_DLP_UPDATE_TIME.getLong()
        val currentTime = System.currentTimeMillis()

        if (currentTime < lastUpdateTime + YT_DLP_UPDATE_INTERVAL.getLong()) {
            return@LaunchedEffect
        }

        runCatching {
                Downloader.updateState(state = Downloader.State.Updating)
                withContext(Dispatchers.IO) { UpdateUtil.updateYtDlp() }
            }
            .onFailure { it.printStackTrace() }
        Downloader.updateState(state = Downloader.State.Idle)
    }
}
