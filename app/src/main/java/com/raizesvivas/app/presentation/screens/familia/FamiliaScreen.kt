@file:Suppress("SpellCheckingInspection", "UNUSED_VALUE")

package com.raizesvivas.app.presentation.screens.familia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaScreen(
    viewModel: FamiliaViewModel = hiltViewModel(),
    onNavigateToDetalhesPessoa: (String) -> Unit,
    onNavigateToCadastroPessoa: () -> Unit = {},
    onNavigateToAdicionarAmigo: () -> Unit = {},
    onNavigateToAlbum: () -> Unit = {}
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
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Buscar"
                            )
                        }
                        IconButton(onClick = onNavigateToAlbum) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = "Álbum de Família"
                            )
                        }
                        IconButton(onClick = { viewModel.onRefresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
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
                .padding(horizontal = 8.dp)
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            when {
                state.isLoading && state.familias.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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

                state.familias.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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

                else -> {
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
                            val todasPessoas = (todasPessoasDasFamilias + state.outrosFamiliares).distinctBy { it.id }
                            
                            todasPessoas.filter { pessoa ->
                                pessoa.nome.lowercase(Locale.getDefault()).contains(termoLower) ||
                                pessoa.apelido?.lowercase(Locale.getDefault())?.contains(termoLower) == true ||
                                pessoa.profissao?.lowercase(Locale.getDefault())?.contains(termoLower) == true ||
                                pessoa.localNascimento?.lowercase(Locale.getDefault())?.contains(termoLower) == true ||
                                pessoa.localResidencia?.lowercase(Locale.getDefault())?.contains(termoLower) == true
                            }
                        }
                    }
                    
                    // Se houver busca, mostrar apenas pessoas individuais
                    val mostrarPessoasIndividuais = termoBusca.isNotBlank()
                    
                    val familiasFiltradas = remember(state.familias) {
                        state.familias
                    }
                    
                    val outrosFamiliaresFiltrados = remember(state.outrosFamiliares) {
                        state.outrosFamiliares
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    key = { it.id }
                                ) { pessoa ->
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
                                key = { it.id }
                            ) { familia ->
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
                                                Modifier.pointerInput(familia.id, familiasFiltradas.size) {
                                                    detectDragGesturesAfterLongPress(
                                                        onDragStart = {
                                                            familiaSendoArrastada = familia.id
                                                            indiceInicialArrasto = indice
                                                            offsetYArrastado = 0f
                                                        },
                                                        onDrag = { change, dragAmount ->
                                                            change.consume()
                                                            offsetYArrastado += dragAmount.y
                                                        },
                                                        onDragEnd = {
                                                            // Calcular nova posição baseada no offset acumulado
                                                            // Usar um threshold de aproximadamente metade da altura de um card (150dp)
                                                            val threshold = 150f
                                                            val deltaIndices = (offsetYArrastado / threshold).toInt()
                                                            
                                                            val novoIndice = (indiceInicialArrasto + deltaIndices).coerceIn(
                                                                0,
                                                                familiasFiltradas.size - 1
                                                            )
                                                            
                                                            // Reordenar apenas se mudou de posição e não é Família Zero
                                                            if (novoIndice != indiceInicialArrasto && novoIndice >= 0 && !familia.ehFamiliaZero) {
                                                                // Garantir que não está tentando mover para a posição da Família Zero
                                                                val familiaZeroIndex = familiasFiltradas.indexOfFirst { it.ehFamiliaZero }
                                                                if (novoIndice != familiaZeroIndex && novoIndice != 0) {
                                                                    val novaOrdem = familiasFiltradas.toMutableList()
                                                                    novaOrdem.removeAt(indiceInicialArrasto)
                                                                    novaOrdem.add(novoIndice, familia)
                                                                    
                                                                    // Atualizar a ordem no ViewModel
                                                                    val novaOrdemIds = novaOrdem.map { it.id }
                                                                    viewModel.reordenarFamilias(novaOrdemIds)
                                                                }
                                                            }
                                                            
                                                            familiaSendoArrastada = null
                                                            offsetYArrastado = 0f
                                                            indiceInicialArrasto = -1
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
                                        onGerenciarFamilia = { familiaSelecionada ->
                                            familiaParaGerenciar.value = familiaSelecionada
                                        },
                                        onNavigateToPessoa = onNavigateToDetalhesPessoa
                                    )
                                }
                            }

                        // Seção de famílias monoparentais rejeitadas (apenas quando não houver busca)
                        if (!mostrarPessoasIndividuais && state.familiasRejeitadas.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { familiasRejeitadasExpandidas = !familiasRejeitadasExpandidas },
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
                                                                familiaRejeitada.filhos.forEach { filho ->
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

                        // Mostrar outros familiares apenas quando não houver busca
                        if (!mostrarPessoasIndividuais && outrosFamiliaresFiltrados.isNotEmpty()) {
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
                                                outrosFamiliaresFiltrados.forEach { pessoaSemGrupo ->
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
                        
                        // Seção de Amigos da Família (apenas quando não houver busca)
                        // IMPORTANTE: Todos os usuários têm acesso total a esta seção
                        // Não há restrições de administrador - qualquer usuário pode:
                        // - Ver o card quando há amigos
                        // - Adicionar, editar e excluir amigos
                        // - Vincular e remover vínculos de familiares
                        if (!mostrarPessoasIndividuais && state.amigos.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { amigosExpandidos.value = !amigosExpandidos.value },
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
                                            Icon(
                                                imageVector = if (amigosExpandidos.value) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                contentDescription = if (amigosExpandidos.value) "Recolher" else "Expandir",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = amigosExpandidos.value,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut()
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 12.dp)
                                            ) {
                                                state.amigos.forEach { amigo ->
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
                                                        onRemoverVinculo = { familiarId ->
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

            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
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
        }
    }

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
                        items(state.outrosFamiliares, key = { it.id }) { pessoa ->
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
                            items(membrosDisponiveis, key = { it.id }) { pessoa ->
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

@Composable
private fun FamiliaCard(
    familia: FamiliaUiModel,
    isExpanded: Boolean,
    usuarioEhAdmin: Boolean,
    onToggle: () -> Unit,
    onEditarNome: () -> Unit,
    onGerenciarFamilia: (FamiliaUiModel) -> Unit,
    onNavigateToPessoa: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (familia.ehFamiliaZero) {
        colorScheme.primaryContainer
    } else {
        colorScheme.surfaceColorAtElevation(3.dp)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
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
                    .clickable { onToggle() }
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir"
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Ícone diferenciado por tipo de família
                        Icon(
                            imageVector = when {
                                familia.ehFamiliaZero -> Icons.Filled.Home
                                familia.ehFamiliaMonoparental -> Icons.Filled.Person
                                else -> Icons.Filled.FamilyRestroom
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = when {
                                familia.ehFamiliaZero -> colorScheme.primary
                                familia.ehFamiliaMonoparental -> colorScheme.secondary
                                else -> colorScheme.tertiary
                            }
                        )
                        Text(
                            text = familia.nomeExibicao,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Badges para identificar tipo de família
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (familia.ehFamiliaZero) {
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        "Família Zero",
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorScheme.primaryContainer,
                                    labelColor = colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        
                        if (familia.ehFamiliaMonoparental) {
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        "Monoparental",
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorScheme.secondaryContainer,
                                    labelColor = colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        
                        // Identificar casal homoafetivo (mesmo gênero) - otimizar com remember
                        val ehHomoafetivo = remember(
                            familia.conjuguePrincipal?.genero,
                            familia.conjugueSecundario?.genero,
                            familia.ehFamiliaMonoparental
                        ) {
                            familia.conjuguePrincipal?.genero != null &&
                            familia.conjugueSecundario?.genero != null &&
                            familia.conjuguePrincipal.genero == familia.conjugueSecundario.genero &&
                            !familia.ehFamiliaMonoparental
                        }
                        
                        if (ehHomoafetivo) {
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        "Homoafetiva",
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorScheme.tertiaryContainer,
                                    labelColor = colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        
                        // Badge para famílias reconstituídas (casamentos anteriores)
                        if (familia.ehFamiliaReconstituida) {
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        "Família Anterior",
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = colorScheme.errorContainer,
                                    labelColor = colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.height(24.dp)
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
                                        containerColor = colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        labelColor = colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.height(24.dp)
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
                                        containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                                        labelColor = colorScheme.onTertiaryContainer
                                    ),
                                    modifier = Modifier.height(24.dp)
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
                                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        labelColor = colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            else -> { /* PARENTESCO ou RECONSTITUIDA já têm badges específicos */ }
                        }
                    }
                }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onGerenciarFamilia(familia) }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Gerenciar família"
                        )
                    }
                    if (usuarioEhAdmin) {
                        IconButton(onClick = onEditarNome) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar nome da família"
                            )
                        }
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
