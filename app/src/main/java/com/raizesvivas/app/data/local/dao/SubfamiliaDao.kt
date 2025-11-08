package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.SubfamiliaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com subfamílias no banco local
 */
@Dao
interface SubfamiliaDao {
    
    // ============================================
    // CONSULTAS (SELECT)
    // ============================================
    
    /**
     * Observa todas as subfamílias ordenadas por nível hierárquico
     */
    @Query("SELECT * FROM subfamilias WHERE ativa = 1 ORDER BY nivelHierarquico ASC, nome ASC")
    fun observarTodasSubfamilias(): Flow<List<SubfamiliaEntity>>
    
    /**
     * Busca todas as subfamílias (uma vez)
     */
    @Query("SELECT * FROM subfamilias WHERE ativa = 1 ORDER BY nivelHierarquico ASC, nome ASC")
    suspend fun buscarTodasSubfamilias(): List<SubfamiliaEntity>
    
    /**
     * Busca subfamília por ID
     */
    @Query("SELECT * FROM subfamilias WHERE id = :subfamiliaId")
    suspend fun buscarPorId(subfamiliaId: String): SubfamiliaEntity?
    
    /**
     * Observa subfamília por ID
     */
    @Query("SELECT * FROM subfamilias WHERE id = :subfamiliaId")
    fun observarPorId(subfamiliaId: String): Flow<SubfamiliaEntity?>
    
    /**
     * Busca subfamílias por família pai
     */
    @Query("SELECT * FROM subfamilias WHERE familiaPaiId = :familiaPaiId AND ativa = 1 ORDER BY nivelHierarquico ASC")
    suspend fun buscarPorFamiliaPai(familiaPaiId: String): List<SubfamiliaEntity>
    
    /**
     * Busca subfamília por membros fundadores
     */
    @Query("""
        SELECT * FROM subfamilias 
        WHERE (membroOrigem1Id = :membroId OR membroOrigem2Id = :membroId) 
        AND ativa = 1
    """)
    suspend fun buscarPorMembroFundador(membroId: String): List<SubfamiliaEntity>
    
    /**
     * Conta total de subfamílias ativas
     */
    @Query("SELECT COUNT(*) FROM subfamilias WHERE ativa = 1")
    suspend fun contarSubfamilias(): Int
    
    // ============================================
    // INSERÇÃO E ATUALIZAÇÃO
    // ============================================
    
    /**
     * Insere uma subfamília (substitui se já existir)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(subfamilia: SubfamiliaEntity)
    
    /**
     * Insere múltiplas subfamílias de uma vez
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(subfamilias: List<SubfamiliaEntity>)
    
    /**
     * Atualiza uma subfamília existente
     */
    @Update
    suspend fun atualizar(subfamilia: SubfamiliaEntity)
    
    /**
     * Marca subfamília como precisando sincronizar
     */
    @Query("UPDATE subfamilias SET precisaSincronizar = 1 WHERE id = :subfamiliaId")
    suspend fun marcarParaSincronizar(subfamiliaId: String)
    
    /**
     * Marca subfamília como sincronizada
     */
    @Query("UPDATE subfamilias SET precisaSincronizar = 0, sincronizadoEm = :timestamp WHERE id = :subfamiliaId")
    suspend fun marcarComoSincronizada(subfamiliaId: String, timestamp: Long = System.currentTimeMillis())
    
    // ============================================
    // DELEÇÃO
    // ============================================
    
    /**
     * Deleta uma subfamília
     */
    @Delete
    suspend fun deletar(subfamilia: SubfamiliaEntity)
    
    /**
     * Deleta subfamília por ID
     */
    @Query("DELETE FROM subfamilias WHERE id = :subfamiliaId")
    suspend fun deletarPorId(subfamiliaId: String)
    
    /**
     * Arquiva subfamília (não deleta, apenas marca como inativa)
     */
    @Query("UPDATE subfamilias SET ativa = 0 WHERE id = :subfamiliaId")
    suspend fun arquivar(subfamiliaId: String)
}
