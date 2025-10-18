package com.raizesvivas.core.domain.repository

import com.raizesvivas.core.domain.model.UserAchievement
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de conquistas do usuário
 * 
 * Define os contratos para operações com conquistas
 * conquistadas pelos usuários.
 */
interface UserAchievementRepository {
    
    /**
     * Obtém todas as conquistas de um usuário
     */
    fun getUserAchievements(userId: String): Flow<List<UserAchievement>>
    
    /**
     * Obtém uma conquista específica do usuário
     */
    suspend fun getUserAchievement(userId: String, achievementId: String): UserAchievement?
    
    /**
     * Verifica se o usuário já conquistou uma conquista
     */
    suspend fun hasUserAchieved(userId: String, achievementId: String): Boolean
    
    /**
     * Adiciona uma conquista ao usuário
     */
    suspend fun addUserAchievement(userAchievement: UserAchievement): Result<UserAchievement>
    
    /**
     * Obtém contagem de conquistas do usuário
     */
    suspend fun getUserAchievementCount(userId: String): Int
    
    /**
     * Obtém conquistas por tipo do usuário
     */
    fun getUserAchievementsByType(userId: String, type: String): Flow<List<UserAchievement>>
}
