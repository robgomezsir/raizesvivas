package com.raizesvivas.app.presentation.screens.cadastro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.remote.firebase.StorageService
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.EstadoCivil
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.usecase.DetectarDuplicatasUseCase
import com.raizesvivas.app.domain.usecase.VerificarConquistasUseCase
import com.raizesvivas.app.utils.DuplicateDetector
import com.raizesvivas.app.utils.ImageCompressor
import com.raizesvivas.app.utils.NetworkUtils
import com.raizesvivas.app.utils.ParentescoCalculator
import com.raizesvivas.app.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel para a tela de Cadastro/Edição de Pessoa
 * 
 * Gerencia o estado do formulário e salva pessoa no Firestore
 */
@HiltViewModel
class CadastroPessoaViewModel @Inject constructor(
    private val authService: AuthService,
    private val pessoaRepository: PessoaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val networkUtils: NetworkUtils,
    private val detectarDuplicatasUseCase: DetectarDuplicatasUseCase,
    private val storageService: StorageService,
    private val verificarConquistasUseCase: VerificarConquistasUseCase
) : ViewModel() {
    
    private val tituloLocale = Locale("pt", "BR")
    
    private val _state = MutableStateFlow(CadastroPessoaState())
    val state = _state.asStateFlow()
    
    private val _duplicatasEncontradas = MutableStateFlow<List<DuplicateDetector.DuplicataResultado>>(emptyList())
    val duplicatasEncontradas = _duplicatasEncontradas.asStateFlow()
    
    // Lista de pessoas para seleção de relacionamentos
    val pessoasDisponiveis: StateFlow<List<Pessoa>> = pessoaRepository
        .observarTodasPessoas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Carrega pessoa existente para edição
     */
    fun carregarPessoa(pessoaId: String) {
        if (pessoaId.isBlank()) return
        
        viewModelScope.launch {
            try {
                val pessoa = pessoaRepository.buscarPorId(pessoaId)
                if (pessoa != null) {
                    _state.update {
                        it.copy(
                            pessoaId = pessoa.id,
                            nome = formatarCapitalizacao(pessoa.nome),
                            apelido = formatarCapitalizacaoOpcional(pessoa.apelido) ?: "",
                            dataNascimento = pessoa.dataNascimento,
                            dataFalecimento = pessoa.dataFalecimento,
                            localNascimento = formatarCapitalizacaoOpcional(pessoa.localNascimento) ?: "",
                            localResidencia = formatarCapitalizacaoOpcional(pessoa.localResidencia) ?: "",
                            profissao = formatarCapitalizacaoOpcional(pessoa.profissao) ?: "",
                            biografia = pessoa.biografia ?: "",
                            telefone = pessoa.telefone ?: "",
                            estadoCivil = pessoa.estadoCivil,
                            genero = pessoa.genero,
                            paiId = pessoa.pai,
                            maeId = pessoa.mae,
                            conjugeId = pessoa.conjugeAtual,
                            exConjugesIds = pessoa.exConjuges,
                            filhosIds = pessoa.filhos,
                            fotoUrl = pessoa.fotoUrl,
                            isEditing = true
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar pessoa")
                _state.update { it.copy(erro = "Erro ao carregar pessoa: ${e.message}") }
            }
        }
    }
    
    // Campos do formulário
    fun onNomeChanged(nome: String) {
        _state.update { it.copy(nome = nome, nomeError = null) }
    }

    fun onApelidoChanged(apelido: String) {
        _state.update { it.copy(apelido = apelido) }
    }
    
    fun onDataNascimentoChanged(data: Date?) {
        _state.update { it.copy(dataNascimento = data) }
    }
    
    fun onDataFalecimentoChanged(data: Date?) {
        _state.update { it.copy(dataFalecimento = data) }
    }
    
    fun onLocalNascimentoChanged(local: String) {
        _state.update { it.copy(localNascimento = local) }
    }
    
    fun onLocalResidenciaChanged(local: String) {
        _state.update { it.copy(localResidencia = local) }
    }
    
    fun onProfissaoChanged(profissao: String) {
        _state.update { it.copy(profissao = profissao) }
    }
    
    fun onBiografiaChanged(biografia: String) {
        _state.update { it.copy(biografia = biografia) }
    }
    
    fun onTelefoneChanged(telefone: String) {
        _state.update { it.copy(telefone = telefone) }
    }
    
    fun onEstadoCivilChanged(estadoCivil: EstadoCivil?) {
        _state.update { 
            it.copy(
                estadoCivil = estadoCivil,
                // Se estado civil não for casado/uniao_estavel, limpar cônjuge
                conjugeId = if (estadoCivil == EstadoCivil.CASADO || estadoCivil == EstadoCivil.UNIAO_ESTAVEL) {
                    it.conjugeId
                } else {
                    null
                }
            )
        }
    }
    
    fun onGeneroChanged(genero: Genero?) {
        _state.update { it.copy(genero = genero) }
    }
    
    // Relacionamentos
    fun onPaiChanged(paiId: String?) {
        _state.update { it.copy(paiId = paiId) }
    }
    
    fun onMaeChanged(maeId: String?) {
        _state.update { it.copy(maeId = maeId) }
    }
    
    fun onConjugeChanged(conjugeId: String?) {
        _state.update { it.copy(conjugeId = conjugeId) }
    }
    
    /**
     * Salva ou atualiza pessoa
     */
    fun salvar() {
        // Limpar erros e normalizar campos com capitalização adequada
        _state.update { atual ->
            atual.copy(
                nomeError = null,
                erro = null,
                nome = formatarCapitalizacao(atual.nome),
                apelido = formatarCapitalizacaoOpcional(atual.apelido) ?: "",
                localNascimento = formatarCapitalizacaoOpcional(atual.localNascimento) ?: "",
                localResidencia = formatarCapitalizacaoOpcional(atual.localResidencia) ?: "",
                profissao = formatarCapitalizacaoOpcional(atual.profissao) ?: ""
            )
        }

        val estadoAtual = _state.value

        // Validar nome (obrigatório)
        val validacaoNome = ValidationUtils.validarNome(estadoAtual.nome)
        if (!validacaoNome.isValid) {
            _state.update { it.copy(nomeError = validacaoNome.errorMessage) }
            return
        }

        // Verificar conectividade antes de salvar
        if (!networkUtils.isConnected()) {
            _state.update {
                it.copy(
                    erro = "Sem conexão com a internet. Verifique sua conexão e tente novamente."
                )
            }
            return
        }

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val estadoParaSalvar = _state.value
                val currentUser = authService.currentUser
                if (currentUser == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        erro = "Usuário não autenticado"
                    ) }
                    return@launch
                }
                
                // Buscar usuário para verificar se é admin
                val usuario = usuarioRepository.buscarPorId(currentUser.uid)
                val ehAdmin = usuario?.ehAdministrador ?: false
                
                // Criar ou atualizar pessoa
                val pessoaId = if (estadoParaSalvar.isEditing) {
                    estadoParaSalvar.pessoaId
                } else {
                    UUID.randomUUID().toString()
                }

                val pessoaExistente = if (estadoParaSalvar.isEditing) {
                    pessoaRepository.buscarPorId(pessoaId)
                } else {
                    null
                }
                
                val pessoa = Pessoa(
                    id = pessoaId,
                    nome = estadoParaSalvar.nome,
                    apelido = estadoParaSalvar.apelido.takeIf { it.isNotBlank() },
                    dataNascimento = estadoParaSalvar.dataNascimento,
                    dataFalecimento = estadoParaSalvar.dataFalecimento,
                    localNascimento = estadoParaSalvar.localNascimento.takeIf { it.isNotBlank() },
                    localResidencia = estadoParaSalvar.localResidencia.takeIf { it.isNotBlank() },
                    profissao = estadoParaSalvar.profissao.takeIf { it.isNotBlank() },
                    biografia = estadoParaSalvar.biografia.takeIf { it.isNotBlank() },
                    telefone = estadoParaSalvar.telefone.takeIf { it.isNotBlank() },
                    estadoCivil = estadoParaSalvar.estadoCivil,
                    genero = estadoParaSalvar.genero,
                    pai = estadoParaSalvar.paiId,
                    mae = estadoParaSalvar.maeId,
                    conjugeAtual = estadoParaSalvar.conjugeId,
                    exConjuges = estadoParaSalvar.exConjugesIds,
                    filhos = estadoParaSalvar.filhosIds,
                    fotoUrl = estadoParaSalvar.fotoUrl,
                    criadoPor = if (estadoParaSalvar.isEditing) {
                        // Manter criador original
                        pessoaExistente?.criadoPor ?: currentUser.uid
                    } else {
                        currentUser.uid
                    },
                    criadoEm = if (estadoParaSalvar.isEditing) {
                        // Manter data original
                        pessoaExistente?.criadoEm ?: Date()
                    } else {
                        Date()
                    },
                    modificadoPor = currentUser.uid,
                    modificadoEm = Date(),
                    aprovado = ehAdmin, // Admin aprova automaticamente
                    versao = if (estadoParaSalvar.isEditing) {
                        // Incrementar versão se editando
                        (pessoaExistente?.versao ?: 1) + 1
                    } else {
                        1
                    },
                    ehFamiliaZero = pessoaExistente?.ehFamiliaZero ?: false,
                    distanciaFamiliaZero = pessoaExistente?.distanciaFamiliaZero ?: 0,
                    familias = pessoaExistente?.familias ?: emptyList(),
                    tipoFiliacao = pessoaExistente?.tipoFiliacao,
                    tipoNascimento = pessoaExistente?.tipoNascimento,
                    grupoGemelarId = pessoaExistente?.grupoGemelarId,
                    ordemNascimento = pessoaExistente?.ordemNascimento,
                    dataCasamento = pessoaExistente?.dataCasamento
                )
                
                // Se há foto para fazer upload, fazer antes de salvar
                val fotoUrl = estadoParaSalvar.fotoPath?.takeIf { !estadoParaSalvar.isEditing }
                    ?.let { path ->
                        fazerUploadFoto(path, pessoaId).getOrNull()
                    } ?: pessoa.fotoUrl
                
                val pessoaComFoto = pessoa.copy(fotoUrl = fotoUrl)
                
                val resultado = if (estadoParaSalvar.isEditing) {
                    pessoaRepository.atualizar(pessoaComFoto, ehAdmin)
                } else {
                    pessoaRepository.salvar(pessoaComFoto, ehAdmin)
                }
                
                resultado.onSuccess {
                    // Atualizar relacionamentos bidirecionais após salvar com sucesso
                    atualizarRelacionamentosBidirecionais(pessoaId, ehAdmin)
                    ParentescoCalculator.limparCache()

                    Timber.d("✅ Pessoa ${if (estadoParaSalvar.isEditing) "atualizada" else "salva"}: ${pessoa.nome}")
                    
                    val usuarioId = authService.currentUser?.uid
                    
                    if (!estadoParaSalvar.isEditing && usuarioId != null) { // Apenas para novas pessoas
                        // Detectar duplicatas após salvar
                        detectarDuplicatasAposSalvar(pessoa)
                        
                        // Verificar conquistas após adicionar nova pessoa
                        verificarConquistasUseCase.verificarTodasConquistas(usuarioId)
                    } else {
                        // Verificar conquistas após atualizar pessoa (pode ter adicionado foto, dados, etc.)
                        if (usuarioId != null) {
                            verificarConquistasUseCase.verificarTodasConquistas(usuarioId)
                        }
                    }
                    
                    _state.update { it.copy(isLoading = false, sucesso = true) }
                }
                
                resultado.onFailure { error ->
                    Timber.e(error, "❌ Erro ao salvar pessoa")
                    _state.update { it.copy(
                        isLoading = false,
                        erro = "Erro ao salvar pessoa: ${error.message}"
                    ) }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro fatal ao salvar pessoa")
                _state.update { it.copy(
                    isLoading = false,
                    erro = "Erro inesperado: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Atualiza relacionamentos bidirecionais
     * Ex: Se pessoa A tem pai B, então pessoa B deve ter pessoa A como filho
     */
    private suspend fun atualizarRelacionamentosBidirecionais(
        pessoaId: String,
        ehAdmin: Boolean
    ) {
        try {
            val currentUser = authService.currentUser ?: return
            val state = _state.value
            
            // Se tem pai, adicionar como filho do pai
            state.paiId?.let { paiId ->
                val pai = pessoaRepository.buscarPorId(paiId)
                pai?.let {
                    val filhosAtualizados = if (!it.filhos.contains(pessoaId)) {
                        it.filhos + pessoaId
                    } else {
                        it.filhos
                    }
                    val paiAtualizado = it.copy(
                        filhos = filhosAtualizados,
                        modificadoPor = currentUser.uid,
                        modificadoEm = Date()
                    )
                    pessoaRepository.atualizar(paiAtualizado, ehAdmin)
                        .onSuccess { ParentescoCalculator.limparCache() }
                }
            }
            
            // Se tem mãe, adicionar como filho da mãe
            state.maeId?.let { maeId ->
                val mae = pessoaRepository.buscarPorId(maeId)
                mae?.let {
                    val filhosAtualizados = if (!it.filhos.contains(pessoaId)) {
                        it.filhos + pessoaId
                    } else {
                        it.filhos
                    }
                    val maeAtualizado = it.copy(
                        filhos = filhosAtualizados,
                        modificadoPor = currentUser.uid,
                        modificadoEm = Date()
                    )
                    pessoaRepository.atualizar(maeAtualizado, ehAdmin)
                        .onSuccess { ParentescoCalculator.limparCache() }
                }
            }
            
            // Se tem cônjuge, vincular bidirecionalmente
            state.conjugeId?.let { conjugeId ->
                val conjuge = pessoaRepository.buscarPorId(conjugeId)
                conjuge?.let {
                    val conjugeAtualizado = it.copy(
                        conjugeAtual = pessoaId,
                        modificadoPor = currentUser.uid,
                        modificadoEm = Date()
                    )
                    val resultadoConjuge = pessoaRepository.atualizar(conjugeAtualizado, ehAdmin)

                    // Verificar conquistas após registrar casamento (se ambos tinham conjugeAtual null antes)
                    resultadoConjuge.onSuccess {
                        ParentescoCalculator.limparCache()
                        val usuarioId = authService.currentUser?.uid
                        if (usuarioId != null && conjuge.conjugeAtual == null) {
                            // Novo casamento registrado
                            verificarConquistasUseCase.verificarTodasConquistas(usuarioId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Erro ao atualizar relacionamentos bidirecionais")
            // Não falhar o salvamento principal por causa disso
        }
    }
    
    /**
     * Remove foto selecionada
     */
    fun removerFoto() {
        _state.update { it.copy(fotoPath = null, fotoUrl = null) }
    }
    
    /**
     * Atualiza estado quando foto é selecionada
     */
    fun onFotoSelecionada(path: String) {
        _state.update { it.copy(fotoPath = path) }
    }
    
    /**
     * Faz upload da foto para o Storage e retorna URL
     */
    private suspend fun fazerUploadFoto(path: String, pessoaId: String): Result<String> {
        return try {
            // Comprimir imagem antes de fazer upload
            val compressedFile = ImageCompressor.compressToFile(path, 10240) // 10KB
            if (compressedFile == null) {
                return Result.failure(Exception("Erro ao comprimir imagem"))
            }
            
            // Upload para Storage
            storageService.uploadPessoaPhoto(compressedFile, pessoaId)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao fazer upload da foto")
            Result.failure(e)
        }
    }

    private fun formatarCapitalizacao(texto: String): String {
        if (texto.isBlank()) return ""
        return texto
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { capitalizarSegmentos(it) }
    }

    private fun formatarCapitalizacaoOpcional(texto: String?): String? {
        if (texto.isNullOrBlank()) return null
        val formatado = formatarCapitalizacao(texto)
        return formatado.takeIf { it.isNotBlank() }
    }

    private fun capitalizarSegmentos(palavraOriginal: String): String {
        if (palavraOriginal.isBlank()) return palavraOriginal
        val delimitadores = charArrayOf('-', '\'')
        val palavra = palavraOriginal.lowercase(tituloLocale)
        val resultado = StringBuilder(palavra.length)
        var inicioSegmento = true

        for (char in palavra) {
            val isDelimitador = delimitadores.any { it == char }
            if (inicioSegmento && char.isLetter()) {
                resultado.append(char.titlecase(tituloLocale))
            } else {
                resultado.append(char)
            }
            inicioSegmento = isDelimitador
        }

        return resultado.toString()
    }
    
    /**
     * Detecta duplicatas após salvar nova pessoa
     */
    private fun detectarDuplicatasAposSalvar(pessoa: Pessoa) {
        viewModelScope.launch {
            try {
                val duplicatas = detectarDuplicatasUseCase.executar(pessoa)
                if (duplicatas.isNotEmpty()) {
                    _duplicatasEncontradas.update { duplicatas }
                    _state.update { it.copy(avisoDuplicatas = "Foram encontradas possíveis duplicatas. Revise-as no menu de perfil.") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao detectar duplicatas")
            }
        }
    }
}

/**
 * Estado da tela de Cadastro Pessoa
 */
data class CadastroPessoaState(
    val pessoaId: String = "",
    val nome: String = "",
    val apelido: String = "",
    val dataNascimento: Date? = null,
    val dataFalecimento: Date? = null,
    val localNascimento: String = "",
    val localResidencia: String = "",
    val profissao: String = "",
    val biografia: String = "",
    val telefone: String = "",
    val estadoCivil: EstadoCivil? = null,
    val genero: Genero? = null,
    
    // Relacionamentos
    val paiId: String? = null,
    val maeId: String? = null,
    val conjugeId: String? = null,
    val exConjugesIds: List<String> = emptyList(),
    val filhosIds: List<String> = emptyList(),
    
    // Outros
    val fotoUrl: String? = null,
    val fotoPath: String? = null, // Caminho local da foto selecionada
    
    // Erros e estados
    val nomeError: String? = null,
    val erro: String? = null,
    val isLoading: Boolean = false,
    val sucesso: Boolean = false,
    val isEditing: Boolean = false,
    val avisoDuplicatas: String? = null
)

