package com.raizesvivas.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de preferências para autenticação biométrica
 * 
 * Armazena:
 * - Email do último usuário logado (para biometria)
 * - Flag se biometria está habilitada
 * 
 * IMPORTANTE: A senha NÃO é armazenada. A biometria apenas confirma a identidade,
 * mas o login completo ainda requer comunicação com o Firebase.
 */
@Singleton
class BiometricPreferences @Inject constructor(
    private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "biometric_prefs")
    
    companion object {
        private val LAST_EMAIL_KEY = stringPreferencesKey("last_email")
        private val BIOMETRIC_ENABLED_KEY = stringPreferencesKey("biometric_enabled")
    }
    
    /**
     * Salva o email do último usuário logado
     */
    suspend fun saveLastEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_EMAIL_KEY] = email
        }
    }
    
    /**
     * Obtém o email do último usuário logado
     */
    fun getLastEmail(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_EMAIL_KEY]
        }
    }
    
    /**
     * Obtém o email do último usuário logado de forma síncrona
     */
    suspend fun getLastEmailSync(): String? {
        return context.dataStore.data.first()[LAST_EMAIL_KEY]
    }
    
    /**
     * Salva se a biometria está habilitada para o email
     */
    suspend fun setBiometricEnabled(email: String, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LAST_EMAIL_KEY] = email
            preferences[BIOMETRIC_ENABLED_KEY] = if (enabled) "true" else "false"
        }
    }
    
    /**
     * Verifica se a biometria está habilitada
     */
    fun isBiometricEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] == "true"
        }
    }
    
    /**
     * Verifica se a biometria está habilitada de forma síncrona
     */
    suspend fun isBiometricEnabledSync(): Boolean {
        return context.dataStore.data.first()[BIOMETRIC_ENABLED_KEY] == "true"
    }
    
    /**
     * Limpa todas as preferências (logout)
     */
    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

