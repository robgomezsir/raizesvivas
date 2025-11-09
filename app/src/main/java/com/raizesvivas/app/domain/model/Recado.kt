package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Modelo representando um recado no mural comunitário
 * 
 * Recados podem ser gerais (para todos) ou direcionados a uma pessoa específica.
 * Todos os familiares podem criar e visualizar recados.
 */
data class Recado(
    val id: String = "",
    val autorId: String = "",              // UserID de quem criou o recado
    val autorNome: String = "",            // Nome do autor (para exibição rápida)
    val destinatarioId: String? = null,    // ID da pessoa destinatária (null = recado geral)
    val destinatarioNome: String? = null,  // Nome do destinatário (para exibição rápida)
    val titulo: String = "",               // Título do recado
    val mensagem: String = "",             // Conteúdo do recado
    val cor: String = "primary",           // Cor do card (primary, secondary, tertiary, etc)
    val criadoEm: Date = Date(),
    val atualizadoEm: Date = Date(),
    val deletado: Boolean = false,         // Soft delete
    val fixado: Boolean = false,           // Se o recado está fixado (não expira)
    val fixadoAte: Date? = null,           // Data até quando está fixado (null = fixado permanentemente)
    val fixadoPor: String? = null,          // UserID do admin que fixou o recado
    val apoiosFamiliares: List<String> = emptyList() // Lista de UserIDs que deram apoio familiar (curtidas)
) {
    /**
     * Verifica se é um recado geral (não direcionado)
     */
    val ehGeral: Boolean
        get() = destinatarioId == null
    
    /**
     * Verifica se é um recado direcionado
     */
    val ehDirecionado: Boolean
        get() = destinatarioId != null
    
    /**
     * Verifica se o recado está expirado (mais de 24h desde criação e não está fixado)
     */
    fun estaExpirado(): Boolean {
        if (fixado) {
            // Se está fixado, verificar se passou da data de fixação
            if (fixadoAte != null) {
                return Date().after(fixadoAte)
            }
            // Fixado permanentemente, nunca expira
            return false
        }
        
        // Não fixado: expira após 24h
        val agora = Date()
        val diferenca = agora.time - criadoEm.time
        val horas24 = 24 * 60 * 60 * 1000L // 24 horas em milissegundos
        return diferenca > horas24
    }
    
    /**
     * Verifica se o recado está fixado e ainda válido
     */
    fun estaFixadoEValido(): Boolean {
        if (!fixado) return false
        return fixadoAte == null || !Date().after(fixadoAte)
    }
    
    /**
     * Retorna a quantidade de apoios familiares (curtidas)
     */
    val totalApoios: Int
        get() = apoiosFamiliares.size
    
    /**
     * Verifica se um usuário específico deu apoio familiar
     */
    fun usuarioDeuApoio(userId: String?): Boolean {
        if (userId == null) return false
        return apoiosFamiliares.contains(userId)
    }
}

