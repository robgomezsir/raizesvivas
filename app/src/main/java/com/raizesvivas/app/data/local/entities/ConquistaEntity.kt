package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import com.raizesvivas.app.domain.model.ProgressoConquista
import java.util.Date

/**
 * Entidade Room para Progresso de Conquista
 * Cada conquista é exclusiva do usuário (chave primária composta: conquistaId + usuarioId)
 * 
 * ATUALIZADO: Usa novos nomes de campos (concluida, progresso) mas mantém compatibilidade
 */
@Entity(
    tableName = "progresso_conquistas",
    primaryKeys = ["conquistaId", "usuarioId"],
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["concluida"]), // Renomeado de "desbloqueada"
        Index(value = ["usuarioId", "concluida"])
    ]
)
data class ConquistaEntity(
    val conquistaId: String,
    val usuarioId: String, // ID do usuário dono desta conquista
    val concluida: Boolean, // Renomeado de "desbloqueada"
    val desbloqueadaEm: Long?, // Date como Long (timestamp)
    val progresso: Int, // Renomeado de "progressoAtual"
    val progressoTotal: Int,
    val nivel: Int = 1, // Novo campo
    val pontuacaoTotal: Int = 0, // Novo campo
    val sincronizadoEm: Long,
    val precisaSincronizar: Boolean
) {
    fun toDomain(): ProgressoConquista {
        return ProgressoConquista(
            conquistaId = conquistaId,
            concluida = concluida,
            desbloqueadaEm = desbloqueadaEm?.let { Date(it) },
            progresso = progresso,
            progressoTotal = progressoTotal,
            nivel = nivel,
            pontuacaoTotal = pontuacaoTotal
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
                concluida = progresso.concluida,
                desbloqueadaEm = progresso.desbloqueadaEm?.time,
                progresso = progresso.progresso,
                progressoTotal = progresso.progressoTotal,
                nivel = progresso.nivel,
                pontuacaoTotal = progresso.pontuacaoTotal,
                sincronizadoEm = sincronizadoEm,
                precisaSincronizar = precisaSincronizar
            )
        }
    }
}

