package org.udmurtlang.learn.core.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed

/** Plays a new prompt once the destination is visible and ready for input. */
@Composable
fun AutoPronounce(wordId: String?, onSpeak: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val speak by rememberUpdatedState(onSpeak)
    LaunchedEffect(lifecycle, wordId) {
        if (wordId != null) lifecycle.withResumed { speak() }
    }
}
