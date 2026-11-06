package org.udmurtlang.learn.feature.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.BuildConfig
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.safeContentTopPadding
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.components.ProgressLine
import org.udmurtlang.learn.core.designsystem.components.SectionChips
import org.udmurtlang.learn.core.designsystem.components.SwitchCard
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.domain.model.ThemeMode
import org.udmurtlang.learn.domain.model.UiLanguage
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var draftVolume by remember(settings.ttsVolume) { mutableFloatStateOf(settings.ttsVolume) }
    var geoDialogVisible by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.onIntent(SettingsIntent.NotificationsToggled(true))
        } else {
            viewModel.markNotificationsDenied()
        }
    }

    val geoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onIntent(SettingsIntent.GeolocationToggled(granted))
    }

    if (geoDialogVisible) {
        AlertDialog(
            onDismissRequest = { geoDialogVisible = false },
            title = { Text(stringResource(R.string.geo_dialog_title)) },
            text = { Text(stringResource(R.string.geo_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        geoDialogVisible = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            geoLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        } else {
                            viewModel.onIntent(SettingsIntent.GeolocationToggled(true))
                        }
                    },
                ) {
                    Text(stringResource(R.string.allow), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { geoDialogVisible = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(scheme.background).safeContentTopPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZcHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
            modifier = Modifier.widthIn(max = ReadingContentWidth),
        )
        ScrollableContent(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            fillViewport = true,
        ) {
            Column {
                SwitchCard(
                    title = stringResource(R.string.setting_sound),
                    checked = settings.soundEnabled,
                    onCheckedChange = { viewModel.onIntent(SettingsIntent.SoundToggled(it)) },
                )
                if (settings.soundEnabled) {
                    val volumeLabel = stringResource(R.string.setting_volume)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = volumeLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${(draftVolume * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                            Slider(
                                value = draftVolume,
                                onValueChange = { draftVolume = it },
                                onValueChangeFinished = {
                                    viewModel.onIntent(SettingsIntent.VolumeChanged(draftVolume))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { contentDescription = volumeLabel },
                                thumb = {
                                    Box(
                                        Modifier.size(20.dp).background(scheme.primary, CircleShape),
                                    )
                                },
                                track = { slider ->
                                    ProgressLine(
                                        progress = slider.value,
                                        track = scheme.onSurfaceVariant.copy(alpha = 0.32f),
                                    )
                                },
                            )
                        }
                    }
                }
                HorizontalDivider(color = scheme.outlineVariant)
                SwitchCard(
                    title = stringResource(R.string.setting_word_notify),
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.onIntent(SettingsIntent.NotificationsToggled(enabled))
                        }
                    },
                )
                if (uiState.notificationsDenied) {
                    Text(
                        text = stringResource(R.string.notifications_permission_rationale),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                WordOfDayTimeSetting(
                    time = settings.wordOfDayTime,
                    onTimeSelected = { viewModel.onIntent(SettingsIntent.WordOfDayTimeSelected(it)) },
                )
                HorizontalDivider(color = scheme.outlineVariant)
                SwitchCard(
                    title = stringResource(R.string.setting_geolocation),
                    checked = settings.geolocationEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            geoDialogVisible = true
                        } else {
                            viewModel.onIntent(SettingsIntent.GeolocationToggled(false))
                        }
                    },
                )
                if (uiState.geoDenied && !settings.geolocationEnabled) {
                    Text(
                        text = stringResource(R.string.geo_denied),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = scheme.outlineVariant)

            val langItems = listOf(
                stringResource(R.string.lang_russian),
                stringResource(R.string.lang_udmurt),
            )
            val themeItems = listOf(
                stringResource(R.string.theme_system),
                stringResource(R.string.theme_light),
                stringResource(R.string.theme_dark),
            )
            Column {
                SectionChips(
                    title = stringResource(R.string.setting_language),
                    items = langItems,
                    selectedIndex = UiLanguage.entries.indexOf(settings.uiLanguage).coerceAtLeast(0),
                    onSelect = { index ->
                        viewModel.onIntent(SettingsIntent.LanguageSelected(UiLanguage.entries[index]))
                    },
                )
                HorizontalDivider(color = scheme.outlineVariant)
                SectionChips(
                    title = stringResource(R.string.setting_theme),
                    items = themeItems,
                    selectedIndex = ThemeMode.entries.indexOf(settings.themeMode).coerceAtLeast(0),
                    onSelect = { index ->
                        viewModel.onIntent(SettingsIntent.ThemeSelected(ThemeMode.entries[index]))
                    },
                )
            }

            HorizontalDivider(color = scheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(
                        if (BuildConfig.SUPABASE_ENABLED) R.string.sync_cloud else R.string.sync_local,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.setting_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WordOfDayTimeSetting(time: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    val context = LocalContext.current
    val dark = LocalBrand.current.isDark
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()
    val use24HourTime = DateFormat.is24HourFormat(context)
    val formatter = remember(locale, use24HourTime) {
        DateTimeFormatter.ofPattern(if (use24HourTime) "H:mm" else "h:mm a", locale)
    }
    val title = stringResource(R.string.setting_word_notify_time)
    val done = stringResource(R.string.done)
    val cancel = stringResource(R.string.cancel)
    val scheme = MaterialTheme.colorScheme
    var showPicker by rememberSaveable { mutableStateOf(false) }
    if (showPicker) {
        DisposableEffect(context, locale, dark, title, done, cancel, time, use24HourTime) {
            val theme = if (dark) android.R.style.Theme_Material_Dialog_Alert
            else android.R.style.Theme_Material_Light_Dialog_Alert
            val dialog = TimePickerDialog(
                context, theme, { _, hour, minute -> onTimeSelected(LocalTime.of(hour, minute)) },
                time.hour, time.minute, use24HourTime,
            )
            dialog.setTitle(title)
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, done, dialog)
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel) { _, _ -> dialog.cancel() }
            dialog.setOnDismissListener { showPicker = false }
            dialog.show()
            onDispose {
                dialog.setOnDismissListener(null)
                dialog.dismiss()
            }
        }
    }

    Surface(
        onClick = { showPicker = true },
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(18.dp),
        color = scheme.surface,
        border = BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = time.format(formatter),
                modifier = Modifier
                    .background(scheme.inverseSurface, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = scheme.inverseOnSurface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
