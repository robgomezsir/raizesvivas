package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Filtros para busca avançada de pessoas
 */
data class PessoaFilter(
    val termoBusca: String = "",
    val genero: Genero? = null,
    val localNascimento: String? = null,
    val dataNascimentoInicio: Date? = null,
    val dataNascimentoFim: Date? = null,
    val apenasVivos: Boolean = false
) {
    fun hasFilters(): Boolean {
        return termoBusca.isNotBlank() || 
               genero != null || 
               !localNascimento.isNullOrBlank() || 
               dataNascimentoInicio != null || 
               dataNascimentoFim != null ||
               apenasVivos
    }
}
