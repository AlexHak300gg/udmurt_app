package org.udmurtlang.learn.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.domain.model.MAX_DISPLAY_NAME_LENGTH
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.CircleAvatar
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.domain.model.AchievementSlugs

/** Цвета медалей по кругу, как в макете. */
private val medalAccents = listOf(ZcRed, ZcOrange, ZcPurple, ZcTeal)

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

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenStats: () -> Unit,
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

    if (state.isEditing) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(ProfileIntent.CancelEdit) },
            title = { Text(stringResource(R.string.profile_name)) },
            text = {
                OutlinedTextField(
                    value = state.editingName,
                    onValueChange = { viewModel.onIntent(ProfileIntent.NameChanged(it)) },
                    label = { Text(stringResource(R.string.profile_name)) },
                    supportingText = {
                        val name = state.editingName
                        Text("${name.codePointCount(0, name.length)} / $MAX_DISPLAY_NAME_LENGTH")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = state.editingName.isNotBlank(),
                    onClick = { viewModel.onIntent(ProfileIntent.SaveName) },
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(ProfileIntent.CancelEdit) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZcHeader(
            title = stringResource(R.string.profile_title),
            onBack = onBack,
            modifier = Modifier.widthIn(max = ReadingContentWidth),
        )
        ScrollableContent(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val name = state.profile?.displayName ?: stringResource(R.string.guest_label)
                CircleAvatar(initial = name.ifBlank { "?" }, size = 88.dp, container = brandAccent(ZcRed))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (state.profile != null) {
                        IconButton(onClick = { viewModel.onIntent(ProfileIntent.EditName) }) {
                            Icon(Icons.Filled.Edit, stringResource(R.string.profile_name), modifier = Modifier.size(18.dp))
                        }
                    }
                }
                state.profile?.takeIf { !it.isGuest }?.let {
                    Text(it.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(16.dp))
                val stats = state.stats
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileStatChip(stats?.wordsKnown ?: 0, stringResource(R.string.profile_stat_words), ZcPurple, Modifier.weight(1f))
                    ProfileStatChip(stats?.gamesPlayed ?: 0, stringResource(R.string.profile_stat_games), ZcTeal, Modifier.weight(1f))
                    ProfileStatChip(stats?.currentStreak ?: 0, stringResource(R.string.profile_stat_days), ZcOrange, Modifier.weight(1f))
                }
            }

            ProfileActionGroup {
                ProfileAction(stringResource(R.string.stats_title), Icons.Filled.BarChart, onClick = onOpenStats)
            }
            Spacer(Modifier.height(18.dp))
            val unlockedSlugs = remember(state.achievements) { state.achievements.map { it.slug }.toSet() }
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
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    achievementCatalog.forEachIndexed { index, badge ->
                        MedalTile(
                            icon = badge.icon,
                            title = stringResource(badge.titleRes),
                            unlocked = badge.slug in unlockedSlugs,
                            accent = medalAccents[index % medalAccents.size],
                        )
                    }
                }
            }

            if (state.profile != null) {
                Spacer(Modifier.height(18.dp))
                ProfileActionGroup {
                    if (state.profile?.isGuest == false) {
                        ProfileAction(stringResource(R.string.change_password), Icons.Filled.Key) {
                            viewModel.onIntent(ProfileIntent.EditPassword)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    ProfileAction(stringResource(R.string.logout_account), Icons.AutoMirrored.Filled.Logout) {
                        viewModel.onIntent(ProfileIntent.Logout)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileAction(
                        title = stringResource(R.string.delete_account),
                        icon = Icons.Filled.Delete,
                        tint = MaterialTheme.colorScheme.error,
                    ) {
                        viewModel.onIntent(ProfileIntent.DeleteAccount)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileActionGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(content = content)
    }
}

@Composable
private fun ProfileAction(
    title: String,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = tint,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProfileStatChip(value: Int, label: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = brandAccent(accent),
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
private fun resolveError(key: String): String = when (key) {
    "auth_error_password" -> stringResource(R.string.auth_error_password)
    "profile_error_old" -> stringResource(R.string.profile_error_old)
    else -> key
}

@Composable
private fun MedalTile(
    icon: ImageVector,
    title: String,
    unlocked: Boolean,
    accent: Color = ZcRed,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier.size(width = 72.dp, height = 100.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
        ) {
            // Макет: плоская цветная плашка с белой иконкой, закрытая — бежевая с замком
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (unlocked) brandAccent(accent) else scheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (unlocked) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ZcWhite,
                        modifier = Modifier.size(26.dp),
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
