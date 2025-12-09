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
import com.raizesvivas.app.domain.usecase.ValidarDuplicataUseCase
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
    private val validarDuplicataUseCase: ValidarDuplicataUseCase,
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
                // Sempre fazer upload se houver fotoPath (nova foto selecionada)
                val fotoUrl = if (estadoParaSalvar.fotoPath != null) {
                    val fotoPath = estadoParaSalvar.fotoPath
                    // Validar se o arquivo existe
                    val arquivo = java.io.File(fotoPath)
                    if (!arquivo.exists()) {
                        Timber.e("❌ Arquivo de imagem não existe: $fotoPath")
                        _state.update { it.copy(
                            isLoading = false,
                            erro = "Arquivo de imagem não encontrado. Tente selecionar a imagem novamente."
                        ) }
                        return@launch
                    }
                    
                    Timber.d("📤 Fazendo upload da foto do perfil...")
                    val uploadResult = fazerUploadFoto(fotoPath, pessoaId)
                    uploadResult.fold(
                        onSuccess = { url ->
                            Timber.d("✅ Upload da foto concluído: $url")
                            url
                        },
                        onFailure = { exception ->
                            Timber.e(exception, "❌ Erro no upload da foto")
                            _state.update { it.copy(
                                isLoading = false,
                                erro = "Erro ao fazer upload da foto: ${exception.message ?: "Erro desconhecido"}"
                            ) }
                            return@launch
                        }
                    )
                } else {
                    // Se não há nova foto, manter a fotoUrl existente
                    estadoParaSalvar.fotoUrl ?: pessoa.fotoUrl
                }
                
                val pessoaComFoto = pessoa.copy(fotoUrl = fotoUrl)
                
                // Validar duplicatas ANTES de salvar (apenas para novos cadastros)
                if (!estadoParaSalvar.isEditing) {
                    val validacaoDuplicata = validarDuplicataUseCase.validar(pessoaComFoto, toleranciaDias = 0)
                    
                    if (validacaoDuplicata.deveBloquear || validacaoDuplicata.deveAvisar) {
                        // Duplicata encontrada - PAUSAR cadastro e mostrar diálogo
                        _state.update {
                            it.copy(
                                isLoading = false,
                                erro = if (validacaoDuplicata.deveBloquear) validacaoDuplicata.mensagem else null,
                                avisoDuplicatas = if (validacaoDuplicata.deveAvisar) validacaoDuplicata.mensagem else null,
                                duplicatasEncontradas = validacaoDuplicata.duplicatasEncontradas.map { dup ->
                                    DuplicataInfo(
                                        pessoaId = dup.pessoa.id,
                                        nome = dup.pessoa.nome,
                                        dataNascimento = dup.pessoa.dataNascimento,
                                        nivel = dup.nivel.name,
                                        razoes = dup.razoes,
                                        scoreSimilaridade = dup.scoreSimilaridade
                                    )
                                },
                                mostrarDialogDuplicata = true,
                                pessoaPendente = pessoaComFoto, // Guardar pessoa para salvar depois
                                ehAdminPendente = ehAdmin
                            )
                        }
                        return@launch
                    }
                }
                
                // Salvar pessoa (sem duplicatas ou após confirmação)
                salvarPessoaInterno(pessoaComFoto, ehAdmin, estadoParaSalvar.isEditing, pessoaId)
                
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
     * Salva pessoa internamente (extraído para reutilização)
     */
    private suspend fun salvarPessoaInterno(
        pessoa: Pessoa,
        ehAdmin: Boolean,
        isEditing: Boolean,
        pessoaId: String
    ) {
        // Buscar pessoa original ANTES de salvar para comparar relacionamentos (apenas se estiver editando)
        val pessoaOriginal = if (isEditing) {
            pessoaRepository.buscarPorId(pessoaId)
        } else {
            null
        }
        
        val usuarioId = authService.currentUser?.uid
        
        val resultado = if (isEditing) {
            pessoaRepository.atualizar(pessoa, ehAdmin)
        } else {
            pessoaRepository.salvar(pessoa, ehAdmin, usuarioId)
        }
        
        resultado.onSuccess {
            Timber.d("✅ Pessoa ${if (isEditing) "atualizada" else "salva"}: ${pessoa.nome}")
            
            // Atualizar relacionamentos bidirecionais após salvar com sucesso
            atualizarRelacionamentosBidirecionais(pessoaId, pessoaOriginal, pessoa, ehAdmin)
            ParentescoCalculator.limparCache()
            
            if (!isEditing && usuarioId != null) { // Apenas para novas pessoas
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
            
            // Atualizar estado com fotoUrl após salvar com sucesso
            // Adicionar timestamp para forçar reload da imagem (cache busting)
            val fotoUrlComCacheBuster = pessoa.fotoUrl?.let { url ->
                if (url.contains("?")) {
                    "$url&t=${System.currentTimeMillis()}"
                } else {
                    "$url?t=${System.currentTimeMillis()}"
                }
            }
            
            _state.update { 
                it.copy(
                    isLoading = false, 
                    sucesso = true,
                    fotoUrl = fotoUrlComCacheBuster, // Atualizar com a URL da foto + cache buster
                    fotoPath = null // Limpar caminho local após upload bem-sucedido
                ) 
            }
        }
        
        resultado.onFailure { error ->
            Timber.e(error, "❌ Erro ao salvar pessoa")
            _state.update { it.copy(
                isLoading = false,
                erro = "Erro ao salvar pessoa: ${error.message}"
            ) }
        }
    }
    
    /**
     * Atualiza relacionamentos bidirecionais
     * Ex: Se pessoa A tem pai B, então pessoa B deve ter pessoa A como filho
     * Também remove relacionamentos quando são removidos
     */
    private suspend fun atualizarRelacionamentosBidirecionais(
        pessoaId: String,
        pessoaOriginal: Pessoa?,
        pessoaAtualizada: Pessoa,
        ehAdmin: Boolean
    ) {
        try {
            val currentUser = authService.currentUser ?: return
            
            // ========== ATUALIZAR/REMOVER RELACIONAMENTO COM PAI ==========
            val paiAnterior = pessoaOriginal?.pai
            val paiAtual = pessoaAtualizada.pai
            
            // Se tinha pai antes e agora não tem, ou mudou de pai
            if (paiAnterior != null && paiAnterior != paiAtual) {
                // Remover da lista de filhos do pai anterior
                val paiAnteriorObj = pessoaRepository.buscarPorId(paiAnterior)
                paiAnteriorObj?.let {
                    val filhosAtualizados = it.filhos.filter { it != pessoaId }
                    val paiAtualizado = it.copy(
                        filhos = filhosAtualizados,
                        modificadoPor = currentUser.uid,
                        modificadoEm = Date()
                    )
                    pessoaRepository.atualizar(paiAtualizado, ehAdmin)
                        .onSuccess { 
                            Timber.d("🔗 Removido relacionamento: ${paiAnteriorObj.nome} não é mais pai de ${pessoaAtualizada.nome}")
                            ParentescoCalculator.limparCache() 
                        }
                }
            }
            
            // Se tem pai agora (novo ou mantido), adicionar como filho do pai
            paiAtual?.let { paiId ->
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
                        .onSuccess { 
                            Timber.d("🔗 Adicionado relacionamento: ${pai.nome} é pai de ${pessoaAtualizada.nome}")
                            ParentescoCalculator.limparCache() 
                        }
                }
            }
            
            // ========== ATUALIZAR/REMOVER RELACIONAMENTO COM MÃE ==========
            val maeAnterior = pessoaOriginal?.mae
            val maeAtual = pessoaAtualizada.mae
            
            // Se tinha mãe antes e agora não tem, ou mudou de mãe
            if (maeAnterior != null && maeAnterior != maeAtual) {
                // Remover da lista de filhos da mãe anterior
                val maeAnteriorObj = pessoaRepository.buscarPorId(maeAnterior)
                maeAnteriorObj?.let {
                    val filhosAtualizados = it.filhos.filter { it != pessoaId }
                    val maeAtualizada = it.copy(
                        filhos = filhosAtualizados,
                        modificadoPor = currentUser.uid,
                        modificadoEm = Date()
                    )
                    pessoaRepository.atualizar(maeAtualizada, ehAdmin)
                        .onSuccess { 
                            Timber.d("🔗 Removido relacionamento: ${maeAnteriorObj.nome} não é mais mãe de ${pessoaAtualizada.nome}")
                            ParentescoCalculator.limparCache() 
                        }
                }
            }
            
            // Se tem mãe agora (nova ou mantida), adicionar como filho da mãe
            maeAtual?.let { maeId ->
                val mae = pessoaRepository.buscarPorId(maeId)
                mae?.let {
                    val filhosAtualizados = if (!it.filhos.contains(pessoaId)) {
                        it.filhos + pessoaId
                    } else {
                        it.filhos
                    }
                    val maeAtualizada = it.copy(
                        filhos = filhosAtualizados,
                        modificadoPor = currentUser.uid,
                        modificadoEm = Date()
                    )
                    pessoaRepository.atualizar(maeAtualizada, ehAdmin)
                        .onSuccess { 
                            Timber.d("🔗 Adicionado relacionamento: ${mae.nome} é mãe de ${pessoaAtualizada.nome}")
                            ParentescoCalculator.limparCache() 
                        }
                }
            }
            
            // ========== ATUALIZAR/REMOVER RELACIONAMENTO COM CÔNJUGE ==========
            val conjugeAnterior = pessoaOriginal?.conjugeAtual
            val conjugeAtual = pessoaAtualizada.conjugeAtual
            
            // Se tinha cônjuge antes e agora não tem, ou mudou de cônjuge
            if (conjugeAnterior != null && conjugeAnterior != conjugeAtual) {
                // Remover relacionamento do cônjuge anterior
                val conjugeAnteriorObj = pessoaRepository.buscarPorId(conjugeAnterior)
                conjugeAnteriorObj?.let {
                    val conjugeAtualizado = it.copy(
                        conjugeAtual = null,
                        modificadoPor = currentUser.uid,
                        modificadoEm = Date()
                    )
                    pessoaRepository.atualizar(conjugeAtualizado, ehAdmin)
                        .onSuccess { 
                            Timber.d("🔗 Removido relacionamento: ${conjugeAnteriorObj.nome} não é mais cônjuge de ${pessoaAtualizada.nome}")
                            ParentescoCalculator.limparCache() 
                        }
                }
            }
            
            // Se tem cônjuge agora (novo ou mantido), vincular bidirecionalmente
            conjugeAtual?.let { conjugeId ->
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
                        Timber.d("🔗 Adicionado relacionamento: ${conjuge.nome} é cônjuge de ${pessoaAtualizada.nome}")
                        ParentescoCalculator.limparCache()
                        val usuarioId = authService.currentUser?.uid
                        if (usuarioId != null && conjuge.conjugeAtual == null) {
                            // Novo casamento registrado
                            verificarConquistasUseCase.verificarTodasConquistas(usuarioId)
                        }
                    }
                }
            }
            
            // ========== ATUALIZAR/REMOVER RELACIONAMENTO COM FILHOS ==========
            val filhosAnteriores = pessoaOriginal?.filhos ?: emptyList()
            val filhosAtuais = pessoaAtualizada.filhos
            
            // Filhos que foram removidos
            val filhosRemovidos = filhosAnteriores.filter { it !in filhosAtuais }
            filhosRemovidos.forEach { filhoId ->
                val filho = pessoaRepository.buscarPorId(filhoId)
                filho?.let {
                    val filhoAtualizado = when {
                        it.pai == pessoaId -> it.copy(
                            pai = null,
                            modificadoPor = currentUser.uid,
                            modificadoEm = Date()
                        )
                        it.mae == pessoaId -> it.copy(
                            mae = null,
                            modificadoPor = currentUser.uid,
                            modificadoEm = Date()
                        )
                        else -> null
                    }
                    filhoAtualizado?.let { filhoAtual ->
                        pessoaRepository.atualizar(filhoAtual, ehAdmin)
                            .onSuccess { 
                                Timber.d("🔗 Removido relacionamento: ${pessoaAtualizada.nome} não é mais pai/mãe de ${filhoAtual.nome}")
                                ParentescoCalculator.limparCache() 
                            }
                    }
                }
            }
            
            // Filhos que foram adicionados
            val filhosAdicionados = filhosAtuais.filter { it !in filhosAnteriores }
            filhosAdicionados.forEach { filhoId ->
                val filho = pessoaRepository.buscarPorId(filhoId)
                filho?.let {
                    // Determinar se deve ser pai ou mãe baseado no gênero
                    // Se gênero não está definido, verificar qual campo (pai ou mae) está vazio
                    val filhoAtualizado = when {
                        pessoaAtualizada.genero == Genero.MASCULINO && it.pai != pessoaId -> {
                            it.copy(
                                pai = pessoaId,
                                modificadoPor = currentUser.uid,
                                modificadoEm = Date()
                            )
                        }
                        pessoaAtualizada.genero == Genero.FEMININO && it.mae != pessoaId -> {
                            it.copy(
                                mae = pessoaId,
                                modificadoPor = currentUser.uid,
                                modificadoEm = Date()
                            )
                        }
                        // Se gênero não está definido, preencher o campo que estiver vazio
                        pessoaAtualizada.genero == null -> {
                            when {
                                it.pai == null && it.mae != pessoaId -> {
                                    it.copy(
                                        pai = pessoaId,
                                        modificadoPor = currentUser.uid,
                                        modificadoEm = Date()
                                    )
                                }
                                it.mae == null && it.pai != pessoaId -> {
                                    it.copy(
                                        mae = pessoaId,
                                        modificadoPor = currentUser.uid,
                                        modificadoEm = Date()
                                    )
                                }
                                else -> null // Ambos já preenchidos ou já está correto
                            }
                        }
                        else -> null // Já está correto
                    }
                    filhoAtualizado?.let { filhoAtual ->
                        pessoaRepository.atualizar(filhoAtual, ehAdmin)
                            .onSuccess { 
                                Timber.d("🔗 Adicionado relacionamento: ${pessoaAtualizada.nome} é pai/mãe de ${filhoAtual.nome}")
                                ParentescoCalculator.limparCache() 
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
     * Remove foto de perfil da pessoa no Firestore
     * TODOS os usuários autenticados podem remover fotos
     */
    fun removerFoto() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val pessoaId = _state.value.pessoaId
            if (pessoaId.isNullOrBlank()) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Pessoa não encontrada"
                    )
                }
                return@launch
            }
            
            Timber.d("🗑️ Removendo foto da pessoa: $pessoaId")
            
            pessoaRepository.removerFoto(pessoaId).fold(
                onSuccess = {
                    _state.update { state ->
                        state.copy(
                            fotoPath = null,
                            fotoUrl = null,
                            isLoading = false
                        )
                    }
                    Timber.d("✅ Foto removida com sucesso no ViewModel")
                },
                onFailure = { erro ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = erro.message ?: "Erro ao remover foto"
                        )
                    }
                    Timber.e(erro, "❌ Erro ao remover foto no ViewModel")
                }
            )
        }
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
            Timber.d("🗜️ Comprimindo imagem do perfil...")
            // Comprimir imagem antes de fazer upload (250KB para perfil)
            val compressedFile = ImageCompressor.compressToFile(path, targetSizeKB = 250, paraPerfil = true)
            if (compressedFile == null) {
                Timber.e("❌ Erro ao comprimir imagem")
                return Result.failure(Exception("Erro ao comprimir imagem. Verifique se o arquivo é uma imagem válida."))
            }
            Timber.d("✅ Imagem comprimida: ${compressedFile.absolutePath}")
            
            // Upload para Storage
            Timber.d("📤 Fazendo upload para Storage...")
            val result = storageService.uploadPessoaPhoto(compressedFile, pessoaId)
            result.onSuccess {
                Timber.d("✅ Upload concluído: $it")
            }.onFailure { e ->
                Timber.e(e, "❌ Erro no upload para Storage")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao fazer upload da foto")
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
    
    /**
     * Fecha o diálogo de duplicatas e permite continuar com o cadastro
     */
    fun confirmarContinuarComDuplicata() {
        val estado = _state.value
        val pessoaPendente = estado.pessoaPendente
        val ehAdminPendente = estado.ehAdminPendente
        
        if (pessoaPendente != null) {
            _state.update { 
                it.copy(
                    isLoading = true,
                    mostrarDialogDuplicata = false,
                    duplicatasEncontradas = emptyList(),
                    avisoDuplicatas = null,
                    pessoaPendente = null,
                    ehAdminPendente = false
                ) 
            }
            
            viewModelScope.launch {
                val pessoaId = pessoaPendente.id
                salvarPessoaInterno(pessoaPendente, ehAdminPendente, false, pessoaId)
            }
        } else {
            _state.update { 
                it.copy(
                    mostrarDialogDuplicata = false,
                    duplicatasEncontradas = emptyList(),
                    avisoDuplicatas = null
                ) 
            }
        }
    }
    
    /**
     * Cancela o cadastro devido a duplicata
     */
    fun cancelarPorDuplicata() {
        _state.update { 
            it.copy(
                isLoading = false,
                mostrarDialogDuplicata = false,
                duplicatasEncontradas = emptyList(),
                avisoDuplicatas = null
            ) 
        }
    }
    
    /**
     * Fecha o diálogo de duplicatas sem ação
     */
    fun fecharDialogDuplicata() {
        _state.update { 
            it.copy(
                mostrarDialogDuplicata = false
            ) 
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
    val avisoDuplicatas: String? = null,
    
    // Duplicatas
    val duplicatasEncontradas: List<DuplicataInfo> = emptyList(),
    val mostrarDialogDuplicata: Boolean = false,
    val pessoaPendente: Pessoa? = null, // Pessoa aguardando confirmação para salvar
    val ehAdminPendente: Boolean = false // Se era admin quando tentou salvar
)

/**
 * Informações sobre uma duplicata encontrada
 */
data class DuplicataInfo(
    val pessoaId: String,
    val nome: String,
    val dataNascimento: Date?,
    val nivel: String, // CRITICO, ALTO, MEDIO
    val razoes: List<String>,
    val scoreSimilaridade: Float
)

