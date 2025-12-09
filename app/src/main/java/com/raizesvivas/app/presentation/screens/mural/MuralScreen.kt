package com.raizesvivas.app.presentation.screens.mural

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
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

import java.text.SimpleDateFormat
import java.util.*
import com.raizesvivas.app.presentation.components.ExpandableFab
import com.raizesvivas.app.presentation.components.FabAction
import com.raizesvivas.app.presentation.screens.home.HomeDrawerContent
import com.raizesvivas.app.presentation.theme.LocalThemeController
import com.raizesvivas.app.presentation.theme.ThemeMode
import com.raizesvivas.app.presentation.viewmodel.NotificacaoViewModel
import androidx.compose.material.icons.filled.MoreVert
import kotlinx.coroutines.launch

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
    onNavigateToChat: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToGerenciarConvites: () -> Unit = {},
    onNavigateToGerenciarEdicoes: () -> Unit = {},
    onNavigateToResolverDuplicatas: () -> Unit = {},
    onNavigateToGerenciarUsuarios: () -> Unit = {},
    onNavigateToConfiguracoes: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val recados by viewModel.recados.collectAsState()
    val pessoas by viewModel.pessoas.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    
    // Notification ViewModel for drawer
    val notificacaoViewModel: NotificacaoViewModel = hiltViewModel()
    val contadorNaoLidas by notificacaoViewModel.contadorNaoLidas.collectAsState()
    
    // Drawer state
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val themeController = LocalThemeController.current
    
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
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                isAdmin = isAdmin,
                isAdminSenior = false,
                notificacoesNaoLidas = contadorNaoLidas,
                pedidosPendentes = 0,
                onClose = { scope.launch { drawerState.close() } },
                onOpenNotificacoes = { scope.launch { drawerState.close() } },
                onNavigateToPerfil = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToPerfil()
                    }
                },
                onGerenciarConvites = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToGerenciarConvites()
                    }
                },
                onGerenciarEdicoes = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToGerenciarEdicoes()
                    }
                },
                onResolverDuplicatas = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToResolverDuplicatas()
                    }
                },
                onGerenciarUsuarios = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToGerenciarUsuarios()
                    }
                },
                onConfiguracoes = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToConfiguracoes()
                    }
                },
                onSair = {
                    scope.launch {
                        drawerState.close()
                        // Logout handled by navigation
                    }
                },
                themeMode = themeController.modo,
                onThemeModeChange = { mode: ThemeMode ->
                    themeController.selecionarModo(mode)
                }
            )
        }
    ) {
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recados (${recadosOrdenados.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                windowInsets = WindowInsets(0.dp),
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
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Abrir menu lateral")
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
            ExpandableFab(
                actions = listOf(
                    FabAction(
                        label = "Chat",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        onClick = onNavigateToChat,
                        containerColor = colorScheme.secondary,
                        contentColor = colorScheme.onSecondary
                    ),
                    FabAction(
                        label = "Novo Recado",
                        icon = Icons.Default.Add,
                        onClick = { viewModel.abrirModalNovoRecado() },
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(paddingValues)
            ) {
                AnimatedVisibility(visible = state.totalMensagensChatNaoLidas > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MarkChatUnread,
                                            contentDescription = null,
                                            tint = colorScheme.primary
                                        )
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val total = state.totalMensagensChatNaoLidas
                                    Text(
                                        text = if (total == 1)
                                            "Você tem 1 nova mensagem no chat"
                                        else
                                            "Você tem $total novas mensagens no chat",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Abra o chat para continuar a conversa",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            TextButton(onClick = onNavigateToChat) {
                                Text("Abrir chat")
                            }
                        }
                    }
                }

                if (state.totalMensagensChatNaoLidas > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when {
                    state.isLoading && recados.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                top = 8.dp,
                                bottom = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
    
    // Modais (fora do Scaffold para garantir que apareçam acima de tudo)
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
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
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
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val usuarioDeuApoio = recado.usuarioDeuApoio(currentUserId)
                val totalApoios = recado.totalApoios
                
                // Estado para animação de clique
                var isAnimating by remember { mutableStateOf(false) }
                
                // Animação de escala ao clicar
                val scale by animateFloatAsState(
                    targetValue = if (isAnimating) 1.3f else 1f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = 400f
                    ),
                    label = "heartScale",
                    finishedListener = { isAnimating = false }
                )
                
                IconButton(
                    onClick = { 
                        isAnimating = true
                        onCurtir(!usuarioDeuApoio) 
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (usuarioDeuApoio) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = if (usuarioDeuApoio) "Remover apoio" else "Dar apoio",
                            tint = if (usuarioDeuApoio) 
                                MaterialTheme.colorScheme.error 
                            else 
                                cores.content.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Contagem de apoios com animação
                AnimatedVisibility(
                    visible = totalApoios > 0,
                    enter = scaleIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(200))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = totalApoios.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = cores.content.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (totalApoios == 1) "apoio" else "apoios",
                            style = MaterialTheme.typography.bodySmall,
                            color = cores.content.copy(alpha = 0.6f)
                        )
                    }
                }
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
            background = colorScheme.tertiaryContainer,
            accent = colorScheme.tertiary,
            content = colorScheme.onTertiaryContainer
        )
        "warning" -> RecadoPalette(
            background = colorScheme.errorContainer.copy(alpha = 0.7f),
            accent = colorScheme.error.copy(alpha = 0.8f),
            content = colorScheme.onErrorContainer
        )
        "info" -> RecadoPalette(
            background = colorScheme.primaryContainer.copy(alpha = 0.7f),
            accent = colorScheme.primary.copy(alpha = 0.8f),
            content = colorScheme.onPrimaryContainer
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

