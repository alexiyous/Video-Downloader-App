package com.junkfood.vdownloader.ui.page.settings.network

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.component.ConfirmButton
import com.junkfood.vdownloader.ui.component.DismissButton
import com.junkfood.vdownloader.util.CONCURRENT
import com.junkfood.vdownloader.util.MAX_RATE
import com.junkfood.vdownloader.util.PROXY_URL
import com.junkfood.vdownloader.util.PreferenceUtil
import com.junkfood.vdownloader.util.PreferenceUtil.getString
import com.junkfood.vdownloader.util.PreferenceUtil.updateString
import com.junkfood.vdownloader.util.isNumberInRange
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RateLimitDialog(onDismissRequest: () -> Unit) {
    var isError by remember { mutableStateOf(false) }
    var maxRate by remember { mutableStateOf(PreferenceUtil.getMaxDownloadRate()) }
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.Speed, null) },
        title = { Text(stringResource(R.string.rate_limit)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.rate_limit_desc),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
                    isError = isError,
                    supportingText = {
                        Text(
                            text = if (isError) stringResource(R.string.invalid_input) else "",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                if (isError) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    value = maxRate,
                    label = { Text(stringResource(R.string.max_rate)) },
                    onValueChange = {
                        if (it.isDigitsOnly()) maxRate = it
                        isError = false
                    },
                    trailingIcon = { Text("K") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
            }
        },
        dismissButton = { DismissButton { onDismissRequest() } },
        confirmButton = {
            ConfirmButton {
                if (maxRate.isNumberInRange(1, 100_0000)) {
                    PreferenceUtil.encodeString(MAX_RATE, maxRate)
                    onDismissRequest()
                } else {
                    isError = true
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcurrentDownloadDialog(onDismissRequest: () -> Unit) {
    var concurrentFragments by remember {
        mutableFloatStateOf(PreferenceUtil.getConcurrentFragments())
    }
    val count by remember {
        derivedStateOf {
            if (concurrentFragments <= 0.125f) 1 else ((concurrentFragments * 3f).roundToInt()) * 8
        }
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.dismiss)) }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    PreferenceUtil.encodeInt(CONCURRENT, count)
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        icon = { Icon(Icons.Outlined.OfflineBolt, null) },
        title = { Text(stringResource(R.string.concurrent_download)) },
        text = {
            Column {
                val interactionSource = remember { MutableInteractionSource() }
                Text(text = stringResource(R.string.concurrent_download_num, count))

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = concurrentFragments,
                    onValueChange = { concurrentFragments = it },
                    steps = 2,
                    valueRange = 0f..1f,
                    thumb = {
                        SliderDefaults.Thumb(
                            modifier = Modifier,
                            interactionSource = interactionSource,
                            thumbSize = DpSize(4.dp, 32.dp),
                        )
                    },
                )
            }
        },
    )
}

@Composable
fun ProxyConfigurationDialog(onDismissRequest: () -> Unit = {}) {
    var proxyUrl by remember { mutableStateOf(PROXY_URL.getString()) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.VpnKey, null) },
        title = { Text(stringResource(R.string.proxy)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.proxy_desc),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
                    value = proxyUrl,
                    label = { Text(stringResource(R.string.proxy)) },
                    onValueChange = { proxyUrl = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
            }
        },
        dismissButton = { DismissButton { onDismissRequest() } },
        confirmButton = {
            ConfirmButton {
                PROXY_URL.updateString(proxyUrl)
                onDismissRequest()
            }
        },
    )
}
