package com.raizesvivas.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.raizesvivas.app.domain.model.PerfilGamificacao
import java.util.Date

/**
 * Entidade Room para Perfil de Gamificação
 */
@Entity(
    tableName = "perfil_gamificacao",
    indices = [
        Index(value = ["usuarioId"])
    ]
)
data class PerfilGamificacaoEntity(
    @PrimaryKey
    val usuarioId: String,
    val nivel: Int,
    val xpTotal: Int, // XP total acumulado
    val conquistasDesbloqueadas: Int,
    val totalConquistas: Int,
    val sincronizadoEm: Long,
    val precisaSincronizar: Boolean
) {
    fun toDomain(): PerfilGamificacao {
        // Calcular XP atual e próximo nível usando SistemaConquistas
        val xpNoNivel = calcularXPNoNivel(xpTotal, nivel)
        val xpProximoNivel = calcularXPProximoNivel(nivel)
        
        return PerfilGamificacao(
            usuarioId = usuarioId,
            nivel = nivel,
            xpAtual = xpNoNivel,
            xpProximoNivel = xpProximoNivel,
            conquistasDesbloqueadas = conquistasDesbloqueadas,
            totalConquistas = totalConquistas,
            historicoXP = emptyList() // TODO: Implementar histórico se necessário
        )
    }
    
    private fun calcularXPProximoNivel(nivel: Int): Int {
        return nivel * 500
    }
    
    private fun calcularXPNoNivel(xpTotal: Int, nivel: Int): Int {
        var xpAcumulado = 0
        for (i in 1 until nivel) {
            xpAcumulado += calcularXPProximoNivel(i)
        }
        return xpTotal - xpAcumulado
    }
    
    companion object {
        fun fromDomain(
            perfil: PerfilGamificacao,
            xpTotal: Int, // XP total calculado
            sincronizadoEm: Long = System.currentTimeMillis(),
            precisaSincronizar: Boolean = false
        ): PerfilGamificacaoEntity {
            return PerfilGamificacaoEntity(
                usuarioId = perfil.usuarioId,
                nivel = perfil.nivel,
                xpTotal = xpTotal,
                conquistasDesbloqueadas = perfil.conquistasDesbloqueadas,
                totalConquistas = perfil.totalConquistas,
                sincronizadoEm = sincronizadoEm,
                precisaSincronizar = precisaSincronizar
            )
        }
    }
}

