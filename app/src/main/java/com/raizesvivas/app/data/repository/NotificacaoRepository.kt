package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.dao.NotificacaoDao
import com.raizesvivas.app.data.local.entities.NotificacaoEntity
import com.raizesvivas.app.domain.model.Notificacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar notificações
 */
@Singleton
class NotificacaoRepository @Inject constructor(
    private val notificacaoDao: NotificacaoDao
) {
    
    /**
     * Observa todas as notificações
     */
    fun observarTodasNotificacoes(): Flow<List<Notificacao>> {
        return notificacaoDao.observarTodasNotificacoes()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    
    /**
     * Observa apenas notificações não lidas
     */
    fun observarNotificacoesNaoLidas(): Flow<List<Notificacao>> {
        return notificacaoDao.observarNotificacoesNaoLidas()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
    
    /**
     * Conta notificações não lidas
     */
    fun contarNaoLidas(): Flow<Int> {
        return notificacaoDao.contarNaoLidas()
    }
    
    /**
     * Cria uma nova notificação
     */
    suspend fun criarNotificacao(notificacao: Notificacao) {
        try {
            val entity = NotificacaoEntity.fromDomain(notificacao)
            notificacaoDao.inserirOuAtualizar(entity)
            Timber.d("✅ Notificação criada: ${notificacao.titulo}")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar notificação")
        }
    }
    
    /**
     * Marca notificação como lida
     */
    suspend fun marcarComoLida(id: String) {
        try {
            notificacaoDao.marcarComoLida(id)
            Timber.d("✅ Notificação marcada como lida: $id")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar notificação como lida")
        }
    }
    
    /**
     * Marca todas as notificações como lidas
     */
    suspend fun marcarTodasComoLidas() {
        try {
            notificacaoDao.marcarTodasComoLidas()
            Timber.d("✅ Todas as notificações marcadas como lidas")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao marcar todas como lidas")
        }
    }
}
