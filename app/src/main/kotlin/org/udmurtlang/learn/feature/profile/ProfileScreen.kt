package org.udmurtlang.learn.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.BuildConfig
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.StarGlyph
import org.udmurtlang.learn.core.designsystem.ZcGold
import org.udmurtlang.learn.core.designsystem.ZcSubtle
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.CircleAvatar
import org.udmurtlang.learn.core.designsystem.components.FactBubble
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.core.designsystem.components.GhostButton
import org.udmurtlang.learn.core.designsystem.components.Gradients
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted
import org.udmurtlang.learn.domain.model.AchievementSlugs
import org.udmurtlang.learn.domain.model.StatsSnapshot

private val avatarPresets = listOf("А", "Б", "В", "Г", "Д", "Е", "Ж", "З")

private val goldDeep = Color(0xFFD98F1F)

private data class AchievementBadge(
    val slug: String,
    val icon: ImageVector,
    val titleRes: Int,
)

private val achievementCatalog = listOf(
    AchievementBadge(AchievementSlugs.FIRST_WORD, Icons.Filled.RecordVoiceOver, R.string.ach_first_word),
    AchievementBadge(AchievementSlugs.STREAK_3, Icons.Filled.LocalFireDepartment, R.string.ach_streak_3),
    AchievementBadge(AchievementSlugs.STREAK_7, Icons.Filled.EmojiEvents, R.string.ach_streak_7),
    AchievementBadge(AchievementSlugs.TOPICS_DONE, Icons.Filled.MenuBook, R.string.ach_topics_done),
    AchievementBadge(AchievementSlugs.GAMES_10, Icons.Filled.SportsEsports, R.string.ach_games_10),
)

private val factPages = listOf(
    R.string.fact_1,
    R.string.fact_2,
    R.string.fact_3,
    R.string.fact_4,
    R.string.fact_5,
    R.string.fact_6,
)

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenReport: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.notice) {
        if (state.notice == "logged_out" || state.notice == "deleted") onLoggedOut()
    }

    if (state.passwordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(ProfileIntent.CancelPassword) },
            title = { Text(stringResource(R.string.change_password)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.oldPassword,
                        onValueChange = { viewModel.onIntent(ProfileIntent.OldPasswordChanged(it)) },
                        label = { Text(stringResource(R.string.old_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = { viewModel.onIntent(ProfileIntent.NewPasswordChanged(it)) },
                        label = { Text(stringResource(R.string.new_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.passwordError?.let { key ->
                        Text(resolveError(key), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(ProfileIntent.SavePassword) }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(ProfileIntent.CancelPassword) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (state.deleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(ProfileIntent.CancelDelete) },
            title = { Text(stringResource(R.string.delete_account)) },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(ProfileIntent.ConfirmDelete) }) {
                    Text(
                        text = stringResource(R.string.delete_account),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(ProfileIntent.CancelDelete) }) {
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
            .verticalScroll(rememberScrollState()),
    ) {
        // Оранжевая hero-карта
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent,
        ) {
            Box(modifier = Modifier.background(Gradients.orange)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                ) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = onVivid(),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val name = state.profile?.displayName ?: stringResource(R.string.guest_label)
                        CircleAvatar(
                            initial = name.ifBlank { "?" },
                            size = 76.dp,
                        )
                        Spacer(modifier = Modifier.size(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = onVivid(),
                                )
                                state.stats?.let { stats ->
                                    Spacer(modifier = Modifier.size(8.dp))
                                    GamesCountBadge(count = stats.gamesPlayed)
                                }
                            }
                            state.profile?.takeIf { !it.isGuest }?.let {
                                Text(
                                    text = it.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onVividMuted(),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val stats: StatsSnapshot? = state.stats
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileStatChip(
                            value = stats?.wordsKnown ?: 0,
                            label = stringResource(R.string.profile_stat_words),
                            modifier = Modifier.weight(1f),
                        )
                        ProfileStatChip(
                            value = stats?.currentStreak ?: 0,
                            label = stringResource(R.string.profile_stat_days),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        val unlockedSlugs = state.achievements.map { it.slug }.toSet()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.achievements_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (unlockedSlugs.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_achievements),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                achievementCatalog.chunked(3).forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        rowItems.forEach { badge ->
                            MedalTile(
                                icon = badge.icon,
                                title = stringResource(badge.titleRes),
                                unlocked = badge.slug in unlockedSlugs,
                            )
                        }
                    }
                }
            }
        }

        StarGlyph(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            size = 18.dp,
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.facts_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            val pagerState = rememberPagerState { factPages.size }
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                FactCard(textRes = factPages[page])
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(factPages.size) { index ->
                    val selected = pagerState.currentPage == index
                    Surface(
                        shape = CircleShape,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(if (selected) 10.dp else 8.dp),
                    ) { }
                    Spacer(modifier = Modifier.size(6.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.isEditing) {
                OutlinedTextField(
                    value = state.editingName,
                    onValueChange = { viewModel.onIntent(ProfileIntent.NameChanged(it)) },
                    label = { Text(stringResource(R.string.profile_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GhostButton(
                        text = stringResource(R.string.cancel),
                        onClick = { viewModel.onIntent(ProfileIntent.CancelEdit) },
                        modifier = Modifier.weight(1f),
                    )
                    GameButton(
                        text = stringResource(R.string.save),
                        onClick = { viewModel.onIntent(ProfileIntent.SaveName) },
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                GhostButton(
                    text = stringResource(R.string.profile_name),
                    icon = Icons.Filled.Edit,
                    onClick = { viewModel.onIntent(ProfileIntent.EditName) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GameButton(
                    text = stringResource(R.string.stats_title),
                    icon = Icons.Filled.BarChart,
                    onClick = onOpenStats,
                    modifier = Modifier.weight(1f),
                )
                GameButton(
                    text = stringResource(R.string.report_title),
                    icon = Icons.Filled.People,
                    onClick = onOpenReport,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                avatarPresets.forEach { avatar ->
                    Surface(
                        onClick = { viewModel.onIntent(ProfileIntent.SelectAvatar(avatar)) },
                        shape = CircleShape,
                        color = if (state.profile?.avatar == avatar) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            ZcSubtle
                        },
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(avatar, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            val currentProfile = state.profile
            if (currentProfile != null && !currentProfile.isGuest) {
                GhostButton(
                    text = stringResource(R.string.change_password),
                    icon = Icons.Filled.Key,
                    onClick = { viewModel.onIntent(ProfileIntent.EditPassword) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GhostButton(
                text = stringResource(R.string.delete_account),
                icon = Icons.Filled.Delete,
                containerColor = MaterialTheme.colorScheme.errorContainer.takeIf { !LocalBrand.current.isDark }
                    ?: MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.error,
                onClick = { viewModel.onIntent(ProfileIntent.DeleteAccount) },
                modifier = Modifier.fillMaxWidth(),
            )
            GhostButton(
                text = stringResource(R.string.logout_account),
                onClick = { viewModel.onIntent(ProfileIntent.Logout) },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.sync_local),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.setting_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 18.dp),
            )
        }
    }
}

@Composable
private fun ProfileStatChip(value: Int, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = ZcWhite.copy(alpha = 0.18f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineSmall,
                color = ZcWhite,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ZcWhite.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun FactCard(textRes: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp),
    ) {
        FactBubble(text = stringResource(textRes))
    }
}

@Composable
private fun resolveError(key: String): String = when (key) {
    "auth_error_password" -> stringResource(R.string.auth_error_password)
    "profile_error_old" -> stringResource(R.string.profile_error_old)
    else -> key
}

@Composable
private fun GamesCountBadge(count: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = ZcWhite.copy(alpha = 0.22f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = ZcWhite,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ZcWhite,
            )
        }
    }
}

@Composable
private fun MedalTile(
    icon: ImageVector,
    title: String,
    unlocked: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (unlocked) scheme.surface else scheme.surface.copy(alpha = 0.6f),
        modifier = Modifier.size(width = 100.dp, height = 116.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            unlocked -> Brush.verticalGradient(
                                listOf(ZcGold, goldDeep),
                            )
                            else -> Brush.verticalGradient(
                                listOf(scheme.surfaceVariant, scheme.surfaceVariant.copy(alpha = 0.75f)),
                            )
                        },
                    )
                    .border(
                        width = 2.dp,
                        color = if (unlocked) {
                            ZcWhite.copy(alpha = 0.65f)
                        } else {
                            scheme.outline.copy(alpha = 0.25f)
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (unlocked) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ZcWhite,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(ZcWhite.copy(alpha = 0.25f), ZcWhite.copy(alpha = 0f)),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY,
                                ),
                            ),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (unlocked) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
