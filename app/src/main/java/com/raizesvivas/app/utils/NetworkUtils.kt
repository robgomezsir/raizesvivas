package com.raizesvivas.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilitário para verificar conectividade de rede
 */
@Singleton
class NetworkUtils @Inject constructor(
    private val context: Context
) {
    /**
     * Verifica se há conectividade ativa
     */
    fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Verifica se está conectado via Wi-Fi
     */
    fun isWifiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Verifica se está conectado via dados móveis
     */
    fun isMobileConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    
    /**
     * Verifica conectividade e loga o resultado
     */
    fun checkConnectivity(): Boolean {
        val connected = isConnected()
        Timber.d("📡 Conectividade: ${if (connected) "Conectado" else "Desconectado"}")
        return connected
    }
}

