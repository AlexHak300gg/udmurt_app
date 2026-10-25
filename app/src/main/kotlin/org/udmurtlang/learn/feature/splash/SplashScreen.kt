package org.udmurtlang.learn.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.LocalBrand
import org.udmurtlang.learn.core.designsystem.OrnamentBand
import org.udmurtlang.learn.core.designsystem.StarGlyph
import org.udmurtlang.learn.core.designsystem.StarMark
import org.udmurtlang.learn.core.designsystem.StarPattern
import org.udmurtlang.learn.core.designsystem.ZcInk
import org.udmurtlang.learn.core.designsystem.ZcOrangeOnDark
import org.udmurtlang.learn.core.designsystem.ZcPurple
import org.udmurtlang.learn.core.designsystem.ZcPurpleOnDark
import org.udmurtlang.learn.core.designsystem.ZcRed
import org.udmurtlang.learn.core.designsystem.ZcRedDark
import org.udmurtlang.learn.core.designsystem.ZcRedOnDark
import org.udmurtlang.learn.core.designsystem.ZcTeal
import org.udmurtlang.learn.core.designsystem.ZcTealOnDark
import org.udmurtlang.learn.core.designsystem.ZcWhite
import org.udmurtlang.learn.data.local.AppDataStore
import org.udmurtlang.learn.domain.repository.AccountRepository
import org.udmurtlang.learn.feature.navigation.Routes
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStore: AppDataStore,
    private val account: AccountRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val onboarded = dataStore.onboardingDone.first()
            val profile = account.observeProfile().first()
            _destination.value = when {
                !onboarded -> Routes.ONBOARDING
                profile == null -> Routes.AUTH
                else -> Routes.MENU
            }
        }
    }
}

@Composable
fun SplashScreen(
    onFinished: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }
    val brand = LocalBrand.current

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "splashAlpha",
    )
    val starScale = remember { Animatable(0.4f) }
    val starRotation = remember { Animatable(-0.3f) }
    val textsOffset = remember { Animatable(36f) }
    val textsAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        visible = true
        launch { starScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) }
        launch { starRotation.animateTo(0f, tween(900)) }
        launch { textsOffset.animateTo(0f, tween(450, delayMillis = 250)) }
        launch { textsAlpha.animateTo(1f, tween(450, delayMillis = 250)) }
    }
    LaunchedEffect(destination) {
        destination?.let(onFinished)
    }

    // Брендбук: коралловый экран, белый логотип, цветной ряд орнаментов внизу.
    // В тёмной теме фон нейтральный, логотип коралловый.
    val bg = if (brand.isDark) MaterialTheme.colorScheme.background else ZcRed
    val on = if (brand.isDark) MaterialTheme.colorScheme.onBackground else ZcWhite
    val onMuted = if (brand.isDark) MaterialTheme.colorScheme.onSurfaceVariant else ZcWhite.copy(alpha = 0.85f)
    val ornamentColors = if (brand.isDark) {
        listOf(ZcRedOnDark, ZcTealOnDark, ZcPurpleOnDark, ZcOrangeOnDark, ZcWhite, ZcRedOnDark)
    } else {
        listOf(ZcWhite, ZcTeal, ZcPurple, ZcWhite, ZcInk, ZcTeal)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .alpha(contentAlpha),
    ) {
        StarPattern(
            modifier = Modifier.fillMaxSize(),
            color = on,
            alpha = 0.05f,
            cell = 110.dp,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StarMark(
                color = if (brand.isDark) ZcRedDark else ZcWhite,
                markColor = bg,
                size = 132.dp,
                modifier = Modifier.graphicsLayer {
                    scaleX = starScale.value
                    scaleY = starScale.value
                    rotationZ = starRotation.value * 180f
                },
            )
            Spacer(modifier = Modifier.height(28.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    translationY = textsOffset.value
                    alpha = textsAlpha.value
                },
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = on,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = onMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(30.dp))
                LoadingStars(color = on)
            }
        }

        OrnamentBand(
            colors = ornamentColors,
            height = 40.dp,
            diamondSize = 30.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        )
    }
}

@Composable
private fun LoadingStars(color: androidx.compose.ui.graphics.Color = ZcRed) {
    val transition = rememberInfiniteTransition(label = "splashLoading")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "splashPhase",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) { index ->
            val delta = (phase - index + 3f) % 3f
            val brightness = when {
                delta < 1f -> delta
                delta > 2f -> 3f - delta
                else -> 1f
            }
            StarGlyph(
                size = 12.dp,
                color = color.copy(alpha = 0.3f + 0.7f * brightness),
            )
        }
    }
}
