package org.udmurtlang.learn.feature.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.Ornament
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.ZcInk
import org.udmurtlang.learn.core.designsystem.ZcRedDark
import org.udmurtlang.learn.core.designsystem.ZigzagEdge
import org.udmurtlang.learn.core.designsystem.ZcRedOnDark
import org.udmurtlang.learn.core.designsystem.ZcPurpleDeep
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.components.SoundButton
import org.udmurtlang.learn.core.designsystem.components.StreakBadge
import org.udmurtlang.learn.domain.model.DailyActivity
import org.udmurtlang.learn.domain.model.Streak
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MenuScreen(
    onOpenLearning: () -> Unit,
    onOpenGames: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenWordOfDay: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    val brand = LocalBrand.current
    val wide = currentWindowClass().isWide
    val short = isShortWindow()
    var showCalendar by remember { mutableStateOf(false) }

    if (showCalendar) {
        StreakCalendarDialog(
            monthActivity = state.monthActivity,
            streak = state.streak,
            onDismiss = { showCalendar = false },
        )
    }

    // Макет: коралловая шапка с зубцами, белая карточка «Слово дня» заходит на шапку
    val headerColor = if (brand.isDark) ZcRedDark else ZcRed
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor),
        ) {
            Ornament(
                kind = UdmOrnament.HEX,
                color = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-10).dp)
                    .size(150.dp),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = WideContentWidth)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = if (wide) 18.dp else 64.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(
                            R.string.menu_greeting,
                            state.profile?.displayName ?: stringResource(R.string.guest_label),
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    StreakBadge(
                        days = state.streak.current,
                        container = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            showCalendar = true
                            viewModel.loadMonthActivity()
                        },
                    )
                }
                Text(
                    text = stringResource(R.string.menu_sub),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        ZigzagEdge(color = headerColor, tooth = 22.dp, depth = 11.dp)

        AdaptivePanes(
            modifier = Modifier
                .widthIn(max = WideContentWidth)
                .offset(y = if (wide) 0.dp else (-58).dp)
                .padding(horizontal = 16.dp, vertical = if (wide) 12.dp else 0.dp),
            sideBySide = wide,
            firstWeight = 0.85f,
            secondWeight = 1.15f,
            spacing = if (wide) 20.dp else 0.dp,
            verticalAlignment = Alignment.Top,
            first = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.wordOfDay?.let { wod ->
                        Surface(
                            onClick = onOpenWordOfDay,
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(wod.word.emoji, fontSize = 32.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.word_of_day),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (brand.isDark) ZcRedOnDark else ZcRed,
                                    )
                                    Text(
                                        text = wod.word.udm,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = wod.word.ru,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                SoundButton(
                                    onClick = onOpenWordOfDay,
                                    container = MaterialTheme.colorScheme.inverseSurface,
                                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                                )
                            }
                        }
                    }
                    if (wide) {
                        MenuSettingsTile(onClick = onOpenSettings)
                        FactsCarousel()
                    }
                }
            },
            second = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.menu_select),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = if (wide) 0.dp else 18.dp, bottom = 10.dp),
                    )

                    // Игры — большая фиолетовая карточка
                    Surface(
                        onClick = onOpenGames,
                        shape = RoundedCornerShape(28.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier.background(
                                brandAccent(ZcPurple),
                            ),
                        ) {
                            Ornament(
                                kind = UdmOrnament.PINWHEEL,
                                color = Color.White.copy(alpha = 0.16f),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(x = 28.dp)
                                    .size(120.dp),
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MenuGlyph(Icons.Outlined.SportsEsports)
                                Spacer(Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.menu_games),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 20.sp,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = stringResource(R.string.menu_tiles_games),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f),
                                    )
                                }
                                Spacer(modifier = Modifier.size(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MenuTile(
                            title = stringResource(R.string.menu_learning),
                            caption = stringResource(R.string.menu_tiles_learning),
                            icon = Icons.Outlined.Book,
                            color = ZcTeal,
                            onClick = onOpenLearning,
                            modifier = Modifier.weight(1f),
                            ornament = UdmOrnament.CROSS,
                            short = short,
                        )
                        MenuTile(
                            title = stringResource(R.string.menu_profile),
                            caption = stringResource(R.string.menu_tiles_profile),
                            icon = Icons.Outlined.PersonOutline,
                            color = ZcOrange,
                            onClick = onOpenProfile,
                            modifier = Modifier.weight(1f),
                            ornament = UdmOrnament.ARC,
                            short = short,
                        )
                    }

                    if (!wide) {
                        Spacer(modifier = Modifier.height(12.dp))
                        MenuSettingsTile(onClick = onOpenSettings)
                        Spacer(modifier = Modifier.height(18.dp))
                        FactsCarousel()
                    }
                }
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MenuSettingsTile(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = stringResource(R.string.menu_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MenuTile(
    title: String,
    caption: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ornament: UdmOrnament = UdmOrnament.STAR,
    short: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(brandAccent(color))
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Ornament(
            kind = ornament,
            color = Color.White.copy(alpha = 0.16f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-36).dp)
                .size(110.dp),
        )
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = if (short) 72.dp else 96.dp),
        ) {
            MenuGlyph(icon, short)
            Spacer(Modifier.height(8.dp).weight(1f))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun MenuGlyph(icon: ImageVector, short: Boolean = false) {
    Box(
        modifier = Modifier.size(if (short) 34.dp else 46.dp)
            .clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.20f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun StreakCalendarDialog(
    monthActivity: List<DailyActivity>,
    streak: Streak,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.current_streak),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "🔥 ${streak.current} ${stringResource(R.string.streak_days)} · " +
                        "${stringResource(R.string.best_streak)}: ${streak.best}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(14.dp))
                CalendarMonthGrid(monthActivity = monthActivity, streak = streak)
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    monthActivity: List<DailyActivity>,
    streak: Streak,
) {
    val month = remember { YearMonth.now() }
    val today = remember { LocalDate.now() }
    val monthTitle = remember(month) {
        month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault()))
            .replaceFirstChar { it.uppercase(Locale.getDefault()) }
    }
    val activeDays = monthActivity
        .filter { it.gamesPlayed > 0 || it.wordsLearned > 0 }
        .map { it.day }
        .toSet()
        .let { fromDaily ->
            val end = streak.lastActiveDay.takeIf { it.isNotBlank() }
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (end != null && streak.current > 0) {
                fromDaily + (0 until streak.current).map { end.minusDays(it.toLong()).toString() }.toSet()
            } else {
                fromDaily
            }
        }

    Text(
        text = monthTitle,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..7).forEach { isoDay ->
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = DayOfWeek.of(isoDay).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    val leadingEmpty = month.atDay(1).dayOfWeek.value - 1
    val cells: List<LocalDate?> = List(leadingEmpty) { null } +
        (1..month.lengthOfMonth()).map { month.atDay(it) }
    cells.chunked(7).forEach { week ->
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            week.forEach { day ->
                if (day == null) {
                    Box(modifier = Modifier.size(36.dp))
                } else {
                    CalendarDayCell(
                        day = day,
                        today = today,
                        active = day.toString() in activeDays,
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: LocalDate,
    today: LocalDate,
    active: Boolean,
) {
    val isToday = day == today
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (active) ZcRed else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .then(
                if (isToday) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${day.dayOfMonth}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
            color = when {
                active -> Color.White
                day.isAfter(today) -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
    }
}
