package com.raizesvivas.app.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.viewmodel.NotificacaoViewModel
import com.raizesvivas.app.presentation.components.NotificacoesModal
import com.raizesvivas.app.presentation.components.ModalSelecionarFamiliaZero
import com.raizesvivas.app.presentation.screens.home.TipoOrdenacao
import com.raizesvivas.app.presentation.theme.LocalThemeController
import com.raizesvivas.app.presentation.theme.ThemeMode
import kotlinx.coroutines.launch

/**
 * Tela Home - Principal do app
 * 
 * Mostra informações gerais e lista de pessoas da árvore
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToFamiliaZero: () -> Unit = {},
    onNavigateToCadastroPessoa: () -> Unit = {},
    onNavigateToEditarPessoa: (String) -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToDetalhesPessoa: (String) -> Unit = {},
    onNavigateToAceitarConvites: () -> Unit = {},
    onNavigateToGerenciarConvites: () -> Unit = {},
    onNavigateToGerenciarEdicoes: () -> Unit = {},
    onNavigateToResolverDuplicatas: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val pessoas by viewModel.pessoas.collectAsState()
    val parentescos by viewModel.parentescos.collectAsState()
    
    // ViewModel de Notificações
    val notificacaoViewModel: NotificacaoViewModel = hiltViewModel()
    val notificacoes by notificacaoViewModel.notificacoes.collectAsState()
    val contadorNaoLidas by notificacaoViewModel.contadorNaoLidas.collectAsState()
    
    // Estado local para busca expansível
    var mostrarBusca by remember { mutableStateOf(false) }
    var termoBusca by remember { mutableStateOf("") }
    
    // Estado local para dropdown de parentes do usuário
    var mostrarDropdownParentes by remember { mutableStateOf(false) }
    
    // Estado local para modal de notificações
    var mostrarModalNotificacoes by remember { mutableStateOf(false) }
    
    // Buscar pessoa vinculada ao usuário (memoizado para evitar recálculos)
    val pessoaVinculada = remember(state.usuario?.pessoaVinculada, pessoas) {
        state.usuario?.pessoaVinculada?.let { pessoaId ->
            pessoas.find { it.id == pessoaId }
        }
    }
    
    // Sincronizar termoBusca local com o estado quando mostrarBusca muda
    LaunchedEffect(mostrarBusca) {
        if (!mostrarBusca) {
            termoBusca = ""
            viewModel.atualizarBusca("")
        }
    }
    
    // Sincronizar termoBusca do estado para o local quando estado mudar externamente
    LaunchedEffect(state.termoBusca) {
        if (state.termoBusca != termoBusca) {
            termoBusca = state.termoBusca
        }
    }
    LaunchedEffect(state.mostrarOnboarding) {
        if (state.mostrarOnboarding) {
            onNavigateToFamiliaZero()
        }
    }
    
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
    
    // Snackbar para mensagens
    val snackbarHostState = remember { SnackbarHostState() }
    
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val themeController = LocalThemeController.current
    val isAdmin = state.usuario?.ehAdministrador == true
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                isAdmin = isAdmin,
                notificacoesNaoLidas = contadorNaoLidas,
                onClose = { scope.launch { drawerState.close() } },
                onOpenNotificacoes = {
                    scope.launch {
                        drawerState.close()
                        mostrarModalNotificacoes = true
                    }
                },
                onAdicionarPessoa = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToCadastroPessoa()
                    }
                },
                onConvitesPendentes = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToAceitarConvites()
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
                onSair = {
                    scope.launch {
                        drawerState.close()
                        viewModel.logout()
                    }
                },
                themeMode = themeController.modo,
                onThemeModeChange = { mode ->
                    themeController.selecionarModo(mode)
                }
            )
        }
    ) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raízes Vivas") },
                actions = {
                    // Botão de notificações
                    Box {
                        IconButton(onClick = { mostrarModalNotificacoes = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificações")
                        }
                        // Badge com contador de não lidas
                        if (contadorNaoLidas > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 12.dp, y = (-8).dp),
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = if (contadorNaoLidas > 99) "99+" else contadorNaoLidas.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Abrir menu lateral")
                    }
                }
            )
        },
          snackbarHost = {
              SnackbarHost(hostState = snackbarHostState)
          }
      ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // 1. Card Família Zero - Primeiro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Nome da família (em negrito) e subtítulo
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = state.familiaZeroNome?.uppercase() ?: if (state.familiaZeroExiste) "CRIADA" else "PENDENTE",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (state.familiaZeroExiste && state.familiaZeroPaiNome != null && state.familiaZeroMaeNome != null) {
                                Text(
                                    text = "${state.familiaZeroPaiNome} & ${state.familiaZeroMaeNome}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            } else if (state.familiaZeroExiste) {
                                Text(
                                    text = "(casal fundador)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // Botão de editar (apenas ADMIN e se Família Zero existe)
                        if (state.usuario?.ehAdministrador == true && state.familiaZeroExiste) {
                            IconButton(
                                onClick = { viewModel.abrirModalEditarNome() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar nome",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                        
                        // Ícone
                        IconButton(
                            onClick = { 
                                // Abrir modal para definir Família Zero (apenas ADMIN)
                                if (state.usuario?.ehAdministrador == true) {
                                    viewModel.abrirModalFamiliaZero()
                                }
                            },
                            enabled = state.usuario?.ehAdministrador == true
                        ) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = "Família Zero",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 2. Card do usuário vinculado (parentesco) - Segundo
                pessoaVinculada?.let { pessoa ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable { mostrarDropdownParentes = true },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(56.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pessoa.nome,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Você • ${parentescos.size} parentes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Ver parentes",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            
                            // Dropdown de parentes
                            DropdownMenu(
                                expanded = mostrarDropdownParentes,
                                onDismissRequest = { mostrarDropdownParentes = false },
                                modifier = Modifier.fillMaxWidth(0.95f)
                            ) {
                                if (parentescos.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Nenhum parente encontrado") },
                                        onClick = { mostrarDropdownParentes = false }
                                    )
                                } else {
                                    // Limitar a quantidade de parentes mostrados para melhor performance
                                    // DropdownMenu tem limitação de altura, então não precisa de LazyColumn
                                    // Adicionar key estável para cada item (otimização de recomposição)
                                    parentescos.take(50).forEach { (parente, resultadoParentesco) ->
                                        key(parente.id) { // Key estável para otimizar recomposições
                                            DropdownMenuItem(
                                                text = { 
                                                    Column {
                                                        Text(
                                                            text = parente.nome,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        Text(
                                                            text = resultadoParentesco.parentesco,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    mostrarDropdownParentes = false
                                                    onNavigateToDetalhesPessoa(parente.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 3. Demais cards de estatísticas - "Paredão de cards"
                Spacer(modifier = Modifier.height(8.dp))
                
                // Primeira linha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Pessoas",
                        value = state.totalPessoas.toString(),
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Famílias",
                        value = state.totalFamilias.toString(),
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Segunda linha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Meninas",
                        value = state.meninas.toString(),
                        icon = Icons.Default.Face,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Meninos",
                        value = state.meninos.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Outros",
                        value = state.outros.toString(),
                        icon = Icons.Default.PersonOutline,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Terceira linha (Ranking e Sobrinhos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Ranking",
                        value = if (state.rankingPessoas > 0) "#${state.rankingPessoas + 1}" else "-",
                        icon = Icons.Default.Star,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (state.totalSobrinhos > 0) {
                        StatCard(
                            title = "Sobrinhos",
                            value = state.totalSobrinhos.toString(),
                            icon = Icons.Default.Face,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Espaço extra no final para garantir que o último item seja visível
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Modal para definir Família Zero
            val mostrarModal by viewModel.mostrarModalFamiliaZero.collectAsState()
            if (mostrarModal) {
                ModalSelecionarFamiliaZero(
                    pessoas = pessoas,
                    onDismiss = { viewModel.fecharModalFamiliaZero() },
                    onConfirmar = { paiId: String, maeId: String ->
                        viewModel.definirFamiliaZero(paiId, maeId)
                    }
                )
            }
            
            // Modal para editar nome da Família Zero
            val mostrarModalEditarNome by viewModel.mostrarModalEditarNome.collectAsState()
            if (mostrarModalEditarNome) {
                ModalEditarNomeFamiliaZero(
                    nomeAtual = state.familiaZeroNome ?: "",
                    onDismiss = { viewModel.fecharModalEditarNome() },
                    onConfirmar = { novoNome ->
                        viewModel.atualizarNomeFamiliaZero(novoNome)
                    },
                    isLoading = state.isLoading
                )
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        
        if (mostrarModalNotificacoes) {
            NotificacoesModal(
                notificacoes = notificacoes,
                viewModel = notificacaoViewModel,
                onDismiss = { mostrarModalNotificacoes = false },
                onNotificacaoClick = { notificacao ->
                    mostrarModalNotificacoes = false
                    when (notificacao.tipo) {
                        com.raizesvivas.app.domain.model.TipoNotificacao.CONQUISTA_DESBLOQUEADA -> {
                            // Futuras navegações específicas podem ser adicionadas aqui
                        }
                        else -> Unit
                    }
                }
            )
        }
    }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PessoaCard(
    pessoa: Pessoa,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pessoa.getNomeExibicao(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (pessoa.dataNascimento != null) {
                    Text(
                        text = pessoa.calcularIdade()?.let { "$it anos" } ?: "Idade desconhecida",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalhes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Modal para editar o nome da Família Zero
 */
@Composable
private fun ModalEditarNomeFamiliaZero(
    nomeAtual: String,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit,
    isLoading: Boolean
) {
    var nomeEditado by remember { mutableStateOf(nomeAtual) }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.FamilyRestroom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Nomear Família Zero",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Digite o nome da família (ex: FAMÍLIA GOMES):",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = nomeEditado,
                    onValueChange = { nomeEditado = it },
                    label = { Text("Nome da Família") },
                    placeholder = { Text("Ex: FAMÍLIA GOMES") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    maxLines = 1
                )
                
                Text(
                    text = "Este nome será exibido como: \"${nomeEditado.uppercase()} (casal fundador)\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(nomeEditado.trim()) },
                enabled = nomeEditado.trim().isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun HomeDrawerContent(
    isAdmin: Boolean,
    notificacoesNaoLidas: Int,
    onClose: () -> Unit,
    onOpenNotificacoes: () -> Unit,
    onAdicionarPessoa: () -> Unit,
    onConvitesPendentes: () -> Unit,
    onGerenciarConvites: () -> Unit,
    onGerenciarEdicoes: () -> Unit,
    onResolverDuplicatas: () -> Unit,
    onSair: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.widthIn(min = 280.dp, max = 360.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Menu rápido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Notificações") },
                selected = false,
                onClick = onOpenNotificacoes,
                icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                badge = {
                    if (notificacoesNaoLidas > 0) {
                        Badge {
                            Text(text = if (notificacoesNaoLidas > 99) "99+" else notificacoesNaoLidas.toString())
                        }
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            DrawerSectionTitle("Ações rápidas")

            NavigationDrawerItem(
                label = { Text("Adicionar Pessoa") },
                selected = false,
                onClick = onAdicionarPessoa,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("Convites pendentes") },
                selected = false,
                onClick = onConvitesPendentes,
                icon = { Icon(Icons.Default.Mail, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            if (isAdmin) {
                NavigationDrawerItem(
                    label = { Text("Gerenciar convites") },
                    selected = false,
                    onClick = onGerenciarConvites,
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Edições pendentes") },
                    selected = false,
                    onClick = onGerenciarEdicoes,
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Resolver duplicatas") },
                    selected = false,
                    onClick = onResolverDuplicatas,
                    icon = { Icon(Icons.Default.CopyAll, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

            DrawerSectionTitle("Tema")

            ThemeSelector(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

            NavigationDrawerItem(
                label = { Text("Sair") },
                selected = false,
                onClick = onSair,
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onErrorContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )

            TextButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            ) {
                Text("Fechar")
            }
        }
    }
}

@Composable
private fun DrawerSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun ThemeSelector(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Escolha como o app deve se comportar em relação ao tema.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOptionChip(
                label = "Sistema",
                selected = themeMode == ThemeMode.SISTEMA,
                onClick = { onThemeModeChange(ThemeMode.SISTEMA) }
            )
            ThemeOptionChip(
                label = "Claro",
                selected = themeMode == ThemeMode.CLARO,
                onClick = { onThemeModeChange(ThemeMode.CLARO) }
            )
            ThemeOptionChip(
                label = "Escuro",
                selected = themeMode == ThemeMode.ESCURO,
                onClick = { onThemeModeChange(ThemeMode.ESCURO) }
            )
        }
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            labelColor = if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        border = if (selected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
        } else {
            null
        }
    )
}
