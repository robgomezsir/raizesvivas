package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.PerfilGamificacaoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para Perfil de Gamificação
 */
@Dao
interface PerfilGamificacaoDao {
    
    /**
     * Observa perfil de gamificação do usuário
     */
    @Query("SELECT * FROM perfil_gamificacao WHERE usuarioId = :usuarioId")
    fun observarPerfil(usuarioId: String): Flow<PerfilGamificacaoEntity?>
    
    /**
     * Busca perfil de gamificação do usuário
     */
    @Query("SELECT * FROM perfil_gamificacao WHERE usuarioId = :usuarioId")
    suspend fun buscarPorUsuarioId(usuarioId: String): PerfilGamificacaoEntity?
    
    /**
     * Insere ou atualiza perfil de gamificação
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirOuAtualizar(perfil: PerfilGamificacaoEntity)
    
    /**
     * Adiciona XP ao perfil
     */
    @Query("""
        UPDATE perfil_gamificacao 
        SET xpTotal = xpTotal + :xp,
            nivel = :novoNivel,
            precisaSincronizar = 1
        WHERE usuarioId = :usuarioId
    """)
    suspend fun adicionarXP(usuarioId: String, xp: Int, novoNivel: Int)
    
    /**
     * Atualiza contador de conquistas desbloqueadas
     */
    @Query("""
        UPDATE perfil_gamificacao 
        SET conquistasDesbloqueadas = :quantidade,
            precisaSincronizar = 1
        WHERE usuarioId = :usuarioId
    """)
    suspend fun atualizarContadorConquistas(usuarioId: String, quantidade: Int)
    
    /**
     * Inicializa perfil de gamificação para novo usuário
     */
    @Query("""
        INSERT OR IGNORE INTO perfil_gamificacao 
        (usuarioId, nivel, xpTotal, conquistasDesbloqueadas, totalConquistas, sincronizadoEm, precisaSincronizar)
        VALUES (:usuarioId, 1, 0, 0, :totalConquistas, :timestamp, 0)
    """)
    suspend fun inicializarPerfil(usuarioId: String, totalConquistas: Int, timestamp: Long)
}

