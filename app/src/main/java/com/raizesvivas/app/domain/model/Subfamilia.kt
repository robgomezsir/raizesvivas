package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo representando uma Subfamília
 * 
 * Subfamílias são ramificações criadas quando membros formam novos núcleos familiares.
 * Derivam da Família Zero ou de outras subfamílias.
 */
data class Subfamilia(
    val id: String = "",
    val nome: String = "",
    val tipo: TipoFamilia = TipoFamilia.SUBFAMILIA,
    val familiaPaiId: String = "", // ID da família pai (Família Zero ou outra subfamília)
    val membroOrigem1Id: String = "", // ID do primeiro membro fundador
    val membroOrigem2Id: String = "", // ID do segundo membro fundador (cônjuge)
    val nivelHierarquico: Int = 1, // Nível na hierarquia (1 = primeira subfamília da Família Zero)
    val criadoEm: Date = Date(),
    val criadoPor: String = "", // UserID de quem criou
    val descricao: String? = null, // Descrição opcional da subfamília
    val ativa: Boolean = true // Se a subfamília está ativa ou foi arquivada
) {
    /**
     * Verifica se a subfamília está válida
     */
    val estaValida: Boolean
        get() = nome.isNotBlank() && 
                membroOrigem1Id.isNotBlank() && 
                membroOrigem2Id.isNotBlank() &&
                familiaPaiId.isNotBlank()
}

/**
 * Enum para tipos de família
 */
enum class TipoFamilia {
    ZERO,      // Família Zero (raiz)
    SUBFAMILIA // Subfamília derivada
}
