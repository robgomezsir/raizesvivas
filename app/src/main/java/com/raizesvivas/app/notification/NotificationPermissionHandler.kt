package com.raizesvivas.app.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Helper para gerenciar permissão de notificações no Android 13+
 */
class NotificationPermissionHandler(
    private val activity: ComponentActivity
) {
    private val Context.dataStore by preferencesDataStore(name = "notification_prefs")
    
    private val permissionAskedKey = booleanPreferencesKey("notification_permission_asked")
    
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Timber.d("✅ Permissão de notificação concedida")
            onPermissionGranted?.invoke()
        } else {
            Timber.w("⚠️ Permissão de notificação negada")
            onPermissionDenied?.invoke()
        }
    }
    
    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null
    
    /**
     * Verifica se a permissão de notificação já foi concedida
     */
    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 e anteriores não precisam de permissão runtime
            true
        }
    }
    
    /**
     * Verifica se já solicitamos a permissão antes
     */
    suspend fun wasPermissionAsked(): Boolean {
        return activity.dataStore.data.map { preferences ->
            preferences[permissionAskedKey] ?: false
        }.first()
    }
    
    /**
     * Marca que a permissão já foi solicitada
     */
    private suspend fun markPermissionAsked() {
        activity.dataStore.edit { preferences ->
            preferences[permissionAskedKey] = true
        }
    }
    
    /**
     * Solicita permissão de notificação se necessário
     * 
     * @param onGranted Callback executado quando permissão é concedida
     * @param onDenied Callback executado quando permissão é negada
     */
    suspend fun requestPermissionIfNeeded(
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        this.onPermissionGranted = onGranted
        this.onPermissionDenied = onDenied
        
        // Android 12 e anteriores não precisam de permissão runtime
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Timber.d("📱 Android < 13, permissão não necessária")
            onGranted()
            return
        }
        
        // Verificar se já tem permissão
        if (hasPermission()) {
            Timber.d("✅ Permissão de notificação já concedida")
            onGranted()
            return
        }
        
        // Verificar se já solicitamos antes
        if (wasPermissionAsked()) {
            Timber.d("⚠️ Permissão já foi solicitada anteriormente")
            onDenied()
            return
        }
        
        // Solicitar permissão
        Timber.d("📲 Solicitando permissão de notificação")
        markPermissionAsked()
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    
    /**
     * Força a solicitação de permissão mesmo se já foi solicitada antes
     * Útil para configurações do app
     */
    fun requestPermission(
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        this.onPermissionGranted = onGranted
        this.onPermissionDenied = onDenied
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onGranted()
        }
    }
}
