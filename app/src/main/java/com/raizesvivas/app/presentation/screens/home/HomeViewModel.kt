package com.raizesvivas.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.FamiliaZeroRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.data.remote.firebase.FirestoreService
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.Usuario
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.FamiliaZero
import com.raizesvivas.app.presentation.screens.familia.FamiliaUiModel
import com.raizesvivas.app.domain.usecase.GerarDadosTesteUseCase
import com.raizesvivas.app.utils.ParentescoCalculator
import com.raizesvivas.app.utils.MinhaFamiliaPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel para a tela Home
 * 
 * Gerencia o estado da tela principal do app
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authService: AuthService,
    private val usuarioRepository: UsuarioRepository,
    private val pessoaRepository: PessoaRepository,
    private val familiaZeroRepository: FamiliaZeroRepository,
    private val gerarDadosTesteUseCase: GerarDadosTesteUseCase,
    private val firestoreService: FirestoreService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _mostrarModalFamiliaZero = MutableStateFlow(false)
    val mostrarModalFamiliaZero = _mostrarModalFamiliaZero.asStateFlow()
    
    private val _mostrarModalEditarNome = MutableStateFlow(false)
    val mostrarModalEditarNome = _mostrarModalEditarNome.asStateFlow()
    
    private val _mostrarModalMinhaFamilia = MutableStateFlow(false)
    val mostrarModalMinhaFamilia = _mostrarModalMinhaFamilia.asStateFlow()
    
    private val _minhaFamiliaId = MutableStateFlow<String?>(null)
    val minhaFamiliaId = _minhaFamiliaId.asStateFlow()
    
    private val _minhaFamiliaNome = MutableStateFlow<String?>(null)
    val minhaFamiliaNome = _minhaFamiliaNome.asStateFlow()
    
    /**
     * Busca usuário por pessoa vinculada
     */
    suspend fun buscarUsuarioPorPessoaId(pessoaId: String): Usuario? {
        return usuarioRepository.buscarUsuarioPorPessoaId(pessoaId)
    }
    
    fun abrirModalFamiliaZero() {
        _mostrarModalFamiliaZero.value = true
    }
    
    fun fecharModalFamiliaZero() {
        _mostrarModalFamiliaZero.value = false
    }
    
    fun abrirModalEditarNome() {
        _mostrarModalEditarNome.value = true
    }
    
    fun fecharModalEditarNome() {
        _mostrarModalEditarNome.value = false
    }
    
    fun logout() {
        authService.logout()
    }
    
    /**
     * Atualiza o nome da Família Zero
     */
    fun atualizarNomeFamiliaZero(nome: String) {
        viewModelScope.launch {
            try {
                val usuarioId = authService.currentUser?.uid
                if (usuarioId == null) {
                    _state.update { it.copy(erro = "Usuário não autenticado") }
                    return@launch
                }
                
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val familiaZeroAtual = familiaZeroRepository.buscar()
                if (familiaZeroAtual == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Família Zero não encontrada"
                        ) 
                    }
                    return@launch
                }
                
                val familiaZeroAtualizada = familiaZeroAtual.copy(
                    arvoreNome = nome.trim()
                )
                
                val resultado = familiaZeroRepository.salvar(familiaZeroAtualizada)
                
                resultado.onSuccess {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            familiaZeroNome = nome.trim()
                        ) 
                    }
                    _mostrarModalEditarNome.value = false
                    Timber.d("✅ Nome da Família Zero atualizado: $nome")
                }
                
                resultado.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao atualizar nome: ${error.message}"
                        )
                    }
                    Timber.e(error, "❌ Erro ao atualizar nome da Família Zero")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao atualizar nome da Família Zero")
                _state.update {
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao atualizar nome: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Verifica se o usuário atual é o primeiro ADMIN
     */
    fun verificarPrimeiroAdmin(callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val ehPrimeiro = usuarioRepository.ehPrimeiroUsuario()
                callback(ehPrimeiro)
            } catch (e: Exception) {
                Timber.e(e, "Erro ao verificar primeiro usuário")
                callback(false)
            }
        }
    }
    
    /**
     * Define a Família Zero com o casal selecionado
     */
    fun definirFamiliaZero(paiId: String, maeId: String) {
        viewModelScope.launch {
            try {
                val usuarioId = authService.currentUser?.uid
                if (usuarioId == null) {
                    _state.update { it.copy(erro = "Usuário não autenticado") }
                    return@launch
                }
                
                _state.update { it.copy(isLoading = true, erro = null) }
                
                // Buscar nomes dos cônjuges
                val pai = pessoaRepository.buscarPorId(paiId)
                val mae = pessoaRepository.buscarPorId(maeId)
                
                if (pai == null || mae == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Pessoa não encontrada"
                        ) 
                    }
                    return@launch
                }
                
                // Criar/atualizar Família Zero
                val familiaZero = FamiliaZero(
                    pai = paiId,
                    mae = maeId,
                    fundadoPor = usuarioId,
                    arvoreNome = "${pai.nome.split(" ").first()} & ${mae.nome.split(" ").first()}"
                )
                
                val resultado = familiaZeroRepository.salvar(familiaZero)
                
                resultado.onSuccess {
                    // IMPORTANTE: Remover flag ehFamiliaZero de TODAS as pessoas antes de definir a nova Família Zero
                    // Isso garante que apenas o casal selecionado seja a Família Zero
                    val todasPessoas = pessoaRepository.observarTodasPessoas().first()
                    val pessoasComFamiliaZero = todasPessoas.filter { it.ehFamiliaZero }
                    
                    Timber.d("🔄 Removendo flag Família Zero de ${pessoasComFamiliaZero.size} pessoa(s) antes de definir nova Família Zero")
                    
                    pessoasComFamiliaZero.forEach { pessoaAntiga ->
                        if (pessoaAntiga.id != paiId && pessoaAntiga.id != maeId) {
                            val pessoaAtualizada = pessoaAntiga.copy(ehFamiliaZero = false)
                            pessoaRepository.salvar(pessoaAtualizada, ehAdmin = true, usuarioId)
                            Timber.d("   ➖ Removido Família Zero de: ${pessoaAntiga.nome}")
                        }
                    }
                    
                    // Agora marcar o novo casal como Família Zero
                    val paiAtualizado = pai.copy(
                        ehFamiliaZero = true,
                        distanciaFamiliaZero = 0,
                        conjugeAtual = maeId
                    )
                    val maeAtualizada = mae.copy(
                        ehFamiliaZero = true,
                        distanciaFamiliaZero = 0,
                        conjugeAtual = paiId
                    )
                    
                    pessoaRepository.salvar(paiAtualizado, ehAdmin = true, usuarioId)
                    pessoaRepository.salvar(maeAtualizada, ehAdmin = true, usuarioId)
                    
                    Timber.d("   ✅ Marcado como Família Zero: ${pai.nome} e ${mae.nome}")
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            mostrarModalFamiliaZero = false,
                            familiaZeroPaiNome = pai.nome,
                            familiaZeroMaeNome = mae.nome
                        )
                    }
                    
                    // Recarregar dados para atualizar todas as abas
                    recarregar()
                    
                    // Forçar atualização da observação de Família Zero
                    // As outras abas também observam via Firestore, então serão atualizadas automaticamente
                    Timber.d("✅ Família Zero definida com sucesso!")
                }.onFailure { error ->
                    Timber.e(error, "❌ Erro ao definir Família Zero")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao definir Família Zero: ${error.message}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao definir Família Zero")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao definir Família Zero: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()
    
    // Observar pessoas em tempo real e aplicar filtros/ordenação
    // Otimizado: filtros e ordenação executados em background thread
    val pessoas: StateFlow<List<Pessoa>> = combine(
        pessoaRepository.observarTodasPessoas(),
        state.map { it.termoBusca },
        state.map { it.ordenacao }
    ) { todasPessoas, termoBusca, ordenacao ->
        var resultado = todasPessoas
        
        // Aplicar busca
        if (termoBusca.isNotBlank()) {
            resultado = filtrarPessoas(todasPessoas, termoBusca)
        }
        
        // Aplicar ordenação
        resultado = ordenarPessoas(resultado, ordenacao)
        
        resultado
    }
        .flowOn(Dispatchers.Default) // Executar filtros/ordenação em background
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Cache de parentescos para evitar recálculos desnecessários
    private var parentescosCache: Pair<String?, List<Pair<Pessoa, ParentescoCalculator.ResultadoParentesco>>>? = null

    // Contador de pedidos de convite pendentes (para badge)
    private val _pedidosPendentes = MutableStateFlow(0)
    val pedidosPendentes = _pedidosPendentes.asStateFlow()
    
    // Parentescos calculados para o usuário vinculado
    // Otimizado: cálculos pesados executados em background thread + cache
    val parentescos: StateFlow<List<Pair<Pessoa, ParentescoCalculator.ResultadoParentesco>>> = combine(
        pessoaRepository.observarTodasPessoas(),
        state.map { it.usuario?.pessoaVinculada }
    ) { todasPessoas, pessoaVinculadaId ->
        // Verificar cache primeiro
        val cacheKey = "${pessoaVinculadaId}_${todasPessoas.map { it.id }.sorted().joinToString("_")}"
        val cached = parentescosCache
        if (cached != null && cached.first == cacheKey) {
            Timber.d("✅ Usando cache de parentescos (${cached.second.size} parentes)")
            return@combine cached.second
        }
        
        if (pessoaVinculadaId == null || todasPessoas.isEmpty()) {
            emptyList()
        } else {
            val pessoaVinculada = todasPessoas.find { it.id == pessoaVinculadaId }
            if (pessoaVinculada != null) {
                // Otimizar: criar map apenas uma vez e reutilizar
                val pessoasMap = todasPessoas.associateBy { it.id }
                // Calcular parentescos (cálculo pesado, mas necessário)
                val resultado = ParentescoCalculator.calcularTodosParentescos(
                    pessoaReferencia = pessoaVinculada,
                    todasPessoas = todasPessoas,
                    pessoasMap = pessoasMap
                )
                val resultadoOrdenado = resultado.sortedWith(
                    compareBy<Pair<Pessoa, ParentescoCalculator.ResultadoParentesco>> {
                        it.first.getNomeExibicao().lowercase(Locale.getDefault())
                    }.thenBy { it.first.id }
                )
                // Atualizar cache
                parentescosCache = cacheKey to resultadoOrdenado
                Timber.d("💾 Cache de parentescos atualizado (${resultadoOrdenado.size} parentes)")
                resultadoOrdenado
            } else {
                emptyList()
            }
        }
    }
        .distinctUntilChanged() // Evitar emissões duplicadas
        .flowOn(Dispatchers.Default) // Executar cálculo pesado em background
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        carregarDados()
        observarFamiliaZero()
        observarEstatisticasGenero()
        promoverPrimeiroAdminSenior()
        carregarMinhaFamilia()
        atualizarPedidosPendentes()
    }
    
    /**
     * Carrega a preferência de "Minha família"
     * O nome será atualizado quando as famílias forem carregadas
     */
    private fun carregarMinhaFamilia() {
        viewModelScope.launch {
            try {
                val familiaId = MinhaFamiliaPreferences.obterFamiliaId(context)
                _minhaFamiliaId.value = familiaId
                // O nome será atualizado quando as famílias forem observadas
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao carregar Minha Família")
            }
        }
    }
    
    /**
     * Atualiza o nome da "Minha família" baseado no ID salvo
     * Deve ser chamado quando as famílias forem carregadas
     */
    fun atualizarNomeMinhaFamilia(familias: List<com.raizesvivas.app.presentation.screens.familia.FamiliaUiModel>) {
        val familiaId = _minhaFamiliaId.value
        if (familiaId != null) {
            val familia = familias.find { it.id == familiaId }
            _minhaFamiliaNome.value = familia?.nomeExibicao
        } else {
            _minhaFamiliaNome.value = null
        }
    }
    
    /**
     * Abre o modal para selecionar "Minha família"
     */
    fun abrirModalMinhaFamilia() {
        _mostrarModalMinhaFamilia.value = true
    }
    
    /**
     * Fecha o modal de seleção de "Minha família"
     */
    fun fecharModalMinhaFamilia() {
        _mostrarModalMinhaFamilia.value = false
    }
    
    /**
     * Define uma família como "Minha família"
     */
    fun definirMinhaFamilia(familiaId: String, familiaNome: String) {
        viewModelScope.launch {
            try {
                MinhaFamiliaPreferences.salvarFamiliaId(context, familiaId)
                _minhaFamiliaId.value = familiaId
                _minhaFamiliaNome.value = familiaNome
                Timber.d("✅ Minha família definida: $familiaNome")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao salvar Minha Família")
            }
        }
    }
    
    /**
     * Remove a seleção de "Minha família"
     */
    fun removerMinhaFamilia() {
        viewModelScope.launch {
            try {
                MinhaFamiliaPreferences.salvarFamiliaId(context, null)
                _minhaFamiliaId.value = null
                _minhaFamiliaNome.value = null
                Timber.d("✅ Minha família removida")
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao remover Minha Família")
            }
        }
    }
    
    /**
     * Promove automaticamente o usuário mais antigo para ADMIN SÊNIOR
     * Executa apenas uma vez - se já existir um ADMIN SR, não faz nada
     */
    private fun promoverPrimeiroAdminSenior() {
        viewModelScope.launch {
            try {
                val resultado = usuarioRepository.promoverPrimeiroAdminSenior()
                resultado.onSuccess {
                    Timber.d("✅ Verificação de ADMIN SÊNIOR concluída")
                }
                resultado.onFailure { error ->
                    Timber.w(error, "⚠️ Aviso ao verificar ADMIN SÊNIOR")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao promover primeiro ADMIN SÊNIOR")
            }
        }
    }
    
    /**
     * Observa a Família Zero em tempo real para atualizar o nome
     */
    private fun observarFamiliaZero() {
        viewModelScope.launch {
            familiaZeroRepository.observar()
                .catch { error ->
                    Timber.e(error, "Erro ao observar Família Zero")
                }
                .collect { familiaZero ->
                    val nomeFamilia = if (familiaZero != null && familiaZero.arvoreNome.isNotBlank()) {
                        familiaZero.getNomeArvore()
                    } else {
                        null
                    }
                    
                    // Buscar nomes do casal fundador
                    var paiNome: String? = null
                    var maeNome: String? = null
                    familiaZero?.let { fz ->
                        if (fz.pai.isNotBlank()) {
                            val pai = pessoaRepository.buscarPorId(fz.pai)
                            paiNome = pai?.nome
                        }
                        if (fz.mae.isNotBlank()) {
                            val mae = pessoaRepository.buscarPorId(fz.mae)
                            maeNome = mae?.nome
                        }
                    }
                    
                    _state.update { 
                        it.copy(
                            familiaZeroExiste = familiaZero != null,
                            familiaZeroNome = nomeFamilia,
                            familiaZeroPaiNome = paiNome,
                            familiaZeroMaeNome = maeNome
                        ) 
                    }
                }
        }
    }
    
    /**
     * Observa pessoas em tempo real para atualizar estatísticas de gênero
     * Exclui pai e mãe da família zero da contagem
     */
    private fun observarEstatisticasGenero() {
        viewModelScope.launch {
            combine(
                pessoaRepository.observarTodasPessoas(),
                familiaZeroRepository.observar()
            ) { todasPessoas, familiaZero ->
                // Obter IDs do pai e da mãe da família zero para excluir
                val idsExcluir = mutableSetOf<String>()
                familiaZero?.let { fz ->
                    if (fz.pai.isNotBlank()) idsExcluir.add(fz.pai)
                    if (fz.mae.isNotBlank()) idsExcluir.add(fz.mae)
                }
                
                // Filtrar pessoas excluindo pai e mãe da família zero
                val pessoasParaContar = todasPessoas.filter { pessoa ->
                    !idsExcluir.contains(pessoa.id)
                }
                
                val meninas = pessoasParaContar.count { it.genero == Genero.FEMININO }
                val meninos = pessoasParaContar.count { it.genero == Genero.MASCULINO }
                val outros = pessoasParaContar.count { it.genero == Genero.OUTRO }
                
                Triple(meninas, meninos, outros)
            }
                .catch { error ->
                    Timber.e(error, "Erro ao observar pessoas para estatísticas")
                }
                .collect { (meninas, meninos, outros) ->
                    _state.update {
                        it.copy(
                            meninas = meninas,
                            meninos = meninos,
                            outros = outros
                        )
                    }
                }
        }
    }
    
    /**
     * Filtra pessoas por qualquer campo
     */
    private fun filtrarPessoas(pessoas: List<Pessoa>, termo: String): List<Pessoa> {
        val termoLower = termo.lowercase()
        
        return pessoas.filter { pessoa ->
            pessoa.nome.lowercase().contains(termoLower) ||
            pessoa.localNascimento?.lowercase()?.contains(termoLower) == true ||
            pessoa.localResidencia?.lowercase()?.contains(termoLower) == true ||
            pessoa.profissao?.lowercase()?.contains(termoLower) == true ||
            pessoa.biografia?.lowercase()?.contains(termoLower) == true ||
            pessoa.nomeNormalizado.contains(termoLower)
        }
    }
    
    /**
     * Ordena pessoas conforme critério selecionado
     */
    private fun ordenarPessoas(pessoas: List<Pessoa>, ordenacao: TipoOrdenacao): List<Pessoa> {
        return when (ordenacao) {
            TipoOrdenacao.NOME_CRESCENTE -> pessoas.sortedBy { it.nome }
            TipoOrdenacao.NOME_DECRESCENTE -> pessoas.sortedByDescending { it.nome }
            TipoOrdenacao.DATA_NASCIMENTO_CRESCENTE -> pessoas.sortedWith(
                compareBy(nullsLast()) { it.dataNascimento?.time ?: Long.MAX_VALUE }
            )
            TipoOrdenacao.DATA_NASCIMENTO_DECRESCENTE -> pessoas.sortedWith(
                compareByDescending(nullsLast()) { it.dataNascimento?.time ?: Long.MAX_VALUE }
            )
            TipoOrdenacao.IDADE_CRESCENTE -> pessoas.sortedWith(
                compareBy(nullsLast()) { it.calcularIdade() ?: Int.MAX_VALUE }
            )
            TipoOrdenacao.IDADE_DECRESCENTE -> pessoas.sortedWith(
                compareByDescending(nullsLast()) { it.calcularIdade() ?: Int.MAX_VALUE }
            )
            TipoOrdenacao.MAIS_ANTIGA -> pessoas.sortedWith(
                compareBy(nullsLast()) { it.criadoEm.time }
            )
            TipoOrdenacao.MAIS_RECENTE -> pessoas.sortedWith(
                compareByDescending(nullsLast()) { it.criadoEm.time }
            )
        }
    }
    
    /**
     * Atualiza termo de busca
     */
    fun atualizarBusca(termo: String) {
        _state.update { it.copy(termoBusca = termo) }
    }
    
    /**
     * Atualiza ordenação
     */
    fun atualizarOrdenacao(ordenacao: TipoOrdenacao) {
        _state.update { it.copy(ordenacao = ordenacao) }
    }
    
    /**
     * Limpa a mensagem de erro
     */
    fun limparErro() {
        _state.update { it.copy(erro = null) }
    }
    
    /**
     * Gera dados de teste com 3 gerações
     */
    fun gerarDadosTeste() {
        viewModelScope.launch {
            try {
                val usuarioId = authService.currentUser?.uid
                if (usuarioId == null) {
                    Timber.w("⚠️ Usuário não autenticado para gerar dados de teste")
                    return@launch
                }
                
                _state.update { it.copy(isLoading = true, erro = null) }
                
                val resultado = gerarDadosTesteUseCase.gerarDadosTeste(usuarioId)
                
                resultado.onSuccess {
                    Timber.d("✅ Dados de teste gerados com sucesso!")
                    _state.update { it.copy(isLoading = false) }
                    // Recarregar dados após gerar
                    recarregar()
                }.onFailure { error ->
                    Timber.e(error, "❌ Erro ao gerar dados de teste")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao gerar dados de teste: ${error.message}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao gerar dados de teste")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao gerar dados de teste: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Carrega dados iniciais da tela
     */
    private fun carregarDados() {
        viewModelScope.launch {
            try {
                val currentUser = authService.currentUser
                if (currentUser == null) {
                    _state.update { it.copy(erro = "Usuário não autenticado") }
                    return@launch
                }
                
                // Observar mudanças no usuário para atualizar parentescos
                usuarioRepository.observarPorId(currentUser.uid)
                    .catch { error ->
                        Timber.e(error, "Erro ao observar usuário")
                    }
                    .collect { usuario ->
                        _state.update { it.copy(usuario = usuario) }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar dados")
                _state.update { it.copy(erro = "Erro ao carregar dados: ${e.message}") }
            }
        }
        
        // Sincronizar dados do usuário para garantir permissões atualizadas
        viewModelScope.launch {
            try {
                val currentUser = authService.currentUser
                if (currentUser != null) {
                    usuarioRepository.sincronizar(currentUser.uid)
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao sincronizar usuário")
            }
        }
        
        viewModelScope.launch {
            try {
                val currentUser = authService.currentUser
                if (currentUser == null) return@launch
                
                // Buscar dados do usuário inicialmente
                val usuario = usuarioRepository.buscarPorId(currentUser.uid)
                _state.update { it.copy(usuario = usuario) }
                
                // Verificar se existe Família Zero e buscar nome
                val familiaZeroExiste = familiaZeroRepository.existe()
                val familiaZero = if (familiaZeroExiste) {
                    familiaZeroRepository.buscar()
                } else {
                    null
                }
                val nomeFamilia = familiaZero?.let { 
                    if (it.arvoreNome.isNotBlank()) it.getNomeArvore() else null
                }
                
                // Buscar nomes do casal fundador
                var paiNome: String? = null
                var maeNome: String? = null
                familiaZero?.let { fz ->
                    if (fz.pai.isNotBlank()) {
                        val pai = pessoaRepository.buscarPorId(fz.pai)
                        paiNome = pai?.nome
                    }
                    if (fz.mae.isNotBlank()) {
                        val mae = pessoaRepository.buscarPorId(fz.mae)
                        maeNome = mae?.nome
                    }
                }
                
                _state.update { 
                    it.copy(
                        familiaZeroExiste = familiaZeroExiste,
                        familiaZeroNome = nomeFamilia,
                        familiaZeroPaiNome = paiNome,
                        familiaZeroMaeNome = maeNome
                    ) 
                }
                
                // Se não existe e é primeiro acesso, deve criar Família Zero
                if (!familiaZeroExiste && usuario?.primeiroAcesso == true) {
                    _state.update { it.copy(mostrarOnboarding = true) }
                }
                
                // Contar pessoas (apenas aprovadas para evitar contar pendentes/duplicatas)
                val totalPessoas = pessoaRepository.contarPessoasAprovadas()
                
                // Contar famílias e obter estatísticas detalhadas
                val estatisticasFamilias = pessoaRepository.obterEstatisticasFamilias()
                val totalFamilias = estatisticasFamilias.total
                
                // Contar pessoas até nascimento do usuário (ranking)
                val pessoaVinculada = usuario?.pessoaVinculada
                val pessoaVinculadaObj = pessoaVinculada?.let { 
                    pessoaRepository.buscarPorId(it)
                }
                val dataNascimentoUsuario = pessoaVinculadaObj?.dataNascimento
                
                // Calcular posição global em relação à família zero
                // Calcular posição detalhada em relação à família zero
                val (posicaoGrupo, posicaoRanking) = if (pessoaVinculada != null) {
                    pessoaRepository.calcularPosicaoDetalhada(
                        pessoaId = pessoaVinculada,
                        familiaZeroPaiId = familiaZero?.pai,
                        familiaZeroMaeId = familiaZero?.mae
                    )
                } else {
                    Pair("", 0)
                }
                
                // Contar sobrinhos
                val totalSobrinhos = pessoaVinculada?.let { 
                    pessoaRepository.contarSobrinhos(it) 
                } ?: 0
                
                // Estatísticas de gênero são atualizadas automaticamente por observeEstatisticasGenero()
                // Não precisamos contar aqui para evitar redundância e race conditions
                _state.update { 
                    it.copy(
                        totalPessoas = totalPessoas,
                        totalFamilias = totalFamilias,
                        familiasMonoparentais = estatisticasFamilias.monoparentais,
                        familiasHomoafetivas = estatisticasFamilias.homoafetivas,

                        rankingPessoas = posicaoRanking,
                        posicaoGrupo = posicaoGrupo,
                        posicaoRanking = posicaoRanking,
                        totalSobrinhos = totalSobrinhos
                        // meninas, meninos e outros são atualizados por observeEstatisticasGenero()
                    )
                }
                
                // Sincronizar do Firestore sempre para garantir que temos dados atualizados
                Timber.d("🔄 Sincronizando pessoas do Firestore (totalPessoas: $totalPessoas)")
                val resultadoSync = pessoaRepository.sincronizarDoFirestore()
                resultadoSync.onSuccess {
                    Timber.d("✅ Sincronização concluída com sucesso")
                    // Recalcular estatísticas após sincronização
                    atualizarEstatisticas()
                }
                resultadoSync.onFailure { error ->
                    Timber.e(error, "❌ Erro na sincronização")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Erro ao carregar dados")
                _state.update { it.copy(erro = "Erro ao carregar dados: ${e.message}") }
            }
        }
    }
    
    /**
     * Atualiza todas as estatísticas
     */
    private fun atualizarEstatisticas() {
        viewModelScope.launch {
            try {
                // Contar apenas pessoas aprovadas para evitar contar pendentes/duplicatas
                val totalPessoas = pessoaRepository.contarPessoasAprovadas()
                val estatisticasFamilias = pessoaRepository.obterEstatisticasFamilias()
                val totalFamilias = estatisticasFamilias.total
                
                val usuario = _state.value.usuario
                val pessoaVinculada = usuario?.pessoaVinculada
                val pessoaVinculadaObj = pessoaVinculada?.let { 
                    pessoaRepository.buscarPorId(it)
                }
                val dataNascimentoUsuario = pessoaVinculadaObj?.dataNascimento
                
                // Calcular posição global em relação à família zero
                // Calcular posição detalhada em relação à família zero
                val (posicaoGrupo, posicaoRanking) = if (pessoaVinculada != null) {
                    val familiaZero = familiaZeroRepository.buscar()
                    pessoaRepository.calcularPosicaoDetalhada(
                        pessoaId = pessoaVinculada,
                        familiaZeroPaiId = familiaZero?.pai,
                        familiaZeroMaeId = familiaZero?.mae
                    )
                } else {
                    Pair("", 0)
                }
                val totalSobrinhos = pessoaVinculada?.let { 
                    pessoaRepository.contarSobrinhos(it) 
                } ?: 0
                
                // Estatísticas de gênero são atualizadas automaticamente por observeEstatisticasGenero()
                // Não precisamos contar aqui para evitar redundância e race conditions
                _state.update { 
                    it.copy(
                        totalPessoas = totalPessoas,
                        totalFamilias = totalFamilias,
                        familiasMonoparentais = estatisticasFamilias.monoparentais,
                        familiasHomoafetivas = estatisticasFamilias.homoafetivas,
                        rankingPessoas = posicaoRanking,
                        posicaoGrupo = posicaoGrupo,
                        posicaoRanking = posicaoRanking,
                        totalSobrinhos = totalSobrinhos
                        // meninas, meninos e outros são atualizados por observeEstatisticasGenero()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao atualizar estatísticas")
            }
        }
    }

    fun atualizarPedidosPendentes() {
        viewModelScope.launch {
            try {
                val usuario = _state.value.usuario
                val podeVer = (usuario?.ehAdministrador == true) || (usuario?.ehAdministradorSenior == true)
                if (!podeVer) {
                    _pedidosPendentes.value = 0
                    return@launch
                }
                val resultado = firestoreService.contarPedidosConvitePendentes()
                _pedidosPendentes.value = resultado.getOrElse { 0 }
            } catch (_: Exception) {
                _pedidosPendentes.value = 0
            }
        }
    }
    
    /**
     * Atualiza contagem de pessoas
     */
    fun atualizarContagem() {
        atualizarEstatisticas()
    }
    
    /**
     * Recarrega dados do Firestore (pull-to-refresh)
     * Substitui completamente o cache local pelos dados do Firestore
     */
    fun recarregar() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, erro = null) }
                
                // Recarregar pessoas do Firestore (substituindo cache)
                val resultado = pessoaRepository.recarregarDoFirestore()
                
                resultado.onSuccess {
                    // Atualizar todas as estatísticas
                    atualizarEstatisticas()
                    
                    // Verificar Família Zero novamente e buscar nome
                    val familiaZeroExiste = familiaZeroRepository.existe()
                    val familiaZero = if (familiaZeroExiste) {
                        familiaZeroRepository.buscar()
                    } else {
                        null
                    }
                    val nomeFamilia = familiaZero?.let { 
                        if (it.arvoreNome.isNotBlank()) it.getNomeArvore() else null
                    }
                    
                    // Buscar nomes do casal fundador
                    var paiNome: String? = null
                    var maeNome: String? = null
                    familiaZero?.let { fz ->
                        if (fz.pai.isNotBlank()) {
                            val pai = pessoaRepository.buscarPorId(fz.pai)
                            paiNome = pai?.nome
                        }
                        if (fz.mae.isNotBlank()) {
                            val mae = pessoaRepository.buscarPorId(fz.mae)
                            maeNome = mae?.nome
                        }
                    }
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            familiaZeroExiste = familiaZeroExiste,
                            familiaZeroNome = nomeFamilia,
                            familiaZeroPaiNome = paiNome,
                            familiaZeroMaeNome = maeNome
                        )
                    }
                    Timber.d("✅ Dados recarregados do Firestore")
                }
                
                resultado.onFailure { error ->
                    Timber.e(error, "❌ Erro ao recarregar dados")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            erro = "Erro ao recarregar: ${error.message}"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro fatal ao recarregar")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        erro = "Erro ao recarregar: ${e.message}"
                    )
                }
            }
        }
    }
}

/**
 * Estado da tela Home
 * 
 * @Stable indica ao Compose que este estado é estável e só deve causar
 * recomposição quando seus valores realmente mudarem
 */
@androidx.compose.runtime.Stable
data class HomeState(
    val usuario: Usuario? = null,
    val totalPessoas: Int = 0,
    val totalFamilias: Int = 0,
    val familiasMonoparentais: Int = 0,
    val familiasHomoafetivas: Int = 0,
    val rankingPessoas: Int = 0,
    val totalSobrinhos: Int = 0,
    val familiaZeroExiste: Boolean = false,
    val familiaZeroNome: String? = null,
    val familiaZeroPaiNome: String? = null,
    val familiaZeroMaeNome: String? = null,
    val meninas: Int = 0,
    val meninos: Int = 0,
    val outros: Int = 0,
    val mostrarOnboarding: Boolean = false,
    val erro: String? = null,
    val isLoading: Boolean = false,
    val termoBusca: String = "",
    val ordenacao: TipoOrdenacao = TipoOrdenacao.NOME_CRESCENTE,
    val mostrarModalFamiliaZero: Boolean = false,
    val posicaoGrupo: String = "",
    val posicaoRanking: Int = 0
)

/**
 * Tipos de ordenação disponíveis
 * 
 * @Immutable indica que este enum nunca muda após criação
 */
@androidx.compose.runtime.Immutable
enum class TipoOrdenacao(val label: String) {
    NOME_CRESCENTE("Nome (A-Z)"),
    NOME_DECRESCENTE("Nome (Z-A)"),
    DATA_NASCIMENTO_CRESCENTE("Mais antigo"),
    DATA_NASCIMENTO_DECRESCENTE("Mais novo"),
    IDADE_CRESCENTE("Idade crescente"),
    IDADE_DECRESCENTE("Idade decrescente"),
    MAIS_ANTIGA("Cadastro mais antigo"),
    MAIS_RECENTE("Cadastro mais recente")
}

