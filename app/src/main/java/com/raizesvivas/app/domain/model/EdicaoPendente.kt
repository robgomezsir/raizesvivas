package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Status de uma edição pendente
 */
enum class StatusEdicao {
    PENDENTE,
    APROVADA,
    REJEITADA
}

/**
 * Modelo representando uma edição aguardando aprovação
 * 
 * Quando um usuário não-admin edita uma pessoa, a edição
 * fica pendente até ser aprovada por um administrador.
 */
data class EdicaoPendente(
    val id: String = "",
    val pessoaId: String = "",            // ID da pessoa sendo editada
    val camposAlterados: Map<String, Any> = emptyMap(), // Campos que foram modificados
    val editadoPor: String = "",          // UserID de quem editou
    val status: StatusEdicao = StatusEdicao.PENDENTE,
    val criadoEm: Date = Date(),
    val revisadoEm: Date? = null,
    val revisadoPor: String? = null       // UserID do admin que revisou
) {
    /**
     * Verifica se ainda está aguardando aprovação
     */
    val estaPendente: Boolean
        get() = status == StatusEdicao.PENDENTE
}

