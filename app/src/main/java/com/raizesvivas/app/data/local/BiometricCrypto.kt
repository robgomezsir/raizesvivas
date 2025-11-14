package com.raizesvivas.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import javax.crypto.AEADBadTagException

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
    private var masterKey: MasterKey? = null
    private var encryptedPrefs: SharedPreferences? = null
    private var isCorrupted = false
    
    companion object {
        private const val PASSWORD_KEY_PREFIX = "password_"
        private const val PREFS_NAME = "biometric_encrypted_prefs"
    }
    
    /**
     * Obtém ou cria a MasterKey
     */
    private fun getMasterKey(): MasterKey {
        if (masterKey == null) {
            masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        }
        return masterKey!!
    }
    
    /**
     * Obtém ou cria o EncryptedSharedPreferences
     * Se detectar dados corrompidos, limpa e recria
     */
    private fun getEncryptedPrefs(): SharedPreferences {
        if (encryptedPrefs != null && !isCorrupted) {
            return encryptedPrefs!!
        }
        
        return try {
            val masterKeyInstance = getMasterKey()
            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKeyInstance,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            encryptedPrefs = prefs
            isCorrupted = false
            prefs
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar EncryptedSharedPreferences")
            // Se falhar, tentar limpar dados corrompidos e recriar
            val isCorruptionError = e is AEADBadTagException || 
                e.javaClass.simpleName == "KeyStoreException" ||
                (e.message?.contains("VERIFICATION_FAILED") == true) ||
                (e.message?.contains("Signature/MAC verification failed") == true)
            
            if (isCorruptionError) {
                Timber.w("⚠️ Dados corrompidos detectados, limpando e recriando...")
                clearCorruptedData()
                // Tentar recriar após limpar
                val masterKeyInstance = getMasterKey()
                val prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKeyInstance,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                encryptedPrefs = prefs
                isCorrupted = false
                prefs
            } else {
                throw e
            }
        }
    }
    
    /**
     * Limpa dados corrompidos do EncryptedSharedPreferences
     */
    private fun clearCorruptedData() {
        try {
            // Tentar limpar o arquivo de preferências diretamente
            val prefsFile = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefsFile.edit().clear().apply()
            Timber.d("✅ Dados corrompidos limpos")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar dados corrompidos")
        }
        // Resetar referências
        encryptedPrefs = null
        isCorrupted = true
    }
    
    /**
     * Gera uma chave segura a partir do email
     * Usa o email completo normalizado para evitar colisões de hashCode
     * 
     * IMPORTANTE: Esta função deve sempre normalizar o email da mesma forma
     * para garantir que savePassword e getPassword usem a mesma chave
     */
    private fun generateKey(email: String): String {
        val normalizedEmail = email.trim().lowercase()
        // Usar o email completo ao invés de apenas hashCode para evitar colisões
        // Substituir caracteres especiais que podem causar problemas em chaves
        // Manter apenas letras, números e alguns caracteres seguros
        val safeEmail = normalizedEmail.replace(Regex("[^a-z0-9._-]"), "_")
        val key = "$PASSWORD_KEY_PREFIX$safeEmail"
        Timber.d("🔑 Chave gerada para email '$normalizedEmail': '$key'")
        return key
    }
    
    /**
     * Lista todas as chaves salvas (apenas para debug)
     * Retorna emptySet se houver erro para não quebrar o fluxo principal
     */
    fun listAllKeys(): Set<String> {
        return try {
            val prefs = getEncryptedPrefs()
            val allKeys = prefs.all.keys
            Timber.d("🔍 Total de chaves salvas: ${allKeys.size}")
            allKeys.forEach { key ->
                Timber.d("🔍   - Chave: $key")
            }
            allKeys
        } catch (e: AEADBadTagException) {
            Timber.e(e, "❌ Erro de criptografia ao listar chaves (dados corrompidos)")
            isCorrupted = true
            clearCorruptedData()
            emptySet()
        } catch (e: Exception) {
            // Verificar se é KeyStoreException pela mensagem ou nome da classe
            val isKeyStoreException = e.javaClass.simpleName == "KeyStoreException" ||
                e.message?.contains("VERIFICATION_FAILED") == true ||
                e.message?.contains("Signature/MAC verification failed") == true
            
            if (isKeyStoreException) {
                Timber.e(e, "❌ Erro de verificação ao listar chaves (dados corrompidos)")
                isCorrupted = true
                clearCorruptedData()
            } else {
                Timber.e(e, "❌ Erro ao listar chaves")
            }
            emptySet()
        }
    }
    
    /**
     * Salva a senha criptografada para um email específico
     * 
     * @param email Email do usuário (usado como chave)
     * @param password Senha a ser salva (será criptografada)
     */
    fun savePassword(email: String, password: String) {
        try {
            // Normalizar email para garantir consistência
            val normalizedEmail = email.trim().lowercase()
            val key = generateKey(normalizedEmail)
            Timber.d("🔐 Salvando senha para email: $normalizedEmail (key: $key)")
            Timber.d("🔐 Senha tem ${password.length} caracteres")
            
            val prefs = getEncryptedPrefs()
            
            // Usar commit() para garantir que seja salvo imediatamente e de forma síncrona
            val success = prefs.edit()
                .putString(key, password)
                .commit()
            
            if (success) {
                Timber.d("✅ Senha salva de forma segura para: $normalizedEmail (key: $key)")
                
                // Verificar se foi salva corretamente imediatamente após salvar
                val savedPassword = prefs.getString(key, null)
                if (savedPassword != null && savedPassword == password) {
                    Timber.d("✅ Verificação: Senha confirmada como salva corretamente para: $normalizedEmail")
                    Timber.d("✅ Senha recuperada tem ${savedPassword.length} caracteres")
                } else {
                    Timber.e("❌ ERRO: Senha não foi salva corretamente para: $normalizedEmail")
                    Timber.e("❌ Senha salva: ${savedPassword != null}, Match: ${savedPassword == password}")
                    if (savedPassword != null) {
                        Timber.e("❌ Senha salva tem ${savedPassword.length} caracteres, esperado ${password.length}")
                    }
                }
            } else {
                Timber.e("❌ ERRO: Falha ao salvar senha (commit retornou false) para: $normalizedEmail")
            }
        } catch (e: AEADBadTagException) {
            Timber.e(e, "❌ Erro de criptografia ao salvar senha (dados corrompidos)")
            isCorrupted = true
            clearCorruptedData()
            // Tentar novamente após limpar
            try {
                val normalizedEmail = email.trim().lowercase()
                val key = generateKey(normalizedEmail)
                val prefs = getEncryptedPrefs()
                prefs.edit().putString(key, password).commit()
                Timber.d("✅ Senha salva após limpar dados corrompidos")
            } catch (retryException: Exception) {
                Timber.e(retryException, "❌ Erro ao salvar senha após limpar dados corrompidos")
                throw retryException
            }
        } catch (e: Exception) {
            // Verificar se é KeyStoreException pela mensagem ou nome da classe
            val isKeyStoreException = e.javaClass.simpleName == "KeyStoreException" ||
                e.message?.contains("VERIFICATION_FAILED") == true ||
                e.message?.contains("Signature/MAC verification failed") == true
            
            if (isKeyStoreException) {
                Timber.e(e, "❌ Erro de verificação ao salvar senha (dados corrompidos)")
                isCorrupted = true
                clearCorruptedData()
                // Tentar novamente após limpar
                try {
                    val normalizedEmail = email.trim().lowercase()
                    val key = generateKey(normalizedEmail)
                    val prefs = getEncryptedPrefs()
                    prefs.edit().putString(key, password).commit()
                    Timber.d("✅ Senha salva após limpar dados corrompidos")
                } catch (retryException: Exception) {
                    Timber.e(retryException, "❌ Erro ao salvar senha após limpar dados corrompidos")
                    throw retryException
                }
            } else {
                Timber.e(e, "❌ Erro ao salvar senha criptografada")
                throw e // Re-throw para que o erro seja tratado pelo chamador
            }
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
            // Normalizar email para garantir consistência
            val normalizedEmail = email.trim().lowercase()
            val key = generateKey(normalizedEmail)
            Timber.d("🔐 Buscando senha para email: '$normalizedEmail' (key: '$key')")
            
            val prefs = getEncryptedPrefs()
            
            // Tentar buscar diretamente primeiro (mais rápido)
            val password = prefs.getString(key, null)
            if (password != null && password.isNotBlank()) {
                Timber.d("✅ Senha encontrada para: '$normalizedEmail' (key: '$key', length: ${password.length})")
                return password
            }
            
            Timber.w("⚠️ Senha não encontrada para: '$normalizedEmail' (key: '$key')")
            Timber.d("🔍 Tentando buscar com variações do email...")
            
            // Tentar com chave antiga usando hashCode (para compatibilidade com versões antigas)
            val keyOldHashCode = "$PASSWORD_KEY_PREFIX${normalizedEmail.hashCode()}"
            Timber.d("🔍 Tentando chave antiga (hashCode): '$keyOldHashCode'")
            val passwordOldHashCode = prefs.getString(keyOldHashCode, null)
            if (passwordOldHashCode != null && passwordOldHashCode.isNotBlank()) {
                Timber.d("✅ Senha encontrada com chave antiga (hashCode), migrando...")
                // Migrar para a chave nova usando email completo
                savePassword(normalizedEmail, passwordOldHashCode)
                return passwordOldHashCode
            }
            
            // Tentar listar chaves apenas se necessário (pode falhar se dados corrompidos)
            val allKeys = try {
                listAllKeys()
            } catch (e: Exception) {
                Timber.w("⚠️ Não foi possível listar chaves, continuando sem essa verificação")
                emptySet()
            }
            
            // Tentar buscar todas as chaves que começam com o prefixo para ver se há alguma similar
            val keysWithPrefix = allKeys.filter { it.startsWith(PASSWORD_KEY_PREFIX) }
            Timber.d("🔍 Chaves com prefixo '$PASSWORD_KEY_PREFIX': ${keysWithPrefix.size}")
            keysWithPrefix.forEach { existingKey ->
                Timber.d("🔍   - Chave existente: '$existingKey'")
                // Tentar extrair o email da chave e comparar
                val emailFromKey = existingKey.removePrefix(PASSWORD_KEY_PREFIX)
                val normalizedEmailFromKey = emailFromKey.replace("_", "").lowercase()
                val normalizedEmailClean = normalizedEmail.replace(Regex("[^a-z0-9]"), "")
                if (normalizedEmailFromKey == normalizedEmailClean || 
                    emailFromKey.replace("_", "@") == normalizedEmail) {
                    Timber.d("🔍 Tentando chave similar: '$existingKey'")
                    val passwordSimilar = prefs.getString(existingKey, null)
                    if (passwordSimilar != null && passwordSimilar.isNotBlank()) {
                        Timber.d("✅ Senha encontrada com chave similar, migrando...")
                        savePassword(normalizedEmail, passwordSimilar)
                        return passwordSimilar
                    }
                }
            }
            
            // Tentar com email original (caso não tenha sido normalizado)
            if (email != normalizedEmail) {
                val keyOriginal = generateKey(email)
                Timber.d("🔍 Tentando com email original: '$email' (key: '$keyOriginal')")
                val passwordOriginal = prefs.getString(keyOriginal, null)
                if (passwordOriginal != null && passwordOriginal.isNotBlank()) {
                    Timber.d("✅ Senha encontrada com email original, migrando...")
                    savePassword(normalizedEmail, passwordOriginal)
                    return passwordOriginal
                }
            }
            
            Timber.e("❌ Senha não encontrada após todas as tentativas para: '$normalizedEmail'")
            null
        } catch (e: AEADBadTagException) {
            Timber.e(e, "❌ Erro de criptografia ao obter senha (dados corrompidos)")
            isCorrupted = true
            clearCorruptedData()
            null
        } catch (e: Exception) {
            // Verificar se é KeyStoreException pela mensagem ou nome da classe
            val isKeyStoreException = e.javaClass.simpleName == "KeyStoreException" ||
                e.message?.contains("VERIFICATION_FAILED") == true ||
                e.message?.contains("Signature/MAC verification failed") == true
            
            if (isKeyStoreException) {
                Timber.e(e, "❌ Erro de verificação ao obter senha (dados corrompidos)")
                isCorrupted = true
                clearCorruptedData()
            } else {
                Timber.e(e, "❌ Erro ao obter senha descriptografada")
            }
            null
        }
    }
    
    /**
     * Remove a senha salva para um email específico
     */
    fun removePassword(email: String) {
        try {
            val normalizedEmail = email.trim().lowercase()
            val key = generateKey(normalizedEmail)
            Timber.d("🗑️ Removendo senha para email: $normalizedEmail (key: $key)")
            val prefs = getEncryptedPrefs()
            prefs.edit()
                .remove(key)
                .apply()
            Timber.d("🗑️ Senha removida para: $normalizedEmail")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao remover senha")
        }
    }
    
    /**
     * Limpa todas as senhas salvas (logout)
     */
    fun clearAllPasswords() {
        try {
            val prefs = getEncryptedPrefs()
            prefs.edit()
                .clear()
                .apply()
            Timber.d("🗑️ Todas as senhas foram removidas")
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao limpar senhas")
            // Se falhar, tentar limpar dados corrompidos
            clearCorruptedData()
        }
    }
}

