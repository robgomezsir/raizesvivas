package com.raizesvivas.app.domain.model

/**
 * Modelo de Conquista Disponível (Pública)
 * 
 * Representa uma conquista que está disponível para todos os usuários.
 * Armazenada em: conquistasDisponiveis/{conquistaId}
 */
data class ConquistaDisponivel(
    val id: String,
    val titulo: String,
    val descricao: String,
    val icone: String,
    val categoria: String,
    val criterio: Int, // Quantas ações necessárias
    val pontosRecompensa: Int
)

