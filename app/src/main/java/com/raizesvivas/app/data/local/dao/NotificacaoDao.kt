package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.NotificacaoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para Notificações
 */
@Dao
interface NotificacaoDao {
    
    /**
     * Observa todas as notificações ordenadas por data (mais recente primeiro)
     */
    @Query("SELECT * FROM notificacoes ORDER BY criadaEm DESC")
    fun observarTodasNotificacoes(): Flow<List<NotificacaoEntity>>
    
    /**
     * Observa apenas notificações não lidas
     */
    @Query("SELECT * FROM notificacoes WHERE lida = 0 ORDER BY criadaEm DESC")
    fun observarNotificacoesNaoLidas(): Flow<List<NotificacaoEntity>>
    
    /**
     * Conta notificações não lidas
     */
    @Query("SELECT COUNT(*) FROM notificacoes WHERE lida = 0")
    fun contarNaoLidas(): Flow<Int>
    
    /**
     * Busca notificação por ID
     */
    @Query("SELECT * FROM notificacoes WHERE id = :id")
    suspend fun buscarPorId(id: String): NotificacaoEntity?
    
    /**
     * Insere ou atualiza notificação
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirOuAtualizar(notificacao: NotificacaoEntity)
    
    /**
     * Insere múltiplas notificações
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(notificacoes: List<NotificacaoEntity>)
    
    /**
     * Marca notificação como lida
     */
    @Query("UPDATE notificacoes SET lida = 1 WHERE id = :id")
    suspend fun marcarComoLida(id: String)
    
    /**
     * Marca todas as notificações como lidas
     */
    @Query("UPDATE notificacoes SET lida = 1")
    suspend fun marcarTodasComoLidas()
    
    /**
     * Deleta notificação
     */
    @Delete
    suspend fun deletar(notificacao: NotificacaoEntity)
    
    /**
     * Deleta notificações antigas (mais de X dias)
     */
    @Query("DELETE FROM notificacoes WHERE criadaEm < :timestampLimite")
    suspend fun deletarAntigas(timestampLimite: Long)
}
