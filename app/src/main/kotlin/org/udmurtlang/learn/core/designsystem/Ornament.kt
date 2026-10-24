package org.udmurtlang.learn.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
/**
 * Восьмилучевая звезда — главный знак удмуртского орнамента.
 */
@Composable
fun StarMark(
    modifier: Modifier = Modifier,
    color: Color = ZcRed,
    size: Dp = 88.dp,
) {
    Canvas(modifier = modifier) {
        drawEightStar(
            center = Offset(this.size.width / 2f, this.size.height / 2f),
            radius = this.size.minDimension / 2f,
            color = color,
        )
    }
}

private fun DrawScope.drawEightStar(center: Offset, radius: Float, color: Color) {
    val inner = radius * 0.46f
    val path = Path()
    for (i in 0 until 16) {
        val r = if (i % 2 == 0) radius else inner
        val angle = PI / 8.0 * i - PI / 2.0
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}

/**
 * Полоса орнамента: ромбики с «крестиками», как на вышивке.
 */
@Composable
fun OrnamentBand(
    modifier: Modifier = Modifier,
    color: Color = ZcRed,
    height: Dp = 18.dp,
    diamondSize: Dp = 14.dp,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val size = diamondSize.toPx()
        val midY = this.size.height / 2f
        var x = 0f
        var index = 0
        while (x < this.size.width + size) {
            when {
                index % 3 == 1 -> drawEightStar(center = Offset(x + size / 2f, midY), radius = size * 0.62f, color = color)
                index % 3 == 0 -> drawDiamond(center = Offset(x + size / 2f, midY), radius = size / 2f, color = color)
                else -> drawCross(center = Offset(x + size / 2f, midY), arm = size / 3f, color = color)
            }
            x += size * 1.55f
            index++
        }
    }
}

private fun DrawScope.drawDiamond(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius, center.y)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius, center.y)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawCross(center: Offset, arm: Float, color: Color) {
    val stroke = Stroke(width = arm / 2.2f)
    drawLine(color, Offset(center.x - arm, center.y), Offset(center.x + arm, center.y), strokeWidth = stroke.width)
    drawLine(color, Offset(center.x, center.y - arm), Offset(center.x, center.y + arm), strokeWidth = stroke.width)
}

/** Негромкий фон-паттерн из восьмилучевых звёздок — как на макете цветных экранов. */
@Composable
fun StarPattern(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    alpha: Float = 0.07f,
    cell: Dp = 96.dp,
) {
    Canvas(modifier = modifier) {
        val step = cell.toPx()
        val r = step * 0.20f
        var y = step / 2f
        var row = 0
        var rowOffset = 0f
        while (y < this.size.height + step) {
            var x = step / 2f + rowOffset
            while (x < this.size.width + step) {
                drawEightStar(center = Offset(x, y), radius = r, color = color.copy(alpha = alpha))
                drawEightStar(
                    center = Offset(x - step / 2f, y - step / 2f),
                    radius = r * 0.5f,
                    color = color.copy(alpha = alpha),
                )
                x += step
            }
            y += step
            row++
            rowOffset = if (row % 2 == 0) 0f else step / 2f
        }
    }
}

/** Маленькая звёздочка для декоративных контекстов. */
@Composable
fun StarGlyph(
    modifier: Modifier = Modifier,
    color: Color = ZcRed,
    size: Dp = 20.dp,
    filled: Boolean = true,
) {
    Canvas(modifier = modifier) {
        val r = this.size.minDimension / 2f
        if (filled) {
            drawEightStar(center = Offset(this.size.width / 2f, this.size.height / 2f), radius = r, color = color)
        } else {
            val path = Path()
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val inner = r * 0.46f
            for (i in 0 until 16) {
                val rr = if (i % 2 == 0) r * 0.86f else inner
                val angle = PI / 8.0 * i - PI / 2.0
                val x = cx + (rr * cos(angle)).toFloat()
                val y = cy + (rr * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = color, style = Stroke(width = r * 0.14f))
        }
    }
}
