package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com usuários no banco local
 */
@Dao
interface UsuarioDao {
    
    /**
     * Busca usuário por ID
     */
    @Query("SELECT * FROM usuarios WHERE id = :userId")
    suspend fun buscarPorId(userId: String): UsuarioEntity?
    
    /**
     * Observa usuário por ID (atualiza automaticamente)
     */
    @Query("SELECT * FROM usuarios WHERE id = :userId")
    fun observarPorId(userId: String): Flow<UsuarioEntity?>
    
    /**
     * Busca todos os usuários
     */
    @Query("SELECT * FROM usuarios ORDER BY nome ASC")
    suspend fun buscarTodos(): List<UsuarioEntity>
    
    /**
     * Busca administradores
     */
    @Query("SELECT * FROM usuarios WHERE ehAdministrador = 1")
    suspend fun buscarAdministradores(): List<UsuarioEntity>
    
    /**
     * Insere ou atualiza usuário
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(usuario: UsuarioEntity)
    
    /**
     * Atualiza usuário
     */
    @Update
    suspend fun atualizar(usuario: UsuarioEntity)
    
    /**
     * Atualiza flag de primeiro acesso
     */
    @Query("UPDATE usuarios SET primeiroAcesso = :valor WHERE id = :userId")
    suspend fun atualizarPrimeiroAcesso(userId: String, valor: Boolean)
    
    /**
     * Atualiza pessoa vinculada
     */
    @Query("UPDATE usuarios SET pessoaVinculada = :pessoaId WHERE id = :userId")
    suspend fun vincularPessoa(userId: String, pessoaId: String)
    
    /**
     * Atualiza Família Zero
     */
    @Query("UPDATE usuarios SET familiaZeroPai = :paiId, familiaZeroMae = :maeId WHERE id = :userId")
    suspend fun atualizarFamiliaZero(userId: String, paiId: String, maeId: String)
    
    /**
     * Deleta usuário
     */
    @Delete
    suspend fun deletar(usuario: UsuarioEntity)
    
    /**
     * Deleta todos os usuários (logout completo)
     */
    @Query("DELETE FROM usuarios")
    suspend fun deletarTodos()
}

