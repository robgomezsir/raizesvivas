package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo representando a Família Zero (raiz da árvore)
 * 
 * A Família Zero é o casal patriarca/matriarca que serve
 * como raiz de toda a árvore genealógica. Só pode existir
 * UMA Família Zero por árvore.
 */
data class FamiliaZero(
    val id: String = "raiz",              // Sempre "raiz" (singleton)
    val pai: String = "",                 // ID do patriarca
    val mae: String = "",                 // ID da matriarca
    val fundadoPor: String = "",          // UserID de quem fundou
    val fundadoEm: Date = Date(),
    val locked: Boolean = true,           // Sempre true - impede deleção
    val arvoreNome: String = ""           // Ex: "Família Silva"
) {
    /**
     * Verifica se a Família Zero está válida
     */
    val estaValida: Boolean
        get() = pai.isNotBlank() && mae.isNotBlank()
    
    /**
     * Retorna nome da árvore para exibição
     */
    fun getNomeArvore(): String {
        return if (arvoreNome.isNotBlank()) arvoreNome else "Árvore Genealógica"
    }
}

