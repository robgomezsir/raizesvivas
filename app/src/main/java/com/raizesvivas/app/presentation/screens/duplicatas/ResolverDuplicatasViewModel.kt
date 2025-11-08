package com.raizesvivas.app.presentation.screens.duplicatas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.usecase.DetectarDuplicatasUseCase
import com.raizesvivas.app.utils.DuplicateDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ResolverDuplicatasViewModel @Inject constructor(
    private val detectarDuplicatasUseCase: DetectarDuplicatasUseCase,
    private val pessoaRepository: PessoaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(ResolverDuplicatasState())
    val state = _state.asStateFlow()
    
    private val _duplicatas = MutableStateFlow<List<DuplicateDetector.DuplicataResultado>>(emptyList())
    val duplicatas = _duplicatas.asStateFlow()
    
    init {
        verificarPermissoes()
        carregarDuplicatas()
    }
    
    private fun verificarPermissoes() {
        viewModelScope.launch {
            val currentUser = authService.currentUser
            if (currentUser == null) {
                _state.update { it.copy(ehAdmin = false, erro = "Usuário não autenticado") }
                return@launch
            }
            
            val usuario = usuarioRepository.buscarPorId(currentUser.uid)
            val ehAdmin = usuario?.ehAdministrador ?: false
            
            _state.update { it.copy(ehAdmin = ehAdmin) }
            
            if (!ehAdmin) {
                _state.update { it.copy(erro = "Apenas administradores podem resolver duplicatas") }
            }
        }
    }
    
    fun carregarDuplicatas() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val duplicatasEncontradas = detectarDuplicatasUseCase.detectarTodasDuplicatas(threshold = 0.8f)
                _duplicatas.value = duplicatasEncontradas
                
                _state.update { it.copy(isLoading = false) }
                
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar duplicatas")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao carregar duplicatas: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun unirDuplicatas(pessoaId1: String, pessoaId2: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val pessoa1 = pessoaRepository.buscarPorId(pessoaId1)
                val pessoa2 = pessoaRepository.buscarPorId(pessoaId2)
                
                if (pessoa1 == null || pessoa2 == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Uma das pessoas não foi encontrada"
                        )
                    }
                    return@launch
                }
                
                // Merge: manter pessoa1, transferir relacionamentos de pessoa2
                val pessoaMerged = pessoa1.copy(
                    // Manter dados mais completos
                    nome = if (pessoa1.nome.length > pessoa2.nome.length) pessoa1.nome else pessoa2.nome,
                    dataNascimento = pessoa1.dataNascimento ?: pessoa2.dataNascimento,
                    dataFalecimento = pessoa1.dataFalecimento ?: pessoa2.dataFalecimento,
                    localNascimento = pessoa1.localNascimento ?: pessoa2.localNascimento,
                    localResidencia = pessoa1.localResidencia ?: pessoa2.localResidencia,
                    profissao = pessoa1.profissao ?: pessoa2.profissao,
                    biografia = if (pessoa1.biografia?.length ?: 0 > pessoa2.biografia?.length ?: 0) 
                        pessoa1.biografia else pessoa2.biografia,
                    fotoUrl = pessoa1.fotoUrl ?: pessoa2.fotoUrl,
                    // Unir relacionamentos
                    pai = pessoa1.pai ?: pessoa2.pai,
                    mae = pessoa1.mae ?: pessoa2.mae,
                    conjugeAtual = pessoa1.conjugeAtual ?: pessoa2.conjugeAtual,
                    exConjuges = (pessoa1.exConjuges + pessoa2.exConjuges).distinct(),
                    filhos = (pessoa1.filhos + pessoa2.filhos).distinct(),
                    versao = maxOf(pessoa1.versao, pessoa2.versao) + 1
                )
                
                // Salvar pessoa merged
                val resultado = pessoaRepository.salvar(pessoaMerged, ehAdmin = true)
                
                resultado.onSuccess {
                    // Atualizar todas as referências de pessoa2 para pessoa1
                    atualizarReferencias(pessoaId2, pessoaId1)
                    
                    // Deletar pessoa2
                    pessoaRepository.deletar(pessoaId2)
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            sucesso = "Duplicatas unidas com sucesso!"
                        )
                    }
                    
                    // Recarregar duplicatas
                    carregarDuplicatas()
                    
                    // Limpar sucesso após 3 segundos
                    kotlinx.coroutines.delay(3000)
                    _state.update { it.copy(sucesso = null) }
                }
                
                resultado.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao unir duplicatas: ${error.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Erro ao unir duplicatas")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao unir duplicatas: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun atualizarReferencias(pessoaIdAntiga: String, pessoaIdNova: String) {
        val todasPessoas = pessoaRepository.buscarTodas()
        
        todasPessoas.forEach { pessoa ->
            var precisaAtualizar = false
            var pessoaAtualizada = pessoa
            
            // Atualizar referências em pai/mãe
            if (pessoa.pai == pessoaIdAntiga) {
                pessoaAtualizada = pessoaAtualizada.copy(pai = pessoaIdNova)
                precisaAtualizar = true
            }
            if (pessoa.mae == pessoaIdAntiga) {
                pessoaAtualizada = pessoaAtualizada.copy(mae = pessoaIdNova)
                precisaAtualizar = true
            }
            
            // Atualizar referências em cônjuge
            if (pessoa.conjugeAtual == pessoaIdAntiga) {
                pessoaAtualizada = pessoaAtualizada.copy(conjugeAtual = pessoaIdNova)
                precisaAtualizar = true
            }
            
            // Atualizar referências em ex-cônjuges
            if (pessoaIdAntiga in pessoa.exConjuges) {
                pessoaAtualizada = pessoaAtualizada.copy(
                    exConjuges = pessoa.exConjuges.map { 
                        if (it == pessoaIdAntiga) pessoaIdNova else it 
                    }
                )
                precisaAtualizar = true
            }
            
            // Atualizar referências em filhos
            if (pessoaIdAntiga in pessoa.filhos) {
                pessoaAtualizada = pessoaAtualizada.copy(
                    filhos = pessoa.filhos.map { 
                        if (it == pessoaIdAntiga) pessoaIdNova else it 
                    }
                )
                precisaAtualizar = true
            }
            
            if (precisaAtualizar) {
                pessoaRepository.atualizar(pessoaAtualizada, ehAdmin = true)
            }
        }
    }
    
    fun marcarComoNaoDuplicatas(pessoaId1: String, pessoaId2: String) {
        // Por enquanto, apenas remove da lista
        // TODO: Salvar decisão no Firestore para não mostrar novamente
        _duplicatas.update { duplicatas ->
            duplicatas.filter { 
                !(it.pessoa1.id == pessoaId1 && it.pessoa2.id == pessoaId2) &&
                !(it.pessoa1.id == pessoaId2 && it.pessoa2.id == pessoaId1)
            }
        }
    }
    
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
}

data class ResolverDuplicatasState(
    val isLoading: Boolean = true,
    val erro: String? = null,
    val sucesso: String? = null,
    val ehAdmin: Boolean = false
)

