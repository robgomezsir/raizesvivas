package com.raizesvivas.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.NotificacaoRepository
import com.raizesvivas.app.domain.model.Notificacao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para gerenciar notificações
 */
@HiltViewModel
class NotificacaoViewModel @Inject constructor(
    private val notificacaoRepository: NotificacaoRepository
) : ViewModel() {
    
    private val _notificacoes = MutableStateFlow<List<Notificacao>>(emptyList())
    val notificacoes = _notificacoes.asStateFlow()
    
    private val _contadorNaoLidas = MutableStateFlow(0)
    val contadorNaoLidas = _contadorNaoLidas.asStateFlow()
    
    init {
        observarNotificacoes()
        observarContadorNaoLidas()
    }
    
    /**
     * Observa todas as notificações
     */
    private fun observarNotificacoes() {
        viewModelScope.launch {
            notificacaoRepository.observarTodasNotificacoes()
                .catch { erro ->
                    Timber.e(erro, "❌ Erro ao observar notificações")
                }
                .collect { notificacoesList ->
                    Timber.d("✅ Notificações atualizadas: ${notificacoesList.size}")
                    _notificacoes.value = notificacoesList
                }
        }
    }
    
    /**
     * Observa contador de não lidas
     */
    private fun observarContadorNaoLidas() {
        viewModelScope.launch {
            notificacaoRepository.contarNaoLidas()
                .catch { erro ->
                    Timber.e(erro, "❌ Erro ao contar notificações não lidas")
                }
                .collect { contador ->
                    _contadorNaoLidas.value = contador
                }
        }
    }
    
    /**
     * Marca notificação como lida
     */
    fun marcarComoLida(notificacao: Notificacao) {
        viewModelScope.launch {
            notificacaoRepository.marcarComoLida(notificacao.id)
        }
    }
    
    /**
     * Marca todas as notificações como lidas
     */
    fun marcarTodasComoLidas() {
        viewModelScope.launch {
            notificacaoRepository.marcarTodasComoLidas()
        }
    }
}
