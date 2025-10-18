package com.raizesvivas.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raizesvivas.core.data.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com membros no Room Database
 */
@Dao
interface MemberDao {
    
    @Query("SELECT * FROM membros WHERE user_id = :userId AND ativo = 1")
    fun getAllMembers(userId: String): Flow<List<MemberEntity>>
    
    @Query("SELECT * FROM membros WHERE id = :id AND user_id = :userId")
    suspend fun getMemberById(id: String, userId: String): MemberEntity?
    
    @Query("SELECT * FROM membros WHERE nome_completo LIKE :searchQuery AND user_id = :userId AND ativo = 1")
    fun searchMembers(searchQuery: String, userId: String): Flow<List<MemberEntity>>
    
    @Query("SELECT * FROM membros WHERE nivel_na_arvore = :level AND user_id = :userId AND ativo = 1")
    fun getMembersByLevel(level: Int, userId: String): Flow<List<MemberEntity>>
    
    @Query("SELECT COUNT(*) FROM membros WHERE user_id = :userId AND ativo = 1")
    suspend fun getMemberCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)
    
    @Update
    suspend fun updateMember(member: MemberEntity)
    
    @Delete
    suspend fun deleteMember(member: MemberEntity)
    
    @Query("UPDATE membros SET ativo = 0 WHERE id = :id AND user_id = :userId")
    suspend fun softDeleteMember(id: String, userId: String)
    
    @Query("DELETE FROM membros WHERE user_id = :userId")
    suspend fun deleteAllMembers(userId: String)
}
