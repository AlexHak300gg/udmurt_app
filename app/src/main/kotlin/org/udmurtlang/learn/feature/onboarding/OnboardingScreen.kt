package org.udmurtlang.learn.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import org.udmurtlang.learn.core.designsystem.safeContentTopPadding
import org.udmurtlang.learn.core.designsystem.brandAccent
import org.udmurtlang.learn.core.designsystem.studyCardColor
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
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .width(if (selected) 22.dp else 8.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) brandAccent(ZcRed)
                                else MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    )
                    if (index < pages.lastIndex) Spacer(modifier = Modifier.width(8.dp))
                }
            }
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
        }
    }
}

@Composable
private fun OnboardingPage(page: Page, wide: Boolean, short: Boolean) {
    if (wide || short) {
        ScrollableContent(
            modifier = Modifier.fillMaxSize().safeContentTopPadding(),
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
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val artworkHeight = maxHeight * 0.69f
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                OnboardingArtwork(page, short = false, modifier = Modifier.height(artworkHeight))
                ZigzagEdge(color = brandAccent(page.bg), tooth = 24.dp, depth = 12.dp)
                OnboardingCopy(page)
            }
        }
    }
}

@Composable
private fun OnboardingArtwork(page: Page, short: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().background(brandAccent(page.bg)),
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
        when {
            !page.bigEmojiAndWord -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStarTile(UdmOrnament.STAR, ZcRed, short)
                    MiniStarTile(UdmOrnament.CROSS, ZcOrange, short)
                    MiniStarTile(UdmOrnament.HEX, ZcTeal, short)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStarTile(UdmOrnament.PINWHEEL, ZcPurpleDeep, short)
                    MiniStarTile(UdmOrnament.ARC, ZcRed, short)
                    MiniStarTile(UdmOrnament.ARROWS, ZcOrange, short)
                }
            }
            page.emoji == "🔥" -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = studyCardColor()) {
                    Box(Modifier.size(if (short) 100.dp else 170.dp), contentAlignment = Alignment.Center) {
                        Text(page.emoji, fontSize = if (short) 48.sp else 68.sp)
                    }
                }
                Surface(shape = CircleShape, color = ZcInk) {
                    Text(
                        stringResource(R.string.onboarding_streak),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                }
            }
            else -> Surface(shape = RoundedCornerShape(32.dp), color = studyCardColor()) {
                Column(
                    modifier = Modifier.widthIn(min = if (short) 132.dp else 196.dp)
                        .padding(horizontal = 14.dp, vertical = if (short) 6.dp else 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(page.emoji, fontSize = if (short) 46.sp else 72.sp)
                    Text("Гондыр", style = MaterialTheme.typography.titleLarge)
                    Text("медведь", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            style = MaterialTheme.typography.headlineSmall,
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
            .size(if (short) 48.dp else 74.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(studyCardColor()),
        contentAlignment = Alignment.Center,
    ) {
        Ornament(kind = kind, color = star, modifier = Modifier.size(if (short) 24.dp else 37.dp))
    }
}
