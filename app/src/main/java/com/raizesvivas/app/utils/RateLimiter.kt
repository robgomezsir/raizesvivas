package com.raizesvivas.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tipo de operação para rate limiting
 */
enum class OperationType {
    UPLOAD_FOTO,
    CRIAR_PESSOA,
    ENVIAR_MENSAGEM
}

/**
 * Limites de rate por tipo de operação
 */
data class RateLimit(
    val maxOperations: Int,
    val timeWindowMs: Long
)

/**
 * Gerenciador de rate limiting para prevenir abuso de operações
 * 
 * Usa DataStore para persistir histórico de operações
 */
@Singleton
class RateLimiter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rate_limiter")
        
        // Limites configurados por tipo de operação
        private val RATE_LIMITS = mapOf(
            OperationType.UPLOAD_FOTO to RateLimit(maxOperations = 10, timeWindowMs = TimeUnit.MINUTES.toMillis(1)),
            OperationType.CRIAR_PESSOA to RateLimit(maxOperations = 20, timeWindowMs = TimeUnit.MINUTES.toMillis(1)),
            OperationType.ENVIAR_MENSAGEM to RateLimit(maxOperations = 30, timeWindowMs = TimeUnit.MINUTES.toMillis(1))
        )
    }
    
    /**
     * Verifica se uma operação pode ser executada (não excedeu o limite)
     * 
     * @param operationType Tipo de operação
     * @param userId ID do usuário (opcional, se null usa timestamp global)
     * @return true se a operação pode ser executada, false se excedeu o limite
     */
    suspend fun canExecute(operationType: OperationType, userId: String? = null): Boolean {
        val limit = RATE_LIMITS[operationType] ?: return true // Sem limite se não configurado
        
        val key = getKey(operationType, userId)
        val now = System.currentTimeMillis()
        val cutoffTime = now - limit.timeWindowMs
        
        return try {
            val timestamps = context.dataStore.data
                .map { preferences ->
                    preferences[key]?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
                }
                .first()
            
            // Filtrar timestamps dentro da janela de tempo
            val recentTimestamps = timestamps.filter { it > cutoffTime }
            
            val canExecute = recentTimestamps.size < limit.maxOperations
            
            if (!canExecute) {
                Timber.w("⚠️ Rate limit excedido para $operationType: ${recentTimestamps.size}/${limit.maxOperations}")
            }
            
            canExecute
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao verificar rate limit")
            true // Em caso de erro, permitir operação
        }
    }
    
    /**
     * Registra uma operação executada
     * 
     * @param operationType Tipo de operação
     * @param userId ID do usuário (opcional)
     */
    suspend fun recordOperation(operationType: OperationType, userId: String? = null) {
        val key = getKey(operationType, userId)
        val now = System.currentTimeMillis()
        val limit = RATE_LIMITS[operationType] ?: return
        val cutoffTime = now - limit.timeWindowMs
        
        try {
            context.dataStore.edit { preferences ->
                val currentTimestamps = preferences[key]?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
                
                // Filtrar timestamps dentro da janela de tempo e adicionar o novo
                val recentTimestamps = currentTimestamps.filter { it > cutoffTime } + now
                
                // Manter apenas os mais recentes (limite + 1 para margem)
                val trimmedTimestamps = recentTimestamps.sortedDescending().take(limit.maxOperations + 1)
                
                preferences[key] = trimmedTimestamps.joinToString(",")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao registrar operação no rate limiter")
        }
    }
    
    /**
     * Obtém a chave de preferência para um tipo de operação e usuário
     */
    private fun getKey(operationType: OperationType, userId: String?): Preferences.Key<String> {
        val suffix = userId ?: "global"
        return stringPreferencesKey("rate_limit_${operationType.name}_$suffix")
    }
    
    /**
     * Obtém mensagem amigável quando rate limit é excedido
     */
    fun getLimitExceededMessage(operationType: OperationType): String {
        val limit = RATE_LIMITS[operationType] ?: return "Limite de operações excedido"
        val timeWindowMinutes = TimeUnit.MILLISECONDS.toMinutes(limit.timeWindowMs)
        return "Você excedeu o limite de ${limit.maxOperations} operações por $timeWindowMinutes minuto(s). Tente novamente em alguns instantes."
    }
}

