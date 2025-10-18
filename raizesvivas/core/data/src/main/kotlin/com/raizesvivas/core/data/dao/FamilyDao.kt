package com.raizesvivas.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raizesvivas.core.data.entity.FamilyEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com famílias no Room Database
 */
@Dao
interface FamilyDao {
    
    @Query("SELECT * FROM familias WHERE user_id = :userId AND ativa = 1")
    fun getAllFamilies(userId: String): Flow<List<FamilyEntity>>
    
    @Query("SELECT * FROM familias WHERE id = :id AND user_id = :userId")
    suspend fun getFamilyById(id: String, userId: String): FamilyEntity?
    
    @Query("SELECT * FROM familias WHERE tipo = 'zero' AND user_id = :userId AND ativa = 1 LIMIT 1")
    suspend fun getFamilyZero(userId: String): FamilyEntity?
    
    @Query("SELECT * FROM familias WHERE tipo = 'subfamilia' AND user_id = :userId AND ativa = 1")
    fun getSubfamilies(userId: String): Flow<List<FamilyEntity>>
    
    @Query("SELECT * FROM familias WHERE familia_pai_id = :parentId AND user_id = :userId AND ativa = 1")
    fun getSubfamiliesByParent(parentId: String, userId: String): Flow<List<FamilyEntity>>
    
    @Query("SELECT COUNT(*) FROM familias WHERE user_id = :userId AND tipo = 'zero' AND ativa = 1")
    suspend fun getFamilyZeroCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: FamilyEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilies(families: List<FamilyEntity>)
    
    @Update
    suspend fun updateFamily(family: FamilyEntity)
    
    @Delete
    suspend fun deleteFamily(family: FamilyEntity)
    
    @Query("UPDATE familias SET ativa = 0 WHERE id = :id AND user_id = :userId")
    suspend fun softDeleteFamily(id: String, userId: String)
    
    @Query("DELETE FROM familias WHERE user_id = :userId")
    suspend fun deleteAllFamilies(userId: String)
}
