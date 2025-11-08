package com.raizesvivas.app.data.local

import timber.log.Timber
import java.util.Date

/**
 * Políticas de cache e invalidação
 */
object CachePolicy {
    
    // Tempos de cache em milissegundos (valores em milissegundos)
    const val CACHE_TIME_PESSOAS = 3600000L // 1 hora = 60 * 60 * 1000 ms
    const val CACHE_TIME_USUARIOS = 7200000L // 2 horas = 2 * 60 * 60 * 1000 ms
    const val CACHE_TIME_FAMILIA_ZERO = 86400000L // 24 horas = 24 * 60 * 60 * 1000 ms
    
    /**
     * Verifica se o cache está válido
     * 
     * @param lastUpdate Timestamp da última atualização
     * @param cacheTimeMs Tempo de cache em milissegundos
     * @return true se cache ainda é válido, false se precisa atualizar
     */
    fun isCacheValid(
        lastUpdate: Long?,
        cacheTimeMs: Long
    ): Boolean {
        if (lastUpdate == null) {
            Timber.d("📦 Cache inválido: nunca foi atualizado")
            return false
        }
        
        val now = Date().time
        val age = now - lastUpdate
        val isValid = age < cacheTimeMs
        
        if (!isValid) {
            Timber.d("📦 Cache expirado: idade ${age}ms, limite ${cacheTimeMs}ms")
        } else {
            Timber.d("📦 Cache válido: idade ${age}ms, limite ${cacheTimeMs}ms")
        }
        
        return isValid
    }
    
    /**
     * Calcula quando o cache expirará
     */
    fun getCacheExpiryTime(
        lastUpdate: Long?,
        cacheTimeMs: Long
    ): Long? {
        return lastUpdate?.let { it + cacheTimeMs }
    }
    
    /**
     * Força invalidação do cache (marca como expirado)
     */
    fun invalidateCache(): Long {
        Timber.d("🗑️ Cache invalidado manualmente")
        return 0L // Retorna 0 para forçar atualização
    }
}

