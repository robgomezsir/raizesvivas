package com.raizesvivas.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Classe Application principal do app Raízes Vivas
 * 
 * Responsabilidades:
 * - Inicializar Hilt para injeção de dependências
 * - Configurar Timber para logging
 * - Configurar Coil para carregamento otimizado de imagens
 * - Configurar Firebase
 */
@HiltAndroidApp
class RaizesVivasApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        
        // Configurar Timber otimizado
        if (BuildConfig.DEBUG) {
            // Em desenvolvimento: logs completos
            Timber.plant(Timber.DebugTree())
            Timber.d("🌳 Raízes Vivas inicializado (DEBUG)")
        } else {
            // Em produção: apenas erros críticos
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    // Apenas logs de erro e warning em produção
                    if (priority >= Log.WARN) {
                        // Aqui você pode enviar para Firebase Crashlytics se necessário
                        // FirebaseCrashlytics.getInstance().log("$tag: $message")
                        if (t != null) {
                            // FirebaseCrashlytics.getInstance().recordException(t)
                        }
                    }
                }
            })
            Timber.i("🌳 Raízes Vivas inicializado (RELEASE)")
        }
    }
    
    /**
     * Configuração otimizada do Coil para carregamento de imagens
     * 
     * Otimizações:
     * - Cache de memória: 25% da memória disponível
     * - Cache de disco: 50MB
     * - Crossfade para transições suaves
     * - Política de cache agressiva
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% da memória disponível
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .crossfade(true) // Transições suaves
            .respectCacheHeaders(false) // Ignorar headers de cache HTTP
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}













































