package com.raizesvivas.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entity de relacionamento para Room Database
 * 
 * Representa um relacionamento no banco de dados local.
 * Sincronizada com o Supabase.
 */
@Entity(tableName = "relacionamentos")
data class RelationshipEntity(
    @PrimaryKey
    val id: String,
    val membro1Id: String,
    val membro2Id: String,
    val tipoRelacionamento: String,
    val dataInicio: LocalDate? = null,
    val dataFim: LocalDate? = null,
    val observacoes: String? = null,
    val ativo: Boolean = true,
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val syncedAt: LocalDateTime? = null
)
