package com.raizesvivas.app.presentation.screens.detalhes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.Pessoa
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para a tela de Detalhes da Pessoa
 */
@HiltViewModel
class DetalhesPessoaViewModel @Inject constructor(
    private val pessoaRepository: PessoaRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DetalhesPessoaState())
    val state: StateFlow<DetalhesPessoaState> = _state.asStateFlow()
    
    /**
     * Carrega os dados da pessoa e seus relacionamentos
     */
    fun carregarPessoa(pessoaId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val pessoa = pessoaRepository.buscarPorId(pessoaId)
                
                if (pessoa != null) {
                    // Carregar nomes dos relacionamentos
                    val paiNome = pessoa.pai?.let { pessoaRepository.buscarPorId(it)?.getNomeExibicao() }
                    val maeNome = pessoa.mae?.let { pessoaRepository.buscarPorId(it)?.getNomeExibicao() }
                    val conjugeNome = pessoa.conjugeAtual?.let { pessoaRepository.buscarPorId(it)?.getNomeExibicao() }
                    val filhosNomes = pessoa.filhos.mapNotNull { 
                        pessoaRepository.buscarPorId(it)?.getNomeExibicao() 
                    }
                    
                    _state.update {
                        it.copy(
                            isLoading = false,
                            pessoa = pessoa,
                            paiNome = paiNome,
                            maeNome = maeNome,
                            conjugeNome = conjugeNome,
                            filhosNomes = filhosNomes
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Pessoa não encontrada"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar pessoa")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar pessoa: ${e.message}"
                    )
                }
            }
        }
    }
}

/**
 * Estado da tela de Detalhes da Pessoa
 */
data class DetalhesPessoaState(
    val pessoa: Pessoa? = null,
    val isLoading: Boolean = false,
    val erro: String? = null,
    val paiNome: String? = null,
    val maeNome: String? = null,
    val conjugeNome: String? = null,
    val filhosNomes: List<String> = emptyList()
)

