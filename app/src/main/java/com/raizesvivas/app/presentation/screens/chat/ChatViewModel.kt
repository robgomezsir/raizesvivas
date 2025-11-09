package com.raizesvivas.app.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.ChatRepository
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.MensagemChat
import com.raizesvivas.app.domain.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * ViewModel para gerenciar o estado e lógica do chat
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authService: AuthService
) : ViewModel() {

    // Estado interno
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    // ID do usuário atual
    val currentUserId: String?
        get() = authService.currentUser?.uid

    // Nome do usuário atual
    private val _currentUserNome = MutableStateFlow<String>("Usuário")
    val currentUserNome: StateFlow<String> = _currentUserNome.asStateFlow()

    // Lista de usuários disponíveis para chat
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    // Mensagens da conversa atual (sincronizadas em tempo real via Firestore)
    val mensagens: StateFlow<List<MensagemChat>> = combine(
        _state.map { it.destinatarioId },
        flow { emit(currentUserId) }
    ) { destinatarioId: String?, remetenteId: String? ->
        when {
            destinatarioId != null && remetenteId != null -> {
                chatRepository.observarMensagens(
                    remetenteId = remetenteId,
                    destinatarioId = destinatarioId
                )
            }
            else -> {
                flowOf(emptyList())
            }
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

    /**
     * Atualiza o nome do remetente (usuário atual)
     */
    private fun atualizarNomeRemetente() {
        viewModelScope.launch {
            val userId = currentUserId
            if (userId != null) {
                try {
                    val usuario = usuarioRepository.buscarPorId(userId)
                    val nome = usuario?.nome?.takeIf { it.isNotBlank() } ?: "Usuário"
                    _currentUserNome.value = nome
                    _state.update { it.copy(remetenteNome = nome) }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao buscar nome do usuário")
                    _currentUserNome.value = "Usuário"
                    _state.update { it.copy(remetenteNome = "Usuário") }
                }
            } else {
                _currentUserNome.value = "Usuário"
                _state.update { it.copy(remetenteNome = "Usuário") }
            }
        }
    }

    /**
     * Carrega a lista de usuários disponíveis para chat
     */
    private fun carregarUsuarios() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val userId = currentUserId
                val resultado = usuarioRepository.buscarTodosUsuarios()

                resultado.onSuccess { lista ->
                    val listaFiltrada = if (userId != null) {
                        lista.filter { it.id != userId }
                    } else {
                        lista
                    }
                    _usuarios.value = listaFiltrada
                    _state.update { it.copy(isLoading = false) }
                }.onFailure { error ->
                    Timber.e(error, "❌ Erro ao carregar usuários")
                    _usuarios.value = emptyList()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao carregar contatos: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar usuários")
                _usuarios.value = emptyList()
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar contatos: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Abre uma conversa com um destinatário
     */
    fun abrirConversa(destinatarioId: String, destinatarioNome: String) {
        _state.update {
            it.copy(
                destinatarioId = destinatarioId,
                destinatarioNome = destinatarioNome,
                mostrarConversa = true
            )
        }

        val remetenteId = currentUserId
        if (remetenteId != null) {
            viewModelScope.launch {
                try {
                    chatRepository.marcarMensagensComoLidas(
                        remetenteId = remetenteId,
                        destinatarioId = destinatarioId
                    )
                } catch (e: Exception) {
                    Timber.e(e, "❌ Erro ao marcar mensagens como lidas")
                }
            }
        }
    }

    /**
     * Fecha a conversa atual
     */
    fun fecharConversa() {
        _state.update {
            it.copy(
                mostrarConversa = false,
                destinatarioId = null,
                destinatarioNome = null
            )
        }
    }

    /**
     * Envia uma mensagem
     */
    fun enviarMensagem(texto: String) {
        val textoLimpo = texto.trim()
        if (textoLimpo.isBlank()) {
            return
        }

        val remetenteId = currentUserId
        val stateAtual = _state.value
        val destinatarioId = stateAtual.destinatarioId
        val destinatarioNome = stateAtual.destinatarioNome
        val remetenteNome = stateAtual.remetenteNome

        if (remetenteId == null) {
            _state.update { it.copy(erro = "Usuário não autenticado") }
            return
        }

        if (destinatarioId == null || destinatarioNome == null) {
            _state.update { it.copy(erro = "Destinatário não selecionado") }
            return
        }

        val mensagem = MensagemChat(
            id = UUID.randomUUID().toString(),
            remetenteId = remetenteId,
            remetenteNome = remetenteNome,
            destinatarioId = destinatarioId,
            destinatarioNome = destinatarioNome,
            texto = textoLimpo,
            enviadoEm = Date(),
            lida = false
        )

        viewModelScope.launch {
            try {
                val resultado = chatRepository.enviarMensagem(mensagem)
                resultado.onSuccess {
                    Timber.d("💬 Mensagem enviada com sucesso: ${mensagem.id}")
                }.onFailure { error ->
                    Timber.e(error, "❌ Erro ao enviar mensagem")
                    _state.update {
                        it.copy(erro = "Erro ao enviar mensagem: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao enviar mensagem")
                _state.update {
                    it.copy(erro = "Erro ao enviar mensagem: ${e.message}")
                }
            }
        }
    }

    /**
     * Limpa mensagens de erro
     */
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
    
    /**
     * Limpa todas as mensagens da conversa atual
     */
    fun limparMensagensConversa() {
        val remetenteId = currentUserId
        val destinatarioId = _state.value.destinatarioId
        
        if (remetenteId == null || destinatarioId == null) {
            _state.update { it.copy(erro = "Não é possível limpar mensagens: conversa não encontrada") }
            return
        }
        
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null, mostrarModalLimparMensagens = false) }
                val resultado = chatRepository.limparMensagensConversa(remetenteId, destinatarioId)
                resultado.onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    Timber.d("✅ Mensagens da conversa limpas com sucesso")
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao limpar mensagens: ${error.message}"
                        )
                    }
                    Timber.e(error, "❌ Erro ao limpar mensagens da conversa")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao limpar mensagens da conversa")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao limpar mensagens: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun abrirModalLimparMensagens() {
        _state.update { it.copy(mostrarModalLimparMensagens = true) }
    }
    
    fun fecharModalLimparMensagens() {
        _state.update { it.copy(mostrarModalLimparMensagens = false) }
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
    val remetenteNome: String = "Usuário",
    val mostrarModalLimparMensagens: Boolean = false
)
