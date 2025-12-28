package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo representando um log de auditoria de ações de usuários
 */
data class AuditLog(
    val id: String = "",
    val usuarioId: String = "",           // ID do usuário que executou a ação
    val usuarioNome: String = "",         // Nome do usuário
    val usuarioEmail: String = "",        // Email do usuário
    val acao: TipoAcaoAudit = TipoAcaoAudit.OUTRO,
    val entidade: String = "",            // Tipo de entidade (Pessoa, Familia, Usuario, etc)
    val entidadeId: String = "",          // ID da entidade afetada
    val entidadeNome: String = "",        // Nome/descrição da entidade
    val detalhes: String = "",            // Detalhes adicionais da ação
    val timestamp: Date = Date(),
    val ipAddress: String? = null,        // Endereço IP (opcional)
    val deviceInfo: String? = null        // Informações do dispositivo (opcional)
)

/**
 * Tipos de ações de auditoria
 */
enum class TipoAcaoAudit {
    CRIAR,          // Criação de registro
    EDITAR,         // Edição de registro
    EXCLUIR,        // Exclusão de registro
    RESTAURAR,      // Restauração de registro excluído
    APROVAR,        // Aprovação de edição pendente
    REJEITAR,       // Rejeição de edição pendente
    LOGIN,          // Login no sistema
    LOGOUT,         // Logout do sistema
    OUTRO           // Outras ações
}
