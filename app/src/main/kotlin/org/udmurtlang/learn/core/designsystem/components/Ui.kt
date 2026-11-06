package org.udmurtlang.learn.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.udmurtlang.learn.core.designsystem.isShortWindow
import androidx.compose.ui.unit.sp
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.OrnamentBand
import org.udmurtlang.learn.core.designsystem.StarGlyph
import org.udmurtlang.learn.core.designsystem.ZcGold
import org.udmurtlang.learn.core.designsystem.ZcInkPill
import org.udmurtlang.learn.core.designsystem.ZcOrangeBottom
import org.udmurtlang.learn.core.designsystem.ZcOrangeTop
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcPurpleDeep
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcRedBottom
import org.udmurtlang.learn.core.designsystem.ZcRedTop
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcTealDeep
import org.udmurtlang.learn.core.designsystem.chromeOnVivid
import org.udmurtlang.learn.core.designsystem.chromeOnVividText
import org.udmurtlang.learn.core.designsystem.onVivid
import org.udmurtlang.learn.core.designsystem.onVividMuted

// ---------- Заливки бренда (брендбук — плоские цвета, без градиентов) ----------

object Gradients {
    val red: Brush = Brush.verticalGradient(listOf(ZcRedTop, ZcRedBottom))
    val orange: Brush = Brush.verticalGradient(listOf(ZcOrangeTop, ZcOrangeBottom))
    val purple: Brush = Brush.verticalGradient(listOf(ZcPurple, ZcPurple))
    val teal: Brush = Brush.verticalGradient(listOf(ZcTeal, ZcTeal))
}

// ---------- Хедеры страниц ----------

/** Хедер страницы: круглая кнопка «назад» + заголовок с подписью. */
@Composable
fun ZcHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    tinted: Boolean = false,
    compact: Boolean = false,
    close: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val on = if (tinted) onVivid() else MaterialTheme.colorScheme.onBackground
    val onMuted = if (tinted) onVividMuted() else MaterialTheme.colorScheme.onSurfaceVariant
    val chip = if (tinted) chromeOnVivid().copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant
    val chipOn = if (tinted) onVivid() else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (isShortWindow() || compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            color = chip,
            contentColor = chipOn,
            modifier = Modifier.size(if (compact) 36.dp else 40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (close) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        if (compact) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                caption?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = onMuted)
                }
                Text(
                    title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = on,
                    textAlign = if (caption == null) TextAlign.Center else TextAlign.End,
                )
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = on)
                caption?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = onMuted) }
            }
        }
        trailing?.invoke(this)
    }
}

/** Малый чип поверх фона (чёрная таблетка на макете). */
@Composable
fun RoundChip(
    text: String,
    modifier: Modifier = Modifier,
    dark: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (dark) ZcInkPill else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (dark) Color.White else MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            icon?.invoke()
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Чип со звёздочкой: ⭐ 5. */
@Composable
fun StarChip(
    count: Int,
    modifier: Modifier = Modifier,
    tinted: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (tinted) chromeOnVivid().copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (tinted) onVivid() else MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            StarGlyph(size = 15.dp, color = ZcGold)
            Text("$count", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ---------- Переключатели-настройки ----------

@Composable
fun SwitchCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface)
            hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant) }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = scheme.secondary,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = if (LocalBrand.current.isDark) scheme.outline else Color(0xFFE6E6E6),
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = scheme.outline,
            ),
        )
    }
}

/** Выбор языка или темы в общей плашке, с контрастным активным сегментом. */
@Composable
fun SectionChips(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    FlowRow(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onBackground,
            modifier = Modifier.padding(end = 8.dp),
        )
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier
                    .selectableGroup()
                    .padding(3.dp)
                    .width(IntrinsicSize.Max)
                    .height(IntrinsicSize.Min),
            ) {
                items.forEachIndexed { index, item ->
                    val selected = index == selectedIndex
                    Surface(
                        selected = selected,
                        onClick = { onSelect(index) },
                        modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(21.dp),
                        color = if (selected) scheme.inverseSurface else Color.Transparent,
                        contentColor = if (selected) scheme.inverseOnSurface else scheme.onSurfaceVariant,
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                item,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- Статистика ----------

/** Тонкая линия прогресса (трек + заливка) без зазубрин. */
@Composable
fun ProgressLine(
    progress: Float,
    modifier: Modifier = Modifier,
    track: Color = MaterialTheme.colorScheme.surfaceVariant,
    fill: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 6.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(fill),
        )
    }
}

/** Белая плитка статистики: значение + подпись (+ иконка сверху). */
@Composable
fun StatTile(
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    iconTint: Color? = null,
    icon: ImageVector? = null,
    container: Color = MaterialTheme.colorScheme.surface,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = container,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = if (icon == null) 6.dp else 12.dp, vertical = 12.dp),
            horizontalAlignment = if (icon == null) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                val tint = iconTint ?: MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(12.dp))
                        .background(tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
                }
            } else if (emoji != null) {
                Text(emoji, fontSize = 22.sp)
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (icon == null) TextAlign.Center else TextAlign.Start,
            )
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (icon == null) TextAlign.Center else TextAlign.Start,
            )
        }
    }
}

/** Вертикальный столбик активности за неделю. */
@Composable
fun WeekBarChart(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    val brand = LocalBrand.current
    val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val accent = MaterialTheme.colorScheme.primary
    val bar = MaterialTheme.colorScheme.primaryContainer
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        values.forEachIndexed { index, value ->
            val isMax = value == max && value > 0
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height((value / max.toFloat() * 72).dp.coerceAtLeast(8.dp))
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isMax) accent else bar),
                    )
                }
                Text(
                    labels.getOrNull(index).orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ---------- Буквы (собери слово) ----------

enum class LetterTileKind { SLOT_EMPTY, SLOT_FILLED, CANDIDATE, CANDIDATE_USED }

@Composable
fun LetterTile(
    letter: String,
    kind: LetterTileKind,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    onClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val bg = when (kind) {
        LetterTileKind.SLOT_FILLED -> scheme.surface
        LetterTileKind.SLOT_EMPTY -> scheme.surface.copy(alpha = 0.6f)
        LetterTileKind.CANDIDATE -> ZcPurple
        LetterTileKind.CANDIDATE_USED -> ZcPurple.copy(alpha = 0.35f)
    }
    val content = when {
        kind == LetterTileKind.CANDIDATE -> Color.White
        kind == LetterTileKind.CANDIDATE_USED -> Color.White.copy(alpha = 0.5f)
        kind == LetterTileKind.SLOT_EMPTY -> scheme.onSurfaceVariant
        else -> scheme.onSurface
    }
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        contentColor = content,
        shadowElevation = if (kind == LetterTileKind.SLOT_FILLED) 2.dp else 0.dp,
        modifier = modifier.size(size),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ---------- Аватар ----------

@Composable
fun CircleAvatar(
    initial: String,
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    container: Color = ZcRed,
    content: Color = Color.White,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial.take(1).uppercase(),
            style = MaterialTheme.typography.displayLarge,
            color = content,
        )
    }
}

// ---------- Пустое место ----------

@Composable
fun Spacer16() = Box(Modifier.height(16.dp))

@Composable
fun FactBubble(
    text: String,
    modifier: Modifier = Modifier,
    brush: Brush = Gradients.teal,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StarGlyph(modifier = Modifier.size(16.dp), color = Color.White.copy(alpha = 0.9f))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                )
            }
        }
    }
}

/** Орнаментальная полоса, Convenient alias. */
@Composable
fun BottomOrnament(modifier: Modifier = Modifier, color: Color = Color.White.copy(alpha = 0.85f)) {
    OrnamentBand(modifier = modifier, color = color, height = 16.dp, diamondSize = 12.dp)
}
