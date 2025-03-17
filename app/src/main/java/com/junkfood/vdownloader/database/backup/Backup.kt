package com.junkfood.vdownloader.database.backup

import com.junkfood.vdownloader.database.objects.CommandTemplate
import com.junkfood.vdownloader.database.objects.DownloadedVideoInfo
import com.junkfood.vdownloader.database.objects.OptionShortcut
import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    val templates: List<CommandTemplate>? = null,
    val shortcuts: List<OptionShortcut>? = null,
    val downloadHistory: List<DownloadedVideoInfo>? = null,
)
