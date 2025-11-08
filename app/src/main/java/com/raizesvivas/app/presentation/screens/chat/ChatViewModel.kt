package com.raizesvivas.app.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.local.ChatPreferences
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.MensagemChat
import com.raizesvivas.app.domain.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * ViewModel para gerenciar o chat
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatPreferences: ChatPreferences,
    private val usuarioRepository: UsuarioRepository,
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()
    
    val currentUserId: String?
        get() = authService.currentUser?.uid
    
    val currentUserNome: StateFlow<String> = flow {
        val userId = currentUserId
        if (userId != null) {
            val usuario = usuarioRepository.buscarPorId(userId)
            emit(usuario?.nome?.takeIf { it.isNotBlank() } ?: "Usuário")
        } else {
            emit("Usuário")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Usuário"
    )
    
    // Lista de todos os usuários cadastrados
    val usuarios: StateFlow<List<Usuario>> = flow {
        val userId = currentUserId
        val resultado = usuarioRepository.buscarTodosUsuarios()
        resultado.onSuccess { lista ->
            emit(lista.filter { it.id != userId }) // Excluir o próprio usuário
        }.onFailure {
            Timber.e(it, "❌ Erro ao buscar usuários")
            emit(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Mensagens da conversa atual
    val mensagens: StateFlow<List<MensagemChat>> = combine(
        _state.map { it.destinatarioId },
        flow {
            emit(currentUserId)
        }
    ) { destinatarioId, remetenteId ->
        if (destinatarioId != null && remetenteId != null) {
            chatPreferences.observarMensagens(remetenteId, destinatarioId)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        carregarUsuarios()
        atualizarNomeRemetente()
    }
    
    private fun atualizarNomeRemetente() {
        viewModelScope.launch {
            currentUserNome.collect { nome ->
                _state.update { it.copy(remetenteNome = nome) }
            }
        }
    }
    
    private fun carregarUsuarios() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val resultado = usuarioRepository.buscarTodosUsuarios()
                resultado.onSuccess {
                    _state.update { it.copy(isLoading = false) }
                }.onFailure { error ->
                    Timber.e(error, "❌ Erro ao carregar usuários")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao carregar contatos: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar usuários")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar contatos: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun abrirConversa(destinatarioId: String, destinatarioNome: String) {
        _state.update { 
            it.copy(
                destinatarioId = destinatarioId,
                destinatarioNome = destinatarioNome,
                mostrarConversa = true
            )
        }
        
        // Marcar mensagens como lidas
        viewModelScope.launch {
            val remetenteId = currentUserId
            if (remetenteId != null) {
                chatPreferences.marcarMensagensComoLidas(destinatarioId, remetenteId)
            }
        }
    }
    
    fun fecharConversa() {
        _state.update { 
            it.copy(
                mostrarConversa = false,
                destinatarioId = null,
                destinatarioNome = null
            )
        }
    }
    
    fun enviarMensagem(texto: String) {
        val remetenteId = currentUserId
        val destinatarioId = _state.value.destinatarioId
        val remetenteNome = _state.value.remetenteNome
        val destinatarioNome = _state.value.destinatarioNome
        
        if (remetenteId == null || destinatarioId == null || texto.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            try {
                val mensagem = MensagemChat(
                    id = UUID.randomUUID().toString(),
                    remetenteId = remetenteId,
                    remetenteNome = remetenteNome,
                    destinatarioId = destinatarioId,
                    destinatarioNome = destinatarioNome,
                    texto = texto.trim(),
                    enviadoEm = Date(),
                    lida = false
                )
                
                chatPreferences.salvarMensagem(mensagem)
                Timber.d("💬 Mensagem enviada: ${mensagem.id}")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao enviar mensagem")
                _state.update { 
                    it.copy(erro = "Erro ao enviar mensagem: ${e.message}")
                }
            }
        }
    }
    
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
}

/**
 * Estado da tela de chat
 */
data class ChatState(
    val isLoading: Boolean = false,
    val erro: String? = null,
    val mostrarConversa: Boolean = false,
    val destinatarioId: String? = null,
    val destinatarioNome: String? = null,
    val remetenteNome: String = "Usuário"
)

