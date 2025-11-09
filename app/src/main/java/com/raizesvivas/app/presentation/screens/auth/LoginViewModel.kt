package com.raizesvivas.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.local.BiometricCrypto
import com.raizesvivas.app.data.local.BiometricPreferences
import com.raizesvivas.app.data.local.BiometricService
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para a tela de Login
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val biometricService: BiometricService,
    private val biometricPreferences: BiometricPreferences,
    private val biometricCrypto: BiometricCrypto
) : ViewModel() {
    
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    
    init {
        // Carregar último email e verificar se biometria está disponível
        viewModelScope.launch {
            val lastEmail = biometricPreferences.getLastEmailSync()
            val biometricAvailable = biometricService.isBiometricAvailable()
            val biometricEnabled = biometricPreferences.isBiometricEnabledSync()
            
            Timber.d("🔐 Biometria - lastEmail: $lastEmail, available: $biometricAvailable, enabled: $biometricEnabled")
            
            _state.update {
                it.copy(
                    lastEmail = lastEmail,
                    biometricAvailable = biometricAvailable,
                    biometricEnabled = biometricEnabled && biometricAvailable
                )
            }
        }
    }
    
    /**
     * Atualiza o email digitado
     */
    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }
    
    /**
     * Atualiza a senha digitada
     */
    fun onSenhaChanged(senha: String) {
        _state.update { it.copy(senha = senha, senhaError = null) }
    }
    
    /**
     * Valida campos e faz login
     */
    fun login() {
        // Limpar erros anteriores
        _state.update { it.copy(emailError = null, senhaError = null, error = null) }
        
        // Validar email
        val emailValidation = ValidationUtils.validarEmail(_state.value.email)
        if (!emailValidation.isValid) {
            _state.update { it.copy(emailError = emailValidation.errorMessage) }
            return
        }
        
        // Validar senha
        if (_state.value.senha.isBlank()) {
            _state.update { it.copy(senhaError = "Senha é obrigatória") }
            return
        }
        
        // Fazer login
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            val result = authService.login(
                email = _state.value.email.trim(),
                senha = _state.value.senha
            )
            
            result.onSuccess { user ->
                Timber.d("✅ Login bem-sucedido: ${user.uid}")
                val email = (user.email ?: _state.value.email).trim().lowercase()
                Timber.d("🔐 Email normalizado para salvar: $email")
                
                // Salvar email para biometria
                biometricPreferences.saveLastEmail(email)
                
                // Verificar novamente se biometria está disponível (pode ter mudado desde init)
                val biometricAvailableNow = biometricService.isBiometricAvailable()
                
                // Sempre salvar senha se biometria estiver disponível
                // Isso garante que a senha estará disponível para login biométrico futuro
                if (biometricAvailableNow) {
                    Timber.d("🔐 Biometria disponível - salvando senha para login futuro")
                    Timber.d("🔐 Email usado para salvar senha: $email")
                    Timber.d("🔐 Senha tem ${_state.value.senha.length} caracteres")
                    biometricCrypto.savePassword(email, _state.value.senha)
                    
                    // Aguardar um pouco para garantir que a senha foi salva
                    kotlinx.coroutines.delay(200)
                    
                    // Verificar se foi salva corretamente
                    val savedPasswordCheck = biometricCrypto.getPassword(email)
                    if (savedPasswordCheck != null) {
                        Timber.d("✅ Confirmação: Senha foi salva e pode ser recuperada")
                    } else {
                        Timber.e("❌ ERRO CRÍTICO: Senha não pode ser recuperada após salvar!")
                    }
                    
                    // Se biometria não estava habilitada, habilitar automaticamente
                    if (!_state.value.biometricEnabled) {
                        Timber.d("🔐 Habilitando biometria automaticamente")
                        biometricPreferences.setBiometricEnabled(email, true)
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                loginSuccess = true,
                                biometricEnabled = true,
                                biometricAvailable = true
                            )
                        }
                    } else {
                        // Se já estava habilitada, apenas atualizar estado
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                loginSuccess = true,
                                biometricAvailable = true
                            ) 
                        }
                    }
                } else {
                    // Biometria não disponível - apenas fazer login
                    Timber.d("⚠️ Biometria não disponível - senha não será salva")
                    _state.update { it.copy(isLoading = false, loginSuccess = true) }
                }
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ Erro no login")
                val errorMessage = when {
                    error.message?.contains("password") == true -> "Senha incorreta"
                    error.message?.contains("user-not-found") == true -> "Usuário não encontrado"
                    error.message?.contains("network") == true -> "Erro de conexão. Verifique sua internet"
                    else -> "Erro ao fazer login. Tente novamente"
                }
                _state.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
    
    /**
     * Tenta fazer login usando biometria
     * 
     * @param activity Activity necessária para o BiometricPrompt
     * @param onBiometricSuccess Callback quando biometria é autenticada (ainda precisa fazer login)
     */
    fun loginWithBiometric(
        activity: android.app.Activity,
        onBiometricSuccess: () -> Unit
    ) {
        Timber.d("🔐 loginWithBiometric chamado")
        val lastEmail = _state.value.lastEmail
        if (lastEmail == null) {
            Timber.e("❌ Nenhum email salvo para biometria")
            _state.update { it.copy(error = "Nenhum email salvo para biometria") }
            return
        }
        
        // Verificar se biometria ainda está disponível
        if (!_state.value.biometricAvailable) {
            Timber.e("❌ Biometria não está mais disponível")
            _state.update { it.copy(error = "Biometria não está disponível") }
            return
        }
        
        viewModelScope.launch {
            Timber.d("🔐 Chamando biometricService.authenticate para email: $lastEmail")
            val result = biometricService.authenticate(
                activity = activity,
                title = "Entrar com Biometria",
                subtitle = "Use sua impressão digital ou rosto para entrar",
                negativeButtonText = "Usar senha"
            )
            
            result.onSuccess {
                Timber.d("✅ Biometria autenticada com sucesso")
                // Normalizar email para buscar senha
                val normalizedEmail = lastEmail.trim().lowercase()
                Timber.d("🔐 Email normalizado para buscar senha: $normalizedEmail")
                
                // Após biometria, fazer login automático com email e senha salvos
                val savedPassword = biometricCrypto.getPassword(normalizedEmail)
                if (savedPassword != null) {
                    Timber.d("🔐 Senha encontrada, fazendo login automático para: $normalizedEmail")
                    Timber.d("🔐 Senha recuperada tem ${savedPassword.length} caracteres")
                    // Atualizar estado primeiro
                    _state.update { 
                        it.copy(
                            email = normalizedEmail,
                            senha = savedPassword,
                            emailError = null,
                            senhaError = null,
                            error = null
                        )
                    }
                    // Aguardar um pouco para garantir que o estado foi atualizado
                    kotlinx.coroutines.delay(100)
                    // Fazer login automaticamente
                    Timber.d("🔐 Chamando login() após biometria")
                    login()
                } else {
                    Timber.w("⚠️ Senha não encontrada para email: $normalizedEmail")
                    Timber.w("⚠️ Tentando buscar com email original: $lastEmail")
                    // Tentar com email original também
                    val savedPasswordOriginal = biometricCrypto.getPassword(lastEmail)
                    if (savedPasswordOriginal != null) {
                        Timber.d("🔐 Senha encontrada com email original, fazendo login")
                        _state.update { 
                            it.copy(
                                email = lastEmail,
                                senha = savedPasswordOriginal,
                                emailError = null,
                                senhaError = null,
                                error = null
                            )
                        }
                        kotlinx.coroutines.delay(100)
                        login()
                    } else {
                        // Se não houver senha salva, apenas preenche o email
                        _state.update { 
                            it.copy(
                                email = lastEmail,
                                error = "Senha não encontrada. Por favor, faça login manualmente."
                            ) 
                        }
                        onBiometricSuccess()
                    }
                }
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ Erro na autenticação biométrica: ${error.message}")
                // Não mostrar erro se foi cancelado pelo usuário
                if (error.message != null && 
                    !error.message!!.contains("cancel", ignoreCase = true) &&
                    !error.message!!.contains("Cancel", ignoreCase = true)) {
                    _state.update { it.copy(error = "Erro na autenticação biométrica: ${error.message}") }
                } else {
                    Timber.d("🔐 Autenticação biométrica cancelada pelo usuário")
                }
            }
        }
    }
    
    /**
     * Habilita ou desabilita biometria para o email atual
     */
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val email = _state.value.email.ifBlank { _state.value.lastEmail }
            if (email != null) {
                biometricPreferences.setBiometricEnabled(email, enabled)
                _state.update { it.copy(biometricEnabled = enabled && biometricService.isBiometricAvailable()) }
            }
        }
    }
}

/**
 * Estado da tela de Login
 */
data class LoginState(
    val email: String = "",
    val senha: String = "",
    val emailError: String? = null,
    val senhaError: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null,
    val lastEmail: String? = null,
    val biometricAvailable: Boolean = false,
    val biometricEnabled: Boolean = false
)

