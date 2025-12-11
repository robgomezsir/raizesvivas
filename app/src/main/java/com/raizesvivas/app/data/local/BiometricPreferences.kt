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
        private val KEEP_CONNECTED_KEY = stringPreferencesKey("keep_connected")
        private val LAST_AUTH_TIMESTAMP_KEY = androidx.datastore.preferences.core.longPreferencesKey("last_auth_timestamp")
    }
    
    /**
     * Salva o email do último usuário logado
     * O email é sempre normalizado (trim + lowercase) para garantir consistência
     */
    suspend fun saveLastEmail(email: String) {
        val normalizedEmail = email.trim().lowercase()
        context.dataStore.edit { preferences ->
            preferences[LAST_EMAIL_KEY] = normalizedEmail
        }
    }
    
    /**
     * Obtém o email do último usuário logado
     * O email retornado é sempre normalizado (trim + lowercase)
     */
    fun getLastEmail(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_EMAIL_KEY]?.trim()?.lowercase()
        }
    }
    
    /**
     * Obtém o email do último usuário logado de forma síncrona
     * O email retornado é sempre normalizado (trim + lowercase)
     */
    suspend fun getLastEmailSync(): String? {
        val email = context.dataStore.data.first()[LAST_EMAIL_KEY]
        return email?.trim()?.lowercase()
    }
    
    /**
     * Salva se a biometria está habilitada para o email
     * O email é sempre normalizado (trim + lowercase) antes de salvar
     */
    suspend fun setBiometricEnabled(email: String, enabled: Boolean) {
        val normalizedEmail = email.trim().lowercase()
        context.dataStore.edit { preferences ->
            preferences[LAST_EMAIL_KEY] = normalizedEmail
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
     * Salva a preferência de "Manter conectado"
     */
    suspend fun saveKeepConnected(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEEP_CONNECTED_KEY] = if (enabled) "true" else "false"
        }
    }

    /**
     * Verifica se "Manter conectado" está habilitado
     */
    suspend fun isKeepConnectedSync(): Boolean {
        return context.dataStore.data.first()[KEEP_CONNECTED_KEY] == "true"
    }

    /**
     * Salva o timestamp da última autenticação bem-sucedida
     */
    suspend fun saveLastAuthTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_AUTH_TIMESTAMP_KEY] = timestamp
        }
    }

    /**
     * Obtém o timestamp da última autenticação
     */
    suspend fun getLastAuthTimestampSync(): Long {
        return context.dataStore.data.first()[LAST_AUTH_TIMESTAMP_KEY] ?: 0L
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

