package com.raizesvivas.app.domain.model

/**
 * Modelo de dados para ranking de usuários por conquistas
 */
data class RankingUsuario(
    val usuarioId: String,
    val nome: String,
    val fotoUrl: String?,
    val xpTotal: Int,
    val nivel: Int,
    val conquistasDesbloqueadas: Int,
    val posicao: Int
)

