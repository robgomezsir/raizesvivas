package com.raizesvivas.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raizesvivas.core.data.entity.KinshipEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com parentescos no Room Database
 */
@Dao
interface KinshipDao {
    
    @Query("SELECT * FROM parentescos_calculados WHERE user_id = :userId AND ativo = 1")
    fun getAllKinships(userId: String): Flow<List<KinshipEntity>>
    
    @Query("SELECT * FROM parentescos_calculados WHERE id = :id AND user_id = :userId")
    suspend fun getKinshipById(id: String, userId: String): KinshipEntity?
    
    @Query("SELECT * FROM parentescos_calculados WHERE (membro_1_id = :memberId OR membro_2_id = :memberId) AND user_id = :userId AND ativo = 1")
    fun getKinshipsByMember(memberId: String, userId: String): Flow<List<KinshipEntity>>
    
    @Query("SELECT * FROM parentescos_calculados WHERE membro_1_id = :member1Id AND membro_2_id = :member2Id AND user_id = :userId AND ativo = 1 LIMIT 1")
    suspend fun getKinshipBetweenMembers(member1Id: String, member2Id: String, userId: String): KinshipEntity?
    
    @Query("SELECT * FROM parentescos_calculados WHERE tipo_parentesco = :type AND user_id = :userId AND ativo = 1")
    fun getKinshipsByType(type: String, userId: String): Flow<List<KinshipEntity>>
    
    @Query("SELECT * FROM parentescos_calculados WHERE familia_referencia_id = :familyId AND user_id = :userId AND ativo = 1")
    fun getKinshipsByFamily(familyId: String, userId: String): Flow<List<KinshipEntity>>
    
    @Query("SELECT COUNT(*) FROM parentescos_calculados WHERE user_id = :userId AND ativo = 1")
    suspend fun getKinshipCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKinship(kinship: KinshipEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKinships(kinships: List<KinshipEntity>)
    
    @Update
    suspend fun updateKinship(kinship: KinshipEntity)
    
    @Delete
    suspend fun deleteKinship(kinship: KinshipEntity)
    
    @Query("UPDATE parentescos_calculados SET ativo = 0 WHERE id = :id AND user_id = :userId")
    suspend fun softDeleteKinship(id: String, userId: String)
    
    @Query("DELETE FROM parentescos_calculados WHERE user_id = :userId")
    suspend fun deleteAllKinships(userId: String)
    
    @Query("DELETE FROM parentescos_calculados WHERE familia_referencia_id = :familyId AND user_id = :userId")
    suspend fun deleteKinshipsByFamily(familyId: String, userId: String)
}
