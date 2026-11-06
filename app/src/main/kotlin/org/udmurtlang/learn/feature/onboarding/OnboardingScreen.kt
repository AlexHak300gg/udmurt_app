package org.udmurtlang.learn.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.AdaptivePanes
import org.udmurtlang.learn.core.designsystem.ReadingContentWidth
import org.udmurtlang.learn.core.designsystem.ScrollableContent
import org.udmurtlang.learn.core.designsystem.WideContentWidth
import org.udmurtlang.learn.core.designsystem.currentWindowClass
import org.udmurtlang.learn.core.designsystem.isShortWindow
import org.udmurtlang.learn.core.designsystem.isWide
import org.udmurtlang.learn.core.designsystem.ZigzagEdge
import org.udmurtlang.learn.core.designsystem.UdmOrnament
import org.udmurtlang.learn.core.designsystem.Ornament
import androidx.compose.foundation.layout.offset
import org.udmurtlang.learn.core.designsystem.ZcInk
import org.udmurtlang.learn.core.designsystem.ZcOrange
import org.udmurtlang.learn.core.designsystem.ZcPurpleDeep
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.core.designsystem.components.GameButton
import org.udmurtlang.learn.data.local.AppDataStore
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: AppDataStore,
) : ViewModel() {
    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            dataStore.setOnboardingDone()
            onDone()
        }
    }
}

private data class Page(
    val bg: Color,
    val emoji: String,
    val bigEmojiAndWord: Boolean,
    val titleRes: Int,
    val textRes: Int,
)

private val pages = listOf(
    Page(ZcTeal, "🐻", true, R.string.onboarding_title_1, R.string.onboarding_text_1),
    Page(ZcPurpleDeep, "🎮", false, R.string.onboarding_title_2, R.string.onboarding_text_2),
    Page(ZcOrange, "🔥", true, R.string.onboarding_title_3, R.string.onboarding_text_3),
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val short = isShortWindow()
    val wide = currentWindowClass().isWide

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { index ->
            OnboardingPage(page = pages[index], wide = wide, short = short)
        }
        Column(
            modifier = Modifier
                .widthIn(max = ReadingContentWidth)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = if (short) 4.dp else 12.dp),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = if (short) 2 else 1,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                GameButton(
                    text = stringResource(
                        if (pagerState.currentPage == pages.lastIndex) R.string.start else R.string.next,
                    ),
                    onClick = {
                        if (pagerState.currentPage == pages.lastIndex) {
                            viewModel.complete(onFinished)
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { viewModel.complete(onFinished) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        stringResource(R.string.skip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (selected && !short) 18.dp else 10.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) ZcRed
                                else MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    )
                    if (index < pages.lastIndex) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: Page, wide: Boolean, short: Boolean) {
    if (wide || short) {
        ScrollableContent(
            modifier = Modifier.fillMaxSize(),
            maxWidth = WideContentWidth,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            centerVertically = true,
        ) {
            AdaptivePanes(
                sideBySide = wide,
                spacing = 8.dp,
                first = {
                    OnboardingArtwork(
                        page = page,
                        short = short,
                        modifier = Modifier.heightIn(min = if (short) 152.dp else 280.dp)
                            .clip(RoundedCornerShape(28.dp)),
                    )
                },
                second = { OnboardingCopy(page) },
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingArtwork(page, short = false, modifier = Modifier.weight(1f))
            ZigzagEdge(color = page.bg, tooth = 24.dp, depth = 12.dp)
            OnboardingCopy(page)
        }
    }
}

@Composable
private fun OnboardingArtwork(page: Page, short: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().background(page.bg),
        contentAlignment = Alignment.Center,
    ) {
        Ornament(
            kind = UdmOrnament.STAR,
            color = ZcWhite.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-30).dp, y = 40.dp)
                .size(130.dp),
        )
        Ornament(
            kind = UdmOrnament.HEX,
            color = ZcWhite.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = 60.dp)
                .size(90.dp),
        )
        Ornament(
            kind = UdmOrnament.CROSS,
            color = ZcWhite.copy(alpha = 0.18f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-24).dp)
                .size(90.dp),
        )
        Box(
            modifier = Modifier
                .widthIn(min = if (short) 124.dp else 180.dp)
                .heightIn(min = if (short) 124.dp else 180.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(ZcWhite)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (page.bigEmojiAndWord) {
                    Text(page.emoji, fontSize = if (short) 48.sp else 80.sp)
                    if (page.emoji == "🔥") {
                        Surface(shape = CircleShape, color = ZcInk) {
                            Text(
                                text = stringResource(R.string.onboarding_streak),
                                fontSize = 13.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    } else {
                        Text(
                            text = "Гондыр",
                            style = MaterialTheme.typography.titleLarge,
                            color = ZcInk,
                        )
                        Text(
                            text = "(медведь)",
                            fontSize = 14.sp,
                            color = Color(0xFF6E6A66),
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStarTile(kind = UdmOrnament.STAR, star = ZcRed, short = short)
                        MiniStarTile(kind = UdmOrnament.CROSS, star = ZcOrange, short = short)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStarTile(kind = UdmOrnament.HEX, star = ZcPurpleDeep, short = short)
                        MiniStarTile(kind = UdmOrnament.PINWHEEL, star = ZcTeal, short = short)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingCopy(page: Page) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 8.dp),
    ) {
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(page.textRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
@Composable
private fun MiniStarTile(kind: UdmOrnament, star: Color, short: Boolean) {
    Box(
        modifier = Modifier
            .size(if (short) 40.dp else 54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(star.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Ornament(kind = kind, color = star, modifier = Modifier.size(30.dp))
    }
}
