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
import kotlinx.coroutines.Job
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

    // Estado da conversa atual (mensagens + status de paginação)
    private val _conversa = MutableStateFlow(ConversaUiState())
    val conversa: StateFlow<ConversaUiState> = _conversa.asStateFlow()

    // Mapa de mensagens não lidas por contato (remetenteId -> quantidade)
    private val _mensagensNaoLidas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val mensagensNaoLidas: StateFlow<Map<String, Int>> = _mensagensNaoLidas.asStateFlow()
    
    // Job para controlar a coleta do Flow de mensagens
    private var conversaJob: Job? = null
    private var carregarUsuariosJob: Job? = null

    init {
        carregarUsuarios()
        atualizarNomeRemetente()
        observarMensagensNaoLidas()
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
    private fun carregarUsuarios(force: Boolean = false) {
        if (_state.value.isLoading && !force) return
        carregarUsuariosJob?.cancel()
        carregarUsuariosJob = viewModelScope.launch {
            if (_state.value.isLoading && !force) return@launch
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

    fun recarregarUsuarios() {
        carregarUsuarios(force = true)
    }
    
    /**
     * Observa mensagens não lidas destinadas ao usuário atual.
     */
    private fun observarMensagensNaoLidas() {
        viewModelScope.launch {
            chatRepository.observarMensagensNaoLidas()
                .distinctUntilChanged()
                .catch { error ->
                    Timber.e(error, "❌ Erro ao observar mensagens não lidas")
                }
                .collect { mapa ->
                    if (_mensagensNaoLidas.value != mapa) {
                        _mensagensNaoLidas.value = mapa
                        _state.update { it.copy(mensagensNaoLidas = mapa) }
                    }
                }
        }
    }

    fun iniciarConversa(
        usuarioId: String,
        contatoId: String,
        marcarRefresh: Boolean = false
    ) {
        Timber.d("🚀 iniciarConversa: usuarioId=$usuarioId, contatoId=$contatoId, refresh=$marcarRefresh")

        if (marcarRefresh) {
            _state.update { it.copy(isRefreshingConversa = true) }
            viewModelScope.launch {
                chatRepository.reiniciarConversa(usuarioId, contatoId)
            }
        } else {
            _conversa.value = ConversaUiState()
        }

        conversaJob?.cancel()
        conversaJob = viewModelScope.launch {
            chatRepository.observarConversa(usuarioId, contatoId)
                .collect { estado ->
                    _conversa.value = ConversaUiState(
                        mensagens = estado.mensagens,
                        isLoadingInicial = estado.isLoadingInicial,
                        isCarregandoMais = estado.isCarregandoMais,
                        possuiMaisAntigas = estado.possuiMaisAntigas
                    )
                    if (_state.value.isRefreshingConversa && !estado.isLoadingInicial && !estado.isCarregandoMais) {
                        _state.update { it.copy(isRefreshingConversa = false) }
                    }
                }
        }

        viewModelScope.launch {
            try {
                val outroUsuarioId = contatoId
                val usuarioAtualId = usuarioId
                chatRepository.marcarMensagensComoLidas(
                    remetenteId = outroUsuarioId,
                    destinatarioId = usuarioAtualId
                )
                Timber.d("✅ Mensagens marcadas como lidas")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao marcar mensagens como lidas")
            }
        }
    }
    
    /**
     * Abre uma conversa com um destinatário (compatibilidade com código existente)
     * Chama iniciarConversa() internamente
     */
    fun abrirConversa(destinatarioId: String, destinatarioNome: String) {
        Timber.d("💬 Abrindo conversa: destinatarioId=$destinatarioId, destinatarioNome=$destinatarioNome")
        
        _state.update {
            it.copy(
                destinatarioId = destinatarioId,
                destinatarioNome = destinatarioNome,
                mostrarConversa = true
            )
        }

        val remetenteId = currentUserId
        if (remetenteId != null) {
            // Usar iniciarConversa() para iniciar os listeners
            iniciarConversa(remetenteId, destinatarioId)
        } else {
            Timber.e("❌ Erro: remetenteId é null ao abrir conversa")
        }
    }

    fun recarregarConversa() {
        val remetenteId = currentUserId ?: return
        val destinatarioId = _state.value.destinatarioId ?: return
        iniciarConversa(remetenteId, destinatarioId, marcarRefresh = true)
    }

    fun carregarMensagensAntigas() {
        val remetenteId = currentUserId ?: return
        val destinatarioId = _state.value.destinatarioId ?: return
        viewModelScope.launch {
            chatRepository.carregarMensagensAntigas(remetenteId, destinatarioId)
        }
    }

    /**
     * Limpa a conversa e cancela os listeners
     * CRÍTICO: Deve ser chamado ao sair da tela para evitar memory leaks
     */
    fun limparConversa() {
        Timber.d("🧹 ChatViewModel.limparConversa: Cancelando listeners e limpando mensagens")
        
        // Cancelar job de coleta de mensagens
        conversaJob?.cancel()
        conversaJob = null
        _conversa.value = ConversaUiState()
        val remetenteId = currentUserId
        val destinatarioId = _state.value.destinatarioId
        if (remetenteId != null && destinatarioId != null) {
            chatRepository.pararObservacao(remetenteId, destinatarioId)
        }

        _state.update {
            it.copy(
                mostrarConversa = false,
                destinatarioId = null,
                destinatarioNome = null
            )
        }
        
        Timber.d("✅ Conversa limpa e listeners cancelados")
    }
    
    /**
     * Fecha a conversa atual (alias para limparConversa)
     */
    fun fecharConversa() {
        limparConversa()
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

        // Garantir que o timestamp seja sempre atual (não futuro)
        val timestampAtual = System.currentTimeMillis()
        val mensagem = MensagemChat(
            id = UUID.randomUUID().toString(),
            remetenteId = remetenteId,
            remetenteNome = remetenteNome,
            destinatarioId = destinatarioId,
            destinatarioNome = destinatarioNome,
            texto = textoLimpo,
            enviadoEm = Date(timestampAtual), // Usar timestamp atual explícito
            lida = false
        )
        
        Timber.d("📤 Enviando mensagem: remetenteId=$remetenteId, destinatarioId=$destinatarioId, timestamp=${mensagem.enviadoEm.time}")

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
     * Limpa todas as mensagens ENVIADAS pelo usuário na conversa atual
     * IMPORTANTE: Remove apenas mensagens enviadas pelo usuário, mantém mensagens recebidas
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
                    Timber.d("✅ Mensagens ENVIADAS da conversa limpas com sucesso")
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
    
    /**
     * Deleta uma mensagem específica (permite deletar mensagens recebidas)
     *
     * @param mensagemId ID da mensagem a ser deletada
     */
    fun deletarMensagem(mensagemId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val resultado = chatRepository.deletarMensagem(mensagemId)
                
                resultado.onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    Timber.d("✅ Mensagem $mensagemId deletada com sucesso")
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao deletar mensagem: ${error.message}"
                        )
                    }
                    Timber.e(error, "❌ Erro ao deletar mensagem")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar mensagem")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao deletar mensagem: ${e.message}"
                    )
                }
            }
        }
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
    val isRefreshingConversa: Boolean = false,
    val mensagensNaoLidas: Map<String, Int> = emptyMap(),
    val mostrarModalLimparMensagens: Boolean = false
)

data class ConversaUiState(
    val mensagens: List<MensagemChat> = emptyList(),
    val isLoadingInicial: Boolean = true,
    val isCarregandoMais: Boolean = false,
    val possuiMaisAntigas: Boolean = true
)
