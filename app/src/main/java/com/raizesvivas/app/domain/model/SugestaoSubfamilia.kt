package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo representando uma sugestão de criação de subfamília
 * 
 * Criada automaticamente pelo sistema quando detecta um casal que pode formar uma nova família
 */
data class SugestaoSubfamilia(
    val id: String = "",
    val membro1Id: String = "", // ID do primeiro membro do casal
    val membro2Id: String = "", // ID do segundo membro do casal (cônjuge)
    val nomeSugerido: String = "", // Nome sugerido (ex: "Família Silva-Santos")
    val membrosIncluidos: List<String> = emptyList(), // IDs de todos os membros que seriam incluídos
    val status: StatusSugestao = StatusSugestao.PENDENTE,
    val criadoEm: Date = Date(),
    val processadoEm: Date? = null, // Data em que foi aceita ou rejeitada
    val usuarioId: String = "", // UserID que recebeu a sugestão
    val familiaZeroId: String = "" // ID da Família Zero relacionada
) {
    /**
     * Verifica se a sugestão está pendente
     */
    val estaPendente: Boolean
        get() = status == StatusSugestao.PENDENTE
    
    /**
     * Verifica se a sugestão foi aceita
     */
    val foiAceita: Boolean
        get() = status == StatusSugestao.ACEITA
}

/**
 * Enum para status de sugestão
 */
enum class StatusSugestao {
    PENDENTE,  // Aguardando decisão do usuário
    ACEITA,    // Aceita, subfamília criada
    REJEITADA, // Rejeitada pelo usuário (nunca sugerir novamente)
    EXPIRADA   // Expirada (não aceita após X tempo)
}
