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
class RedefinirSenhaViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(RedefinirSenhaState())
    val state = _state.asStateFlow()
    
    fun setOobCode(oobCode: String) {
        _state.update { it.copy(oobCode = oobCode) }
    }
    
    fun onNovaSenhaChanged(senha: String) {
        _state.update { 
            it.copy(
                novaSenha = senha,
                senhaError = null,
                error = null
            ) 
        }
    }
    
    fun onConfirmarSenhaChanged(senha: String) {
        _state.update { 
            it.copy(
                confirmarSenha = senha,
                confirmarSenhaError = null,
                error = null
            ) 
        }
    }
    
    fun toggleSenhaVisibility() {
        _state.update { it.copy(senhaVisivel = !it.senhaVisivel) }
    }
    
    fun toggleConfirmarSenhaVisibility() {
        _state.update { it.copy(confirmarSenhaVisivel = !it.confirmarSenhaVisivel) }
    }
    
    fun redefinirSenha() {
        _state.update { it.copy(senhaError = null, confirmarSenhaError = null, error = null) }
        
        // Validar nova senha
        val senhaValidation = ValidationUtils.validarSenha(_state.value.novaSenha)
        if (!senhaValidation.isValid) {
            _state.update { it.copy(senhaError = senhaValidation.errorMessage) }
            return
        }
        
        // Validar confirmação de senha
        if (_state.value.novaSenha != _state.value.confirmarSenha) {
            _state.update { it.copy(confirmarSenhaError = "As senhas não coincidem") }
            return
        }
        
        val oobCode = _state.value.oobCode
        if (oobCode.isBlank()) {
            _state.update { it.copy(error = "Código de verificação inválido") }
            return
        }
        
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            val result = authService.confirmarRedefinicaoSenha(
                oobCode = oobCode,
                newPassword = _state.value.novaSenha.trim()
            )
            
            result.onSuccess {
                Timber.d("✅ Senha redefinida com sucesso")
                _state.update { it.copy(isLoading = false, senhaRedefinida = true) }
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ Erro ao redefinir senha")
                val errorMessage = when {
                    error.message?.contains("expired") == true -> "O link de redefinição expirou. Solicite um novo."
                    error.message?.contains("invalid") == true -> "Código de verificação inválido. Solicite um novo link."
                    error.message?.contains("network") == true -> "Erro de conexão. Verifique sua internet"
                    else -> "Erro ao redefinir senha. Tente novamente"
                }
                _state.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
}

data class RedefinirSenhaState(
    val oobCode: String = "",
    val novaSenha: String = "",
    val confirmarSenha: String = "",
    val senhaError: String? = null,
    val confirmarSenhaError: String? = null,
    val senhaVisivel: Boolean = false,
    val confirmarSenhaVisivel: Boolean = false,
    val isLoading: Boolean = false,
    val senhaRedefinida: Boolean = false,
    val error: String? = null
)

