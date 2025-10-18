package com.raizesvivas.core.domain.usecase.family

import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.model.FamilyType
import com.raizesvivas.core.domain.repository.FamilyRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case para criar família-zero
 * 
 * Cria automaticamente a família-zero do usuário se não existir.
 * A família-zero é a raiz da árvore genealógica.
 */
class CreateFamilyZeroUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    
    suspend operator fun invoke(userId: String, nome: String): Result<Family> {
        return try {
            // Verificar se já existe família-zero
            val existingFamilyZero = familyRepository.getFamilyZero(userId)
            if (existingFamilyZero != null) {
                return Result.failure(
                    FamilyZeroAlreadyExistsException("Usuário já possui uma família-zero")
                )
            }
            
            // Criar nova família-zero
            val familyZero = Family(
                id = UUID.randomUUID().toString(),
                nome = nome,
                tipo = FamilyType.ZERO,
                familiaPaiId = null,
                criadaPorCasamento = false,
                dataCriacao = LocalDateTime.now(),
                nivelHierarquico = 0,
                ativa = true,
                userId = userId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            familyRepository.createFamily(familyZero)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Exceção para família-zero já existente
 */
class FamilyZeroAlreadyExistsException(message: String) : Exception(message)
