package com.raizesvivas.core.domain.repository

import com.raizesvivas.core.domain.model.Family
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de famílias
 * 
 * Define os contratos para operações com famílias
 * no sistema Raízes Vivas.
 */
interface FamilyRepository {
    
    /**
     * Obtém todas as famílias do usuário
     */
    fun getAllFamilies(userId: String): Flow<List<Family>>
    
    /**
     * Obtém uma família por ID
     */
    suspend fun getFamilyById(id: String, userId: String): Family?
    
    /**
     * Obtém a família-zero do usuário
     */
    suspend fun getFamilyZero(userId: String): Family?
    
    /**
     * Obtém todas as subfamílias do usuário
     */
    fun getSubfamilies(userId: String): Flow<List<Family>>
    
    /**
     * Obtém subfamílias por família pai
     */
    fun getSubfamiliesByParent(parentId: String, userId: String): Flow<List<Family>>
    
    /**
     * Cria uma nova família
     */
    suspend fun createFamily(family: Family): Result<Family>
    
    /**
     * Atualiza uma família existente
     */
    suspend fun updateFamily(family: Family): Result<Family>
    
    /**
     * Exclui uma família
     */
    suspend fun deleteFamily(id: String, userId: String): Result<Unit>
    
    /**
     * Verifica se o usuário tem uma família-zero
     */
    suspend fun hasFamilyZero(userId: String): Boolean
    
    /**
     * Cria família-zero automaticamente
     */
    suspend fun createFamilyZero(userId: String, nome: String): Result<Family>
}
