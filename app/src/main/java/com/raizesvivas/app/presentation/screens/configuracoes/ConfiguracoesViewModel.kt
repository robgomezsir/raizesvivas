package com.raizesvivas.app.presentation.screens.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.NotificacaoRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel para tela de Configurações (apenas ADMIN SÊNIOR)
 */
@HiltViewModel
class ConfiguracoesViewModel @Inject constructor(
    private val notificacaoRepository: NotificacaoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(ConfiguracoesState())
    val state = _state.asStateFlow()
    
    init {
        verificarSeEhAdminSenior()
        carregarUsuarioMaisAntigo()
    }
    
    private fun verificarSeEhAdminSenior() {
        viewModelScope.launch {
            val usuarioAtual = authService.currentUser
            if (usuarioAtual != null) {
                val usuario = usuarioRepository.buscarPorId(usuarioAtual.uid)
                val ehAdminSenior = usuario?.ehAdministradorSenior == true
                
                _state.update { it.copy(ehAdminSenior = ehAdminSenior) }
                
                if (!ehAdminSenior) {
                    _state.update { 
                        it.copy(erro = "Apenas ADMIN SÊNIOR pode acessar esta página") 
                    }
                }
            }
        }
    }
    
    private fun carregarUsuarioMaisAntigo() {
        viewModelScope.launch {
            val resultado = usuarioRepository.buscarUsuarioMaisAntigo()
            resultado.onSuccess { usuario ->
                _state.update { it.copy(usuarioMaisAntigo = usuario) }
            }
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao buscar usuário mais antigo")
            }
        }
    }
    
    fun atualizarTitulo(titulo: String) {
        _state.update { it.copy(titulo = titulo) }
    }
    
    fun atualizarMensagem(mensagem: String) {
        _state.update { it.copy(mensagem = mensagem) }
    }
    
    fun enviarNotificacao() {
        val estadoAtual = _state.value
        
        // Validações
        if (estadoAtual.titulo.isBlank()) {
            _state.update { it.copy(erro = "O título é obrigatório") }
            return
        }
        
        if (estadoAtual.mensagem.isBlank()) {
            _state.update { it.copy(erro = "A mensagem é obrigatória") }
            return
        }
        
        if (!estadoAtual.ehAdminSenior) {
            _state.update { it.copy(erro = "Apenas ADMIN SÊNIOR pode enviar notificações") }
            return
        }
        
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    isLoading = true, 
                    erro = null,
                    sucesso = null
                ) 
            }
            
            val resultado = notificacaoRepository.criarNotificacaoParaTodosUsuarios(
                titulo = estadoAtual.titulo,
                mensagem = estadoAtual.mensagem
            )
            
            resultado.onSuccess { quantidade ->
                Timber.d("✅ Notificação enviada para $quantidade usuário(s)")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        sucesso = "Notificação enviada para $quantidade usuário(s) com sucesso!",
                        titulo = "",
                        mensagem = ""
                    ) 
                }
            }
            
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao enviar notificação")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao enviar notificação: ${error.message}"
                    ) 
                }
            }
        }
    }
    
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
    
    fun limparSucesso() {
        _state.update { it.copy(sucesso = null) }
    }

    // ==============================
    // Atualização do App
    // ==============================
    fun atualizarVersaoAtualizacao(versao: String) {
        _state.update { it.copy(versaoAtualizacao = versao) }
    }

    fun atualizarLinkDownloadAtualizacao(link: String) {
        _state.update { it.copy(linkDownloadAtualizacao = link) }
    }

    fun enviarAtualizacao() {
        val estadoAtual = _state.value

        if (!estadoAtual.ehAdminSenior) {
            _state.update { it.copy(erro = "Apenas ADMIN SÊNIOR pode enviar notificações") }
            return
        }

        if (estadoAtual.versaoAtualizacao.isBlank()) {
            _state.update { it.copy(erro = "A versão é obrigatória (ex: v2.5)") }
            return
        }

        if (estadoAtual.linkDownloadAtualizacao.isBlank()) {
            _state.update { it.copy(erro = "O link para download é obrigatório") }
            return
        }

        // Validação simples de URL
        val link = estadoAtual.linkDownloadAtualizacao.trim()
        val isValidUrl = link.startsWith("https://") || link.startsWith("http://")
        if (!isValidUrl) {
            _state.update { it.copy(erro = "Link inválido. Use um URL iniciando com https://") }
            return
        }

        val titulo = "Nova atualização!"
        val mensagem = "Baixe agora mesmo a nova atualização ${estadoAtual.versaoAtualizacao} do app Raízes Vivas"

        viewModelScope.launch {
            _state.update { it.copy(isSendingUpdate = true, erro = null, sucesso = null) }

            val resultado = notificacaoRepository.criarNotificacaoAtualizacaoParaTodosUsuarios(
                titulo = titulo,
                mensagem = mensagem,
                versao = estadoAtual.versaoAtualizacao,
                downloadUrl = link
            )

            resultado.onSuccess { quantidade ->
                Timber.d("✅ Notificação de atualização enviada para $quantidade usuário(s)")
                _state.update {
                    it.copy(
                        isSendingUpdate = false,
                        sucesso = "Aviso de atualização enviado para $quantidade usuário(s)!",
                        versaoAtualizacao = "",
                        linkDownloadAtualizacao = ""
                    )
                }
            }

            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao enviar atualização")
                _state.update {
                    it.copy(
                        isSendingUpdate = false,
                        erro = "Erro ao enviar atualização: ${error.message}"
                    )
                }
            }
        }
    }
}

/**
 * Estado da tela de Configurações
 */
data class ConfiguracoesState(
    val titulo: String = "",
    val mensagem: String = "",
    val isLoading: Boolean = false,
    val ehAdminSenior: Boolean = false,
    val usuarioMaisAntigo: Usuario? = null,
    val erro: String? = null,
    val sucesso: String? = null,
    // Campos para atualização do app
    val versaoAtualizacao: String = "",
    val linkDownloadAtualizacao: String = "",
    val isSendingUpdate: Boolean = false
)

