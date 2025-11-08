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
                val email = user.email ?: _state.value.email
                // Salvar email para biometria
                biometricPreferences.saveLastEmail(email)
                // Se biometria está disponível mas não habilitada, habilitar automaticamente e salvar senha
                val shouldEnableBiometric = _state.value.biometricAvailable && !_state.value.biometricEnabled
                if (shouldEnableBiometric) {
                    biometricPreferences.setBiometricEnabled(email, true)
                    biometricCrypto.savePassword(email, _state.value.senha)
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            loginSuccess = true,
                            biometricEnabled = true
                        )
                    }
                } else if (_state.value.biometricEnabled) {
                    // Se já estava habilitada, apenas atualizar senha se necessário
                    biometricCrypto.savePassword(email, _state.value.senha)
                    _state.update { it.copy(isLoading = false, loginSuccess = true) }
                } else {
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
        
        viewModelScope.launch {
            Timber.d("🔐 Chamando biometricService.authenticate")
            val result = biometricService.authenticate(
                activity = activity,
                title = "Entrar com Biometria",
                subtitle = "Use sua impressão digital ou rosto para entrar",
                negativeButtonText = "Usar senha"
            )
            
            result.onSuccess {
                Timber.d("✅ Biometria autenticada com sucesso")
                // Após biometria, fazer login automático com email e senha salvos
                val savedPassword = biometricCrypto.getPassword(lastEmail)
                if (savedPassword != null) {
                    _state.update { 
                        it.copy(
                            email = lastEmail,
                            senha = savedPassword
                        )
                    }
                    // Fazer login automaticamente
                    login()
                } else {
                    // Se não houver senha salva, apenas preenche o email
                    _state.update { it.copy(email = lastEmail) }
                    onBiometricSuccess()
                }
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ Erro na autenticação biométrica")
                if (error.message != null && !error.message!!.contains("cancel")) {
                    _state.update { it.copy(error = "Erro na autenticação biométrica") }
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

