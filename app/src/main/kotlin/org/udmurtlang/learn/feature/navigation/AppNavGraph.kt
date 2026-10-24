package org.udmurtlang.learn.feature.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.udmurtlang.learn.feature.auth.AuthScreen
import org.udmurtlang.learn.feature.learning.TopicListScreen
import org.udmurtlang.learn.feature.learning.WordCardsScreen
import org.udmurtlang.learn.feature.games.GamesHubScreen
import org.udmurtlang.learn.feature.games.GameRoute
import org.udmurtlang.learn.feature.games.ResultScreen
import org.udmurtlang.learn.feature.games.builder.BuilderScreen
import org.udmurtlang.learn.feature.games.choice.ChoiceGameScreen
import org.udmurtlang.learn.feature.games.memory.MemoryScreen
import org.udmurtlang.learn.feature.games.oddoneout.OddOneOutScreen
import org.udmurtlang.learn.feature.games.sorting.SortingScreen
import org.udmurtlang.learn.feature.games.truefalse.TrueFalseScreen
import org.udmurtlang.learn.feature.menu.MenuScreen
import org.udmurtlang.learn.feature.onboarding.OnboardingScreen
import org.udmurtlang.learn.feature.profile.ProfileScreen
import org.udmurtlang.learn.feature.report.ReportScreen
import org.udmurtlang.learn.feature.settings.SettingsScreen
import org.udmurtlang.learn.feature.splash.SplashScreen
import org.udmurtlang.learn.feature.stats.StatsScreen
import org.udmurtlang.learn.feature.wordofday.WordOfDayScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut() },
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
                onBack = { navController.popBackStack() },
                onOpenTopic = { navController.navigate(Routes.cards(it)) },
            )
        }
        composable(
            Routes.CARDS,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType }),
        ) {
            WordCardsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.GAMES) {
            GamesHubScreen(
                onBack = { navController.popBackStack() },
                onOpenGame = { navController.navigate(Routes.game(it)) },
            )
        }
        composable(
            Routes.GAME,
            arguments = listOf(navArgument("slug") { type = NavType.StringType }),
        ) {
            GameRoute(
                slug = it.arguments?.getString("slug").orEmpty(),
                onFinished = { slug, score, correct, total ->
                    navController.navigate(Routes.result(slug, score, correct, total)) {
                        popUpTo(Routes.GAMES)
                    }
                },
                onBack = { navController.popBackStack() },
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
                onToMenu = { navController.popBackStack(Routes.GAMES, inclusive = false) },
            )
        }
        composable(Routes.STATS) {
            StatsScreen(
                onBack = { navController.popBackStack() },
                onOpenReport = { navController.navigate(Routes.REPORT) },
            )
        }
        composable(Routes.REPORT) {
            ReportScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenReport = { navController.navigate(Routes.REPORT) },
                onLoggedOut = {
                    navController.navigate(Routes.AUTH) { popUpTo(0) { inclusive = true } }
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.WORD_OF_DAY) {
            WordOfDayScreen(onBack = { navController.popBackStack() })
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
