package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        val gson = Gson()
        val tipoEnum = try {
            TipoNotificacao.valueOf(tipo)
        } catch (e: IllegalArgumentException) {
            TipoNotificacao.OUTRO
        }
        
        val dadosMap: Map<String, String> = try {
            if (dadosExtras.isBlank()) {
                emptyMap()
            } else {
                val type = object : TypeToken<Map<String, String>>() {}.type
                gson.fromJson<Map<String, String>>(dadosExtras, type) ?: emptyMap()
            }
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
            val gson = Gson()
            val dadosExtrasJson = try {
                gson.toJson(notificacao.dadosExtras)
            } catch (e: Exception) {
                ""
            }
            
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
