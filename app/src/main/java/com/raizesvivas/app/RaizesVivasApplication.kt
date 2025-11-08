package com.raizesvivas.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Classe Application principal do app Raízes Vivas
 * 
 * Responsabilidades:
 * - Inicializar Hilt para injeção de dependências
 * - Configurar Timber para logging
 * - Configurar Firebase
 */
@HiltAndroidApp
class RaizesVivasApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Configurar Timber para logging em desenvolvimento
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("🌳 Raízes Vivas inicializado")
    }
}











































