package com.raizesvivas.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity de família para Room Database
 * 
 * Representa uma família no banco de dados local.
 * Sincronizada com o Supabase.
 */
@Entity(tableName = "familias")
data class FamilyEntity(
    @PrimaryKey
    val id: String,
    val nome: String,
    val tipo: String,
    val familiaPaiId: String? = null,
    val criadaPorCasamento: Boolean = false,
    val membroOrigem1Id: String? = null,
    val membroOrigem2Id: String? = null,
    val dataCriacao: LocalDateTime,
    val iconeArvore: String? = null,
    val nivelHierarquico: Int = 0,
    val ativa: Boolean = true,
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val syncedAt: LocalDateTime? = null
)
