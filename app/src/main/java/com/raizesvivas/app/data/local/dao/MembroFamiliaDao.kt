package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.MembroFamiliaEntity
import com.raizesvivas.app.domain.model.PapelFamilia
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com membros de famílias no banco local
 */
@Dao
interface MembroFamiliaDao {
    
    // ============================================
    // CONSULTAS (SELECT)
    // ============================================
    
    /**
     * Observa todos os membros de uma família
     */
    @Query("SELECT * FROM membros_familias WHERE familiaId = :familiaId ORDER BY geracaoNaFamilia ASC")
    fun observarMembrosPorFamilia(familiaId: String): Flow<List<MembroFamiliaEntity>>
    
    /**
     * Busca membros de uma família (uma vez)
     */
    @Query("SELECT * FROM membros_familias WHERE familiaId = :familiaId ORDER BY geracaoNaFamilia ASC")
    suspend fun buscarMembrosPorFamilia(familiaId: String): List<MembroFamiliaEntity>
    
    /**
     * Busca famílias de um membro
     */
    @Query("SELECT * FROM membros_familias WHERE membroId = :membroId")
    suspend fun buscarFamiliasPorMembro(membroId: String): List<MembroFamiliaEntity>
    
    /**
     * Busca membro específico em uma família
     */
    @Query("SELECT * FROM membros_familias WHERE membroId = :membroId AND familiaId = :familiaId")
    suspend fun buscarMembroEmFamilia(membroId: String, familiaId: String): MembroFamiliaEntity?
    
    /**
     * Busca membros por papel na família
     */
    @Query("SELECT * FROM membros_familias WHERE familiaId = :familiaId AND papelNaFamilia = :papel")
    suspend fun buscarMembrosPorPapel(familiaId: String, papel: PapelFamilia): List<MembroFamiliaEntity>
    
    /**
     * Conta membros de uma família
     */
    @Query("SELECT COUNT(*) FROM membros_familias WHERE familiaId = :familiaId")
    suspend fun contarMembros(familiaId: String): Int
    
    // ============================================
    // INSERÇÃO E ATUALIZAÇÃO
    // ============================================
    
    /**
     * Insere uma relação membro-família (substitui se já existir)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(membroFamilia: MembroFamiliaEntity)
    
    /**
     * Insere múltiplas relações de uma vez
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(membrosFamilias: List<MembroFamiliaEntity>)
    
    /**
     * Atualiza uma relação existente
     */
    @Update
    suspend fun atualizar(membroFamilia: MembroFamiliaEntity)
    
    // ============================================
    // DELEÇÃO
    // ============================================
    
    /**
     * Deleta uma relação membro-família
     */
    @Delete
    suspend fun deletar(membroFamilia: MembroFamiliaEntity)
    
    /**
     * Remove membro de uma família específica
     */
    @Query("DELETE FROM membros_familias WHERE membroId = :membroId AND familiaId = :familiaId")
    suspend fun removerMembroDeFamilia(membroId: String, familiaId: String)
    
    /**
     * Remove todos os membros de uma família
     */
    @Query("DELETE FROM membros_familias WHERE familiaId = :familiaId")
    suspend fun removerTodosMembros(familiaId: String)
    
    /**
     * Remove membro de todas as famílias
     */
    @Query("DELETE FROM membros_familias WHERE membroId = :membroId")
    suspend fun removerMembroDeTodasFamilias(membroId: String)
}
