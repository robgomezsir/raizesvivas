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
                
                // Se já está desbloqueada, pular
                if (progressoAtual?.desbloqueada == true) {
                    return@forEach
                }
                
                // Verificar condição
                val novoProgresso = verificarCondicao(conquista, usuarioId)
                
                if (novoProgresso != null) {
                    val foiDesbloqueada = novoProgresso >= conquista.condicao.valor
                    
                    // Atualizar progresso do usuário
                    gamificacaoRepository.atualizarProgressoConquista(
                        conquistaId = conquista.id,
                        usuarioId = usuarioId,
                        progressoAtual = novoProgresso,
                        desbloqueada = foiDesbloqueada
                    )
                    
                    // Se foi desbloqueada E não estava desbloqueada antes, adicionar XP e criar notificação
                    if (foiDesbloqueada && progressoAtual?.desbloqueada != true) {
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
    suspend fun verificarConquista(conquista: Conquista, usuarioId: String) {
        try {
            val progressoAtual = gamificacaoRepository.observarProgressoConquista(conquista.id, usuarioId).first()
            
            // Se já está desbloqueada, pular
            if (progressoAtual?.desbloqueada == true) {
                return
            }
            
            // Verificar condição
            val novoProgresso = verificarCondicao(conquista, usuarioId)
            
            if (novoProgresso != null) {
                val foiDesbloqueada = novoProgresso >= conquista.condicao.valor
                
                // Atualizar progresso do usuário
                gamificacaoRepository.atualizarProgressoConquista(
                    conquistaId = conquista.id,
                    usuarioId = usuarioId,
                    progressoAtual = novoProgresso,
                    desbloqueada = foiDesbloqueada
                )
                
                // Se foi desbloqueada, adicionar XP e criar notificação
                if (foiDesbloqueada) {
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
            TipoCondicao.ADICIONAR_MEMBROS -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.size
            }
            TipoCondicao.ADICIONAR_FOTOS -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.count { it.fotoUrl != null && it.fotoUrl.isNotEmpty() }
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
                    // Calcular gerações baseado na distância da Família Zero
                    val geracoes = pessoas.mapNotNull { it.distanciaFamiliaZero }.distinct()
                    geracoes.maxOrNull() ?: 0
                }
            }
            TipoCondicao.CRIAR_SUBFAMILIAS -> {
                val subfamilias = subfamiliaRepository.observarTodasSubfamilias().first()
                subfamilias.size
            }
            TipoCondicao.CRIAR_FAMILIA_ZERO -> {
                // Verificar se existe família zero
                val subfamilias = subfamiliaRepository.observarTodasSubfamilias().first()
                // Se há subfamílias, significa que há família zero
                if (subfamilias.isNotEmpty()) 1 else 0
            }
            TipoCondicao.ADICIONAR_MEMBROS_TOTAL -> {
                val pessoas = pessoaRepository.observarTodasPessoas().first()
                pessoas.size
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
                    val geracoes = pessoas.mapNotNull { it.distanciaFamiliaZero }.distinct()
                    geracoes.size
                }
            }
            TipoCondicao.DESCOBRIR_PARENTESCO_DISTANTE -> {
                // TODO: Implementar verificação de parentesco distante
                // Por enquanto, retorna 0
                0
            }
            TipoCondicao.VISUALIZAR_FLORESTA -> {
                // Esta conquista é verificada quando a tela da floresta é visualizada pela primeira vez
                // O valor é sempre 1 (já visualizou) ou 0 (nunca visualizou)
                // A verificação é feita no ArvoreViewModel
                1 // Se chegou aqui, já visualizou
            }
        }
    }
}

