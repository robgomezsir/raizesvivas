package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Status possíveis de um convite
 */
enum class StatusConvite {
    PENDENTE,
    ACEITO,
    REJEITADO,
    EXPIRADO
}

/**
 * Modelo representando um convite para entrar na árvore
 * 
 * Apenas administradores podem criar convites.
 * Convites expiram em 7 dias.
 */
data class Convite(
    val id: String = "",
    val emailConvidado: String = "",
    val convidadoPor: String = "",        // UserID de quem convidou
    val pessoaVinculada: String? = null,  // ID da pessoa que o convidado representa (opcional)
    val status: StatusConvite = StatusConvite.PENDENTE,
    val criadoEm: Date = Date(),
    val expiraEm: Date = Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)) // 7 dias
) {
    /**
     * Verifica se o convite ainda está válido
     */
    val estaValido: Boolean
        get() = status == StatusConvite.PENDENTE && Date().before(expiraEm)
    
    /**
     * Verifica se o convite expirou
     */
    val expirou: Boolean
        get() = Date().after(expiraEm)
}

