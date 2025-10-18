package com.raizesvivas.core.domain.repository

import com.raizesvivas.core.domain.model.Achievement
import com.raizesvivas.core.domain.model.AchievementType
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de conquistas
 * 
 * Define os contratos para operações com conquistas
 * no sistema de gamificação.
 */
interface AchievementRepository {
    
    /**
     * Obtém todas as conquistas disponíveis
     */
    fun getAllAchievements(): Flow<List<Achievement>>
    
    /**
     * Obtém uma conquista por ID
     */
    suspend fun getAchievementById(id: String): Achievement?
    
    /**
     * Obtém conquistas por tipo
     */
    fun getAchievementsByType(type: AchievementType): Flow<List<Achievement>>
    
    /**
     * Obtém conquistas ativas
     */
    fun getActiveAchievements(): Flow<List<Achievement>>
}
