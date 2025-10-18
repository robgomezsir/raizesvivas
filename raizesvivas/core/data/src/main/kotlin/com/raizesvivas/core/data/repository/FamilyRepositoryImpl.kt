package com.raizesvivas.core.data.repository

import com.raizesvivas.core.data.dao.FamilyDao
import com.raizesvivas.core.data.entity.FamilyEntity
import com.raizesvivas.core.data.mapper.FamilyMapper
import com.raizesvivas.core.data.source.remote.SupabaseClient
import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementação do repositório de famílias
 * 
 * Gerencia dados de famílias usando Room Database local
 * e Supabase para sincronização.
 */
@Singleton
class FamilyRepositoryImpl @Inject constructor(
    private val familyDao: FamilyDao,
    private val supabaseClient: SupabaseClient
) : FamilyRepository {
    
    override fun getAllFamilies(userId: String): Flow<List<Family>> {
        return familyDao.getAllFamilies(userId).map { entities ->
            entities.map { FamilyMapper.toDomain(it) }
        }
    }
    
    override suspend fun getFamilyById(id: String, userId: String): Family? {
        val entity = familyDao.getFamilyById(id, userId)
        return entity?.let { FamilyMapper.toDomain(it) }
    }
    
    override suspend fun getFamilyZero(userId: String): Family? {
        val entity = familyDao.getFamilyZero(userId)
        return entity?.let { FamilyMapper.toDomain(it) }
    }
    
    override fun getSubfamilies(userId: String): Flow<List<Family>> {
        return familyDao.getSubfamilies(userId).map { entities ->
            entities.map { FamilyMapper.toDomain(it) }
        }
    }
    
    override fun getSubfamiliesByParent(parentId: String, userId: String): Flow<List<Family>> {
        return familyDao.getSubfamiliesByParent(parentId, userId).map { entities ->
            entities.map { FamilyMapper.toDomain(it) }
        }
    }
    
    override suspend fun createFamily(family: Family): Result<Family> {
        return try {
            val entity = FamilyMapper.toEntity(family)
            familyDao.insertFamily(entity)
            
            // TODO: Sincronizar com Supabase
            // supabaseClient.client.from("familias").insert(entity.toSupabaseMap())
            
            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateFamily(family: Family): Result<Family> {
        return try {
            val entity = FamilyMapper.toEntity(family)
            familyDao.updateFamily(entity)
            
            // TODO: Sincronizar com Supabase
            
            Result.success(family)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteFamily(id: String, userId: String): Result<Unit> {
        return try {
            familyDao.softDeleteFamily(id, userId)
            
            // TODO: Sincronizar com Supabase
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun hasFamilyZero(userId: String): Boolean {
        return familyDao.getFamilyZeroCount(userId) > 0
    }
    
    override suspend fun createFamilyZero(userId: String, nome: String): Result<Family> {
        return try {
            val familyZero = Family(
                id = java.util.UUID.randomUUID().toString(),
                nome = nome,
                tipo = com.raizesvivas.core.domain.model.FamilyType.ZERO,
                familiaPaiId = null,
                criadaPorCasamento = false,
                dataCriacao = java.time.LocalDateTime.now(),
                nivelHierarquico = 0,
                ativa = true,
                userId = userId,
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            )
            
            createFamily(familyZero)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
