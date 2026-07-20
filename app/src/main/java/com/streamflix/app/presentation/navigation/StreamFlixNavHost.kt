package com.streamflix.app.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streamflix.app.R
import com.streamflix.app.presentation.components.BottomTab
import com.streamflix.app.presentation.components.GlassBottomBar
import com.streamflix.app.presentation.screens.channels.LiveChannelsScreen
import com.streamflix.app.presentation.screens.detail.MovieDetailScreen
import com.streamflix.app.presentation.screens.detail.SeriesDetailScreen
import com.streamflix.app.presentation.screens.favorites.FavoritesScreen
import com.streamflix.app.presentation.screens.home.HomeScreen
import com.streamflix.app.presentation.screens.imdb_top.ImdbTopMoviesScreen
import com.streamflix.app.presentation.screens.movies.MoviesScreen
import com.streamflix.app.presentation.screens.notifications.NotificationsScreen
import com.streamflix.app.presentation.screens.player.PlayerScreen
import com.streamflix.app.presentation.screens.search.SearchScreen
import com.streamflix.app.presentation.screens.settings.SettingsScreen
import com.streamflix.app.presentation.screens.sports.SportsScreen
import com.streamflix.app.presentation.screens.splash.SplashScreen
import com.streamflix.app.presentation.screens.welcome.WelcomeDialog
import com.streamflix.app.presentation.screens.welcome.WelcomeDialogScreen

sealed class Route(val route: String) {
    object Splash : Route("splash")
    object Welcome : Route("welcome")
    object Home : Route("home")
    object Movies : Route("movies")
    object Sports : Route("sports")
    object Favorites : Route("favorites")
    object Settings : Route("settings")
    object Search : Route("search")
    object Notifications : Route("notifications")
    object ImdbTop : Route("imdb_top")
    object MovieDetail : Route("movie_detail/{movieId}") {
        fun build(id: String) = "movie_detail/$id"
    }
    object SeriesDetail : Route("series_detail/{seriesId}") {
        fun build(id: String) = "series_detail/$id"
    }
    object Player : Route("player/{type}/{contentId}") {
        fun build(type: String, contentId: String) = "player/$type/$contentId"
    }
    object EpisodePlayer : Route("episode_player/{seriesId}/{season}/{episode}") {
        fun build(seriesId: String, season: Int, episode: Int) = "episode_player/$seriesId/$season/$episode"
    }
    object LiveChannels : Route("live_channels")
}

@Composable
fun StreamFlixNavHost() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: ""

    val showBottomBar = currentRoute in setOf(
        Route.Home.route, Route.Movies.route, Route.Sports.route,
        Route.Favorites.route, Route.Settings.route
    )

    val tabs = listOf(
        BottomTab(Route.Home.route, R.string.nav_home, Icons.Filled.Home),
        BottomTab(Route.Movies.route, R.string.nav_movies, Icons.Filled.Movie),
        BottomTab(Route.Sports.route, R.string.nav_sports, Icons.Filled.SportsSoccer),
        BottomTab(Route.Favorites.route, R.string.nav_favorites, Icons.Filled.Person),
        BottomTab(Route.Settings.route, R.string.nav_settings, Icons.Filled.Movie)
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GlassBottomBar(
                    currentRoute = currentRoute,
                    tabs = tabs,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            nav.navigate(route) {
                                popUpTo(Route.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Route.Splash.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(Route.Splash.route, exitTransition = { fadeOut(tween(300)) }) {
                SplashScreen(onDone = {
                    // لو أول مرة يفتح التطبيق → شاشة الترحيب
                    // لو لا → الذهاب مباشرة للـ Home
                    val context = nav.context
                    if (!WelcomeDialog.hasBeenShown(context)) {
                        nav.navigate(Route.Welcome.route) { popUpTo(Route.Splash.route) { inclusive = true } }
                    } else {
                        nav.navigate(Route.Home.route) { popUpTo(Route.Splash.route) { inclusive = true } }
                    }
                })
            }
            composable(Route.Welcome.route) {
                WelcomeDialogScreen(
                    onContinue = {
                        nav.navigate(Route.Home.route) { popUpTo(Route.Welcome.route) { inclusive = true } }
                    }
                )
            }
            composable(Route.Home.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
                HomeScreen(
                    onNavigate = { },
                    onOpenMovie = { id ->
                        if (id.startsWith("tmdb_series_")) {
                            nav.navigate(Route.SeriesDetail.build(id))
                        } else {
                            nav.navigate(Route.MovieDetail.build(id))
                        }
                    },
                    onOpenSeries = { id -> nav.navigate(Route.SeriesDetail.build(id)) },
                    onOpenAnime = { id -> nav.navigate(Route.MovieDetail.build(id)) },
                    onOpenMatch = { id -> nav.navigate(Route.Player.build("match", id)) },
                    onOpenChannel = { id -> nav.navigate(Route.Player.build("channel", id)) },
                    onOpenSearch = { nav.navigate(Route.Search.route) },
                    onOpenNotifications = { nav.navigate(Route.Notifications.route) },
                    onOpenImdbTop = { nav.navigate(Route.ImdbTop.route) }
                )
            }
            composable(Route.Movies.route) {
                MoviesScreen(onOpenMovie = { id ->
                    if (id.startsWith("tmdb_series_")) {
                        nav.navigate(Route.SeriesDetail.build(id))
                    } else {
                        nav.navigate(Route.MovieDetail.build(id))
                    }
                })
            }
            composable(Route.Sports.route) {
                SportsScreen(
                    onOpenMatch = { id ->
                        if (id.startsWith("bein_channel:")) {
                            val channelId = id.removePrefix("bein_channel:")
                            nav.navigate(Route.Player.build("bein_channel", channelId))
                        } else {
                            nav.navigate(Route.Player.build("match", id))
                        }
                    },
                    onOpenChannels = { nav.navigate(Route.LiveChannels.route) }
                )
            }
            composable(Route.Favorites.route) {
                FavoritesScreen(onOpen = { type, id ->
                    when (type) {
                        "movie" -> nav.navigate(Route.MovieDetail.build(id))
                        "series" -> nav.navigate(Route.SeriesDetail.build(id))
                        "match" -> nav.navigate(Route.Player.build("match", id))
                        else -> nav.navigate(Route.Player.build(type, id))
                    }
                })
            }
            composable(Route.Settings.route) { SettingsScreen() }
            composable(Route.Search.route) {
                SearchScreen(
                    onBack = { nav.popBackStack() },
                    onOpenMovie = { id ->
                        if (id.startsWith("tmdb_series_")) {
                            nav.navigate(Route.SeriesDetail.build(id))
                        } else {
                            nav.navigate(Route.MovieDetail.build(id))
                        }
                    }
                )
            }
            composable(Route.Notifications.route) {
                NotificationsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenMovie = { id ->
                        if (id.startsWith("tmdb_series_")) {
                            nav.navigate(Route.SeriesDetail.build(id))
                        } else {
                            nav.navigate(Route.MovieDetail.build(id))
                        }
                    }
                )
            }
            composable(Route.ImdbTop.route) {
                ImdbTopMoviesScreen(
                    onBack = { nav.popBackStack() },
                    onOpenMovie = { id ->
                        if (id.startsWith("tmdb_series_")) {
                            nav.navigate(Route.SeriesDetail.build(id))
                        } else {
                            nav.navigate(Route.MovieDetail.build(id))
                        }
                    }
                )
            }
            composable(Route.LiveChannels.route) {
                LiveChannelsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenChannel = { id -> nav.navigate(Route.Player.build("channel", id)) }
                )
            }
            composable(
                route = Route.MovieDetail.route,
                arguments = listOf(navArgument("movieId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("movieId") ?: ""
                MovieDetailScreen(
                    movieId = id,
                    onBack = { nav.popBackStack() },
                    onPlay = { nav.navigate(Route.Player.build("movie", id)) },
                    onOpenMovie = { otherId -> nav.navigate(Route.MovieDetail.build(otherId)) }
                )
            }
            composable(
                route = Route.SeriesDetail.route,
                arguments = listOf(navArgument("seriesId") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("seriesId") ?: ""
                SeriesDetailScreen(
                    seriesId = id,
                    onBack = { nav.popBackStack() },
                    onPlayEpisode = { seriesId, season, episode ->
                        nav.navigate(Route.EpisodePlayer.build(seriesId, season, episode))
                    }
                )
            }
            composable(
                route = Route.Player.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("contentId") { type = NavType.StringType }
                )
            ) { entry ->
                val type = entry.arguments?.getString("type") ?: "movie"
                val id = entry.arguments?.getString("contentId") ?: ""
                PlayerScreen(type = type, contentId = id, onBack = { nav.popBackStack() })
            }
            composable(
                route = Route.EpisodePlayer.route,
                arguments = listOf(
                    navArgument("seriesId") { type = NavType.StringType },
                    navArgument("season") { type = NavType.IntType },
                    navArgument("episode") { type = NavType.IntType }
                )
            ) { entry ->
                val seriesId = entry.arguments?.getString("seriesId") ?: ""
                val season = entry.arguments?.getInt("season") ?: 1
                val episode = entry.arguments?.getInt("episode") ?: 1
                PlayerScreen(
                    type = "episode",
                    contentId = seriesId,
                    seasonNumber = season,
                    episodeNumber = episode,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
