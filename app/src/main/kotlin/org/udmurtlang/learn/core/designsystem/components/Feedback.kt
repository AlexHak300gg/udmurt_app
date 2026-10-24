package org.udmurtlang.learn.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.OrnamentBand
import org.udmurtlang.learn.core.designsystem.ZcGold
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcOrange
import kotlin.math.cos
import kotlin.math.sin

/** Кольцо прогресса. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    label: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 8.dp,
    labelColor: Color? = null,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2f
            val arcSize = Size(this.size.width - stroke.width, this.size.height - stroke.width)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke,
            )
        }
        if (label != null) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = labelColor ?: MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Значок серии дней (огонёк + счётчик) на коралловой таблетки. */
@Composable
fun StreakBadge(
    days: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = CircleShape
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("🔥", fontSize = MaterialTheme.typography.labelLarge.fontSize)
            Text(
                text = "$days",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
            )
        }
    }
    if (onClick != null) {
        Surface(
            modifier = modifier,
            onClick = onClick,
            shape = shape,
            color = ZcRed,
            contentColor = Color.White,
            shadowElevation = 2.dp,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = ZcRed,
            contentColor = Color.White,
            shadowElevation = 2.dp,
            content = content,
        )
    }
}

/** Конфетти поверх экрана при успехе. */
@Composable
fun ConfettiOverlay(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!active) return
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "confettiProgress",
    )
    val palette = listOf(ZcRed, ZcTeal, ZcGold, ZcPurple, ZcOrange)
    Canvas(modifier = modifier.fillMaxSize()) {
        val piece = 14.dp.toPx()
        repeat(28) { i ->
            val seed = i * 0.618f
            val x = ((seed * 137.5f) % 1f) * this.size.width
            val fall = (progress + seed) % 1f
            val y = fall * this.size.height
            val angle = (fall * 720f + i * 25f) * (Math.PI / 180.0).toFloat()
            val cx = x + sin(angle) * 18.dp.toPx()
            drawCircle(
                color = palette[i % palette.size],
                radius = piece / 2f,
                center = Offset(cx, y),
                alpha = 1f - fall * 0.5f,
            )
        }
    }
}

/** Пустое состояние с звёздочкой-заглушкой. */
@Composable
fun EmptyState(
    title: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("⭐", fontSize = MaterialTheme.typography.displayLarge.fontSize)
        Text(
            text = stringResource(R.string.loading),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OrnamentBand(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
