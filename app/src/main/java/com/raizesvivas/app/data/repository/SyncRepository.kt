package com.raizesvivas.app.data.repository

import com.raizesvivas.app.data.local.CachePolicy
import com.raizesvivas.app.data.sync.SyncManager
import com.raizesvivas.app.data.sync.SyncResult
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gerenciar sincronização
 * 
 * Facilita acesso ao SyncManager
 */
@Singleton
class SyncRepository @Inject constructor(
    private val syncManager: SyncManager
) {
    /**
     * Sincroniza dados incrementalmente
     */
    fun sincronizarIncremental(forceSync: Boolean = false): Flow<SyncResult> {
        Timber.d("🔄 Iniciando sincronização incremental (force=$forceSync)")
        return syncManager.syncIncremental(forceSync)
    }
    
    /**
     * Força invalidação de cache e sincronização completa
     */
    fun sincronizarForcado(): Flow<SyncResult> {
        Timber.d("🔄 Forçando sincronização completa")
        return syncManager.syncIncremental(forceSync = true)
    }
    
    /**
     * Limpa cache de sincronização
     */
    fun limparCache() {
        syncManager.clearSyncTime()
        Timber.d("🗑️ Cache de sincronização limpo")
    }
}

