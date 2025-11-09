package com.raizesvivas.app.presentation.screens.mural

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Recado
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.theme.PastelInfo
import com.raizesvivas.app.presentation.theme.PastelSuccess
import com.raizesvivas.app.presentation.theme.PastelWarning
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela do Mural de Recados Comunitário
 * 
 * Exibe recados gerais e direcionados em cards coloridos e lúdicos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuralScreen(
    viewModel: MuralViewModel = hiltViewModel(),
    onNavigateToDetalhesPessoa: (String) -> Unit = {},
    onNavigateToChat: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val recados by viewModel.recados.collectAsState()
    val pessoas by viewModel.pessoas.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    
    // Ordenar recados: fixados primeiro, depois por data (mais recentes primeiro)
    val recadosOrdenados = remember(recados) {
        recados.sortedWith(
            compareByDescending<Recado> { it.estaFixadoEValido() }
                .thenByDescending { it.criadoEm }
        )
    }
    
    val isRefreshing = state.isLoading
    val pullToRefreshState = rememberPullToRefreshState()
    val colorScheme = MaterialTheme.colorScheme
    val backgroundBrush = remember(colorScheme) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.18f),
                colorScheme.secondary.copy(alpha = 0.14f),
                colorScheme.background
            )
        )
    }
    
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
    
    // Snackbar para mensagens de erro
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Exibir mensagens de erro
    LaunchedEffect(state.erro) {
        state.erro?.let { mensagemErro ->
            snackbarHostState.showSnackbar(
                message = mensagemErro,
                duration = SnackbarDuration.Long
            )
            // Limpar erro após exibir
            viewModel.limparErro()
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Recados", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${recadosOrdenados.size} recado(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.recarregar() },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recarregar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = colorScheme.background.copy(alpha = 0.75f),
                    actionIconContentColor = colorScheme.primary,
                    titleContentColor = colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FAB do Chat
                FloatingActionButton(
                    onClick = onNavigateToChat,
                    containerColor = colorScheme.secondary,
                    contentColor = colorScheme.onSecondary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = "Chat"
                    )
                }
                
                // FAB principal - Novo Recado
                FloatingActionButton(
                    onClick = { viewModel.abrirModalNovoRecado() },
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Novo Recado"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(paddingValues)
            ) {
            when {
                state.isLoading && recados.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Carregando recados...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                recados.isEmpty() -> {
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
                                Icons.Default.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                "Nenhum recado ainda",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Seja o primeiro a deixar um recado para a família!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                else -> {
                    val listState = rememberLazyListState()
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = recadosOrdenados,
                            key = { it.id }
                        ) { recado ->
                            RecadoCard(
                                recado = recado,
                                currentUserId = viewModel.currentUserId,
                                isAdmin = isAdmin,
                                onDelete = { viewModel.abrirModalExcluirRecado(recado.id) },
                                onFixar = { viewModel.abrirModalFixarRecado(recado.id) },
                                onCurtir = { curtir -> viewModel.curtirRecado(recado.id, curtir) },
                                onNavigateToDetalhesPessoa = { pessoaId ->
                                    if (pessoaId != null) {
                                        onNavigateToDetalhesPessoa(pessoaId)
                                    }
                                }
                            )
                        }
                        
                        // Espaço extra no final
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        
        // FAB do Chat posicionado acima do FAB principal do Scaffold
        // Posicionado fora do Box com paddingValues para garantir visibilidade
        // O FAB principal do Scaffold fica a 16.dp do canto, então este precisa estar acima
        // ExtendedFAB tem altura de ~56.dp, então precisamos de pelo menos 72.dp de espaçamento
        FloatingActionButton(
            onClick = onNavigateToChat,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 88.dp),
            containerColor = colorScheme.secondary,
            contentColor = colorScheme.onSecondary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = "Chat",
                modifier = Modifier.size(24.dp)
            )
        }
    }
        
    // Modais (fora do Scaffold para garantir que apareçam acima de tudo)
    // Modal para criar novo recado
        if (state.mostrarModalNovoRecado) {
            ModalNovoRecado(
                pessoas = pessoas,
                onDismiss = { viewModel.fecharModalNovoRecado() },
                onConfirmar = { titulo, mensagem, destinatarioId, cor ->
                    viewModel.criarRecado(titulo, mensagem, destinatarioId, cor)
                },
                isLoading = state.isLoading
            )
        }
        
        // Modal para fixar recado (apenas admin)
        state.mostrarModalFixarRecado?.let { recadoId ->
            val recado = recados.find { it.id == recadoId }
            if (recado != null && isAdmin) {
                ModalFixarRecado(
                    recado = recado,
                    onDismiss = { viewModel.fecharModalFixarRecado() },
                    onConfirmar = { fixado, fixadoAte ->
                        viewModel.fixarRecado(recadoId, fixado, fixadoAte)
                    },
                    isLoading = state.isLoading
                )
            }
        }

        // Modal para confirmar exclusão de recado
        state.mostrarModalExcluirRecado?.let { recadoId ->
            val recado = recados.find { it.id == recadoId }
            if (recado != null) {
                ModalExcluirRecado(
                    recado = recado,
                    isLoading = state.isLoading,
                    onDismiss = { viewModel.fecharModalExcluirRecado() },
                    onConfirmar = { viewModel.deletarRecado(recado.id) }
                )
            } else {
                viewModel.fecharModalExcluirRecado()
            }
        }
    }
}

/**
 * Card de recado colorido e lúdico
 */
@Composable
private fun RecadoCard(
    recado: Recado,
    currentUserId: String?,
    isAdmin: Boolean = false,
    onDelete: () -> Unit,
    onFixar: () -> Unit = {},
    onCurtir: (Boolean) -> Unit = {},
    onNavigateToDetalhesPessoa: (String?) -> Unit
) {
    val cores = getCoresRecado(recado.cor)
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")) }
    val dataFormatada = remember(recado.criadoEm) {
        dateFormat.format(recado.criadoEm)
    }
    
    // Animação suave de entrada
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "alpha"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = alpha)
            .clickable(enabled = isAdmin) { if (isAdmin) onFixar() },
        shape = MaterialTheme.shapes.extraLarge,
        color = cores.background,
        tonalElevation = if (recado.estaFixadoEValido()) 8.dp else 4.dp,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header com autor e ações
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar circular colorido
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = cores.accent
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = cores.content,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = recado.autorNome.ifBlank { "Anônimo" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = cores.content,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Badge de fixado
                            if (recado.estaFixadoEValido()) {
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text("Fixado", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.primary,
                                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                        Text(
                            text = dataFormatada,
                            style = MaterialTheme.typography.bodySmall,
                            color = cores.content.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Botões de ação (admin pode deletar todos, autor pode deletar próprio)
                Row {
                    if (isAdmin) {
                        // Admin pode fixar/desfixar
                        IconButton(onClick = onFixar) {
                            Icon(
                                if (recado.estaFixadoEValido()) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (recado.estaFixadoEValido()) "Desfixar" else "Fixar",
                                tint = cores.content.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    // Botão de deletar (autor ou admin)
                    if (currentUserId != null && (recado.autorId == currentUserId || isAdmin)) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Deletar",
                                tint = cores.content.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            // Badge de tipo (Geral ou Direcionado)
            if (recado.ehDirecionado && recado.destinatarioNome != null) {
                AssistChip(
                    onClick = { onNavigateToDetalhesPessoa(recado.destinatarioId) },
                    label = { Text("Para: ${recado.destinatarioNome}", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = cores.accent.copy(alpha = 0.25f),
                        labelColor = cores.content,
                        leadingIconContentColor = cores.content
                    )
                )
            } else {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Recado Geral", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = cores.accent.copy(alpha = 0.25f),
                        disabledContainerColor = cores.accent.copy(alpha = 0.25f),
                        labelColor = cores.content,
                        leadingIconContentColor = cores.content
                    )
                )
            }
            
            // Título
            if (recado.titulo.isNotBlank()) {
                Text(
                    text = recado.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = cores.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Mensagem
            Text(
                text = recado.mensagem,
                style = MaterialTheme.typography.bodyLarge,
                color = cores.content,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
            )
            
            // Apoio Familiar (curtidas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val usuarioDeuApoio = recado.usuarioDeuApoio(currentUserId)
                val totalApoios = recado.totalApoios
                
                IconButton(
                    onClick = { onCurtir(!usuarioDeuApoio) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (usuarioDeuApoio) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        contentDescription = if (usuarioDeuApoio) "Remover apoio" else "Dar apoio",
                        tint = if (usuarioDeuApoio) MaterialTheme.colorScheme.error else cores.content.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = if (totalApoios > 0) totalApoios.toString() else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cores.content.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

internal data class RecadoPalette(
    val background: Color,
    val accent: Color,
    val content: Color
)

/**
 * Retorna as cores do tema baseado no nome da cor
 */
@Composable
internal fun getCoresRecado(corNome: String): RecadoPalette {
    val colorScheme = MaterialTheme.colorScheme
    return when (corNome.lowercase()) {
        "primary" -> RecadoPalette(
            background = colorScheme.primaryContainer,
            accent = colorScheme.primary.copy(alpha = 0.75f),
            content = colorScheme.onPrimaryContainer
        )
        "secondary" -> RecadoPalette(
            background = colorScheme.secondaryContainer,
            accent = colorScheme.secondary.copy(alpha = 0.75f),
            content = colorScheme.onSecondaryContainer
        )
        "tertiary" -> RecadoPalette(
            background = colorScheme.tertiaryContainer,
            accent = colorScheme.tertiary.copy(alpha = 0.75f),
            content = colorScheme.onTertiaryContainer
        )
        "error" -> RecadoPalette(
            background = colorScheme.errorContainer,
            accent = colorScheme.error.copy(alpha = 0.65f),
            content = colorScheme.onErrorContainer
        )
        "success" -> RecadoPalette(
            background = blend(colorScheme.surface, PastelSuccess, 0.2f),
            accent = PastelSuccess,
            content = colorScheme.onSurface
        )
        "warning" -> RecadoPalette(
            background = blend(colorScheme.surface, PastelWarning, 0.22f),
            accent = PastelWarning,
            content = colorScheme.onSurface
        )
        "info" -> RecadoPalette(
            background = blend(colorScheme.surface, PastelInfo, 0.22f),
            accent = PastelInfo,
            content = colorScheme.onSurface
        )
        else -> RecadoPalette(
            background = colorScheme.surfaceVariant,
            accent = colorScheme.primary.copy(alpha = 0.7f),
            content = colorScheme.onSurfaceVariant
        )
    }
}

internal fun blend(colorA: Color, colorB: Color, ratio: Float): Color {
    val inverse = 1f - ratio
    return Color(
        red = (colorA.red * inverse) + (colorB.red * ratio),
        green = (colorA.green * inverse) + (colorB.green * ratio),
        blue = (colorA.blue * inverse) + (colorB.blue * ratio),
        alpha = (colorA.alpha * inverse) + (colorB.alpha * ratio)
    )
}

