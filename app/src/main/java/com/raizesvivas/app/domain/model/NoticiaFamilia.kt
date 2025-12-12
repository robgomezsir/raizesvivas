package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Tipos de notícias/atividades na família
 */
enum class TipoNoticiaFamilia(val icone: String, val descricao: String) {
    NOVA_PESSOA("👤", "Nova pessoa"),
    NOVA_FOTO("📸", "Nova foto"),
    NOVO_COMENTARIO("💬", "Novo comentário"),
    APOIO_FAMILIAR("❤️", "Apoio familiar"),
    NOVO_RECADO("📌", "Novo recado"),
    ANIVERSARIO_HOJE("🎂", "Aniversário hoje"),
    NOVA_SUBFAMILIA("👨‍👩‍👧‍👦", "Nova subfamília"),
    EDICAO_APROVADA("✅", "Edição aprovada"),
    CONQUISTA_DESBLOQUEADA("🏆", "Conquista desbloqueada"),
    MEMBRO_VINCULADO("🔗", "Membro vinculado"),
    CASAMENTO("💒", "Casamento"),
    NASCIMENTO("👶", "Nascimento"),
    FALECIMENTO("🕊️", "Falecimento")
}

/**
 * Modelo representando uma notícia/atividade recente na família
 */
data class NoticiaFamilia(
    val id: String = "",
    val tipo: TipoNoticiaFamilia,
    val titulo: String,                          // Ex: "Vanildo adicionou uma foto"
    val descricao: String? = null,               // Ex: "ao álbum da Família Gomes"
    val autorId: String,                         // UserID de quem gerou a notícia
    val autorNome: String,                       // Nome do autor
    val pessoaRelacionadaId: String? = null,     // ID da pessoa relacionada
    val pessoaRelacionadaNome: String? = null,   // Nome da pessoa relacionada
    val recursoId: String? = null,               // ID do recurso (foto, recado, etc)
    val criadoEm: Date = Date(),
    val lida: Boolean = false                    // Se a notícia foi visualizada
) {
    /**
     * Verifica se a notícia é de hoje
     */
    val ehHoje: Boolean
        get() {
            val hoje = java.util.Calendar.getInstance()
            val dataNot = java.util.Calendar.getInstance().apply { time = criadoEm }
            
            return hoje.get(java.util.Calendar.YEAR) == dataNot.get(java.util.Calendar.YEAR) &&
                   hoje.get(java.util.Calendar.DAY_OF_YEAR) == dataNot.get(java.util.Calendar.DAY_OF_YEAR)
        }
    
    /**
     * Verifica se a notícia é recente (últimas 24h)
     */
    val ehRecente: Boolean
        get() {
            val agora = Date()
            val diferenca = agora.time - criadoEm.time
            val horas24 = 24 * 60 * 60 * 1000L
            return diferenca < horas24
        }
    
    /**
     * Retorna texto formatado da notícia
     */
    fun getTextoCompleto(): String {
        return if (descricao != null) {
            "$titulo $descricao"
        } else {
            titulo
        }
    }
    
    /**
     * Retorna tempo relativo (ex: "há 2 horas")
     */
    fun getTempoRelativo(): String {
        val agora = Date()
        val diferenca = agora.time - criadoEm.time
        
        val minutos = diferenca / (60 * 1000)
        val horas = diferenca / (60 * 60 * 1000)
        val dias = diferenca / (24 * 60 * 60 * 1000)
        
        return when {
            minutos < 1 -> "agora"
            minutos < 60 -> "há ${minutos}min"
            horas < 24 -> "há ${horas}h"
            dias < 7 -> "há ${dias}d"
            else -> {
                val calendar = java.util.Calendar.getInstance().apply { time = criadoEm }
                val dia = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                val mes = calendar.get(java.util.Calendar.MONTH) + 1
                "$dia/$mes"
            }
        }
    }
}
