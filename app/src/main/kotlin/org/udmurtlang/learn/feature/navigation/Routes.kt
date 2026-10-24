package org.udmurtlang.learn.feature.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val MENU = "menu"
    const val TOPICS = "topics"
    const val CARDS = "cards/{topicId}"
    fun cards(topicId: String) = "cards/$topicId"
    const val GAMES = "games"
    const val GAME = "game/{slug}"
    fun game(slug: String) = "game/$slug"
    const val RESULT = "result/{slug}/{score}/{correct}/{total}"
    fun result(slug: String, score: Int, correct: Int, total: Int) =
        "result/$slug/$score/$correct/$total"
    const val STATS = "stats"
    const val REPORT = "report"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val WORD_OF_DAY = "wordofday"
}
