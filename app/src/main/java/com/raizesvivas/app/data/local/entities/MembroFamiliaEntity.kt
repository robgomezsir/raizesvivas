package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raizesvivas.app.domain.model.MembroFamilia
import com.raizesvivas.app.domain.model.PapelFamilia
import com.raizesvivas.app.domain.model.ElementoArvore

/**
 * Entity Room para armazenar relações de membros com famílias localmente (cache)
 */
@Entity(
    tableName = "membros_familias",
    primaryKeys = ["membroId", "familiaId"]
)
data class MembroFamiliaEntity(
    val membroId: String,
    val familiaId: String,
    val papelNaFamilia: PapelFamilia,
    val elementoNestaFamilia: ElementoArvore,
    val geracaoNaFamilia: Int
) {
    /**
     * ID composto para referência externa
     */
    val id: String
        get() = "${membroId}_${familiaId}"
}

/**
 * Converte de Entity para Model de Domínio
 */
fun MembroFamiliaEntity.toDomain(): MembroFamilia {
    return MembroFamilia(
        id = id,
        membroId = membroId,
        familiaId = familiaId,
        papelNaFamilia = papelNaFamilia,
        elementoNestaFamilia = elementoNestaFamilia,
        geracaoNaFamilia = geracaoNaFamilia
    )
}

/**
 * Converte de Model de Domínio para Entity
 */
fun MembroFamilia.toEntity(): MembroFamiliaEntity {
    return MembroFamiliaEntity(
        membroId = membroId,
        familiaId = familiaId,
        papelNaFamilia = papelNaFamilia,
        elementoNestaFamilia = elementoNestaFamilia,
        geracaoNaFamilia = geracaoNaFamilia
    )
}
