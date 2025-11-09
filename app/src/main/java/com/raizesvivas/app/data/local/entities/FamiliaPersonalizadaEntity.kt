package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raizesvivas.app.domain.model.FamiliaPersonalizada
import java.util.Date

@Entity(tableName = "familias_personalizadas")
data class FamiliaPersonalizadaEntity(
    @PrimaryKey val familiaId: String,
    val nome: String,
    val conjuguePrincipalId: String?,
    val conjugueSecundarioId: String?,
    val ehFamiliaZero: Boolean,
    val atualizadoPor: String?,
    val atualizadoEm: Date,
    val sincronizadoEm: Date? = null,
    val precisaSincronizar: Boolean = false
)

fun FamiliaPersonalizadaEntity.toDomain(): FamiliaPersonalizada =
    FamiliaPersonalizada(
        familiaId = familiaId,
        nome = nome,
        conjuguePrincipalId = conjuguePrincipalId,
        conjugueSecundarioId = conjugueSecundarioId,
        ehFamiliaZero = ehFamiliaZero,
        atualizadoPor = atualizadoPor,
        atualizadoEm = atualizadoEm
    )

fun FamiliaPersonalizada.toEntity(
    sincronizadoEm: Date? = Date(),
    precisaSincronizar: Boolean = false
): FamiliaPersonalizadaEntity =
    FamiliaPersonalizadaEntity(
        familiaId = familiaId,
        nome = nome,
        conjuguePrincipalId = conjuguePrincipalId,
        conjugueSecundarioId = conjugueSecundarioId,
        ehFamiliaZero = ehFamiliaZero,
        atualizadoPor = atualizadoPor,
        atualizadoEm = atualizadoEm,
        sincronizadoEm = sincronizadoEm,
        precisaSincronizar = precisaSincronizar
    )

