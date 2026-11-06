package org.udmurtlang.learn.feature.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.udmurtlang.learn.R
import org.udmurtlang.learn.core.designsystem.components.FactBubble

private val factPages = listOf(
    R.string.fact_1,
    R.string.fact_2,
    R.string.fact_3,
    R.string.fact_4,
    R.string.fact_5,
    R.string.fact_6,
)
private val firstFactPage = Int.MAX_VALUE / 2 / factPages.size * factPages.size

@Composable
fun FactsCarousel(modifier: Modifier = Modifier) {
    val pager = rememberPagerState(initialPage = firstFactPage) { Int.MAX_VALUE }
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.facts_title),
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onBackground,
        )
        HorizontalPager(
            state = pager,
            pageSpacing = 12.dp,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            FactBubble(
                text = stringResource(factPages[page % factPages.size]),
                modifier = Modifier.heightIn(min = 112.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                scope.launch { pager.animateScrollToPage((pager.targetPage - 1).coerceAtLeast(0)) }
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.back))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(factPages.size) { index ->
                    val active = pager.currentPage % factPages.size == index
                    Box(Modifier.size(8.dp).background(
                        if (active) scheme.primary else scheme.outlineVariant,
                        CircleShape,
                    ))
                }
            }
            IconButton(onClick = {
                scope.launch { pager.animateScrollToPage((pager.targetPage + 1).coerceAtMost(pager.pageCount - 1)) }
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.next))
            }
        }
    }
}
