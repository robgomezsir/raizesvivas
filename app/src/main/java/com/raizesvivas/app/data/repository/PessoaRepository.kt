package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.PessoaDao
import com.raizesvivas.app.data.local.entities.PessoaEntity
import com.raizesvivas.app.data.local.entities.toDomain
import com.raizesvivas.app.data.local.entities.toEntity
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Pessoa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar pessoas
 * 
 * Este repository implementa o padrão Repository, coordenando:
 * - Dados locais (Room) para cache e modo offline
 * - Dados remotos (Firestore) como fonte da verdade
 * 
 * Estratégia: Cache-First com sincronização bidirecional
 */
@Singleton
class PessoaRepository @Inject constructor(
    private val pessoaDao: PessoaDao,
    private val firestoreService: FirestoreService,
    private val edicaoPendenteRepository: EdicaoPendenteRepository
) {
    
    /**
     * Observa todas as pessoas (do cache local)
     * Atualiza automaticamente quando o cache muda
     */
    fun observarTodasPessoas(): Flow<List<Pessoa>> {
        return pessoaDao.observarTodasPessoas()
            .map { entities -> 
                Timber.d("📋 Observando pessoas: ${entities.size} entidades no cache local")
                entities.map { it.toDomain() } 
            }
    }
    
    /**
     * Busca pessoa por ID (cache local primeiro)
     */
    suspend fun buscarPorId(pessoaId: String): Pessoa? {
        if (pessoaId.isBlank()) {
            Timber.w("⚠️ Tentativa de buscar pessoa com ID vazio")
            return null
        }
        
        // Buscar no cache local
        val local = pessoaDao.buscarPorId(pessoaId)?.toDomain()
        
        // Se não estiver no cache, buscar no Firestore
        if (local == null) {
            val remoto = firestoreService.buscarPessoa(pessoaId).getOrNull()
            
            // Salvar no cache se encontrou
            remoto?.let {
                pessoaDao.inserir(it.toEntity())
            }
            
            return remoto
        }
        
        return local
    }
    
    /**
     * Sincroniza pessoas do Firestore para o cache local
     * Não limpa dados existentes, apenas atualiza/insere
     */
    suspend fun sincronizarDoFirestore(): Result<Unit> {
        return try {
            Timber.d("🔄 Sincronizando pessoas do Firestore...")
            
            // Buscar todas as pessoas do Firestore
            val resultado = firestoreService.buscarTodasPessoas()
            
            resultado.onSuccess { pessoas ->
                Timber.d("📥 Recebidas ${pessoas.size} pessoas do Firestore")
                
                // Converter para entities
                val entities = mutableListOf<PessoaEntity>()
                pessoas.forEachIndexed { index, pessoa ->
                    try {
                        val entity = pessoa.toEntity()
                        entities.add(entity)
                        Timber.d("📝 Convertida pessoa $index: ${pessoa.nome} (ID: ${pessoa.id})")
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Erro ao converter pessoa $index: ${pessoa.nome} (ID: ${pessoa.id})")
                    }
                }
                
                Timber.d("💾 Salvando ${entities.size} pessoas no cache local...")
                val totalAntes = pessoaDao.contarPessoas()
                pessoaDao.inserirTodas(entities)
                
                // Verificar se realmente foram salvas
                val totalDepois = pessoaDao.contarPessoas()
                val inseridas = totalDepois - totalAntes
                Timber.d("✅ ${pessoas.size} pessoas sincronizadas. Total no cache: $totalDepois (inseridas: $inseridas)")
                
                if (inseridas < entities.size) {
                    Timber.w("⚠️ Discrepância: ${entities.size} entidades tentaram inserir, mas apenas $inseridas foram inseridas")
                }
            }
            
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro na sincronização")
            }
            
            resultado.map { }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro fatal na sincronização")
            Result.failure(e)
        }
    }
    
    /**
     * Recarrega dados do Firestore, substituindo completamente o cache local
     * Use esta função para forçar atualização (ex: pull-to-refresh)
     */
    suspend fun recarregarDoFirestore(): Result<Unit> {
        return try {
            Timber.d("🔄 Recarregando pessoas do Firestore (substituindo cache)...")
            
            // Buscar todas as pessoas do Firestore
            val resultado = firestoreService.buscarTodasPessoas()
            
            resultado.onSuccess { pessoas ->
                Timber.d("📥 Recebidas ${pessoas.size} pessoas do Firestore para recarregar")
                
                // Limpar cache local primeiro
                pessoaDao.deletarTodas()
                Timber.d("🗑️ Cache local limpo")
                
                // Converter para entities
                val entities = mutableListOf<PessoaEntity>()
                pessoas.forEachIndexed { index, pessoa ->
                    try {
                        val entity = pessoa.toEntity()
                        entities.add(entity)
                        Timber.d("📝 Convertida pessoa $index: ${pessoa.nome} (ID: ${pessoa.id})")
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Erro ao converter pessoa $index: ${pessoa.nome} (ID: ${pessoa.id})")
                    }
                }
                
                // Inserir novos dados do Firestore
                Timber.d("💾 Inserindo ${entities.size} pessoas no cache local...")
                pessoaDao.inserirTodas(entities)
                
                // Verificar se realmente foram salvas
                val totalSalvas = pessoaDao.contarPessoas()
                Timber.d("✅ ${pessoas.size} pessoas recarregadas do Firestore. Total no cache: $totalSalvas")
                
                if (totalSalvas != entities.size) {
                    Timber.w("⚠️ Discrepância: ${entities.size} entidades tentaram inserir, mas $totalSalvas no cache")
                }
            }
            
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao recarregar do Firestore")
            }
            
            resultado.map { }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro fatal ao recarregar do Firestore")
            Result.failure(e)
        }
    }
    
    /**
     * Salva pessoa (local + remoto)
     */
    suspend fun salvar(pessoa: Pessoa, ehAdmin: Boolean): Result<Unit> {
        return try {
            // Validações básicas
            if (pessoa.id.isBlank()) {
                return Result.failure(Exception("ID da pessoa não pode estar vazio"))
            }
            if (pessoa.nome.isBlank()) {
                return Result.failure(Exception("Nome da pessoa não pode estar vazio"))
            }
            
            // Se não for admin, marca como não aprovado
            val pessoaFinal = if (!ehAdmin) {
                pessoa.copy(aprovado = false)
            } else {
                pessoa.copy(aprovado = true)
            }
            
            // Salvar no Firestore
            val resultado = firestoreService.salvarPessoa(pessoaFinal)
            
            resultado.onSuccess {
                // Salvar no cache local
                pessoaDao.inserir(pessoaFinal.toEntity())
                Timber.d("✅ Pessoa salva: ${pessoaFinal.nome}")
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar pessoa")
            Result.failure(e)
        }
    }
    
    /**
     * Busca todas as pessoas (do cache local)
     */
    suspend fun buscarTodas(): List<Pessoa> {
        return try {
            // Buscar do cache local primeiro
            val entities = pessoaDao.buscarTodasPessoas()
            entities.map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao buscar todas as pessoas")
            emptyList()
        }
    }
    
    /**
     * Atualiza pessoa existente
     */
    suspend fun atualizar(pessoa: Pessoa, ehAdmin: Boolean): Result<Unit> {
        return try {
            // Validações básicas
            if (pessoa.id.isBlank()) {
                return Result.failure(Exception("ID da pessoa não pode estar vazio"))
            }
            if (pessoa.nome.isBlank()) {
                return Result.failure(Exception("Nome da pessoa não pode estar vazio"))
            }
            
            if (ehAdmin) {
                // Admin: atualizar diretamente
                val pessoaAtualizada = pessoa.copy(
                    versao = pessoa.versao + 1,
                    aprovado = true
                )
                
                val resultado = firestoreService.salvarPessoa(pessoaAtualizada)
                
                resultado.onSuccess {
                    pessoaDao.atualizar(pessoaAtualizada.toEntity())
                }
                
                resultado
            } else {
                // Não-admin: criar edição pendente
                val pessoaOriginal = buscarPorId(pessoa.id)
                
                if (pessoaOriginal == null) {
                    return Result.failure(Exception("Pessoa não encontrada"))
                }
                
                // Criar edição pendente
                val resultado = edicaoPendenteRepository.criarEdicaoPendente(
                    pessoaOriginal = pessoaOriginal,
                    pessoaEditada = pessoa
                )
                
                resultado.map { 
                    Timber.d("✅ Edição pendente criada para pessoa ${pessoa.id}")
                    Unit
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar pessoa")
            Result.failure(e)
        }
    }
    
    /**
     * Deleta pessoa
     */
    suspend fun deletar(pessoaId: String): Result<Unit> {
        return try {
            if (pessoaId.isBlank()) {
                return Result.failure(Exception("ID da pessoa não pode estar vazio"))
            }
            
            // Deletar do Firestore
            val resultado = firestoreService.deletarPessoa(pessoaId)
            
            resultado.onSuccess {
                // Deletar do cache local
                pessoaDao.deletarPorId(pessoaId)
            }
            
            resultado
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao deletar pessoa")
            Result.failure(e)
        }
    }
    
    /**
     * Busca pessoas por nome
     */
    suspend fun buscarPorNome(termo: String): List<Pessoa> {
        return try {
            if (termo.isBlank()) {
                return emptyList()
            }
            // Buscar no cache local primeiro
            pessoaDao.buscarPorNome(termo).map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao buscar pessoas por nome")
            emptyList()
        }
    }
    
    /**
     * Busca filhos de uma pessoa
     */
    suspend fun buscarFilhos(pessoaId: String): List<Pessoa> {
        return try {
            if (pessoaId.isBlank()) {
                return emptyList()
            }
            pessoaDao.buscarFilhos(pessoaId).map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao buscar filhos")
            emptyList()
        }
    }
    
    /**
     * Busca irmãos
     */
    suspend fun buscarIrmaos(paiId: String?, maeId: String?, excluirId: String): List<Pessoa> {
        return try {
            if (excluirId.isBlank()) {
                return emptyList()
            }
            pessoaDao.buscarIrmaos(paiId, maeId, excluirId).map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao buscar irmãos")
            emptyList()
        }
    }
    
    /**
     * Conta total de pessoas
     */
    suspend fun contarPessoas(): Int {
        return pessoaDao.contarPessoas()
    }
    
    /**
     * Conta quantas famílias (casais únicos) existem na árvore
     */
    suspend fun contarFamilias(): Int {
        val todasPessoas = buscarTodas()
        val casais = mutableSetOf<Pair<String, String>>()
        
        todasPessoas.forEach { pessoa ->
            pessoa.conjugeAtual?.let { conjugeId ->
                // Criar par ordenado (menor ID primeiro) para evitar duplicatas
                val par = if (pessoa.id < conjugeId) {
                    Pair(pessoa.id, conjugeId)
                } else {
                    Pair(conjugeId, pessoa.id)
                }
                casais.add(par)
            }
        }
        
        return casais.size
    }
    
    /**
     * Conta quantas pessoas nasceram antes da data de nascimento do usuário (ranking)
     */
    suspend fun contarPessoasAteNascimento(dataNascimentoUsuario: java.util.Date?): Int {
        if (dataNascimentoUsuario == null) return 0
        
        val todasPessoas = buscarTodas()
        return todasPessoas.count { pessoa ->
            pessoa.dataNascimento != null && 
            pessoa.dataNascimento.before(dataNascimentoUsuario)
        }
    }
    
    /**
     * Conta sobrinhos do usuário (filhos dos irmãos)
     */
    suspend fun contarSobrinhos(pessoaId: String): Int {
        if (pessoaId.isBlank()) {
            return 0
        }
        
        val pessoa = buscarPorId(pessoaId) ?: return 0
        
        // Buscar irmãos da pessoa
        val irmaos = buscarIrmaos(pessoa.pai, pessoa.mae, pessoaId)
        
        // Contar filhos de todos os irmãos (buscar do banco também)
        var totalSobrinhos = 0
        irmaos.forEach { irmao ->
            val filhos = buscarFilhos(irmao.id)
            totalSobrinhos += filhos.size
        }
        
        return totalSobrinhos
    }
}

