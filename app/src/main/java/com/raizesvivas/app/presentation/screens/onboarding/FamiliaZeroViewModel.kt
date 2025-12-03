package com.raizesvivas.app.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.FamiliaZeroRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.FamiliaZero
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.usecase.VerificarConquistasUseCase
import com.raizesvivas.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel para a tela de criação da Família Zero
 * 
 * Gerencia o estado do formulário de criação do casal raiz
 */
@HiltViewModel
class FamiliaZeroViewModel @Inject constructor(
    private val authService: AuthService,
    private val familiaZeroRepository: FamiliaZeroRepository,
    private val pessoaRepository: PessoaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val verificarConquistasUseCase: VerificarConquistasUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(FamiliaZeroState())
    val state = _state.asStateFlow()
    
    init {
        // Verificar se já existe Família Zero
        viewModelScope.launch {
            val existe = familiaZeroRepository.existe()
            _state.update { it.copy(familiaZeroJaExiste = existe) }
        }
    }
    
    /**
     * Atualiza o nome do patriarca
     */
    fun onNomePaiChanged(nome: String) {
        _state.update { it.copy(nomePai = nome, nomePaiError = null) }
    }
    
    /**
     * Atualiza o nome da matriarca
     */
    fun onNomeMaeChanged(nome: String) {
        _state.update { it.copy(nomeMae = nome, nomeMaeError = null) }
    }
    
    /**
     * Atualiza o nome da árvore
     */
    fun onNomeArvoreChanged(nome: String) {
        _state.update { it.copy(nomeArvore = nome) }
    }
    
    /**
     * Cria a Família Zero e o casal raiz
     */
    fun criarFamiliaZero() {
        // Limpar erros
        _state.update { it.copy(
            nomePaiError = null,
            nomeMaeError = null,
            error = null
        ) }
        
        // Validar nome do pai
        val validacaoPai = ValidationUtils.validarNome(_state.value.nomePai)
        if (!validacaoPai.isValid) {
            _state.update { it.copy(nomePaiError = validacaoPai.errorMessage) }
            return
        }
        
        // Validar nome da mãe
        val validacaoMae = ValidationUtils.validarNome(_state.value.nomeMae)
        if (!validacaoMae.isValid) {
            _state.update { it.copy(nomeMaeError = validacaoMae.errorMessage) }
            return
        }
        
        // Verificar se já existe
        if (_state.value.familiaZeroJaExiste) {
            _state.update { it.copy(error = "A Família Zero já foi criada!") }
            return
        }
        
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                val currentUser = authService.currentUser
                if (currentUser == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Usuário não autenticado"
                    ) }
                    return@launch
                }
                
                // Criar IDs únicos para pai e mãe
                val paiId = UUID.randomUUID().toString()
                val maeId = UUID.randomUUID().toString()
                
                // Criar pessoa do pai
                val pai = Pessoa(
                    id = paiId,
                    nome = _state.value.nomePai.trim(),
                    ehFamiliaZero = true,
                    distanciaFamiliaZero = 0,
                    aprovado = true, // Família Zero sempre aprovada
                    criadoPor = currentUser.uid,
                    modificadoPor = currentUser.uid,
                    criadoEm = Date(),
                    modificadoEm = Date()
                )
                
                // Criar pessoa da mãe
                val mae = Pessoa(
                    id = maeId,
                    nome = _state.value.nomeMae.trim(),
                    ehFamiliaZero = true,
                    distanciaFamiliaZero = 0,
                    aprovado = true, // Família Zero sempre aprovada
                    criadoPor = currentUser.uid,
                    modificadoPor = currentUser.uid,
                    criadoEm = Date(),
                    modificadoEm = Date()
                )
                
                // Vincular casal (cônjuges)
                val paiComConjuge = pai.copy(conjugeAtual = maeId)
                val maeComConjuge = mae.copy(conjugeAtual = paiId)
                
                // Salvar pessoas (admin = true, pois é o fundador)
                val usuarioId = currentUser.uid
                val resultadoPai = pessoaRepository.salvar(paiComConjuge, ehAdmin = true, usuarioId)
                val resultadoMae = pessoaRepository.salvar(maeComConjuge, ehAdmin = true, usuarioId)
                
                if (resultadoPai.isFailure || resultadoMae.isFailure) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Erro ao criar pessoas da Família Zero"
                    ) }
                    return@launch
                }
                
                // Criar Família Zero
                val familiaZero = FamiliaZero(
                    id = "raiz",
                    pai = paiId,
                    mae = maeId,
                    fundadoPor = currentUser.uid,
                    fundadoEm = Date(),
                    locked = true,
                    arvoreNome = _state.value.nomeArvore.trim()
                )
                
                val resultado = familiaZeroRepository.criar(familiaZero)
                
                resultado.onSuccess {
                    // Verificar conquistas após criar Família Zero
                    verificarConquistasUseCase.verificarTodasConquistas(currentUser.uid)
                    
                    // Atualizar usuário como administrador (garantir que seja admin se criou Família Zero)
                    val usuario = usuarioRepository.buscarPorId(currentUser.uid)
                    usuario?.let {
                        val usuarioAtualizado = it.copy(
                            ehAdministrador = true, // Sempre admin se criou Família Zero
                            familiaZeroPai = paiId,
                            familiaZeroMae = maeId,
                            primeiroAcesso = false
                        )
                        val resultadoAtualizacao = usuarioRepository.atualizar(usuarioAtualizado)
                        
                        resultadoAtualizacao.onSuccess {
                            Timber.d("✅ Usuário atualizado como admin após criar Família Zero")
                        }
                        
                        resultadoAtualizacao.onFailure { error ->
                            Timber.e(error, "⚠️ Erro ao atualizar usuário como admin, mas Família Zero foi criada")
                        }
                    } ?: run {
                        // Se usuário não existe ainda (raro), criar como admin
                        Timber.w("⚠️ Usuário não encontrado após criar Família Zero. Criando usuário admin.")
                        val novoUsuario = com.raizesvivas.app.domain.model.Usuario(
                            id = currentUser.uid,
                            nome = currentUser.displayName ?: "",
                            email = currentUser.email ?: "",
                            ehAdministrador = true,
                            familiaZeroPai = paiId,
                            familiaZeroMae = maeId,
                            primeiroAcesso = false,
                            criadoEm = Date()
                        )
                        usuarioRepository.salvar(novoUsuario)
                    }
                    
                    Timber.d("🌳 Família Zero criada com sucesso!")
                    _state.update { it.copy(isLoading = false, sucesso = true) }
                }
                
                resultado.onFailure { error ->
                    Timber.e(error, "❌ Erro ao criar Família Zero")
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Erro ao criar Família Zero"
                    ) }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro fatal ao criar Família Zero")
                _state.update { it.copy(
                    isLoading = false,
                    error = "Erro inesperado: ${e.message}"
                ) }
            }
        }
    }
}

/**
 * Estado da tela de Família Zero
 */
data class FamiliaZeroState(
    val nomePai: String = "",
    val nomeMae: String = "",
    val nomeArvore: String = "",
    val nomePaiError: String? = null,
    val nomeMaeError: String? = null,
    val isLoading: Boolean = false,
    val sucesso: Boolean = false,
    val error: String? = null,
    val familiaZeroJaExiste: Boolean = false
)

