package com.raizesvivas.core.domain.usecase.kinship

import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.model.Kinship
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.domain.repository.FamilyRepository
import com.raizesvivas.core.domain.repository.KinshipRepository
import com.raizesvivas.core.domain.repository.MemberRepository
import com.raizesvivas.core.domain.repository.RelationshipRepository
import com.raizesvivas.core.utils.algorithms.KinshipCalculator
import javax.inject.Inject

/**
 * Use Case para calcular parentesco
 * 
 * Calcula o parentesco entre dois membros usando
 * o algoritmo de parentesco do sistema.
 */
class CalculateKinshipUseCase @Inject constructor(
    private val kinshipRepository: KinshipRepository,
    private val memberRepository: MemberRepository,
    private val relationshipRepository: RelationshipRepository,
    private val familyRepository: FamilyRepository,
    private val kinshipCalculator: KinshipCalculator
) {
    
    suspend operator fun invoke(
        member1Id: String,
        member2Id: String,
        userId: String
    ): Result<Kinship> {
        return try {
            // Buscar membros
            val member1 = memberRepository.getMemberById(member1Id, userId)
            val member2 = memberRepository.getMemberById(member2Id, userId)
            
            if (member1 == null || member2 == null) {
                return Result.failure(
                    IllegalArgumentException("Um ou ambos os membros não foram encontrados")
                )
            }
            
            // Buscar família-zero
            val familyZero = familyRepository.getFamilyZero(userId)
            if (familyZero == null) {
                return Result.failure(
                    IllegalArgumentException("Família-zero não encontrada")
                )
            }
            
            // Verificar se já existe cálculo
            val existingKinship = kinshipRepository.getKinshipBetweenMembers(member1Id, member2Id, userId)
            if (existingKinship != null) {
                return Result.success(existingKinship)
            }
            
            // Buscar relacionamentos
            val relationships = relationshipRepository.getAllRelationships(userId)
            
            // Calcular parentesco
            val kinshipResult = kinshipCalculator.calculate(
                member1 = member1,
                member2 = member2,
                familyZeroId = familyZero.id,
                relationships = relationships
            )
            
            if (kinshipResult.isSuccess) {
                val kinship = kinshipResult.getOrNull()!!
                kinshipRepository.saveKinship(kinship)
                Result.success(kinship)
            } else {
                Result.failure(kinshipResult.exceptionOrNull()!!)
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
