package com.raizesvivas.app.presentation.screens.amigo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.AmigoRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.Amigo
import com.raizesvivas.app.domain.model.Pessoa
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel para a tela de Adicionar Amigo
 */
@HiltViewModel
class AdicionarAmigoViewModel @Inject constructor(
    private val authService: AuthService,
    private val amigoRepository: AmigoRepository,
    private val pessoaRepository: PessoaRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AdicionarAmigoState())
    val state = _state.asStateFlow()
    
    // Lista de pessoas para seleção
    val pessoasDisponiveis: StateFlow<List<Pessoa>> = pessoaRepository
        .observarTodasPessoas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun atualizarNome(nome: String) {
        _state.update { it.copy(nome = nome) }
    }
    
    fun atualizarTelefone(telefone: String) {
        _state.update { it.copy(telefone = telefone) }
    }
    
    fun atualizarFamiliarSelecionado(familiarId: String?) {
        _state.update { it.copy(familiarSelecionado = familiarId) }
    }
    
    fun salvarAmigo() {
        val currentState = _state.value
        val usuarioAtual = authService.currentUser
        
        if (usuarioAtual == null) {
            _state.update { it.copy(erro = "Usuário não autenticado") }
            return
        }
        
        if (currentState.nome.isBlank()) {
            _state.update { it.copy(erro = "Nome é obrigatório") }
            return
        }
        
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val familiaresVinculados = if (currentState.familiarSelecionado != null) {
                    listOf(currentState.familiarSelecionado!!)
                } else {
                    emptyList()
                }
                
                val amigo = Amigo(
                    id = UUID.randomUUID().toString(),
                    nome = currentState.nome.trim(),
                    telefone = currentState.telefone.takeIf { it.isNotBlank() },
                    familiaresVinculados = familiaresVinculados,
                    criadoPor = usuarioAtual.uid,
                    criadoEm = Date(),
                    modificadoEm = Date()
                )
                
                val resultado = amigoRepository.salvar(amigo)
                
                resultado.onSuccess {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            sucesso = true,
                            nome = "",
                            telefone = "",
                            familiarSelecionado = null
                        ) 
                    }
                    Timber.d("✅ Amigo salvo com sucesso: ${amigo.nome}")
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao salvar amigo: ${error.message}"
                        ) 
                    }
                    Timber.e(error, "❌ Erro ao salvar amigo")
                }
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro inesperado: ${e.message}"
                    ) 
                }
                Timber.e(e, "❌ Erro inesperado ao salvar amigo")
            }
        }
    }
    
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
    
    fun limparSucesso() {
        _state.update { it.copy(sucesso = false) }
    }
}

data class AdicionarAmigoState(
    val nome: String = "",
    val telefone: String = "",
    val familiarSelecionado: String? = null,
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false
)

