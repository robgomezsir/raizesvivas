package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raizesvivas.app.domain.model.FamiliaExcluida
import java.util.Date

/**
 * Entity do Room para persistir famílias excluídas localmente
 */
@Entity(tableName = "familias_excluidas")
data class FamiliaExcluidaEntity(
    @PrimaryKey
    val familiaId: String,
    val excluidoPor: String,
    val excluidoEm: Long, // Timestamp em milissegundos
    val motivo: String?,
    val sincronizadoEm: Long?, // Timestamp da última sincronização com Firestore
    val precisaSincronizar: Boolean // Flag para sincronização offline
)

/**
 * Converte Entity para modelo de domínio
 */
fun FamiliaExcluidaEntity.toDomain(): FamiliaExcluida {
    return FamiliaExcluida(
        familiaId = familiaId,
        excluidoPor = excluidoPor,
        excluidoEm = Date(excluidoEm),
        motivo = motivo
    )
}

/**
 * Converte modelo de domínio para Entity
 */
fun FamiliaExcluida.toEntity(
    sincronizadoEm: Date? = null,
    precisaSincronizar: Boolean = false
): FamiliaExcluidaEntity {
    return FamiliaExcluidaEntity(
        familiaId = familiaId,
        excluidoPor = excluidoPor,
        excluidoEm = excluidoEm.time,
        motivo = motivo,
        sincronizadoEm = sincronizadoEm?.time,
        precisaSincronizar = precisaSincronizar
    )
}
