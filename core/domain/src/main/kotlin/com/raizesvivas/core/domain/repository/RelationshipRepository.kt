package com.raizesvivas.core.domain.repository

import com.raizesvivas.core.domain.model.Relationship
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de relacionamentos
 * 
 * Define os contratos para operações com relacionamentos
 * no sistema Raízes Vivas.
 */
interface RelationshipRepository {
    
    /**
     * Obtém todos os relacionamentos do usuário
     */
    fun getAllRelationships(userId: String): Flow<List<Relationship>>
    
    /**
     * Obtém um relacionamento por ID
     */
    suspend fun getRelationshipById(id: String, userId: String): Relationship?
    
    /**
     * Obtém relacionamentos de um membro específico
     */
    fun getRelationshipsByMember(memberId: String, userId: String): Flow<List<Relationship>>
    
    /**
     * Obtém relacionamentos por tipo
     */
    fun getRelationshipsByType(type: String, userId: String): Flow<List<Relationship>>
    
    /**
     * Cria um novo relacionamento
     */
    suspend fun createRelationship(relationship: Relationship): Result<Relationship>
    
    /**
     * Atualiza um relacionamento existente
     */
    suspend fun updateRelationship(relationship: Relationship): Result<Relationship>
    
    /**
     * Exclui um relacionamento
     */
    suspend fun deleteRelationship(id: String, userId: String): Result<Unit>
    
    /**
     * Obtém contagem de relacionamentos
     */
    suspend fun getRelationshipCount(userId: String): Int
}
