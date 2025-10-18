package com.raizesvivas.core.domain.usecase.family

import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.model.FamilyType
import com.raizesvivas.core.domain.repository.FamilyRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case para criar subfamília
 * 
 * Cria uma nova subfamília a partir de um casamento
 * entre dois membros da família.
 */
class CreateSubfamilyUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) {
    
    suspend operator fun invoke(
        userId: String,
        nome: String,
        familiaPaiId: String,
        membroOrigem1Id: String,
        membroOrigem2Id: String,
        iconeArvore: String? = null
    ): Result<Family> {
        return try {
            // Validar dados básicos
            if (nome.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("Nome da subfamília é obrigatório")
                )
            }
            
            if (membroOrigem1Id == membroOrigem2Id) {
                return Result.failure(
                    IllegalArgumentException("Os membros de origem devem ser diferentes")
                )
            }
            
            // Buscar família pai
            val familiaPai = familyRepository.getFamilyById(familiaPaiId, userId)
            if (familiaPai == null) {
                return Result.failure(
                    IllegalArgumentException("Família pai não encontrada")
                )
            }
            
            // Criar subfamília
            val subfamily = Family(
                id = UUID.randomUUID().toString(),
                nome = nome,
                tipo = FamilyType.SUBFAMILIA,
                familiaPaiId = familiaPaiId,
                criadaPorCasamento = true,
                membroOrigem1Id = membroOrigem1Id,
                membroOrigem2Id = membroOrigem2Id,
                dataCriacao = LocalDateTime.now(),
                iconeArvore = iconeArvore,
                nivelHierarquico = familiaPai.nivelHierarquico + 1,
                ativa = true,
                userId = userId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            familyRepository.createFamily(subfamily)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
