package com.raizesvivas.app.presentation.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.raizesvivas.app.presentation.navigation.Screen
import com.raizesvivas.app.presentation.screens.home.HomeScreen
import com.raizesvivas.app.presentation.screens.familia.FamiliaScreen
import com.raizesvivas.app.presentation.screens.perfil.PerfilScreen
import com.raizesvivas.app.presentation.screens.conquistas.ConquistasScreen
import com.raizesvivas.app.presentation.screens.mural.MuralScreen
import com.raizesvivas.app.presentation.screens.chat.ChatContactsScreen
import com.raizesvivas.app.presentation.screens.chat.ChatConversationScreen

/**
 * Navegação principal do app com Bottom Navigation persistente
 * 
 * Gerencia navegação entre Home, Árvore e Perfil usando bottom navigation bar.
 * A bottom navigation permanece visível em todas as telas principais e sub-telas,
 * exceto nas telas modais (que abrem como fullscreen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    navControllerPrincipal: NavHostController,
    startDestination: String = Screen.Home.route
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Determinar se deve mostrar a bottom navigation
    // Não mostrar em telas modais ou de detalhes
    val mostrarBottomNav = currentDestination?.route?.let { route ->
        !route.contains("cadastro_pessoa") &&
        !route.contains("detalhes_pessoa") &&
        !route.contains("aceitar_convites") &&
        !route.contains("gerenciar_convites") &&
        !route.contains("gerenciar_edicoes") &&
        !route.contains("resolver_duplicatas") &&
        !route.contains("familia_zero") &&
        !route.contains("chat_contacts") &&
        !route.contains("chat_conversation")
    } ?: true
    
    Scaffold(
        bottomBar = {
            if (mostrarBottomNav) {
                NavigationBar(
                    modifier = Modifier.height(64.dp) // Altura padrão do NavigationBar menos 4.dp
                ) {
                    // Home
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = {},
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Mural
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Mural") },
                        label = {},
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Mural.route } == true,
                        onClick = {
                            navController.navigate(Screen.Mural.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Família
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.People, contentDescription = "Família") },
                        label = {},
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Familia.route } == true,
                        onClick = {
                            navController.navigate(Screen.Familia.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Conquistas
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = "Conquistas") },
                        label = {},
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Conquistas.route } == true,
                        onClick = {
                            navController.navigate(Screen.Conquistas.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Perfil
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = {},
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Perfil.route } == true,
                        onClick = {
                            navController.navigate(Screen.Perfil.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCadastroPessoa = {
                        navControllerPrincipal.navigate(Screen.CadastroPessoa.route)
                    },
                    onNavigateToEditarPessoa = { pessoaId ->
                        navControllerPrincipal.navigate(Screen.EditarPessoa.createRoute(pessoaId))
                    },
                    onNavigateToPerfil = {
                        navController.navigate(Screen.Perfil.route)
                    },
                    onNavigateToFamiliaZero = {
                        navControllerPrincipal.navigate(Screen.FamiliaZero.route)
                    },
                    onNavigateToDetalhesPessoa = { pessoaId ->
                        navControllerPrincipal.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                    },
                    onNavigateToAceitarConvites = {
                        navControllerPrincipal.navigate(Screen.AceitarConvites.route)
                    },
                    onNavigateToGerenciarConvites = {
                        navControllerPrincipal.navigate(Screen.GerenciarConvites.route)
                    },
                    onNavigateToGerenciarEdicoes = {
                        navControllerPrincipal.navigate(Screen.GerenciarEdicoes.route)
                    },
                    onNavigateToResolverDuplicatas = {
                        navControllerPrincipal.navigate(Screen.ResolverDuplicatas.route)
                    },
                    onNavigateToGerenciarUsuarios = {
                        navControllerPrincipal.navigate(Screen.GerenciarUsuarios.route)
                    }
                )
            }
            
            composable(Screen.Conquistas.route) {
                ConquistasScreen()
            }
            
            composable(Screen.Familia.route) {
                FamiliaScreen(
                    onNavigateToDetalhesPessoa = { pessoaId ->
                        navControllerPrincipal.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                    },
                    onNavigateToCadastroPessoa = {
                        navControllerPrincipal.navigate(Screen.CadastroPessoa.route)
                    }
                )
            }
            
            composable(Screen.Mural.route) {
                MuralScreen(
                    onNavigateToDetalhesPessoa = { pessoaId ->
                        navControllerPrincipal.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                    },
                    onNavigateToChat = {
                        navController.navigate(Screen.ChatContacts.route)
                    }
                )
            }
            
            composable(Screen.ChatContacts.route) {
                ChatContactsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onOpenConversation = { destinatarioId, destinatarioNome ->
                        navController.navigate(Screen.ChatConversation.createRoute(destinatarioId, destinatarioNome))
                    }
                )
            }
            
            composable(
                route = Screen.ChatConversation.route,
                arguments = listOf(
                    navArgument("destinatarioId") {
                        type = androidx.navigation.NavType.StringType
                    },
                    navArgument("destinatarioNome") {
                        type = androidx.navigation.NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val destinatarioId = backStackEntry.arguments?.getString("destinatarioId") ?: ""
                val destinatarioNome = backStackEntry.arguments?.getString("destinatarioNome")?.replace("_", "/") ?: ""
                
                ChatConversationScreen(
                    destinatarioId = destinatarioId,
                    destinatarioNome = destinatarioNome,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Perfil.route) {
                PerfilScreen(
                    onNavigateToCadastroPessoaComId = { _ ->
                        navControllerPrincipal.navigate(Screen.CadastroPessoa.route)
                    }
                )
            }
        }
    }
}

