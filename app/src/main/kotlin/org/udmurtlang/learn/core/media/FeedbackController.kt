package org.udmurtlang.learn.core.media

import javax.inject.Inject
import javax.inject.Singleton

/** Абстракция «правильный/ошибка/победа» — удобно подменять в тестах. */
interface FeedbackPort {
    fun onCorrect()
    fun onWrong()
    fun onClick()
    fun onVictory()
}

/** Единая точка «правильный/ошибка/победа»: вибро + звук. */
@Singleton
class FeedbackController @Inject constructor(
    private val haptics: Haptics,
    private val sfx: SfxPlayer,
) : FeedbackPort {

    override fun onCorrect() {
        haptics.success()
        sfx.playSuccess()
    }

    override fun onWrong() {
        haptics.softError()
        sfx.playError()
    }

    override fun onClick() {
        sfx.playClick()
    }

    override fun onVictory() {
        haptics.success()
        sfx.playFanfare()
    }
}
