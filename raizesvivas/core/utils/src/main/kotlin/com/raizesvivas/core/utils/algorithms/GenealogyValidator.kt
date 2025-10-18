package com.raizesvivas.core.utils.algorithms

import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.domain.model.RelationshipType
import java.time.LocalDate

/**
 * Validador de genealogia para o sistema Raízes Vivas
 * 
 * Valida relacionamentos familiares e previne problemas
 * como loops genealógicos e datas impossíveis.
 */
class GenealogyValidator {
    
    /**
     * Valida um relacionamento antes de ser criado
     */
    fun validateRelationship(
        relationship: Relationship,
        existingRelationships: List<Relationship>,
        members: List<Member>
    ): Result<Unit> {
        return try {
            // 1. Validar membros existem
            validateMembersExist(relationship, members)
            
            // 2. Validar datas
            validateDates(relationship)
            
            // 3. Validar se não cria loop genealógico
            validateNoGenealogicalLoop(relationship, existingRelationships)
            
            // 4. Validar relacionamentos impossíveis
            validatePossibleRelationship(relationship, existingRelationships, members)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Valida se os membros existem
     */
    private fun validateMembersExist(relationship: Relationship, members: List<Member>) {
        val member1Exists = members.any { it.id == relationship.membro1Id }
        val member2Exists = members.any { it.id == relationship.membro2Id }
        
        if (!member1Exists) {
            throw IllegalArgumentException("Membro 1 não encontrado: ${relationship.membro1Id}")
        }
        
        if (!member2Exists) {
            throw IllegalArgumentException("Membro 2 não encontrado: ${relationship.membro2Id}")
        }
    }
    
    /**
     * Valida datas do relacionamento
     */
    private fun validateDates(relationship: Relationship) {
        relationship.dataInicio?.let { inicio ->
            relationship.dataFim?.let { fim ->
                if (fim.isBefore(inicio)) {
                    throw IllegalArgumentException("Data de fim não pode ser anterior à data de início")
                }
            }
        }
    }
    
    /**
     * Valida se o relacionamento não cria um loop genealógico
     */
    private fun validateNoGenealogicalLoop(
        relationship: Relationship,
        existingRelationships: List<Relationship>
    ) {
        // Criar uma cópia temporária com o novo relacionamento
        val testRelationships = existingRelationships + relationship
        
        // Verificar se cria loop
        if (createsGenealogicalLoop(relationship.membro1Id, relationship.membro2Id, testRelationships)) {
            throw IllegalArgumentException("Este relacionamento criaria um loop genealógico")
        }
    }
    
    /**
     * Verifica se um relacionamento criaria um loop genealógico
     */
    private fun createsGenealogicalLoop(
        member1Id: String,
        member2Id: String,
        relationships: List<Relationship>
    ): Boolean {
        // Usar busca em profundidade para detectar ciclos
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()
        
        return hasCycle(member1Id, relationships, visited, recursionStack) ||
               hasCycle(member2Id, relationships, visited, recursionStack)
    }
    
    /**
     * Verifica se há ciclo usando DFS
     */
    private fun hasCycle(
        memberId: String,
        relationships: List<Relationship>,
        visited: MutableSet<String>,
        recursionStack: MutableSet<String>
    ): Boolean {
        visited.add(memberId)
        recursionStack.add(memberId)
        
        // Buscar todos os relacionamentos onde este membro é pai/mãe
        val parentRelationships = relationships.filter { 
            it.membro1Id == memberId && 
            (it.tipoRelacionamento == RelationshipType.PAI || it.tipoRelacionamento == RelationshipType.MAE) &&
            it.ativo
        }
        
        for (relationship in parentRelationships) {
            val childId = relationship.membro2Id
            
            if (!visited.contains(childId)) {
                if (hasCycle(childId, relationships, visited, recursionStack)) {
                    return true
                }
            } else if (recursionStack.contains(childId)) {
                return true // Ciclo encontrado
            }
        }
        
        recursionStack.remove(memberId)
        return false
    }
    
    /**
     * Valida se o relacionamento é possível baseado em datas de nascimento
     */
    private fun validatePossibleRelationship(
        relationship: Relationship,
        existingRelationships: List<Relationship>,
        members: List<Member>
    ) {
        val member1 = members.find { it.id == relationship.membro1Id }
        val member2 = members.find { it.id == relationship.membro2Id }
        
        if (member1 == null || member2 == null) return
        
        // Validar idade mínima para ser pai/mãe
        if (relationship.tipoRelacionamento == RelationshipType.PAI || 
            relationship.tipoRelacionamento == RelationshipType.MAE) {
            validateParentChildAge(member1, member2)
        }
        
        // Validar idade mínima para ser avô/avó
        if (relationship.tipoRelacionamento == RelationshipType.AVO || 
            relationship.tipoRelacionamento == RelationshipType.AVO_FEMININO) {
            validateGrandparentGrandchildAge(member1, member2)
        }
    }
    
    /**
     * Valida idade mínima para ser pai/mãe
     */
    private fun validateParentChildAge(parent: Member, child: Member) {
        val parentBirth = parent.dataNascimento
        val childBirth = child.dataNascimento
        
        if (parentBirth != null && childBirth != null) {
            val ageDifference = parentBirth.until(childBirth).years
            
            if (ageDifference < 13) {
                throw IllegalArgumentException("Diferença de idade muito pequena para ser pai/mãe")
            }
            
            if (ageDifference > 80) {
                throw IllegalArgumentException("Diferença de idade muito grande para ser pai/mãe")
            }
        }
    }
    
    /**
     * Valida idade mínima para ser avô/avó
     */
    private fun validateGrandparentGrandchildAge(grandparent: Member, grandchild: Member) {
        val grandparentBirth = grandparent.dataNascimento
        val grandchildBirth = grandchild.dataNascimento
        
        if (grandparentBirth != null && grandchildBirth != null) {
            val ageDifference = grandparentBirth.until(grandchildBirth).years
            
            if (ageDifference < 25) {
                throw IllegalArgumentException("Diferença de idade muito pequena para ser avô/avó")
            }
            
            if (ageDifference > 120) {
                throw IllegalArgumentException("Diferença de idade muito grande para ser avô/avó")
            }
        }
    }
    
    /**
     * Valida se um membro pode ser adicionado
     */
    fun validateMember(member: Member): Result<Unit> {
        return try {
            // Validar nome obrigatório
            if (member.nomeCompleto.isBlank()) {
                throw IllegalArgumentException("Nome completo é obrigatório")
            }
            
            // Validar datas de nascimento e falecimento
            member.dataNascimento?.let { birth ->
                member.dataFalecimento?.let { death ->
                    if (death.isBefore(birth)) {
                        throw IllegalArgumentException("Data de falecimento não pode ser anterior à data de nascimento")
                    }
                }
            }
            
            // Validar nível na árvore
            if (member.nivelNaArvore < 0) {
                throw IllegalArgumentException("Nível na árvore deve ser positivo")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
