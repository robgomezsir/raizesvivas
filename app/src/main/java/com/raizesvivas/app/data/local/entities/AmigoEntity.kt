package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.raizesvivas.app.data.local.Converters
import com.raizesvivas.app.domain.model.Amigo
import java.util.Date

/**
 * Entity Room para armazenar amigos da família localmente
 */
@Entity(tableName = "amigos")
@TypeConverters(Converters::class)
data class AmigoEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val telefone: String?,
    val familiaresVinculados: List<String>, // IDs das pessoas vinculadas
    val criadoPor: String,
    val criadoEm: Date,
    val modificadoEm: Date
)

/**
 * Converte de Entity para Model de Domínio
 */
fun AmigoEntity.toDomain(): Amigo {
    return Amigo(
        id = id,
        nome = nome,
        telefone = telefone,
        familiaresVinculados = familiaresVinculados,
        criadoPor = criadoPor,
        criadoEm = criadoEm,
        modificadoEm = modificadoEm
    )
}

/**
 * Converte de Model de Domínio para Entity
 */
fun Amigo.toEntity(): AmigoEntity {
    return AmigoEntity(
        id = id,
        nome = nome,
        telefone = telefone,
        familiaresVinculados = familiaresVinculados,
        criadoPor = criadoPor,
        criadoEm = criadoEm,
        modificadoEm = modificadoEm
    )
}

