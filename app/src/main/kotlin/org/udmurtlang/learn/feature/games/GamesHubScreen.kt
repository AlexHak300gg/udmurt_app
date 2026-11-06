package org.udmurtlang.learn.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.WindowClass
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.gridSpacing
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.StarGlyph
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.ZcGold
import org.udmurtlang.learn.core.designsystem.Ornament
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcPurpleTile
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.components.ZcHeader
import org.udmurtlang.learn.domain.model.GameType

private data class GameEntry(
    val type: GameType,
    val titleRes: Int,
    val captionRes: Int,
    val color: Color,
    val ornament: UdmOrnament,
)

private val entries = listOf(
    GameEntry(GameType.CHOICE, R.string.game_choice, R.string.game_choice_sub, ZcRed, UdmOrnament.HEX),
    GameEntry(GameType.ODD_ONE_OUT, R.string.game_odd, R.string.game_odd_sub, ZcPurple, UdmOrnament.STAR),
    GameEntry(GameType.SORTING, R.string.game_sorting, R.string.game_sorting_sub, ZcTeal, UdmOrnament.ARC),
    GameEntry(GameType.MEMORY, R.string.game_memory, R.string.game_memory_sub, ZcOrange, UdmOrnament.PINWHEEL),
    GameEntry(GameType.WORD_BUILDER, R.string.game_builder, R.string.game_builder_sub, ZcPurple, UdmOrnament.CROSS),
    GameEntry(GameType.TRUE_FALSE, R.string.game_truefalse, R.string.game_tf_sub, ZcRed, UdmOrnament.ARROWS),
)

@Composable
fun GamesHubScreen(
    onBack: () -> Unit,
    onOpenGame: (String) -> Unit,
    starsByGame: Map<GameType, Int> = emptyMap(),
) {
    val brand = LocalBrand.current
    val window = currentWindowClass()
    val short = isShortWindow()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ZcHeader(
            title = stringResource(R.string.games_title),
            caption = stringResource(R.string.games_hint),
            onBack = onBack,
            modifier = Modifier.widthIn(max = WideContentWidth),
        )
        BoxWithConstraints(
            modifier = Modifier.weight(1f).widthIn(max = WideContentWidth).fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            val columns = if (window == WindowClass.EXPANDED || short && window.isWide) 3 else 2
            val rows = (entries.size + columns - 1) / columns
            val spacing = window.gridSpacing
            val bottomPadding = 16.dp
            val rowHeight = (maxHeight - bottomPadding - spacing * (rows - 1)) / rows
            val minHeight = rowHeight.coerceIn(if (short) 102.dp else 148.dp, if (window.isWide) 210.dp else 158.dp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                contentPadding = PaddingValues(bottom = bottomPadding),
            ) {
                items(entries, key = { it.type.slug }) { entry ->
                    GameCard(
                        ornament = entry.ornament,
                        title = stringResource(entry.titleRes),
                        caption = stringResource(entry.captionRes),
                        color = brandAccent(entry.color),
                        patterned = !brand.isDark,
                        stars = starsByGame[entry.type] ?: 0,
                        minHeight = minHeight,
                        short = short,
                        onClick = { onOpenGame(entry.type.slug) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCard(
    ornament: UdmOrnament,
    title: String,
    caption: String,
    color: Color,
    patterned: Boolean,
    stars: Int,
    minHeight: Dp,
    short: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.background(color)) {
            // Макет: один крупный фирменный орнамент в правом верхнем углу
            Ornament(
                kind = ornament,
                color = Color.White.copy(alpha = if (patterned) 0.16f else 0.10f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 26.dp, y = (-20).dp)
                    .size(104.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .padding(if (short) 12.dp else 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                BadgeStarCount(stars = stars)
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeStarCount(stars: Int) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StarGlyph(size = 12.dp, color = ZcGold)
        Text("$stars", style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}
