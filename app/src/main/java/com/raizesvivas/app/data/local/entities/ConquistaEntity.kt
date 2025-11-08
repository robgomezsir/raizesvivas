package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import com.raizesvivas.app.domain.model.ProgressoConquista
import java.util.Date

/**
 * Entidade Room para Progresso de Conquista
 * Cada conquista é exclusiva do usuário (chave primária composta: conquistaId + usuarioId)
 */
@Entity(
    tableName = "progresso_conquistas",
    primaryKeys = ["conquistaId", "usuarioId"],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["desbloqueada"]),
        Index(value = ["usuarioId", "desbloqueada"])
    ]
)
data class ConquistaEntity(
    val conquistaId: String,
    val usuarioId: String, // ID do usuário dono desta conquista
    val desbloqueada: Boolean,
    val desbloqueadaEm: Long?, // Date como Long (timestamp)
    val progressoAtual: Int,
    val progressoTotal: Int,
    val sincronizadoEm: Long,
    val precisaSincronizar: Boolean
) {
    fun toDomain(): ProgressoConquista {
        return ProgressoConquista(
            conquistaId = conquistaId,
            desbloqueada = desbloqueada,
            desbloqueadaEm = desbloqueadaEm?.let { Date(it) },
            progressoAtual = progressoAtual,
            progressoTotal = progressoTotal
        )
    }
    
    companion object {
        fun fromDomain(
            progresso: ProgressoConquista,
            usuarioId: String, // ID do usuário (obrigatório)
            sincronizadoEm: Long = System.currentTimeMillis(),
            precisaSincronizar: Boolean = false
        ): ConquistaEntity {
            return ConquistaEntity(
                conquistaId = progresso.conquistaId,
                usuarioId = usuarioId,
                desbloqueada = progresso.desbloqueada,
                desbloqueadaEm = progresso.desbloqueadaEm?.time,
                progressoAtual = progresso.progressoAtual,
                progressoTotal = progresso.progressoTotal,
                sincronizadoEm = sincronizadoEm,
                precisaSincronizar = precisaSincronizar
            )
        }
    }
}

