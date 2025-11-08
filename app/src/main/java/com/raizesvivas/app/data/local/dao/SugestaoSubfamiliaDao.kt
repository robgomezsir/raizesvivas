package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.SugestaoSubfamiliaEntity
import com.raizesvivas.app.domain.model.StatusSugestao
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com sugestões de subfamílias no banco local
 */
@Dao
interface SugestaoSubfamiliaDao {
    
    // ============================================
    // CONSULTAS (SELECT)
    // ============================================
    
    /**
     * Observa todas as sugestões pendentes
     */
    @Query("SELECT * FROM sugestoes_subfamilias WHERE status = 'PENDENTE' ORDER BY criadoEm DESC")
    fun observarSugestoesPendentes(): Flow<List<SugestaoSubfamiliaEntity>>
    
    /**
     * Busca sugestões pendentes (uma vez)
     */
    @Query("SELECT * FROM sugestoes_subfamilias WHERE status = 'PENDENTE' ORDER BY criadoEm DESC")
    suspend fun buscarSugestoesPendentes(): List<SugestaoSubfamiliaEntity>
    
    /**
     * Busca sugestão por ID
     */
    @Query("SELECT * FROM sugestoes_subfamilias WHERE id = :sugestaoId")
    suspend fun buscarPorId(sugestaoId: String): SugestaoSubfamiliaEntity?
    
    /**
     * Busca sugestões por usuário
     */
    @Query("SELECT * FROM sugestoes_subfamilias WHERE usuarioId = :usuarioId ORDER BY criadoEm DESC")
    suspend fun buscarPorUsuario(usuarioId: String): List<SugestaoSubfamiliaEntity>
    
    /**
     * Busca sugestão pendente para um casal específico
     */
    @Query("""
        SELECT * FROM sugestoes_subfamilias 
        WHERE ((membro1Id = :membro1Id AND membro2Id = :membro2Id) 
            OR (membro1Id = :membro2Id AND membro2Id = :membro1Id))
        AND status = 'PENDENTE'
        LIMIT 1
    """)
    suspend fun buscarPendentePorCasal(membro1Id: String, membro2Id: String): SugestaoSubfamiliaEntity?
    
    // ============================================
    // INSERÇÃO E ATUALIZAÇÃO
    // ============================================
    
    /**
     * Insere uma sugestão (substitui se já existir)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(sugestao: SugestaoSubfamiliaEntity)
    
    /**
     * Insere múltiplas sugestões de uma vez
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(sugestoes: List<SugestaoSubfamiliaEntity>)
    
    /**
     * Atualiza status de uma sugestão
     */
    @Query("""
        UPDATE sugestoes_subfamilias 
        SET status = :status, 
            processadoEm = :processadoEm
        WHERE id = :sugestaoId
    """)
    suspend fun atualizarStatus(
        sugestaoId: String,
        status: StatusSugestao,
        processadoEm: Long = System.currentTimeMillis()
    )
    
    // ============================================
    // DELEÇÃO
    // ============================================
    
    /**
     * Deleta uma sugestão
     */
    @Delete
    suspend fun deletar(sugestao: SugestaoSubfamiliaEntity)
    
    /**
     * Deleta sugestão por ID
     */
    @Query("DELETE FROM sugestoes_subfamilias WHERE id = :sugestaoId")
    suspend fun deletarPorId(sugestaoId: String)
    
    /**
     * Limpa sugestões antigas (rejeitadas ou expiradas)
     */
    @Query("DELETE FROM sugestoes_subfamilias WHERE status IN ('REJEITADA', 'EXPIRADA') AND processadoEm < :timestamp")
    suspend fun limparAntigas(timestamp: Long)
}
