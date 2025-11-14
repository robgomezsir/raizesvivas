package com.raizesvivas.app.presentation.screens.mural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.RecadoRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.data.repository.ChatRepository
import com.raizesvivas.app.domain.model.Recado
import java.util.Date
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para a tela do Mural de Recados
 */
@HiltViewModel
class MuralViewModel @Inject constructor(
    private val recadoRepository: RecadoRepository,
    private val pessoaRepository: PessoaRepository,
    private val authService: AuthService,
    private val usuarioRepository: UsuarioRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(MuralState())
    val state = _state.asStateFlow()
    
    // UserId atual (para verificar se pode deletar recados)
    val currentUserId: String?
        get() = authService.currentUser?.uid
    
    // Verificar se usuário é admin
    val isAdmin: StateFlow<Boolean> = flow {
        val currentUser = authService.currentUser
        if (currentUser != null) {
            val usuario = usuarioRepository.buscarPorId(currentUser.uid)
            emit(usuario?.ehAdministrador == true)
        } else {
            emit(false)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    // Observar recados em tempo real
    val recados: StateFlow<List<Recado>> = recadoRepository.observarRecados()
        .catch { error ->
            Timber.e(error, "Erro ao observar recados")
            _state.update { it.copy(erro = "Erro ao carregar recados: ${error.message}") }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Observar pessoas para seleção de destinatário
    val pessoas: StateFlow<List<com.raizesvivas.app.domain.model.Pessoa>> = 
        pessoaRepository.observarTodasPessoas()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    init {
        carregarDados()
        observarMensagensNaoLidas()
    }
    
    private fun carregarDados() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                // A lógica de filtro de recados será feita no FirestoreService
                // baseado na pessoa vinculada do usuário
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar dados do mural")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar dados: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun criarRecado(
        titulo: String,
        mensagem: String,
        destinatarioId: String? = null,
        cor: String = "primary"
    ) {
        viewModelScope.launch {
            try {
                // Validação básica
                if (titulo.isBlank() && mensagem.isBlank()) {
                    _state.update { 
                        it.copy(
                            erro = "Título ou mensagem deve ser preenchido"
                        )
                    }
                    return@launch
                }
                
                Timber.d("📝 Criando recado: título='$titulo', destinatarioId=$destinatarioId")
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val resultado = recadoRepository.criarRecado(
                    titulo = titulo.trim(),
                    mensagem = mensagem.trim(),
                    destinatarioId = destinatarioId,
                    cor = cor
                )
                
                resultado.onSuccess { recadoSalvo ->
                    Timber.d("✅ Recado criado com sucesso: ID=${recadoSalvo.id}")
                    // Fechar modal apenas após sucesso confirmado
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            mostrarModalNovoRecado = false,
                            erro = null
                        )
                    }
                    // O observeRecados vai atualizar automaticamente a lista
                }
                
                resultado.onFailure { error ->
                    Timber.e(error, "❌ Erro ao criar recado: ${error.message}")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao criar recado: ${error.message ?: "Erro desconhecido"}"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Exceção ao criar recado: ${e.message}")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao criar recado: ${e.message ?: "Erro desconhecido"}"
                    )
                }
            }
        }
    }
    
    fun deletarRecado(recadoId: String) {
        viewModelScope.launch {
            try {
                _state.update { 
                    it.copy(
                        isLoading = true,
                        erro = null,
                        mostrarModalExcluirRecado = null
                    )
                }
                
                val resultado = recadoRepository.deletarRecado(recadoId)
                
                resultado.onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    Timber.d("✅ Recado deletado com sucesso")
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao deletar recado: ${error.message}"
                        )
                    }
                    Timber.e(error, "❌ Erro ao deletar recado")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar recado")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao deletar recado: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun abrirModalNovoRecado() {
        _state.update { it.copy(mostrarModalNovoRecado = true) }
    }
    
    fun fecharModalNovoRecado() {
        _state.update { it.copy(mostrarModalNovoRecado = false) }
    }
    
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
    
    fun recarregar() {
        carregarDados()
    }
    
    fun fixarRecado(recadoId: String, fixado: Boolean, fixadoAte: Date? = null) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val resultado = recadoRepository.fixarRecado(recadoId, fixado, fixadoAte)
                
                resultado.onSuccess {
                    _state.update { it.copy(isLoading = false, mostrarModalFixarRecado = null) }
                    Timber.d("✅ Recado ${if (fixado) "fixado" else "desfixado"} com sucesso")
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao ${if (fixado) "fixar" else "desfixar"} recado: ${error.message}"
                        )
                    }
                    Timber.e(error, "❌ Erro ao fixar recado")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao fixar recado")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao fixar recado: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun abrirModalFixarRecado(recadoId: String) {
        _state.update { it.copy(mostrarModalFixarRecado = recadoId) }
    }
    
    fun fecharModalFixarRecado() {
        _state.update { it.copy(mostrarModalFixarRecado = null) }
    }

    fun abrirModalExcluirRecado(recadoId: String) {
        _state.update { it.copy(mostrarModalExcluirRecado = recadoId) }
    }
    
    fun fecharModalExcluirRecado() {
        _state.update { it.copy(mostrarModalExcluirRecado = null) }
    }
    
    fun curtirRecado(recadoId: String, curtir: Boolean) {
        viewModelScope.launch {
            try {
                val resultado = recadoRepository.curtirRecado(recadoId, curtir)
                
                resultado.onSuccess {
                    Timber.d("✅ Recado ${if (curtir) "curtido" else "descurtido"} com sucesso")
                }
                
                resultado.onFailure { error ->
                    Timber.e(error, "❌ Erro ao ${if (curtir) "curtir" else "descurtir"} recado")
                    _state.update { 
                        it.copy(
                            erro = "Erro ao ${if (curtir) "curtir" else "descurtir"} recado: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao ${if (curtir) "curtir" else "descurtir"} recado")
                _state.update { 
                    it.copy(
                        erro = "Erro ao ${if (curtir) "curtir" else "descurtir"} recado: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun observarMensagensNaoLidas() {
        viewModelScope.launch {
            chatRepository.observarMensagensNaoLidas()
                .distinctUntilChanged()
                .catch { error ->
                    Timber.e(error, "Erro ao observar mensagens não lidas do chat")
                }
                .collect { mapa ->
                    val total = mapa.values.sum()
                    if (_state.value.totalMensagensChatNaoLidas != total) {
                        _state.update { it.copy(totalMensagensChatNaoLidas = total) }
                    }
                }
        }
    }
}

/**
 * Estado da tela do Mural
 */
data class MuralState(
    val isLoading: Boolean = false,
    val erro: String? = null,
    val mostrarModalNovoRecado: Boolean = false,
    val mostrarModalFixarRecado: String? = null, // ID do recado que está sendo fixado
    val mostrarModalExcluirRecado: String? = null, // ID do recado que está sendo avaliado para exclusão
    val totalMensagensChatNaoLidas: Int = 0
)

