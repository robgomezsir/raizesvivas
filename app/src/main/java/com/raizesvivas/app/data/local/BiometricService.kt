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
     * Segue as melhores práticas da documentação oficial do Android:
     * https://developer.android.com/training/sign-in/biometric-auth
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
            Timber.d("🔐 BiometricService.authenticate chamado")
            
            // Verificar disponibilidade antes de prosseguir
            val biometricStatus = getBiometricStatus()
            if (biometricStatus != BiometricStatus.AVAILABLE) {
                val errorMsg = when (biometricStatus) {
                    BiometricStatus.NO_HARDWARE -> "Dispositivo não possui hardware biométrico"
                    BiometricStatus.HARDWARE_UNAVAILABLE -> "Hardware biométrico não está disponível"
                    BiometricStatus.NONE_ENROLLED -> "Nenhuma biometria cadastrada no dispositivo"
                    else -> "Biometria não disponível"
                }
                Timber.e("❌ $errorMsg")
                continuation.resume(Result.failure(Exception(errorMsg)))
                return@suspendCancellableCoroutine
            }
            
            // BiometricPrompt requer FragmentActivity
            val fragmentActivity = activity as? FragmentActivity
            if (fragmentActivity == null) {
                Timber.e("❌ Activity não é FragmentActivity: ${activity.javaClass.simpleName}")
                continuation.resume(Result.failure(Exception("Activity precisa ser FragmentActivity para usar BiometricPrompt")))
                return@suspendCancellableCoroutine
            }
            
            // Verificar se a Activity está no estado correto (não destruída)
            if (fragmentActivity.isFinishing || fragmentActivity.isDestroyed) {
                Timber.e("❌ Activity está finalizando ou destruída")
                continuation.resume(Result.failure(Exception("Activity não está disponível")))
                return@suspendCancellableCoroutine
            }
            
            Timber.d("🔐 Criando BiometricPrompt com FragmentActivity: ${fragmentActivity.javaClass.simpleName}")
            
            // Usar o executor da activity para garantir que está na thread principal
            // Segundo a documentação oficial, o executor deve ser do MainThread
            val executor = ContextCompat.getMainExecutor(fragmentActivity)
            
            // Criar callback antes de construir o prompt
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Timber.d("✅ Autenticação biométrica bem-sucedida")
                    // Verificar se a corrotina ainda está ativa antes de resumir
                    if (continuation.isActive) {
                        continuation.resume(Result.success(Unit))
                        Timber.d("✅ Corrotina resumida com sucesso")
                    } else {
                        Timber.w("⚠️ Corrotina não está mais ativa - ignorando resultado")
                    }
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val errorMessage = "$errString (code: $errorCode)"
                    Timber.e("❌ Erro na autenticação biométrica: $errorMessage")
                    
                    // Não considerar erro de cancelamento pelo usuário como falha crítica
                    val isCanceled = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || 
                                    errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                    errorCode == BiometricPrompt.ERROR_CANCELED ||
                                    errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS
                    
                    if (isCanceled) {
                        Timber.d("🔐 Autenticação cancelada pelo usuário ou sem biometria (code: $errorCode)")
                        if (continuation.isActive) {
                            continuation.cancel()
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception(errorMessage)))
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Timber.w("⚠️ Autenticação biométrica falhou - usuário pode tentar novamente")
                    // Não fazer nada aqui - o prompt permite tentar novamente automaticamente
                    // Este método é chamado quando a biometria não corresponde, mas o prompt continua ativo
                }
            }
            
            val biometricPrompt = BiometricPrompt(
                fragmentActivity,
                executor,
                callback
            )
            
            // Construir PromptInfo seguindo as melhores práticas
            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setNegativeButtonText(negativeButtonText)
            
            // Adicionar subtitle apenas se fornecido
            subtitle?.let { 
                promptInfoBuilder.setSubtitle(it)
            }
            
            // Configurar authenticators permitidos
            // BIOMETRIC_STRONG é preferível, mas aceitamos WEAK também para compatibilidade
            promptInfoBuilder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            
            val promptInfo = promptInfoBuilder.build()
            
            // Configurar cancelamento
            continuation.invokeOnCancellation {
                Timber.d("🔒 Autenticação biométrica cancelada (corrotina cancelada)")
                // Não precisamos fazer nada aqui - o BiometricPrompt gerencia seu próprio lifecycle
            }
            
            Timber.d("🔐 Exibindo BiometricPrompt")
            // Segundo a documentação, authenticate() deve ser chamado na thread principal
            // Mas como já estamos usando MainExecutor, podemos chamar diretamente se já estivermos na main thread
            // Para segurança, vamos usar runOnUiThread
            try {
                if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                    // Já estamos na thread principal
                    biometricPrompt.authenticate(promptInfo)
                    Timber.d("🔐 BiometricPrompt.authenticate chamado com sucesso (main thread)")
                } else {
                    // Precisamos mudar para a thread principal
                    fragmentActivity.runOnUiThread {
                        try {
                            biometricPrompt.authenticate(promptInfo)
                            Timber.d("🔐 BiometricPrompt.authenticate chamado com sucesso (via runOnUiThread)")
                        } catch (e: Exception) {
                            Timber.e(e, "❌ Erro ao exibir BiometricPrompt no runOnUiThread")
                            if (continuation.isActive) {
                                continuation.resume(Result.failure(e))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao exibir BiometricPrompt")
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }
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

