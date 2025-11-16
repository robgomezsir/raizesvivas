package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo de domínio representando um amigo da família
 */
data class Amigo(
    val id: String,
    val nome: String,
    val telefone: String?,
    val familiaresVinculados: List<String>, // IDs das pessoas vinculadas
    val criadoPor: String,
    val criadoEm: Date,
    val modificadoEm: Date
) {
    fun getNomeExibicao(): String = nome
}

