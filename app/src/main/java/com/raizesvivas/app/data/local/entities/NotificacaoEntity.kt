package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.raizesvivas.app.domain.model.Notificacao
import com.raizesvivas.app.domain.model.TipoNotificacao

/**
 * Entidade Room para Notificações
 */
@Entity(
    tableName = "notificacoes",
    indices = [
        Index(value = ["lida"]),
        Index(value = ["criadaEm"])
    ]
)
data class NotificacaoEntity(
    @PrimaryKey
    val id: String,
    val tipo: String, // TipoNotificacao como String
    val titulo: String,
    val mensagem: String,
    val lida: Boolean,
    val criadaEm: Long, // Date como Long (timestamp)
    val relacionadoId: String?,
    val dadosExtras: String, // Map como JSON string
    val sincronizadoEm: Long,
    val precisaSincronizar: Boolean
) {
    fun toDomain(): Notificacao {
        val tipoEnum = try {
            TipoNotificacao.valueOf(tipo)
        } catch (e: IllegalArgumentException) {
            TipoNotificacao.OUTRO
        }
        
        val dadosMap = try {
            // Parse JSON string to Map (simplificado - usar Gson se necessário)
            emptyMap<String, String>() // TODO: Implementar parsing se necessário
        } catch (e: Exception) {
            emptyMap()
        }
        
        return Notificacao(
            id = id,
            tipo = tipoEnum,
            titulo = titulo,
            mensagem = mensagem,
            lida = lida,
            criadaEm = java.util.Date(criadaEm),
            relacionadoId = relacionadoId,
            dadosExtras = dadosMap
        )
    }
    
    companion object {
        fun fromDomain(
            notificacao: Notificacao,
            sincronizadoEm: Long = System.currentTimeMillis(),
            precisaSincronizar: Boolean = false
        ): NotificacaoEntity {
            val dadosExtrasJson = "" // TODO: Serializar Map para JSON se necessário
            
            return NotificacaoEntity(
                id = notificacao.id,
                tipo = notificacao.tipo.name,
                titulo = notificacao.titulo,
                mensagem = notificacao.mensagem,
                lida = notificacao.lida,
                criadaEm = notificacao.criadaEm.time,
                relacionadoId = notificacao.relacionadoId,
                dadosExtras = dadosExtrasJson,
                sincronizadoEm = sincronizadoEm,
                precisaSincronizar = precisaSincronizar
            )
        }
    }
}
