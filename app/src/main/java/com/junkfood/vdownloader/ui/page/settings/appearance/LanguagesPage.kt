package com.junkfood.vdownloader.ui.page.settings.appearance

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.junkfood.vdownloader.R
import com.junkfood.vdownloader.ui.component.BackButton
import com.junkfood.vdownloader.ui.component.PreferenceSingleChoiceItem
import com.junkfood.vdownloader.ui.theme.DownloaderTheme
import com.junkfood.vdownloader.util.PreferenceUtil
import com.junkfood.vdownloader.util.setLanguage
import com.junkfood.vdownloader.util.toDisplayName
import java.util.Locale

@Composable
fun LanguagePage(onNavigateBack: () -> Unit = {}) {
    val selectedLocale by remember { mutableStateOf(Locale.getDefault()) }

    //    val supportedLocales = LocaleLanguageCodeMap.keys
    val supportedLocales =
        setOf(
            Locale("en"), // English
            Locale("es"), // Español
            Locale("de"), // Deutsch
            Locale.CHINESE, // Chinese
            Locale("ru"), // Русский
            Locale("fr"), // Français
        )

    val preferredLocales = remember {
        val defaultLocaleListCompat = LocaleListCompat.getDefault()
        val mLocaleSet = mutableSetOf<Locale>()

        for (index in 0..defaultLocaleListCompat.size()) {
            val locale = defaultLocaleListCompat[index]
            if (locale != null) {
                mLocaleSet.add(locale)
            }
        }

        return@remember mLocaleSet
    }

    val suggestedLocales =
        remember(preferredLocales) {
            val localeSet = mutableSetOf<Locale>()

            preferredLocales.forEach { desired ->
                val matchedLocale =
                    supportedLocales.firstOrNull { supported ->
                        LocaleListCompat.matchesLanguageAndScript(
                            /* supported = */ desired,
                            /* desired = */ supported,
                        )
                    }
                if (matchedLocale != null) {
                    localeSet.add(matchedLocale)
                }
            }

            return@remember localeSet
        }

    val otherLocales = supportedLocales - suggestedLocales

    LanguagePageImpl(
        onNavigateBack = onNavigateBack,
        suggestedLocales = suggestedLocales,
        otherLocales = otherLocales,
        selectedLocale = selectedLocale,
    ) {
        PreferenceUtil.saveLocalePreference(it)
        setLanguage(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePageImpl(
    onNavigateBack: () -> Unit = {},
    suggestedLocales: Set<Locale>,
    otherLocales: Set<Locale>,
    selectedLocale: Locale,
    onLanguageSelected: (Locale?) -> Unit = {},
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(
                initialHeightOffset = rememberTopAppBarState().heightOffsetLimit,
                initialContentOffset = 0f,
            ),
            canScroll = { true },
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(modifier = Modifier, text = stringResource(id = R.string.language))
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
                colors =
                    TopAppBarDefaults.largeTopAppBarColors(
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                actions = {
                    IconButton(onClick = { onNavigateBack() }) {
                        // Directly use the custom icon
                        Image(
                            painter =
                                painterResource(
                                    id = R.drawable.ico_checked
                                ), // Load the custom drawable icon
                            contentDescription =
                                "Checked Icon", // Provide a description for accessibility
                            modifier = Modifier.size(20.dp), // Adjust the size of the icon
                        )
                    }
                },
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                contentPadding = it,
            ) {
                if (suggestedLocales.isNotEmpty()) {
                    if (!suggestedLocales.contains(Locale.getDefault())) {
                        item {
                            PreferenceSingleChoiceItem(
                                modifier = Modifier.padding(3.dp),
                                text = stringResource(id = R.string.follow_system),
                                selected = !suggestedLocales.contains(selectedLocale),
                            ) {
                                onLanguageSelected(null)
                            }
                        }
                    }

                    for (locale in suggestedLocales) {
                        item {
                            PreferenceSingleChoiceItem(
                                modifier = Modifier.padding(3.dp),
                                text = locale.toDisplayName(),
                                selected = selectedLocale == locale,
                            ) {
                                onLanguageSelected(locale)
                            }
                        }
                    }
                }

                for (locale in otherLocales) {
                    item {
                        PreferenceSingleChoiceItem(
                            text = locale.toDisplayName(),
                            selected = selectedLocale == locale,
                        ) {
                            onLanguageSelected(locale)
                        }
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun LanguagePagePreview() {
    var language by remember { mutableStateOf(Locale.JAPANESE) }
    val map = setOf(Locale.forLanguageTag("en-US"))
    DownloaderTheme {
        LanguagePageImpl(
            suggestedLocales = map,
            otherLocales = map + Locale.forLanguageTag("ja-JP"),
            selectedLocale = language,
        ) {
            language = it
        }
    }
}
