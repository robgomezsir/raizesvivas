package com.raizesvivas.core.utils.algorithms

import com.raizesvivas.core.domain.model.Kinship
import com.raizesvivas.core.domain.model.KinshipDegree
import com.raizesvivas.core.domain.model.KinshipType
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.domain.model.RelationshipType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

/**
 * Calculadora de parentesco para o sistema Raízes Vivas
 * 
 * Este é o coração do sistema de genealogia. Calcula o parentesco
 * entre dois membros baseado na família-zero como referência.
 * 
 * ATENÇÃO: Este é o coração do app.
 * TESTAR EXAUSTIVAMENTE antes de integrar.
 */
class KinshipCalculator {
    
    /**
     * Calcula o parentesco entre dois membros
     * 
     * @param member1 Primeiro membro
     * @param member2 Segundo membro
     * @param familyZeroId ID da família-zero como referência
     * @param relationships Lista de relacionamentos para análise
     * @return Resultado do cálculo de parentesco
     */
    suspend fun calculate(
        member1: Member,
        member2: Member,
        familyZeroId: String,
        relationships: List<Relationship>
    ): Result<Kinship> = withContext(Dispatchers.Default) {
        try {
            // Validações básicas
            if (member1.id == member2.id) {
                return@withContext Result.success(
                    createSelfKinship(member1, familyZeroId)
                )
            }
            
            // Buscar ancestrais comuns
            val commonAncestors = findCommonAncestors(member1, member2, relationships)
            
            if (commonAncestors.isEmpty()) {
                return@withContext Result.success(
                    createUnrelatedKinship(member1, member2, familyZeroId)
                )
            }
            
            // Calcular distância geracional
            val generationalDistance = calculateGenerationalDistance(
                member1, member2, commonAncestors, relationships
            )
            
            // Determinar tipo de parentesco
            val kinshipType = determineKinshipType(
                member1, member2, generationalDistance, relationships
            )
            
            // Calcular grau de parentesco
            val kinshipDegree = calculateKinshipDegree(generationalDistance)
            
            // Criar resultado
            val kinship = createKinship(
                member1, member2, kinshipType, kinshipDegree,
                generationalDistance, familyZeroId
            )
            
            Result.success(kinship)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Busca ancestrais comuns entre dois membros
     */
    private fun findCommonAncestors(
        member1: Member,
        member2: Member,
        relationships: List<Relationship>
    ): List<String> {
        val ancestors1 = getAncestors(member1.id, relationships)
        val ancestors2 = getAncestors(member2.id, relationships)
        
        return ancestors1.intersect(ancestors2).toList()
    }
    
    /**
     * Obtém todos os ancestrais de um membro
     */
    private fun getAncestors(
        memberId: String,
        relationships: List<Relationship>
    ): Set<String> {
        val ancestors = mutableSetOf<String>()
        val toProcess = mutableListOf(memberId)
        
        while (toProcess.isNotEmpty()) {
            val current = toProcess.removeAt(0)
            
            // Buscar pais
            val parents = relationships.filter { 
                it.membro2Id == current && 
                (it.tipoRelacionamento == RelationshipType.PAI || 
                 it.tipoRelacionamento == RelationshipType.MAE) &&
                it.ativo
            }
            
            parents.forEach { parent ->
                if (ancestors.add(parent.membro1Id)) {
                    toProcess.add(parent.membro1Id)
                }
            }
        }
        
        return ancestors
    }
    
    /**
     * Calcula a distância geracional entre dois membros
     */
    private fun calculateGenerationalDistance(
        member1: Member,
        member2: Member,
        commonAncestors: List<String>,
        relationships: List<Relationship>
    ): Int {
        if (commonAncestors.isEmpty()) return -1
        
        // Encontrar o ancestral comum mais próximo
        val closestAncestor = commonAncestors.minByOrNull { ancestor ->
            getDistanceToAncestor(member1.id, ancestor, relationships) +
            getDistanceToAncestor(member2.id, ancestor, relationships)
        } ?: return -1
        
        val distance1 = getDistanceToAncestor(member1.id, closestAncestor, relationships)
        val distance2 = getDistanceToAncestor(member2.id, closestAncestor, relationships)
        
        return distance1 + distance2
    }
    
    /**
     * Calcula a distância de um membro até um ancestral
     */
    private fun getDistanceToAncestor(
        memberId: String,
        ancestorId: String,
        relationships: List<Relationship>
    ): Int {
        val visited = mutableSetOf<String>()
        val queue = mutableListOf(Pair(memberId, 0))
        
        while (queue.isNotEmpty()) {
            val (current, distance) = queue.removeAt(0)
            
            if (current == ancestorId) {
                return distance
            }
            
            if (visited.add(current)) {
                // Buscar pais
                val parents = relationships.filter { 
                    it.membro2Id == current && 
                    (it.tipoRelacionamento == RelationshipType.PAI || 
                     it.tipoRelacionamento == RelationshipType.MAE) &&
                    it.ativo
                }
                
                parents.forEach { parent ->
                    queue.add(Pair(parent.membro1Id, distance + 1))
                }
            }
        }
        
        return -1
    }
    
    /**
     * Determina o tipo de parentesco baseado na distância geracional
     */
    private fun determineKinshipType(
        member1: Member,
        member2: Member,
        generationalDistance: Int,
        relationships: List<Relationship>
    ): KinshipType {
        return when (generationalDistance) {
            0 -> KinshipType.IRMAO // Mesmo nível, provavelmente irmãos
            1 -> {
                // Verificar se é pai/filho
                if (isParentChild(member1.id, member2.id, relationships)) {
                    if (isParent(member1.id, member2.id, relationships)) {
                        KinshipType.PAI
                    } else {
                        KinshipType.FILHO
                    }
                } else {
                    KinshipType.IRMAO
                }
            }
            2 -> {
                // Verificar se é avô/neto
                if (isGrandparentGrandchild(member1.id, member2.id, relationships)) {
                    if (isGrandparent(member1.id, member2.id, relationships)) {
                        KinshipType.AVO
                    } else {
                        KinshipType.NETO
                    }
                } else {
                    KinshipType.TIO // Tio/sobrinho
                }
            }
            3 -> KinshipType.PRIMO // Primos
            4 -> KinshipType.PRIMO_SEGUNDO // Primos segundos
            else -> KinshipType.PRIMO_SEGUNDO // Parentesco distante
        }
    }
    
    /**
     * Verifica se um membro é pai/mãe de outro
     */
    private fun isParentChild(member1Id: String, member2Id: String, relationships: List<Relationship>): Boolean {
        return relationships.any { 
            it.membro1Id == member1Id && it.membro2Id == member2Id &&
            (it.tipoRelacionamento == RelationshipType.PAI || it.tipoRelacionamento == RelationshipType.MAE) &&
            it.ativo
        }
    }
    
    /**
     * Verifica se um membro é pai de outro
     */
    private fun isParent(member1Id: String, member2Id: String, relationships: List<Relationship>): Boolean {
        return relationships.any { 
            it.membro1Id == member1Id && it.membro2Id == member2Id &&
            (it.tipoRelacionamento == RelationshipType.PAI || it.tipoRelacionamento == RelationshipType.MAE) &&
            it.ativo
        }
    }
    
    /**
     * Verifica se é relacionamento avô/neto
     */
    private fun isGrandparentGrandchild(member1Id: String, member2Id: String, relationships: List<Relationship>): Boolean {
        // Buscar se member1 é avô de member2
        val parents = relationships.filter { 
            it.membro2Id == member2Id && 
            (it.tipoRelacionamento == RelationshipType.PAI || it.tipoRelacionamento == RelationshipType.MAE) &&
            it.ativo
        }
        
        return parents.any { parent ->
            relationships.any { 
                it.membro1Id == member1Id && it.membro2Id == parent.membro1Id &&
                (it.tipoRelacionamento == RelationshipType.PAI || it.tipoRelacionamento == RelationshipType.MAE) &&
                it.ativo
            }
        }
    }
    
    /**
     * Verifica se um membro é avô de outro
     */
    private fun isGrandparent(member1Id: String, member2Id: String, relationships: List<Relationship>): Boolean {
        return isGrandparentGrandchild(member1Id, member2Id, relationships)
    }
    
    /**
     * Calcula o grau de parentesco
     */
    private fun calculateKinshipDegree(generationalDistance: Int): KinshipDegree {
        return when (generationalDistance) {
            0 -> KinshipDegree.FIRST
            1 -> KinshipDegree.FIRST
            2 -> KinshipDegree.SECOND
            3 -> KinshipDegree.THIRD
            4 -> KinshipDegree.FOURTH
            else -> KinshipDegree.DISTANT
        }
    }
    
    /**
     * Cria kinship para o mesmo membro
     */
    private fun createSelfKinship(member: Member, familyZeroId: String): Kinship {
        return Kinship(
            id = UUID.randomUUID().toString(),
            membro1Id = member.id,
            membro2Id = member.id,
            tipoParentesco = KinshipType.IRMAO, // Mesmo indivíduo
            grauParentesco = KinshipDegree.ZERO,
            distanciaGeracional = 0,
            familiaReferenciaId = familyZeroId,
            dataCalculo = LocalDateTime.now(),
            ativo = true,
            userId = member.userId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Cria kinship para membros sem parentesco
     */
    private fun createUnrelatedKinship(member1: Member, member2: Member, familyZeroId: String): Kinship {
        return Kinship(
            id = UUID.randomUUID().toString(),
            membro1Id = member1.id,
            membro2Id = member2.id,
            tipoParentesco = KinshipType.PRIMO_SEGUNDO, // Sem parentesco conhecido
            grauParentesco = KinshipDegree.DISTANT,
            distanciaGeracional = -1,
            familiaReferenciaId = familyZeroId,
            dataCalculo = LocalDateTime.now(),
            ativo = true,
            userId = member1.userId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Cria kinship com os dados calculados
     */
    private fun createKinship(
        member1: Member,
        member2: Member,
        kinshipType: KinshipType,
        kinshipDegree: KinshipDegree,
        generationalDistance: Int,
        familyZeroId: String
    ): Kinship {
        return Kinship(
            id = UUID.randomUUID().toString(),
            membro1Id = member1.id,
            membro2Id = member2.id,
            tipoParentesco = kinshipType,
            grauParentesco = kinshipDegree,
            distanciaGeracional = generationalDistance,
            familiaReferenciaId = familyZeroId,
            dataCalculo = LocalDateTime.now(),
            ativo = true,
            userId = member1.userId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
