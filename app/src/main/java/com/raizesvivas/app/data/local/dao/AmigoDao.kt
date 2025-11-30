package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.AmigoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com amigos da família no banco local
 */
@Dao
interface AmigoDao {
    
    /**
     * Busca todos os amigos ordenados por nome
     * Retorna Flow para observar mudanças em tempo real
     */
    @Query("SELECT * FROM amigos ORDER BY nome ASC")
    fun observarTodosAmigos(): Flow<List<AmigoEntity>>
    
    /**
     * Busca todos os amigos (uma vez)
     */
    @Query("SELECT * FROM amigos ORDER BY nome ASC")
    suspend fun buscarTodosAmigos(): List<AmigoEntity>
    
    /**
     * Busca amigo por ID
     */
    @Query("SELECT * FROM amigos WHERE id = :amigoId")
    suspend fun buscarPorId(amigoId: String): AmigoEntity?
    
    /**
     * Observa amigo por ID (atualiza automaticamente)
     */
    @Query("SELECT * FROM amigos WHERE id = :amigoId")
    fun observarPorId(amigoId: String): Flow<AmigoEntity?>
    
    /**
     * Insere ou atualiza um amigo
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirOuAtualizar(amigo: AmigoEntity)
    
    /**
     * Insere ou atualiza uma lista de amigos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirOuAtualizarTodos(amigos: List<AmigoEntity>)
    
    /**
     * Deleta um amigo
     */
    @Delete
    suspend fun deletar(amigo: AmigoEntity)
    
    /**
     * Deleta amigo por ID
     */
    @Query("DELETE FROM amigos WHERE id = :amigoId")
    suspend fun deletarPorId(amigoId: String)
}

