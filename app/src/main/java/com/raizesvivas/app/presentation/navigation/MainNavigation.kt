package com.raizesvivas.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
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
import com.raizesvivas.app.presentation.screens.album.AlbumFamiliaScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.presentation.screens.perfil.PerfilViewModel
import com.raizesvivas.app.presentation.screens.familia.ReordenarFamiliasScreen

/**
 * Calcula o tamanho de fonte único para todos os labels da NavigationBar
 * Baseado no texto mais longo para garantir que todos caibam em uma única linha
 */
@Composable
fun rememberAdaptiveNavigationFontSize(): androidx.compose.ui.unit.TextUnit {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Lista de todos os textos possíveis na navigation bar
    val allLabels = listOf("Início", "Mural", "Família", "Rede", "Eu")
    
    // Encontrar o texto mais longo
    val longestText = allLabels.maxByOrNull { it.length } ?: "Família"
    
    return remember(screenWidth) {
        // Calcular largura disponível por item (assumindo 5 itens na navigation bar)
        // Usar 80% da largura disponível para dar margem de segurança
        val availableWidthPerItem = (screenWidth / 5) * 0.80f
        
        // Calcular tamanho da fonte baseado no texto mais longo
        // Fator de 1.5 para dar mais espaço e permitir fonte maior
        // Estimativa: cada caractere ocupa aproximadamente fontSize * 0.6 em dp
        val maxFontSize = (availableWidthPerItem / longestText.length) * 1.5f
        
        // Limitar entre 10sp (mínimo legível) e 14sp (máximo reduzido em 4sp)
        maxFontSize.coerceAtMost(14f).coerceAtLeast(10f).sp
    }
}

/**
 * Texto adaptativo que ajusta o tamanho da fonte para caber em uma única linha
 * Todos os labels usam o mesmo tamanho de fonte para manter consistência
 */
@Composable
fun AdaptiveNavigationLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    // Usar o mesmo tamanho de fonte para todos os labels
    val fontSize = rememberAdaptiveNavigationFontSize()
    
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

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
    startDestination: String = Screen.Home.route,
    openDrawerOnStart: Boolean = false,
    openFotoAlbumId: String? = null
) {
    val navController = rememberNavController()

    // Handle deep linking to photo album
    LaunchedEffect(openFotoAlbumId) {
        if (openFotoAlbumId != null) {
            navController.navigate(Screen.AlbumFamilia.createRoute(openFotoAlbumId)) {
                // Determine behavior: pop up to Home? or just navigate?
                // Usually we want to switch tab.
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Determinar se deve mostrar a bottom navigation
    // Não mostrar em telas modais ou de detalhes
    val mostrarBottomNav = currentDestination?.route?.let { route ->
        !route.contains("cadastro_pessoa") &&
        !route.contains("aceitar_convites") &&
        !route.contains("gerenciar_convites") &&
        !route.contains("gerenciar_edicoes") &&
        !route.contains("resolver_duplicatas") &&
        !route.contains("familia_zero") &&
        !route.contains("chat_contacts") &&
        !route.contains("chat_conversation") &&
        !route.contains("detalhes_pessoa") &&
        !route.contains("reordenar_familias") &&
        !route.contains("moderacao")
    } ?: true
    
    Scaffold(
        bottomBar = {
            if (mostrarBottomNav) {
                NavigationBar {
                    // Home
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                painter = painterResource(id = com.raizesvivas.app.R.drawable.home),
                                contentDescription = "Início",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { AdaptiveNavigationLabel("Início") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                        onClick = {
                            if (currentDestination?.route != Screen.Home.route) {
                                // Sempre limpar o back stack e ir para Home
                                // Isso garante que Home seja a rota raiz, mesmo após navegar para Perfil
                                navController.navigate(Screen.Home.route) {
                                    // Limpar todo o back stack até a rota inicial
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                    
                    // Mural
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                painter = painterResource(id = com.raizesvivas.app.R.drawable.mural),
                                contentDescription = "Mural",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { AdaptiveNavigationLabel("Mural") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Mural.route } == true,
                        onClick = {
                            navController.navigate(Screen.Mural.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Família
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                painter = painterResource(id = com.raizesvivas.app.R.drawable.familia),
                                contentDescription = "Família",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { AdaptiveNavigationLabel("Família") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Familia.route } == true,
                        onClick = {
                            navController.navigate(Screen.Familia.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Rede
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Rede") },
                        label = { AdaptiveNavigationLabel("Rede") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.AlbumFamilia.route } == true,
                        onClick = {
                            navController.navigate(Screen.AlbumFamilia.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Eu (Perfil)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Eu") },
                        label = { AdaptiveNavigationLabel("Eu") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Perfil.route } == true,
                        onClick = {
                            navController.navigate(Screen.Perfil.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
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
                // ViewModel para verificar se é o próprio perfil
                val perfilViewModel: PerfilViewModel = hiltViewModel()
                val perfilState by perfilViewModel.state.collectAsState()
                
                HomeScreen(
                    openDrawerOnStart = openDrawerOnStart,
                    onNavigateToCadastroPessoa = {
                        navControllerPrincipal.navigate(Screen.CadastroPessoa.route)
                    },
                    onNavigateToEditarPessoa = { pessoaId ->
                        navControllerPrincipal.navigate(Screen.EditarPessoa.createRoute(pessoaId))
                    },
                    onNavigateToPerfil = {
                        navController.navigate(Screen.Perfil.route) {
                            // Manter Home no back stack para poder voltar
                            launchSingleTop = true
                        }
                    },
                    onNavigateToConquistas = {
                        navController.navigate(Screen.Conquistas.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToFamiliaZero = {
                        navControllerPrincipal.navigate(Screen.FamiliaZero.route)
                    },
                    onNavigateToDetalhesPessoa = { pessoaId ->
                        if (pessoaId == perfilState.pessoaVinculadaId) {
                            navControllerPrincipal.navigate(Screen.Perfil.route)
                        } else {
                            navControllerPrincipal.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                        }
                    },
                    onNavigateToAceitarConvites = {
                        navControllerPrincipal.navigate(Screen.AceitarConvites.route)
                    },
                    onNavigateToGerenciarConvites = {
                        navControllerPrincipal.navigate(Screen.GerenciarConvites.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarEdicoes = {
                        navControllerPrincipal.navigate(Screen.GerenciarEdicoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToResolverDuplicatas = {
                        navControllerPrincipal.navigate(Screen.ResolverDuplicatas.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarUsuarios = {
                        navControllerPrincipal.navigate(Screen.GerenciarUsuarios.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToConfiguracoes = {
                        navControllerPrincipal.navigate(Screen.Configuracoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToModeracao = {
                        navControllerPrincipal.navigate(Screen.Moderacao.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToChat = { destinatarioId, destinatarioNome ->
                        navController.navigate(Screen.ChatConversation.createRoute(destinatarioId, destinatarioNome))
                    },
                    onNavigateToPoliticaPrivacidade = {
                        navControllerPrincipal.navigate(Screen.PoliticaPrivacidade.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(Screen.Conquistas.route) {
                ConquistasScreen()
            }
            
            composable(Screen.Familia.route) {
                // ViewModel para verificar se é o próprio perfil
                val perfilViewModel: PerfilViewModel = hiltViewModel()
                val perfilState by perfilViewModel.state.collectAsState()
                
                FamiliaScreen(
                    onNavigateToDetalhesPessoa = { pessoaId ->
                        if (pessoaId == perfilState.pessoaVinculadaId) {
                            navControllerPrincipal.navigate(Screen.Perfil.route)
                        } else {
                            navControllerPrincipal.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                        }
                    },
                    onNavigateToCadastroPessoa = {
                        navControllerPrincipal.navigate(Screen.CadastroPessoa.route)
                    },
                    onNavigateToAlbum = {
                        navController.navigate(Screen.AlbumFamilia.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToPerfil = {
                        navController.navigate(Screen.Perfil.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToGerenciarConvites = {
                        navControllerPrincipal.navigate(Screen.GerenciarConvites.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarEdicoes = {
                        navControllerPrincipal.navigate(Screen.GerenciarEdicoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToResolverDuplicatas = {
                        navControllerPrincipal.navigate(Screen.ResolverDuplicatas.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarUsuarios = {
                        navControllerPrincipal.navigate(Screen.GerenciarUsuarios.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToConfiguracoes = {
                        navControllerPrincipal.navigate(Screen.Configuracoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPoliticaPrivacidade = {
                        navControllerPrincipal.navigate(Screen.PoliticaPrivacidade.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToReordenarFamilias = {
                        navController.navigate(Screen.ReordenarFamilias.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSearch = {
                        navControllerPrincipal.navigate(Screen.Search.route)
                    },
                    onNavigateToModeracao = {
                        navControllerPrincipal.navigate(Screen.Moderacao.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(Screen.Mural.route) {
                // ViewModel para verificar se é o próprio perfil
                val perfilViewModel: PerfilViewModel = hiltViewModel()
                val perfilState by perfilViewModel.state.collectAsState()
                
                MuralScreen(
                    onNavigateToDetalhesPessoa = { pessoaId ->
                        if (pessoaId == perfilState.pessoaVinculadaId) {
                            navControllerPrincipal.navigate(Screen.Perfil.route)
                        } else {
                            navControllerPrincipal.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                        }
                    },
                    onNavigateToChat = {
                        navController.navigate(Screen.ChatContacts.route)
                    },
                    onNavigateToPerfil = {
                        navController.navigate(Screen.Perfil.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToGerenciarConvites = {
                        navControllerPrincipal.navigate(Screen.GerenciarConvites.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarEdicoes = {
                        navControllerPrincipal.navigate(Screen.GerenciarEdicoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToResolverDuplicatas = {
                        navControllerPrincipal.navigate(Screen.ResolverDuplicatas.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarUsuarios = {
                        navControllerPrincipal.navigate(Screen.GerenciarUsuarios.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToConfiguracoes = {
                        navControllerPrincipal.navigate(Screen.Configuracoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPoliticaPrivacidade = {
                        navControllerPrincipal.navigate(Screen.PoliticaPrivacidade.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToModeracao = {
                        navControllerPrincipal.navigate(Screen.Moderacao.route) {
                            launchSingleTop = true
                        }
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
                    onNavigateToCadastroPessoaComId = { pessoaId ->
                        if (pessoaId != null) {
                            navControllerPrincipal.navigate(Screen.EditarPessoa.createRoute(pessoaId))
                        } else {
                            navControllerPrincipal.navigate(Screen.CadastroPessoa.route)
                        }
                    },
                    onNavigateToEditar = { pessoaId ->
                        navControllerPrincipal.navigate(Screen.EditarPessoa.createRoute(pessoaId))
                    },
                    onNavigateToGerenciarConvites = {
                        navControllerPrincipal.navigate(Screen.GerenciarConvites.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarEdicoes = {
                        navControllerPrincipal.navigate(Screen.GerenciarEdicoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToResolverDuplicatas = {
                        navControllerPrincipal.navigate(Screen.ResolverDuplicatas.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarUsuarios = {
                        navControllerPrincipal.navigate(Screen.GerenciarUsuarios.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToConfiguracoes = {
                        navControllerPrincipal.navigate(Screen.Configuracoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToFotoAlbum = { fotoId ->
                        navController.navigate(Screen.AlbumFamilia.createRoute(fotoId)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPoliticaPrivacidade = {
                        navControllerPrincipal.navigate(Screen.PoliticaPrivacidade.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToModeracao = {
                        navControllerPrincipal.navigate(Screen.Moderacao.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(
                route = Screen.AlbumFamilia.route,
                arguments = listOf(
                    navArgument("fotoId") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val fotoId = backStackEntry.arguments?.getString("fotoId")
                
                // ViewModel para verificar se é o próprio perfil
                val perfilViewModel: PerfilViewModel = hiltViewModel()
                val perfilState by perfilViewModel.state.collectAsState()
                
                AlbumFamiliaScreen(
                    fotoIdParaAbrir = fotoId,
                    onNavigateBack = {
                        // Volta para a tela anterior (pode ser Home se acessado pela barra inferior,
                        // ou Família se acessado de dentro de Família)
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            // Se não houver back stack, vai para Home
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToDetalhesPessoa = { pessoaId ->
                        if (pessoaId == perfilState.pessoaVinculadaId) {
                            navControllerPrincipal.navigate(Screen.Perfil.route)
                        } else {
                            navControllerPrincipal.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                        }
                    },
                    onNavigateToPerfil = {
                        // Navegar para a aba Perfil
                        navController.navigate(Screen.Perfil.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToGerenciarConvites = {
                        navControllerPrincipal.navigate(Screen.GerenciarConvites.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarEdicoes = {
                        navControllerPrincipal.navigate(Screen.GerenciarEdicoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToResolverDuplicatas = {
                        navControllerPrincipal.navigate(Screen.ResolverDuplicatas.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToGerenciarUsuarios = {
                        navControllerPrincipal.navigate(Screen.GerenciarUsuarios.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToConfiguracoes = {
                        navControllerPrincipal.navigate(Screen.Configuracoes.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPoliticaPrivacidade = {
                        navControllerPrincipal.navigate(Screen.PoliticaPrivacidade.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToModeracao = {
                        navControllerPrincipal.navigate(Screen.Moderacao.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(Screen.ReordenarFamilias.route) {
                ReordenarFamiliasScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

