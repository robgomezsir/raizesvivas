package com.raizesvivas.app.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.presentation.screens.auth.CadastroScreen
import com.raizesvivas.app.presentation.screens.auth.LoginScreen
import com.raizesvivas.app.presentation.screens.auth.RecuperarSenhaScreen
import com.raizesvivas.app.presentation.screens.cadastro.CadastroPessoaScreen
import com.raizesvivas.app.presentation.screens.convites.AceitarConvitesScreen
import com.raizesvivas.app.presentation.screens.convites.GerenciarConvitesScreen
import com.raizesvivas.app.presentation.screens.edicoes.GerenciarEdicoesScreen
import com.raizesvivas.app.presentation.screens.detalhes.DetalhesPessoaScreen
import com.raizesvivas.app.presentation.screens.duplicatas.ResolverDuplicatasScreen
import com.raizesvivas.app.presentation.screens.onboarding.FamiliaZeroScreen
import com.raizesvivas.app.presentation.screens.usuarios.GerenciarUsuariosScreen
import timber.log.Timber

/**
 * NavGraph principal do app
 * 
 * Define todas as rotas de navegação e suas conexões
 * Observa o estado de autenticação para redirecionar para login quando necessário
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    authService: AuthService,
    startDestination: String = Screen.Login.route
) {
    // Observar mudanças no estado de autenticação
    val authState by authService.observeAuthState().collectAsState(initial = authService.currentUser)
    
    // Navegar para login quando o usuário fizer logout
    LaunchedEffect(authState) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (authState == null) {
            // Usuário fez logout - navegar para login
            if (currentRoute != Screen.Login.route && 
                currentRoute != Screen.Cadastro.route && 
                currentRoute != Screen.RecuperarSenha.route) {
                Timber.d("👋 Usuário deslogado, navegando para Login")
                // Limpar toda a pilha de navegação e ir para login
                navController.navigate(Screen.Login.route) {
                    // Limpar toda a pilha até a raiz (startDestination)
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ============================================
        // AUTENTICAÇÃO
        // ============================================
        
        composable(
            route = Screen.Login.route,
            enterTransition = { Transitions.enterTransition() },
            exitTransition = { Transitions.exitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    // Verificar se precisa criar Família Zero
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToCadastro = {
                    navController.navigate(Screen.Cadastro.route)
                },
                onNavigateToRecuperarSenha = {
                    navController.navigate(Screen.RecuperarSenha.route)
                }
            )
        }
        
        composable(
            route = Screen.Cadastro.route,
            enterTransition = { Transitions.enterTransition() },
            exitTransition = { Transitions.exitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            CadastroScreen(
                onCadastroSuccess = {
                    // Verificar se precisa criar Família Zero
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.RecuperarSenha.route,
            enterTransition = { Transitions.modalEnterTransition() },
            exitTransition = { Transitions.modalExitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            RecuperarSenhaScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // ============================================
        // ONBOARDING
        // ============================================
        
        composable(
            route = Screen.FamiliaZero.route,
            enterTransition = { Transitions.modalEnterTransition() },
            exitTransition = { Transitions.modalExitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            FamiliaZeroScreen(
                onFamiliaZeroCriada = {
                    // Navegar para Home após criar Família Zero
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.FamiliaZero.route) { inclusive = true }
                    }
                }
            )
        }
        
        // ============================================
        // PRINCIPAL (com Bottom Navigation)
        // ============================================
        
        // Usar MainNavigation para as telas principais com bottom nav
        composable(Screen.Home.route) {
            MainNavigation(
                navControllerPrincipal = navController,
                startDestination = Screen.Home.route
            )
        }
        
        composable(Screen.Perfil.route) {
            MainNavigation(
                navControllerPrincipal = navController,
                startDestination = Screen.Perfil.route
            )
        }
        
        composable(Screen.Familia.route) {
            MainNavigation(
                navControllerPrincipal = navController,
                startDestination = Screen.Familia.route
            )
        }
        
        composable(
            route = Screen.CadastroPessoa.route,
            enterTransition = { Transitions.modalEnterTransition() },
            exitTransition = { Transitions.modalExitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            CadastroPessoaScreen(
                pessoaId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSalvo = {
                    navController.popBackStack()
                },
                onNavigateToCadastroPessoa = { _ ->
                    navController.navigate(Screen.CadastroPessoa.route)
                }
            )
        }
        
        composable(
            route = Screen.EditarPessoa.route,
            arguments = listOf(
                androidx.navigation.navArgument("pessoaId") {
                    type = androidx.navigation.NavType.StringType
                }
            ),
            enterTransition = { Transitions.modalEnterTransition() },
            exitTransition = { Transitions.modalExitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) { backStackEntry ->
            val pessoaId = backStackEntry.arguments?.getString("pessoaId")
            CadastroPessoaScreen(
                pessoaId = pessoaId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSalvo = {
                    navController.popBackStack()
                },
                onNavigateToCadastroPessoa = { _ ->
                    navController.navigate(Screen.CadastroPessoa.route)
                }
            )
        }
        
        composable(
            route = Screen.DetalhesPessoa.route,
            arguments = listOf(
                navArgument("pessoaId") {
                    type = NavType.StringType
                }
            ),
            enterTransition = { Transitions.enterTransition() },
            exitTransition = { Transitions.exitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) { backStackEntry ->
            val pessoaId = backStackEntry.arguments?.getString("pessoaId") ?: ""
            DetalhesPessoaScreen(
                pessoaId = pessoaId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditar = { id ->
                    navController.navigate(Screen.EditarPessoa.createRoute(id))
                }
            )
        }
        
        // ============================================
        // CONVITES
        // ============================================
        
        composable(
            route = Screen.AceitarConvites.route,
            enterTransition = { Transitions.modalEnterTransition() },
            exitTransition = { Transitions.modalExitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            AceitarConvitesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConviteAceito = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.GerenciarConvites.route,
            enterTransition = { Transitions.enterTransition() },
            exitTransition = { Transitions.exitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            GerenciarConvitesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // ============================================
        // EDIÇÕES PENDENTES
        // ============================================
        
        composable(
            route = Screen.GerenciarEdicoes.route,
            enterTransition = { Transitions.enterTransition() },
            exitTransition = { Transitions.exitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            GerenciarEdicoesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // ============================================
        // DUPLICATAS
        // ============================================
        
        composable(
            route = Screen.ResolverDuplicatas.route,
            enterTransition = { Transitions.enterTransition() },
            exitTransition = { Transitions.exitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            ResolverDuplicatasScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // ============================================
        // GERENCIAR USUÁRIOS (ADMIN)
        // ============================================
        
        composable(
            route = Screen.GerenciarUsuarios.route,
            enterTransition = { Transitions.enterTransition() },
            exitTransition = { Transitions.exitTransition() },
            popEnterTransition = { Transitions.popEnterTransition() },
            popExitTransition = { Transitions.popExitTransition() }
        ) {
            GerenciarUsuariosScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

