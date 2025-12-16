package com.raizesvivas.app.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.PessoaFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import java.util.Date

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: PessoaRepository
) : ViewModel() {

    private val _filtro = MutableStateFlow(PessoaFilter())
    val filtro: StateFlow<PessoaFilter> = _filtro.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val pessoasPaginadas: Flow<PagingData<Pessoa>> = _filtro
        .debounce(500) // Debounce para digitação
        .flatMapLatest { filtroAtual ->
            repository.buscarPessoasAvancado(filtroAtual)
        }
        .cachedIn(viewModelScope)

    fun atualizarTermoBusca(novoTermo: String) {
        _filtro.update { it.copy(termoBusca = novoTermo) }
    }

    fun aplicarFiltros(
        genero: Genero?,
        localNascimento: String?,
        dataInicio: Date?,
        dataFim: Date?,
        apenasVivos: Boolean
    ) {
        _filtro.update {
            it.copy(
                genero = genero,
                localNascimento = localNascimento?.takeIf { s -> s.isNotBlank() },
                dataNascimentoInicio = dataInicio,
                dataNascimentoFim = dataFim,
                apenasVivos = apenasVivos
            )
        }
    }

    fun limparFiltros() {
        // Mantém o termo de busca, limpa o resto
        _filtro.update { 
            PessoaFilter(termoBusca = it.termoBusca)
        }
    }
}
