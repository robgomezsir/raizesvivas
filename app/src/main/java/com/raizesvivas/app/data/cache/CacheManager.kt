package com.raizesvivas.app.data.cache

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import timber.log.Timber

/**
 * Gerenciador de cache para otimização de performance
 * 
 * Gerencia cache em memória para dados frequentemente acessados
 */
object CacheManager {
    
    // Cache de pessoas por ID
    private val pessoasCache = ConcurrentHashMap<String, Any>()
    
    // Cache de árvore genealógica calculada
    private val arvoreCache = MutableStateFlow<Map<String, Any>?>(null)
    
    // Cache de relacionamentos
    private val relacionamentosCache = ConcurrentHashMap<String, List<String>>()
    
    // Timestamp do último cache
    private var ultimaAtualizacaoCache = 0L
    
    // TTL do cache (5 minutos)
    private const val CACHE_TTL_MS = 5 * 60 * 1000L
    
    /**
     * Verifica se o cache está válido
     */
    fun isCacheValido(): Boolean {
        val agora = System.currentTimeMillis()
        return (agora - ultimaAtualizacaoCache) < CACHE_TTL_MS
    }
    
    /**
     * Atualiza timestamp do cache
     */
    fun atualizarCache() {
        ultimaAtualizacaoCache = System.currentTimeMillis()
        Timber.d("✅ Cache atualizado")
    }
    
    /**
     * Limpa todo o cache
     */
    fun limparCache() {
        pessoasCache.clear()
        relacionamentosCache.clear()
        arvoreCache.value = null
        ultimaAtualizacaoCache = 0L
        Timber.d("🗑️ Cache limpo")
    }
    
    /**
     * Cache de pessoa por ID
     */
    fun <T> getPessoa(id: String): T? {
        @Suppress("UNCHECKED_CAST")
        return pessoasCache[id] as? T
    }
    
    fun <T> setPessoa(id: String, pessoa: T) {
        pessoasCache[id] = pessoa as Any
    }
    
    /**
     * Cache de relacionamentos
     */
    fun getRelacionamentos(pessoaId: String): List<String>? {
        return relacionamentosCache[pessoaId]
    }
    
    fun setRelacionamentos(pessoaId: String, relacionamentos: List<String>) {
        relacionamentosCache[pessoaId] = relacionamentos
    }
    
    /**
     * Cache de árvore genealógica
     */
    val arvoreCacheFlow: StateFlow<Map<String, Any>?> = arvoreCache.asStateFlow()
    
    fun setArvoreCache(cache: Map<String, Any>) {
        arvoreCache.value = cache
        atualizarCache()
    }
    
    /**
     * Remove pessoa do cache
     */
    fun removerPessoa(id: String) {
        pessoasCache.remove(id)
        relacionamentosCache.remove(id)
    }
    
    /**
     * Estatísticas do cache
     */
    fun getEstatisticas(): CacheEstatisticas {
        return CacheEstatisticas(
            pessoasEmCache = pessoasCache.size,
            relacionamentosEmCache = relacionamentosCache.size,
            cacheValido = isCacheValido(),
            ultimaAtualizacao = ultimaAtualizacaoCache
        )
    }
}

/**
 * Estatísticas do cache
 */
data class CacheEstatisticas(
    val pessoasEmCache: Int,
    val relacionamentosEmCache: Int,
    val cacheValido: Boolean,
    val ultimaAtualizacao: Long
)

