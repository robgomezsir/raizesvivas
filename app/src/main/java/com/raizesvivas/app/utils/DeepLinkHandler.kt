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
                    "familia" -> navController.navigate(Screen.Familia.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    "perfil" -> navController.navigate(Screen.Perfil.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    "pessoa" -> {
                        val pessoaId = uri.pathSegments.getOrNull(1)
                        if (pessoaId != null) {
                            // Navegar para Perfil - os detalhes da pessoa vinculada serão mostrados automaticamente
                            navController.navigate(Screen.Perfil.route)
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
            "reset-password" -> {
                val oobCode = uri.getQueryParameter("oobCode")
                val mode = uri.getQueryParameter("mode")
                if (oobCode != null && mode == "resetPassword") {
                    Timber.d("🔗 Navegando para redefinição de senha com oobCode")
                    navController.navigate(Screen.RedefinirSenha.createRoute(oobCode)) {
                        popUpTo(Screen.Login.route) { inclusive = false }
                    }
                } else {
                    Timber.w("⚠️ Parâmetros inválidos para reset-password: oobCode=$oobCode, mode=$mode")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
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
        
        // Tratar links do Firebase Auth (reset password)
        val mode = uri.getQueryParameter("mode")
        val oobCode = uri.getQueryParameter("oobCode")
        if (mode == "resetPassword" && oobCode != null) {
            Timber.d("🔗 Processando link de reset de senha do Firebase")
            navController.navigate(Screen.RedefinirSenha.createRoute(oobCode)) {
                popUpTo(Screen.Login.route) { inclusive = false }
            }
            return
        }
        
        when {
            host == "raizesvivas.com" || host?.contains("raizesvivas") == true -> {
                when (path.firstOrNull()) {
                    "pessoa" -> {
                        val pessoaId = path.getOrNull(1)
                        if (pessoaId != null) {
                            // Navegar para Perfil - os detalhes da pessoa vinculada serão mostrados automaticamente
                            navController.navigate(Screen.Perfil.route)
                        }
                    }
                    "home" -> navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    "familia" -> navController.navigate(Screen.Familia.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    "reset-password" -> {
                        // Tratar reset-password via HTTP também
                        if (oobCode != null) {
                            navController.navigate(Screen.RedefinirSenha.createRoute(oobCode)) {
                                popUpTo(Screen.Login.route) { inclusive = false }
                            }
                        }
                    }
                    else -> {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            // Tratar links do Firebase (suasraizesvivas.firebaseapp.com)
            host?.contains("firebaseapp.com") == true -> {
                if (mode == "resetPassword" && oobCode != null) {
                    Timber.d("🔗 Processando link de reset do Firebase App")
                    navController.navigate(Screen.RedefinirSenha.createRoute(oobCode)) {
                        popUpTo(Screen.Login.route) { inclusive = false }
                    }
                }
            }
            else -> {
                Timber.w("⚠️ Domínio não reconhecido: $host")
            }
        }
    }
}

