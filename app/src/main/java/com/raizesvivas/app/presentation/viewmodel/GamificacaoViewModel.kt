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
     * IMPORTANTE: Mostra TODAS as conquistas disponíveis, criando progressos zerados para as que não existem
     * Cada usuário vê todas as conquistas, mas com progresso individual
     */
    private fun atualizarConquistasComProgresso(progressos: List<ProgressoConquista>) {
        val conquistas = SistemaConquistas.obterTodas()
        
        // Criar um mapa de progressos por conquistaId para acesso rápido
        val progressosMap = progressos.associateBy { it.conquistaId }
        
        // Combinar TODAS as conquistas com seus progressos (ou criar progresso zerado se não existir)
        val conquistasComProgresso = conquistas.map { conquista ->
            val progresso = progressosMap[conquista.id] ?: ProgressoConquista(
                conquistaId = conquista.id,
                concluida = false,
                desbloqueadaEm = null,
                progresso = 0,
                progressoTotal = conquista.condicao.valor,
                nivel = 1,
                pontuacaoTotal = 0
            )
            
            ConquistaComProgresso(
                conquista = conquista,
                progresso = progresso
            )
        }
            .sortedBy { it.conquista.ordem } // Ordenar pela ordem definida no sistema
        
        _conquistasComProgresso.value = conquistasComProgresso
        Timber.d("📊 ${conquistasComProgresso.size} conquistas disponíveis (${progressos.size} com progresso)")
    }
    
    /**
     * Registra ação do usuário e atualiza progresso das conquistas relacionadas
     * 
     * NOVO: Sistema de rastreamento de ações em tempo real
     * 
     * @param usuarioId ID do usuário que realizou a ação
     * @param tipoAcao Tipo da ação realizada
     */
    fun registrarAcao(usuarioId: String, tipoAcao: TipoAcao) {
        viewModelScope.launch {
            try {
                Timber.d("🎯 Registrando ação: $tipoAcao para usuário: $usuarioId")
                gamificacaoRepository.registrarAcao(usuarioId, tipoAcao)
                
                // Após registrar ação, verificar se alguma conquista foi desbloqueada
                // Isso garante que o progresso seja atualizado imediatamente
                verificarConquistas(usuarioId)
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao registrar ação: $tipoAcao")
                _state.value = _state.value.copy(
                    error = e.message ?: "Erro ao registrar ação"
                )
            }
        }
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
    
    /**
     * Busca ranking de usuários
     */
    suspend fun buscarRanking(usuarioIdAtual: String): Result<List<com.raizesvivas.app.domain.model.RankingUsuario>> {
        return try {
            gamificacaoRepository.buscarRanking(usuarioIdAtual)
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao buscar ranking")
            Result.failure(e)
        }
    }
    
    /**
     * Obtém o ID do usuário atual
     */
    fun obterUsuarioIdAtual(): String? {
        return authService.currentUser?.uid
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

