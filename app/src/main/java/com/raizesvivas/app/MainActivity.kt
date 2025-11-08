package com.raizesvivas.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.presentation.navigation.NavGraph
import com.raizesvivas.app.presentation.theme.RaizesVivasTheme
import com.raizesvivas.app.utils.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RaizesVivasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Tratar deep links
                    HandleDeepLink(navController = navController)
                    
                    NavGraph(
                        navController = navController,
                        authService = authService
                    )
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

