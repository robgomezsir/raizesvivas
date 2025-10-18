package com.raizesvivas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raizesvivas.core.ui.theme.RaizesVivasTheme
import com.raizesvivas.feature.auth.presentation.screen.LoginScreen
import com.raizesvivas.feature.auth.presentation.screen.RegisterScreen
import com.raizesvivas.feature.auth.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Activity principal do Raízes Vivas
 * 
 * Gerencia a navegação principal da aplicação e
 * o estado de autenticação.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RaizesVivasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RaizesVivasNavigation()
                }
            }
        }
    }
}

/**
 * Navegação principal da aplicação
 * 
 * Gerencia a navegação entre telas de autenticação
 * e a tela principal da aplicação.
 */
@Composable
fun RaizesVivasNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToMain = {
                    // TODO: Implementar navegação para tela principal
                    // navController.navigate("main")
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    // TODO: Implementar navegação para tela principal
                    // navController.navigate("main")
                }
            )
        }
        
        // TODO: Adicionar composable para tela principal
        // composable("main") {
        //     MainScreen()
        // }
    }
}
