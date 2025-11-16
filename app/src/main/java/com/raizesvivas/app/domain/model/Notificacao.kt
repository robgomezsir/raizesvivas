package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo de Notificação
 * 
 * Representa uma notificação do sistema para o usuário
 */
data class Notificacao(
    val id: String,
    val tipo: TipoNotificacao,
    val titulo: String,
    val mensagem: String,
    val lida: Boolean = false,
    val criadaEm: Date = Date(),
    val relacionadoId: String? = null, // ID relacionado (ex: sugestão de subfamília)
    val dadosExtras: Map<String, String> = emptyMap()
)

/**
 * Tipos de notificações disponíveis
 */
enum class TipoNotificacao(val descricao: String) {
    SUGESTAO_SUBFAMILIA("Sugestão de Subfamília"),
    PARENTESCO_ATUALIZADO("Parentesco Atualizado"),
    MEMBRO_ADICIONADO("Novo Membro Adicionado"),
    CONVITE_RECEBIDO("Convite Recebido"),
    EDICAO_PENDENTE("Edição Pendente"),
    CONQUISTA_DESBLOQUEADA("Conquista Desbloqueada"),
    ANIVERSARIO("Aniversário"),
    ADMIN_MENSAGEM("Mensagem do Administrador"),
    OUTRO("Outro")
}
