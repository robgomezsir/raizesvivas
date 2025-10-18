package com.raizesvivas.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entity de membro para Room Database
 * 
 * Representa um membro no banco de dados local.
 * Sincronizada com o Supabase.
 */
@Entity(tableName = "membros")
data class MemberEntity(
    @PrimaryKey
    val id: String,
    val nomeCompleto: String,
    val nomeAbreviado: String? = null,
    val dataNascimento: LocalDate? = null,
    val dataFalecimento: LocalDate? = null,
    val localNascimento: String? = null,
    val localFalecimento: String? = null,
    val profissao: String? = null,
    val observacoes: String? = null,
    val fotoUrl: String? = null,
    val elementosVisuais: String? = null, // JSON string
    val nivelNaArvore: Int = 0,
    val posicaoX: Float? = null,
    val posicaoY: Float? = null,
    val ativo: Boolean = true,
    val userId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val syncedAt: LocalDateTime? = null
)
