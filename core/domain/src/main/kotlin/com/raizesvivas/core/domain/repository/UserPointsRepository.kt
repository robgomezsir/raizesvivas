package com.raizesvivas.core.domain.repository

import com.raizesvivas.core.domain.model.UserPoints

/**
 * Interface do repositório de pontos do usuário
 * 
 * Define os contratos para operações com pontos
 * e níveis dos usuários.
 */
interface UserPointsRepository {
    
    /**
     * Obtém os pontos de um usuário
     */
    suspend fun getUserPoints(userId: String): UserPoints?
    
    /**
     * Adiciona pontos a um usuário
     */
    suspend fun addPoints(userId: String, points: Int): Result<UserPoints>
    
    /**
     * Atualiza os pontos de um usuário
     */
    suspend fun updateUserPoints(userPoints: UserPoints): Result<UserPoints>
    
    /**
     * Cria pontos iniciais para um usuário
     */
    suspend fun createUserPoints(userId: String): Result<UserPoints>
    
    /**
     * Obtém o ranking de usuários por pontos
     */
    suspend fun getUserRanking(limit: Int = 10): List<UserPoints>
    
    /**
     * Obtém a posição de um usuário no ranking
     */
    suspend fun getUserRankingPosition(userId: String): Int
}
