package com.raizesvivas.app.utils

import android.net.Uri
import androidx.navigation.NavController
import com.raizesvivas.app.presentation.navigation.Screen
import timber.log.Timber

/**
 * Handler para processar deep links
 */
object DeepLinkHandler {
    
    /**
     * Processa um deep link e navega para a rota apropriada
     * 
     * @param uri URI do deep link
     * @param navController NavController para navegação
     */
    fun handleDeepLink(uri: Uri, navController: NavController) {
        Timber.d("🔗 Processando deep link: $uri")
        
        when (uri.scheme) {
            "raizesvivas" -> handleCustomScheme(uri, navController)
            "https", "http" -> handleHttpScheme(uri, navController)
            else -> {
                Timber.w("⚠️ Esquema de deep link não suportado: ${uri.scheme}")
            }
        }
    }
    
    /**
     * Processa deep links com esquema customizado (raizesvivas://)
     */
    private fun handleCustomScheme(uri: Uri, navController: NavController) {
        when (uri.host) {
            "app" -> {
                val path = uri.pathSegments.firstOrNull()
                when (path) {
                    "home" -> navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    "arvore" -> navController.navigate(Screen.Arvore.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    "perfil" -> navController.navigate(Screen.Perfil.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    "pessoa" -> {
                        val pessoaId = uri.pathSegments.getOrNull(1)
                        if (pessoaId != null) {
                            navController.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                        } else {
                            navController.navigate(Screen.CadastroPessoa.route)
                        }
                    }
                    else -> {
                        Timber.w("⚠️ Path não reconhecido: $path")
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            else -> {
                Timber.w("⚠️ Host não reconhecido: ${uri.host}")
            }
        }
    }
    
    /**
     * Processa deep links HTTP/HTTPS
     */
    private fun handleHttpScheme(uri: Uri, navController: NavController) {
        val host = uri.host
        val path = uri.pathSegments
        
        when {
            host == "raizesvivas.com" || host?.contains("raizesvivas") == true -> {
                when (path.firstOrNull()) {
                    "pessoa" -> {
                        val pessoaId = path.getOrNull(1)
                        if (pessoaId != null) {
                            navController.navigate(Screen.DetalhesPessoa.createRoute(pessoaId))
                        }
                    }
                    "home" -> navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    "arvore" -> navController.navigate(Screen.Arvore.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    else -> {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            else -> {
                Timber.w("⚠️ Domínio não reconhecido: $host")
            }
        }
    }
}

