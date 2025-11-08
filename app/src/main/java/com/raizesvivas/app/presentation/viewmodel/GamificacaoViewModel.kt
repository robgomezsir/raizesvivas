package com.raizesvivas.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.GamificacaoRepository
import com.raizesvivas.app.domain.model.*
import com.raizesvivas.app.domain.usecase.VerificarConquistasUseCase
import com.raizesvivas.app.data.remote.firebase.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para gerenciar gamificação
 */
@HiltViewModel
class GamificacaoViewModel @Inject constructor(
    private val gamificacaoRepository: GamificacaoRepository,
    private val verificarConquistasUseCase: VerificarConquistasUseCase,
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(GamificacaoState())
    val state = _state.asStateFlow()
    
    private val _perfil = MutableStateFlow<PerfilGamificacao?>(null)
    val perfil = _perfil.asStateFlow()
    
    private val _progressos = MutableStateFlow<List<ProgressoConquista>>(emptyList())
    val progressos = _progressos.asStateFlow()
    
    // Conquistas com progresso combinado
    private val _conquistasComProgresso = MutableStateFlow<List<ConquistaComProgresso>>(emptyList())
    val conquistasComProgresso = _conquistasComProgresso.asStateFlow()
    
    init {
        val usuarioId = authService.currentUser?.uid
        when {
            usuarioId == null -> {
                Timber.w("⚠️ Tentando inicializar GamificacaoViewModel sem usuário autenticado")
            }
            usuarioId.isBlank() -> {
                Timber.e("❌ ERRO CRÍTICO: usuarioId está vazio no ViewModel!")
            }
            else -> {
                Timber.d("🔍 GamificacaoViewModel inicializando para usuarioId: $usuarioId")
                observarPerfil(usuarioId)
                observarConquistas()
                // Sincronizar conquistas ao iniciar (carrega do Firestore se houver perfil existente)
                // IMPORTANTE: Não verificar conquistas automaticamente aqui
                // As conquistas só serão verificadas quando o usuário realizar ações
                sincronizarConquistas(usuarioId)
            }
        }
    }
    
    /**
     * Sincroniza conquistas com Firestore (carrega remoto e envia local)
     */
    fun sincronizarConquistas(usuarioId: String) {
        viewModelScope.launch {
            try {
                gamificacaoRepository.sincronizarTodasConquistas(usuarioId)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao sincronizar conquistas")
            }
        }
    }
    
    /**
     * Observa perfil de gamificação
     */
    private fun observarPerfil(usuarioId: String) {
        viewModelScope.launch {
            gamificacaoRepository.observarPerfilGamificacao(usuarioId)
                .catch { erro ->
                    Timber.e(erro, "❌ Erro ao observar perfil de gamificação")
                }
                .collect { perfilAtual ->
                    _perfil.value = perfilAtual
                    
                    // Se não existe perfil, inicializar (novo usuário começa com nível 1, XP 0)
                    if (perfilAtual == null) {
                        gamificacaoRepository.inicializarPerfil(usuarioId)
                    }
                }
        }
    }
    
    /**
     * Observa conquistas do usuário
     */
    private fun observarConquistas() {
        val usuarioId = authService.currentUser?.uid
        if (usuarioId == null || usuarioId.isBlank()) {
            Timber.w("⚠️ Tentando observar conquistas sem usuário autenticado ou com usuarioId vazio")
            return
        }
        
        Timber.d("🔍 Observando conquistas para usuarioId: $usuarioId")
        
        viewModelScope.launch {
            gamificacaoRepository.observarTodasConquistas(usuarioId)
                .catch { erro ->
                    Timber.e(erro, "❌ Erro ao observar conquistas para usuarioId: $usuarioId")
                }
                .collect { progressosList ->
                    _progressos.value = progressosList
                    atualizarConquistasComProgresso(progressosList)
                    Timber.d("📊 ${progressosList.size} conquistas observadas para usuarioId: $usuarioId")
                }
        }
    }
    
    /**
     * Combina conquistas do sistema com progressos do usuário
     */
    private fun atualizarConquistasComProgresso(progressos: List<ProgressoConquista>) {
        val conquistas = SistemaConquistas.obterTodas()
        val conquistasComProgresso = conquistas.map { conquista ->
            val progresso = progressos.find { it.conquistaId == conquista.id }
                ?: ProgressoConquista(
                    conquistaId = conquista.id,
                    desbloqueada = false,
                    desbloqueadaEm = null,
                    progressoAtual = 0,
                    progressoTotal = conquista.condicao.valor
                )
            
            ConquistaComProgresso(
                conquista = conquista,
                progresso = progresso
            )
        }
        
        _conquistasComProgresso.value = conquistasComProgresso
    }
    
    /**
     * Verifica e desbloqueia conquistas
     */
    fun verificarConquistas(usuarioId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                verificarConquistasUseCase.verificarTodasConquistas(usuarioId)
                // Após verificar, sincronizar mudanças com Firestore
                gamificacaoRepository.sincronizarConquistasParaFirestore(usuarioId)
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao verificar conquistas")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao verificar conquistas"
                )
            }
        }
    }
}

/**
 * Estado do ViewModel de Gamificação
 */
data class GamificacaoState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Conquista com progresso combinado
 */
data class ConquistaComProgresso(
    val conquista: Conquista,
    val progresso: ProgressoConquista
)

