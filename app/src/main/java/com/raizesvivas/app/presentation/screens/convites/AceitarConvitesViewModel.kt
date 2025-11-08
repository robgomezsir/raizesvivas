package com.raizesvivas.app.presentation.screens.convites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.ConviteRepository
import com.raizesvivas.app.domain.model.Convite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para aceitação de convites
 * 
 * Gerencia convites pendentes do usuário atual
 */
@HiltViewModel
class AceitarConvitesViewModel @Inject constructor(
    private val conviteRepository: ConviteRepository,
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(AceitarConvitesState())
    val state = _state.asStateFlow()
    
    private val _convitesPendentes = MutableStateFlow<List<Convite>>(emptyList())
    val convitesPendentes = _convitesPendentes.asStateFlow()
    
    init {
        observarConvitesPendentes()
    }
    
    /**
     * Observa convites pendentes em tempo real
     */
    private fun observarConvitesPendentes() {
        viewModelScope.launch {
            conviteRepository.observarConvitesPendentes()
                .catch { error ->
                    Timber.e(error, "❌ Erro ao observar convites")
                    _state.update { 
                        it.copy(erro = "Erro ao carregar convites: ${error.message}")
                    }
                }
                .collect { convites ->
                    _convitesPendentes.value = convites
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
    
    /**
     * Aceita um convite
     */
    fun aceitarConvite(conviteId: String, pessoaId: String? = null) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val resultado = conviteRepository.aceitarConvite(
                    conviteId = conviteId,
                    pessoaId = pessoaId
                )
                
                resultado.onSuccess {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            sucesso = "Convite aceito com sucesso!"
                        )
                    }
                    
                    // Limpar mensagem após 3 segundos
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        _state.update { it.copy(sucesso = null) }
                    }
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao aceitar convite: ${error.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao aceitar convite")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao aceitar convite: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Rejeita um convite
     */
    fun rejeitarConvite(conviteId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val resultado = conviteRepository.rejeitarConvite(conviteId)
                
                resultado.onSuccess {
                    _state.update { it.copy(isLoading = false) }
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao rejeitar convite: ${error.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao rejeitar convite")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao rejeitar convite: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Limpa mensagem de erro
     */
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
}

/**
 * Estado da tela de aceitar convites
 */
data class AceitarConvitesState(
    val isLoading: Boolean = true,
    val erro: String? = null,
    val sucesso: String? = null
)

