package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Tipos de eventos familiares
 */
enum class TipoEventoFamilia(val icone: String, val descricao: String) {
    ANIVERSARIO("🎂", "Aniversário"),
    REUNIAO("👥", "Reunião"),
    BODAS("💍", "Bodas"),
    CASAMENTO("💒", "Casamento"),
    NASCIMENTO("👶", "Nascimento"),
    FORMATURA("🎓", "Formatura"),
    OUTRO("🎉", "Outro")
}

/**
 * Modelo representando um evento familiar
 */
data class EventoFamilia(
    val id: String = "",
    val tipo: TipoEventoFamilia,
    val titulo: String,
    val descricao: String? = null,
    val data: Date,
    val pessoaRelacionadaId: String? = null,  // ID da pessoa relacionada ao evento
    val pessoaRelacionadaNome: String? = null,
    val local: String? = null,
    val criadoPor: String = "",
    val criadoEm: Date = Date(),
    val participantes: List<String> = emptyList()  // IDs dos participantes
) {
    /**
     * Verifica se o evento já passou
     */
    val jaPassou: Boolean
        get() = Date().after(data)
    
    /**
     * Verifica se o evento é hoje
     */
    val ehHoje: Boolean
        get() {
            val hoje = java.util.Calendar.getInstance()
            val dataEvento = java.util.Calendar.getInstance().apply { time = data }
            
            return hoje.get(java.util.Calendar.YEAR) == dataEvento.get(java.util.Calendar.YEAR) &&
                   hoje.get(java.util.Calendar.DAY_OF_YEAR) == dataEvento.get(java.util.Calendar.DAY_OF_YEAR)
        }
    
    /**
     * Verifica se o evento é nos próximos 7 dias
     */
    val ehProximo: Boolean
        get() {
            val hoje = Date()
            val seteDiasDepois = Date(hoje.time + 7 * 24 * 60 * 60 * 1000L)
            return data.after(hoje) && data.before(seteDiasDepois)
        }
    
    /**
     * Retorna descrição formatada do evento
     */
    fun getDescricaoFormatada(): String {
        return when {
            pessoaRelacionadaNome != null -> "$titulo - $pessoaRelacionadaNome"
            else -> titulo
        }
    }
}
