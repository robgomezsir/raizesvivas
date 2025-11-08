package com.raizesvivas.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RecuperarSenhaViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(RecuperarSenhaState())
    val state = _state.asStateFlow()
    
    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null, error = null) }
    }
    
    fun enviarEmail() {
        _state.update { it.copy(emailError = null, error = null) }
        
        // Validar email
        val emailValidation = ValidationUtils.validarEmail(_state.value.email)
        if (!emailValidation.isValid) {
            _state.update { it.copy(emailError = emailValidation.errorMessage) }
            return
        }
        
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            val result = authService.recuperarSenha(_state.value.email.trim())
            
            result.onSuccess {
                Timber.d("✅ Email de recuperação enviado")
                _state.update { it.copy(isLoading = false, enviado = true) }
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ Erro ao enviar email de recuperação")
                val errorMessage = when {
                    error.message?.contains("user-not-found") == true -> "Este email não está cadastrado"
                    error.message?.contains("network") == true -> "Erro de conexão. Verifique sua internet"
                    else -> "Erro ao enviar email. Tente novamente"
                }
                _state.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
}

data class RecuperarSenhaState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val enviado: Boolean = false,
    val error: String? = null
)

