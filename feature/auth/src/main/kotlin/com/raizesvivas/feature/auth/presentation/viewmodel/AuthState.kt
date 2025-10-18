package com.raizesvivas.feature.auth.presentation.viewmodel

import com.raizesvivas.core.domain.model.User

/**
 * Estados possíveis da autenticação
 * 
 * Define todos os estados que a autenticação pode estar
 * durante o ciclo de vida da aplicação.
 */
sealed class AuthState {
    /**
     * Estado inicial - ainda não verificou autenticação
     */
    object Initial : AuthState()
    
    /**
     * Carregando - operação de autenticação em andamento
     */
    object Loading : AuthState()
    
    /**
     * Usuário autenticado
     */
    data class Authenticated(val user: User?) : AuthState()
    
    /**
     * Usuário não autenticado
     */
    object Unauthenticated : AuthState()
    
    /**
     * Erro na autenticação
     */
    data class Error(val message: String) : AuthState()
}
