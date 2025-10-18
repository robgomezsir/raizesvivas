package com.raizesvivas.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raizesvivas.core.data.entity.RelationshipEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com relacionamentos no Room Database
 */
@Dao
interface RelationshipDao {
    
    @Query("SELECT * FROM relacionamentos WHERE user_id = :userId AND ativo = 1")
    fun getAllRelationships(userId: String): Flow<List<RelationshipEntity>>
    
    @Query("SELECT * FROM relacionamentos WHERE id = :id AND user_id = :userId")
    suspend fun getRelationshipById(id: String, userId: String): RelationshipEntity?
    
    @Query("SELECT * FROM relacionamentos WHERE membro_1_id = :memberId AND user_id = :userId AND ativo = 1")
    fun getRelationshipsByMember1(memberId: String, userId: String): Flow<List<RelationshipEntity>>
    
    @Query("SELECT * FROM relacionamentos WHERE membro_2_id = :memberId AND user_id = :userId AND ativo = 1")
    fun getRelationshipsByMember2(memberId: String, userId: String): Flow<List<RelationshipEntity>>
    
    @Query("SELECT * FROM relacionamentos WHERE (membro_1_id = :memberId OR membro_2_id = :memberId) AND user_id = :userId AND ativo = 1")
    fun getRelationshipsByMember(memberId: String, userId: String): Flow<List<RelationshipEntity>>
    
    @Query("SELECT * FROM relacionamentos WHERE tipo_relacionamento = :type AND user_id = :userId AND ativo = 1")
    fun getRelationshipsByType(type: String, userId: String): Flow<List<RelationshipEntity>>
    
    @Query("SELECT COUNT(*) FROM relacionamentos WHERE user_id = :userId AND ativo = 1")
    suspend fun getRelationshipCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: RelationshipEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(relationships: List<RelationshipEntity>)
    
    @Update
    suspend fun updateRelationship(relationship: RelationshipEntity)
    
    @Delete
    suspend fun deleteRelationship(relationship: RelationshipEntity)
    
    @Query("UPDATE relacionamentos SET ativo = 0 WHERE id = :id AND user_id = :userId")
    suspend fun softDeleteRelationship(id: String, userId: String)
    
    @Query("DELETE FROM relacionamentos WHERE user_id = :userId")
    suspend fun deleteAllRelationships(userId: String)
}
