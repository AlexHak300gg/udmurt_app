package org.udmurtlang.learn.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.BuildConfig
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcMuted
import org.udmurtlang.learn.core.designsystem.components.SectionChips
import org.udmurtlang.learn.core.designsystem.components.SwitchCard
import org.udmurtlang.learn.domain.model.ThemeMode
import org.udmurtlang.learn.domain.model.UiLanguage

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SwitchCard(
                title = stringResource(R.string.setting_sound),
                checked = settings.soundEnabled,
                onCheckedChange = { viewModel.onIntent(SettingsIntent.SoundToggled(it)) },
            )
            if (settings.soundEnabled) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                        Text(
                            text = stringResource(R.string.setting_volume),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Slider(
                            value = settings.ttsVolume,
                            onValueChange = { viewModel.onIntent(SettingsIntent.VolumeChanged(it)) },
                            enabled = settings.soundEnabled,
                            colors = SliderDefaults.colors(
                                activeTrackColor = ZcRed,
                                thumbColor = Color.White,
                                activeTickColor = Color.Transparent,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        )
                    }
                }
            }
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

        Spacer(modifier = Modifier.height(18.dp))

        val langItems = listOf(
            stringResource(R.string.lang_russian),
            stringResource(R.string.lang_udmurt),
        )
        val themeItems = listOf(
            stringResource(R.string.theme_system),
            stringResource(R.string.theme_light),
            stringResource(R.string.theme_dark),
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionChips(
                title = stringResource(R.string.setting_language),
                items = langItems,
                selectedIndex = UiLanguage.entries.indexOf(settings.uiLanguage).coerceAtLeast(0),
                onSelect = { index ->
                    viewModel.onIntent(SettingsIntent.LanguageSelected(UiLanguage.entries[index]))
                },
            )
            SectionChips(
                title = stringResource(R.string.setting_theme),
                items = themeItems,
                selectedIndex = ThemeMode.entries.indexOf(settings.themeMode).coerceAtLeast(0),
                onSelect = { index ->
                    viewModel.onIntent(SettingsIntent.ThemeSelected(ThemeMode.entries[index]))
                },
            )
        }

        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(
                if (BuildConfig.SUPABASE_ENABLED) R.string.sync_cloud else R.string.sync_local,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.setting_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 2.dp, bottom = 24.dp),
        )
    }
}
