package com.raizesvivas.app.presentation.screens.convites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.ConviteRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.AccessRequest
import com.raizesvivas.app.domain.model.Convite
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.utils.NetworkUtils
import com.raizesvivas.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * ViewModel para gerenciamento de convites (apenas admin)
 * 
 * Permite criar, listar e gerenciar convites
 */
@HiltViewModel
class GerenciarConvitesViewModel @Inject constructor(
    private val conviteRepository: ConviteRepository,
    private val pessoaRepository: PessoaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authService: AuthService,
    private val networkUtils: NetworkUtils,
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    private val _state = MutableStateFlow(GerenciarConvitesState())
    val state = _state.asStateFlow()
    
    private val _convites = MutableStateFlow<List<Convite>>(emptyList())
    val convites = _convites.asStateFlow()

    private val _pedidos = MutableStateFlow<List<AccessRequest>>(emptyList())
    val pedidos = _pedidos.asStateFlow()
    private val _pedidosHasMore = MutableStateFlow(false)
    val pedidosHasMore = _pedidosHasMore.asStateFlow()
    private var pedidosCursor: Date? = null
    private val pageSize = 20
    var filtroStatus = "pending"
        private set
    var filtroEmail = MutableStateFlow("")
    
    val pessoasDisponiveis = pessoaRepository.observarTodasPessoas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        carregarConvites()
        carregarPedidos(reset = true)
        verificarPermissoes()
    }
    
    /**
     * Verifica se usuário é admin
     */
    private fun verificarPermissoes() {
        viewModelScope.launch {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                _state.update { it.copy(
                    erro = "Usuário não autenticado",
                    ehAdmin = false
                ) }
                return@launch
            }
            
            val usuario = usuarioRepository.buscarPorId(currentUser.uid)
            val ehAdmin = (usuario?.ehAdministrador == true) || (usuario?.ehAdministradorSenior == true)
            
            _state.update { it.copy(ehAdmin = ehAdmin) }
            
            if (!ehAdmin) {
                _state.update { it.copy(
                    erro = "Apenas administradores podem gerenciar convites"
                ) }
            }
        }
    }
    
    /**
     * Carrega todos os convites
     */
    fun carregarConvites() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val resultado = conviteRepository.buscarTodosConvites()
                
                resultado.onSuccess { convites ->
                    _convites.value = convites.sortedByDescending { it.criadoEm }
                    _state.update { it.copy(isLoading = false) }
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao carregar convites: ${error.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar convites")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar convites: ${e.message}"
                    )
                }
            }
        }
    }

    fun carregarPedidos(reset: Boolean = false) {
        viewModelScope.launch {
            try {
                if (reset) {
                    pedidosCursor = null
                    _pedidos.value = emptyList()
                }
                val resultado = firestoreService.buscarPedidosConvitePaginado(
                    limit = pageSize,
                    startAfterDate = pedidosCursor,
                    status = if (filtroStatus.isNotBlank()) filtroStatus else null
                )
                resultado.onSuccess { (lista, last) ->
                    val filtrada = lista.filter { req ->
                        val f = filtroEmail.value.trim().lowercase()
                        f.isBlank() || req.email.lowercase().contains(f)
                    }
                    _pedidos.value = _pedidos.value + filtrada
                    pedidosCursor = last
                    _pedidosHasMore.value = lista.size >= pageSize
                }
                resultado.onFailure { e ->
                    _state.update { it.copy(erro = "Erro ao carregar pedidos: ${e.message}") }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar pedidos de convite")
            }
        }
    }

    fun atualizarFiltroEmail(novo: String) {
        filtroEmail.value = novo
        carregarPedidos(reset = true)
    }

    fun atualizarFiltroStatus(novo: String) {
        filtroStatus = novo
        carregarPedidos(reset = true)
    }
    
    /**
     * Atualiza email do novo convite
     */
    fun onEmailConvidadoChanged(email: String) {
        _state.update { it.copy(emailConvidado = email) }
    }
    
    /**
     * Atualiza pessoa vinculada
     */
    fun onPessoaVinculadaChanged(pessoaId: String?) {
        _state.update { it.copy(pessoaVinculadaId = pessoaId) }
    }
    
    /**
     * Cria novo convite
     */
    fun criarConvite() {
        val state = _state.value
        
        // Validar email
        val validacaoEmail = ValidationUtils.validarEmail(state.emailConvidado)
        if (!validacaoEmail.isValid) {
            _state.update { it.copy(emailError = validacaoEmail.errorMessage) }
            return
        }
        
        // Verificar conectividade
        if (!networkUtils.isConnected()) {
            _state.update { it.copy(
                erro = "Sem conexão com a internet. Verifique sua conexão e tente novamente."
            ) }
            return
        }
        
        _state.update { it.copy(isLoading = true, emailError = null) }
        
        viewModelScope.launch {
            try {
                val resultado = conviteRepository.criarConvite(
                    emailConvidado = state.emailConvidado.trim(),
                    pessoaVinculada = state.pessoaVinculadaId
                )
                
                resultado.onSuccess {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            sucesso = true,
                            emailConvidado = "",
                            pessoaVinculadaId = null
                        )
                    }
                    carregarConvites() // Recarregar lista
                    
                    // Limpar sucesso após 3 segundos
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        _state.update { it.copy(sucesso = false) }
                    }
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao criar convite: ${error.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao criar convite")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao criar convite: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Aprova um pedido gerando um convite e atualiza o status
     */
    fun aprovarPedido(request: AccessRequest, pessoaVinculadaId: String?) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val resultado = conviteRepository.criarConvite(
                    emailConvidado = request.email,
                    pessoaVinculada = pessoaVinculadaId
                )
                resultado.onSuccess {
                    firestoreService.atualizarStatusPedidoConvite(request.id, "approved")
                    _state.update { it.copy(isLoading = false, sucesso = true) }
                    carregarConvites()
                    carregarPedidos()
                }
                resultado.onFailure { e ->
                    _state.update { it.copy(isLoading = false, erro = "Erro ao aprovar: ${e.message}") }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao aprovar pedido")
                _state.update { it.copy(isLoading = false, erro = "Erro ao aprovar: ${e.message}") }
            }
        }
    }

    fun rejeitarPedido(requestId: String) {
        viewModelScope.launch {
            firestoreService.atualizarStatusPedidoConvite(requestId, "rejected")
            carregarPedidos()
        }
    }
    
    /**
     * Deleta convite
     */
    fun deletarConvite(conviteId: String) {
        viewModelScope.launch {
            try {
                val resultado = conviteRepository.deletarConvite(conviteId)
                
                resultado.onSuccess {
                    carregarConvites() // Recarregar lista
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(erro = "Erro ao deletar convite: ${error.message}")
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar convite")
                _state.update { 
                    it.copy(erro = "Erro ao deletar convite: ${e.message}")
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
    
    /**
     * Limpa template de email após compartilhar
     */
    fun limparTemplateEmail() {
        _state.update { it.copy(templateEmail = null) }
    }
    
    /**
     * Gera template de email para compartilhar convite
     */
    fun gerarTemplateEmail() {
        val state = _state.value
        
        // Validar email
        val validacaoEmail = ValidationUtils.validarEmail(state.emailConvidado)
        if (!validacaoEmail.isValid) {
            _state.update { it.copy(emailError = validacaoEmail.errorMessage) }
            return
        }
        
        val pessoaVinculada = state.pessoaVinculadaId?.let { pessoaId ->
            pessoasDisponiveis.value.find { it.id == pessoaId }
        }
        
        // Gerar template de email
        val template = gerarTemplateEmailConvite(
            emailConvidado = state.emailConvidado.trim(),
            pessoaVinculada = pessoaVinculada
        )
        
        // Emitir evento para compartilhar (será tratado pela UI)
        _state.update { it.copy(templateEmail = template) }
    }
    
    /**
     * Gera o template de email do convite
     */
    private fun gerarTemplateEmailConvite(
        emailConvidado: String,
        pessoaVinculada: Pessoa?
    ): String {
        val pessoaText = if (pessoaVinculada != null) {
            "\n\nVocê será vinculado a: ${pessoaVinculada.getNomeExibicao()}"
        } else {
            ""
        }
        
        return """
            Assunto: Convite para Árvore Genealógica - Raízes Vivas
            
            Olá,
            
            Seu pedido de convite foi aprovado! Você foi convidado(a) para participar de uma árvore genealógica no aplicativo Raízes Vivas!$pessoaText
            
            Para acessar o aplicativo:
            1. Baixe o aplicativo Raízes Vivas (se ainda não tiver)
            2. CRIE SUA CONTA usando este e-mail: $emailConvidado
               - Vá na tela de Cadastro
               - Use este mesmo e-mail: $emailConvidado
               - Crie uma senha
               - Preencha seu nome completo
            3. Após criar a conta, faça login
            4. Vá em "Aceitar Convites" e confirme seu convite
            
            ⚠️ IMPORTANTE: Você precisa criar uma conta primeiro antes de fazer login!
            
            O convite expira em 7 dias.
            
            Aguardamos sua participação!
            
            Atenciosamente,
            Equipe Raízes Vivas
        """.trimIndent()
    }
}

/**
 * Estado da tela de gerenciamento de convites
 */
data class GerenciarConvitesState(
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: Boolean = false,
    val emailConvidado: String = "",
    val emailError: String? = null,
    val pessoaVinculadaId: String? = null,
    val ehAdmin: Boolean = false,
    val templateEmail: String? = null
)

