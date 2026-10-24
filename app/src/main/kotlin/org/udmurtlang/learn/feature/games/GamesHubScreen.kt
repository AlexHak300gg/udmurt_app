package org.udmurtlang.learn.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.dp
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.StarGlyph
import org.udmurtlang.learn.core.designsystem.ZcBerry
import org.udmurtlang.learn.core.designsystem.ZcGold
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
    val stars: Int,
)

private val entries = listOf(
    GameEntry(GameType.CHOICE, R.string.game_choice, R.string.game_choice_sub, ZcRed, 2),
    GameEntry(GameType.ODD_ONE_OUT, R.string.game_odd, R.string.game_odd_sub, ZcPurple, 9),
    GameEntry(GameType.SORTING, R.string.game_sorting, R.string.game_sorting_sub, ZcTeal, 2),
    GameEntry(GameType.MEMORY, R.string.game_memory, R.string.game_memory_sub, ZcOrange, 15),
    GameEntry(GameType.WORD_BUILDER, R.string.game_builder, R.string.game_builder_sub, ZcPurpleTile, 9),
    GameEntry(GameType.TRUE_FALSE, R.string.game_truefalse, R.string.game_tf_sub, ZcBerry, 3),
)

@Composable
fun GamesHubScreen(
    onBack: () -> Unit,
    onOpenGame: (String) -> Unit,
) {
    val brand = LocalBrand.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        ZcHeader(
            title = stringResource(R.string.games_title),
            caption = stringResource(R.string.games_hint),
            onBack = onBack,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        ) {
            items(entries, key = { it.type.slug }) { entry ->
                GameCard(
                    title = stringResource(entry.titleRes),
                    caption = stringResource(entry.captionRes),
                    color = entry.color,
                    patterned = !brand.isDark,
                    stars = entry.stars,
                    onClick = { onOpenGame(entry.type.slug) },
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    title: String,
    caption: String,
    color: Color,
    patterned: Boolean,
    stars: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        Box(modifier = Modifier.background(color)) {
            if (patterned) {
                org.udmurtlang.learn.core.designsystem.StarPattern(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    alpha = 0.10f,
                    cell = 56.dp,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StarGlyph(
                        modifier = Modifier.size(18.dp),
                        color = Color.White.copy(alpha = 0.55f),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    BadgeStarCount(stars = stars)
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                    )
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
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
