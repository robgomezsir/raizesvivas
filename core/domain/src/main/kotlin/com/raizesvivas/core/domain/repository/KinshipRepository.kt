package com.raizesvivas.core.domain.repository

import com.raizesvivas.core.domain.model.Kinship
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de parentescos
 * 
 * Define os contratos para operações com parentescos
 * no sistema Raízes Vivas.
 */
interface KinshipRepository {
    
    /**
     * Obtém todos os parentescos do usuário
     */
    fun getAllKinships(userId: String): Flow<List<Kinship>>
    
    /**
     * Obtém um parentesco por ID
     */
    suspend fun getKinshipById(id: String, userId: String): Kinship?
    
    /**
     * Obtém parentescos de um membro específico
     */
    fun getKinshipsByMember(memberId: String, userId: String): Flow<List<Kinship>>
    
    /**
     * Obtém parentesco entre dois membros específicos
     */
    suspend fun getKinshipBetweenMembers(member1Id: String, member2Id: String, userId: String): Kinship?
    
    /**
     * Obtém parentescos por tipo
     */
    fun getKinshipsByType(type: String, userId: String): Flow<List<Kinship>>
    
    /**
     * Obtém parentescos por família de referência
     */
    fun getKinshipsByFamily(familyId: String, userId: String): Flow<List<Kinship>>
    
    /**
     * Salva um parentesco calculado
     */
    suspend fun saveKinship(kinship: Kinship): Result<Kinship>
    
    /**
     * Atualiza um parentesco existente
     */
    suspend fun updateKinship(kinship: Kinship): Result<Kinship>
    
    /**
     * Exclui um parentesco
     */
    suspend fun deleteKinship(id: String, userId: String): Result<Unit>
    
    /**
     * Exclui todos os parentescos de uma família
     */
    suspend fun deleteKinshipsByFamily(familyId: String, userId: String): Result<Unit>
    
    /**
     * Obtém contagem de parentescos
     */
    suspend fun getKinshipCount(userId: String): Int
}
