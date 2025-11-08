package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.NotificacaoRepository
import com.raizesvivas.app.domain.model.Notificacao
import com.raizesvivas.app.domain.model.SugestaoSubfamilia
import com.raizesvivas.app.domain.model.TipoNotificacao
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * UseCase para criar notificações
 */
class CriarNotificacaoUseCase @Inject constructor(
    private val notificacaoRepository: NotificacaoRepository
) {
    
    /**
     * Cria uma notificação genérica
     */
    suspend fun executar(
        tipo: TipoNotificacao,
        titulo: String,
        mensagem: String,
        relacionadoId: String? = null,
        dadosExtras: Map<String, String> = emptyMap()
    ) {
        val notificacao = Notificacao(
            id = UUID.randomUUID().toString(),
            tipo = tipo,
            titulo = titulo,
            mensagem = mensagem,
            lida = false,
            criadaEm = Date(),
            relacionadoId = relacionadoId,
            dadosExtras = dadosExtras
        )
        
        notificacaoRepository.criarNotificacao(notificacao)
    }
    
    /**
     * Cria notificação para sugestão de subfamília
     */
    suspend fun criarNotificacaoSugestaoSubfamilia(sugestao: SugestaoSubfamilia) {
        executar(
            tipo = TipoNotificacao.SUGESTAO_SUBFAMILIA,
            titulo = "Nova Sugestão de Subfamília",
            mensagem = "O sistema detectou que ${sugestao.nomeSugerido} pode formar uma nova subfamília com ${sugestao.membrosIncluidos.size} membros.",
            relacionadoId = sugestao.id,
            dadosExtras = mapOf(
                "nomeSugerido" to sugestao.nomeSugerido,
                "quantidadeMembros" to sugestao.membrosIncluidos.size.toString()
            )
        )
    }
    
    /**
     * Cria múltiplas notificações para sugestões de subfamílias
     */
    suspend fun criarNotificacoesSugestoesSubfamilias(sugestoes: List<SugestaoSubfamilia>) {
        sugestoes.forEach { sugestao ->
            criarNotificacaoSugestaoSubfamilia(sugestao)
        }
    }
}
