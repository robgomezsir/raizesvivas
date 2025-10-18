package com.raizesvivas.core.domain.usecase.relationship

import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.domain.model.RelationshipType
import com.raizesvivas.core.domain.repository.MemberRepository
import com.raizesvivas.core.domain.repository.RelationshipRepository
import com.raizesvivas.core.utils.algorithms.GenealogyValidator
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case para adicionar relacionamento
 * 
 * Adiciona um novo relacionamento entre dois membros
 * com validações de genealogia.
 */
class AddRelationshipUseCase @Inject constructor(
    private val relationshipRepository: RelationshipRepository,
    private val memberRepository: MemberRepository,
    private val genealogyValidator: GenealogyValidator
) {
    
    suspend operator fun invoke(
        userId: String,
        membro1Id: String,
        membro2Id: String,
        tipoRelacionamento: RelationshipType,
        dataInicio: String? = null,
        observacoes: String? = null
    ): Result<Relationship> {
        return try {
            // Validar dados básicos
            if (membro1Id == membro2Id) {
                return Result.failure(
                    IllegalArgumentException("Um membro não pode ter relacionamento consigo mesmo")
                )
            }
            
            // Buscar membros
            val member1 = memberRepository.getMemberById(membro1Id, userId)
            val member2 = memberRepository.getMemberById(membro2Id, userId)
            
            if (member1 == null || member2 == null) {
                return Result.failure(
                    IllegalArgumentException("Um ou ambos os membros não foram encontrados")
                )
            }
            
            // Criar relacionamento
            val relationship = Relationship(
                id = UUID.randomUUID().toString(),
                membro1Id = membro1Id,
                membro2Id = membro2Id,
                tipoRelacionamento = tipoRelacionamento,
                dataInicio = parseDate(dataInicio),
                observacoes = observacoes?.trim(),
                ativo = true,
                userId = userId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            // Validar relacionamento
            val existingRelationships = relationshipRepository.getAllRelationships(userId)
            val members = listOf(member1, member2)
            
            // TODO: Implementar validação com GenealogyValidator
            // genealogyValidator.validateRelationship(relationship, existingRelationships, members)
            
            // Salvar relacionamento
            relationshipRepository.createRelationship(relationship)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseDate(dateString: String?): java.time.LocalDate? {
        if (dateString.isNullOrBlank()) return null
        
        return try {
            java.time.LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}
