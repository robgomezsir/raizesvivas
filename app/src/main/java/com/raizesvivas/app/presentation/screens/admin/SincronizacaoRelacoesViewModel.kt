package com.raizesvivas.app.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.PessoaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SincronizacaoRelacoesViewModel @Inject constructor(
    private val pessoaRepository: PessoaRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(SincronizacaoRelacoesState())
    val state: StateFlow<SincronizacaoRelacoesState> = _state.asStateFlow()
    
    fun executarSincronizacao() {
        viewModelScope.launch {
            _state.update { it.copy(isSincronizando = true, erro = null) }
            
            try {
                val resultado = pessoaRepository.sincronizarRelacoesFamiliares()
                
                resultado.onSuccess { relatorio ->
                    _state.update {
                        it.copy(
                            isSincronizando = false,
                            ultimoRelatorio = relatorio,
                            erro = null
                        )
                    }
                    Timber.d("✅ Sincronização concluída: ${relatorio.pessoasCorrigidas} pessoas corrigidas")
                }.onFailure { exception ->
                    _state.update {
                        it.copy(
                            isSincronizando = false,
                            erro = exception.message ?: "Erro desconhecido ao sincronizar"
                        )
                    }
                    Timber.e(exception, "❌ Erro ao sincronizar relações")
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSincronizando = false,
                        erro = e.message ?: "Erro desconhecido"
                    )
                }
                Timber.e(e, "❌ Erro ao executar sincronização")
            }
        }
    }
    
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
}

data class SincronizacaoRelacoesState(
    val isSincronizando: Boolean = false,
    val ultimoRelatorio: PessoaRepository.RelatorioSincronizacao? = null,
    val erro: String? = null
)

