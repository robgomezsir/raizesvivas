package com.raizesvivas.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Usuario
import com.raizesvivas.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CadastroViewModel @Inject constructor(
    private val authService: AuthService,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(CadastroState())
    val state = _state.asStateFlow()
    
    fun onNomeCompletoChanged(nome: String) {
        _state.update { it.copy(nomeCompleto = nome, nomeCompletoError = null) }
    }
    
    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }
    
    fun onSenhaChanged(senha: String) {
        _state.update { it.copy(senha = senha, senhaError = null) }
    }
    
    fun onConfirmarSenhaChanged(confirmarSenha: String) {
        _state.update { it.copy(confirmarSenha = confirmarSenha, confirmarSenhaError = null) }
    }
    
    fun cadastrar() {
        _state.update { it.copy(
            nomeCompletoError = null,
            emailError = null,
            senhaError = null,
            confirmarSenhaError = null,
            error = null
        ) }
        
        // Validar nome completo
        if (_state.value.nomeCompleto.isBlank()) {
            _state.update { it.copy(nomeCompletoError = "Nome completo é obrigatório") }
            return
        }
        
        // Validar email
        val emailValidation = ValidationUtils.validarEmail(_state.value.email)
        if (!emailValidation.isValid) {
            _state.update { it.copy(emailError = emailValidation.errorMessage) }
            return
        }
        
        // Validar senha
        val senhaValidation = ValidationUtils.validarSenha(_state.value.senha)
        if (!senhaValidation.isValid) {
            _state.update { it.copy(senhaError = senhaValidation.errorMessage) }
            return
        }
        
        // Validar confirmação de senha
        if (_state.value.senha != _state.value.confirmarSenha) {
            _state.update { it.copy(confirmarSenhaError = "As senhas não coincidem") }
            return
        }
        
        // Cadastrar
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            val result = authService.cadastrar(
                email = _state.value.email.trim(),
                senha = _state.value.senha,
                nomeCompleto = _state.value.nomeCompleto.trim()
            )
            
            result.onSuccess { user ->
                try {
                    // REGRA ESPECIAL: O primeiro cadastro de usuário (o mais antigo) SEMPRE será ADMIN SÊNIOR
                    // Verificar se é o primeiro usuário (nenhum admin existe ainda)
                    val ehPrimeiroUsuario = usuarioRepository.ehPrimeiroUsuario()
                    
                    // Criar documento do usuário no Firestore
                    val usuario = Usuario(
                        id = user.uid,
                        nome = _state.value.nomeCompleto.trim(),
                        email = _state.value.email.trim(),
                        ehAdministrador = ehPrimeiroUsuario, // Primeiro usuário = admin
                        ehAdministradorSenior = ehPrimeiroUsuario, // EXCEÇÃO: Primeiro usuário = ADMIN SÊNIOR automaticamente
                        primeiroAcesso = true,
                        criadoEm = Date()
                    )
                    
                    val resultadoSalvar = usuarioRepository.salvar(usuario)
                    
                    resultadoSalvar.onSuccess {
                        if (ehPrimeiroUsuario) {
                            Timber.d("✅ Primeiro usuário criado no Firestore: ${user.uid} (ADMIN SÊNIOR)")
                        } else {
                            Timber.d("✅ Usuário criado no Firestore: ${user.uid}")
                        }
                        _state.update { it.copy(isLoading = false, cadastroSuccess = true) }
                    }
                    
                    resultadoSalvar.onFailure { error ->
                        Timber.e(error, "❌ Erro ao salvar usuário no Firestore")
                        // Usar a mensagem de erro mais específica se disponível
                        val errorMessage = error.message ?: "Erro ao salvar dados do usuário. Tente novamente."
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                error = errorMessage
                            ) 
                        }
                    }
                    
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao criar usuário no Firestore")
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Erro ao criar perfil do usuário. Tente novamente."
                        ) 
                    }
                }
            }
            
            result.onFailure { error ->
                Timber.e(error, "❌ Erro no cadastro")
                // Usar a mensagem de erro já formatada pelo AuthService
                val errorMessage = error.message ?: when {
                    error.message?.contains("email-already-in-use") == true ||
                    error.message?.contains("EMAIL_EXISTS") == true ||
                    error.message?.contains("already in use") == true -> {
                        "Este email já está cadastrado. Faça login em vez de criar uma nova conta."
                    }
                    error.message?.contains("weak-password") == true -> "Senha muito fraca. Use pelo menos 6 caracteres."
                    error.message?.contains("invalid-email") == true -> "Email inválido. Verifique o formato do email."
                    error.message?.contains("network") == true -> "Erro de conexão. Verifique sua internet"
                    else -> "Erro ao criar conta. Tente novamente"
                }
                _state.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }
}

data class CadastroState(
    val nomeCompleto: String = "",
    val email: String = "",
    val senha: String = "",
    val confirmarSenha: String = "",
    val nomeCompletoError: String? = null,
    val emailError: String? = null,
    val senhaError: String? = null,
    val confirmarSenhaError: String? = null,
    val isLoading: Boolean = false,
    val cadastroSuccess: Boolean = false,
    val error: String? = null
)

