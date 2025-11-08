package com.raizesvivas.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.FamiliaZeroRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.Usuario
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.FamiliaZero
import com.raizesvivas.app.domain.usecase.GerarDadosTesteUseCase
import com.raizesvivas.app.utils.ParentescoCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged

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
    private val gerarDadosTesteUseCase: GerarDadosTesteUseCase
) : ViewModel() {
    
    private val _mostrarModalFamiliaZero = MutableStateFlow(false)
    val mostrarModalFamiliaZero = _mostrarModalFamiliaZero.asStateFlow()
    
    private val _mostrarModalEditarNome = MutableStateFlow(false)
    val mostrarModalEditarNome = _mostrarModalEditarNome.asStateFlow()
    
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
                            pessoaRepository.salvar(pessoaAtualizada, ehAdmin = true)
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
                    
                    pessoaRepository.salvar(paiAtualizado, ehAdmin = true)
                    pessoaRepository.salvar(maeAtualizada, ehAdmin = true)
                    
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
                // Atualizar cache
                parentescosCache = cacheKey to resultado
                Timber.d("💾 Cache de parentescos atualizado (${resultado.size} parentes)")
                resultado
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
     */
    private fun observarEstatisticasGenero() {
        viewModelScope.launch {
            pessoaRepository.observarTodasPessoas()
                .catch { error ->
                    Timber.e(error, "Erro ao observar pessoas para estatísticas")
                }
                .collect { todasPessoas ->
                    val meninas = todasPessoas.count { it.genero == Genero.FEMININO }
                    val meninos = todasPessoas.count { it.genero == Genero.MASCULINO }
                    val outros = todasPessoas.count { it.genero == Genero.OUTRO }
                    
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
                
                // Contar pessoas
                val totalPessoas = pessoaRepository.contarPessoas()
                
                // Contar famílias
                val totalFamilias = pessoaRepository.contarFamilias()
                
                // Contar pessoas até nascimento do usuário (ranking)
                val pessoaVinculada = usuario?.pessoaVinculada
                val dataNascimentoUsuario = pessoaVinculada?.let { 
                    pessoaRepository.buscarPorId(it)?.dataNascimento 
                }
                val rankingPessoas = pessoaRepository.contarPessoasAteNascimento(dataNascimentoUsuario)
                
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
                        rankingPessoas = rankingPessoas,
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
                val totalPessoas = pessoaRepository.contarPessoas()
                val totalFamilias = pessoaRepository.contarFamilias()
                
                val usuario = _state.value.usuario
                val pessoaVinculada = usuario?.pessoaVinculada
                val dataNascimentoUsuario = pessoaVinculada?.let { 
                    pessoaRepository.buscarPorId(it)?.dataNascimento 
                }
                val rankingPessoas = pessoaRepository.contarPessoasAteNascimento(dataNascimentoUsuario)
                val totalSobrinhos = pessoaVinculada?.let { 
                    pessoaRepository.contarSobrinhos(it) 
                } ?: 0
                
                // Estatísticas de gênero são atualizadas automaticamente por observeEstatisticasGenero()
                // Não precisamos contar aqui para evitar redundância e race conditions
                _state.update { 
                    it.copy(
                        totalPessoas = totalPessoas,
                        totalFamilias = totalFamilias,
                        rankingPessoas = rankingPessoas,
                        totalSobrinhos = totalSobrinhos
                        // meninas, meninos e outros são atualizados por observeEstatisticasGenero()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao atualizar estatísticas")
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
 */
data class HomeState(
    val usuario: Usuario? = null,
    val totalPessoas: Int = 0,
    val totalFamilias: Int = 0,
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
    val mostrarModalFamiliaZero: Boolean = false
)

/**
 * Tipos de ordenação disponíveis
 */
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

