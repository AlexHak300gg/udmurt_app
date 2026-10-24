package org.udmurtlang.learn.core.designsystem.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.ZcRed

/** Крупная «жвачная» кнопка с пружинным нажатием — основной вызов к действию. */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    gradient: Brush? = null,
    shape: Shape = MaterialTheme.shapes.large,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "buttonScale",
    )
    val bg = if (gradient != null) {
        Modifier.background(gradient, shape)
    } else {
        Modifier
    }
    Button(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .heightIn(min = 56.dp)
            .then(bg)
            .semantics { contentDescription = text },
        enabled = enabled,
        interactionSource = interactionSource,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (gradient != null) Color.Transparent else containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f),
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(22.dp),
                )
            }
            if (emoji != null) {
                Text(emoji, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Таблетка на кремовом фоне: белая карточка с тёмным текстом. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        interactionSource = remember { MutableInteractionSource() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier
                    .padding(end = 10.dp)
                    .size(20.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

enum class TileState { NEUTRAL, CORRECT, WRONG }

/** Плитка варианта ответа в играх: белая карточка; верный ответ — бирюза с галочкой. */
@Composable
fun ChoiceTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    state: TileState = TileState.NEUTRAL,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tileScale",
    )
    val scheme = MaterialTheme.colorScheme
    val background = when (state) {
        TileState.NEUTRAL -> scheme.surface
        TileState.CORRECT -> scheme.secondary
        TileState.WRONG -> if (LocalBrand.current.isDark) scheme.surfaceVariant else scheme.surface
    }
    val content = when (state) {
        TileState.NEUTRAL -> scheme.onSurface
        TileState.CORRECT -> Color.White
        TileState.WRONG -> scheme.error
    }
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .heightIn(min = 58.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = background,
        contentColor = content,
        shadowElevation = if (state == TileState.NEUTRAL) 3.dp else 0.dp,
        border = if (state == TileState.WRONG) BorderStroke(2.dp, scheme.error) else null,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (emoji != null) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(end = 10.dp))
            }
            Text(label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (state == TileState.CORRECT) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(20.dp),
                )
            }
        }
    }
}

/** Круглая кнопка озвучки слова (TTS). */
@Composable
fun SoundButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Прослушать",
    container: Color = Color.White,
    contentColor: Color = ZcRed,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "soundScale",
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .scale(scale)
            .semantics { this.contentDescription = contentDescription },
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = contentColor,
        shadowElevation = 2.dp,
        interactionSource = interactionSource,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.VolumeUp,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
fun OrnamentRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(modifier = modifier, content = content)
}
