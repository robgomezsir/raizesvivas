package com.raizesvivas.app.presentation.screens.edicoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.EdicaoPendenteRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.EdicaoPendente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GerenciarEdicoesViewModel @Inject constructor(
    private val edicaoPendenteRepository: EdicaoPendenteRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(GerenciarEdicoesState())
    val state = _state.asStateFlow()
    
    private val _edicoesPendentes = MutableStateFlow<List<EdicaoPendente>>(emptyList())
    val edicoesPendentes = _edicoesPendentes.asStateFlow()
    
    init {
        verificarPermissoes()
        observarEdicoes()
    }
    
    private fun verificarPermissoes() {
        viewModelScope.launch {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                _state.update { it.copy(ehAdmin = false, erro = "Usuário não autenticado") }
                return@launch
            }
            
            val usuario = usuarioRepository.buscarPorId(currentUser.uid)
            val ehAdmin = usuario?.ehAdministrador ?: false
            
            _state.update { it.copy(ehAdmin = ehAdmin) }
            
            if (!ehAdmin) {
                _state.update { it.copy(erro = "Apenas administradores podem gerenciar edições") }
            }
        }
    }
    
    private fun observarEdicoes() {
        viewModelScope.launch {
            edicaoPendenteRepository.observarEdicoesPendentes()
                .catch { error ->
                    Timber.e(error, "Erro ao observar edições")
                    _state.update { it.copy(erro = "Erro ao carregar edições: ${error.message}") }
                }
                .collect { edicoes ->
                    _edicoesPendentes.value = edicoes
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
    
    fun aprovarEdicao(edicaoId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val resultado = edicaoPendenteRepository.aprovarEdicao(edicaoId)
                
                resultado.onSuccess {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            sucesso = "Edição aprovada com sucesso!"
                        )
                    }
                    
                    // Limpar sucesso após 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _state.update { it.copy(sucesso = null) }
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao aprovar edição: ${error.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao aprovar edição")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao aprovar edição: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun rejeitarEdicao(edicaoId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val resultado = edicaoPendenteRepository.rejeitarEdicao(edicaoId)
                
                resultado.onSuccess {
                    _state.update { it.copy(isLoading = false) }
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao rejeitar edição: ${error.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao rejeitar edição")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao rejeitar edição: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
}

data class GerenciarEdicoesState(
    val isLoading: Boolean = true,
    val erro: String? = null,
    val sucesso: String? = null,
    val ehAdmin: Boolean = false
)

