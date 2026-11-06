package org.udmurtlang.learn.feature.navigation

import androidx.activity.BackEventCompat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.udmurtlang.learn.core.media.WordSpeaker
import org.udmurtlang.learn.feature.auth.AuthScreen
import org.udmurtlang.learn.feature.learning.TopicListScreen
import org.udmurtlang.learn.feature.learning.WordCardsScreen
import org.udmurtlang.learn.feature.games.GamesHubScreen
import org.udmurtlang.learn.feature.games.GameRoute
import org.udmurtlang.learn.feature.games.ResultScreen
import org.udmurtlang.learn.feature.menu.MenuScreen
import org.udmurtlang.learn.feature.onboarding.OnboardingScreen
import org.udmurtlang.learn.feature.profile.ProfileScreen
import org.udmurtlang.learn.feature.report.ReportScreen
import org.udmurtlang.learn.feature.settings.SettingsScreen
import org.udmurtlang.learn.feature.splash.SplashScreen
import org.udmurtlang.learn.feature.stats.StatsScreen
import org.udmurtlang.learn.feature.wordofday.WordOfDayScreen

private const val SLIDE_MS = 260

@Composable
fun AppNavGraph(
    wordSpeaker: WordSpeaker,
    navController: NavHostController = rememberNavController(),
) {
    DisposableEffect(navController, wordSpeaker) {
        val listener = NavController.OnDestinationChangedListener { _, _, _ -> wordSpeaker.stop() }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = Modifier.clipToBounds(),
        enterTransition = { slideInHorizontally(tween(SLIDE_MS), initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(tween(SLIDE_MS), targetOffsetX = { -it / 4 }) },
        popEnterTransition = { slideInHorizontally(tween(SLIDE_MS), initialOffsetX = { -it / 4 }) },
        popExitTransition = { slideOutHorizontally(tween(SLIDE_MS), targetOffsetX = { it }) },
        predictivePopEnterTransition = { edge ->
            slideInHorizontally(tween(SLIDE_MS)) { if (edge == BackEventCompat.EDGE_RIGHT) it / 4 else -it / 4 }
        },
        predictivePopExitTransition = { edge ->
            slideOutHorizontally(tween(SLIDE_MS)) { if (edge == BackEventCompat.EDGE_RIGHT) -it else it }
        },
    ) {
        slideIn { SplashScreen(onFinished = { route -> navController.navigate(route) { popUpTo(Routes.SPLASH) { inclusive = true } } }) }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.AUTH) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                },
            )
        }
        composable(Routes.AUTH) {
            AuthScreen(onAuthenticated = {
                navController.navigate(Routes.MENU) { popUpTo(Routes.AUTH) { inclusive = true } }
            })
        }
        composable(Routes.MENU) {
            MenuScreen(
                onOpenLearning = { navController.navigate(Routes.TOPICS) },
                onOpenGames = { navController.navigate(Routes.GAMES) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenWordOfDay = { navController.navigate(Routes.WORD_OF_DAY) },
            )
        }
        composable(Routes.TOPICS) {
            TopicListScreen(
                onBack = dropUnlessResumed { navController.popBackStack() },
                onOpenTopic = { navController.navigate(Routes.cards(it)) },
            )
        }
        composable(
            Routes.CARDS,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) {
            WordCardsScreen(onBack = dropUnlessResumed { navController.popBackStack() })
        }
        composable(Routes.GAMES) {
            GamesHubScreen(
                onBack = dropUnlessResumed { navController.popBackStack() },
                onOpenGame = { navController.navigate(Routes.game(it)) },
            )
        }
        composable(
            Routes.GAME,
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) { gameEntry ->
            GameRoute(
                slug = gameEntry.arguments?.getString("slug").orEmpty(),
                onFinished = { slug, score, correct, total ->
                    if (navController.currentBackStackEntry == gameEntry) {
                        navController.navigate(Routes.result(slug, score, correct, total)) {
                            popUpTo(Routes.GAMES)
                        }
                    }
                },
                onBack = dropUnlessResumed { navController.popBackStack() },
                onSpeak = wordSpeaker::speak,
            )
        }
        composable(
            Routes.RESULT,
            arguments = listOf(
                navArgument("slug") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType },
                navArgument("correct") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
            ),
        ) {
            ResultScreen(
                onPlayAgain = { slug ->
                    navController.navigate(Routes.game(slug)) { popUpTo(Routes.GAMES) }
                },
                onToMenu = dropUnlessResumed { navController.popBackStack(Routes.GAMES, inclusive = false) },
            )
        }
        composable(Routes.STATS) {
            StatsScreen(
                onBack = dropUnlessResumed { navController.popBackStack() },
                onOpenReport = { navController.navigate(Routes.REPORT) },
            )
        }
        composable(Routes.REPORT) {
            ReportScreen(onBack = dropUnlessResumed { navController.popBackStack() })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = dropUnlessResumed { navController.popBackStack() },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenReport = { navController.navigate(Routes.REPORT) },
                onLoggedOut = {
                    navController.navigate(Routes.AUTH) { popUpTo(0) { inclusive = true } }
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = dropUnlessResumed { navController.popBackStack() })
        }
        composable(Routes.WORD_OF_DAY) {
            WordOfDayScreen(onBack = dropUnlessResumed { navController.popBackStack() })
        }
    }
}

private fun NavGraphBuilder.slideIn(content: @Composable () -> Unit) {
    composable(
        Routes.SPLASH,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) { content() }
}
