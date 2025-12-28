package com.raizesvivas.app.presentation.screens.moderacao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.AuditLog
import com.raizesvivas.app.domain.model.TipoAcaoAudit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ModeracaoViewModel @Inject constructor(
    private val authService: AuthService,
    private val usuarioRepository: UsuarioRepository,
    private val auditLogRepository: com.raizesvivas.app.data.repository.AuditLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ModeracaoState())
    val state: StateFlow<ModeracaoState> = _state

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, erro = null) }

            try {
                // Verificar se o usuário é Admin SR
                val usuarioId = authService.currentUser?.uid
                if (usuarioId == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Usuário não autenticado",
                            ehAdminSenior = false
                        )
                    }
                    return@launch
                }

                val usuario = usuarioRepository.buscarPorId(usuarioId)
                val ehAdminSenior = usuario?.ehAdministradorSenior == true

                if (!ehAdminSenior) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Acesso restrito a Administradores Sênior",
                            ehAdminSenior = false
                        )
                    }
                    return@launch
                }

                // Buscar logs reais do Firestore
                val logsResult = auditLogRepository.buscarLogs(limit = 200)
                
                logsResult.fold(
                    onSuccess = { logs ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                ehAdminSenior = true,
                                logs = logs,
                                erro = null
                            )
                        }
                        Timber.d("✅ Logs de auditoria carregados: ${logs.size} registros")
                    },
                    onFailure = { error ->
                        Timber.e(error, "❌ Erro ao carregar logs de auditoria")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                ehAdminSenior = true,
                                logs = emptyList(),
                                erro = "Erro ao carregar logs: ${error.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar logs de auditoria")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar logs: ${e.message}"
                    )
                }
            }
        }
    }

    fun filtrarPorAcao(acao: TipoAcaoAudit?) {
        _state.update { it.copy(filtroAcao = acao) }
    }

    fun filtrarPorUsuario(usuarioId: String?) {
        _state.update { it.copy(filtroUsuario = usuarioId) }
    }

    fun limparFiltros() {
        _state.update {
            it.copy(
                filtroAcao = null,
                filtroUsuario = null
            )
        }
    }

    fun recarregar() {
        carregarDados()
    }
}

data class ModeracaoState(
    val isLoading: Boolean = false,
    val ehAdminSenior: Boolean = false,
    val logs: List<AuditLog> = emptyList(),
    val filtroAcao: TipoAcaoAudit? = null,
    val filtroUsuario: String? = null,
    val erro: String? = null
) {
    val logsFiltrados: List<AuditLog>
        get() {
            var resultado = logs

            filtroAcao?.let { acao ->
                resultado = resultado.filter { it.acao == acao }
            }

            filtroUsuario?.let { usuarioId ->
                resultado = resultado.filter { it.usuarioId == usuarioId }
            }

            return resultado
        }

    val usuariosUnicos: List<Pair<String, String>>
        get() = logs
            .map { it.usuarioId to it.usuarioNome }
            .distinct()
            .sortedBy { it.second }
}
