package com.raizesvivas.app.data.sync

import com.raizesvivas.app.data.local.CachePolicy
import com.raizesvivas.app.data.repository.PessoaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de sincronização incremental
 * 
 * Gerencia sincronização apenas de mudanças desde a última atualização
 */
@Singleton
class SyncManager @Inject constructor(
    private val pessoaRepository: PessoaRepository
) {
    
    private var lastSyncTime: Long? = null
    
    /**
     * Sincroniza apenas mudanças desde a última sincronização
     * 
     * @param forceSync Se true, força sincronização completa
     * @return Flow com progresso da sincronização
     */
    fun syncIncremental(forceSync: Boolean = false): Flow<SyncResult> = flow {
        try {
            emit(SyncResult.InProgress(0))
            
            // Se nunca sincronizou ou forceSync, faz sincronização completa
            if (lastSyncTime == null || forceSync) {
                Timber.d("🔄 Sincronização completa iniciada")
                val result = pessoaRepository.sincronizarDoFirestore()
                
                if (result.isSuccess) {
                    lastSyncTime = Date().time
                    emit(SyncResult.Success("Sincronização completa concluída"))
                } else {
                    emit(SyncResult.Error(result.exceptionOrNull()?.message ?: "Erro desconhecido"))
                }
                return@flow
            }
            
            // Sincronização incremental baseada em timestamp
            // TODO: Implementar query Firestore para buscar apenas documentos modificados após lastSyncTime
            // Por enquanto, faz sincronização completa se cache expirou
            val cacheExpired = !CachePolicy.isCacheValid(
                lastSyncTime,
                CachePolicy.CACHE_TIME_PESSOAS
            )
            
            if (cacheExpired) {
                Timber.d("🔄 Cache expirado, sincronizando...")
                val result = pessoaRepository.sincronizarDoFirestore()
                
                if (result.isSuccess) {
                    lastSyncTime = Date().time
                    emit(SyncResult.Success("Sincronização incremental concluída"))
                } else {
                    emit(SyncResult.Error(result.exceptionOrNull()?.message ?: "Erro desconhecido"))
                }
            } else {
                Timber.d("✅ Cache válido, não precisa sincronizar")
                emit(SyncResult.Success("Cache atualizado, sem sincronização necessária"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro na sincronização incremental")
            emit(SyncResult.Error(e.message ?: "Erro desconhecido"))
        }
    }
    
    /**
     * Limpa timestamp de última sincronização
     */
    fun clearSyncTime() {
        lastSyncTime = null
        Timber.d("🗑️ Timestamp de sincronização limpo")
    }
}

/**
 * Resultado da sincronização
 */
sealed class SyncResult {
    data class InProgress(val progress: Int) : SyncResult()
    data class Success(val message: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

