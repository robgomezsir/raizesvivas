package com.raizesvivas.core.data.repository

import com.raizesvivas.core.data.dao.MemberDao
import com.raizesvivas.core.data.entity.MemberEntity
import com.raizesvivas.core.data.mapper.MemberMapper
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de membros
 * 
 * Gerencia dados de membros usando Room Database local
 * e Supabase para sincronização.
 */
@Singleton
class MemberRepositoryImpl @Inject constructor(
    private val memberDao: MemberDao
) : MemberRepository {
    
    override fun getAllMembers(userId: String): Flow<List<Member>> {
        return memberDao.getAllMembers(userId).map { entities ->
            entities.map { MemberMapper.toDomain(it) }
        }
    }
    
    override suspend fun getMemberById(id: String, userId: String): Member? {
        val entity = memberDao.getMemberById(id, userId)
        return entity?.let { MemberMapper.toDomain(it) }
    }
    
    override fun searchMembers(searchQuery: String, userId: String): Flow<List<Member>> {
        val query = "%$searchQuery%"
        return memberDao.searchMembers(query, userId).map { entities ->
            entities.map { MemberMapper.toDomain(it) }
        }
    }
    
    override fun getMembersByLevel(level: Int, userId: String): Flow<List<Member>> {
        return memberDao.getMembersByLevel(level, userId).map { entities ->
            entities.map { MemberMapper.toDomain(it) }
        }
    }
    
    override suspend fun addMember(member: Member): Result<Member> {
        return try {
            val entity = MemberMapper.toEntity(member)
            memberDao.insertMember(entity)
            
            // TODO: Sincronizar com Supabase
            
            Result.success(member)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateMember(member: Member): Result<Member> {
        return try {
            val entity = MemberMapper.toEntity(member)
            memberDao.updateMember(entity)
            
            // TODO: Sincronizar com Supabase
            
            Result.success(member)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMember(id: String, userId: String): Result<Unit> {
        return try {
            memberDao.softDeleteMember(id, userId)
            
            // TODO: Sincronizar com Supabase
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getMemberCount(userId: String): Int {
        return memberDao.getMemberCount(userId)
    }
}
