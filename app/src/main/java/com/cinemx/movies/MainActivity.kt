package com.cinemx.movies

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinemx.movies.core.ui.theme.CineMxTheme
import com.cinemx.movies.feature.auth.domain.SessionState
import com.cinemx.movies.navigation.AppNavHost
import com.cinemx.movies.navigation.LoginRoute
import com.cinemx.movies.navigation.MovieListRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        // Hasta saber si hay sesión, para que el primer frame ya sea el destino correcto.
        splash.setKeepOnScreenCondition {
            viewModel.sessionState.value is SessionState.Resolving
        }

        // Iconos claros siempre: la cabecera de marca y el backdrop del detalle
        // son oscuros en ambos temas, así que el juego automático no sirve aquí.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)

        setContent {
            CineMxTheme {
                val session by viewModel.sessionState.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (session !is SessionState.Resolving) {
                        // Se fija una sola vez: las transiciones las conduce el NavHost,
                        // en vez de recrear el grafo cada vez que cambia la sesión.
                        val startDestination = remember {
                            if (session is SessionState.Authenticated) MovieListRoute else LoginRoute
                        }
                        AppNavHost(startDestination = startDestination)
                    }
                }
            }
        }
    }
}
