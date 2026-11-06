package org.udmurtlang.learn.core.designsystem

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.suspendCancellableCoroutine
import org.udmurtlang.learn.domain.model.ThemeMode
import org.udmurtlang.learn.domain.model.UiLanguage
import kotlin.coroutines.resume
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

sealed interface AppearanceSelection {
    data class Theme(val mode: ThemeMode, val centerInWindow: Offset?) : AppearanceSelection
    data class Language(val language: UiLanguage) : AppearanceSelection
}

internal data class AppearanceFrame(val image: ImageBitmap, val revealCenter: Offset?)

internal suspend fun captureAppearance(
    window: Window,
    bounds: Rect,
    revealFrom: Offset?,
): AppearanceFrame? {
    val source = android.graphics.Rect(
        bounds.left.roundToInt(), bounds.top.roundToInt(),
        bounds.right.roundToInt(), bounds.bottom.roundToInt(),
    )
    if (source.isEmpty) return null
    val center = revealFrom?.let {
        Offset(
            ((it.x - source.left) / source.width()).coerceIn(0f, 1f),
            ((it.y - source.top) / source.height()).coerceIn(0f, 1f),
        )
    }
    val scale = sqrt(4_000_000.0 / (source.width().toDouble() * source.height())).coerceAtMost(1.0)
    return suspendCancellableCoroutine { continuation ->
        val image = Bitmap.createBitmap(
            (source.width() * scale).roundToInt().coerceAtLeast(1),
            (source.height() * scale).roundToInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        try {
            PixelCopy.request(window, source, image, { result ->
                if (continuation.isActive) {
                    continuation.resume(
                        if (result == PixelCopy.SUCCESS) AppearanceFrame(image.asImageBitmap(), center) else null,
                    )
                }
            }, Handler(Looper.getMainLooper()))
        } catch (_: IllegalArgumentException) {
            continuation.resume(null)
        }
    }
}

@Composable
internal fun AppearanceOverlay(frame: AppearanceFrame, progress: () -> Float) {
    Canvas(Modifier.fillMaxSize()) {
        val fraction = progress()
        if (fraction >= 1f) return@Canvas
        val destination = IntSize(size.width.roundToInt(), size.height.roundToInt())
        val center = frame.revealCenter
        if (center == null) {
            translate(top = -8.dp.toPx() * fraction) {
                drawImage(frame.image, dstSize = destination, alpha = 1f - fraction)
            }
        } else if (fraction == 0f) {
            drawImage(frame.image, dstSize = destination)
        } else {
            val origin = Offset(center.x * size.width, center.y * size.height)
            val radius = hypot(
                maxOf(origin.x, size.width - origin.x),
                maxOf(origin.y, size.height - origin.y),
            ) * fraction
            val circle = Path().apply { addOval(Rect(origin, radius)) }
            clipPath(circle, ClipOp.Difference) {
                drawImage(frame.image, dstSize = destination)
            }
        }
    }
}
