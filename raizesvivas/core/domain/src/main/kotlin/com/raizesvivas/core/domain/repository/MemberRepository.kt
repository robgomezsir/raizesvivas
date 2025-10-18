package com.raizesvivas.core.domain.repository

import com.raizesvivas.core.domain.model.Member
import kotlinx.coroutines.flow.Flow

/**
 * Interface do repositório de membros
 * 
 * Define os contratos para operações com membros
 * no sistema Raízes Vivas.
 */
interface MemberRepository {
    
    /**
     * Obtém todos os membros do usuário
     */
    fun getAllMembers(userId: String): Flow<List<Member>>
    
    /**
     * Obtém um membro por ID
     */
    suspend fun getMemberById(id: String, userId: String): Member?
    
    /**
     * Busca membros por nome
     */
    fun searchMembers(searchQuery: String, userId: String): Flow<List<Member>>
    
    /**
     * Obtém membros por nível na árvore
     */
    fun getMembersByLevel(level: Int, userId: String): Flow<List<Member>>
    
    /**
     * Adiciona um novo membro
     */
    suspend fun addMember(member: Member): Result<Member>
    
    /**
     * Atualiza um membro existente
     */
    suspend fun updateMember(member: Member): Result<Member>
    
    /**
     * Exclui um membro
     */
    suspend fun deleteMember(id: String, userId: String): Result<Unit>
    
    /**
     * Obtém contagem de membros
     */
    suspend fun getMemberCount(userId: String): Int
}
