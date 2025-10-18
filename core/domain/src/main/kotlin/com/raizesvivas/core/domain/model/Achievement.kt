package com.raizesvivas.core.domain.model

import java.time.LocalDateTime

/**
 * Modelo de conquista do domínio
 * 
 * Representa uma conquista no sistema de gamificação
 * do Raízes Vivas.
 */
data class Achievement(
    val id: String,
    val nome: String,
    val descricao: String,
    val tipoConquista: AchievementType,
    val icone: String? = null,
    val pontosConquista: Int = 0,
    val requisitos: Map<String, Any> = emptyMap(),
    val ativa: Boolean = true,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Tipos de conquista
 */
enum class AchievementType(val value: String, val description: String) {
    MEMBRO_ADICIONADO("membro_adicionado", "Membro Adicionado"),
    RELACIONAMENTO_CRIADO("relacionamento_criado", "Relacionamento Criado"),
    ARVORE_COMPLETA("arvore_completa", "Árvore Completa"),
    SUBFAMILIA_CRIADA("subfamilia_criada", "Subfamília Criada"),
    PARENTESCO_CALCULADO("parentesco_calculado", "Parentesco Calculado"),
    FOTO_ADICIONADA("foto_adicionada", "Foto Adicionada"),
    OBSERVACAO_ADICIONADA("observacao_adicionada", "Observação Adicionada"),
    ARVORE_VISUALIZADA("arvore_visualizada", "Árvore Visualizada"),
    FAMILIA_COMPARTILHADA("familia_compartilhada", "Família Compartilhada");
    
    companion object {
        fun fromValue(value: String): AchievementType {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Tipo de conquista inválido: $value")
        }
    }
}
