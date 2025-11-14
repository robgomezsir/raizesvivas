package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.ConquistaDao
import com.raizesvivas.app.data.local.entities.ConquistaEntity
import com.raizesvivas.app.domain.model.ProgressoConquista
import com.raizesvivas.app.domain.model.TipoAcao
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar conquistas disponíveis do Firestore
 * e rastreamento de ações
 */
@Singleton
class ConquistasRepository @Inject constructor(
    private val conquistaDao: ConquistaDao,
    private val firestoreService: FirestoreService
) {
    
    /**
     * Mapeia tipo de ação para IDs de conquistas relacionadas
     * 
     * Baseado no sistema expandido de 80+ conquistas
     */
    fun mapearAcaoParaConquistas(tipoAcao: TipoAcao): List<String> {
        return when (tipoAcao) {
            // ========================================
            // BEM-VINDO: Onboarding
            // ========================================
            TipoAcao.PRIMEIRO_LOGIN -> listOf("bem_vindo")
            TipoAcao.COMPLETAR_PERFIL -> listOf("primeiro_passo")
            TipoAcao.EXPLORAR_ARVORE_PRIMEIRA_VEZ -> listOf("explorador_curioso")
            TipoAcao.COMPLETAR_TUTORIAL -> listOf("tutorial_completo")
            TipoAcao.ACESSO_DIARIO -> listOf(
                "primeira_visita_semanal",  // 3 dias
                "primeira_semana",           // 7 dias
                "usuario_mensal",           // 30 dias
                "veterano",                 // 100 dias
                "lenda"                     // 365 dias
            )
            
            // ========================================
            // CONSTRUTOR: Adicionar membros
            // ========================================
            TipoAcao.ADICIONAR_MEMBRO -> listOf(
                "primeiro_membro",           // 1 membro
                "construtor_iniciante",      // 5 membros
                "construtor_intermediario", // 15 membros
                "arvore_crescendo",         // 25 membros
                "construtor_avancado",      // 50 membros
                "construtor_mestre"         // 100 membros
            )
            TipoAcao.ADICIONAR_PAIS_IRMAOS -> listOf("familia_nuclear") // 3 membros
            TipoAcao.ADICIONAR_DUAS_GERACOES -> listOf("duas_geracoes")
            TipoAcao.ADICIONAR_TRES_GERACOES -> listOf("tres_geracoes")
            TipoAcao.ADICIONAR_QUATRO_GERACOES -> listOf("quatro_geracoes")
            TipoAcao.ADICIONAR_CINCO_GERACOES -> listOf("cinco_geracoes")
            TipoAcao.CRIAR_FAMILIA_ZERO -> listOf("raizes_plantadas")
            TipoAcao.CRIAR_SUBFAMILIA -> listOf(
                "unificador_familiar"       // 10 subfamílias (épica)
            )
            
            // ========================================
            // HISTORIADOR: Adicionar informações
            // ========================================
            TipoAcao.ADICIONAR_FOTO -> listOf(
                "primeira_foto",            // 1 foto
                "fotografo_familiar",       // 5 fotos
                "colecionador_memorias",    // 15 fotos
                "historiador_avancado",     // 50 fotos
                "arquivista_mestre"         // 100 fotos
            )
            
            TipoAcao.ADICIONAR_DATA_NASCIMENTO -> listOf("primeira_data") // 3 datas
            TipoAcao.ADICIONAR_LOCAL_NASCIMENTO -> listOf("detalhista") // 10 locais
            
            TipoAcao.ADICIONAR_BIOGRAFIA -> listOf(
                "primeira_biografia",       // 1 biografia
                "biografo",                 // 5 biografias
                "escritor_familiar"         // 15 biografias
            )
            
            TipoAcao.PREENCHER_COMPLETO -> listOf(
                "historiador_iniciante",    // 5 completos
                "cronista_familiar",        // 25 completos
                "perfeccionista"            // 50 completos (épica)
            )
            
            // ========================================
            // CONECTOR: Interação social
            // ========================================
            TipoAcao.ENVIAR_MENSAGEM -> listOf(
                "primeira_mensagem",        // 1 mensagem
                "conector_iniciante",       // 10 mensagens
                "comunicador",              // 50 mensagens
                "conector_avancado",        // 200 mensagens
                "conector_mestre"           // 1000 mensagens
            )
            TipoAcao.ENVIAR_MENSAGEM_DIFERENTES_PARENTES -> listOf(
                "sociavel",                 // 3 contatos
                "rede_social"               // 10 contatos
            )
            
            TipoAcao.CRIAR_RECADO -> listOf(
                "primeiro_recado",          // 1 recado
                "publicador"                // 10 recados
            )
            
            TipoAcao.DAR_APOIO_FAMILIAR -> listOf(
                "apoiador"                  // 5 apoios
            )
            
            TipoAcao.RECEBER_APOIO_FAMILIAR -> listOf(
                "influencer_familiar",      // 50 apoios recebidos
                "celebridade_familiar"      // 200 apoios recebidos
            )
            
            // ========================================
            // EXPLORADOR: Navegação
            // ========================================
            TipoAcao.VISUALIZAR_MEMBRO -> listOf(
                "primeira_exploracao",      // 5 perfis
                "explorador_ativo",         // 25 perfis
                "conhecedor_familia"        // 50 perfis
            )
            
            TipoAcao.VISUALIZAR_ARVORE -> listOf(
                "curioso",                  // 10 vezes
                "navegador",                // 50 vezes
                "explorador_mestre"         // 200 vezes
            )
            
            TipoAcao.VISUALIZAR_PARENTESCO -> listOf(
                "descobridor_parentesco"    // 1ª vez
            )
            
            // ========================================
            // ASSIDUIDADE: Engajamento temporal
            // ========================================
            TipoAcao.ACESSO_MANHA -> listOf("madrugador") // Antes 8h
            TipoAcao.ACESSO_NOITE -> listOf("noturno") // Depois 22h
            TipoAcao.ACESSO_FIM_SEMANA -> listOf("fim_de_semana") // 10 fins de semana
            
            // ========================================
            // EVENTOS ESPECIAIS
            // ========================================
            TipoAcao.ACESSO_ANIVERSARIO -> listOf("aniversariante")
            TipoAcao.ACESSO_NATAL -> listOf("natal_familiar")
            TipoAcao.ACESSO_ANO_NOVO -> listOf("ano_novo")
            TipoAcao.ACESSO_DIA_MAES -> listOf("dia_das_maes")
            TipoAcao.ACESSO_DIA_PAIS -> listOf("dia_dos_pais")
            
            // ========================================
            // ÉPICAS: Meta-conquistas (geradas automaticamente)
            // ========================================
            TipoAcao.TODAS_CONSTRUTOR -> listOf("genealogista_profissional")
            TipoAcao.TODAS_HISTORIADOR -> listOf("historiador_mestre_epico")
            TipoAcao.ALCANCAR_NIVEL -> listOf("lenda_viva") // Nível 50
            TipoAcao.TODAS_CONQUISTAS -> listOf("colecionador_supremo")
            
            else -> emptyList()
        }
    }
    
    /**
     * Registra ação do usuário e atualiza progresso das conquistas relacionadas
     * 
     * @param usuarioId ID do usuário que realizou a ação
     * @param tipoAcao Tipo da ação realizada
     */
    suspend fun registrarAcao(usuarioId: String, tipoAcao: TipoAcao) {
        if (usuarioId.isBlank()) {
            Timber.e("❌ registrarAcao: usuarioId está vazio!")
            return
        }
        
        try {
            val conquistasRelacionadas = mapearAcaoParaConquistas(tipoAcao)
            
            if (conquistasRelacionadas.isEmpty()) {
                Timber.d("ℹ️ Nenhuma conquista relacionada à ação: $tipoAcao")
                return
            }
            
            Timber.d("🎯 Registrando ação: $tipoAcao → ${conquistasRelacionadas.size} conquistas")
            
            // Atualizar progresso de cada conquista relacionada
            conquistasRelacionadas.forEach { conquistaId ->
                atualizarProgressoConquista(usuarioId, conquistaId, incremento = 1)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao registrar ação: $tipoAcao")
        }
    }
    
    /**
     * Atualiza progresso de uma conquista específica
     * 
     * @param usuarioId ID do usuário
     * @param conquistaId ID da conquista
     * @param incremento Valor a incrementar no progresso (padrão: 1)
     */
    private suspend fun atualizarProgressoConquista(
        usuarioId: String,
        conquistaId: String,
        incremento: Int = 1
    ) {
        try {
            // Buscar progresso atual
            val progressoAtual = conquistaDao.buscarPorId(conquistaId, usuarioId)
            
            // Buscar conquista disponível do Firestore para obter critério
            val conquistaDisponivel = firestoreService.buscarConquistaDisponivel(conquistaId)
                .getOrNull()
            
            if (conquistaDisponivel == null) {
                Timber.w("⚠️ Conquista não encontrada no Firestore, tentando SistemaConquistas: $conquistaId")
                // Fallback: tentar buscar do sistema hardcoded
                val conquistaSistema = com.raizesvivas.app.domain.model.SistemaConquistas.obterTodas()
                    .find { it.id == conquistaId }
                
                if (conquistaSistema == null) {
                    Timber.e("❌ Conquista não encontrada em nenhum lugar: $conquistaId")
                    return
                }
                
                // Usar dados do sistema hardcoded
                val novoProgresso = (progressoAtual?.progresso ?: 0) + incremento
                val criterio = conquistaSistema.condicao.valor
                val foiConcluida = novoProgresso >= criterio
                
                // Calcular pontuacaoTotal (XP ganho) quando concluída
                val pontuacaoTotal = if (foiConcluida && progressoAtual?.concluida != true) {
                    conquistaSistema.recompensaXP
                } else {
                    progressoAtual?.pontuacaoTotal ?: 0
                }
                
                val timestampDesbloqueio = if (foiConcluida && progressoAtual?.desbloqueadaEm == null) {
                    System.currentTimeMillis()
                } else {
                    progressoAtual?.desbloqueadaEm
                }
                
                // Criar ou atualizar progresso usando dados do sistema
                val entity = if (progressoAtual == null) {
                    ConquistaEntity.fromDomain(
                        progresso = ProgressoConquista(
                            conquistaId = conquistaId,
                            concluida = foiConcluida,
                            desbloqueadaEm = timestampDesbloqueio?.let { java.util.Date(it) },
                            progresso = novoProgresso,
                            progressoTotal = criterio,
                            pontuacaoTotal = pontuacaoTotal
                        ),
                        usuarioId = usuarioId,
                        precisaSincronizar = true
                    )
                } else {
                    progressoAtual.copy(
                        progresso = novoProgresso,
                        concluida = foiConcluida,
                        desbloqueadaEm = timestampDesbloqueio,
                        pontuacaoTotal = pontuacaoTotal,
                        precisaSincronizar = true
                    )
                }
                
                conquistaDao.inserirOuAtualizar(entity)
                
                // Sincronizar com Firestore
                // Garantir que desbloqueadaEm seja enviado quando concluída
                val desbloqueadaEmParaFirestore = if (foiConcluida && entity.desbloqueadaEm == null) {
                    System.currentTimeMillis()
                } else {
                    entity.desbloqueadaEm
                }
                
                firestoreService.salvarConquista(
                    usuarioId = usuarioId,
                    conquistaId = conquistaId,
                    concluida = foiConcluida,
                    desbloqueadaEm = desbloqueadaEmParaFirestore,
                    progresso = novoProgresso,
                    progressoTotal = criterio,
                    nivel = entity.nivel,
                    pontuacaoTotal = pontuacaoTotal
                )
                
                if (foiConcluida && progressoAtual?.concluida != true) {
                    Timber.d("✅ Conquista concluída: $conquistaId (+$pontuacaoTotal XP)")
                }
                return
            }
            
            val novoProgresso = (progressoAtual?.progresso ?: 0) + incremento
            val criterio = conquistaDisponivel.criterio
            val foiConcluida = novoProgresso >= criterio
            
            // Calcular pontuacaoTotal (XP ganho) quando concluída
            val pontuacaoTotal = if (foiConcluida && progressoAtual?.concluida != true) {
                conquistaDisponivel.pontosRecompensa
            } else {
                progressoAtual?.pontuacaoTotal ?: 0
            }
            
            val timestampDesbloqueio = if (foiConcluida && progressoAtual?.desbloqueadaEm == null) {
                System.currentTimeMillis()
            } else {
                progressoAtual?.desbloqueadaEm
            }
            
            // Criar ou atualizar progresso
            val entity = if (progressoAtual == null) {
                ConquistaEntity.fromDomain(
                    progresso = ProgressoConquista(
                        conquistaId = conquistaId,
                        concluida = foiConcluida,
                        desbloqueadaEm = timestampDesbloqueio?.let { java.util.Date(it) },
                        progresso = novoProgresso,
                        progressoTotal = criterio,
                        pontuacaoTotal = pontuacaoTotal
                    ),
                    usuarioId = usuarioId,
                    precisaSincronizar = true
                )
            } else {
                progressoAtual.copy(
                    progresso = novoProgresso,
                    concluida = foiConcluida,
                    desbloqueadaEm = timestampDesbloqueio,
                    pontuacaoTotal = pontuacaoTotal,
                    precisaSincronizar = true
                )
            }
            
            conquistaDao.inserirOuAtualizar(entity)
            
            // Sincronizar com Firestore (não crítico se falhar - dados já salvos localmente)
            // Garantir que desbloqueadaEm seja enviado quando concluída
            val desbloqueadaEmParaFirestore = if (foiConcluida && entity.desbloqueadaEm == null) {
                System.currentTimeMillis()
            } else {
                entity.desbloqueadaEm
            }
            
            val resultadoFirestore = firestoreService.salvarConquista(
                usuarioId = usuarioId,
                conquistaId = conquistaId,
                concluida = foiConcluida,
                desbloqueadaEm = desbloqueadaEmParaFirestore,
                progresso = novoProgresso,
                progressoTotal = criterio,
                nivel = entity.nivel,
                pontuacaoTotal = pontuacaoTotal
            )
            
            resultadoFirestore.onFailure { erro ->
                // Log do erro mas não interrompe o fluxo
                when {
                    erro is com.google.firebase.firestore.FirebaseFirestoreException && 
                    erro.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        Timber.w("⚠️ Permissão negada ao salvar conquista no Firestore (dados salvos localmente): $conquistaId")
                    }
                    else -> {
                        Timber.e(erro, "⚠️ Erro ao sincronizar conquista com Firestore (dados salvos localmente): $conquistaId")
                    }
                }
            }
            
            if (foiConcluida && progressoAtual?.concluida != true) {
                Timber.d("✅ Conquista concluída: $conquistaId (+$pontuacaoTotal XP)")
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Re-throw cancellation exceptions para não mascarar cancelamentos legítimos
            throw e
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao atualizar progresso: $conquistaId")
            // Não re-throw para não cancelar o job pai
        }
    }
}

