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
            // Fazer cópia local para evitar problema de smart cast
            val syncTime = lastSyncTime ?: return@flow
            val lastSyncDate = Date(syncTime)
            Timber.d("🔄 Sincronização incremental iniciada desde ${lastSyncDate}")
            
            val result = pessoaRepository.sincronizarModificadasDesde(lastSyncDate)
            
            if (result.isSuccess) {
                // Atualizar timestamp apenas se sincronização foi bem-sucedida
                lastSyncTime = Date().time
                emit(SyncResult.Success("Sincronização incremental concluída"))
            } else {
                // Se falhar, fazer fallback para sincronização completa
                Timber.w("⚠️ Sincronização incremental falhou, tentando sincronização completa...")
                val fallbackResult = pessoaRepository.sincronizarDoFirestore()
                
                if (fallbackResult.isSuccess) {
                    lastSyncTime = Date().time
                    emit(SyncResult.Success("Sincronização completa concluída (fallback)"))
                } else {
                    emit(SyncResult.Error(fallbackResult.exceptionOrNull()?.message ?: "Erro desconhecido"))
                }
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

