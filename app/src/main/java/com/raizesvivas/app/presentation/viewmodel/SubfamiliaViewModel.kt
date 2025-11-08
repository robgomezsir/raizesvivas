package com.raizesvivas.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.SubfamiliaRepository
import com.raizesvivas.app.domain.model.SugestaoSubfamilia
import com.raizesvivas.app.domain.model.Subfamilia
import com.raizesvivas.app.domain.model.StatusSugestao
import com.raizesvivas.app.domain.usecase.CriarSubfamiliaUseCase
import com.raizesvivas.app.domain.usecase.DetectarSubfamiliasUseCase
import com.raizesvivas.app.domain.usecase.CriarNotificacaoUseCase
import com.raizesvivas.app.data.remote.firebase.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para gerenciar subfamílias e sugestões
 */
@HiltViewModel
class SubfamiliaViewModel @Inject constructor(
    private val subfamiliaRepository: SubfamiliaRepository,
    private val criarSubfamiliaUseCase: CriarSubfamiliaUseCase,
    private val detectarSubfamiliasUseCase: DetectarSubfamiliasUseCase,
    private val criarNotificacaoUseCase: CriarNotificacaoUseCase,
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(SubfamiliaState())
    val state = _state.asStateFlow()
    
    private val _sugestoes = MutableStateFlow<List<SugestaoSubfamilia>>(emptyList())
    val sugestoes = _sugestoes.asStateFlow()
    
    private val _subfamilias = MutableStateFlow<List<Subfamilia>>(emptyList())
    val subfamilias = _subfamilias.asStateFlow()
    
    init {
        observarSugestoesPendentes()
        observarSubfamilias()
    }
    
    /**
     * Observa sugestões pendentes em tempo real
     */
    private fun observarSugestoesPendentes() {
        viewModelScope.launch {
            subfamiliaRepository.observarSugestoesPendentes()
                .catch { erro ->
                    Timber.e(erro, "❌ Erro ao observar sugestões")
                    _state.update { it.copy(error = erro.message) }
                }
                .collect { sugestoesList ->
                    Timber.d("✅ Sugestões atualizadas: ${sugestoesList.size}")
                    _sugestoes.value = sugestoesList
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
    
    /**
     * Observa subfamílias em tempo real
     */
    private fun observarSubfamilias() {
        viewModelScope.launch {
            subfamiliaRepository.observarTodasSubfamilias()
                .catch { erro ->
                    Timber.e(erro, "❌ Erro ao observar subfamílias")
                }
                .collect { subfamiliasList ->
                    Timber.d("✅ Subfamílias atualizadas: ${subfamiliasList.size}")
                    _subfamilias.value = subfamiliasList
                }
        }
    }
    
    /**
     * Detecta novas subfamílias automaticamente
     */
    fun detectarSubfamilias(familiaZeroId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val usuarioId = authService.currentUser?.uid
                if (usuarioId == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Usuário não autenticado"
                        )
                    }
                    return@launch
                }
                
                val sugestoes = detectarSubfamiliasUseCase.executar(usuarioId, familiaZeroId)
                Timber.d("✅ Detecção concluída: ${sugestoes.size} sugestões criadas")
                
                // Criar notificações para as novas sugestões
                if (sugestoes.isNotEmpty()) {
                    criarNotificacaoUseCase.criarNotificacoesSugestoesSubfamilias(sugestoes)
                }
                
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao detectar subfamílias")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao detectar subfamílias"
                    )
                }
            }
        }
    }
    
    /**
     * Aceita uma sugestão e cria a subfamília
     */
    fun aceitarSugestao(sugestao: SugestaoSubfamilia, nomePersonalizado: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val usuarioId = authService.currentUser?.uid
                if (usuarioId == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Usuário não autenticado"
                        )
                    }
                    return@launch
                }
                
                val resultado = criarSubfamiliaUseCase.executar(
                    sugestao = sugestao,
                    nomePersonalizado = nomePersonalizado,
                    usuarioId = usuarioId
                )
                
                resultado.onSuccess {
                    Timber.d("✅ Subfamília criada com sucesso")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            sucesso = "Subfamília criada com sucesso!"
                        )
                    }
                }.onFailure { erro ->
                    Timber.e(erro, "❌ Erro ao criar subfamília")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = erro.message ?: "Erro ao criar subfamília"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao aceitar sugestão")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao aceitar sugestão"
                    )
                }
            }
        }
    }
    
    /**
     * Rejeita uma sugestão
     */
    fun rejeitarSugestao(sugestao: SugestaoSubfamilia) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val resultado = subfamiliaRepository.atualizarStatusSugestao(
                    sugestaoId = sugestao.id,
                    status = StatusSugestao.REJEITADA
                )
                
                resultado.onSuccess {
                    Timber.d("✅ Sugestão rejeitada")
                    _state.update { it.copy(isLoading = false) }
                }.onFailure { erro ->
                    Timber.e(erro, "❌ Erro ao rejeitar sugestão")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = erro.message ?: "Erro ao rejeitar sugestão"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao rejeitar sugestão")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao rejeitar sugestão"
                    )
                }
            }
        }
    }
    
    /**
     * Limpa mensagens de erro/sucesso
     */
    fun limparMensagens() {
        _state.update { it.copy(error = null, sucesso = null) }
    }
    
    /**
     * Sincroniza subfamílias do Firestore
     */
    fun sincronizarSubfamilias() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val resultado = subfamiliaRepository.sincronizarDoFirestore()
                resultado.onSuccess {
                    Timber.d("✅ Subfamílias sincronizadas")
                    _state.update { it.copy(isLoading = false) }
                }.onFailure { erro ->
                    Timber.e(erro, "❌ Erro ao sincronizar subfamílias")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = erro.message ?: "Erro ao sincronizar"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao sincronizar")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao sincronizar"
                    )
                }
            }
        }
    }
}

/**
 * Estado do ViewModel de Subfamílias
 */
data class SubfamiliaState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sucesso: String? = null
)
