package com.raizesvivas.app.utils

import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.pow

/**
 * Helper para implementar retry logic em operações que podem falhar
 */
object RetryHelper {
    
    /**
     * Executa uma operação com retry automático em caso de falha
     * 
     * @param maxRetries Número máximo de tentativas
     * @param initialDelayMs Delay inicial entre tentativas (exponencial)
     * @param operation Operação a ser executada (suspend)
     * @return Resultado da operação
     */
    suspend fun <T> withRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        operation: suspend () -> Result<T>
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            val result = operation()
            
            if (result.isSuccess) {
                return result
            }
            
            lastException = result.exceptionOrNull() as? Exception
            val delayMs = initialDelayMs * (2.0.pow(attempt)).toLong()
            
            if (attempt < maxRetries - 1) {
                Timber.w("Tentativa ${attempt + 1}/$maxRetries falhou. Retentando em ${delayMs}ms...")
                delay(delayMs)
            }
        }
        
        Timber.e(lastException, "Todas as $maxRetries tentativas falharam")
        return Result.failure(
            lastException ?: Exception("Operação falhou após $maxRetries tentativas")
        )
    }
    
    /**
     * Executa uma operação com retry específico para operações de rede
     */
    suspend fun <T> withNetworkRetry(
        operation: suspend () -> Result<T>
    ): Result<T> {
        return withRetry(
            maxRetries = 3,
            initialDelayMs = 1000,
            operation = operation
        )
    }
}

