package com.raizesvivas.app.domain.model

import java.util.Date

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
    val ordem: Int = 0 // Ordem de exibição
)

