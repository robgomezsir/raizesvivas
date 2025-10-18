package com.raizesvivas.core.domain.usecase.family

import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.repository.FamilyRepository
import javax.inject.Inject

/**
 * Use Case para obter família-zero
 * 
 * Obtém a família-zero do usuário (raiz da árvore genealógica).
 */
class GetFamilyZeroUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    
    suspend operator fun invoke(userId: String): Result<Family?> {
        return try {
            val familyZero = familyRepository.getFamilyZero(userId)
            Result.success(familyZero)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
