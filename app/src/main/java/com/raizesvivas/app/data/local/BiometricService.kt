package com.raizesvivas.app.data.local

import android.app.Activity
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Serviço para gerenciar autenticação biométrica
 * 
 * Suporta:
 * - Impressão digital
 * - Reconhecimento facial
 * - Iris (quando disponível)
 */
@Singleton
class BiometricService @Inject constructor(
    private val context: Context
) {
    
    /**
     * Verifica se o dispositivo possui suporte para autenticação biométrica
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Obtém o status da autenticação biométrica
     */
    fun getBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
            else -> BiometricStatus.UNAVAILABLE
        }
    }
    
    /**
     * Autentica usando biometria
     * 
     * @param activity Activity necessária para exibir o BiometricPrompt
     * @param title Título do diálogo de autenticação
     * @param subtitle Subtítulo do diálogo (opcional)
     * @param negativeButtonText Texto do botão negativo (padrão: "Cancelar")
     * @return Result com sucesso ou erro
     */
    suspend fun authenticate(
        activity: Activity,
        title: String = "Autenticação Biométrica",
        subtitle: String? = "Use sua impressão digital ou rosto para entrar",
        negativeButtonText: String = "Cancelar"
    ): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            if (!isBiometricAvailable()) {
                continuation.resume(Result.failure(Exception("Biometria não disponível")))
                return@suspendCancellableCoroutine
            }
            
            // BiometricPrompt requer FragmentActivity
            val fragmentActivity = activity as? FragmentActivity
            if (fragmentActivity == null) {
                continuation.resume(Result.failure(Exception("Activity precisa ser FragmentActivity para usar BiometricPrompt")))
                return@suspendCancellableCoroutine
            }
            
            val executor = ContextCompat.getMainExecutor(context)
            
            val biometricPrompt = BiometricPrompt(
                fragmentActivity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Timber.d("✅ Autenticação biométrica bem-sucedida")
                        continuation.resume(Result.success(Unit))
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Timber.e("❌ Erro na autenticação biométrica: $errString (code: $errorCode)")
                        
                        // Não considerar erro de cancelamento pelo usuário como falha crítica
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || 
                            errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_CANCELED) {
                            continuation.cancel()
                        } else {
                            continuation.resume(Result.failure(Exception(errString.toString())))
                        }
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Timber.w("⚠️ Autenticação biométrica falhou")
                        continuation.resume(Result.failure(Exception("Falha na autenticação biométrica")))
                    }
                }
            )
            
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .apply {
                    subtitle?.let { setSubtitle(it) }
                }
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .build()
            
            continuation.invokeOnCancellation {
                Timber.d("🔒 Autenticação biométrica cancelada")
            }
            
            biometricPrompt.authenticate(promptInfo)
        }
    }
}

/**
 * Status da autenticação biométrica
 */
enum class BiometricStatus {
    AVAILABLE,              // Disponível e pronto para uso
    NO_HARDWARE,            // Dispositivo não possui hardware biométrico
    HARDWARE_UNAVAILABLE,   // Hardware biométrico não está disponível
    NONE_ENROLLED,          // Nenhuma biometria cadastrada
    UNAVAILABLE             // Indisponível por outro motivo
}

