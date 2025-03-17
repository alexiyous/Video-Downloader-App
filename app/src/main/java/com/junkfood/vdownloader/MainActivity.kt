package com.junkfood.vdownloader

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.junkfood.vdownloader.App.Companion.context
import com.junkfood.vdownloader.ui.common.LocalDarkTheme
import com.junkfood.vdownloader.ui.common.SettingsProvider
import com.junkfood.vdownloader.ui.page.AppEntry
import com.junkfood.vdownloader.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.junkfood.vdownloader.ui.theme.DownloaderTheme
import com.junkfood.vdownloader.util.PreferenceUtil
import com.junkfood.vdownloader.util.matchUrlFromSharedText
import com.junkfood.vdownloader.util.setLanguage
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext

class MainActivity : AppCompatActivity() {
    private val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101
    private val dialogViewModel: DownloadDialogViewModel by viewModel()

    private val showPermissionDialog = mutableStateOf(false)
    private lateinit var requestManageStorageLauncher: ActivityResultLauncher<Intent>

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < 33) {
            runBlocking { setLanguage(PreferenceUtil.getLocaleFromPreference()) }
        }
        enableEdgeToEdge()

        context = this.baseContext
        showPermissionDialog.value = false

        // Register the launcher for MANAGE_EXTERNAL_STORAGE intent
        requestManageStorageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    showPermissionDialog.value = !Environment.isExternalStorageManager()

                    //                if (!showPermissionDialog.value) {
                    //
                    //                }
                }
            }
        setContent {
            KoinContext {
                val windowSizeClass = calculateWindowSizeClass(this)
                SettingsProvider(windowWidthSizeClass = windowSizeClass.widthSizeClass) {
                    DownloaderTheme(darkTheme = LocalDarkTheme.current.isDarkTheme()) {
                        AppEntry(
                            dialogViewModel = dialogViewModel,
                            showPermissionDialog = showPermissionDialog,
                            onRequestPermission = { requestStoragePermission() },
                            onCancelPermission = { showPermissionDialog.value = false },
                            isStoragePermissionGranted =
                                ::isStoragePermissionGranted, // Pass the function reference
                        )
                    }
                }
            }
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check if the app has MANAGE_EXTERNAL_STORAGE permission
            Environment.isExternalStorageManager()
        } else {
            // For older versions (below Android 11), check if WRITE_EXTERNAL_STORAGE is granted
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                requestManageStorageLauncher.launch(intent)
            } else {
                showPermissionDialog.value = false
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MANAGE_EXTERNAL_STORAGE_REQUEST_CODE,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val url = intent.getSharedURL()
        if (url != null) {
            dialogViewModel.postAction(DownloadDialogViewModel.Action.ShowSheet(listOf(url)))
        }
    }

    private fun Intent.getSharedURL(): String? {
        val intent = this

        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.dataString
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedContent ->
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    matchUrlFromSharedText(sharedContent).also { matchedUrl ->
                        if (sharedUrlCached != matchedUrl) {
                            sharedUrlCached = matchedUrl
                        }
                    }
                }
            }

            else -> {
                null
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private var sharedUrlCached = ""
    }
}
