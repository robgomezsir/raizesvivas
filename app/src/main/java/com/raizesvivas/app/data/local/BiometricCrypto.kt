package com.raizesvivas.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serviço para armazenar senha de forma segura usando Android Keystore
 * 
 * Usa EncryptedSharedPreferences para armazenar a senha criptografada.
 * A criptografia usa chaves do Android Keystore que são protegidas por hardware.
 */
@Singleton
class BiometricCrypto @Inject constructor(
    private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "biometric_encrypted_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    companion object {
        private const val PASSWORD_KEY_PREFIX = "password_"
    }
    
    /**
     * Salva a senha criptografada para um email específico
     * 
     * @param email Email do usuário (usado como chave)
     * @param password Senha a ser salva (será criptografada)
     */
    fun savePassword(email: String, password: String) {
        try {
            val key = "$PASSWORD_KEY_PREFIX${email.trim().lowercase().hashCode()}"
            Timber.d("🔐 Salvando senha para email: ${email.trim().lowercase()} (key: $key)")
            encryptedPrefs.edit()
                .putString(key, password)
                .commit() // Usar commit() ao invés de apply() para garantir que seja salvo imediatamente
            Timber.d("✅ Senha salva de forma segura para: $email (key: $key)")
            
            // Verificar se foi salva corretamente
            val savedPassword = encryptedPrefs.getString(key, null)
            if (savedPassword != null) {
                Timber.d("✅ Verificação: Senha confirmada como salva para: $email")
            } else {
                Timber.e("❌ ERRO: Senha não foi salva corretamente para: $email")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar senha criptografada")
        }
    }
    
    /**
     * Obtém a senha descriptografada para um email específico
     * 
     * @param email Email do usuário
     * @return Senha descriptografada ou null se não encontrada
     */
    fun getPassword(email: String): String? {
        return try {
            val key = "$PASSWORD_KEY_PREFIX${email.trim().lowercase().hashCode()}"
            Timber.d("🔐 Buscando senha para email: ${email.trim().lowercase()} (key: $key)")
            val password = encryptedPrefs.getString(key, null)
            if (password != null) {
                Timber.d("✅ Senha encontrada para: $email (key: $key)")
            } else {
                Timber.w("⚠️ Senha não encontrada para: $email (key: $key)")
                // Tentar buscar todas as chaves para debug
                val allKeys = encryptedPrefs.all.keys
                Timber.d("🔍 Chaves disponíveis: $allKeys")
            }
            password
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao obter senha descriptografada")
            null
        }
    }
    
    /**
     * Remove a senha salva para um email específico
     */
    fun removePassword(email: String) {
        try {
            val key = "$PASSWORD_KEY_PREFIX${email.hashCode()}"
            encryptedPrefs.edit()
                .remove(key)
                .apply()
            Timber.d("🗑️ Senha removida para: $email")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao remover senha")
        }
    }
    
    /**
     * Limpa todas as senhas salvas (logout)
     */
    fun clearAllPasswords() {
        try {
            encryptedPrefs.edit()
                .clear()
                .apply()
            Timber.d("🗑️ Todas as senhas foram removidas")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar senhas")
        }
    }
}

