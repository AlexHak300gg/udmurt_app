package org.udmurtlang.learn.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import org.udmurtlang.learn.core.designsystem.StarGlyph
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcGold
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
    val onBg: Color,
)

private val pages = listOf(
    Page(ZcTeal, "🐻", true, R.string.onboarding_title_1, R.string.onboarding_text_1, ZcInk),
    Page(ZcPurpleDeep, "🎮", false, R.string.onboarding_title_2, R.string.onboarding_text_2, ZcWhite),
    Page(ZcOrange, "🔥", true, R.string.onboarding_title_3, R.string.onboarding_text_3, ZcInk),
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { index ->
            OnboardingPage(page = pages[index])
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 14.dp),
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
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                onClick = { viewModel.complete(onFinished) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(
                    stringResource(R.string.skip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                            .size(if (selected) 22.dp else 10.dp)
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
private fun OnboardingPage(page: Page) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(page.bg),
    ) {
        StarPattern(modifier = Modifier.fillMaxSize(), color = ZcWhite, alpha = 0.10f)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .background(ZcWhite),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (page.bigEmojiAndWord) {
                        Text(page.emoji, fontSize = 92.sp)
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
                                color = Color(0xFF9A8B80),
                            )
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MiniStarTile(bg = Color(0xFFF6EADC), star = ZcOrange)
                            MiniStarTile(bg = Color(0xFFE4E0EF), star = ZcPurpleDeep)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MiniStarTile(bg = Color(0xFFFCE5D8), star = ZcRed)
                            MiniStarTile(bg = Color(0xFFF7EDD3), star = ZcGold)
                        }
                    }
                }
            }
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                color = page.onBg,
                modifier = Modifier.padding(top = 26.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                text = stringResource(page.textRes),
                style = MaterialTheme.typography.bodyMedium,
                color = page.onBg.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MiniStarTile(bg: Color, star: Color) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        StarGlyph(size = 24.dp, color = star)
    }
}
