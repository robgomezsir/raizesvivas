package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.raizesvivas.app.data.local.Converters
import com.raizesvivas.app.domain.model.SugestaoSubfamilia
import com.raizesvivas.app.domain.model.StatusSugestao
import java.util.Date

/**
 * Entity Room para armazenar sugestões de subfamílias localmente (cache)
 */
@Entity(tableName = "sugestoes_subfamilias")
@TypeConverters(Converters::class)
data class SugestaoSubfamiliaEntity(
    @PrimaryKey
    val id: String,
    val membro1Id: String,
    val membro2Id: String,
    val nomeSugerido: String,
    val membrosIncluidos: List<String>,
    val status: StatusSugestao,
    val criadoEm: Date,
    val processadoEm: Date?,
    val usuarioId: String,
    val familiaZeroId: String,
    
    // Controle de sincronização
    val sincronizadoEm: Date = Date(),
    val precisaSincronizar: Boolean = false
)

/**
 * Converte de Entity para Model de Domínio
 */
fun SugestaoSubfamiliaEntity.toDomain(): SugestaoSubfamilia {
    return SugestaoSubfamilia(
        id = id,
        membro1Id = membro1Id,
        membro2Id = membro2Id,
        nomeSugerido = nomeSugerido,
        membrosIncluidos = membrosIncluidos,
        status = status,
        criadoEm = criadoEm,
        processadoEm = processadoEm,
        usuarioId = usuarioId,
        familiaZeroId = familiaZeroId
    )
}

/**
 * Converte de Model de Domínio para Entity
 */
fun SugestaoSubfamilia.toEntity(): SugestaoSubfamiliaEntity {
    return SugestaoSubfamiliaEntity(
        id = id,
        membro1Id = membro1Id,
        membro2Id = membro2Id,
        nomeSugerido = nomeSugerido,
        membrosIncluidos = membrosIncluidos,
        status = status,
        criadoEm = criadoEm,
        processadoEm = processadoEm,
        usuarioId = usuarioId,
        familiaZeroId = familiaZeroId,
        sincronizadoEm = Date(),
        precisaSincronizar = false
    )
}
