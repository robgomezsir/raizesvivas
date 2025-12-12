package com.raizesvivas.app.domain.model

import java.util.Date

/**
 * Representa uma família que foi explicitamente excluída por um ADMIN SR.
 * Famílias nesta lista não serão recriadas automaticamente pelo algoritmo de agrupamento.
 * 
 * @property familiaId ID da família excluída (mesmo formato usado em FamiliaGrupo)
 * @property excluidoPor ID do usuário ADMIN SR que excluiu a família
 * @property excluidoEm Data e hora da exclusão
 * @property motivo Motivo opcional da exclusão (para auditoria)
 */
data class FamiliaExcluida(
    val familiaId: String = "",
    val excluidoPor: String = "",
    val excluidoEm: Date = Date(),
    val motivo: String? = null
)
