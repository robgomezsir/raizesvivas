package com.raizesvivas.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.core.domain.repository.AuthRepository
import com.raizesvivas.feature.auth.presentation.viewmodel.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para autenticação
 * 
 * Gerencia o estado de autenticação e as operações
 * de login, registro e logout.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<AuthState>(AuthState.Initial)
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    /**
     * Verifica o estado atual de autenticação
     */
    private fun checkAuthState() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            
            try {
                val isSignedIn = authRepository.isSignedIn()
                if (isSignedIn) {
                    val userResult = authRepository.getCurrentUser()
                    if (userResult.isSuccess) {
                        _state.value = AuthState.Authenticated(userResult.getOrNull())
                    } else {
                        _state.value = AuthState.Unauthenticated
                    }
                } else {
                    _state.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Realiza login do usuário
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            
            try {
                val result = authRepository.signIn(email, password)
                if (result.isSuccess) {
                    _state.value = AuthState.Authenticated(result.getOrNull())
                } else {
                    _state.value = AuthState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao fazer login"
                    )
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Registra um novo usuário
     */
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            
            try {
                val result = authRepository.signUp(email, password)
                if (result.isSuccess) {
                    _state.value = AuthState.Authenticated(result.getOrNull())
                } else {
                    _state.value = AuthState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao registrar usuário"
                    )
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Realiza logout do usuário
     */
    fun signOut() {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            
            try {
                val result = authRepository.signOut()
                if (result.isSuccess) {
                    _state.value = AuthState.Unauthenticated
                } else {
                    _state.value = AuthState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao fazer logout"
                    )
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}
