package com.cinemx.movies.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cinemx.movies.feature.auth.presentation.LoginScreen
import com.cinemx.movies.feature.movies.presentation.detail.MovieDetailScreen
import com.cinemx.movies.feature.movies.presentation.list.MovieListScreen

private const val TRANSITION_MILLIS = 260

@Composable
fun AppNavHost(
    startDestination: Any,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_MILLIS),
            ) + fadeIn(tween(TRANSITION_MILLIS))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_MILLIS),
            ) + fadeOut(tween(TRANSITION_MILLIS))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_MILLIS),
            ) + fadeIn(tween(TRANSITION_MILLIS))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_MILLIS),
            ) + fadeOut(tween(TRANSITION_MILLIS))
        },
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(MovieListRoute) {
                        // Se elimina el login del back stack: "atrás" no debe regresar a él.
                        popUpTo<LoginRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<MovieListRoute> {
            MovieListScreen(
                onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
                onLoggedOut = {
                    navController.navigate(LoginRoute) {
                        // Tras cerrar sesión se limpia todo el back stack.
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<MovieDetailRoute> {
            MovieDetailScreen(onBack = navController::navigateUp)
        }
    }
}
