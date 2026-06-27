package com.streamvault.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streamvault.app.ui.home.HomeScreen
import com.streamvault.app.ui.live.LiveTvScreen
import com.streamvault.app.ui.movies.MoviesScreen
import com.streamvault.app.ui.series.SeriesScreen
import com.streamvault.app.ui.favorites.FavoritesScreen
import com.streamvault.app.ui.search.SearchScreen
import com.streamvault.app.ui.settings.SettingsScreen
import com.streamvault.app.ui.player.PlayerScreen

object Routes {
    const val HOME = "home"
    const val LIVE = "live"
    const val MOVIES = "movies"
    const val SERIES = "series"
    const val FAVORITES = "favorites"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val PLAYER = "player/{type}/{streamId}/{title}"
    const val SERIES_DETAIL = "series_detail/{seriesId}"
    const val CATEGORY = "category/{type}/{categoryId}/{categoryName}"
}

@Composable
fun StreamVaultNavHost(
    navController: NavHostController = rememberNavController(),
    isTv: Boolean = false
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) },
                onPlay = { type, id, title ->
                    navController.navigate("player/$type/$id/$title")
                },
                isTv = isTv
            )
        }

        composable(Routes.LIVE) {
            LiveTvScreen(
                onBack = { navController.popBackStack() },
                onPlay = { type, id, title ->
                    navController.navigate("player/$type/$id/$title")
                },
                isTv = isTv
            )
        }

        composable(Routes.MOVIES) {
            MoviesScreen(
                onBack = { navController.popBackStack() },
                onPlay = { type, id, title ->
                    navController.navigate("player/$type/$id/$title")
                },
                isTv = isTv
            )
        }

        composable(Routes.SERIES) {
            SeriesScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) },
                isTv = isTv
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onPlay = { type, id, title ->
                    navController.navigate("player/$type/$id/$title")
                },
                isTv = isTv
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onPlay = { type, id, title ->
                    navController.navigate("player/$type/$id/$title")
                },
                isTv = isTv
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                isTv = isTv
            )
        }

        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("streamId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "live"
            val streamId = backStackEntry.arguments?.getString("streamId") ?: "0"
            val title = backStackEntry.arguments?.getString("title") ?: ""
            PlayerScreen(
                type = type,
                streamId = streamId,
                title = title,
                onBack = { navController.popBackStack() },
                isTv = isTv
            )
        }

        composable(
            route = Routes.SERIES_DETAIL,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: "0"
            com.streamvault.app.ui.series.SeriesDetailScreen(
                seriesId = seriesId,
                onBack = { navController.popBackStack() },
                onPlay = { type, id, title ->
                    navController.navigate("player/$type/$id/$title")
                },
                isTv = isTv
            )
        }
    }
}
