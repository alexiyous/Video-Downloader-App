package com.junkfood.vdownloader.ui.page.whatsapp

import android.app.Application
import android.app.PendingIntent
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize


class WhatsappMediaViewModel(
    private val application: Application
) : ViewModel() {
    private val _mediaState = MutableStateFlow<MediaState>(MediaState.Loading)
    val mediaState = _mediaState.asStateFlow()

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted = _permissionGranted.asStateFlow()

    init {
        checkPermissionAndLoadMedia()
    }

    private fun checkPermissionAndLoadMedia() {
        viewModelScope.launch {
            if (hasRequiredPermissions()) {
                _permissionGranted.value = true
                loadWhatsAppMedia()
            } else {
                _permissionGranted.value = false
                _mediaState.value = MediaState.NoPermission
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                ContextCompat.checkSelfPermission(
                    application,
                    android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    application,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            application,
                            android.Manifest.permission.READ_MEDIA_IMAGES
                        ) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                ContextCompat.checkSelfPermission(
                    application,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun requestMediaAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            viewModelScope.launch {
                try {
                    val writeRequest = MediaStore.createWriteRequest(
                        application.contentResolver,
                        getWhatsAppMediaUris()
                    )
                    _mediaState.value = MediaState.RequestAccess(writeRequest)
                } catch (e: Exception) {
                    _mediaState.value = MediaState.Error(e.message ?: "Failed to request media access")
                }
            }
        }
    }

    private fun getWhatsAppMediaUris(): List<Uri> {
        val uris = mutableListOf<Uri>()

        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DATA} LIKE ?"
        val selectionArgs = arrayOf("%WhatsApp%Media%")

        // Query images
        application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                uris.add(
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                )
            }
        }

        // Query videos
        application.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                uris.add(
                    ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                )
            }
        }

        return uris
    }

    private fun loadWhatsAppMedia() {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            try {
                val media = getWhatsAppVideos() + getWhatsAppImages()
                _mediaState.value = if (media.isEmpty()) {
                    MediaState.Empty
                } else {
                    MediaState.Success(media.sortedByDescending { it.dateAdded })
                }
            } catch (e: Exception) {
                _mediaState.value = MediaState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun getWhatsAppVideos(): List<WhatsAppMedia> {
        val videos = mutableListOf<WhatsAppMedia>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION
        )

        val selection = "${MediaStore.Video.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%WhatsApp%Media%")
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        application.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(pathColumn)
                val date = cursor.getLong(dateColumn)
                val duration = cursor.getLong(durationColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Get thumbnail URI
                val thumbnailUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()
                } else {
                    // For older versions, use bitmap thumbnail
                    val thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                        application.contentResolver,
                        id,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null
                    )
                    // Convert bitmap to URI using content provider
                    val thumbnailPath = MediaStore.Images.Media.insertImage(
                        application.contentResolver,
                        thumbnail,
                        "thumbnail_$id",
                        null
                    )
                    thumbnailPath ?: contentUri.toString()
                }

                videos.add(
                    WhatsAppMedia(
                        id = id,
                        uri = contentUri.toString(),
                        thumbnailUri = thumbnailUri,
                        path = path,
                        dateAdded = date,
                        duration = duration,
                        type = MediaType.VIDEO
                    )
                )
            }
        }
        return videos
    }

    private fun getWhatsAppImages(): List<WhatsAppMedia> {
        val images = mutableListOf<WhatsAppMedia>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%WhatsApp%Media%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(pathColumn)
                val date = cursor.getLong(dateColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // For images, we can use the same URI for both full image and thumbnail
                // But we'll create a thumbnail URI that requests a smaller size for efficiency
                val thumbnailUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    "${contentUri}?resize=300,300"
                } else {
                    // For older versions, we can still use the same URI
                    // Coil will handle resizing efficiently
                    contentUri.toString()
                }

                images.add(
                    WhatsAppMedia(
                        id = id,
                        uri = contentUri.toString(),
                        thumbnailUri = thumbnailUri,
                        path = path,
                        dateAdded = date,
                        duration = 0,
                        type = MediaType.IMAGE
                    )
                )
            }
        }
        return images
    }

    fun onMediaAccessResult(granted: Boolean) {
        _permissionGranted.value = granted
        if (granted) {
            loadWhatsAppMedia()
        } else {
            _mediaState.value = MediaState.NoPermission
        }
    }
}

// Data classes and enums
@Parcelize
enum class MediaType: Parcelable {
    IMAGE,
    VIDEO
}

@Parcelize
data class WhatsAppMedia(
    val id: Long,
    val uri: String,
    val thumbnailUri: String,
    val path: String,
    val dateAdded: Long,
    val duration: Long = 0,
    val type: MediaType
): Parcelable

sealed class MediaState {
    object Loading : MediaState()
    object Empty : MediaState()
    object NoPermission : MediaState()
    data class Success(val media: List<WhatsAppMedia>) : MediaState()
    data class Error(val message: String) : MediaState()
    data class RequestAccess(val intent: PendingIntent) : MediaState()
}
