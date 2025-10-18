package com.raizesvivas.core.domain.model

import java.time.LocalDateTime

/**
 * Modelo de família do domínio
 * 
 * Representa uma família no sistema Raízes Vivas.
 * Pode ser uma família-zero (raiz da árvore) ou uma subfamília.
 */
data class Family(
    val id: String,
    val nome: String,
    val tipo: FamilyType,
    val familiaPaiId: String? = null,
    val criadaPorCasamento: Boolean = false,
    val membroOrigem1Id: String? = null,
    val membroOrigem2Id: String? = null,
    val dataCriacao: LocalDateTime,
    val iconeArvore: String? = null,
    val nivelHierarquico: Int = 0,
    val ativa: Boolean = true,
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Tipos de família
 */
enum class FamilyType(val value: String) {
    ZERO("zero"),
    SUBFAMILIA("subfamilia");
    
    companion object {
        fun fromValue(value: String): FamilyType {
            return values().find { it.value == value } 
                ?: throw IllegalArgumentException("Tipo de família inválido: $value")
        }
    }
}
