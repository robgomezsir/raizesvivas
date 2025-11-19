package com.raizesvivas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raizesvivas.app.data.local.entities.FamiliaPersonalizadaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamiliaPersonalizadaDao {

    @Query("SELECT * FROM familias_personalizadas")
    fun observarTodas(): Flow<List<FamiliaPersonalizadaEntity>>

    @Query("SELECT * FROM familias_personalizadas WHERE familiaId = :familiaId LIMIT 1")
    suspend fun buscarPorId(familiaId: String): FamiliaPersonalizadaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(entity: FamiliaPersonalizadaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(entities: List<FamiliaPersonalizadaEntity>)

    @Query("DELETE FROM familias_personalizadas")
    suspend fun deletarTodas()

    @Update
    suspend fun atualizar(entity: FamiliaPersonalizadaEntity)
}

