package com.raizesvivas.app.presentation.screens.detalhes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.FotoAlbumRepository
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.FotoAlbum
import java.util.Locale
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
    private val pessoaRepository: PessoaRepository,
    private val fotoAlbumRepository: FotoAlbumRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DetalhesPessoaState())
    val state: StateFlow<DetalhesPessoaState> = _state.asStateFlow()
    
    private val _mostrarModalConfirmacao = MutableStateFlow(false)
    val mostrarModalConfirmacao: StateFlow<Boolean> = _mostrarModalConfirmacao.asStateFlow()
    
    /**
     * Abre o modal de confirmação de exclusão
     */
    fun abrirModalConfirmacao() {
        _mostrarModalConfirmacao.value = true
    }
    
    /**
     * Fecha o modal de confirmação de exclusão
     */
    fun fecharModalConfirmacao() {
        _mostrarModalConfirmacao.value = false
    }
    
    /**
     * Deleta a pessoa do banco de dados
     */
    fun deletarPessoa(pessoaId: String, onSucesso: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val resultado = pessoaRepository.deletar(pessoaId)
                
                resultado.onSuccess {
                    Timber.d("✅ Pessoa deletada com sucesso: $pessoaId")
                    _mostrarModalConfirmacao.value = false
                    _state.update { it.copy(isLoading = false) }
                    onSucesso()
                }
                
                resultado.onFailure { erro ->
                    Timber.e(erro, "❌ Erro ao deletar pessoa")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao deletar pessoa: ${erro.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao deletar pessoa")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao deletar pessoa: ${e.message}"
                    )
                }
            }
        }
    }
    
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

                    // Consolidar filhos a partir da lista manual + relação pai/mãe no banco
                    val filhosPorIds = pessoa.filhos
                        .filter { it.isNotBlank() }
                        .mapNotNull { filhoId ->
                            runCatching { pessoaRepository.buscarPorId(filhoId) }.getOrNull()
                        }
                    val filhosRelacionados = runCatching {
                        pessoaRepository.buscarFilhos(pessoa.id)
                    }.getOrElse { emptyList() }

                    val filhosUnicos = (filhosPorIds + filhosRelacionados)
                        .filterNotNull()
                        .distinctBy { it.id }
                        .sortedWith(
                            compareByDescending<Pessoa> { it.calcularIdade() ?: 0 }
                                .thenBy { it.nome.lowercase(Locale.getDefault()) }
                        )
                    val filhosNomes = filhosUnicos.map { it.getNomeExibicao() }
                    
                    // Buscar fotos do álbum da pessoa
                    val fotosResult = fotoAlbumRepository.buscarFotosPorPessoa(pessoaId)
                    val fotos = fotosResult.getOrNull() ?: emptyList()
                    Timber.d("📸 Fotos do álbum carregadas: ${fotos.size} fotos para pessoa ${pessoa.nome}")
                    
                    _state.update {
                        it.copy(
                            isLoading = false,
                            pessoa = pessoa,
                            paiNome = paiNome,
                            maeNome = maeNome,
                            conjugeNome = conjugeNome,
                            filhosNomes = filhosNomes,
                            fotosAlbum = fotos
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
    val filhosNomes: List<String> = emptyList(),
    val fotosAlbum: List<FotoAlbum> = emptyList()
)

