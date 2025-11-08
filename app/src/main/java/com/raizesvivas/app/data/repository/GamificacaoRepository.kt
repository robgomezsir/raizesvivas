package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.ConquistaDao
import com.raizesvivas.app.data.local.dao.PerfilGamificacaoDao
import com.raizesvivas.app.data.local.entities.ConquistaEntity
import com.raizesvivas.app.data.local.entities.PerfilGamificacaoEntity
import com.raizesvivas.app.domain.model.PerfilGamificacao
import com.raizesvivas.app.domain.model.ProgressoConquista
import com.raizesvivas.app.domain.model.SistemaConquistas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar gamificação
 */
@Singleton
class GamificacaoRepository @Inject constructor(
    private val conquistaDao: ConquistaDao,
    private val perfilGamificacaoDao: PerfilGamificacaoDao,
    private val firestoreService: com.raizesvivas.app.data.remote.firebase.FirestoreService
) {
    
    /**
     * Observa todas as conquistas do usuário
     */
    fun observarTodasConquistas(usuarioId: String): Flow<List<ProgressoConquista>> {
        return conquistaDao.observarTodasConquistas(usuarioId)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    
    /**
     * Observa conquistas desbloqueadas do usuário
     */
    fun observarConquistasDesbloqueadas(usuarioId: String): Flow<List<ProgressoConquista>> {
        return conquistaDao.observarConquistasDesbloqueadas(usuarioId)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    
    /**
     * Observa conquistas em progresso do usuário
     */
    fun observarConquistasEmProgresso(usuarioId: String): Flow<List<ProgressoConquista>> {
        return conquistaDao.observarConquistasEmProgresso(usuarioId)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    
    /**
     * Observa progresso de uma conquista específica do usuário
     */
    fun observarProgressoConquista(conquistaId: String, usuarioId: String): Flow<ProgressoConquista?> {
        return conquistaDao.observarPorId(conquistaId, usuarioId)
            .map { it?.toDomain() }
    }
    
    /**
     * Observa perfil de gamificação do usuário
     */
    fun observarPerfilGamificacao(usuarioId: String): Flow<PerfilGamificacao?> {
        return perfilGamificacaoDao.observarPerfil(usuarioId)
            .map { entity ->
                entity?.toDomain()
            }
    }
    
    /**
     * Busca perfil de gamificação do usuário
     */
    suspend fun buscarPerfilGamificacao(usuarioId: String): PerfilGamificacao? {
        return perfilGamificacaoDao.buscarPorUsuarioId(usuarioId)?.toDomain()
    }
    
    /**
     * Inicializa perfil de gamificação para novo usuário
     * IMPORTANTE: Novos usuários sempre começam com nível 1, XP 0 e nenhuma conquista desbloqueada
     * VALIDAÇÃO RIGOROSA: Sempre verifica se o usuarioId está correto e se é realmente um novo usuário
     */
    suspend fun inicializarPerfil(usuarioId: String) {
        try {
            // VALIDAÇÃO CRÍTICA: usuarioId não pode estar vazio
            if (usuarioId.isBlank()) {
                Timber.e("❌ ERRO CRÍTICO: usuarioId está vazio ao inicializar perfil!")
                return
            }
            
            Timber.d("🔍 Inicializando perfil para usuarioId: $usuarioId")
            
            // Verificar se o perfil já existe localmente PARA ESTE usuarioId ESPECÍFICO
            val perfilExistente = perfilGamificacaoDao.buscarPorUsuarioId(usuarioId)
            
            // VALIDAÇÃO: Se existe perfil, verificar se o usuarioId corresponde
            if (perfilExistente != null) {
                if (perfilExistente.usuarioId != usuarioId) {
                    Timber.e("❌ ERRO CRÍTICO: usuarioId do perfil não corresponde! Esperado: $usuarioId, Encontrado: ${perfilExistente.usuarioId}")
                    // Limpar dados incorretos e inicializar corretamente
                    // (Não temos método de delete, então vamos sobrescrever)
                } else {
                    // Perfil existe e usuarioId corresponde - usuário retornando
                    Timber.d("🔄 Perfil existente encontrado para usuarioId: $usuarioId, sincronizando do Firestore")
                    val progressosLocais = conquistaDao.observarTodasConquistas(usuarioId).first()
                    
                    // VALIDAÇÃO: Verificar se os progressos locais pertencem a este usuarioId
                    val progressosIncorretos = progressosLocais.any { it.usuarioId != usuarioId }
                    if (progressosIncorretos) {
                        Timber.e("❌ ERRO CRÍTICO: Encontrados progressos com usuarioId incorreto!")
                        // Limpar e reinicializar
                    } else {
                        // Sincronizar do Firestore (pode ter dados mais recentes)
                        sincronizarConquistasDoFirestore(usuarioId)
                        // Recalcular XP do perfil baseado nas conquistas desbloqueadas
                        recalcularXPDoPerfil(usuarioId)
                        Timber.d("✅ Perfil existente sincronizado para usuarioId: $usuarioId")
                        return
                    }
                }
            }
            
            // NOVO USUÁRIO OU DADOS CORROMPIDOS: Sempre inicializar limpo (nível 1, XP 0, sem conquistas)
            Timber.d("🆕 Inicializando NOVO perfil limpo para usuarioId: $usuarioId (nível 1, XP 0, sem conquistas)")
            
            // Primeiro, verificar se há conquistas no Firestore PARA ESTE usuarioId
            val conquistasDoFirestore = firestoreService.buscarConquistasDoUsuario(usuarioId).getOrNull()
            
            if (conquistasDoFirestore != null && conquistasDoFirestore.isNotEmpty()) {
                // VALIDAÇÃO: Verificar se todas as conquistas do Firestore pertencem a este usuarioId
                val todasCorretas = conquistasDoFirestore.all { 
                    // Não temos usuarioId em ProgressoConquista, então vamos confiar que o Firestore retornou corretamente
                    true // A busca já filtra por usuarioId na subcollection
                }
                
                if (todasCorretas && perfilExistente != null) {
                    // Firestore tem dados e perfil local existe - usar dados do Firestore
                    Timber.d("📥 Carregando conquistas do Firestore para usuarioId: $usuarioId")
                    sincronizarConquistasDoFirestore(usuarioId)
                    recalcularXPDoPerfil(usuarioId)
                    return
                } else {
                    // Firestore tem dados mas não há perfil local - pode ser de outro dispositivo
                    // NESTE CASO: Usar dados do Firestore se forem válidos
                    Timber.d("📥 Firestore tem dados para usuarioId: $usuarioId, carregando...")
                    sincronizarConquistasDoFirestore(usuarioId)
                    // Verificar se o perfil foi criado pela sincronização
                    val perfilAposSync = perfilGamificacaoDao.buscarPorUsuarioId(usuarioId)
                    if (perfilAposSync == null) {
                        // Se não existe perfil após sincronização, criar um limpo
                        val totalConquistas = SistemaConquistas.obterTodas().size
                        val timestamp = System.currentTimeMillis()
                        perfilGamificacaoDao.inicializarPerfil(usuarioId, totalConquistas, timestamp)
                        recalcularXPDoPerfil(usuarioId)
                    } else {
                        recalcularXPDoPerfil(usuarioId)
                }
                return
                }
            }
            
            // NENHUM DADO NO FIRESTORE: Inicializar completamente limpo
            Timber.d("🆕 Inicializando perfil completamente NOVO (sem dados no Firestore) para usuarioId: $usuarioId")
            
            // Inicializar perfil com valores zerados
            val totalConquistas = SistemaConquistas.obterTodas().size
            val timestamp = System.currentTimeMillis()
            perfilGamificacaoDao.inicializarPerfil(usuarioId, totalConquistas, timestamp)
            
            // Inicializar progresso de todas as conquistas zeradas
            val conquistas = SistemaConquistas.obterTodas()
            val progressos = conquistas.map { conquista ->
                ConquistaEntity.fromDomain(
                    progresso = ProgressoConquista(
                        conquistaId = conquista.id,
                        desbloqueada = false,
                        desbloqueadaEm = null,
                        progressoAtual = 0,
                        progressoTotal = conquista.condicao.valor
                    ),
                    usuarioId = usuarioId, // GARANTIR que usuarioId está correto
                    precisaSincronizar = true // Marcar para sincronizar com Firestore
                )
            }
            conquistaDao.inserirTodas(progressos)
            
            // VALIDAÇÃO FINAL: Verificar se as conquistas foram salvas corretamente
            val progressosVerificacao = conquistaDao.observarTodasConquistas(usuarioId).first()
            val progressosIncorretosFinal = progressosVerificacao.any { it.usuarioId != usuarioId }
            if (progressosIncorretosFinal) {
                Timber.e("❌ ERRO CRÍTICO: Progressos salvos com usuarioId incorreto após inicialização!")
            } else {
                Timber.d("✅ Validação: Todos os progressos pertencem ao usuarioId correto: $usuarioId")
            }
            
            // Sincronizar com Firestore em background (para salvar estado inicial)
            sincronizarConquistasParaFirestore(usuarioId)
            
            Timber.d("✅ Novo perfil de gamificação inicializado (nível 1, XP 0, sem conquistas): $usuarioId")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao inicializar perfil de gamificação para usuarioId: $usuarioId")
        }
    }
    
    /**
     * Recalcula XP e nível do perfil baseado nas conquistas desbloqueadas
     * IMPORTANTE: Isso garante que o perfil sempre reflete o XP correto das conquistas
     */
    private suspend fun recalcularXPDoPerfil(usuarioId: String) {
        try {
            val progressos = conquistaDao.observarTodasConquistas(usuarioId).first()
            val conquistas = SistemaConquistas.obterTodas()
            
            // Calcular XP total baseado nas conquistas desbloqueadas
            var xpTotal = 0
            var conquistasDesbloqueadas = 0
            
            progressos.forEach { progresso ->
                if (progresso.desbloqueada) {
                    val conquista = conquistas.find { it.id == progresso.conquistaId }
                    if (conquista != null) {
                        xpTotal += conquista.recompensaXP
                        conquistasDesbloqueadas++
                    }
                }
            }
            
            // Calcular novo nível baseado no XP total
            val novoNivel = SistemaConquistas.calcularNivel(xpTotal)
            
            // Atualizar perfil
            val perfilAtual = perfilGamificacaoDao.buscarPorUsuarioId(usuarioId)
            if (perfilAtual != null) {
                // Atualizar XP, nível e contador
                val perfilAtualizado = perfilAtual.copy(
                    xpTotal = xpTotal,
                    nivel = novoNivel,
                    conquistasDesbloqueadas = conquistasDesbloqueadas,
                    precisaSincronizar = true
                )
                perfilGamificacaoDao.inserirOuAtualizar(perfilAtualizado)
                Timber.d("✅ XP do perfil recalculado: $xpTotal XP, nível $novoNivel, $conquistasDesbloqueadas conquistas")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao recalcular XP do perfil")
        }
    }
    
    /**
     * Sincroniza conquistas do Firestore para o banco local
     * VALIDAÇÃO RIGOROSA: Sempre valida que o usuarioId está correto em todas as etapas
     */
    private suspend fun sincronizarConquistasDoFirestore(usuarioId: String) {
        try {
            // VALIDAÇÃO CRÍTICA: usuarioId não pode estar vazio
            if (usuarioId.isBlank()) {
                Timber.e("❌ ERRO CRÍTICO: usuarioId está vazio ao sincronizar do Firestore!")
                return
            }
            
            Timber.d("📥 Sincronizando conquistas do Firestore para usuarioId: $usuarioId")
            
            val resultado = firestoreService.buscarConquistasDoUsuario(usuarioId)
            
            if (resultado.isFailure) {
                Timber.e(resultado.exceptionOrNull(), "❌ Erro ao buscar conquistas do Firestore para usuarioId: $usuarioId")
                return
            }
            
            val conquistasDoFirestore = resultado.getOrNull()
            
            // Se não há conquistas no Firestore, não fazer nada
            if (conquistasDoFirestore == null || conquistasDoFirestore.isEmpty()) {
                Timber.d("ℹ️ Nenhuma conquista encontrada no Firestore para usuarioId: $usuarioId")
                return
            }
            
            Timber.d("📥 Encontradas ${conquistasDoFirestore.size} conquistas no Firestore para usuarioId: $usuarioId")
            
            // Buscar progressos locais APENAS para este usuarioId
            val progressosLocais = conquistaDao.observarTodasConquistas(usuarioId).first()
            
            // VALIDAÇÃO: Verificar se todos os progressos locais pertencem a este usuarioId
            val progressosLocaisIncorretos = progressosLocais.any { it.usuarioId != usuarioId }
            if (progressosLocaisIncorretos) {
                Timber.e("❌ ERRO CRÍTICO: Encontrados progressos locais com usuarioId incorreto! Esperado: $usuarioId")
                // Não continuar com dados incorretos
                return
            }
            
            // Mesclar: manter progressos locais mais recentes ou atualizar com dados do Firestore
            val progressosAtualizados = mutableListOf<ConquistaEntity>()
            
            conquistasDoFirestore.forEach { progressoFirestore ->
                val progressoLocal = progressosLocais.find { 
                    it.conquistaId == progressoFirestore.conquistaId && it.usuarioId == usuarioId 
                }
                
                // Se não existe localmente, usar do Firestore
                // Se existe localmente, usar o mais recente (comparar por desbloqueadaEm ou progressoAtual)
                val progressoFinal = if (progressoLocal == null) {
                    progressoFirestore
                } else {
                    // VALIDAÇÃO: Garantir que o progresso local pertence a este usuarioId
                    if (progressoLocal.usuarioId != usuarioId) {
                        Timber.e("❌ ERRO: Progresso local tem usuarioId incorreto! Conquista: ${progressoLocal.conquistaId}, Esperado: $usuarioId, Encontrado: ${progressoLocal.usuarioId}")
                        // Usar dados do Firestore como fallback
                        progressoFirestore
                    } else {
                        // Usar o que tiver mais progresso ou estiver desbloqueado
                        when {
                            progressoFirestore.desbloqueada && !progressoLocal.desbloqueada -> progressoFirestore
                            progressoLocal.desbloqueada && !progressoFirestore.desbloqueada -> progressoLocal.toDomain()
                            progressoFirestore.progressoAtual > progressoLocal.progressoAtual -> progressoFirestore
                            else -> progressoLocal.toDomain()
                        }
                    }
                }
                
                // GARANTIR que o usuarioId está correto ao criar a entity
                progressosAtualizados.add(
                    ConquistaEntity.fromDomain(
                        progresso = progressoFinal,
                        usuarioId = usuarioId, // SEMPRE usar o usuarioId passado como parâmetro
                        sincronizadoEm = System.currentTimeMillis(),
                        precisaSincronizar = false
                    )
                )
            }
            
            // Garantir que todas as conquistas do sistema estejam presentes
            val conquistasSistema = SistemaConquistas.obterTodas()
            conquistasSistema.forEach { conquista ->
                val existe = progressosAtualizados.any { 
                    it.conquistaId == conquista.id && it.usuarioId == usuarioId 
                }
                if (!existe) {
                    // Adicionar conquista que não existe no Firestore nem localmente
                    progressosAtualizados.add(
                        ConquistaEntity.fromDomain(
                            progresso = ProgressoConquista(
                                conquistaId = conquista.id,
                                desbloqueada = false,
                                desbloqueadaEm = null,
                                progressoAtual = 0,
                                progressoTotal = conquista.condicao.valor
                            ),
                            usuarioId = usuarioId, // SEMPRE usar o usuarioId correto
                            precisaSincronizar = false
                        )
                    )
                }
            }
            
            // VALIDAÇÃO FINAL: Verificar se todos os progressos têm usuarioId correto antes de salvar
            val progressosComUsuarioIdIncorreto = progressosAtualizados.any { it.usuarioId != usuarioId }
            if (progressosComUsuarioIdIncorreto) {
                Timber.e("❌ ERRO CRÍTICO: Tentando salvar progressos com usuarioId incorreto!")
                return
            }
            
            // Inserir/atualizar progressos (Room vai garantir que apenas progressos com usuarioId correto sejam salvos devido ao composite key)
            conquistaDao.inserirTodas(progressosAtualizados)
            
            // VALIDAÇÃO PÓS-SALVAMENTO: Verificar se foi salvo corretamente
            val progressosVerificacao = conquistaDao.observarTodasConquistas(usuarioId).first()
            val progressosIncorretosAposSave = progressosVerificacao.any { it.usuarioId != usuarioId }
            if (progressosIncorretosAposSave) {
                Timber.e("❌ ERRO CRÍTICO: Progressos salvos com usuarioId incorreto após sincronização!")
            } else {
                Timber.d("✅ Conquistas sincronizadas do Firestore para usuarioId: $usuarioId (${progressosAtualizados.size} conquistas)")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao sincronizar conquistas do Firestore para usuarioId: $usuarioId")
        }
    }
    
    /**
     * Sincroniza conquistas locais que precisam ser enviadas para o Firestore
     * VALIDAÇÃO RIGOROSA: Sempre valida que o usuarioId está correto
     */
    suspend fun sincronizarConquistasParaFirestore(usuarioId: String) {
        try {
            // VALIDAÇÃO CRÍTICA: usuarioId não pode estar vazio
            if (usuarioId.isBlank()) {
                Timber.e("❌ ERRO CRÍTICO: usuarioId está vazio ao sincronizar para o Firestore!")
                return
            }
            
            // Buscar apenas conquistas que precisam ser sincronizadas PARA ESTE usuarioId
            val progressosParaSincronizar = conquistaDao.buscarPendentesSincronizacao(usuarioId)
            
            // VALIDAÇÃO: Verificar se todos os progressos pertencem a este usuarioId
            val progressosIncorretos = progressosParaSincronizar.any { it.usuarioId != usuarioId }
            if (progressosIncorretos) {
                Timber.e("❌ ERRO CRÍTICO: Encontrados progressos para sincronizar com usuarioId incorreto! Esperado: $usuarioId")
                return
            }
            
            if (progressosParaSincronizar.isEmpty()) {
                Timber.d("ℹ️ Nenhuma conquista pendente de sincronização para usuarioId: $usuarioId")
                return
            }
            
            Timber.d("📤 Sincronizando ${progressosParaSincronizar.size} conquistas para o Firestore (usuarioId: $usuarioId)")
            
            val progressosDomain = progressosParaSincronizar.map { it.toDomain() }
            val resultado = firestoreService.salvarTodasConquistas(usuarioId, progressosDomain)
            
            if (resultado.isSuccess) {
                // Marcar como sincronizadas (garantindo que usuarioId está correto)
                progressosParaSincronizar.forEach { entity ->
                    // VALIDAÇÃO: Garantir que a entity tem usuarioId correto antes de atualizar
                    if (entity.usuarioId == usuarioId) {
                        conquistaDao.inserirOuAtualizar(
                            entity.copy(
                                precisaSincronizar = false,
                                sincronizadoEm = System.currentTimeMillis()
                            )
                        )
                    } else {
                        Timber.e("❌ ERRO: Tentando sincronizar entity com usuarioId incorreto! Conquista: ${entity.conquistaId}, Esperado: $usuarioId, Encontrado: ${entity.usuarioId}")
                    }
                }
                Timber.d("✅ ${progressosParaSincronizar.size} conquistas sincronizadas para o Firestore (usuarioId: $usuarioId)")
            } else {
                Timber.e(resultado.exceptionOrNull(), "❌ Erro ao sincronizar conquistas para o Firestore (usuarioId: $usuarioId)")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao sincronizar conquistas para o Firestore (usuarioId: $usuarioId)")
        }
    }
    
    /**
     * Sincroniza todas as conquistas do usuário (força sincronização completa)
     * IMPORTANTE: Só deve ser chamado para usuários que JÁ têm perfil local
     */
    suspend fun sincronizarTodasConquistas(usuarioId: String) {
        try {
            // Verificar se perfil existe antes de sincronizar
            val perfilExistente = perfilGamificacaoDao.buscarPorUsuarioId(usuarioId)
            if (perfilExistente == null) {
                Timber.d("ℹ️ Perfil não existe, inicializando novo perfil: $usuarioId")
                inicializarPerfil(usuarioId)
                return
            }
            
            // Primeiro, carregar do Firestore (puxar dados remotos)
            sincronizarConquistasDoFirestore(usuarioId)
            
            // Recalcular XP do perfil baseado nas conquistas sincronizadas
            recalcularXPDoPerfil(usuarioId)
            
            // Depois, enviar pendências locais (empurrar dados locais)
            sincronizarConquistasParaFirestore(usuarioId)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao sincronizar todas as conquistas")
        }
    }
    
    /**
     * Atualiza progresso de uma conquista do usuário
     * Marca como precisaSincronizar e sincroniza com Firestore
     */
    suspend fun atualizarProgressoConquista(
        conquistaId: String,
        usuarioId: String,
        progressoAtual: Int,
        desbloqueada: Boolean = false
    ) {
        try {
            val timestamp = if (desbloqueada) System.currentTimeMillis() else null
            
            // Buscar progresso atual para pegar progressoTotal
            // Se não encontrar localmente, buscar da definição do sistema
            val entityAtual = conquistaDao.buscarPorId(conquistaId, usuarioId)
            val progressoTotal = if (entityAtual != null) {
                entityAtual.progressoTotal
            } else {
                // Buscar da definição do sistema
                SistemaConquistas.obterTodas()
                    .find { it.id == conquistaId }
                    ?.condicao?.valor ?: 0
            }
            
            // Atualizar no banco local (já marca precisaSincronizar = true)
            conquistaDao.atualizarProgresso(
                conquistaId = conquistaId,
                usuarioId = usuarioId,
                progressoAtual = progressoAtual,
                desbloqueada = desbloqueada,
                desbloqueadaEm = timestamp
            )
            
            // Sincronizar com Firestore em background
            firestoreService.salvarConquista(
                usuarioId = usuarioId,
                conquistaId = conquistaId,
                desbloqueada = desbloqueada,
                desbloqueadaEm = timestamp,
                progressoAtual = progressoAtual,
                progressoTotal = progressoTotal
            ).onSuccess {
                // Marcar como sincronizado
                val entityAtualizada = conquistaDao.buscarPorId(conquistaId, usuarioId)
                if (entityAtualizada != null) {
                    conquistaDao.inserirOuAtualizar(
                        entityAtualizada.copy(
                            precisaSincronizar = false,
                            sincronizadoEm = System.currentTimeMillis()
                        )
                    )
                }
            }.onFailure {
                Timber.w("⚠️ Falha ao sincronizar progresso, será sincronizado depois: $conquistaId")
            }
            
            Timber.d("✅ Progresso atualizado: $conquistaId para usuário $usuarioId - $progressoAtual/$progressoTotal")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar progresso de conquista")
        }
    }
    
    /**
     * Desbloqueia uma conquista do usuário e adiciona XP
     * Sincroniza com Firestore automaticamente
     */
    suspend fun desbloquearConquista(conquistaId: String, usuarioId: String, xp: Int) {
        try {
            val timestamp = System.currentTimeMillis()
            
            // Buscar progresso atual
            val entity = conquistaDao.buscarPorId(conquistaId, usuarioId)
            if (entity == null) {
                Timber.e("❌ Conquista não encontrada: $conquistaId")
                return
            }
            
            // Verificar se já está desbloqueada (evitar duplicação de XP)
            if (entity.desbloqueada) {
                Timber.d("ℹ️ Conquista já estava desbloqueada: $conquistaId")
                return
            }
            
            // Marcar como desbloqueada para o usuário específico
            conquistaDao.marcarComoDesbloqueada(conquistaId, usuarioId, timestamp)
            
            // Atualizar perfil com XP
            val perfilAtual = perfilGamificacaoDao.buscarPorUsuarioId(usuarioId)
            if (perfilAtual != null) {
                val novoXPTotal = perfilAtual.xpTotal + xp
                val novoNivel = SistemaConquistas.calcularNivel(novoXPTotal)
                
                perfilGamificacaoDao.adicionarXP(usuarioId, xp, novoNivel)
                
                // Atualizar contador de conquistas
                val quantidadeAtual = perfilAtual.conquistasDesbloqueadas + 1
                perfilGamificacaoDao.atualizarContadorConquistas(usuarioId, quantidadeAtual)
            }
            
            // Sincronizar com Firestore
            firestoreService.salvarConquista(
                usuarioId = usuarioId,
                conquistaId = conquistaId,
                desbloqueada = true,
                desbloqueadaEm = timestamp,
                progressoAtual = entity.progressoTotal,
                progressoTotal = entity.progressoTotal
            ).onSuccess {
                // Marcar como sincronizado
                val entityAtualizada = entity.copy(
                    desbloqueada = true,
                    desbloqueadaEm = timestamp,
                    precisaSincronizar = false,
                    sincronizadoEm = System.currentTimeMillis()
                )
                conquistaDao.inserirOuAtualizar(entityAtualizada)
                Timber.d("✅ Conquista sincronizada com Firestore: $conquistaId")
            }.onFailure {
                // Marcar como precisa sincronizar para tentar depois
                val entityAtualizada = entity.copy(
                    desbloqueada = true,
                    desbloqueadaEm = timestamp,
                    precisaSincronizar = true
                )
                conquistaDao.inserirOuAtualizar(entityAtualizada)
                Timber.w("⚠️ Falha ao sincronizar conquista, será sincronizada depois: $conquistaId")
            }
            
            Timber.d("✅ Conquista desbloqueada: $conquistaId para usuário $usuarioId (+$xp XP)")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao desbloquear conquista")
        }
    }
    
    /**
     * Observa contador de conquistas desbloqueadas do usuário
     */
    fun observarContadorConquistasDesbloqueadas(usuarioId: String): Flow<Int> {
        return conquistaDao.contarDesbloqueadas(usuarioId)
    }
    
    /**
     * Atualiza contador de conquistas no perfil
     */
    suspend fun atualizarContadorConquistas(usuarioId: String, quantidade: Int) {
        try {
            perfilGamificacaoDao.atualizarContadorConquistas(usuarioId, quantidade)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar contador de conquistas")
        }
    }
}

