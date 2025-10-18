package com.raizesvivas.core.domain.usecase.gamification

import com.raizesvivas.core.domain.repository.UserPointsRepository
import javax.inject.Inject

/**
 * Use Case para obter pontos do usuário
 * 
 * Obtém os pontos e nível de um usuário no sistema
 * de gamificação.
 */
class GetUserPointsUseCase @Inject constructor(
    private val userPointsRepository: UserPointsRepository
) {
    
    /**
     * Obtém os pontos de um usuário
     */
    suspend operator fun invoke(userId: String): Result<com.raizesvivas.core.domain.model.UserPoints?> {
        return try {
            val userPoints = userPointsRepository.getUserPoints(userId)
            Result.success(userPoints)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
