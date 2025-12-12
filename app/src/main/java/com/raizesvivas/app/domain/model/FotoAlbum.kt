package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Tipos de emoção/apoio disponíveis para fotos
 */
enum class TipoApoioFoto(val emoji: String, val nome: String) {
    CORACAO("❤️", "Coração"),
    TRISTE("😢", "Triste"),
    GARGALHADA("😂", "Gargalhada"),
    COMEMORACAO("🎉", "Comemoração"),
    RAIVA("😠", "Raiva");
    
    companion object {
        fun fromString(value: String): TipoApoioFoto? {
            return values().find { it.name == value }
        }
    }
}

/**
 * Modelo representando um apoio dado por um usuário
 */
data class ApoioFoto(
    val usuarioId: String, // UserID de quem deu o apoio
    val tipo: TipoApoioFoto,
    val data: Date = Date()
)

/**
 * Modelo representando um comentário em uma foto do álbum
 */
data class ComentarioFoto(
    val id: String = "",
    val fotoId: String = "", // ID da foto
    val usuarioId: String = "", // UserID de quem comentou
    val usuarioNome: String = "", // Nome do usuário (para exibição rápida)
    val usuarioApelido: String? = null, // Apelido do usuário (para exibição preferencial)
    val usuarioFotoUrl: String? = null, // Foto do perfil do usuário
    val texto: String = "",
    val criadoEm: Date = Date(),
    val deletado: Boolean = false // Soft delete
) {
    /**
     * Valida se o comentário está completo e válido
     */
    fun validar(): Boolean {
        return texto.isNotBlank() && texto.length >= 1 && texto.length <= 500
    }
}

/**
 * Modelo representando uma foto no álbum de família
 */
data class FotoAlbum(
    val id: String = "",
    val familiaId: String = "", // ID da família
    val pessoaId: String = "",
    val pessoaNome: String = "",
    val url: String = "",
    val descricao: String = "",
    val criadoPor: String = "", // UserID de quem fez upload
    val criadoEm: Date = Date(),
    val ordem: Int = 0, // Ordem de exibição
    val apoios: Map<String, TipoApoioFoto> = emptyMap() // Map<userId, tipoApoio>
) {
    /**
     * Retorna o total de apoios
     */
    val totalApoios: Int
        get() = apoios.size
    
    /**
     * Retorna a quantidade de cada tipo de apoio
     */
    fun contarApoiosPorTipo(tipo: TipoApoioFoto): Int {
        return apoios.values.count { it == tipo }
    }
    
    /**
     * Verifica se um usuário específico deu apoio
     */
    fun usuarioDeuApoio(userId: String?): Boolean {
        if (userId == null) return false
        return apoios.containsKey(userId)
    }
    
    /**
     * Retorna o tipo de apoio dado por um usuário específico
     */
    fun obterApoioUsuario(userId: String?): TipoApoioFoto? {
        if (userId == null) return null
        return apoios[userId]
    }
    
    /**
     * Retorna lista de user IDs que reagiram com um tipo específico de apoio
     */
    fun obterUsuariosPorTipo(tipo: TipoApoioFoto): List<String> {
        return apoios.filter { it.value == tipo }.keys.toList()
    }
    
    /**
     * Retorna mapa agrupando user IDs por tipo de apoio
     */
    fun obterApoiosAgrupados(): Map<TipoApoioFoto, List<String>> {
        return apoios.entries
            .groupBy({ it.value }, { it.key })
    }
}

