package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo de domínio representando uma mensagem do chat
 */
data class MensagemChat(
    val id: String,
    val remetenteId: String,
    val remetenteNome: String,
    val destinatarioId: String,
    val destinatarioNome: String,
    val texto: String,
    val enviadoEm: Date = Date(),
    val lida: Boolean = false
) {
    /**
     * Verifica se a mensagem foi enviada pelo usuário atual
     */
    fun ehEnviadaPor(usuarioId: String): Boolean {
        return remetenteId == usuarioId
    }
}
