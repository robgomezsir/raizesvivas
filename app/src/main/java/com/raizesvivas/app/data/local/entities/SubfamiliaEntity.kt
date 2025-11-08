package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.raizesvivas.app.data.local.Converters
import com.raizesvivas.app.domain.model.Subfamilia
import com.raizesvivas.app.domain.model.TipoFamilia
import java.util.Date

/**
 * Entity Room para armazenar subfamílias localmente (cache)
 */
@Entity(tableName = "subfamilias")
@TypeConverters(Converters::class)
data class SubfamiliaEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val tipo: TipoFamilia,
    val familiaPaiId: String,
    val membroOrigem1Id: String,
    val membroOrigem2Id: String,
    val nivelHierarquico: Int,
    val criadoEm: Date,
    val criadoPor: String,
    val descricao: String?,
    val ativa: Boolean,
    
    // Controle de sincronização
    val sincronizadoEm: Date = Date(),
    val precisaSincronizar: Boolean = false
)

/**
 * Converte de Entity para Model de Domínio
 */
fun SubfamiliaEntity.toDomain(): Subfamilia {
    return Subfamilia(
        id = id,
        nome = nome,
        tipo = tipo,
        familiaPaiId = familiaPaiId,
        membroOrigem1Id = membroOrigem1Id,
        membroOrigem2Id = membroOrigem2Id,
        nivelHierarquico = nivelHierarquico,
        criadoEm = criadoEm,
        criadoPor = criadoPor,
        descricao = descricao,
        ativa = ativa
    )
}

/**
 * Converte de Model de Domínio para Entity
 */
fun Subfamilia.toEntity(): SubfamiliaEntity {
    return SubfamiliaEntity(
        id = id,
        nome = nome,
        tipo = tipo,
        familiaPaiId = familiaPaiId,
        membroOrigem1Id = membroOrigem1Id,
        membroOrigem2Id = membroOrigem2Id,
        nivelHierarquico = nivelHierarquico,
        criadoEm = criadoEm,
        criadoPor = criadoPor,
        descricao = descricao,
        ativa = ativa,
        sincronizadoEm = Date(),
        precisaSincronizar = false
    )
}
