package com.raizesvivas.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity de parentesco para Room Database
 * 
 * Representa um parentesco calculado no banco de dados local.
 * Sincronizada com o Supabase.
 */
@Entity(tableName = "parentescos_calculados")
data class KinshipEntity(
    @PrimaryKey
    val id: String,
    val membro1Id: String,
    val membro2Id: String,
    val tipoParentesco: String,
    val grauParentesco: Int,
    val distanciaGeracional: Int,
    val familiaReferenciaId: String,
    val dataCalculo: LocalDateTime,
    val ativo: Boolean = true,
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val syncedAt: LocalDateTime? = null
)
