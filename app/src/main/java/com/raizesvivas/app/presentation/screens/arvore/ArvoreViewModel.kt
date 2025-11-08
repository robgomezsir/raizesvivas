package com.raizesvivas.app.presentation.screens.arvore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.app.data.repository.FamiliaZeroRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.usecase.VerificarConquistasUseCase
import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.utils.ArvoreHierarquicaCalculator
import com.raizesvivas.app.utils.TreeBuilder
import com.raizesvivas.app.presentation.components.TreeNodeData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para a tela de Árvore Genealógica
 * 
 * Gerencia estado da visualização estilo mapa mental
 */
@HiltViewModel
class ArvoreViewModel @Inject constructor(
    private val pessoaRepository: PessoaRepository,
    @Suppress("UNUSED_PARAMETER") private val familiaZeroRepository: FamiliaZeroRepository,
    private val verificarConquistasUseCase: VerificarConquistasUseCase,
    private val authService: AuthService
) : ViewModel() {
    
    private val _state = MutableStateFlow(ArvoreState())
    val state = _state.asStateFlow()
    
    private val _pessoas = MutableStateFlow<List<Pessoa>>(emptyList())
    val pessoas = _pessoas.asStateFlow()
    
    private val _treeData = MutableStateFlow<TreeNodeData?>(null)
    val treeData = _treeData.asStateFlow()
    
    private val _nosHierarquicos = MutableStateFlow<List<ArvoreHierarquicaCalculator.NoHierarquico>>(emptyList())
    val nosHierarquicos = _nosHierarquicos.asStateFlow()
    
    private val _nosExpandidos = MutableStateFlow<Set<String>>(setOf())
    val nosExpandidos = _nosExpandidos.asStateFlow()
    
    private val _layoutResultado = MutableStateFlow<ArvoreHierarquicaCalculator.ResultadoLayout?>(
        ArvoreHierarquicaCalculator.ResultadoLayout(emptyList(), 0f, 0f)
    )
    val layoutResultado = _layoutResultado.asStateFlow()
    
    @Suppress("UNUSED_VARIABLE")
    private val _casalFamiliaZero = MutableStateFlow<Pair<Pessoa?, Pessoa?>>(Pair(null, null))
    val casalFamiliaZero = _casalFamiliaZero.asStateFlow()
    
    private var florestaVisualizada = false
    
    init {
        // Observar mudanças em tempo real nas pessoas
        observarPessoas()
        
        // Sincronizar do Firestore na primeira vez
        sincronizarInicialmente()
        
        // Inicializar com todos os nós contraídos (sem expandir)
        viewModelScope.launch {
            _nosExpandidos.value = setOf() // Todos contraídos inicialmente
        }
        
        // Verificar conquista de visualizar floresta (primeira vez)
        viewModelScope.launch {
            val usuarioId = authService.currentUser?.uid
            if (usuarioId != null && !florestaVisualizada) {
                florestaVisualizada = true
                verificarConquistasUseCase.verificarTodasConquistas(usuarioId)
            }
        }
    }
    
    /**
     * Observa mudanças nas pessoas em tempo real
     */
    private fun observarPessoas() {
        viewModelScope.launch {
            try {
                pessoaRepository.observarTodasPessoas()
                    .collect { pessoasList: List<Pessoa> ->
                        try {
                            Timber.d("🔄 Pessoas atualizadas: ${pessoasList.size}")
                            val pessoasAnteriores = _pessoas.value
                            _pessoas.value = pessoasList
                            
                            // Se tinha pessoas antes e agora está vazio, pode ser um problema
                            if (pessoasList.isEmpty() && pessoasAnteriores.isNotEmpty()) {
                                Timber.w("⚠️ Lista de pessoas ficou vazia! Tinha ${pessoasAnteriores.size}, agora tem 0")
                            }
                            
                            // Recalcular posições quando pessoas mudarem (com debounce para otimização)
                            recalcularPosicoesComDebounce()
                        } catch (e: Exception) {
                            Timber.e(e, "Erro ao processar atualização de pessoas")
                            _state.value = _state.value.copy(erro = "Erro ao processar pessoas: ${e.message}")
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Erro ao observar pessoas")
                _state.value = _state.value.copy(erro = "Erro ao carregar pessoas: ${e.message}")
            }
        }
    }
    
    /**
     * Sincroniza do Firestore na primeira vez se o cache estiver vazio
     */
    private fun sincronizarInicialmente() {
        viewModelScope.launch {
            try {
                // Verificar se há pessoas no cache
                val pessoasIniciais = pessoaRepository.buscarTodas()
                Timber.d("📊 Pessoas iniciais no cache: ${pessoasIniciais.size}")
                
                if (pessoasIniciais.isEmpty()) {
                    Timber.d("🔄 Cache vazio, sincronizando do Firestore...")
                    _state.value = _state.value.copy(isLoading = true)
                    
                    val resultado = pessoaRepository.sincronizarDoFirestore()
                    
                    resultado.onSuccess {
                        Timber.d("✅ Sincronização inicial concluída")
                    }
                    
                    resultado.onFailure { error ->
                        Timber.e(error, "❌ Erro na sincronização inicial")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            erro = "Erro ao sincronizar dados: ${error.message}"
                        )
                    }
                } else {
                    // Já tem pessoas, apenas recalcular
                    recalcularPosicoesComPessoas(pessoasIniciais)
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro ao sincronizar inicialmente")
                _state.value = _state.value.copy(
                    isLoading = false,
                    erro = "Erro ao carregar dados: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Recalcula posições com a lista de pessoas fornecida
     */
    private fun recalcularPosicoesComPessoas(todasPessoas: List<Pessoa>) {
        if (todasPessoas.isEmpty()) {
            Timber.w("⚠️ Tentativa de recalcular com lista vazia")
            _nosHierarquicos.value = emptyList()
            _layoutResultado.value = ArvoreHierarquicaCalculator.ResultadoLayout(emptyList(), 0f, 0f)
            _state.value = _state.value.copy(isLoading = false)
            return
        }
        
        try {
            Timber.d("🔍 Iniciando recálculo com ${todasPessoas.size} pessoas")
            
            // Aplicar filtros ANTES de buscar Família Zero
            val pessoasFiltradas = aplicarFiltros(todasPessoas)
            
            Timber.d("📋 Pessoas após filtros: ${pessoasFiltradas.size} de ${todasPessoas.size}")
            
            if (pessoasFiltradas.isEmpty()) {
                Timber.w("⚠️ Nenhuma pessoa passou pelos filtros")
                _nosHierarquicos.value = emptyList()
                _layoutResultado.value = ArvoreHierarquicaCalculator.ResultadoLayout(emptyList(), 0f, 0f)
                _state.value = _state.value.copy(isLoading = false)
                return
            }
            
            // Buscar casal Família Zero (usar pessoas filtradas)
            val casal = ArvoreHierarquicaCalculator.encontrarCasalFamiliaZero(pessoasFiltradas)
            _casalFamiliaZero.value = casal
            
            Timber.d("👥 Casal Família Zero encontrado: ${casal.first?.nome ?: "null"} e ${casal.second?.nome ?: "null"}")
            
            // Determinar raiz da árvore
            val raizId = when {
                // Se há pessoa central selecionada (focada), usar ela
                _state.value.pessoaCentralId != null -> {
                    _state.value.pessoaCentralId
                }
                // Tentar usar Família Zero como padrão
                casal.first != null -> {
                    casal.first?.id
                }
                casal.second != null -> {
                    casal.second?.id
                }
                // Fallback: usar primeira pessoa disponível
                else -> {
                    Timber.d("⚠️ Nenhuma Família Zero encontrada, usando primeira pessoa disponível")
                    pessoasFiltradas.firstOrNull()?.id
                }
            }
            
            if (raizId == null) {
                Timber.e("❌ Não foi possível determinar raiz - nenhuma pessoa disponível")
                _nosHierarquicos.value = emptyList()
                _layoutResultado.value = ArvoreHierarquicaCalculator.ResultadoLayout(emptyList(), 0f, 0f)
                _state.value = _state.value.copy(isLoading = false)
                return
            }
            
            // Verificar se a raiz está nas pessoas filtradas
            val raizPessoa = pessoasFiltradas.firstOrNull { it.id == raizId }
            if (raizPessoa == null) {
                Timber.e("❌ Pessoa raiz não encontrada nas pessoas filtradas: $raizId")
                _nosHierarquicos.value = emptyList()
                _layoutResultado.value = ArvoreHierarquicaCalculator.ResultadoLayout(emptyList(), 0f, 0f)
                _state.value = _state.value.copy(isLoading = false)
                return
            }
            
            // Executar cálculos pesados em background thread
            viewModelScope.launch {
                val resultado = withContext(Dispatchers.Default) {
                    val pessoasMapCalculo = pessoasFiltradas.associateBy { it.id }
                    
                    // Manter todos os nós contraídos inicialmente
                    // Não expandir automaticamente a raiz - todos os cards começam contraídos
                    val nosExpandidosParaCalcular = _nosExpandidos.value
                    
                    Timber.d("📊 Calculando layout com ${nosExpandidosParaCalcular.size} nós expandidos: $nosExpandidosParaCalcular")
                    
                    // Calcular layout hierárquico com nós expandidos corretos
                    ArvoreHierarquicaCalculator.calcularLayoutHierarquico(
                        todasPessoas = pessoasFiltradas,
                        pessoaRaizId = raizId,
                        pessoasMap = pessoasMapCalculo,
                        nosExpandidos = nosExpandidosParaCalcular,
                        casalFamiliaZero = casal
                    )
                }
                
                Timber.d("✅ Layout hierárquico calculado: ${resultado.nos.size} nós, largura: ${resultado.larguraTotal}, altura: ${resultado.alturaTotal}")
                
                // Construir estrutura de árvore recursiva (também em background)
                val treeData = withContext(Dispatchers.Default) {
                    TreeBuilder.buildTree(
                        pessoas = pessoasFiltradas,
                        casalFamiliaZero = casal,
                        nosExpandidos = _nosExpandidos.value
                    )
                }
                _treeData.value = treeData
                
                _nosHierarquicos.value = resultado.nos
                _layoutResultado.value = resultado
                _state.value = _state.value.copy(isLoading = false)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao recalcular layout")
            _nosHierarquicos.value = emptyList()
            _layoutResultado.value = ArvoreHierarquicaCalculator.ResultadoLayout(emptyList(), 0f, 0f)
            _state.value = _state.value.copy(
                isLoading = false,
                erro = "Erro ao calcular árvore: ${e.message}"
            )
        }
    }
    
    /**
     * Foca em uma pessoa (muda a raiz da árvore)
     */
    fun focarPessoa(pessoaId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(pessoaCentralId = pessoaId, modoCompacto = false)
            recalcularPosicoesComPessoas(_pessoas.value)
        }
    }
    
    /**
     * Alterna expansão de um nó específico
     */
    fun toggleNo(noId: String) {
        viewModelScope.launch {
            val expandidosAtuais = _nosExpandidos.value.toMutableSet()
            if (expandidosAtuais.contains(noId)) {
                expandidosAtuais.remove(noId)
            } else {
                expandidosAtuais.add(noId)
            }
            _nosExpandidos.value = expandidosAtuais
            
            // Reconstruir árvore com novos estados de expansão
            val casal = _casalFamiliaZero.value
            val pessoasFiltradas = aplicarFiltros(_pessoas.value)
            val treeData = TreeBuilder.buildTree(
                pessoas = pessoasFiltradas,
                casalFamiliaZero = casal,
                nosExpandidos = _nosExpandidos.value
            )
            _treeData.value = treeData
        }
    }
    
    /**
     * Expande todos os nós
     */
    fun expandirTudo() {
        viewModelScope.launch {
            val todosIds = _nosHierarquicos.value.map { it.pessoa.id }.toSet()
            _nosExpandidos.value = todosIds
            recalcularPosicoesComPessoas(_pessoas.value)
        }
    }
    
    /**
     * Recolhe todos os nós (exceto raiz)
     */
    fun recolherTudo() {
        viewModelScope.launch {
            val raizIds = _nosHierarquicos.value.filter { it.nivel == 0 }.map { it.pessoa.id }.toSet()
            _nosExpandidos.value = raizIds
            recalcularPosicoesComPessoas(_pessoas.value)
        }
    }
    
    /**
     * Expande a árvore para mostrar toda a descendência
     */
    fun expandirArvore() {
        expandirTudo()
    }
    
    /**
     * Recolhe a árvore para mostrar apenas a Família Zero
     */
    fun recolherArvore() {
        recolherTudo()
    }
    
    /**
     * Aplica filtros atuais
     */
    private fun aplicarFiltros(pessoas: List<Pessoa>): List<Pessoa> {
        return pessoas.filter { pessoa ->
            val state = _state.value
            
            // Filtro por status (vivos/falecidos)
            when (state.filtroStatus) {
                FiltroStatus.APENAS_VIVOS -> pessoa.dataFalecimento == null
                FiltroStatus.APENAS_FALECIDOS -> pessoa.dataFalecimento != null
                FiltroStatus.TODOS -> true
            }
        }.filter { pessoa ->
            val state = _state.value
            
            // Filtro por aprovação
            if (state.mostrarApenasAprovados) {
                pessoa.aprovado
            } else {
                true
            }
        }.filter { pessoa ->
            val state = _state.value
            
            // Filtro por busca
            if (state.termoBusca.isNotBlank()) {
                pessoa.nomeNormalizado.contains(state.termoBusca.lowercase()) ||
                pessoa.nome.contains(state.termoBusca, ignoreCase = true)
            } else {
                true
            }
        }
    }
    
    /**
     * Atualiza termo de busca
     */
    fun onBuscaChanged(termo: String) {
        _state.value = _state.value.copy(termoBusca = termo)
        recalcularPosicoesComDebounce()
    }
    
    /**
     * Altera modo de visualização
     */
    fun alterarModoVisualizacao(modo: ModoVisualizacao) {
        _state.value = _state.value.copy(modoVisualizacao = modo)
        // Recalcular posições quando mudar o modo
        recalcularPosicoesComDebounce()
    }
    
    /**
     * Atualiza filtro de status
     */
    fun onFiltroStatusChanged(filtro: FiltroStatus) {
        _state.value = _state.value.copy(filtroStatus = filtro)
        recalcularPosicoesComDebounce()
    }
    
    /**
     * Alterna filtro de aprovação
     */
    fun onMostrarApenasAprovadosChanged(mostrar: Boolean) {
        _state.value = _state.value.copy(mostrarApenasAprovados = mostrar)
        recalcularPosicoesComDebounce()
    }
    
    /**
     * Seleciona pessoa na árvore
     */
    fun selecionarPessoa(pessoaId: String) {
        _state.value = _state.value.copy(pessoaSelecionadaId = pessoaId)
    }
    
    /**
     * Limpa seleção
     */
    fun limparSelecao() {
        _state.value = _state.value.copy(pessoaSelecionadaId = null)
    }
    
    /**
     * Recalcula posições após mudança de filtros
     */
    private fun recalcularPosicoes() {
        recalcularPosicoesComPessoas(_pessoas.value)
    }
    
    // Debounce para recalcular posições (evitar recálculos excessivos)
    private var recalcularJob: kotlinx.coroutines.Job? = null
    
    private fun recalcularPosicoesComDebounce() {
        recalcularJob?.cancel()
        recalcularJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300) // Debounce de 300ms
            // Usar lista atual de pessoas
            recalcularPosicoesComPessoas(_pessoas.value)
        }
    }
    
    /**
     * Atualiza o termo de busca
     */
    fun atualizarBusca(termo: String) {
        _state.value = _state.value.copy(termoBusca = termo)
    }
    
    /**
     * Recarrega árvore do Firestore (pull-to-refresh)
     * Substitui completamente o cache local pelos dados do Firestore
     */
    fun recarregar() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, erro = null)
                
                // Recarregar pessoas do Firestore (substituindo cache)
                val resultado = pessoaRepository.recarregarDoFirestore()
                
                resultado.onSuccess {
                    // Os dados serão atualizados automaticamente pelo observarTodasPessoas()
                    Timber.d("✅ Árvore recarregada do Firestore")
                }
                
                resultado.onFailure { error ->
                    Timber.e(error, "❌ Erro ao recarregar árvore")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        erro = "Erro ao recarregar: ${error.message}"
                    )
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Erro fatal ao recarregar árvore")
                _state.value = _state.value.copy(
                    isLoading = false,
                    erro = "Erro ao recarregar: ${e.message}"
                )
            }
        }
    }
}

/**
 * Estado da tela de Árvore
 */
data class ArvoreState(
    val isLoading: Boolean = false,
    val erro: String? = null,
    val termoBusca: String = "",
    val filtroStatus: FiltroStatus = FiltroStatus.TODOS,
    val mostrarApenasAprovados: Boolean = false,
    val pessoaSelecionadaId: String? = null,
    val pessoaCentralId: String? = null, // ID da pessoa raiz da árvore
    val modoCompacto: Boolean = false, // Modo compacto mostra só Família Zero (iniciar expandido por padrão)
    val modoVisualizacao: ModoVisualizacao = ModoVisualizacao.RADIAL // Modo de visualização (radial ou hierárquico)
)

/**
 * Modo de visualização da árvore
 */
enum class ModoVisualizacao(val descricao: String) {
    RADIAL("Mapa Mental"),
    HIERARQUICO("Hierárquico"),
    LISTA("Lista Expandível")
}

/**
 * Filtros de status
 */
enum class FiltroStatus {
    TODOS,
    APENAS_VIVOS,
    APENAS_FALECIDOS
}

