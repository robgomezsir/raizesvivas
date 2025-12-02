@file:Suppress("SpellCheckingInspection", "UNUSED_VALUE")

package com.raizesvivas.app.presentation.screens.familia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.border
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import coil.compose.AsyncImage
import com.raizesvivas.app.presentation.components.PersonAvatar
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.raizesvivas.app.domain.model.Amigo
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.RaizesVivasTextField
import com.raizesvivas.app.presentation.components.AnimatedSearchBar
import com.raizesvivas.app.presentation.components.TreeNodeData
import java.text.SimpleDateFormat
import java.util.Locale
import com.raizesvivas.app.presentation.theme.LocalThemeController
import com.raizesvivas.app.presentation.theme.ThemeMode
import com.raizesvivas.app.presentation.components.ExpandableFab
import com.raizesvivas.app.presentation.components.FabAction
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaScreen(
    viewModel: FamiliaViewModel = hiltViewModel(),
    onNavigateToDetalhesPessoa: (String) -> Unit,
    onNavigateToCadastroPessoa: () -> Unit = {},
    onNavigateToAdicionarAmigo: () -> Unit = {},
    onNavigateToAlbum: () -> Unit = {},
    onNavigateToArvoreHierarquica: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing && !state.isRefreshing) {
            viewModel.onRefresh()
        }
    }

    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing && pullRefreshState.isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }

    val familiaParaEdicao = remember { mutableStateOf<FamiliaUiModel?>(null) }
    val familiaParaGerenciar = remember { mutableStateOf<FamiliaUiModel?>(null) }
    val familiaParaRemocao = remember { mutableStateOf<FamiliaUiModel?>(null) }
    val familiaParaAdicao = remember { mutableStateOf<FamiliaUiModel?>(null) }
    val mostrarSelecaoAdicionar = rememberSaveable { mutableStateOf(false) }
    val pessoaParaConfirmarRemocao = remember { mutableStateOf<Pessoa?>(null) }
    val pessoaParaConfirmarAdicao = remember { mutableStateOf<Pessoa?>(null) }
    val outrosFamiliaresExpandidos = rememberSaveable { mutableStateOf(false) }
    var familiasRejeitadasExpandidas by rememberSaveable { mutableStateOf(false) }
    val amigosExpandidos = rememberSaveable { mutableStateOf(false) }
    val amigoParaVincular = remember { mutableStateOf<com.raizesvivas.app.domain.model.Amigo?>(null) }
    val amigoParaExcluir = remember { mutableStateOf<com.raizesvivas.app.domain.model.Amigo?>(null) }
    val amigoParaEditar = remember { mutableStateOf<com.raizesvivas.app.domain.model.Amigo?>(null) }
    val nomeAmigoEditado = remember { mutableStateOf("") }
    val telefoneAmigoEditado = remember { mutableStateOf("") }
    val nomeEditado = remember { mutableStateOf("") }
    var mostrarBusca by rememberSaveable { mutableStateOf(false) }
    var termoBusca by rememberSaveable { mutableStateOf("") }
    var modoVisualizacao by rememberSaveable { mutableStateOf(ModoVisualizacao.LISTA) }
    var abaSelecionada by rememberSaveable { mutableStateOf(0) } // 0 = Hierárquica, 1 = Lista
    var contadorAcessoHierarquica by rememberSaveable { mutableStateOf(0) } // Contador para mudar imagem ao acessar aba
    
    // Extrair todas as pessoas das famílias para uso em toda a tela
    val todasPessoasDasFamilias = remember(state.familias) {
        state.familias.flatMap { familia ->
            val pessoas = mutableSetOf<Pessoa>()
            // Adicionar cônjuges
            familia.conjuguePrincipal?.let { pessoas.add(it) }
            familia.conjugueSecundario?.let { pessoas.add(it) }
            // Adicionar membros da árvore
            familia.membrosFlatten.forEach { item ->
                pessoas.add(item.pessoa)
                item.conjuge?.let { pessoas.add(it) }
            }
            // Adicionar membros extras
            familia.membrosExtras.forEach { pessoas.add(it) }
            pessoas.toList()
        }.distinctBy { pessoa -> pessoa.id }
    }
    
    // Estado para drag and drop
    var familiaSendoArrastada by remember { mutableStateOf<String?>(null) }
    var offsetYArrastado by remember { mutableStateOf(0f) }
    var indiceInicialArrasto by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!mostrarBusca) {
                        Text(
                            text = "Famílias (${state.familias.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        AnimatedSearchBar(
                            query = termoBusca,
                            onQueryChange = { termoBusca = it },
                            isSearchActive = mostrarBusca,
                            onSearchActiveChange = { 
                                mostrarBusca = it
                                if (!it) termoBusca = ""
                            },
                            placeholder = "Buscar pessoa...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                actions = {
                    if (!mostrarBusca) {
                        IconButton(onClick = { mostrarBusca = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Buscar")
                        }
                        IconButton(onClick = onNavigateToAlbum) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "Álbum de Família")
                        }
                        IconButton(onClick = { viewModel.onRefresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
                        }
                    } else {
                        IconButton(onClick = { 
                            mostrarBusca = false
                            termoBusca = ""
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Fechar busca")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        },
        floatingActionButton = {
            // IMPORTANTE: Todos os usuários podem adicionar amigos
            // Não há restrições de administrador para esta funcionalidade
            ExpandableFab(
                actions = listOf(
                    FabAction(
                        label = "Adicionar Amigo",
                        icon = Icons.Outlined.GroupAdd,
                        onClick = onNavigateToAdicionarAmigo,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    FabAction(
                        label = "Adicionar Pessoa",
                        icon = Icons.Filled.Person,
                        onClick = onNavigateToCadastroPessoa,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            // Pull to refresh indicator - só aparece quando está refreshing
            if (pullRefreshState.isRefreshing || pullRefreshState.progress > 0) {
                PullToRefreshContainer(
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            
            state.erro?.let { mensagemErro ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 0.dp
                    ) {
                        Text(
                            text = mensagemErro,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
        Column(
            modifier = Modifier
                    .fillMaxSize()
            ) {
                // TabRow com as duas abas
                TabRow(
                    selectedTabIndex = abaSelecionada,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = abaSelecionada == 0,
                        onClick = { 
                            abaSelecionada = 0
                            contadorAcessoHierarquica++ // Incrementar contador ao acessar aba
                        },
                        text = { Text("Hierárquica") },
                        icon = {
                                        Icon(
                                            imageVector = Icons.Filled.AccountTree,
                                contentDescription = "Árvore Hierárquica"
                            )
                        }
                    )
                    Tab(
                        selected = abaSelecionada == 1,
                        onClick = { abaSelecionada = 1 },
                        text = { Text("Lista") },
                        icon = {
                                        Icon(
                                imageVector = Icons.Filled.ViewList,
                                contentDescription = "Árvore Lista"
                            )
                        }
                    )
                }
                
                // Conteúdo baseado na aba selecionada
                when (abaSelecionada) {
                    0 -> {
                        // Aba Hierárquica - Mostrar árvore diretamente
                        ConteudoAbaHierarquica(
                            state = state,
                            onNavigateToDetalhesPessoa = onNavigateToDetalhesPessoa,
                            keyAcesso = contadorAcessoHierarquica
                        )
                    }
                    1 -> {
                        // Aba Lista - Mostrar lista de famílias
                        ConteudoAbaLista(
                            state = state,
                            todasPessoasDasFamilias = todasPessoasDasFamilias,
                            familiaSendoArrastada = familiaSendoArrastada,
                            offsetYArrastado = offsetYArrastado,
                            indiceInicialArrasto = indiceInicialArrasto,
                            onFamiliaSendoArrastadaChange = { familiaSendoArrastada = it },
                            onOffsetYArrastadoChange = { offsetYArrastado = it },
                            onIndiceInicialArrastoChange = { indiceInicialArrasto = it },
                            viewModel = viewModel,
                            familiaParaEdicao = familiaParaEdicao,
                            nomeEditado = nomeEditado,
                            familiaParaGerenciar = familiaParaGerenciar,
                            familiasRejeitadasExpandidas = familiasRejeitadasExpandidas,
                            onFamiliasRejeitadasExpandidasChange = { familiasRejeitadasExpandidas = it },
                            outrosFamiliaresExpandidos = outrosFamiliaresExpandidos,
                            amigosExpandidos = amigosExpandidos,
                            amigoParaVincular = amigoParaVincular,
                            amigoParaExcluir = amigoParaExcluir,
                            amigoParaEditar = amigoParaEditar,
                            nomeAmigoEditado = nomeAmigoEditado,
                            telefoneAmigoEditado = telefoneAmigoEditado,
                            termoBusca = termoBusca,
                            onNavigateToDetalhesPessoa = onNavigateToDetalhesPessoa,
                            onNavigateToAdicionarAmigo = onNavigateToAdicionarAmigo
                        )
                    }
                }
            }
        } // Fim do Box
    } // Fim do Scaffold

    // Dialogs e modais (mantidos fora do Scaffold)

    if (familiaParaEdicao.value != null) {
        AlertDialog(
            onDismissRequest = { familiaParaEdicao.value = null },
            title = {
                Text(
                    text = "Renomear família",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = nomeEditado.value,
                        onValueChange = { nomeEditado.value = it },
                        label = { Text("Nome da família") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Sugestão: ${familiaParaEdicao.value?.nomePadrao}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    familiaParaEdicao.value?.let { familia ->
                        viewModel.atualizarNomeFamilia(familia, nomeEditado.value)
                    }
                    familiaParaEdicao.value = null
                }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                Button(onClick = { familiaParaEdicao.value = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    familiaParaGerenciar.value?.let { familiaSelecionada ->
        AlertDialog(
            onDismissRequest = { familiaParaGerenciar.value = null },
            title = {
                Text(
                    text = "Gerenciar família",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            familiaParaAdicao.value = familiaSelecionada
                            mostrarSelecaoAdicionar.value = true
                            familiaParaGerenciar.value = null
                        },
                        enabled = !state.isLoading && state.outrosFamiliares.isNotEmpty()
                    ) {
                        Text("Adicionar familiar")
                    }
                    OutlinedButton(
                        onClick = {
                            familiaParaRemocao.value = familiaSelecionada
                            familiaParaGerenciar.value = null
                        },
                        enabled = !state.isLoading
                    ) {
                        Text("Remover familiar")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { familiaParaGerenciar.value = null }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (mostrarSelecaoAdicionar.value && familiaParaAdicao.value != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarSelecaoAdicionar.value = false
                familiaParaAdicao.value = null
            },
            title = {
                Text(
                    text = "Selecionar familiar",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                if (state.outrosFamiliares.isEmpty()) {
                    Text(
                        text = "Nenhum familiar disponível para adicionar nesta família.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.outrosFamiliares, key = { pessoa: Pessoa -> pessoa.id }) { pessoa: Pessoa ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(enabled = !state.isLoading) {
                                        pessoaParaConfirmarAdicao.value = pessoa
                                        mostrarSelecaoAdicionar.value = false
                                    },
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = pessoa.getNomeExibicao(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    pessoa.calcularIdade()?.let { idade ->
                                        Text(
                                            text = "$idade anos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarSelecaoAdicionar.value = false
                        familiaParaAdicao.value = null
                    }
                ) {
                    Text("Fechar")
                }
            }
        )
    }

    // Diálogo para confirmar criação de família monoparental com pai + filhos
    state.familiaPendenteAtual?.let { pendente ->
        if (state.mostrarDialogFamiliaPendente) {
            AlertDialog(
                onDismissRequest = { viewModel.fecharDialogFamiliaPendente() },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Família Monoparental com Pai",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Foi detectada uma família monoparental com pai + filhos:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Responsável:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = pendente.responsavel.getNomeExibicao(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                if (pendente.filhos.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Filhos (${pendente.filhos.size}):",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    pendente.filhos.forEach { filho ->
                                        Text(
                                            text = "• ${filho.getNomeExibicao()}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                        
                        Text(
                            text = "Deseja criar esta família monoparental?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "Nota: Por padrão, famílias monoparentais são criadas apenas para mãe + filhos. Famílias com pai + filhos requerem confirmação.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmarCriarFamiliaMonoparental() }
                    ) {
                        Text("Criar Família")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.cancelarCriarFamiliaMonoparental() }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    if (pessoaParaConfirmarAdicao.value != null && familiaParaAdicao.value != null) {
        val pessoaSelecionada = pessoaParaConfirmarAdicao.value!!
        val familiaSelecionada = familiaParaAdicao.value!!
        AlertDialog(
            onDismissRequest = { pessoaParaConfirmarAdicao.value = null },
            title = {
                Text(
                    text = "Confirmar adição",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Deseja adicionar ${pessoaSelecionada.getNomeExibicao()} à família \"${familiaSelecionada.nomeExibicao}\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adicionarMembro(familiaSelecionada.id, pessoaSelecionada.id)
                        pessoaParaConfirmarAdicao.value = null
                        familiaParaAdicao.value = null
                    },
                    enabled = !state.isLoading
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        pessoaParaConfirmarAdicao.value = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    familiaParaRemocao.value?.let { familiaSelecionada ->
        val membrosDisponiveis = remember(familiaSelecionada) {
            val membros = mutableListOf<Pessoa>()
            familiaSelecionada.conjuguePrincipal?.let { membros.add(it) }
            familiaSelecionada.conjugueSecundario?.let { membros.add(it) }
            membros.addAll(familiaSelecionada.membrosFlatten.map { it.pessoa })
            membros.addAll(
                familiaSelecionada.membrosFlatten.mapNotNull { it.conjuge }
            )
            membros.addAll(familiaSelecionada.membrosExtras)
            membros.distinctBy { it.id }.filter { it.id.isNotBlank() }
        }

        AlertDialog(
            onDismissRequest = { familiaParaRemocao.value = null },
            title = {
                Text(
                    text = "Remover familiar",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                if (membrosDisponiveis.isEmpty()) {
                    Text(
                        text = "Nenhum membro disponível para remoção.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Selecione quem deseja remover da família.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(membrosDisponiveis, key = { pessoa: Pessoa -> pessoa.id }) { pessoa: Pessoa ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(enabled = !state.isLoading) {
                                            pessoaParaConfirmarRemocao.value = pessoa
                                        },
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = pessoa.getNomeExibicao(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        pessoa.calcularIdade()?.let { idade ->
                                            Text(
                                                text = "$idade anos",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { familiaParaRemocao.value = null }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (pessoaParaConfirmarRemocao.value != null && familiaParaRemocao.value != null) {
        val pessoaSelecionada = pessoaParaConfirmarRemocao.value!!
        val familiaSelecionada = familiaParaRemocao.value!!
        AlertDialog(
            onDismissRequest = { pessoaParaConfirmarRemocao.value = null },
            title = {
                Text(
                    text = "Confirmar remoção",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Remover ${pessoaSelecionada.getNomeExibicao()} da família \"${familiaSelecionada.nomeExibicao}\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removerMembro(familiaSelecionada.id, pessoaSelecionada.id)
                        pessoaParaConfirmarRemocao.value = null
                    },
                    enabled = !state.isLoading
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pessoaParaConfirmarRemocao.value = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Modal para vincular familiar ao amigo
    amigoParaVincular.value?.let { amigo ->
        val pessoasDisponiveisParaVincular = remember(state.amigos, state.outrosFamiliares, todasPessoasDasFamilias) {
            val todasPessoas = (todasPessoasDasFamilias + state.outrosFamiliares).distinctBy { pessoa -> pessoa.id }
            todasPessoas.filter { pessoa ->
                pessoa.id !in amigo.familiaresVinculados
            }
        }
        
        AlertDialog(
            onDismissRequest = { amigoParaVincular.value = null },
            title = {
                Text(
                    text = "Vincular Familiar a ${amigo.nome}",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                if (pessoasDisponiveisParaVincular.isEmpty()) {
                    Text(
                        text = "Todos os familiares já estão vinculados a este amigo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pessoasDisponiveisParaVincular, key = { pessoa: Pessoa -> pessoa.id }) { pessoa ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(enabled = !state.isLoading) {
                                        viewModel.vincularFamiliarAoAmigo(amigo.id, pessoa.id)
                                        amigoParaVincular.value = null
                                    },
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = pessoa.getNomeExibicao(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    pessoa.calcularIdade()?.let { idade ->
                                        Text(
                                            text = "$idade anos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        amigoParaVincular.value = null
                    }
                ) {
                    Text("Fechar")
                }
            }
        )
    }
    
    // Modal de confirmação para excluir amigo
    amigoParaExcluir.value?.let { amigo ->
        AlertDialog(
            onDismissRequest = { amigoParaExcluir.value = null },
            title = {
                Text(
                    text = "Excluir Amigo",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Deseja realmente excluir ${amigo.nome}? Esta ação não pode ser desfeita.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.excluirAmigo(amigo.id)
                        amigoParaExcluir.value = null
                    },
                    enabled = !state.isLoading
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { amigoParaExcluir.value = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal para editar amigo (nome e telefone)
    amigoParaEditar.value?.let { amigo ->
        AlertDialog(
            onDismissRequest = { amigoParaEditar.value = null },
            title = {
                Text(
                    text = "Editar Amigo",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = nomeAmigoEditado.value,
                        onValueChange = { nomeAmigoEditado.value = it },
                        label = { Text("Nome") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = telefoneAmigoEditado.value,
                        onValueChange = { telefoneAmigoEditado.value = it },
                        label = { Text("Telefone (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.atualizarAmigo(
                            amigoId = amigo.id,
                            novoNome = nomeAmigoEditado.value,
                            novoTelefone = telefoneAmigoEditado.value
                        )
                        amigoParaEditar.value = null
                    },
                    enabled = nomeAmigoEditado.value.trim().isNotEmpty() && !state.isLoading
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { amigoParaEditar.value = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OutroFamiliarRow(
    pessoa: Pessoa,
    onNavigateToPessoa: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onNavigateToPessoa(pessoa.id) },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = pessoa.getNomeExibicao(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                val idade = pessoa.calcularIdade()
                val idadeTexto = idade?.let { "$it anos" }
                    ?: pessoa.dataNascimento?.let { data -> dateFormat.format(data) }
                    ?: "Idade não informada"
                val apelido = pessoa.apelido?.takeIf { it.isNotBlank() }
                val linhaSecundaria = when {
                    apelido != null && idadeTexto.isNotBlank() -> "$apelido - $idadeTexto"
                    apelido != null -> apelido
                    else -> idadeTexto
                }
                Text(
                    text = linhaSecundaria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = "Ver detalhes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FamiliaHierarquiaNode(
    node: FamiliaHierarquiaItem,
    expansionState: MutableMap<String, Boolean>,
    onNavigateToPessoa: (String) -> Unit
) {
    val nodeKey = node.pessoa.id
    val hasChildren = node.filhos.isNotEmpty()
    val expanded = expansionState[nodeKey] ?: false
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "seta-filhos"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val colorScheme = MaterialTheme.colorScheme
        val cardColor = obterCorCardPorNivel(node.nivel, colorScheme)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onNavigateToPessoa(node.pessoa.id) },
            color = cardColor,
            contentColor = contentColorFor(cardColor),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val titulo = obterTituloHierarquia(node)
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (hasChildren) {
                            IconButton(
                                onClick = {
                                    expansionState[nodeKey] = !expanded
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ExpandMore,
                                    contentDescription = if (expanded) "Recolher descendentes" else "Expandir descendentes",
                                    modifier = Modifier.rotate(rotation)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = node.pessoa.getNomeExibicao(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            // Otimizar: usar remember para cálculos pesados
                            val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
                            val linhaSecundaria = remember(node.pessoa.id, node.pessoa.dataNascimento, node.pessoa.apelido) {
                                val idade = node.pessoa.calcularIdade()
                                val idadeTexto = idade?.let { "$it anos" }
                                    ?: node.pessoa.dataNascimento?.let { data -> "Desde ${dateFormat.format(data).takeLast(4)}" }
                                    ?: "Idade não informada"
                                val apelido = node.pessoa.apelido?.takeIf { it.isNotBlank() }
                                when {
                                    apelido != null && idadeTexto.isNotBlank() -> "$apelido - $idadeTexto"
                                    apelido != null -> apelido
                                    else -> idadeTexto
                                }
                            }
                            Text(
                                text = linhaSecundaria,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                node.conjuge?.let { parceiro ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                    // Otimizar: reutilizar dateFormat e usar remember para cálculos
                    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
                    val linhaSecundariaConjuge = remember(parceiro.id, parceiro.dataNascimento, parceiro.apelido) {
                        val idadeParceiro = parceiro.calcularIdade()
                        val idadeParceiroTexto = idadeParceiro?.let { "$it anos" }
                            ?: parceiro.dataNascimento?.let { data -> "Desde ${dateFormat.format(data).takeLast(4)}" }
                            ?: "Idade não informada"
                        val apelidoParceiro = parceiro.apelido?.takeIf { it.isNotBlank() }
                        when {
                            apelidoParceiro != null && idadeParceiroTexto.isNotBlank() -> "$apelidoParceiro - $idadeParceiroTexto"
                            apelidoParceiro != null -> apelidoParceiro
                            else -> idadeParceiroTexto
                        }
                    }

                    // Otimizar: usar remember para label do cônjuge
                    val labelConjuge = remember(node.nivel, parceiro.genero) {
                        if (node.nivel == 0) {
                            when (parceiro.genero) {
                                Genero.FEMININO -> "MÃE"
                                Genero.MASCULINO -> "PAI"
                                else -> "RESPONSÁVEL"
                            }
                        } else {
                            "CÔNJUGE"
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardColor.copy(alpha = 0.6f))
                            .clickable { onNavigateToPessoa(parceiro.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = labelConjuge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = parceiro.getNomeExibicao(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = linhaSecundariaConjuge,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Ver cônjuge",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (hasChildren) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    node.filhos.forEach { filho ->
                        FamiliaHierarquiaNode(
                            node = filho,
                            expansionState = expansionState,
                            onNavigateToPessoa = onNavigateToPessoa
                        )
                    }
                }
            }
        }
    }
}

private data class FamiliaHierarquiaItem(
    val pessoa: Pessoa,
    val conjuge: Pessoa? = null,
    val nivel: Int,
    val papel: PapelHierarquia,
    val ordemEntreIrmaos: Int? = null,
    val filhos: List<FamiliaHierarquiaItem> = emptyList()
)

private enum class PapelHierarquia {
    ASCENDENTE,
    DESCENDENTE
}

private fun construirHierarquiaFamilia(familia: FamiliaUiModel): List<FamiliaHierarquiaItem> {
    val raiz = familia.treeRoot ?: return construirHierarquiaSemArvore(familia)
    return raiz.normalizarAscendente().toHierarchyList()
}

private fun construirHierarquiaSemArvore(familia: FamiliaUiModel): List<FamiliaHierarquiaItem> {
    val resultado = mutableListOf<FamiliaHierarquiaItem>()
    when {
        familia.conjuguePrincipal != null -> {
            resultado.add(
                FamiliaHierarquiaItem(
                    pessoa = familia.conjuguePrincipal,
                    conjuge = familia.conjugueSecundario,
                    nivel = 0,
                    papel = PapelHierarquia.ASCENDENTE
                )
            )
        }
        familia.conjugueSecundario != null -> {
            resultado.add(
                FamiliaHierarquiaItem(
                    pessoa = familia.conjugueSecundario,
                    nivel = 0,
                    papel = PapelHierarquia.ASCENDENTE
                )
            )
        }
    }

    if (resultado.isEmpty() && familia.membrosFlatten.isNotEmpty()) {
        val itensFallback = familia.membrosFlatten.map { item ->
            FamiliaHierarquiaItem(
                pessoa = item.pessoa,
                conjuge = item.conjuge,
                nivel = item.nivel,
                papel = if (item.nivel == 0) PapelHierarquia.ASCENDENTE else PapelHierarquia.DESCENDENTE
            )
        }
        resultado.addAll(itensFallback)
    }

    return resultado
}

private fun TreeNodeData.normalizarAscendente(): TreeNodeData {
    val parceiro = conjuge
    return if (
        parceiro != null &&
        pessoa.genero != Genero.FEMININO &&
        parceiro.genero == Genero.FEMININO
    ) {
        copy(pessoa = parceiro, conjuge = pessoa)
    } else {
        this
    }
}

private fun TreeNodeData.toHierarchyList(): List<FamiliaHierarquiaItem> {
    fun adicionar(node: TreeNodeData, nivel: Int, papel: PapelHierarquia, ordem: Int?): FamiliaHierarquiaItem {
        val filhosOrdenados = ordenarDescendentes(node.children)
        val filhos = filhosOrdenados.mapIndexed { index, filho ->
            adicionar(filho, nivel + 1, PapelHierarquia.DESCENDENTE, index + 1)
        }
        
        return FamiliaHierarquiaItem(
            pessoa = node.pessoa,
            conjuge = node.conjuge,
            nivel = nivel,
            papel = papel,
            ordemEntreIrmaos = ordem,
            filhos = filhos
        )
    }

    return listOf(adicionar(this, 0, PapelHierarquia.ASCENDENTE, null))
}

private fun ordenarDescendentes(children: List<TreeNodeData>): List<TreeNodeData> {
    if (children.isEmpty()) return emptyList()

    return children.sortedWith { a, b ->
        val idadeA = a.pessoa.calcularIdade()
        val idadeB = b.pessoa.calcularIdade()
        when {
            idadeA != null && idadeB != null -> idadeB.compareTo(idadeA)
            idadeA != null -> -1
            idadeB != null -> 1
            else -> {
                val nascimentoA = a.pessoa.dataNascimento?.time
                val nascimentoB = b.pessoa.dataNascimento?.time
                when {
                    nascimentoA != null && nascimentoB != null -> nascimentoA.compareTo(nascimentoB)
                    nascimentoA != null -> -1
                    nascimentoB != null -> 1
                    else -> a.pessoa.nome.lowercase(Locale.getDefault())
                        .compareTo(b.pessoa.nome.lowercase(Locale.getDefault()))
                }
            }
        }
    }
}

private fun obterCorCardPorNivel(nivel: Int, colorScheme: ColorScheme): Color {
    return when (nivel % 4) {
        0 -> colorScheme.primaryContainer
        1 -> colorScheme.secondaryContainer
        2 -> colorScheme.tertiaryContainer
        else -> colorScheme.surfaceVariant
    }
}

private fun obterTituloHierarquia(item: FamiliaHierarquiaItem): String {
    val genero = item.pessoa.genero
    return when (item.nivel) {
        0 -> when (genero) {
            Genero.FEMININO -> "MÃE"
            Genero.MASCULINO -> "PAI"
            else -> "RESPONSÁVEL"
        }
        1 -> {
            val base = when (genero) {
                Genero.FEMININO -> "FILHA"
                Genero.MASCULINO -> "FILHO"
                else -> "FILHO/FILHA"
            }
            val posicao = item.ordemEntreIrmaos ?: 0
            if (posicao > 0) "$base ($posicao)" else base
        }
        2 -> when (genero) {
            Genero.FEMININO -> "NETA"
            Genero.MASCULINO -> "NETO"
            else -> "NETO/NETA"
        }
        3 -> when (genero) {
            Genero.FEMININO -> "BISNETA"
            Genero.MASCULINO -> "BISNETO"
            else -> "BISNETO/BISNETA"
        }
        4 -> when (genero) {
            Genero.FEMININO -> "TRINETA"
            Genero.MASCULINO -> "TRINETO"
            else -> "TRINETO/TRINETA"
        }
        5 -> when (genero) {
            Genero.FEMININO -> "TETRANETA"
            Genero.MASCULINO -> "TETRANETO"
            else -> "TETRANETO/TETRANETA"
        }
        6 -> when (genero) {
            Genero.FEMININO -> "PENTANETA"
            Genero.MASCULINO -> "PENTANETO"
            else -> "PENTANETO/PENTANETA"
        }
        else -> "DESCENDENTE"
    }
}



/**
 * Card de exibição de amigo da família
 * 
 * IMPORTANTE: Todos os usuários têm acesso total a todas as funcionalidades deste card:
 * - Editar amigo
 * - Excluir amigo
 * - Vincular familiares
 * - Remover vínculos de familiares
 * 
 * Não há restrições de administrador.
 */
@Composable
private fun AmigoCard(
    amigo: Amigo,
    pessoasDisponiveis: List<Pessoa>,
    onVincularFamiliar: () -> Unit,
    onExcluirAmigo: () -> Unit,
    onEditarAmigo: () -> Unit,
    onRemoverVinculo: (String) -> Unit
) {
    val familiaresVinculados = remember(amigo.familiaresVinculados, pessoasDisponiveis) {
        pessoasDisponiveis
            .filter { it.id in amigo.familiaresVinculados }
            .distinctBy { pessoa -> pessoa.id }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = amigo.getNomeExibicao(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    amigo.telefone?.let { telefone ->
                        if (telefone.isNotBlank()) {
                            Text(
                                text = telefone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Botões de ação
                Row {
                    // Botão editar
                    IconButton(onClick = onEditarAmigo) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar Amigo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Botão de excluir
                    IconButton(onClick = onExcluirAmigo) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Excluir Amigo",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    // Botão + para vincular mais familiares
                    IconButton(onClick = onVincularFamiliar) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Vincular Familiar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Lista de familiares vinculados
            if (familiaresVinculados.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                Text(
                    text = "Vinculado(s):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    familiaresVinculados.forEach { familiar ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = familiar.getNomeExibicao(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onRemoverVinculo(familiar.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remover vínculo",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Conteúdo da aba Hierárquica - Mostra card que navega para a visualização hierárquica
 */
@Composable
private fun ConteudoAbaHierarquica(
    state: com.raizesvivas.app.presentation.screens.familia.FamiliaState,
    onNavigateToDetalhesPessoa: (String) -> Unit,
    keyAcesso: Int = 0 // Chave que muda a cada acesso à aba
) {
    // Extrair todas as pessoas das famílias
    val todasPessoas = remember(state.familias) {
        state.familias.flatMap { familia ->
            val pessoas = mutableSetOf<Pessoa>()
            familia.conjuguePrincipal?.let { pessoas.add(it) }
            familia.conjugueSecundario?.let { pessoas.add(it) }
            familia.membrosFlatten.forEach { item ->
                pessoas.add(item.pessoa)
                item.conjuge?.let { pessoas.add(it) }
            }
            familia.membrosExtras.forEach { pessoas.add(it) }
            pessoas.toList()
        }.distinctBy { it.id }
    }
    
    // Construir estrutura de árvore recursiva
    val arvoreRaiz = remember(todasPessoas) {
        construirArvoreRecursiva(todasPessoas)
    }
    
    // Helper para encontrar irmãos de um nó
    fun encontrarIrmaos(nodeId: String, raiz: NoArvore?): List<String> {
        if (raiz == null) return emptyList()
        val irmaos = mutableListOf<String>()
        
        fun buscarIrmaos(no: NoArvore) {
            no.filhos.forEach { filho ->
                if (filho.pessoa.id == nodeId) {
                    no.filhos.forEach { irmao ->
                        if (irmao.pessoa.id != nodeId) {
                            irmaos.add(irmao.pessoa.id)
                        }
                    }
                } else {
                    buscarIrmaos(filho)
                }
            }
        }
        
        buscarIrmaos(raiz)
        return irmaos
    }
    
    // Helper para encontrar o caminho de ancestrais até um nó
    fun encontrarCaminhoAncestral(nodeId: String, raiz: NoArvore?): List<String> {
        if (raiz == null) return emptyList()
        val caminho = mutableListOf<String>()
        
        fun buscarCaminho(no: NoArvore): Boolean {
            if (no.pessoa.id == nodeId) {
                caminho.add(no.pessoa.id)
                return true
            }
            
            for (filho in no.filhos) {
                if (buscarCaminho(filho)) {
                    caminho.add(0, no.pessoa.id)
                    return true
                }
            }
            
            return false
        }
        
        buscarCaminho(raiz)
        return caminho
    }
    
    // Helper para coletar todos os nós em níveis anteriores ao nó especificado
    fun coletarNosNiveisAnteriores(nodeId: String, raiz: NoArvore?): List<String> {
        if (raiz == null) return emptyList()
        val todosNos = mutableListOf<String>()
        var nivelAlvo = -1
        
        fun encontrarNivel(no: NoArvore, nivelAtual: Int): Int {
            if (no.pessoa.id == nodeId) return nivelAtual
            for (filho in no.filhos) {
                val nivel = encontrarNivel(filho, nivelAtual + 1)
                if (nivel != -1) return nivel
            }
            return -1
        }
        
        nivelAlvo = encontrarNivel(raiz, 0)
        if (nivelAlvo == -1) return emptyList()
        
        fun coletar(no: NoArvore, nivelAtual: Int) {
            if (nivelAtual < nivelAlvo) {
                todosNos.add(no.pessoa.id)
                no.filhos.forEach { coletar(it, nivelAtual + 1) }
            }
        }
        
        coletar(raiz, 0)
        return todosNos
    }
    
    // Estado de expansão dos FILHOS (IDs dos nós com filhos visíveis)
    val expandedNodeIds = remember { mutableStateListOf<String>() }
    
    // Estado de expansão dos CÔNJUGES (IDs dos nós com cônjuge visível)
    val expandedSpouseIds = remember { mutableStateListOf<String>() }
    
    // Calcular posições Verticais com base na expansão
    val verticalNodes = remember(arvoreRaiz, expandedNodeIds.toList(), expandedSpouseIds.toList()) {
        if (arvoreRaiz != null) {
            calcularLayoutVertical(arvoreRaiz, expandedNodeIds.toSet(), expandedSpouseIds.toSet())
        } else {
            emptyList()
        }
    }
    
    // Estado para zoom e pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.2f, 4f)
        offset += panChange
    }
    
    // Cores do estilo Raízes Vivas
    val Heritage50 = Color(0xFFF5FDF7)
    val Heritage900 = Color(0xFF0A3D00)
    val Emerald = Color(0xFF67EF1F)
    val MaternalColor = Color(0xFF4CAF50)
    val PaternalColor = Color(0xFFFFA726)
    val Slate = Color(0xFF00A200)
    
    // Obter contexto para acessar recursos dinamicamente
    val context = LocalContext.current
    
    // Lista de nomes de recursos de imagens (img_01.png até img_012.png)
    // Os arquivos foram renomeados para seguir a convenção do Android (nomes devem começar com letra)
    val nomesImagens = listOf("img_01", "img_02", "img_03", "img_04", "img_05", "img_06", "img_07", "img_08", "img_09", "img_010", "img_011", "img_012")
    
    // Obter IDs dos recursos dinamicamente
    val imagensBackground = remember(context) {
        nomesImagens.mapNotNull { nome ->
            val resourceId = context.resources.getIdentifier(nome, "drawable", context.packageName)
            if (resourceId != 0) resourceId else null
        }
    }
    
    // Estado para rastrear a imagem atual e a última usada
    var imagemAtualIndex by rememberSaveable { mutableStateOf(-1) }
    var ultimaImagemIndex by rememberSaveable { mutableStateOf(-1) }
    
    // Selecionar uma nova imagem aleatória ao acessar a aba
    // A chave keyAcesso muda toda vez que a aba é acessada, forçando uma nova seleção
    LaunchedEffect(keyAcesso) {
        if (imagensBackground.isNotEmpty()) {
            // Selecionar uma imagem diferente da última usada (se houver e se for um novo acesso)
            val imagensDisponiveis = if (ultimaImagemIndex >= 0 && keyAcesso > 0) {
                imagensBackground.indices.filter { it != ultimaImagemIndex }
            } else {
                imagensBackground.indices.toList()
            }
            
            if (imagensDisponiveis.isEmpty()) {
                // Se só há uma imagem ou todas foram usadas, usar qualquer uma
                imagemAtualIndex = (0 until imagensBackground.size).random()
            } else {
                imagemAtualIndex = imagensDisponiveis.random()
            }
            ultimaImagemIndex = imagemAtualIndex
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = transformableState)
    ) {
        // Imagem de fundo com opacidade de 60% - renderizada primeiro para ficar atrás de tudo
        if (imagensBackground.isNotEmpty() && imagemAtualIndex >= 0 && imagemAtualIndex < imagensBackground.size) {
            Image(
                painter = painterResource(id = imagensBackground[imagemAtualIndex]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.6f),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback: background sólido se não houver imagem
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Heritage50)
            )
        }
        when {
            state.isLoading && todasPessoas.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Emerald)
                }
            }
            
            arvoreRaiz == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Árvore genealógica incompleta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Heritage900
                        )
                        Text(
                            text = "Adicione a Família Zero (Raiz) para visualizar a árvore.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            else -> {
                // Canvas Infinito Vertical
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    val density = LocalDensity.current
                    
                    // Centralizar a Raiz inicialmente no topo/centro
                    val startX = constraints.maxWidth / 2f
                    val startY = 100f
                    
                    // 1. Desenhar Conexões (Background)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        translate(left = startX, top = startY) {
                            verticalNodes.forEach { node ->
                                node.childrenIds.forEach { childId ->
                                    val childNode = verticalNodes.find { it.id == childId }
                                    if (childNode != null) {
                                        val start = node.position.copy(y = node.position.y + 35f)
                                        val end = childNode.position.copy(y = childNode.position.y - 35f)
                                        
                                        val color = when (childNode.genero) {
                                            com.raizesvivas.app.domain.model.Genero.FEMININO -> MaternalColor
                                            com.raizesvivas.app.domain.model.Genero.MASCULINO -> PaternalColor
                                            else -> Slate
                                        }
                                        
                                        val path = Path()
                                        path.moveTo(start.x, start.y)
                                        
                                        val midY = (start.y + end.y) / 2
                                        
                                        path.cubicTo(
                                            start.x, midY,
                                            end.x, midY,
                                            end.x, end.y
                                        )
                                        
                                        drawPath(
                                            path = path,
                                            color = color,
                                            style = Stroke(
                                                width = 3f,
                                                cap = StrokeCap.Round
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 2. Desenhar Nós (Foreground)
                    verticalNodes.forEach { node ->
                        val nodeX = startX + node.position.x
                        val nodeY = startY + node.position.y
                        val avatarSize = 70.dp
                        val isSpouseVisible = expandedSpouseIds.contains(node.id) && node.conjuge != null
                        val cardWidth = if (isSpouseVisible) avatarSize * 2 + 10.dp else avatarSize
                        
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = with(density) { nodeX.toDp() } - (cardWidth / 2),
                                    y = with(density) { nodeY.toDp() } - (avatarSize / 2)
                                )
                                .width(cardWidth)
                                .height(avatarSize + 50.dp)
                        ) {
                            PessoaVerticalCard(
                                pessoa = node.pessoa,
                                conjuge = node.conjuge,
                                corBorda = getCorPorNivel(node.level),
                                tamanho = avatarSize,
                                isSpouseVisible = isSpouseVisible,
                                onSingleTap = { 
                                    val isCurrentlyExpanded = expandedNodeIds.contains(node.id) || expandedSpouseIds.contains(node.id)
                                    
                                    if (isCurrentlyExpanded) {
                                        expandedNodeIds.remove(node.id)
                                        expandedSpouseIds.remove(node.id)
                                    } else {
                                        val caminhoAncestral = encontrarCaminhoAncestral(node.id, arvoreRaiz)
                                        val nosNiveisAnteriores = coletarNosNiveisAnteriores(node.id, arvoreRaiz)
                                        
                                        nosNiveisAnteriores.forEach { noId ->
                                            if (!caminhoAncestral.contains(noId)) {
                                                expandedNodeIds.remove(noId)
                                                expandedSpouseIds.remove(noId)
                                            }
                                        }
                                        
                                        val irmaos = encontrarIrmaos(node.id, arvoreRaiz)
                                        irmaos.forEach { irmaoId ->
                                            expandedNodeIds.remove(irmaoId)
                                            expandedSpouseIds.remove(irmaoId)
                                        }
                                        
                                        if (node.hasChildren) {
                                            expandedNodeIds.add(node.id)
                                        }
                                        if (node.conjuge != null) {
                                            expandedSpouseIds.add(node.id)
                                        }
                                    }
                                },
                                onDoubleTap = {
                                    onNavigateToDetalhesPessoa(node.id)
                                },
                                label = getLabelPorNivel(node.level, node.pessoa.genero),
                                isExpanded = expandedNodeIds.contains(node.id) || expandedSpouseIds.contains(node.id),
                                hasChildren = node.hasChildren
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Conteúdo da aba Lista - Mostra lista completa de famílias
 */
@Composable
private fun ConteudoAbaLista(
    state: com.raizesvivas.app.presentation.screens.familia.FamiliaState,
    todasPessoasDasFamilias: List<Pessoa>,
    familiaSendoArrastada: String?,
    offsetYArrastado: Float,
    indiceInicialArrasto: Int,
    onFamiliaSendoArrastadaChange: (String?) -> Unit,
    onOffsetYArrastadoChange: (Float) -> Unit,
    onIndiceInicialArrastoChange: (Int) -> Unit,
    viewModel: FamiliaViewModel,
    familiaParaEdicao: androidx.compose.runtime.MutableState<FamiliaUiModel?>,
    nomeEditado: androidx.compose.runtime.MutableState<String>,
    familiaParaGerenciar: androidx.compose.runtime.MutableState<FamiliaUiModel?>,
    familiasRejeitadasExpandidas: Boolean,
    onFamiliasRejeitadasExpandidasChange: (Boolean) -> Unit,
    outrosFamiliaresExpandidos: androidx.compose.runtime.MutableState<Boolean>,
    amigosExpandidos: androidx.compose.runtime.MutableState<Boolean>,
    amigoParaVincular: androidx.compose.runtime.MutableState<Amigo?>,
    amigoParaExcluir: androidx.compose.runtime.MutableState<Amigo?>,
    amigoParaEditar: androidx.compose.runtime.MutableState<Amigo?>,
    nomeAmigoEditado: androidx.compose.runtime.MutableState<String>,
    telefoneAmigoEditado: androidx.compose.runtime.MutableState<String>,
    termoBusca: String,
    onNavigateToDetalhesPessoa: (String) -> Unit,
    onNavigateToAdicionarAmigo: () -> Unit
) {
    // Buscar pessoas individuais quando houver termo de busca
    val pessoasFiltradas = remember(
        todasPessoasDasFamilias,
        state.outrosFamiliares,
        termoBusca
    ) {
        if (termoBusca.isBlank()) {
            emptyList<Pessoa>()
        } else {
            val termoLower = termoBusca.lowercase(Locale.getDefault())
            val todasPessoas = (todasPessoasDasFamilias + state.outrosFamiliares).distinctBy { pessoa: Pessoa -> pessoa.id }
            
            todasPessoas.filter { pessoa: Pessoa ->
                pessoa.nome.lowercase(Locale.getDefault()).contains(termoLower) ||
                pessoa.apelido?.lowercase(Locale.getDefault())?.contains(termoLower) == true ||
                pessoa.profissao?.lowercase(Locale.getDefault())?.contains(termoLower) == true ||
                pessoa.localNascimento?.lowercase(Locale.getDefault())?.contains(termoLower) == true ||
                pessoa.localResidencia?.lowercase(Locale.getDefault())?.contains(termoLower) == true
            }
        }
    }
    
    val mostrarPessoasIndividuais = termoBusca.isNotBlank()
    val familiasFiltradas = remember(state.familias) { state.familias }
    val outrosFamiliaresFiltrados = remember(state.outrosFamiliares) { state.outrosFamiliares }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Estados de loading e vazio
        if (state.isLoading && state.familias.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Carregando famílias...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (state.familias.isEmpty() && !mostrarPessoasIndividuais) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Nenhuma família encontrada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cadastre novos membros para visualizar a hierarquia familiar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Conteúdo da lista de famílias
            if (mostrarPessoasIndividuais) {
                // Mostrar apenas pessoas individuais quando houver busca
                if (pessoasFiltradas.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma pessoa encontrada",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = pessoasFiltradas,
                        key = { pessoa: Pessoa -> pessoa.id }
                    ) { pessoa: Pessoa ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDetalhesPessoa(pessoa.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = pessoa.getNomeExibicao(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                pessoa.apelido?.let { apelido ->
                                    if (apelido.isNotBlank() && apelido != pessoa.nome) {
                                        Text(
                                            text = "Apelido: $apelido",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                pessoa.profissao?.let { profissao ->
                                    if (profissao.isNotBlank()) {
                                        Text(
                                            text = "Profissão: $profissao",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                pessoa.localNascimento?.let { local ->
                                    if (local.isNotBlank()) {
                                        Text(
                                            text = "Local de Nascimento: $local",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                pessoa.localResidencia?.let { residencia ->
                                    if (residencia.isNotBlank()) {
                                        Text(
                                            text = "Local de Residência: $residencia",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Mostrar famílias normalmente quando não houver busca
                items(
                    items = familiasFiltradas,
                    key = { familia: FamiliaUiModel -> familia.id }
                ) { familia: FamiliaUiModel ->
                    val indice = familiasFiltradas.indexOf(familia)
                    val estaSendoArrastada = familiaSendoArrastada == familia.id
                    val offsetY = if (estaSendoArrastada) offsetYArrastado else 0f
                    
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = offsetY
                                alpha = if (estaSendoArrastada) 0.7f else 1f
                            }
                            .then(
                                if (!familia.ehFamiliaZero) {
                                    Modifier.pointerInput(familia.id, familiasFiltradas.size, offsetYArrastado, indiceInicialArrasto) {
                                        var offsetYAcumulado = 0f
                                        
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                onFamiliaSendoArrastadaChange(familia.id)
                                                onIndiceInicialArrastoChange(indice)
                                                onOffsetYArrastadoChange(0f)
                                                offsetYAcumulado = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                offsetYAcumulado += dragAmount.y
                                                onOffsetYArrastadoChange(offsetYAcumulado)
                                            },
                                            onDragEnd = {
                                                val threshold = 120f // Altura aproximada de um card com padding
                                                val deltaIndices = (offsetYAcumulado / threshold).toInt()
                                                
                                                val novoIndice = (indiceInicialArrasto + deltaIndices).coerceIn(
                                                    0,
                                                    familiasFiltradas.size - 1
                                                )
                                                
                                                if (novoIndice != indiceInicialArrasto && novoIndice >= 0 && !familia.ehFamiliaZero) {
                                                    val familiaZeroIndex = familiasFiltradas.indexOfFirst { f: FamiliaUiModel -> f.ehFamiliaZero }
                                                    if (novoIndice != familiaZeroIndex && novoIndice >= 0) {
                                                        val novaOrdem = familiasFiltradas.toMutableList()
                                                        novaOrdem.removeAt(indiceInicialArrasto)
                                                        novaOrdem.add(novoIndice, familia)
                                                        
                                                        val novaOrdemIds = novaOrdem.map { f: FamiliaUiModel -> f.id }
                                                        viewModel.reordenarFamilias(novaOrdemIds)
                                                    }
                                                }
                                                
                                                onFamiliaSendoArrastadaChange(null)
                                                onOffsetYArrastadoChange(0f)
                                                onIndiceInicialArrastoChange(-1)
                                                offsetYAcumulado = 0f
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        FamiliaCard(
                            familia = familia,
                            isExpanded = state.expandedFamilias.contains(familia.id),
                            usuarioEhAdmin = state.usuarioEhAdmin,
                            onToggle = { viewModel.toggleFamilia(familia.id) },
                            onEditarNome = {
                                familiaParaEdicao.value = familia
                                nomeEditado.value = familia.nomeExibicao
                            },
                            onGerenciarFamilia = { familiaSelecionada: FamiliaUiModel ->
                                familiaParaGerenciar.value = familiaSelecionada
                            },
                            onNavigateToPessoa = onNavigateToDetalhesPessoa,
                            modoVisualizacao = ModoVisualizacao.LISTA
                        )
                    }
                }
                
                // Seção de famílias monoparentais rejeitadas
                if (state.familiasRejeitadas.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFamiliasRejeitadasExpandidasChange(!familiasRejeitadasExpandidas) },
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Sugestões de Outras Familias",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${state.familiasRejeitadas.size} família(s) que você optou por não criar. Clique para solicitar a criação novamente.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = if (familiasRejeitadasExpandidas) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = if (familiasRejeitadasExpandidas) "Recolher" else "Expandir",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                AnimatedVisibility(
                                    visible = familiasRejeitadasExpandidas,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    ) {
                                        state.familiasRejeitadas.forEach { familiaRejeitada ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Text(
                                                        text = "Responsável:",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = familiaRejeitada.responsavel.getNomeExibicao(),
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.clickable {
                                                            onNavigateToDetalhesPessoa(familiaRejeitada.responsavel.id)
                                                        }
                                                    )
                                                    
                                                    if (familiaRejeitada.filhos.isNotEmpty()) {
                                                        Text(
                                                            text = "Filhos (${familiaRejeitada.filhos.size}):",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        familiaRejeitada.filhos.forEach { filho: Pessoa ->
                                                            Text(
                                                                text = "• ${filho.getNomeExibicao()}",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                modifier = Modifier.clickable {
                                                                    onNavigateToDetalhesPessoa(filho.id)
                                                                }
                                                            )
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    
                                                    Button(
                                                        onClick = {
                                                            viewModel.solicitarCriarFamiliaMonoparental(familiaRejeitada.responsavel.id)
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("Solicitar Criação desta Família")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Outros familiares
                if (outrosFamiliaresFiltrados.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { outrosFamiliaresExpandidos.value = !outrosFamiliaresExpandidos.value },
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Outros familiares",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Membros sem vínculo direto a um núcleo familiar.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = if (outrosFamiliaresExpandidos.value) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = if (outrosFamiliaresExpandidos.value) "Recolher" else "Expandir",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                AnimatedVisibility(
                                    visible = outrosFamiliaresExpandidos.value,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    ) {
                                        outrosFamiliaresFiltrados.forEach { pessoaSemGrupo: Pessoa ->
                                            OutroFamiliarRow(
                                                pessoa = pessoaSemGrupo,
                                                onNavigateToPessoa = onNavigateToDetalhesPessoa
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Amigos da Família
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = state.amigos.isNotEmpty()) { 
                                if (state.amigos.isNotEmpty()) {
                                    amigosExpandidos.value = !amigosExpandidos.value
                                }
                            },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Amigo da Família",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Amigos próximos da família.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (state.amigos.isNotEmpty()) {
                                    Icon(
                                        imageVector = if (amigosExpandidos.value) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = if (amigosExpandidos.value) "Recolher" else "Expandir",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            AnimatedVisibility(
                                visible = amigosExpandidos.value || state.amigos.isEmpty(),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    if (state.amigos.isEmpty()) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp)
                                        ) {
                                            Text(
                                                text = "Nenhum amigo cadastrado",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        state.amigos.forEach { amigo: Amigo ->
                                            AmigoCard(
                                                amigo = amigo,
                                                pessoasDisponiveis = todasPessoasDasFamilias + state.outrosFamiliares,
                                                onVincularFamiliar = {
                                                    amigoParaVincular.value = amigo
                                                },
                                                onExcluirAmigo = {
                                                    amigoParaExcluir.value = amigo
                                                },
                                                onEditarAmigo = {
                                                    amigoParaEditar.value = amigo
                                                    nomeAmigoEditado.value = amigo.nome
                                                    telefoneAmigoEditado.value = amigo.telefone ?: ""
                                                },
                                                onRemoverVinculo = { familiarId: String ->
                                                    viewModel.removerFamiliarDoAmigo(amigo.id, familiarId)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card para visualização em LISTA - Design rico com cores e badges
 */
@Composable
private fun FamiliaCardLista(
    familia: FamiliaUiModel,
    isExpanded: Boolean,
    usuarioEhAdmin: Boolean,
    onToggle: () -> Unit,
    onEditarNome: () -> Unit,
    onGerenciarFamilia: (FamiliaUiModel) -> Unit,
    onNavigateToPessoa: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Cor única e consistente para todos os cards usando a nova paleta
    val containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onToggle() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Recolher" else "Expandir",
                            tint = when {
                                familia.ehFamiliaZero -> colorScheme.primary
                                familia.ehFamiliaMonoparental -> colorScheme.secondary
                                else -> colorScheme.onSurface
                            }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Ícone diferenciado por tipo de família
                            Icon(
                                imageVector = when {
                                    familia.ehFamiliaZero -> Icons.Filled.Home
                                    familia.ehFamiliaMonoparental -> Icons.Filled.Person
                                    else -> Icons.Filled.FamilyRestroom
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = when {
                                    familia.ehFamiliaZero -> colorScheme.primary
                                    familia.ehFamiliaMonoparental -> colorScheme.secondary
                                    else -> colorScheme.tertiary
                                }
                            )
                            Text(
                                text = familia.nomeExibicao,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { onGerenciarFamilia(familia) },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = when {
                                    familia.ehFamiliaZero -> colorScheme.primary
                                    else -> colorScheme.onSurface
                                }
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Gerenciar família",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (usuarioEhAdmin) {
                            IconButton(
                                onClick = onEditarNome,
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Editar nome da família",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                // Badges para identificar tipo de família
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (familia.ehFamiliaReconstituida) {
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    "Família Anterior",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                ) 
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = colorScheme.error,
                                labelColor = colorScheme.onError
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    
                    // Badge para tipo de núcleo familiar
                    when (familia.tipoNucleoFamiliar) {
                        com.raizesvivas.app.domain.model.TipoNucleoFamiliar.RESIDENCIAL -> {
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        "Residencial",
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorScheme.secondaryContainer,
                                    labelColor = colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                        com.raizesvivas.app.domain.model.TipoNucleoFamiliar.EMOCIONAL -> {
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        "Emocional",
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorScheme.tertiaryContainer,
                                    labelColor = colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                        com.raizesvivas.app.domain.model.TipoNucleoFamiliar.ADOTIVA -> {
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        "Adotiva",
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorScheme.primaryContainer,
                                    labelColor = colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                        else -> { /* PARENTESCO ou RECONSTITUIDA já têm badges específicos */ }
                    }
                }
            }


            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val hierarquia = remember(
                        familia.treeRoot,
                        familia.conjuguePrincipal,
                        familia.conjugueSecundario,
                        familia.membrosFlatten
                    ) {
                        construirHierarquiaFamilia(familia)
                    }

                    val expansionState = remember(familia.id) {
                        mutableStateMapOf<String, Boolean>()
                    }

                    LaunchedEffect(familia.id, isExpanded) {
                        expansionState.clear()
                    }

                    if (hierarquia.isEmpty()) {
                        Text(
                            text = "Nenhum membro cadastrado para esta família.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        hierarquia.forEach { node ->
                            FamiliaHierarquiaNode(
                                node = node,
                                expansionState = expansionState,
                                onNavigateToPessoa = onNavigateToPessoa
                            )
                        }
                    }

                    if (familia.membrosExtras.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Membros vinculados manualmente",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            familia.membrosExtras.forEach { extra ->
                                AssistChip(
                                    onClick = { onNavigateToPessoa(extra.id) },
                                    label = { Text(extra.getNomeExibicao()) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card para visualização HIERÁRQUICA (Árvore) - Design mais simples e compacto
 */
@Composable
private fun FamiliaCardHierarquia(
    familia: FamiliaUiModel,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToPessoa: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (familia.ehFamiliaZero) {
                colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                colorScheme.surfaceColorAtElevation(1.dp)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggle() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when {
                            familia.ehFamiliaZero -> Icons.Filled.Home
                            familia.ehFamiliaMonoparental -> Icons.Filled.Person
                            else -> Icons.Filled.FamilyRestroom
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (familia.ehFamiliaZero) colorScheme.primary else colorScheme.onSurface
                    )
                    Text(
                        text = familia.nomeExibicao,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (familia.ehFamiliaZero) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val hierarquia = remember(
                        familia.treeRoot,
                        familia.conjuguePrincipal,
                        familia.conjugueSecundario,
                        familia.membrosFlatten
                    ) {
                        construirHierarquiaFamilia(familia)
                    }

                    val expansionState = remember(familia.id) {
                        mutableStateMapOf<String, Boolean>()
                    }

                    LaunchedEffect(familia.id, isExpanded) {
                        expansionState.clear()
                    }

                    if (hierarquia.isEmpty()) {
                        Text(
                            text = "Nenhum membro cadastrado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        hierarquia.forEach { node ->
                            FamiliaHierarquiaNode(
                                node = node,
                                expansionState = expansionState,
                                onNavigateToPessoa = onNavigateToPessoa
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Wrapper que decide qual card usar (mantém compatibilidade)
 */
@Composable
private fun FamiliaCard(
    familia: FamiliaUiModel,
    isExpanded: Boolean,
    usuarioEhAdmin: Boolean,
    onToggle: () -> Unit,
    onEditarNome: () -> Unit,
    onGerenciarFamilia: (FamiliaUiModel) -> Unit,
    onNavigateToPessoa: (String) -> Unit,
    modoVisualizacao: ModoVisualizacao = ModoVisualizacao.LISTA
) {
    when (modoVisualizacao) {
        ModoVisualizacao.LISTA -> {
            FamiliaCardLista(
                familia = familia,
                isExpanded = isExpanded,
                usuarioEhAdmin = usuarioEhAdmin,
                onToggle = onToggle,
                onEditarNome = onEditarNome,
                onGerenciarFamilia = onGerenciarFamilia,
                onNavigateToPessoa = onNavigateToPessoa
            )
        }
        ModoVisualizacao.ARVORE -> {
            FamiliaCardHierarquia(
                familia = familia,
                isExpanded = isExpanded,
                onToggle = onToggle,
                onNavigateToPessoa = onNavigateToPessoa
            )
        }
    }
}

private enum class ModoVisualizacao {
    LISTA,
    ARVORE
}

// --- Estruturas e Funções Auxiliares para Árvore Hierárquica ---

data class NoArvore(
    val pessoa: Pessoa,
    val conjuge: Pessoa? = null,
    val filhos: List<NoArvore> = emptyList(),
    val nivel: Int = 0,
    val isMaternal: Boolean = true
)

data class VerticalNode(
    val id: String,
    val pessoa: Pessoa,
    val conjuge: Pessoa?,
    val level: Int,
    val position: Offset,
    val childrenIds: List<String>,
    val genero: Genero?,
    val hasChildren: Boolean
)

fun construirArvoreRecursiva(pessoas: List<Pessoa>): NoArvore? {
    if (pessoas.isEmpty()) return null
    val pessoasMap = pessoas.associateBy { it.id }
    val familiaZeroList = pessoas.filter { it.ehFamiliaZero }
    if (familiaZeroList.isEmpty()) return null
    
    val raizPrincipal = familiaZeroList.first()
    val raizConjuge = if (familiaZeroList.size > 1) {
        val otherZero = familiaZeroList.first { it.id != raizPrincipal.id }
        if (raizPrincipal.conjugeAtual == otherZero.id || otherZero.conjugeAtual == raizPrincipal.id) otherZero else null
    } else null
    
    fun construirNo(pessoa: Pessoa, conjuge: Pessoa?, nivel: Int, isMaternal: Boolean): NoArvore {
        val filhosIds = mutableSetOf<String>()
        pessoa.filhos.forEach { filhosIds.add(it) }
        conjuge?.filhos?.forEach { filhosIds.add(it) }
        
        val filhosPorRef = pessoas.filter { p ->
            (p.pai == pessoa.id || p.mae == pessoa.id) || (conjuge != null && (p.pai == conjuge.id || p.mae == conjuge.id))
        }
        filhosPorRef.forEach { filhosIds.add(it.id) }
        
        val filhosNos = filhosIds.mapNotNull { id -> pessoasMap[id] }
            .distinctBy { it.id }
            .map { filho ->
                val conjugeFilhoId = filho.conjugeAtual
                val conjugeFilho = if (conjugeFilhoId != null) pessoasMap[conjugeFilhoId] else null
                construirNo(filho, conjugeFilho, nivel + 1, isMaternal)
            }
            .sortedBy { it.pessoa.dataNascimento }
            
        return NoArvore(pessoa, conjuge, filhosNos, nivel, isMaternal)
    }
    
    return construirNo(raizPrincipal, raizConjuge, 0, true)
}

fun calcularLayoutVertical(
    raiz: NoArvore, 
    expandedNodeIds: Set<String>, 
    expandedSpouseIds: Set<String>
): List<VerticalNode> {
    val nodes = mutableListOf<VerticalNode>()
    val levelSpacing = 320f
    val nodeSpacing = 200f
    val spouseExtraWidth = 80f
    
    fun calcularLarguraSubarvore(no: NoArvore): Float {
        if (!expandedNodeIds.contains(no.pessoa.id) || no.filhos.isEmpty()) {
            val baseWidth = nodeSpacing
            return if (expandedSpouseIds.contains(no.pessoa.id) && no.conjuge != null) baseWidth + spouseExtraWidth else baseWidth
        }
        
        val childrenWidth = no.filhos.sumOf { calcularLarguraSubarvore(it).toDouble() }.toFloat()
        val selfWidth = if (expandedSpouseIds.contains(no.pessoa.id) && no.conjuge != null) nodeSpacing + spouseExtraWidth else nodeSpacing
        
        return maxOf(childrenWidth, selfWidth)
    }
    
    fun processNode(no: NoArvore, y: Float, xLeft: Float, width: Float) {
        val xCenter = xLeft + width / 2
        
        nodes.add(
            VerticalNode(
                id = no.pessoa.id,
                pessoa = no.pessoa,
                conjuge = no.conjuge,
                level = no.nivel,
                position = Offset(xCenter, y),
                childrenIds = no.filhos.map { it.pessoa.id },
                genero = no.pessoa.genero,
                hasChildren = no.filhos.isNotEmpty()
            )
        )
        
        if (expandedNodeIds.contains(no.pessoa.id)) {
            var currentX = xLeft
            val realChildrenWidth = no.filhos.sumOf { calcularLarguraSubarvore(it).toDouble() }.toFloat()
            val startX = xLeft + (width - realChildrenWidth) / 2
            
            currentX = startX
            
            no.filhos.forEach { filho ->
                val childWidth = calcularLarguraSubarvore(filho)
                processNode(filho, y + levelSpacing, currentX, childWidth)
                currentX += childWidth
            }
        }
    }
    
    val totalWidth = calcularLarguraSubarvore(raiz)
    processNode(raiz, 0f, -totalWidth / 2, totalWidth)
    
    return nodes
}

fun getCorPorNivel(nivel: Int): Color {
    return when (nivel) {
        0 -> Color(0xFF00A200)
        1 -> Color(0xFF67EF1F)
        2 -> Color(0xFF4DDC17)
        else -> Color(0xFF1AB508)
    }
}

fun getLabelPorNivel(nivel: Int, genero: Genero?): String? {
    val isFemale = genero == Genero.FEMININO
    return when (nivel) {
        0 -> if (isFemale) "Mãe/Raiz" else "Pai/Raiz"
        1 -> if (isFemale) "Filha" else "Filho"
        2 -> if (isFemale) "Neta" else "Neto"
        else -> "${nivel}ª Geração"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PessoaVerticalCard(
    pessoa: Pessoa,
    conjuge: Pessoa?,
    corBorda: Color,
    tamanho: androidx.compose.ui.unit.Dp,
    isSpouseVisible: Boolean,
    onSingleTap: () -> Unit,
    onDoubleTap: () -> Unit,
    label: String? = null,
    isExpanded: Boolean,
    hasChildren: Boolean
) {
    val Heritage50 = Color(0xFFF5FDF7)
    val Heritage900 = Color(0xFF0A3D00)
    val Emerald = Color(0xFF67EF1F)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .wrapContentWidth()
                .combinedClickable(
                    onClick = onSingleTap,
                    onDoubleClick = onDoubleTap
                )
        ) {
            AvatarComBorda(pessoa, corBorda, tamanho)
            
            AnimatedVisibility(
                visible = isSpouseVisible && conjuge != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (conjuge != null) {
                    Row {
                        Spacer(modifier = Modifier.width(8.dp))
                        AvatarComBorda(conjuge, corBorda, tamanho)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        val nomePrincipal = pessoa.nome.split(" ").first()
        val nomeConjuge = conjuge?.nome?.split(" ")?.first()
        val textoNome = if (isSpouseVisible && nomeConjuge != null) "$nomePrincipal & $nomeConjuge" else nomePrincipal
        
        Surface(
            modifier = Modifier.wrapContentWidth(),
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(6.dp),
            shadowElevation = 2.dp
        ) {
            Text(
                text = textoNome,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Heritage900,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .padding(top = 2.dp),
            color = Heritage50.copy(alpha = 0.9f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                
                if (hasChildren || conjuge != null) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Expandido" else "Recolhido",
                        modifier = Modifier.size(14.dp),
                        tint = if (isExpanded) Emerald else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarComBorda(
    pessoa: Pessoa,
    corBorda: Color,
    tamanho: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .shadow(4.dp, CircleShape)
            .background(Color.White, CircleShape)
            .border(3.dp, corBorda, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (pessoa.fotoUrl != null && pessoa.fotoUrl.isNotBlank()) {
            AsyncImage(
                model = pessoa.fotoUrl,
                contentDescription = pessoa.nome,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            PersonAvatar(
                personId = pessoa.id,
                personName = pessoa.nome,
                size = tamanho,
                textSize = (tamanho.value * 0.4).sp
            )
        }
    }
}

