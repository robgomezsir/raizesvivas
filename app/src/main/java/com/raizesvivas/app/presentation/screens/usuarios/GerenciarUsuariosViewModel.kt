package com.raizesvivas.app.presentation.screens.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para gerenciar usuários (apenas admin)
 */
@HiltViewModel
class GerenciarUsuariosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authService: AuthService,
    private val pessoaRepository: com.raizesvivas.app.data.repository.PessoaRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(GerenciarUsuariosState())
    val state = _state.asStateFlow()
    
    init {
        carregarUsuarios()
        verificarSeEhAdmin()
    }
    
    private fun verificarSeEhAdmin() {
        viewModelScope.launch {
            val usuarioAtual = authService.currentUser
            if (usuarioAtual != null) {
                val usuario = usuarioRepository.buscarPorId(usuarioAtual.uid)
                _state.update { 
                    it.copy(
                        ehAdmin = usuario?.ehAdministrador == true,
                        ehAdminSenior = usuario?.ehAdministradorSenior == true
                    ) 
                }
                
                if (usuario?.ehAdministrador != true) {
                    _state.update { it.copy(erro = "Apenas administradores podem gerenciar usuários") }
                }
            }
        }
    }
    
    fun carregarUsuarios() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, erro = null) }
            
            val resultado = usuarioRepository.buscarTodosUsuarios()
            
            resultado.onSuccess { usuarios ->
                _state.update { 
                    it.copy(
                        usuarios = usuarios,
                        isLoading = false
                    )
                }
                
                // Carregar nomes das pessoas vinculadas
                carregarNomesPessoasVinculadas(usuarios)
            }
            
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao carregar usuários")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar usuários: ${error.message}"
                    )
                }
            }
        }
    }
    
    fun atualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, erro = null) }
            
            val resultado = usuarioRepository.atualizar(usuario)
            
            resultado.onSuccess {
                Timber.d("✅ Usuário atualizado: ${usuario.nome}")
                _state.update { it.copy(isLoading = false, sucesso = "Usuário atualizado com sucesso") }
                carregarUsuarios() // Recarregar lista
            }
            
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao atualizar usuário")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao atualizar usuário: ${error.message}"
                    )
                }
            }
        }
    }
    
    fun deletarUsuario(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, erro = null) }
            
            val resultado = usuarioRepository.deletarUsuario(userId)
            
            resultado.onSuccess {
                Timber.d("✅ Usuário deletado: $userId")
                _state.update { it.copy(isLoading = false, sucesso = "Usuário deletado com sucesso") }
                carregarUsuarios() // Recarregar lista
            }
            
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao deletar usuário")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao deletar usuário: ${error.message}"
                    )
                }
            }
        }
    }
    
    fun enviarEmailResetSenha(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, erro = null) }
            
            val resultado = authService.recuperarSenha(email)
            
            resultado.onSuccess {
                Timber.d("✅ Email de reset de senha enviado para: $email")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        sucesso = "Email de recuperação de senha enviado para $email"
                    )
                }
            }
            
            resultado.onFailure { error ->
                Timber.e(error, "❌ Erro ao enviar email de reset")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao enviar email: ${error.message}"
                    )
                }
            }
        }
    }
    
    private fun carregarNomesPessoasVinculadas(usuarios: List<Usuario>) {
        viewModelScope.launch {
            val idsPessoas = usuarios.mapNotNull { it.pessoaVinculada }.distinct()
            val nomesMap = mutableMapOf<String, String>()
            
            idsPessoas.forEach { id ->
                val pessoa = pessoaRepository.buscarPorId(id)
                pessoa?.let {
                    nomesMap[id] = it.nome
                }
            }
            
            _state.update { it.copy(nomesPessoasVinculadas = nomesMap) }
        }
    }

    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
    
    fun limparSucesso() {
        _state.update { it.copy(sucesso = null) }
    }
}

/**
 * Estado da tela de gerenciar usuários
 */
data class GerenciarUsuariosState(
    val usuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val erro: String? = null,
    val sucesso: String? = null,
    val ehAdmin: Boolean = false,
    val ehAdminSenior: Boolean = false,
    val nomesPessoasVinculadas: Map<String, String> = emptyMap()
)

