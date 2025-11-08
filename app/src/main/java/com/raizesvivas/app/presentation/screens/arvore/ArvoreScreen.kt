package com.raizesvivas.app.presentation.screens.arvore

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.EstadoCivil
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.NoHierarquico
import com.raizesvivas.app.presentation.screens.arvore.FiltroStatus
import com.raizesvivas.app.utils.ArvoreHierarquicaCalculator
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tela de visualização da árvore genealógica hierárquica
 * 
 * Implementa visualização hierárquica vertical organizada com:
 * - Layout top-down hierárquico
 * - Casal Família Zero no topo (raiz)
 * - Conexões verticais e horizontais claras
 * - Expandir/recolher nós com animações fluidas
 * - Scroll vertical e horizontal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArvoreScreen(
    viewModel: ArvoreViewModel = hiltViewModel(),
    onNavigateToDetalhesPessoa: (String) -> Unit = {},
    onNavigateToCadastroPessoa: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val pessoas by viewModel.pessoas.collectAsState()
    val nosHierarquicos by viewModel.nosHierarquicos.collectAsState()
    val nosExpandidos by viewModel.nosExpandidos.collectAsState()
    val layoutResultado by viewModel.layoutResultado.collectAsState()
    
    // Estado para controlar popup de informações
    var pessoaPopupInfo by remember { mutableStateOf<Pessoa?>(null) }
    var mostrarFiltros by remember { mutableStateOf(false) }
    
    val isRefreshing = state.isLoading
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Atualizar quando pull-to-refresh for acionado
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing && !isRefreshing) {
            viewModel.recarregar()
        }
    }
    
    // Finalizar refresh quando carregamento terminar
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Árvore Genealógica Hierárquica")
                        Text(
                            text = "Família Zero no topo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    // Botão busca
                    IconButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    
                    // Botão expandir tudo
                    IconButton(onClick = { viewModel.expandirTudo() }) {
                        Icon(Icons.Default.UnfoldMore, contentDescription = "Expandir tudo")
                    }
                    
                    // Botão recolher tudo
                    IconButton(onClick = { viewModel.recolherTudo() }) {
                        Icon(Icons.Default.UnfoldLess, contentDescription = "Recolher tudo")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCadastroPessoa,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Pessoa")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            // Filtros e busca
            if (mostrarFiltros) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Filtros",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Busca por nome
                        OutlinedTextField(
                            value = state.termoBusca,
                            onValueChange = { viewModel.onBuscaChanged(it) },
                            label = { Text("Buscar por nome") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (state.termoBusca.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onBuscaChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // Filtro de status
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.labelMedium
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.filtroStatus == FiltroStatus.TODOS,
                                onClick = { viewModel.onFiltroStatusChanged(FiltroStatus.TODOS) },
                                label = { Text("Todos") }
                            )
                            FilterChip(
                                selected = state.filtroStatus == FiltroStatus.APENAS_VIVOS,
                                onClick = { viewModel.onFiltroStatusChanged(FiltroStatus.APENAS_VIVOS) },
                                label = { Text("Vivos") }
                            )
                            FilterChip(
                                selected = state.filtroStatus == FiltroStatus.APENAS_FALECIDOS,
                                onClick = { viewModel.onFiltroStatusChanged(FiltroStatus.APENAS_FALECIDOS) },
                                label = { Text("Falecidos") }
                            )
                        }
                        
                        // Filtro de aprovação
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Apenas aprovados")
                            Switch(
                                checked = state.mostrarApenasAprovados,
                                onCheckedChange = { viewModel.onMostrarApenasAprovadosChanged(it) }
                            )
                        }
                    }
                }
            }
            
            // Conteúdo da árvore
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                state.erro != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.erro ?: "Erro desconhecido",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.recarregar() }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }
                
                nosHierarquicos.isEmpty() && !state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (pessoas.isEmpty()) {
                                    "Nenhuma pessoa encontrada"
                                } else {
                                    "Nenhuma pessoa para exibir"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (pessoas.isEmpty()) {
                                    "Sincronize os dados do Firestore ou adicione pessoas na tela de cadastro"
                                } else {
                                    "Ajuste os filtros para ver mais pessoas ou certifique-se de que há uma Família Zero cadastrada"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (pessoas.isEmpty()) {
                                Button(onClick = { viewModel.recarregar() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sincronizar do Firestore")
                                }
                            }
                        }
                    }
                }
                
                else -> {
                    // Visualização hierárquica com scroll
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        val maxWidth = constraints.maxWidth.toFloat()
                        val maxHeight = constraints.maxHeight.toFloat()
                        
                        val larguraTotal = layoutResultado?.larguraTotal ?: 0f
                        val alturaTotal = layoutResultado?.alturaTotal ?: 0f
                        
                        // Scroll horizontal e vertical
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(rememberScrollState())
                                .verticalScroll(rememberScrollState())
                                .size(
                                    width = maxOf(larguraTotal.dp, maxWidth.dp.coerceAtLeast(0.dp)),
                                    height = maxOf(alturaTotal.dp, maxHeight.dp.coerceAtLeast(0.dp))
                                )
                        ) {
                            // Memoizar pessoasMap para evitar recriação em cada recomposição
                            val pessoasMapMemoizado = remember(pessoas) {
                                pessoas.associateBy { it.id }
                            }
                            
                            // Desenhar conexões primeiro
                            DesenharConexoesHierarquicas(
                                nosHierarquicos = nosHierarquicos,
                                pessoasMap = pessoasMapMemoizado,
                                nosExpandidos = nosExpandidos,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Desenhar nós com animação de expansão
                            // Otimizado: memoizar estado selecionado para evitar recálculo
                            val pessoaSelecionadaId = remember(state.pessoaSelecionadaId) {
                                state.pessoaSelecionadaId
                            }
                            
                            nosHierarquicos.forEachIndexed { _, no ->
                                val isRaiz = no.nivel == 0
                                
                                // Se um nó está na lista, significa que foi calculado e deve ser renderizado
                                // A lista já foi filtrada pelo cálculo de layout baseado em expansão
                                NoHierarquico(
                                    pessoa = no.pessoa,
                                    nivel = no.nivel,
                                    x = no.x,
                                    y = no.y,
                                    temFilhos = no.filhosIds.isNotEmpty(),
                                    isExpanded = no.isExpanded,
                                    isSelected = pessoaSelecionadaId == no.pessoa.id,
                                    onClick = {
                                        viewModel.selecionarPessoa(no.pessoa.id)
                                    },
                                    onToggleExpand = {
                                        viewModel.toggleNo(no.pessoa.id)
                                    },
                                    onDoubleClick = {
                                        pessoaPopupInfo = no.pessoa
                                    },
                                    isRaiz = isRaiz
                                )
                            }
                        }
                    }
                }
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
            // Popup de informações da pessoa (double tap)
            pessoaPopupInfo?.let { pessoa ->
                PopupInformacoesPessoa(
                    pessoa = pessoa,
                    pessoasMap = pessoas.associateBy { it.id },
                    onDismiss = { pessoaPopupInfo = null },
                    onVerDetalhes = {
                        pessoaPopupInfo = null
                        viewModel.selecionarPessoa(pessoa.id)
                        onNavigateToDetalhesPessoa(pessoa.id)
                    }
                )
            }
        }
    }
}

/**
 * Desenha conexões hierárquicas verticais e horizontais entre nós
 */
@Composable
private fun DesenharConexoesHierarquicas(
    nosHierarquicos: List<ArvoreHierarquicaCalculator.NoHierarquico>,
    @Suppress("UNUSED_PARAMETER") pessoasMap: Map<String, Pessoa>,
    @Suppress("UNUSED_PARAMETER") nosExpandidos: Set<String>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Canvas(modifier = modifier) {
        // Criar mapa de nós por ID para acesso rápido
        val nosMap = nosHierarquicos.associateBy { it.pessoa.id }
        
        nosHierarquicos.forEach noLoop@{ no ->
            if (no.filhosIds.isEmpty()) return@noLoop
            
            // Usar o estado de expansão do próprio nó (já calculado no layout)
            if (!no.isExpanded) return@noLoop
            
            // Posição do nó pai (centro do card)
            val paiX = no.x + 32.dp.toPx() + 45.dp.toPx() // Offset do botão + meio do card
            val paiY = no.y + 45.dp.toPx() // Meio do card
            
            // Desenhar conexões para cada filho visível
            no.filhosIds.forEach filhoLoop@{ filhoId ->
                val filho = nosMap[filhoId] ?: return@filhoLoop
                
                // Posição do nó filho
                val filhoX = filho.x + 32.dp.toPx() + 45.dp.toPx()
                val filhoY = filho.y + 45.dp.toPx()
                
                // Cor baseada no tipo de relacionamento
                val cor = when (filho.tipoRelacao) {
                    ArvoreHierarquicaCalculator.TipoRelacao.PAI,
                    ArvoreHierarquicaCalculator.TipoRelacao.MAE -> secondaryColor.copy(alpha = 0.7f)
                    ArvoreHierarquicaCalculator.TipoRelacao.FILHO -> primaryColor.copy(alpha = 0.7f)
                    ArvoreHierarquicaCalculator.TipoRelacao.IRMAO -> primaryColor.copy(alpha = 0.5f)
                    else -> primaryColor.copy(alpha = 0.4f)
                }
                
                // Desenhar conexão hierárquica: vertical para baixo do pai, depois horizontal até o filho
                val linhaHorizontalY = filhoY // Linha horizontal no nível do filho
                
                // Linha vertical do pai até o nível do filho
                drawLine(
                    color = cor,
                    start = Offset(paiX, paiY),
                    end = Offset(paiX, linhaHorizontalY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // Linha horizontal até o filho
                drawLine(
                    color = cor,
                    start = Offset(paiX, linhaHorizontalY),
                    end = Offset(filhoX, linhaHorizontalY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // Linha vertical do horizontal até o filho
                drawLine(
                    color = cor,
                    start = Offset(filhoX, linhaHorizontalY),
                    end = Offset(filhoX, filhoY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Popup de informações da pessoa (aparece ao dar double tap)
 */
@Composable
private fun PopupInformacoesPessoa(
    pessoa: Pessoa,
    @Suppress("UNUSED_PARAMETER") pessoasMap: Map<String, Pessoa>,
    onDismiss: () -> Unit,
    onVerDetalhes: () -> Unit
) {
    @Suppress("DEPRECATION")
    val dateFormat = remember { 
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = pessoa.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InformacaoLinha(label = "Nome completo", valor = pessoa.nome)
                
                pessoa.dataNascimento?.let { data ->
                    val idade = pessoa.calcularIdade()
                    InformacaoLinha(
                        label = "Nascimento",
                        valor = "${dateFormat.format(data)}${idade?.let { " ($it anos)" } ?: ""}"
                    )
                }
                
                pessoa.dataFalecimento?.let { data ->
                    InformacaoLinha(
                        label = "Falecimento",
                        valor = dateFormat.format(data)
                    )
                }
                
                pessoa.estadoCivil?.let { estado ->
                    InformacaoLinha(
                        label = "Estado civil",
                        valor = estado.label
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onVerDetalhes) {
                Text("Ver detalhes completos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

/**
 * Linha de informação no popup
 */
@Composable
private fun InformacaoLinha(
    label: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
