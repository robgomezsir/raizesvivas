package com.raizesvivas.app.utils

import android.content.Context
import coil.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Utilitário para gerenciar cache de imagens do Coil
 */
object ImageCacheManager {
    
    /**
     * Limpa completamente o cache de imagens (memória + disco)
     * Use quando precisar forçar reload de todas as imagens
     */
    suspend fun clearAllCache(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader(context)
                
                // Limpar cache de memória
                imageLoader.memoryCache?.clear()
                Timber.d("🗑️ Cache de memória do Coil limpo")
                
                // Limpar cache de disco
                imageLoader.diskCache?.clear()
                Timber.d("🗑️ Cache de disco do Coil limpo")
                
                Timber.d("✅ Cache de imagens completamente limpo")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao limpar cache de imagens")
            }
        }
    }
    
    /**
     * Limpa cache de uma URL específica
     */
    suspend fun clearCacheForUrl(context: Context, url: String) {
        withContext(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader(context)
                
                // Remover da memória
                imageLoader.memoryCache?.remove(coil.memory.MemoryCache.Key(url))
                
                // Remover do disco
                imageLoader.diskCache?.remove(url)
                
                Timber.d("🗑️ Cache limpo para URL: $url")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao limpar cache para URL: $url")
            }
        }
    }
}
