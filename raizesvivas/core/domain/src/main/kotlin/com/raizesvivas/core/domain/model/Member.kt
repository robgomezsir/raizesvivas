package com.raizesvivas.core.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de membro do domínio
 * 
 * Representa um membro da família no sistema Raízes Vivas.
 * Contém todas as informações pessoais e elementos visuais.
 */
data class Member(
    val id: String,
    val nomeCompleto: String,
    val nomeAbreviado: String? = null,
    val dataNascimento: LocalDate? = null,
    val dataFalecimento: LocalDate? = null,
    val localNascimento: String? = null,
    val localFalecimento: String? = null,
    val profissao: String? = null,
    val observacoes: String? = null,
    val fotoUrl: String? = null,
    val elementosVisuais: List<TreeElement> = emptyList(),
    val nivelNaArvore: Int = 0,
    val posicaoX: Float? = null,
    val posicaoY: Float? = null,
    val ativo: Boolean = true,
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * Elementos visuais da árvore genealógica
 */
enum class TreeElement(val value: String, val description: String) {
    ROOT("raiz", "Raiz da árvore"),
    TRUNK("tronco", "Tronco principal"),
    BRANCH("galho", "Galho da árvore"),
    LEAF("folha", "Folha"),
    FLOWER("flor", "Flor"),
    POLLINATOR("polinizador", "Polinizador"),
    BIRD("passaro", "Pássaro");
    
    companion object {
        fun fromValue(value: String): TreeElement {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Elemento visual inválido: $value")
        }
    }
}
