package com.raizesvivas.app.domain.model

/**
 * Tipos de núcleos familiares
 * Define diferentes formas de agrupamento familiar
 */
enum class TipoNucleoFamiliar(val label: String) {
    PARENTESCO("Parentesco"),      // Família por parentesco (casal + filhos)
    RESIDENCIAL("Residencial"),     // Pessoas que vivem juntas
    EMOCIONAL("Emocional"),         // Relacionamento próximo (amigos próximos, etc)
    ADOTIVA("Adotiva"),             // Família adotiva
    RECONSTITUIDA("Reconstituída")  // Família reconstituída (casamento anterior)
}

