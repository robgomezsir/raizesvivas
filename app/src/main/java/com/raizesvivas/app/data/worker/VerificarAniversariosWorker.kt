package com.raizesvivas.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raizesvivas.app.data.repository.NotificacaoRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Notificacao
import com.raizesvivas.app.domain.model.TipoNotificacao
import com.raizesvivas.app.utils.AniversarioPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Worker para verificar aniversários diariamente e criar notificações
 * Executa uma vez por dia para verificar quem faz aniversário
 */
@HiltWorker
class VerificarAniversariosWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val pessoaRepository: PessoaRepository,
    private val notificacaoRepository: NotificacaoRepository,
    private val usuarioRepository: UsuarioRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("🎂 Iniciando verificação de aniversários...")
            
            // Verificar se notificações estão habilitadas
            val notificacoesHabilitadas = AniversarioPreferences.notificacoesHabilitadas(applicationContext)
            if (!notificacoesHabilitadas) {
                Timber.d("⏭️ Notificações de aniversário desabilitadas, pulando verificação")
                return Result.success()
            }
            
            val notificarAniversariante = AniversarioPreferences.notificarAniversariante(applicationContext)
            val notificarFamiliares = AniversarioPreferences.notificarFamiliares(applicationContext)
            
            // Buscar todas as pessoas
            val todasPessoas = pessoaRepository.buscarTodas()
            val hoje = Calendar.getInstance()
            val diaHoje = hoje.get(Calendar.DAY_OF_MONTH)
            val mesHoje = hoje.get(Calendar.MONTH)
            
            val aniversariantes = todasPessoas.filter { pessoa ->
                pessoa.dataNascimento?.let { dataNasc ->
                    val calNasc = Calendar.getInstance().apply {
                        time = dataNasc
                    }
                    val diaNasc = calNasc.get(Calendar.DAY_OF_MONTH)
                    val mesNasc = calNasc.get(Calendar.MONTH)
                    
                    // Verificar se é aniversário hoje
                    diaNasc == diaHoje && mesNasc == mesHoje
                } ?: false
            }
            
            Timber.d("🎂 Encontrados ${aniversariantes.size} aniversariantes hoje")
            
            if (aniversariantes.isEmpty()) {
                Timber.d("✅ Nenhum aniversário hoje")
                return Result.success()
            }
            
            // Buscar todos os usuários para notificar familiares
            val todosUsuarios = if (notificarFamiliares) {
                usuarioRepository.buscarTodosUsuarios().getOrNull() ?: emptyList()
            } else {
                emptyList()
            }
            
            var notificacoesCriadas = 0
            
            // Criar notificações para cada aniversariante
            aniversariantes.forEach { aniversariante ->
                val idade = aniversariante.calcularIdade()
                val nomeExibicao = aniversariante.getNomeExibicao()
                
                // Notificar o aniversariante (se habilitado)
                // Buscar usuário vinculado ao aniversariante
                if (notificarAniversariante) {
                    // Buscar usuário que tem esta pessoa vinculada
                    val usuarioAniversariante = todosUsuarios.find { usuario ->
                        usuario.pessoaVinculada == aniversariante.id ||
                        aniversariante.criadoPor == usuario.id ||
                        aniversariante.modificadoPor == usuario.id
                    }
                    
                    // Se encontrou usuário vinculado, criar notificação para ele
                    // Caso contrário, criar notificação genérica (será exibida para todos que têm acesso)
                    val mensagemAniversariante = when {
                        idade != null -> "Parabéns, $nomeExibicao! 🎉 Hoje você completa $idade anos! Que este dia seja repleto de alegria e felicidade!"
                        else -> "Parabéns, $nomeExibicao! 🎉 Que este dia seja especial e repleto de alegria!"
                    }
                    
                    val notificacaoAniversariante = Notificacao(
                        id = UUID.randomUUID().toString(),
                        tipo = TipoNotificacao.ANIVERSARIO,
                        titulo = "🎉 Feliz Aniversário!",
                        mensagem = mensagemAniversariante,
                        lida = false,
                        criadaEm = Date(),
                        relacionadoId = aniversariante.id,
                        dadosExtras = mapOf(
                            "pessoaId" to aniversariante.id,
                            "usuarioId" to (usuarioAniversariante?.id ?: ""),
                            "idade" to (idade?.toString() ?: ""),
                            "tipo" to "aniversariante"
                        )
                    )
                    
                    notificacaoRepository.criarNotificacao(notificacaoAniversariante)
                    notificacoesCriadas++
                    Timber.d("✅ Notificação criada para aniversariante: $nomeExibicao${usuarioAniversariante?.let { " (usuário: ${it.nome})" } ?: ""}")
                }
                
                // Notificar familiares (se habilitado)
                if (notificarFamiliares && todosUsuarios.isNotEmpty()) {
                    // Buscar pessoas vinculadas aos usuários
                    val pessoasUsuarios = todasPessoas.filter { pessoa ->
                        todosUsuarios.any { usuario ->
                            usuario.pessoaVinculada == pessoa.id || 
                            pessoa.criadoPor == usuario.id || 
                            pessoa.modificadoPor == usuario.id
                        }
                    }
                    
                    pessoasUsuarios.forEach { pessoaUsuario ->
                        // Verificar se a pessoa do usuário tem relacionamento com o aniversariante
                        val temRelacionamento = verificarRelacionamento(aniversariante, pessoaUsuario)
                        
                        if (temRelacionamento) {
                            // Encontrar o usuário correspondente
                            val usuario = todosUsuarios.find { 
                                it.pessoaVinculada == pessoaUsuario.id || 
                                pessoaUsuario.criadoPor == it.id || 
                                pessoaUsuario.modificadoPor == it.id
                            }
                            
                            if (usuario != null) {
                                val mensagemFamiliares = when {
                                    idade != null -> "Hoje é aniversário de $nomeExibicao! Ela(e) completa $idade anos. 🎂"
                                    else -> "Hoje é aniversário de $nomeExibicao! 🎂"
                                }
                                
                                val notificacaoFamiliar = Notificacao(
                                    id = UUID.randomUUID().toString(),
                                    tipo = TipoNotificacao.ANIVERSARIO,
                                    titulo = "🎂 Aniversário de Familiar",
                                    mensagem = mensagemFamiliares,
                                    lida = false,
                                    criadaEm = Date(),
                                    relacionadoId = aniversariante.id,
                                    dadosExtras = mapOf(
                                        "pessoaId" to aniversariante.id,
                                        "usuarioId" to usuario.id,
                                        "idade" to (idade?.toString() ?: ""),
                                        "tipo" to "familiar"
                                    )
                                )
                                
                                notificacaoRepository.criarNotificacao(notificacaoFamiliar)
                                notificacoesCriadas++
                                Timber.d("✅ Notificação criada para familiar (${usuario.nome}) sobre: $nomeExibicao")
                            }
                        }
                    }
                }
            }
            
            Timber.d("✅ Verificação de aniversários concluída: $notificacoesCriadas notificações criadas")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao verificar aniversários")
            Result.retry()
        }
    }
    
    /**
     * Verifica se a pessoa do usuário tem relacionamento com o aniversariante
     * Considera: pai, mãe, filhos, cônjuge, irmãos
     */
    private fun verificarRelacionamento(
        aniversariante: com.raizesvivas.app.domain.model.Pessoa,
        pessoaUsuario: com.raizesvivas.app.domain.model.Pessoa
    ): Boolean {
        // Não notificar se for a mesma pessoa
        if (aniversariante.id == pessoaUsuario.id) {
            return false
        }
        
        // Verificar relacionamentos diretos
        return when {
            // É pai ou mãe
            aniversariante.pai == pessoaUsuario.id || aniversariante.mae == pessoaUsuario.id -> true
            // É filho
            pessoaUsuario.pai == aniversariante.id || pessoaUsuario.mae == aniversariante.id -> true
            // É cônjuge
            aniversariante.conjugeAtual == pessoaUsuario.id || pessoaUsuario.conjugeAtual == aniversariante.id -> true
            // É irmão (mesmo pai ou mesma mãe)
            (aniversariante.pai != null && pessoaUsuario.pai != null && aniversariante.pai == pessoaUsuario.pai) ||
            (aniversariante.mae != null && pessoaUsuario.mae != null && aniversariante.mae == pessoaUsuario.mae) -> true
            // Está na lista de filhos
            pessoaUsuario.filhos.contains(aniversariante.id) || aniversariante.filhos.contains(pessoaUsuario.id) -> true
            else -> false
        }
    }
}

