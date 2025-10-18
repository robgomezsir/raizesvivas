package com.raizesvivas.core.domain.repository

import com.raizesvivas.core.domain.model.User

/**
 * Interface do repositório de autenticação
 * 
 * Define os contratos para operações de autenticação
 * no sistema Raízes Vivas.
 */
interface AuthRepository {
    
    /**
     * Realiza login do usuário
     * 
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return Result com o usuário logado ou erro
     */
    suspend fun signIn(email: String, password: String): Result<User>
    
    /**
     * Registra um novo usuário
     * 
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return Result com o usuário registrado ou erro
     */
    suspend fun signUp(email: String, password: String): Result<User>
    
    /**
     * Realiza logout do usuário
     * 
     * @return Result indicando sucesso ou erro
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Obtém o usuário atual
     * 
     * @return Result com o usuário atual ou null se não estiver logado
     */
    suspend fun getCurrentUser(): Result<User?>
    
    /**
     * Verifica se o usuário está logado
     * 
     * @return true se estiver logado, false caso contrário
     */
    suspend fun isSignedIn(): Boolean
}
