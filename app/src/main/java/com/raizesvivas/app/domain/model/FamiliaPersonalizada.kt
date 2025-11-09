package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Nome personalizado atribuído a um núcleo familiar (casal + descendentes).
 *
 * Persistido no Firestore e espelhado no cache local para manter a experiência offline.
 */
data class FamiliaPersonalizada(
    val familiaId: String,
    val nome: String,
    val conjuguePrincipalId: String? = null,
    val conjugueSecundarioId: String? = null,
    val ehFamiliaZero: Boolean = false,
    val atualizadoPor: String? = null,
    val atualizadoEm: Date = Date()
)

