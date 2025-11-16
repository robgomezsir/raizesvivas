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
        sincronizarNotificacoes()
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
    
    /**
     * Busca notificação de aniversário de hoje não lida
     */
    suspend fun buscarAniversarioHojeNaoLido(): Notificacao? {
        return notificacaoRepository.buscarAniversarioHojeNaoLido()
    }
    
    /**
     * Cria uma nova notificação
     */
    fun criarNotificacao(notificacao: Notificacao) {
        viewModelScope.launch {
            notificacaoRepository.criarNotificacao(notificacao)
        }
    }
    
    /**
     * Busca primeira notificação ADMIN_MENSAGEM não lida
     */
    suspend fun buscarAdminMensagemNaoLida(): Notificacao? {
        return notificacaoRepository.buscarAdminMensagemNaoLida()
    }
    
    /**
     * Sincroniza notificações do Firestore para o banco local
     * Chamado automaticamente no init, mas pode ser chamado manualmente também
     */
    private fun sincronizarNotificacoes() {
        viewModelScope.launch {
            try {
                val resultado = notificacaoRepository.sincronizarNotificacoesDoFirestore()
                resultado.onSuccess { quantidade ->
                    if (quantidade > 0) {
                        Timber.d("✅ $quantidade notificação(ões) sincronizada(s) do Firestore")
                    }
                }
                resultado.onFailure { error ->
                    Timber.w(error, "⚠️ Aviso ao sincronizar notificações (continuando com dados locais)")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao sincronizar notificações")
            }
        }
    }
    
    /**
     * Força uma nova sincronização de notificações do Firestore
     */
    fun forcarSincronizacao() {
        sincronizarNotificacoes()
    }

    /**
     * Registra analytics de clique no download da atualização
     */
    fun registrarCliqueDownloadAtualizacao(notificacao: Notificacao) {
        viewModelScope.launch {
            val versao = notificacao.dadosExtras["versao"]
            val url = notificacao.dadosExtras["downloadUrl"]
            try {
                notificacaoRepository.registrarCliqueDownloadAtualizacao(
                    notificacaoId = notificacao.id,
                    versao = versao,
                    downloadUrl = url
                )
            } catch (_: Exception) {
                // Analytics não deve afetar UX
            }
        }
    }
}
