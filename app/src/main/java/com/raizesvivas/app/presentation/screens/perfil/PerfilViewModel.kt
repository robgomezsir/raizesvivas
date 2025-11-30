package com.raizesvivas.app.presentation.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.local.BiometricCrypto
import com.raizesvivas.app.data.local.BiometricPreferences
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.data.repository.ConviteRepository
import com.raizesvivas.app.data.repository.EdicaoPendenteRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para a tela de Perfil
 */
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val authService: AuthService,
    private val firestoreService: FirestoreService,
    private val usuarioRepository: UsuarioRepository,
    private val pessoaRepository: PessoaRepository,
    private val conviteRepository: ConviteRepository,
    private val edicaoPendenteRepository: EdicaoPendenteRepository,
    private val biometricPreferences: BiometricPreferences,
    private val biometricCrypto: BiometricCrypto
) : ViewModel() {
    
    private val _state = MutableStateFlow(PerfilState())
    val state = _state.asStateFlow()
    
    // Lista de pessoas para seleção de vinculação
    val pessoasDisponiveis = pessoaRepository
        .observarTodasPessoas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Lista de todos os usuários (para admins promoverem outros admins)
    private val _todosUsuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val todosUsuarios = _todosUsuarios.asStateFlow()
    
    init {
        carregarDados()
        observarConvitesPendentes()
        observarEdicoesPendentes()
        observarStatusAdminParaCarregarUsuarios()
    }
    
    /**
     * Observa mudanças no status de admin para carregar lista de usuários quando necessário
     */
    private fun observarStatusAdminParaCarregarUsuarios() {
        viewModelScope.launch {
            state.map { it.ehAdmin }
                .distinctUntilChanged()
                .collect { ehAdmin ->
                    if (ehAdmin) {
                        Timber.d("👤 Usuário é admin, carregando lista de usuários...")
                        carregarTodosUsuarios()
                    } else {
                        // Limpar lista se não for mais admin
                        _todosUsuarios.value = emptyList()
                    }
                }
        }
    }
    
    /**
     * Carrega todos os usuários (apenas para admins)
     */
    private fun carregarTodosUsuarios() {
        viewModelScope.launch {
            try {
                val currentUser = authService.currentUser
                if (currentUser == null) {
                    Timber.w("⚠️ Usuário não autenticado, não é possível carregar lista de usuários")
                    return@launch
                }
                
                Timber.d("🔄 Carregando todos os usuários...")
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val resultado = usuarioRepository.buscarTodosUsuarios()
                
                resultado.onSuccess { usuarios ->
                    Timber.d("✅ ${usuarios.size} usuário(s) carregado(s)")
                    _todosUsuarios.value = usuarios
                    _state.update { it.copy(isLoading = false) }
                }
                
                resultado.onFailure { error ->
                    Timber.e(error, "❌ Erro ao carregar todos os usuários")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao carregar lista de usuários: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar todos os usuários")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar lista de usuários: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Promove ou rebaixa um usuário a administrador
     */
    fun promoverAdmin(userId: String, ehAdmin: Boolean) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val resultado = usuarioRepository.promoverAdmin(userId, ehAdmin)
                
                resultado.onSuccess {
                    // Atualizar lista local imediatamente para feedback visual
                    val listaAtualizada = _todosUsuarios.value.map { usuario ->
                        if (usuario.id == userId) {
                            usuario.copy(ehAdministrador = ehAdmin)
                        } else {
                            usuario
                        }
                    }
                    _todosUsuarios.value = listaAtualizada
                    
                    // Recarregar do Firestore em background para garantir sincronização
                    carregarTodosUsuarios()
                    
                    _state.update { it.copy(isLoading = false) }
                    Timber.d("✅ Usuário ${if (ehAdmin) "promovido" else "rebaixado"} com sucesso")
                }
                
                resultado.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao ${if (ehAdmin) "promover" else "rebaixar"} admin: ${error.message}"
                        )
                    }
                    Timber.e(error, "❌ Erro ao ${if (ehAdmin) "promover" else "rebaixar"} admin")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao ${if (ehAdmin) "promover" else "rebaixar"} admin")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao ${if (ehAdmin) "promover" else "rebaixar"} admin: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Promove ou rebaixa múltiplos usuários como administradores em lote
     */
    fun promoverAdminEmLote(userIds: Set<String>, ehAdmin: Boolean) {
        viewModelScope.launch {
            try {
                if (userIds.isEmpty()) return@launch
                
                _state.update { it.copy(isLoading = true, erro = null) }
                
                Timber.d("🔄 ${if (ehAdmin) "Promovendo" else "Rebaixando"} ${userIds.size} usuário(s) como admin(s)...")
                
                // Processar cada usuário sequencialmente
                var sucessos = 0
                var falhas = 0
                userIds.forEach { userId ->
                    val resultado = usuarioRepository.promoverAdmin(userId, ehAdmin)
                    resultado.onSuccess {
                        sucessos++
                        Timber.d("✅ Usuário ${if (ehAdmin) "promovido" else "rebaixado"} com sucesso")
                    }
                    resultado.onFailure { error ->
                        falhas++
                        Timber.e(error, "❌ Erro ao ${if (ehAdmin) "promover" else "rebaixar"} usuário $userId")
                    }
                }
                
                // Atualizar lista local imediatamente para feedback visual
                val listaAtualizada = _todosUsuarios.value.map { usuario ->
                    if (usuario.id in userIds) {
                        usuario.copy(ehAdministrador = ehAdmin)
                    } else {
                        usuario
                    }
                }
                _todosUsuarios.value = listaAtualizada
                
                // Recarregar do Firestore em background para garantir sincronização
                carregarTodosUsuarios()
                
                _state.update { it.copy(isLoading = false) }
                Timber.d("✅ ${userIds.size} usuário(s) ${if (ehAdmin) "promovido(s)" else "rebaixado(s)"} com sucesso")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao ${if (ehAdmin) "promover" else "rebaixar"} administradores em lote")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao ${if (ehAdmin) "promover" else "rebaixar"} administradores: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Observa convites pendentes
     */
    private fun observarConvitesPendentes() {
        viewModelScope.launch {
            conviteRepository.observarConvitesPendentes()
                .catch { error ->
                    Timber.e(error, "Erro ao observar convites")
                }
                .collect { convites ->
                    _state.update { it.copy(convitesPendentes = convites.size) }
                }
        }
    }
    
    /**
     * Observa edições pendentes (apenas para admin)
     */
    private fun observarEdicoesPendentes() {
        viewModelScope.launch {
            edicaoPendenteRepository.observarEdicoesPendentes()
                .catch { error ->
                    Timber.e(error, "Erro ao observar edições")
                }
                .collect { edicoes ->
                    _state.update { it.copy(edicoesPendentes = edicoes.size) }
                }
        }
    }
    
    /**
     * Carrega dados do usuário
     */
    private fun carregarDados() {
        viewModelScope.launch {
            try {
                val currentUser = authService.currentUser
                if (currentUser != null) {
                    // Forçar atualização do Firestore para garantir dados atualizados
                    val usuarioRemoto = firestoreService.buscarUsuario(currentUser.uid).getOrNull()
                    
                    // Atualizar cache local se encontrou dados remotos
                    usuarioRemoto?.let {
                        usuarioRepository.atualizar(it)
                    }
                    
                    // Usar dados remotos se disponíveis, senão buscar do repositório
                    val usuario = usuarioRemoto ?: usuarioRepository.buscarPorId(currentUser.uid)
                    
                    val ehAdmin = usuario?.ehAdministrador ?: false
                    
                    _state.update {
                        it.copy(
                            nome = usuario?.nome ?: currentUser.displayName,
                            email = currentUser.email,
                            fotoUrl = usuario?.fotoUrl,
                            ehAdmin = ehAdmin,
                            pessoaVinculadaId = usuario?.pessoaVinculada,
                            pessoaVinculadaNome = usuario?.pessoaVinculada?.let { 
                                pessoaRepository.buscarPorId(it)?.nome 
                            }
                        )
                    }
                    
                    // Se o usuário é admin, garantir que a lista de usuários está carregada
                    if (ehAdmin && _todosUsuarios.value.isEmpty()) {
                        Timber.d("🔄 Usuário é admin mas lista está vazia, carregando...")
                        carregarTodosUsuarios()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar dados do perfil")
            }
        }
    }
    
    /**
     * Vincula usuário a uma pessoa
     */
    fun vincularPessoa(pessoaId: String?) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val currentUser = authService.currentUser
                if (currentUser == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Usuário não autenticado"
                        ) 
                    }
                    return@launch
                }
                
                // Validação adicional: garantir que o userId não está vazio
                if (currentUser.uid.isBlank()) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "ID do usuário inválido"
                        ) 
                    }
                    return@launch
                }
                
                val resultado = if (pessoaId != null) {
                    Timber.d("🔗 Vinculando usuário ${currentUser.uid} à pessoa $pessoaId")
                    usuarioRepository.vincularPessoa(currentUser.uid, pessoaId)
                } else {
                    // Desvincular
                    Timber.d("🔓 Desvinculando usuário ${currentUser.uid}")
                    val usuario = usuarioRepository.buscarPorId(currentUser.uid)
                    if (usuario != null) {
                        usuarioRepository.atualizar(usuario.copy(pessoaVinculada = null))
                    } else {
                        Result.failure(Exception("Usuário não encontrado"))
                    }
                }
                
                resultado.onSuccess {
                    Timber.d("✅ Vinculação realizada com sucesso")
                    // Atualizar estado imediatamente com a pessoa vinculada
                    if (pessoaId != null) {
                        val pessoaVinculada = pessoaRepository.buscarPorId(pessoaId)
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                pessoaVinculadaId = pessoaId,
                                pessoaVinculadaNome = pessoaVinculada?.nome,
                                erro = null // Limpar erro em caso de sucesso
                            ) 
                        }
                    } else {
                        // Desvinculado
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                pessoaVinculadaId = null,
                                pessoaVinculadaNome = null,
                                erro = null // Limpar erro em caso de sucesso
                            ) 
                        }
                    }
                    // Recarregar dados completos em background
                    carregarDados()
                }
                
                resultado.onFailure { error ->
                    Timber.e(error, "❌ Erro ao vincular pessoa")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao vincular: ${error.message}"
                        ) 
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao vincular pessoa")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao vincular: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Retorna o ID do usuário atual
     */
    fun getCurrentUserId(): String? {
        return authService.currentUser?.uid
    }
    
    /**
     * Recarrega a lista de usuários manualmente (para admins)
     */
    fun recarregarListaUsuarios() {
        val currentUser = authService.currentUser
        if (currentUser == null) {
            Timber.w("⚠️ Usuário não autenticado, não é possível recarregar lista de usuários")
            return
        }
        
        val ehAdmin = _state.value.ehAdmin
        if (!ehAdmin) {
            Timber.w("⚠️ Usuário não é administrador, não é possível carregar lista de usuários")
            return
        }
        
        Timber.d("🔄 Recarregando lista de usuários manualmente...")
        carregarTodosUsuarios()
    }
    
    /**
     * Faz logout do usuário
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Limpar dados do usuário
                usuarioRepository.limparDados()
                
                // Limpar dados biométricos
                biometricCrypto.clearAllPasswords()
                biometricPreferences.clear()
                
                // Fazer logout do Firebase
                authService.logout()
                
                Timber.d("Logout realizado")
            } catch (e: Exception) {
                Timber.e(e, "Erro ao fazer logout")
            }
        }
    }
}

/**
 * Estado da tela de Perfil
 */
data class PerfilState(
    val nome: String? = null,
    val email: String? = null,
    val fotoUrl: String? = null,
    val ehAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val convitesPendentes: Int = 0,
    val edicoesPendentes: Int = 0,
    val pessoaVinculadaId: String? = null,
    val pessoaVinculadaNome: String? = null,
    val erro: String? = null
)

