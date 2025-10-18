package com.raizesvivas.core.domain.usecase.gamification

import com.raizesvivas.core.domain.model.AchievementType
import com.raizesvivas.core.domain.repository.AchievementRepository
import com.raizesvivas.core.domain.repository.UserAchievementRepository
import com.raizesvivas.core.domain.repository.UserPointsRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case para verificar conquistas
 * 
 * Verifica se o usuário conquistou alguma conquista
 * baseada em ações realizadas.
 */
class CheckAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val userAchievementRepository: UserAchievementRepository,
    private val userPointsRepository: UserPointsRepository
) {
    
    /**
     * Verifica conquistas baseadas em uma ação
     */
    suspend fun checkAchievements(
        userId: String,
        actionType: AchievementType,
        actionData: Map<String, Any> = emptyMap()
    ): Result<List<String>> {
        return try {
            val achievements = achievementRepository.getAchievementsByType(actionType)
            val newAchievements = mutableListOf<String>()
            
            achievements.collect { achievementList ->
                achievementList.forEach { achievement ->
                    if (!userAchievementRepository.hasUserAchieved(userId, achievement.id)) {
                        if (checkAchievementRequirements(achievement, actionData)) {
                            // Conquistar a conquista
                            val userAchievement = com.raizesvivas.core.domain.model.UserAchievement(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                conquistaId = achievement.id,
                                dataConquista = LocalDateTime.now(),
                                pontosGanhos = achievement.pontosConquista,
                                ativa = true,
                                createdAt = LocalDateTime.now(),
                                updatedAt = LocalDateTime.now(),
                                achievement = achievement
                            )
                            
                            userAchievementRepository.addUserAchievement(userAchievement)
                            
                            // Adicionar pontos
                            userPointsRepository.addPoints(userId, achievement.pontosConquista)
                            
                            newAchievements.add(achievement.nome)
                        }
                    }
                }
            }
            
            Result.success(newAchievements)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica se os requisitos de uma conquista foram atendidos
     */
    private suspend fun checkAchievementRequirements(
        achievement: com.raizesvivas.core.domain.model.Achievement,
        actionData: Map<String, Any>
    ): Boolean {
        return when (achievement.tipoConquista) {
            AchievementType.MEMBRO_ADICIONADO -> {
                checkMemberCountRequirements(achievement, actionData)
            }
            AchievementType.RELACIONAMENTO_CRIADO -> {
                checkRelationshipCountRequirements(achievement, actionData)
            }
            AchievementType.PARENTESCO_CALCULADO -> {
                checkKinshipCountRequirements(achievement, actionData)
            }
            else -> {
                // Para outros tipos, verificar se a ação foi realizada
                true
            }
        }
    }
    
    /**
     * Verifica requisitos de contagem de membros
     */
    private suspend fun checkMemberCountRequirements(
        achievement: com.raizesvivas.core.domain.model.Achievement,
        actionData: Map<String, Any>
    ): Boolean {
        val requiredCount = achievement.requisitos["count"] as? Int ?: 1
        val currentCount = actionData["current_count"] as? Int ?: 1
        
        return currentCount >= requiredCount
    }
    
    /**
     * Verifica requisitos de contagem de relacionamentos
     */
    private suspend fun checkRelationshipCountRequirements(
        achievement: com.raizesvivas.core.domain.model.Achievement,
        actionData: Map<String, Any>
    ): Boolean {
        val requiredCount = achievement.requisitos["count"] as? Int ?: 1
        val currentCount = actionData["current_count"] as? Int ?: 1
        
        return currentCount >= requiredCount
    }
    
    /**
     * Verifica requisitos de contagem de parentescos
     */
    private suspend fun checkKinshipCountRequirements(
        achievement: com.raizesvivas.core.domain.model.Achievement,
        actionData: Map<String, Any>
    ): Boolean {
        val requiredCount = achievement.requisitos["count"] as? Int ?: 1
        val currentCount = actionData["current_count"] as? Int ?: 1
        
        return currentCount >= requiredCount
    }
}
