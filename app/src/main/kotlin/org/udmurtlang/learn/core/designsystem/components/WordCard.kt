package org.udmurtlang.learn.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.Ornament
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.studyCardColor
import org.udmurtlang.learn.domain.model.Word

/**
 * Карточка слова в стиле макета: белый скруглённый квадрат — эмодзи,
 * слово крупно, транскрипция в скобках, подпись «(перевод)» и перевод.
 */
@Composable
fun WordCard(
    word: Word,
    modifier: Modifier = Modifier,
    onSpeak: (() -> Unit)? = null,
    speakLabel: String = "Прослушать",
    translationLabel: String = "Перевод",
    pronunciationLabel: String = "Произношение",
    exampleLabel: String = "Пример",
    sideBySide: Boolean = false,
    minHeight: Dp = 0.dp,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = studyCardColor()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .then(
                    if (onSpeak != null) {
                        Modifier.clickable(onClick = onSpeak)
                    } else {
                        Modifier
                    }
                ),
        ) {
            AdaptivePanes(
                sideBySide = sideBySide,
                firstWeight = 0.55f,
                secondWeight = 1.45f,
                spacing = if (sideBySide) 12.dp else 4.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 20.dp, end = 20.dp, top = if (onSpeak != null) 48.dp else 20.dp, bottom = 20.dp),
                first = { Text(word.emoji, fontSize = if (sideBySide) 62.sp else 84.sp) },
                second = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = word.udm,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "(${word.transcription})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            text = translationLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 14.dp),
                        )
                        Text(
                            text = word.ru,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
            if (minHeight >= 320.dp) {
                Ornament(
                    UdmOrnament.STAR,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 30.dp).size(50.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                )
            }
            if (onSpeak != null) {
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = speakLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun WordRow(word: Word, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(word.emoji, fontSize = 20.sp)
        }
        Column {
            Text(word.udm, style = MaterialTheme.typography.titleSmall)
            Text(
                text = word.ru,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
