package org.udmurtlang.learn.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Фирменные орнаменты из брендбука «Столетие Удмуртии».
 * Пути перенесены 1:1 из макета в Figma (SVG viewBox каждого знака).
 */
enum class UdmOrnament(val w: Float, val h: Float, internal val data: List<String>) {
    /** Звезда с вырезом-ромбом. */
    STAR(
        72f, 72f,
        listOf(
            "M44.9551 14.3809L61.4561 10.5439L57.6191 27.0449L72 36L57.6191 44.9551L61.4561 61.4561L44.9551 57.6191L36 72L27.0449 57.6191L10.5439 61.4561L14.3809 44.9551L0 36L14.3809 27.0449L10.5439 10.5439L27.0449 14.3809L36 0L44.9551 14.3809ZM28.252 29.252L15 36.5L28.252 43.748L35.5 57L42.748 43.748L56 36.5L42.748 29.252L35.5 16L28.252 29.252Z",
        ),
    ),

    /** Крест из ромбов. */
    CROSS(
        74f, 74f,
        listOf(
            "M50.0205 13.5107L38.5205 25.0098L48.0098 34.5L59.5107 23L73.0205 36.5107L59.5107 50.0205L48.0098 38.5205L38.5205 48.0098L50.0205 59.5107L36.5107 73.0205L23 59.5107L34.5 48.0098L25.0098 38.5205L13.5107 50.0205L0 36.5107L13.5107 23L25.0098 34.5L34.5 25.0098L23 13.5107L36.5107 0L50.0205 13.5107Z",
        ),
    ),

    /** Шестиугольник со звездой внутри. */
    HEX(
        63f, 72f,
        listOf(
            "M62.3535 18V54L31.1768 72L0 54V18L31.1768 0L62.3535 18ZM26.3291 11.4785C25.1354 21.7834 16.9602 29.9587 6.65527 31.1523V40.8477C16.9604 42.0413 25.1357 50.2172 26.3291 60.5225H37.0684C38.2213 50.5673 45.8892 42.5992 55.6992 40.9932V31.0059C45.8895 29.3998 38.2215 21.4333 37.0684 11.4785H26.3291Z",
            "M31.6987 19.8263C35.0853 26.5996 40.5775 32.0918 47.3509 35.4785C40.5775 38.8651 35.0853 44.3573 31.6987 51.1306C28.312 44.3573 22.8198 38.8651 16.0465 35.4785C22.8198 32.0918 28.312 26.5996 31.6987 19.8263Z",
        ),
    ),

    /** Вертушка из шевронов. */
    PINWHEEL(
        75f, 75f,
        listOf(
            "M53.5 17L37.5 33L21.5 17V0L37.5 16L53.5 0V17Z",
            "M21.5 58L37.5 42L53.5 58L53.5 75L37.5 59L21.5 75L21.5 58Z",
            "M58 53.5L42 37.5L58 21.5L75 21.5L59 37.5L75 53.5L58 53.5Z",
            "M17 21.5L33 37.5L17 53.5L0 53.5L16 37.5L0 21.5L17 21.5Z",
        ),
    ),

    /** Круг-дуги с ромбом. */
    ARC(
        68f, 70f,
        listOf(
            "M53.7637 36.083C57.8586 38.3384 63.3637 41.4065 67.6543 43.8848C63.7221 58.9118 50.0538 70 33.7939 70C17.7093 70 4.16111 59.1494 0.0644531 44.3691L13.8438 36.4141C14.5692 46.7999 23.2237 55 33.7939 55C44.4763 55 53.2011 46.6251 53.7637 36.083ZM33.7939 0C49.9684 0 63.5777 10.9722 67.5908 25.8789L53.7607 33.8643C53.1719 23.347 44.4584 15 33.7939 15C23.2018 15 14.5339 23.2341 13.8398 33.6504C9.65978 31.3453 4.18432 28.2899 0 25.8652C4.01772 10.9654 17.6244 0 33.7939 0Z",
            "M34.229 22L47.664 35.435L34.229 48.87L20.794 35.435Z",
        ),
    ),

    /** Стрелки-треугольники. */
    ARROWS(
        71f, 73f,
        listOf(
            "M19.5 36.5L0 17V56L19.5 36.5Z",
            "M51 36.5L70.5 56L70.5 17L51 36.5Z",
            "M33 19.5L52.5 39L52.5 0L33 19.5Z",
            "M33 53.5L52.5 73L52.5 34L33 53.5Z",
            "M37.5 19.5L18 0L18 39L37.5 19.5Z",
            "M37.5 53.5L18 34L18 73L37.5 53.5Z",
        ),
    ),
    ;

    internal val paths: List<Path> by lazy { data.map { PathParser().parsePathString(it).toPath() } }
}

/** Знак-логотип приложения: звезда с вертушкой из шевронов (из макета). */
private const val LOGO_BOX = 150f
private val logoStarPath: Path by lazy {
    PathParser().parsePathString(
        "M75.5 11L88.6471 34.0373L112.824 23.1274L109.92 49.4927L135.892 54.8774L118.045 74.5L135.892 94.1226L109.92 99.5073L112.824 125.873L88.6471 114.963L75.5 138L62.3529 114.963L38.1756 125.873L41.0804 99.5073L15.1079 94.1226L32.955 74.5L15.1079 54.8774L41.0804 49.4927L38.1756 23.1274L62.3529 34.0373L75.5 11Z",
    ).toPath()
}
private val logoMarkPaths: List<Path> by lazy {
    listOf(
        "M89.7933 56.1867L75.5 70.48L61.2067 56.1867V41L75.5 55.2933L89.7933 41V56.1867Z",
        "M61.2067 92.8133L75.5 78.52L89.7933 92.8133L89.7933 108L75.5 93.7067L61.2067 108L61.2067 92.8133Z",
        "M93.8133 88.7933L79.52 74.5L93.8133 60.2067L109 60.2067L94.7067 74.5L109 88.7933L93.8133 88.7933Z",
        "M57.1867 60.2067L71.48 74.5L57.1867 88.7933L42 88.7933L56.2933 74.5L42 60.2067L57.1867 60.2067Z",
    ).map { PathParser().parsePathString(it).toPath() }
}

/** Рисует орнамент, вписав его по центру квадрата со стороной [side] вокруг [center]. */
fun DrawScope.drawOrnament(kind: UdmOrnament, center: Offset, side: Float, color: Color) {
    val k = side / max(kind.w, kind.h)
    translate(left = center.x - kind.w * k / 2f, top = center.y - kind.h * k / 2f) {
        scale(scale = k, pivot = Offset.Zero) {
            kind.paths.forEach { drawPath(it, color = color) }
        }
    }
}

private fun DrawScope.drawLogo(center: Offset, side: Float, star: Color, mark: Color) {
    val k = side / LOGO_BOX
    translate(left = center.x - LOGO_BOX * k / 2f, top = center.y - LOGO_BOX * k / 2f) {
        scale(scale = k, pivot = Offset.Zero) {
            drawPath(logoStarPath, color = star)
            if (mark.alpha > 0f) logoMarkPaths.forEach { drawPath(it, color = mark) }
        }
    }
}

/** Один фирменный орнамент. */
@Composable
fun Ornament(
    kind: UdmOrnament,
    modifier: Modifier = Modifier,
    color: Color = ZcRed,
) {
    Canvas(modifier = modifier) {
        drawOrnament(kind, Offset(size.width / 2f, size.height / 2f), size.minDimension, color)
    }
}

/**
 * Главный знак приложения — звезда из логотипа.
 * [markColor] — цвет вертушки внутри (обычно цвет фона); прозрачный — только звезда.
 */
@Composable
fun StarMark(
    modifier: Modifier = Modifier,
    color: Color = ZcRed,
    size: Dp = 88.dp,
    markColor: Color = Color.Transparent,
) {
    Canvas(modifier = modifier.size(size)) {
        drawLogo(
            center = Offset(this.size.width / 2f, this.size.height / 2f),
            side = this.size.minDimension,
            star = color,
            mark = markColor,
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
 * Полоса орнамента: шесть фирменных знаков по кругу.
 * [colors] — если задано несколько цветов, знаки красятся по очереди.
 */
@Composable
fun OrnamentBand(
    modifier: Modifier = Modifier,
    color: Color = ZcRed,
    height: Dp = 18.dp,
    diamondSize: Dp = 14.dp,
    colors: List<Color> = emptyList(),
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val side = min(diamondSize.toPx(), this.size.height)
        val step = side * 1.6f
        val count = max(1, (this.size.width / step).toInt())
        val startX = (this.size.width - (count - 1) * step) / 2f
        val all = UdmOrnament.entries
        for (i in 0 until count) {
            val c = if (colors.isEmpty()) color else colors[i % colors.size]
            drawOrnament(all[i % all.size], Offset(startX + i * step, this.size.height / 2f), side, c)
        }
    }
}

/** Негромкий фон-паттерн из фирменных орнаментов для цветных экранов. */
@Composable
fun StarPattern(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    alpha: Float = 0.07f,
    cell: Dp = 96.dp,
) {
    Canvas(modifier = modifier) {
        val step = cell.toPx()
        val side = step * 0.42f
        val all = UdmOrnament.entries
        var y = step / 2f
        var row = 0
        while (y < this.size.height + step) {
            var x = step / 2f + if (row % 2 == 0) 0f else step / 2f
            var col = 0
            while (x < this.size.width + step) {
                drawOrnament(all[(row * 2 + col) % all.size], Offset(x, y), side, color.copy(alpha = alpha))
                x += step
                col++
            }
            y += step
            row++
        }
    }
}

/**
 * Рваный треугольный край, как на баннерах брендбука.
 * Зубцы смотрят вниз ([pointDown] = true) — ставится под цветной шапкой того же цвета.
 */
@Composable
fun ZigzagEdge(
    modifier: Modifier = Modifier,
    color: Color = ZcRed,
    tooth: Dp = 24.dp,
    depth: Dp = 12.dp,
    pointDown: Boolean = true,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(depth),
    ) {
        val t = tooth.toPx()
        val h = this.size.height
        val path = Path()
        if (pointDown) {
            path.moveTo(0f, 0f)
            var x = 0f
            while (x < this.size.width) {
                path.lineTo(x + t / 2f, h)
                path.lineTo(x + t, 0f)
                x += t
            }
        } else {
            path.moveTo(0f, h)
            var x = 0f
            while (x < this.size.width) {
                path.lineTo(x + t / 2f, 0f)
                path.lineTo(x + t, h)
                x += t
            }
        }
        path.close()
        drawPath(path, color = color)
    }
}

/** Маленькая звёздочка для очков и рекордов. */
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
