package com.raizesvivas.core.domain.model

import java.time.LocalDateTime

/**
 * Modelo de conquista do usuário
 * 
 * Representa uma conquista conquistada por um usuário
 * no sistema de gamificação.
 */
data class UserAchievement(
    val id: String,
    val userId: String,
    val conquistaId: String,
    val dataConquista: LocalDateTime,
    val pontosGanhos: Int = 0,
    val ativa: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val achievement: Achievement? = null
)
