package com.raizesvivas.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.worker.WorkManagerInitializer
import com.raizesvivas.app.presentation.navigation.NavGraph
import com.raizesvivas.app.presentation.theme.LocalThemeController
import com.raizesvivas.app.presentation.theme.RaizesVivasTheme
import com.raizesvivas.app.presentation.theme.ThemeController
import com.raizesvivas.app.presentation.theme.ThemeMode
import com.raizesvivas.app.presentation.theme.ThemeModeStateSaver
import com.raizesvivas.app.utils.DeepLinkHandler
import com.raizesvivas.app.utils.ThemePreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import com.raizesvivas.app.presentation.components.MascotOverlay
import com.raizesvivas.app.presentation.splash.VideoSplashScreen
import kotlinx.coroutines.launch

/**
 * Activity principal do aplicativo
 * 
 * Esta activity hospeda toda a navegação do app usando Jetpack Compose.
 * A navegação real é definida em NavGraph.kt
 * 
 * Também trata deep links e App Links para navegação direta
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var authService: AuthService
    
    @Inject
    lateinit var workManagerInitializer: WorkManagerInitializer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var showSplash by rememberSaveable { mutableStateOf(true) }
            val themeModeState = rememberSaveable(saver = ThemeModeStateSaver) {
                mutableStateOf(ThemeMode.SISTEMA)
            }
            val coroutineScope = rememberCoroutineScope()
            
            // Inicializar jobs periódicos após Hilt estar pronto
            LaunchedEffect(Unit) {
                try {
                    workManagerInitializer.agendarSincronizacaoRelacoes()
                    workManagerInitializer.agendarVerificacaoAniversarios()
                    Timber.d("✅ Jobs periódicos inicializados")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao inicializar jobs periódicos")
                }
                
                ThemePreferenceManager.readThemeMode(applicationContext)?.let {
                    themeModeState.value = it
                }
            }
            
            if (showSplash) {
                VideoSplashScreen(
                    onVideoEnd = { showSplash = false }
                )
            } else {
                val systemDark = isSystemInDarkTheme()
                val isDarkTheme = when (themeModeState.value) {
                    ThemeMode.SISTEMA -> systemDark
                    ThemeMode.ESCURO -> true
                    ThemeMode.CLARO -> false
                }

                CompositionLocalProvider(
                    LocalThemeController provides ThemeController(
                        modo = themeModeState.value,
                        selecionarModo = {
                            themeModeState.value = it
                            Timber.i("Theme mode alterado para ${it.name}")
                            coroutineScope.launch {
                                ThemePreferenceManager.writeThemeMode(applicationContext, it)
                            }
                        }
                    )
                ) {
                RaizesVivasTheme(darkTheme = isDarkTheme) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val colorScheme = MaterialTheme.colorScheme
                            val statusBarColor = colorScheme.surfaceColorAtElevation(2.dp)
                            val navigationBarColor = colorScheme.surfaceColorAtElevation(0.dp)

                            SideEffect {
                                val window = window
                                window.statusBarColor = statusBarColor.toArgb()
                                window.navigationBarColor = navigationBarColor.toArgb()

                                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                                insetsController.isAppearanceLightStatusBars = !isDarkTheme
                                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
                            }

                            val navController = rememberNavController()
                            
                            // Tratar deep links
                            HandleDeepLink(navController = navController)
                            
                            NavGraph(
                                navController = navController,
                                authService = authService
                            )
                        }
                        
                        // Mascote Global Overlay
                        MascotOverlay()
                    }
                }
            }
        }
    }
        
        // Tratar deep link na criação da activity
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null) {
            Timber.d("🔗 Deep link recebido na MainActivity: $uri")
            // A navegação será tratada pelo Composable HandleDeepLink
        }
    }
}

@Composable
private fun HandleDeepLink(navController: androidx.navigation.NavController) {
    val activity = LocalContext.current as? MainActivity
    var currentIntent by remember { mutableStateOf(activity?.intent) }
    
    // Observar mudanças na intent (para onNewIntent)
    LaunchedEffect(activity?.intent) {
        val intent = activity?.intent
        if (intent != currentIntent) {
            currentIntent = intent
            intent?.data?.let { uri ->
                Timber.d("🔗 Processando deep link no Composable: $uri")
                DeepLinkHandler.handleDeepLink(uri, navController)
            }
        }
    }
}

