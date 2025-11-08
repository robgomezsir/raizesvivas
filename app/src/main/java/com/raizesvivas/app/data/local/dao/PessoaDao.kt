package com.raizesvivas.app.data.local.dao

import androidx.room.*
import com.raizesvivas.app.data.local.entities.PessoaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operações com pessoas no banco local
 * 
 * Este DAO fornece métodos para:
 * - Buscar pessoas (todas, por ID, por relacionamentos)
 * - Inserir/atualizar/deletar pessoas
 * - Buscar pessoas que precisam sincronizar
 */
@Dao
interface PessoaDao {
    
    // ============================================
    // CONSULTAS (SELECT)
    // ============================================
    
    /**
     * Busca todas as pessoas ordenadas por nome
     * Retorna Flow para observar mudanças em tempo real
     */
    @Query("SELECT * FROM pessoas ORDER BY nome ASC")
    fun observarTodasPessoas(): Flow<List<PessoaEntity>>
    
    /**
     * Busca todas as pessoas (uma vez)
     */
    @Query("SELECT * FROM pessoas ORDER BY nome ASC")
    suspend fun buscarTodasPessoas(): List<PessoaEntity>
    
    /**
     * Busca pessoa por ID
     */
    @Query("SELECT * FROM pessoas WHERE id = :pessoaId")
    suspend fun buscarPorId(pessoaId: String): PessoaEntity?
    
    /**
     * Observa pessoa por ID (atualiza automaticamente)
     */
    @Query("SELECT * FROM pessoas WHERE id = :pessoaId")
    fun observarPorId(pessoaId: String): Flow<PessoaEntity?>
    
    /**
     * Busca o casal da Família Zero
     */
    @Query("SELECT * FROM pessoas WHERE ehFamiliaZero = 1")
    suspend fun buscarFamiliaZero(): List<PessoaEntity>
    
    /**
     * Observa o casal da Família Zero
     */
    @Query("SELECT * FROM pessoas WHERE ehFamiliaZero = 1")
    fun observarFamiliaZero(): Flow<List<PessoaEntity>>
    
    /**
     * Busca filhos de uma pessoa
     */
    @Query("SELECT * FROM pessoas WHERE pai = :pessoaId OR mae = :pessoaId")
    suspend fun buscarFilhos(pessoaId: String): List<PessoaEntity>
    
    /**
     * Busca irmãos (mesmo pai E mesma mãe)
     */
    @Query("""
        SELECT * FROM pessoas 
        WHERE pai = :paiId 
        AND mae = :maeId 
        AND id != :excluirId
    """)
    suspend fun buscarIrmaos(
        paiId: String?,
        maeId: String?,
        excluirId: String
    ): List<PessoaEntity>
    
    /**
     * Busca pessoas por nome (pesquisa parcial, case-insensitive)
     */
    @Query("""
        SELECT * FROM pessoas 
        WHERE nome LIKE '%' || :termo || '%' 
        ORDER BY nome ASC
    """)
    suspend fun buscarPorNome(termo: String): List<PessoaEntity>
    
    /**
     * Busca pessoas aprovadas (visíveis na árvore)
     */
    @Query("SELECT * FROM pessoas WHERE aprovado = 1 ORDER BY nome ASC")
    suspend fun buscarPessoasAprovadas(): List<PessoaEntity>
    
    /**
     * Busca pessoas aguardando aprovação
     */
    @Query("SELECT * FROM pessoas WHERE aprovado = 0 ORDER BY criadoEm DESC")
    suspend fun buscarPessoasPendentes(): List<PessoaEntity>
    
    /**
     * Busca pessoas que precisam sincronizar com Firestore
     */
    @Query("SELECT * FROM pessoas WHERE precisaSincronizar = 1")
    suspend fun buscarPessoasParaSincronizar(): List<PessoaEntity>
    
    /**
     * Busca pessoas vivas
     */
    @Query("SELECT * FROM pessoas WHERE dataFalecimento IS NULL ORDER BY nome ASC")
    suspend fun buscarPessoasVivas(): List<PessoaEntity>
    
    /**
     * Busca pessoas falecidas
     */
    @Query("SELECT * FROM pessoas WHERE dataFalecimento IS NOT NULL ORDER BY dataFalecimento DESC")
    suspend fun buscarPessoasFalecidas(): List<PessoaEntity>
    
    /**
     * Conta total de pessoas
     */
    @Query("SELECT COUNT(*) FROM pessoas")
    suspend fun contarPessoas(): Int
    
    /**
     * Conta total de pessoas aprovadas
     */
    @Query("SELECT COUNT(*) FROM pessoas WHERE aprovado = 1")
    suspend fun contarPessoasAprovadas(): Int
    
    // ============================================
    // INSERÇÃO E ATUALIZAÇÃO
    // ============================================
    
    /**
     * Insere uma pessoa (substitui se já existir)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(pessoa: PessoaEntity)
    
    /**
     * Insere múltiplas pessoas de uma vez
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(pessoas: List<PessoaEntity>)
    
    /**
     * Atualiza uma pessoa existente
     */
    @Update
    suspend fun atualizar(pessoa: PessoaEntity)
    
    /**
     * Atualiza múltiplas pessoas
     */
    @Update
    suspend fun atualizarTodas(pessoas: List<PessoaEntity>)
    
    /**
     * Marca pessoa como precisando sincronizar
     */
    @Query("UPDATE pessoas SET precisaSincronizar = 1 WHERE id = :pessoaId")
    suspend fun marcarParaSincronizar(pessoaId: String)
    
    /**
     * Marca pessoa como sincronizada
     */
    @Query("UPDATE pessoas SET precisaSincronizar = 0, sincronizadoEm = :timestamp WHERE id = :pessoaId")
    suspend fun marcarComoSincronizada(pessoaId: String, timestamp: Long = System.currentTimeMillis())
    
    // ============================================
    // DELEÇÃO
    // ============================================
    
    /**
     * Deleta uma pessoa
     */
    @Delete
    suspend fun deletar(pessoa: PessoaEntity)
    
    /**
     * Deleta pessoa por ID
     */
    @Query("DELETE FROM pessoas WHERE id = :pessoaId")
    suspend fun deletarPorId(pessoaId: String)
    
    /**
     * Deleta todas as pessoas (use com cuidado!)
     */
    @Query("DELETE FROM pessoas")
    suspend fun deletarTodas()
    
    /**
     * Deleta pessoas não sincronizadas (limpeza de cache)
     */
    @Query("DELETE FROM pessoas WHERE precisaSincronizar = 0 AND sincronizadoEm < :timestamp")
    suspend fun limparCacheAntigo(timestamp: Long)
}

