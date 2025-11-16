package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.GamificacaoRepository
import com.raizesvivas.app.data.repository.NotificacaoRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.SubfamiliaRepository
import com.raizesvivas.app.domain.model.*
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase para verificar e desbloquear conquistas automaticamente
 */
class VerificarConquistasUseCase @Inject constructor(
    private val gamificacaoRepository: GamificacaoRepository,
    private val pessoaRepository: PessoaRepository,
    private val subfamiliaRepository: SubfamiliaRepository,
    private val notificacaoRepository: NotificacaoRepository
) {
    
    /**
     * Verifica todas as conquistas e desbloqueia as que foram completadas
     */
    suspend fun verificarTodasConquistas(usuarioId: String) {
        if (usuarioId.isBlank()) {
            Timber.e("❌ VerificarConquistasUseCase: usuarioId é nulo ou vazio.")
            return
        }
        
        try {
            // Garantir que o perfil de gamificação e as conquistas estejam inicializados
            gamificacaoRepository.inicializarPerfil(usuarioId)
            
            val conquistas = SistemaConquistas.obterTodas()
            val progressos = gamificacaoRepository.observarTodasConquistas(usuarioId).first()
            
            conquistas.forEach { conquista ->
                val progressoAtual = progressos.find { it.conquistaId == conquista.id }
                
                // Se já está concluída, pular
                if (progressoAtual?.concluida == true) {
                    return@forEach
                }
                
                // Verificar condição
                val novoProgresso = verificarCondicao(conquista, usuarioId)
                
                if (novoProgresso != null) {
                    val foiConcluida = novoProgresso >= conquista.condicao.valor
                    
                    // Atualizar progresso do usuário
                    gamificacaoRepository.atualizarProgressoConquista(
                        conquistaId = conquista.id,
                        usuarioId = usuarioId,
                        progresso = novoProgresso,
                        concluida = foiConcluida
                    )
                    
                    // Se foi concluída E não estava concluída antes, adicionar XP e criar notificação
                    if (foiConcluida && progressoAtual?.concluida != true) {
                        gamificacaoRepository.desbloquearConquista(
                            conquistaId = conquista.id,
                            usuarioId = usuarioId,
                            xp = conquista.recompensaXP
                        )
                        
                        // Criar notificação de conquista desbloqueada
                        notificacaoRepository.criarNotificacao(
                            Notificacao(
                                id = java.util.UUID.randomUUID().toString(),
                                tipo = TipoNotificacao.CONQUISTA_DESBLOQUEADA,
                                titulo = "Conquista Desbloqueada!",
                                mensagem = "Você desbloqueou: ${conquista.nome} (+${conquista.recompensaXP} XP)",
                                lida = false,
                                criadaEm = java.util.Date(),
                                relacionadoId = conquista.id,
                                dadosExtras = mapOf(
                                    "conquistaId" to conquista.id,
                                    "xp" to conquista.recompensaXP.toString()
                                )
                            )
                        )
                        
                        Timber.d("✅ Conquista desbloqueada: ${conquista.nome} (+${conquista.recompensaXP} XP)")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao verificar conquistas")
        }
    }
    
    /**
     * Verifica uma conquista específica do usuário
     */
    @Suppress("unused")
    suspend fun verificarConquista(conquista: Conquista, usuarioId: String) {
        try {
            val progressoAtual = gamificacaoRepository.observarProgressoConquista(conquista.id, usuarioId).first()
            
            // Se já está concluída, pular
            if (progressoAtual?.concluida == true) {
                return
            }
            
            // Verificar condição
            val novoProgresso = verificarCondicao(conquista, usuarioId)
            
            if (novoProgresso != null) {
                val foiConcluida = novoProgresso >= conquista.condicao.valor
                
                // Atualizar progresso do usuário
                gamificacaoRepository.atualizarProgressoConquista(
                    conquistaId = conquista.id,
                    usuarioId = usuarioId,
                    progresso = novoProgresso,
                    concluida = foiConcluida
                )
                
                // Se foi concluída, adicionar XP e criar notificação
                if (foiConcluida) {
                    gamificacaoRepository.desbloquearConquista(
                        conquistaId = conquista.id,
                        usuarioId = usuarioId,
                        xp = conquista.recompensaXP
                    )
                    
                    // Criar notificação
                    notificacaoRepository.criarNotificacao(
                        Notificacao(
                            id = java.util.UUID.randomUUID().toString(),
                            tipo = TipoNotificacao.CONQUISTA_DESBLOQUEADA,
                            titulo = "Conquista Desbloqueada!",
                            mensagem = "Você desbloqueou: ${conquista.nome} (+${conquista.recompensaXP} XP)",
                            lida = false,
                            criadaEm = java.util.Date(),
                            relacionadoId = conquista.id,
                            dadosExtras = mapOf(
                                "conquistaId" to conquista.id,
                                "xp" to conquista.recompensaXP.toString()
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao verificar conquista: ${conquista.id}")
        }
    }
    
    /**
     * Verifica condição de uma conquista e retorna o progresso atual
     */
    private suspend fun verificarCondicao(conquista: Conquista, @Suppress("UNUSED_PARAMETER") usuarioId: String): Int? {
        return when (conquista.condicao.tipo) {
            // Bem-vindo
            TipoCondicao.PRIMEIRO_LOGIN -> {
                // Verificar se já existe progresso para esta conquista
                // Se existe progresso > 0, significa que já fez login
                val progressoAtual = gamificacaoRepository.observarProgressoConquista(conquista.id, usuarioId).first()
                if (progressoAtual != null && progressoAtual.progresso > 0) {
                    progressoAtual.progresso
                } else {
                    // Se não tem progresso, verificar se é realmente o primeiro login
                    // Verificando se o usuário tem perfil de gamificação criado recentemente
                    val perfil = gamificacaoRepository.observarPerfilGamificacao(usuarioId).first()
                    if (perfil != null) {
                        // Se o perfil existe, significa que já fez login antes
                        // Mas vamos retornar 0 para não desbloquear automaticamente
                        // A conquista só será desbloqueada quando registrarAcao for chamado
                        0
                    } else {
                        // Novo usuário sem perfil ainda - retornar 0
                        0
                    }
                }
            }
            TipoCondicao.COMPLETAR_PERFIL -> {
                // TODO: Verificar se perfil está completo (nome e foto)
                0
            }
            TipoCondicao.EXPLORAR_ARVORE_PRIMEIRA_VEZ -> {
                // Verificar se já existe progresso para esta conquista
                val progressoAtual = gamificacaoRepository.observarProgressoConquista(conquista.id, usuarioId).first()
                if (progressoAtual != null && progressoAtual.progresso > 0) {
                    progressoAtual.progresso
                } else {
                    // Se não tem progresso, retornar 0
                    // A conquista só será desbloqueada quando registrarAcao for chamado
                    0
                }
            }
            TipoCondicao.COMPLETAR_TUTORIAL -> {
                // TODO: Verificar se tutorial foi completado
                0
            }
            TipoCondicao.ACESSO_DIARIO -> {
                // TODO: Implementar contagem de dias consecutivos de acesso
                0
            }
            
            // Construtor
            TipoCondicao.ADICIONAR_MEMBROS -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.size
            }
            TipoCondicao.ADICIONAR_MEMBROS_TOTAL -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.size
            }
            TipoCondicao.ADICIONAR_PAIS_IRMAOS -> {
                // TODO: Verificar se adicionou pais e irmãos (3 membros)
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.size.coerceAtMost(3)
            }
            TipoCondicao.ADICIONAR_DUAS_GERACOES -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                if (pessoas.isEmpty()) 0
                else {
                    pessoas.mapNotNull { it.distanciaFamiliaZero }.toSet().size.coerceAtMost(2)
                }
            }
            TipoCondicao.ADICIONAR_TRES_GERACOES -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                if (pessoas.isEmpty()) 0
                else {
                    pessoas.mapNotNull { it.distanciaFamiliaZero }.toSet().size.coerceAtMost(3)
                }
            }
            TipoCondicao.ADICIONAR_QUATRO_GERACOES -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                if (pessoas.isEmpty()) 0
                else {
                    pessoas.mapNotNull { it.distanciaFamiliaZero }.toSet().size.coerceAtMost(4)
                }
            }
            TipoCondicao.ADICIONAR_CINCO_GERACOES -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                if (pessoas.isEmpty()) 0
                else {
                    pessoas.mapNotNull { it.distanciaFamiliaZero }.toSet().size.coerceAtMost(5)
                }
            }
            TipoCondicao.CRIAR_FAMILIA_ZERO -> {
                // Verificar se existe família zero
                val subfamilias = subfamiliaRepository.observarTodasSubfamilias().first()
                // Se há subfamílias, significa que há família zero
                if (subfamilias.isNotEmpty()) 1 else 0
            }
            TipoCondicao.CRIAR_SUBFAMILIAS -> {
                val subfamilias = subfamiliaRepository.observarTodasSubfamilias().first()
                subfamilias.size
            }
            
            // Historiador
            TipoCondicao.ADICIONAR_FOTOS -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.count { it.fotoUrl != null && it.fotoUrl.isNotEmpty() }
            }
            TipoCondicao.ADICIONAR_DATA_NASCIMENTO -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.count { it.dataNascimento != null }
            }
            TipoCondicao.ADICIONAR_BIOGRAFIA -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.count { !it.biografia.isNullOrEmpty() }
            }
            TipoCondicao.ADICIONAR_LOCAL_NASCIMENTO -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.count { !it.localNascimento.isNullOrEmpty() }
            }
            TipoCondicao.COMPLETAR_MEMBROS -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                // Considera completo se tem nome, data de nascimento e local de nascimento
                pessoas.count { 
                    it.nome.isNotEmpty() && 
                    it.dataNascimento != null && 
                    !it.localNascimento.isNullOrEmpty()
                }
            }
            TipoCondicao.PREENCHER_COMPLETO -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                // Considera completo se tem todos os campos preenchidos
                pessoas.count { 
                    it.nome.isNotEmpty() && 
                    it.dataNascimento != null && 
                    !it.localNascimento.isNullOrEmpty() &&
                    it.fotoUrl != null && it.fotoUrl.isNotEmpty() &&
                    !it.biografia.isNullOrEmpty()
                }
            }
            
            // Conector
            TipoCondicao.ENVIAR_MENSAGEM -> {
                // TODO: Implementar contagem de mensagens enviadas
                0
            }
            TipoCondicao.ENVIAR_MENSAGEM_DIFERENTES_PARENTES -> {
                // TODO: Implementar contagem de parentes diferentes com quem conversou
                0
            }
            TipoCondicao.CRIAR_RECADO -> {
                // TODO: Implementar contagem de recados criados
                0
            }
            TipoCondicao.DAR_APOIO_FAMILIAR -> {
                // TODO: Implementar contagem de apoios dados
                0
            }
            TipoCondicao.RECEBER_APOIO_FAMILIAR -> {
                // TODO: Implementar contagem de apoios recebidos
                0
            }
            
            // Explorador
            TipoCondicao.VISUALIZAR_MEMBRO -> {
                // TODO: Implementar contagem de perfis visualizados
                0
            }
            TipoCondicao.VISUALIZAR_ARVORE -> {
                // TODO: Implementar contagem de vezes que abriu a árvore
                0
            }
            TipoCondicao.VISUALIZAR_PARENTESCO -> {
                // Verificado quando o cálculo de parentesco é visualizado pela primeira vez
                1
            }
            TipoCondicao.VISUALIZAR_FLORESTA -> {
                // Esta conquista é verificada quando a tela da floresta é visualizada pela primeira vez
                // O valor é sempre 1 (já visualizou) ou 0 (nunca visualizou)
                // A verificação é feita no ArvoreViewModel
                1 // Se chegou aqui, já visualizou
            }
            
            // Assiduidade
            TipoCondicao.ACESSO_MANHA -> {
                // TODO: Verificar se acessou antes das 8h
                0
            }
            TipoCondicao.ACESSO_NOITE -> {
                // TODO: Verificar se acessou depois das 22h
                0
            }
            TipoCondicao.ACESSO_FIM_SEMANA -> {
                // TODO: Implementar contagem de fins de semana acessados
                0
            }
            
            // Especiais
            TipoCondicao.ACESSO_ANIVERSARIO -> {
                // TODO: Verificar se acessou no aniversário
                0
            }
            TipoCondicao.ACESSO_NATAL -> {
                // TODO: Verificar se acessou no Natal
                0
            }
            TipoCondicao.ACESSO_ANO_NOVO -> {
                // TODO: Verificar se acessou no Réveillon
                0
            }
            TipoCondicao.ACESSO_DIA_MAES -> {
                // TODO: Verificar se acessou no Dia das Mães
                0
            }
            TipoCondicao.ACESSO_DIA_PAIS -> {
                // TODO: Verificar se acessou no Dia dos Pais
                0
            }
            
            // Épicas
            TipoCondicao.TODAS_CONSTRUTOR -> {
                // TODO: Verificar se completou todas as conquistas de Construtor
                0
            }
            TipoCondicao.TODAS_HISTORIADOR -> {
                // TODO: Verificar se completou todas as conquistas de Historiador
                0
            }
            TipoCondicao.ALCANCAR_NIVEL -> {
                // TODO: Verificar nível do usuário
                0
            }
            TipoCondicao.TODAS_CONQUISTAS -> {
                // TODO: Verificar se desbloqueou todas as conquistas
                0
            }
            
            // Legado (mantido para compatibilidade)
            TipoCondicao.REGISTRAR_CASAMENTOS -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                // Contar casamentos únicos (cada par conta como 1)
                val casamentos = pessoas.filter { 
                    it.conjugeAtual != null && 
                    (it.estadoCivil == EstadoCivil.CASADO || it.estadoCivil == EstadoCivil.UNIAO_ESTAVEL)
                }.map { it.id to it.conjugeAtual!! }
                    .filter { (pessoaId, conjugeId) -> pessoaId < conjugeId } // Contar cada par apenas uma vez
                casamentos.size
            }
            TipoCondicao.MAPEAR_GERACOES -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                if (pessoas.isEmpty()) 0
                else {
                    pessoas.mapNotNull { it.distanciaFamiliaZero }.toSet().maxOrNull() ?: 0
                }
            }
            TipoCondicao.DESCOBRIR_PARENTESCO_DISTANTE -> {
                // TODO: Implementar verificação de parentesco distante
                // Por enquanto, retorna 0
                0
            }
            TipoCondicao.MAPEAR_ANOS -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                if (pessoas.isEmpty() || pessoas.all { it.dataNascimento == null }) {
                    0
                } else {
                    val calendar = java.util.Calendar.getInstance()
                    val anos = pessoas.mapNotNull { nascimento ->
                        nascimento.dataNascimento?.let {
                            calendar.time = it
                            calendar.get(java.util.Calendar.YEAR)
                        }
                    }
                    if (anos.isEmpty()) 0
                    else (anos.maxOrNull() ?: 0) - (anos.minOrNull() ?: 0)
                }
            }
            TipoCondicao.MEMBRO_IDADE -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                val agora = java.util.Calendar.getInstance()
                val resultado = pessoas.any { pessoa ->
                    pessoa.dataNascimento?.let { nascimento ->
                        val calendarNascimento = java.util.Calendar.getInstance()
                        calendarNascimento.time = nascimento
                        val idade = agora.get(java.util.Calendar.YEAR) - calendarNascimento.get(java.util.Calendar.YEAR)
                        idade >= conquista.condicao.valor
                    } ?: false
                }
                if (resultado) 1 else 0
            }
            TipoCondicao.MAPEAR_GERACOES_TOTAL -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                if (pessoas.isEmpty()) 0
                else {
                    pessoas.mapNotNull { it.distanciaFamiliaZero }.toSet().size
                }
            }
        }
    }
}

