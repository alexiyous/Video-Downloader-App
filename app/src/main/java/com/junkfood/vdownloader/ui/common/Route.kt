package com.junkfood.vdownloader.ui.common

object Route {
    const val TOS = "tos"

    const val HOME = "home"
    const val PROGRESS = "progress"
    const val DOWNLOADS = "download_history"
    const val History = "history"
    const val TAB_LIST = "tab_list"
    const val LANGUAGES = "languages"

    const val WHATSAPP_IMAGE = "whatsapp_image"
    const val WHATSAPP_VIDEO = "whatsapp_video"
    const val WHATSAPP_VIDEO_VIEWER = "whatsapp_video_viewer"
    const val WHATSAPP_SAVE = "whatsapp_save"
    const val WHATSAPP_VIEW = "whatsapp_VIEW"

    const val Help = "help"
    const val SETTINGS = "settings"
    const val SETTINGS_PAGE = "settings_page"
    const val TASK_LIST = "task_list"
    const val TASK_LOG = "task_log"

    const val PLAYLIST = "playlist"
    const val FORMAT_SELECTION = "format"
    const val APPEARANCE = "appearance"
    const val INTERACTION = "interaction"
    const val GENERAL_DOWNLOAD_PREFERENCES = "general_download_preferences"
    const val ABOUT = "about"
    const val DOWNLOAD_DIRECTORY = "download_directory"
    const val CREDITS = "credits"

    const val SHARE_APP = "share_app"
    const val RATE_US = "rate_us"
    const val PRIVACY_POLICY = "privacy_policy"
    const val TEMPLATE = "template"
    const val TEMPLATE_EDIT = "template_edit"
    const val DARK_THEME = "dark_theme"
    const val DOWNLOAD_QUEUE = "queue"
    const val DOWNLOAD_FORMAT = "download_format"
    const val NETWORK_PREFERENCES = "network_preferences"
    const val COOKIE_PROFILE = "cookie_profile"
    const val COOKIE_GENERATOR_WEBVIEW = "cookie_webview"
    const val SUBTITLE_PREFERENCES = "subtitle_preferences"
    const val AUTO_UPDATE = "auto_update"
    const val DONATE = "donate"
    const val TROUBLESHOOTING = "troubleshooting"

    const val TASK_HASHCODE = "task_hashcode"
    const val TEMPLATE_ID = "template_id"
}

infix fun String.arg(arg: String) = "$this/{$arg}"

infix fun String.id(id: Int) = "$this/$id"
