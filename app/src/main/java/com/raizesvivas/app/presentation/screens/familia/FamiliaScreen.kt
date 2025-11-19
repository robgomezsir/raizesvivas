@file:Suppress("SpellCheckingInspection")

package com.raizesvivas.app.presentation.screens.familia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Pessoa
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaScreen(
    viewModel: FamiliaViewModel = hiltViewModel(),
    onNavigateToDetalhesPessoa: (String) -> Unit,
    onNavigateToCadastroPessoa: () -> Unit = {}
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
    val nomeEditado = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Famílias",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${state.familias.size} núcleo(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCadastroPessoa,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Adicionar Pessoa"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp)
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(
                            items = state.familias,
                            key = { it.id }
                        ) { familia ->
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

                        if (state.outrosFamiliares.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                            Column {
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
                                            IconButton(onClick = { outrosFamiliaresExpandidos.value = !outrosFamiliaresExpandidos.value }) {
                                                Icon(
                                                    imageVector = if (outrosFamiliaresExpandidos.value) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                    contentDescription = if (outrosFamiliaresExpandidos.value) "Recolher" else "Expandir"
                                                )
                                            }
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
                                                state.outrosFamiliares.forEach { pessoaSemGrupo ->
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
                        tonalElevation = 2.dp
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
                    OutlinedTextField(
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
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (familia.ehFamiliaZero) 6.dp else 2.dp)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Recolher" else "Expandir"
                        )
                    }
                    Column {
                        Text(
                            text = familia.nomeExibicao,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (familia.ehFamiliaZero) {
                            Text(
                                text = "Família Zero",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
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
                    val membros = familia.membrosFlatten.ifEmpty {
                        listOf(
                            FamiliaPessoaItem(
                                pessoa = familia.conjuguePrincipal ?: return@ifEmpty emptyList(),
                                conjuge = familia.conjugueSecundario,
                                nivel = 0
                            )
                        )
                    }

                    membros.forEach { item ->
                        FamiliaPessoaRow(
                            item = item,
                            onNavigateToPessoa = onNavigateToPessoa
                        )
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
                pessoa.dataNascimento?.let { data ->
                    Text(
                        text = dateFormat.format(data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
private fun FamiliaPessoaRow(
    item: FamiliaPessoaItem,
    onNavigateToPessoa: (String) -> Unit
) {
    val indent = (item.nivel * 28).dp
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    val pessoa = item.pessoa

    val idade = pessoa.calcularIdade()
    val idadeTexto = idade?.let { "$it anos" }
        ?: pessoa.dataNascimento?.let { data -> "Desde ${dateFormat.format(data).takeLast(4)}" }
        ?: "Idade não informada"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onNavigateToPessoa(pessoa.id) },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = pessoa.getNomeExibicao().firstOrNull()?.uppercase() ?: "?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pessoa.getNomeExibicao(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = idadeTexto,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item.conjuge?.let { parceiro ->
                AssistChip(
                    onClick = { onNavigateToPessoa(parceiro.id) },
                    label = {
                        Text(
                            text = "${parceiro.getNomeExibicao()} • ${parceiro.calcularIdade()?.let { "$it anos" } ?: "Idade não informada"}"
                        )
                    }
                )
            }
        }
    }
}

