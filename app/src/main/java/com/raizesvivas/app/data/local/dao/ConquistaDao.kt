package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.ConquistaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para Progresso de Conquistas
 * Todas as queries filtram por usuarioId para garantir que cada usuário veja apenas suas próprias conquistas
 * 
 * ATUALIZADO: Usa novos nomes de campos (concluida, progresso)
 */
@Dao
interface ConquistaDao {
    
    /**
     * Observa todas as conquistas do usuário
     */
    @Query("SELECT * FROM progresso_conquistas WHERE usuarioId = :usuarioId ORDER BY concluida DESC, progresso DESC")
    fun observarTodasConquistas(usuarioId: String): Flow<List<ConquistaEntity>>
    
    /**
     * Observa apenas conquistas concluídas do usuário
     */
    @Query("SELECT * FROM progresso_conquistas WHERE usuarioId = :usuarioId AND concluida = 1 ORDER BY desbloqueadaEm DESC")
    fun observarConquistasDesbloqueadas(usuarioId: String): Flow<List<ConquistaEntity>>
    
    /**
     * Observa apenas conquistas não concluídas (com progresso) do usuário
     */
    @Query("SELECT * FROM progresso_conquistas WHERE usuarioId = :usuarioId AND concluida = 0 AND progresso > 0 ORDER BY progresso DESC")
    fun observarConquistasEmProgresso(usuarioId: String): Flow<List<ConquistaEntity>>
    
    /**
     * Busca progresso de uma conquista específica do usuário
     */
    @Query("SELECT * FROM progresso_conquistas WHERE conquistaId = :conquistaId AND usuarioId = :usuarioId")
    suspend fun buscarPorId(conquistaId: String, usuarioId: String): ConquistaEntity?
    
    /**
     * Busca progresso de uma conquista específica do usuário (Flow)
     */
    @Query("SELECT * FROM progresso_conquistas WHERE conquistaId = :conquistaId AND usuarioId = :usuarioId")
    fun observarPorId(conquistaId: String, usuarioId: String): Flow<ConquistaEntity?>
    
    /**
     * Conta conquistas concluídas do usuário
     */
    @Query("SELECT COUNT(*) FROM progresso_conquistas WHERE usuarioId = :usuarioId AND concluida = 1")
    fun contarDesbloqueadas(usuarioId: String): Flow<Int>
    
    /**
     * Insere ou atualiza progresso de conquista
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirOuAtualizar(conquista: ConquistaEntity)
    
    /**
     * Insere múltiplas conquistas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(conquistas: List<ConquistaEntity>)
    
    /**
     * Atualiza progresso de uma conquista do usuário
     */
    @Query("""
        UPDATE progresso_conquistas 
        SET progresso = :progresso,
            concluida = :concluida,
            desbloqueadaEm = :desbloqueadaEm,
            precisaSincronizar = 1
        WHERE conquistaId = :conquistaId AND usuarioId = :usuarioId
    """)
    suspend fun atualizarProgresso(
        conquistaId: String,
        usuarioId: String,
        progresso: Int,
        concluida: Boolean,
        desbloqueadaEm: Long?
    )
    
    /**
     * Marca conquista como concluída para o usuário
     * ATUALIZADO: Também atualiza progresso para igual ao progressoTotal
     */
    @Query("""
        UPDATE progresso_conquistas 
        SET concluida = 1,
            progresso = progressoTotal,
            desbloqueadaEm = :timestamp,
            precisaSincronizar = 1
        WHERE conquistaId = :conquistaId AND usuarioId = :usuarioId
    """)
    suspend fun marcarComoDesbloqueada(conquistaId: String, usuarioId: String, timestamp: Long)
    
    /**
     * Busca conquistas que precisam ser sincronizadas
     */
    @Query("SELECT * FROM progresso_conquistas WHERE usuarioId = :usuarioId AND precisaSincronizar = 1")
    suspend fun buscarPendentesSincronizacao(usuarioId: String): List<ConquistaEntity>
}

