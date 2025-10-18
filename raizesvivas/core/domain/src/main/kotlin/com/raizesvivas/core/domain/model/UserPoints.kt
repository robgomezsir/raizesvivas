package com.raizesvivas.core.domain.model

import java.time.LocalDateTime

/**
 * Modelo de pontos do usuário
 * 
 * Representa os pontos e nível de um usuário
 * no sistema de gamificação.
 */
data class UserPoints(
    val id: String,
    val userId: String,
    val pontosTotais: Int = 0,
    val nivelAtual: Int = 1,
    val pontosProximoNivel: Int = 100,
    val conquistasConquistadas: Int = 0,
    val ultimaAtualizacao: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    /**
     * Calcula o progresso para o próximo nível
     */
    fun getProgressToNextLevel(): Float {
        val pontosNecessarios = pontosProximoNivel - getPontosNivelAtual()
        val pontosRestantes = pontosProximoNivel - pontosTotais
        
        return if (pontosNecessarios > 0) {
            (pontosNecessarios - pontosRestantes).toFloat() / pontosNecessarios
        } else {
            1f
        }
    }
    
    /**
     * Obtém os pontos do nível atual
     */
    private fun getPontosNivelAtual(): Int {
        return (nivelAtual - 1) * 100
    }
    
    /**
     * Verifica se o usuário subiu de nível
     */
    fun hasLeveledUp(newPoints: Int): Boolean {
        return newPoints >= pontosProximoNivel
    }
    
    /**
     * Calcula o novo nível baseado nos pontos
     */
    fun calculateNewLevel(points: Int): Int {
        return (points / 100) + 1
    }
    
    /**
     * Calcula os pontos necessários para o próximo nível
     */
    fun calculatePointsForNextLevel(level: Int): Int {
        return level * 100
    }
}
