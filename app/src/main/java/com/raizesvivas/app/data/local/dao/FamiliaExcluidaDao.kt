package com.raizesvivas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.raizesvivas.app.data.local.entities.FamiliaExcluidaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações de famílias excluídas no banco de dados local
 */
@Dao
interface FamiliaExcluidaDao {
    
    /**
     * Observa todas as famílias excluídas
     */
    @Query("SELECT * FROM familias_excluidas ORDER BY excluidoEm DESC")
    fun observarTodas(): Flow<List<FamiliaExcluidaEntity>>
    
    /**
     * Busca uma família excluída por ID
     */
    @Query("SELECT * FROM familias_excluidas WHERE familiaId = :familiaId")
    suspend fun buscarPorId(familiaId: String): FamiliaExcluidaEntity?
    
    /**
     * Insere ou atualiza uma família excluída
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(familiaExcluida: FamiliaExcluidaEntity)
    
    /**
     * Insere ou atualiza múltiplas famílias excluídas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(familiasExcluidas: List<FamiliaExcluidaEntity>)
    
    /**
     * Remove uma família excluída por ID (para restaurar)
     */
    @Query("DELETE FROM familias_excluidas WHERE familiaId = :familiaId")
    suspend fun deletarPorId(familiaId: String)
    
    /**
     * Remove todas as famílias excluídas
     */
    @Query("DELETE FROM familias_excluidas")
    suspend fun deletarTodas()
    
    /**
     * Busca famílias que precisam ser sincronizadas
     */
    @Query("SELECT * FROM familias_excluidas WHERE precisaSincronizar = 1")
    suspend fun buscarPendenteSincronizacao(): List<FamiliaExcluidaEntity>
}
