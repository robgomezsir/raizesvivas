package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.PessoaDao
import com.raizesvivas.app.data.local.entities.PessoaEntity
import com.raizesvivas.app.data.local.entities.toDomain
import com.raizesvivas.app.data.local.entities.toEntity
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.presentation.components.agruparPessoasPorFamilias
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
            var pessoaFinal = if (!ehAdmin) {
                pessoa.copy(aprovado = false)
            } else {
                pessoa.copy(aprovado = true)
            }
            
            // Validar e corrigir consistência das relações antes de salvar
            val todasPessoas = buscarTodas()
            val inconsistencias = validarConsistenciaRelacoes(pessoaFinal, todasPessoas)
            
            if (inconsistencias.isNotEmpty()) {
                Timber.w("⚠️ Encontradas ${inconsistencias.size} inconsistências para pessoa ${pessoaFinal.id}")
                inconsistencias.forEach { inconsistencia ->
                    Timber.w("  - ${inconsistencia.mensagem}")
                }
                
                // Corrigir automaticamente
                pessoaFinal = corrigirConsistenciaRelacoes(pessoaFinal, todasPessoas)
                Timber.d("✅ Consistência corrigida automaticamente para pessoa ${pessoaFinal.id}")
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
     * Conta total de pessoas aprovadas (visíveis na árvore)
     */
    suspend fun contarPessoasAprovadas(): Int {
        return pessoaDao.contarPessoasAprovadas()
    }
    
    /**
     * Conta o número de grupos familiares usando a mesma lógica da tela de Familias
     * Agrupa pessoas em: Família Zero + outras subfamílias (casais com filhos)
     * Isso garante que o card na Home mostre o mesmo número que a aba Familias
     */
    suspend fun contarFamilias(): Int {
        val todasPessoas = buscarTodas()
        val pessoasMap = todasPessoas.associateBy { it.id }
        
        // Usar a mesma lógica de agrupamento da tela de Familias
        val grupos = agruparPessoasPorFamilias(todasPessoas, pessoasMap)
        
        // Retornar o número de grupos familiares (Família Zero + subfamílias)
        return grupos.size
    }
    
    /**
     * Retorna estatísticas detalhadas sobre as famílias
     */
    suspend fun obterEstatisticasFamilias(): EstatisticasFamilias {
        val todasPessoas = buscarTodas()
        val pessoasMap = todasPessoas.associateBy { it.id }
        
        // Usar a mesma lógica de agrupamento da tela de Familias
        val grupos = agruparPessoasPorFamilias(todasPessoas, pessoasMap)
        
        val total = grupos.size
        val familiaZero = grupos.count { it.ehFamiliaZero }
        val monoparentais = grupos.count { it.ehFamiliaMonoparental }
        val casais = grupos.count { !it.ehFamiliaZero && !it.ehFamiliaMonoparental }
        
        // Contar casais homoafetivos (mesmo gênero)
        val homoafetivas = grupos.count { grupo ->
            !grupo.ehFamiliaZero && 
            !grupo.ehFamiliaMonoparental &&
            grupo.conjugue1?.genero != null &&
            grupo.conjugue2?.genero != null &&
            grupo.conjugue1?.genero == grupo.conjugue2?.genero
        }
        
        return EstatisticasFamilias(
            total = total,
            familiaZero = familiaZero,
            monoparentais = monoparentais,
            casais = casais,
            homoafetivas = homoafetivas
        )
    }
    
    /**
     * Estatísticas detalhadas sobre famílias
     */
    data class EstatisticasFamilias(
        val total: Int,
        val familiaZero: Int,
        val monoparentais: Int,
        val casais: Int,
        val homoafetivas: Int
    )
    
    /**
     * Representa uma inconsistência encontrada nas relações familiares
     */
    data class Inconsistencia(
        val tipo: TipoInconsistencia,
        val pessoaId: String,
        val campo: String,
        val valorAtual: Any?,
        val valorEsperado: Any?,
        val mensagem: String
    )
    
    /**
     * Tipos de inconsistências que podem ser encontradas
     */
    enum class TipoInconsistencia {
        FILHO_SEM_PAI_NA_LISTA,      // Filho não está na lista de filhos do pai
        PAI_SEM_FILHO_NA_RELACAO,     // Pai não está como pai do filho
        MAE_SEM_FILHO_NA_RELACAO,     // Mãe não está como mãe do filho
        CONJUGE_BIDIRECIONAL          // ConjugeAtual não é recíproco
    }
    
    /**
     * Valida a consistência das relações familiares de uma pessoa
     * Verifica se as relações bidirecionais estão sincronizadas
     */
    private suspend fun validarConsistenciaRelacoes(
        pessoa: Pessoa,
        todasPessoas: List<Pessoa>
    ): List<Inconsistencia> {
        val inconsistencias = mutableListOf<Inconsistencia>()
        val pessoasMap = todasPessoas.associateBy { it.id }
        
        // Validar relação pai ↔ filhos
        pessoa.pai?.let { paiId ->
            val pai = pessoasMap[paiId]
            if (pai != null) {
                // Verificar se pessoa está na lista de filhos do pai
                if (!pai.filhos.contains(pessoa.id)) {
                    inconsistencias.add(
                        Inconsistencia(
                            tipo = TipoInconsistencia.FILHO_SEM_PAI_NA_LISTA,
                            pessoaId = paiId,
                            campo = "filhos",
                            valorAtual = pai.filhos,
                            valorEsperado = pai.filhos + pessoa.id,
                            mensagem = "Pessoa ${pessoa.id} não está na lista de filhos do pai ${paiId}"
                        )
                    )
                }
            }
        }
        
        // Validar relação mãe ↔ filhos
        pessoa.mae?.let { maeId ->
            val mae = pessoasMap[maeId]
            if (mae != null) {
                // Verificar se pessoa está na lista de filhos da mãe
                if (!mae.filhos.contains(pessoa.id)) {
                    inconsistencias.add(
                        Inconsistencia(
                            tipo = TipoInconsistencia.FILHO_SEM_PAI_NA_LISTA,
                            pessoaId = maeId,
                            campo = "filhos",
                            valorAtual = mae.filhos,
                            valorEsperado = mae.filhos + pessoa.id,
                            mensagem = "Pessoa ${pessoa.id} não está na lista de filhos da mãe ${maeId}"
                        )
                    )
                }
            }
        }
        
        // Validar filhos ↔ pai/mae
        pessoa.filhos.forEach { filhoId ->
            val filho = pessoasMap[filhoId]
            if (filho != null) {
                // Verificar se pessoa está como pai ou mãe do filho
                if (filho.pai != pessoa.id && filho.mae != pessoa.id) {
                    // Determinar se deveria ser pai ou mãe baseado no gênero (se disponível)
                    val deveriaSerPai = pessoa.genero == Genero.MASCULINO
                    val deveriaSerMae = pessoa.genero == Genero.FEMININO
                    
                    when {
                        deveriaSerPai && filho.pai != pessoa.id -> {
                            inconsistencias.add(
                                Inconsistencia(
                                    tipo = TipoInconsistencia.PAI_SEM_FILHO_NA_RELACAO,
                                    pessoaId = filhoId,
                                    campo = "pai",
                                    valorAtual = filho.pai,
                                    valorEsperado = pessoa.id,
                                    mensagem = "Filho ${filhoId} não tem ${pessoa.id} como pai"
                                )
                            )
                        }
                        deveriaSerMae && filho.mae != pessoa.id -> {
                            inconsistencias.add(
                                Inconsistencia(
                                    tipo = TipoInconsistencia.MAE_SEM_FILHO_NA_RELACAO,
                                    pessoaId = filhoId,
                                    campo = "mae",
                                    valorAtual = filho.mae,
                                    valorEsperado = pessoa.id,
                                    mensagem = "Filho ${filhoId} não tem ${pessoa.id} como mãe"
                                )
                            )
                        }
                        // Se gênero não está definido, verificar se pelo menos um dos campos está vazio
                        filho.pai == null && filho.mae == null -> {
                            inconsistencias.add(
                                Inconsistencia(
                                    tipo = TipoInconsistencia.PAI_SEM_FILHO_NA_RELACAO,
                                    pessoaId = filhoId,
                                    campo = "pai/mae",
                                    valorAtual = "nenhum",
                                    valorEsperado = pessoa.id,
                                    mensagem = "Filho ${filhoId} não tem pai nem mãe definidos, mas está na lista de filhos de ${pessoa.id}"
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // Validar relação cônjuge bidirecional
        pessoa.conjugeAtual?.let { conjugeId ->
            val conjuge = pessoasMap[conjugeId]
            if (conjuge != null) {
                // Verificar se o cônjuge também tem esta pessoa como cônjuge
                if (conjuge.conjugeAtual != pessoa.id) {
                    inconsistencias.add(
                        Inconsistencia(
                            tipo = TipoInconsistencia.CONJUGE_BIDIRECIONAL,
                            pessoaId = conjugeId,
                            campo = "conjugeAtual",
                            valorAtual = conjuge.conjugeAtual,
                            valorEsperado = pessoa.id,
                            mensagem = "Cônjuge ${conjugeId} não tem ${pessoa.id} como cônjuge (relação não é recíproca)"
                        )
                    )
                }
                
                // Validar que cônjuge atual não está na lista de ex-cônjuges
                if (pessoa.exConjuges.contains(conjugeId)) {
                    inconsistencias.add(
                        Inconsistencia(
                            tipo = TipoInconsistencia.CONJUGE_BIDIRECIONAL,
                            pessoaId = pessoa.id,
                            campo = "exConjuges",
                            valorAtual = pessoa.exConjuges,
                            valorEsperado = pessoa.exConjuges - conjugeId,
                            mensagem = "Cônjuge atual ${conjugeId} não pode estar na lista de ex-cônjuges"
                        )
                    )
                }
            }
        }
        
        // Validar que ex-cônjuges não estão como cônjuge atual
        pessoa.exConjuges.forEach { exConjugeId ->
            if (pessoa.conjugeAtual == exConjugeId) {
                inconsistencias.add(
                    Inconsistencia(
                        tipo = TipoInconsistencia.CONJUGE_BIDIRECIONAL,
                        pessoaId = pessoa.id,
                        campo = "conjugeAtual",
                        valorAtual = pessoa.conjugeAtual,
                        valorEsperado = null,
                        mensagem = "Ex-cônjuge ${exConjugeId} não pode ser cônjuge atual ao mesmo tempo"
                    )
                )
            }
        }
        
        return inconsistencias
    }
    
    /**
     * Corrige automaticamente as inconsistências encontradas nas relações familiares
     * Retorna a pessoa corrigida (mas não salva automaticamente)
     */
    private suspend fun corrigirConsistenciaRelacoes(
        pessoa: Pessoa,
        todasPessoas: List<Pessoa>
    ): Pessoa {
        var pessoaCorrigida = pessoa
        val pessoasMap = todasPessoas.associateBy { it.id }
        val pessoasParaAtualizar = mutableMapOf<String, Pessoa>()
        
        // Corrigir relação pai ↔ filhos
        pessoa.pai?.let { paiId ->
            val pai = pessoasMap[paiId]
            if (pai != null && !pai.filhos.contains(pessoa.id)) {
                val filhosAtualizados = pai.filhos + pessoa.id
                pessoasParaAtualizar[paiId] = pai.copy(filhos = filhosAtualizados)
                Timber.d("🔧 Corrigindo: adicionando ${pessoa.id} à lista de filhos do pai ${paiId}")
            }
        }
        
        // Corrigir relação mãe ↔ filhos
        pessoa.mae?.let { maeId ->
            val mae = pessoasMap[maeId]
            if (mae != null && !mae.filhos.contains(pessoa.id)) {
                val filhosAtualizados = mae.filhos + pessoa.id
                pessoasParaAtualizar[maeId] = mae.copy(filhos = filhosAtualizados)
                Timber.d("🔧 Corrigindo: adicionando ${pessoa.id} à lista de filhos da mãe ${maeId}")
            }
        }
        
        // Corrigir filhos ↔ pai/mae
        pessoa.filhos.forEach { filhoId ->
            val filho = pessoasMap[filhoId]
            if (filho != null) {
                val deveriaSerPai = pessoa.genero == Genero.MASCULINO
                val deveriaSerMae = pessoa.genero == Genero.FEMININO
                
                when {
                    deveriaSerPai && filho.pai != pessoa.id -> {
                        pessoasParaAtualizar[filhoId] = filho.copy(pai = pessoa.id)
                        Timber.d("🔧 Corrigindo: definindo ${pessoa.id} como pai do filho ${filhoId}")
                    }
                    deveriaSerMae && filho.mae != pessoa.id -> {
                        pessoasParaAtualizar[filhoId] = filho.copy(mae = pessoa.id)
                        Timber.d("🔧 Corrigindo: definindo ${pessoa.id} como mãe do filho ${filhoId}")
                    }
                    // Se gênero não está definido e filho não tem pai nem mãe, tentar inferir
                    filho.pai == null && filho.mae == null -> {
                        // Não podemos determinar automaticamente, então não corrigimos
                        // Isso requer intervenção manual
                        Timber.w("⚠️ Não é possível determinar automaticamente se ${pessoa.id} é pai ou mãe de ${filhoId} (gênero não definido)")
                    }
                }
            }
        }
        
        // Corrigir relação cônjuge bidirecional
        pessoa.conjugeAtual?.let { conjugeId ->
            val conjuge = pessoasMap[conjugeId]
            if (conjuge != null) {
                // Corrigir reciprocidade
                if (conjuge.conjugeAtual != pessoa.id) {
                    pessoasParaAtualizar[conjugeId] = conjuge.copy(conjugeAtual = pessoa.id)
                    Timber.d("🔧 Corrigindo: definindo ${pessoa.id} como cônjuge de ${conjugeId}")
                }
                
                // Remover cônjuge atual da lista de ex-cônjuges se estiver lá
                if (pessoa.exConjuges.contains(conjugeId)) {
                    val exConjugesCorrigidos = pessoa.exConjuges - conjugeId
                    pessoaCorrigida = pessoaCorrigida.copy(exConjuges = exConjugesCorrigidos)
                    Timber.d("🔧 Corrigindo: removendo cônjuge atual ${conjugeId} da lista de ex-cônjuges")
                }
            }
        }
        
        // Remover ex-cônjuges que são cônjuge atual
        val exConjugesCorrigidos = pessoa.exConjuges.filter { it != pessoa.conjugeAtual }
        if (exConjugesCorrigidos.size != pessoa.exConjuges.size) {
            pessoaCorrigida = pessoaCorrigida.copy(exConjuges = exConjugesCorrigidos)
            Timber.d("🔧 Corrigindo: removendo ex-cônjuges que são cônjuge atual")
        }
        
        // Salvar todas as pessoas atualizadas (em background, não bloqueia)
        pessoasParaAtualizar.forEach { (id, pessoaAtualizada) ->
            try {
                // Salvar no Firestore e cache local
                firestoreService.salvarPessoa(pessoaAtualizada)
                pessoaDao.inserir(pessoaAtualizada.toEntity())
                Timber.d("✅ Pessoa $id atualizada para corrigir consistência")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao atualizar pessoa $id para correção de consistência")
            }
        }
        
        return pessoaCorrigida
    }
    
    /**
     * Sincroniza todas as relações familiares, validando e corrigindo inconsistências
     * Útil para executar periodicamente ou manualmente por admin
     */
    suspend fun sincronizarRelacoesFamiliares(): Result<RelatorioSincronizacao> {
        return try {
            Timber.d("🔄 Iniciando sincronização de relações familiares...")
            val todasPessoas = buscarTodas()
            val inconsistenciasTotais = mutableListOf<Inconsistencia>()
            val pessoasCorrigidas = mutableSetOf<String>()
        
        todasPessoas.forEach { pessoa ->
                val inconsistencias = validarConsistenciaRelacoes(pessoa, todasPessoas)
                if (inconsistencias.isNotEmpty()) {
                    inconsistenciasTotais.addAll(inconsistencias)
                    corrigirConsistenciaRelacoes(pessoa, todasPessoas)
                    pessoasCorrigidas.add(pessoa.id)
                }
            }
            
            val relatorio = RelatorioSincronizacao(
                totalPessoas = todasPessoas.size,
                inconsistenciasEncontradas = inconsistenciasTotais.size,
                pessoasCorrigidas = pessoasCorrigidas.size,
                detalhes = inconsistenciasTotais
            )
            
            Timber.d("✅ Sincronização concluída: ${relatorio.pessoasCorrigidas} pessoas corrigidas, ${relatorio.inconsistenciasEncontradas} inconsistências encontradas")
            Result.success(relatorio)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao sincronizar relações familiares")
            Result.failure(e)
        }
    }
    
    /**
     * Relatório de sincronização de relações familiares
     */
    data class RelatorioSincronizacao(
        val totalPessoas: Int,
        val inconsistenciasEncontradas: Int,
        val pessoasCorrigidas: Int,
        val detalhes: List<Inconsistencia>
    )
    
    /**
     * Conta quantas pessoas nasceram antes da data de nascimento do usuário (ranking)
     * Exclui os IDs fornecidos da contagem (ex: pai e mãe)
     */
    suspend fun contarPessoasAteNascimento(
        dataNascimentoUsuario: java.util.Date?,
        excluirIds: List<String> = emptyList()
    ): Int {
        if (dataNascimentoUsuario == null) return 0
        
        val todasPessoas = buscarTodas()
        val idsExcluir = excluirIds.toSet()
        
        return todasPessoas.count { pessoa ->
            pessoa.id !in idsExcluir &&
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

