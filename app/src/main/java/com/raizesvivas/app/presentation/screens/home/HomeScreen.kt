package com.raizesvivas.app.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.Notificacao
import com.raizesvivas.app.domain.model.TipoNotificacao
import com.raizesvivas.app.domain.model.Amigo
import com.raizesvivas.app.presentation.components.RaizesVivasTextField
import com.raizesvivas.app.presentation.viewmodel.NotificacaoViewModel
import com.raizesvivas.app.presentation.components.NotificacoesModal
import com.raizesvivas.app.presentation.components.ModalFestivoAniversario
import com.raizesvivas.app.presentation.components.ModalNotificacaoAdmin
import com.raizesvivas.app.presentation.components.ModalNovaMensagem
import com.raizesvivas.app.presentation.components.ModalSelecionarFamiliaZero
import com.raizesvivas.app.presentation.screens.chat.ChatViewModel
import com.raizesvivas.app.presentation.screens.familia.FamiliaViewModel
import com.raizesvivas.app.presentation.theme.LocalThemeController
import com.raizesvivas.app.presentation.theme.ThemeMode
import com.raizesvivas.app.utils.ParentescoCalculator
import kotlinx.coroutines.launch
import com.raizesvivas.app.BuildConfig
import java.util.Calendar

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
    onNavigateToConquistas: () -> Unit = {},
    onNavigateToDetalhesPessoa: (String) -> Unit = {},
    onNavigateToAceitarConvites: () -> Unit = {},
    onNavigateToGerenciarConvites: () -> Unit = {},
    onNavigateToGerenciarEdicoes: () -> Unit = {},
    onNavigateToResolverDuplicatas: () -> Unit = {},
    onNavigateToGerenciarUsuarios: () -> Unit = {},
    onNavigateToConfiguracoes: () -> Unit = {},
    onNavigateToPoliticaPrivacidade: () -> Unit = {},

    onNavigateToChat: (String, String) -> Unit = { _, _ -> }, // destinatarioId, destinatarioNome
    openDrawerOnStart: Boolean = false
) {
    val state by viewModel.state.collectAsState()
    val pessoas by viewModel.pessoas.collectAsState()
    val parentescos by viewModel.parentescos.collectAsState()
    
    // ViewModel de Notificações
    val notificacaoViewModel: NotificacaoViewModel = hiltViewModel()
    val notificacoes by notificacaoViewModel.notificacoes.collectAsState()
    val contadorNaoLidas by notificacaoViewModel.contadorNaoLidas.collectAsState()
    
    // ViewModel de Chat para observar mensagens não lidas
    val chatViewModel: ChatViewModel = hiltViewModel()
    val mensagensNaoLidas by chatViewModel.mensagensNaoLidas.collectAsState()
    
    // ViewModel de Família para obter lista de famílias
    val familiaViewModel: FamiliaViewModel = hiltViewModel()
    val familiaState by familiaViewModel.state.collectAsState()
    
    // Estado de "Minha família"
    val minhaFamiliaId by viewModel.minhaFamiliaId.collectAsState()
    val minhaFamiliaNome by viewModel.minhaFamiliaNome.collectAsState()
    val mostrarModalMinhaFamilia by viewModel.mostrarModalMinhaFamilia.collectAsState()
    
    // Atualizar nome da "Minha família" quando as famílias mudarem
    LaunchedEffect(familiaState.familias, minhaFamiliaId) {
        viewModel.atualizarNomeMinhaFamilia(familiaState.familias)
    }
    
    // Estado local para busca expansível
    var mostrarBusca by remember { mutableStateOf(false) }
    var termoBusca by remember { mutableStateOf("") }
    
    // Estado local para dropdown de parentes do usuário
    var mostrarDropdownParentes by remember { mutableStateOf(false) }
    
    // Estado local para modal de notificações
    var mostrarModalNotificacoes by remember { mutableStateOf(false) }
    
    // Estado local para modal festivo de aniversário
    var mostrarModalAniversario by remember { mutableStateOf(false) }
    var notificacaoAniversario by remember { mutableStateOf<Notificacao?>(null) }
    var pessoaAniversarioNome by remember { mutableStateOf<String?>(null) }
    var filaAniversarios by remember { mutableStateOf<List<Pair<Notificacao, String>>>(emptyList()) }
    var indiceAniversarioAtual by remember { mutableIntStateOf(0) }
    
    // Estado local para modal de notificação ADMIN
    var mostrarModalAdminMensagem by remember { mutableStateOf(false) }
    var notificacaoAdminMensagem by remember { mutableStateOf<Notificacao?>(null) }
    
    // Estado local para modal de nova mensagem
    var mostrarModalNovaMensagem by remember { mutableStateOf(false) }
    var remetenteIdMensagem by remember { mutableStateOf<String?>(null) }
    var remetenteNomeMensagem by remember { mutableStateOf<String?>(null) }
    var quantidadeMensagens by remember { mutableIntStateOf(0) }
    val mensagensJaProcessadas = remember { mutableStateOf(mutableSetOf<String>()) } // IDs de remetentes já processados
    
    // CoroutineScope para operações assíncronas
    val scope = rememberCoroutineScope()
    
    // Verificar aniversários de hoje ao entrar na tela e quando pessoas carregarem
    LaunchedEffect(pessoas.isNotEmpty(), notificacoes) {
        // Aguardar um pouco após o login para garantir que tudo está carregado
        kotlinx.coroutines.delay(1000)
        
        // Verificar diretamente nas pessoas se há aniversários hoje
        val hoje = java.util.Calendar.getInstance()
        val diaHoje = hoje.get(java.util.Calendar.DAY_OF_MONTH)
        val mesHoje = hoje.get(java.util.Calendar.MONTH)
        
        // Buscar TODAS as pessoas que fazem aniversário hoje
        val aniversariantesHoje = pessoas.filter { pessoa ->
            pessoa.dataNascimento?.let { dataNasc ->
                val calNasc = java.util.Calendar.getInstance().apply {
                    time = dataNasc
                }
                val diaNasc = calNasc.get(java.util.Calendar.DAY_OF_MONTH)
                val mesNasc = calNasc.get(java.util.Calendar.MONTH)
                diaNasc == diaHoje && mesNasc == mesHoje
            } ?: false
        }
        
        // Buscar IDs de pessoas que já têm notificações de aniversário marcadas como lidas hoje
        val hojeInicio = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        
        val hojeFim = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }.time
        
        val aniversariosJaProcessados = notificacoes
            .filter { notificacao ->
                notificacao.tipo == TipoNotificacao.ANIVERSARIO &&
                notificacao.lida &&
                notificacao.criadaEm.after(hojeInicio) &&
                notificacao.criadaEm.before(hojeFim)
            }
            .mapNotNull { it.relacionadoId }
            .toSet()
        
        // Filtrar aniversariantes que ainda não foram processados
        val aniversariantesNaoProcessados = aniversariantesHoje.filter { aniversariante ->
            aniversariante.id !in aniversariosJaProcessados
        }
        
        // Criar fila de notificações apenas para aniversariantes não processados
        // e apenas se ainda não foi criada
        if (aniversariantesNaoProcessados.isNotEmpty() && filaAniversarios.isEmpty()) {
            val fila = aniversariantesNaoProcessados.map { aniversariante ->
                val idade = aniversariante.calcularIdade()
                val nomeExibicao = aniversariante.getNomeExibicao()
                
                val notificacao = Notificacao(
                    id = java.util.UUID.randomUUID().toString(),
                    tipo = TipoNotificacao.ANIVERSARIO,
                    titulo = "🎉 Feliz Aniversário!",
                    mensagem = when {
                        idade != null -> "Parabéns, $nomeExibicao! 🎉 Hoje você completa $idade anos!"
                        else -> "Parabéns, $nomeExibicao! 🎉 Que este dia seja especial!"
                    },
                    lida = false,
                    criadaEm = java.util.Date(),
                    relacionadoId = aniversariante.id,
                    dadosExtras = mapOf(
                        "pessoaId" to aniversariante.id,
                        "idade" to (idade?.toString() ?: "")
                    )
                )
                
                // Salvar notificação no banco de dados para que possa ser verificada depois
                scope.launch {
                    notificacaoViewModel.criarNotificacao(notificacao)
                }
                
                Pair(notificacao, nomeExibicao)
            }
            filaAniversarios = fila
            indiceAniversarioAtual = 0
        } else if (aniversariantesNaoProcessados.isEmpty() && filaAniversarios.isNotEmpty()) {
            // Se não há mais aniversariantes não processados, limpar a fila
            filaAniversarios = emptyList()
            indiceAniversarioAtual = 0
            notificacaoAniversario = null
            pessoaAniversarioNome = null
            mostrarModalAniversario = false
        }
    }
    
    // Verificar notificações ADMIN_MENSAGEM não lidas ao entrar na tela
    LaunchedEffect(notificacoes) {
        // Aguardar um pouco após o login para garantir que tudo está carregado
        kotlinx.coroutines.delay(1500)
        
        // Verificar se já há uma notificação ADMIN_MENSAGEM não lida
        if (!mostrarModalAdminMensagem && notificacaoAdminMensagem == null) {
            val adminMensagem = notificacaoViewModel.buscarAdminMensagemNaoLida()
            if (adminMensagem != null) {
                notificacaoAdminMensagem = adminMensagem
                mostrarModalAdminMensagem = true
            }
        }
    }
    
    // Observar mensagens não lidas e exibir modal quando houver novas
    // Detecta tanto no login quanto quando novas mensagens chegam em tempo real
    LaunchedEffect(mensagensNaoLidas) {
        // Verificar se há mensagens não lidas e se não há outros modais abertos
        if (!mostrarModalAdminMensagem && 
            !mostrarModalNovaMensagem && 
            !mostrarModalAniversario && 
            mensagensNaoLidas.isNotEmpty()) {
            
            // Encontrar o primeiro remetente com mensagens não lidas que ainda não foi processado
            val remetenteComMensagens = mensagensNaoLidas.entries.firstOrNull { (remetenteId, quantidade) ->
                quantidade > 0 && remetenteId !in mensagensJaProcessadas.value
            }
            
            if (remetenteComMensagens != null) {
                val (remetenteId, quantidade) = remetenteComMensagens
                
                // Aguardar um pouco para garantir que os usuários foram carregados (apenas na primeira vez)
                if (mensagensJaProcessadas.value.isEmpty()) {
                    kotlinx.coroutines.delay(2000) // Delay maior apenas no login
                } else {
                    kotlinx.coroutines.delay(300) // Delay menor para mensagens em tempo real
                }
                
                // Verificar novamente se ainda não há outros modais abertos (pode ter mudado durante o delay)
                if (!mostrarModalAdminMensagem && 
                    !mostrarModalNovaMensagem && 
                    !mostrarModalAniversario) {
                    
                    // Buscar nome do remetente usando o ChatViewModel que já tem a lista de usuários
                    val usuarios = chatViewModel.usuarios.value
                    val remetente = usuarios.firstOrNull { it.id == remetenteId }
                    val nomeRemetente = remetente?.nome ?: "Usuário"
                    
                    // Exibir modal
                    remetenteIdMensagem = remetenteId
                    remetenteNomeMensagem = nomeRemetente
                    quantidadeMensagens = quantidade
                    mostrarModalNovaMensagem = true
                    
                    // Marcar como processado para não exibir novamente
                    mensagensJaProcessadas.value.add(remetenteId)
                }
            }
        }
    }
    
    // Exibir próximo modal da fila quando não há modal aberto
    LaunchedEffect(filaAniversarios.isNotEmpty(), mostrarModalAniversario, indiceAniversarioAtual) {
        if (filaAniversarios.isNotEmpty() && 
            indiceAniversarioAtual < filaAniversarios.size && 
            !mostrarModalAniversario) {
            val (notificacao, nomePessoa) = filaAniversarios[indiceAniversarioAtual]
            notificacaoAniversario = notificacao
            pessoaAniversarioNome = nomePessoa
            mostrarModalAniversario = true
        }
    }
    
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
            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
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
    
    // Mostra erro se houver
    LaunchedEffect(state.erro) {
        state.erro?.let { erro ->
            snackbarHostState.showSnackbar(
                message = erro,
                duration = SnackbarDuration.Long
            )
            viewModel.limparErro()
        }
    }
    
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val themeController = LocalThemeController.current
    val isAdmin = state.usuario?.ehAdministrador == true
    val isAdminSenior = state.usuario?.ehAdministradorSenior == true
    
    // Contador de pedidos pendentes para badge na sidebar (admins)
    val pedidosPendentes by viewModel.pedidosPendentes.collectAsState()
    LaunchedEffect(isAdmin || isAdminSenior) {
        viewModel.atualizarPedidosPendentes()
    }

    // Abrir drawer automaticamente se solicitado (ex: ao voltar de uma tela do menu)
    LaunchedEffect(openDrawerOnStart) {
        if (openDrawerOnStart) {
            drawerState.open()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                isAdmin = isAdmin,
                isAdminSenior = isAdminSenior,
                notificacoesNaoLidas = contadorNaoLidas,
                pedidosPendentes = pedidosPendentes,
                onClose = { scope.launch { drawerState.close() } },
                onOpenNotificacoes = {
                    scope.launch {
                        drawerState.close()
                    }
                    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                    mostrarModalNotificacoes = true
                },
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
                onNavigateToPoliticaPrivacidade = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToPoliticaPrivacidade()
                    }
                },
                onSair = {
                    scope.launch {
                        drawerState.close()
                        viewModel.logout()
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
        topBar = {
            TopAppBar(
                title = { Text("Raízes Vivas") },
                windowInsets = WindowInsets(0.dp),
                actions = {
                    // Botão de Conquistas
                    IconButton(onClick = { onNavigateToConquistas() }) {
                        Icon(Icons.Default.Star, contentDescription = "Conquistas")
                    }
                    // Botão de notificações
                    Box {
                        IconButton(onClick = { 
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            mostrarModalNotificacoes = true
                        }) {
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
                    .padding(8.dp)
            ) {
                // 1. Card Família Zero - Estilo Neon
                FamiliaZeroCard(
                    familiaZeroNome = state.familiaZeroNome,
                    familiaZeroExiste = state.familiaZeroExiste,
                    paiNome = state.familiaZeroPaiNome,
                    maeNome = state.familiaZeroMaeNome,
                    ehAdministrador = isAdminSenior, // Apenas ADMIN SR pode alterar
                    onEditarNome = { viewModel.abrirModalEditarNome() },
                    onAbrirModal = { viewModel.abrirModalFamiliaZero() }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 2. Card "Minha família" - Estilo Neon
                MinhaFamiliaCard(
                    familiaNome = minhaFamiliaNome,
                    onClick = { viewModel.abrirModalMinhaFamilia() }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 3. Card do usuário vinculado (parentesco) - Estilo Neon
                pessoaVinculada?.let { pessoa ->
                    UsuarioCard(
                        pessoa = pessoa,
                        totalParentes = parentescos.size,
                        mostrarDropdown = mostrarDropdownParentes,
                        onToggleDropdown = { mostrarDropdownParentes = !mostrarDropdownParentes },
                        onDismissDropdown = { mostrarDropdownParentes = false },
                        parentescos = parentescos,
                        onNavigateToDetalhes = onNavigateToDetalhesPessoa
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Título da seção de estatísticas
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Família em números",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 3. Demais cards de estatísticas - "Paredão de cards"
                
                // Primeira linha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Pessoas",
                        value = state.totalPessoas.toString(),
                        painter = painterResource(id = com.raizesvivas.app.R.drawable.pessoas),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Card de Famílias - Estilo Neon
                    StatCard(
                        title = "Famílias",
                        value = state.totalFamilias.toString(),
                        painter = painterResource(id = com.raizesvivas.app.R.drawable.familia),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Segunda linha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Meninas",
                        value = state.meninas.toString(),
                        painter = painterResource(id = com.raizesvivas.app.R.drawable.menina),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Meninos",
                        value = state.meninos.toString(),
                        painter = painterResource(id = com.raizesvivas.app.R.drawable.menino),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Outros",
                        value = state.outros.toString(),
                        painter = painterResource(id = com.raizesvivas.app.R.drawable.outros),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Terceira linha (Ranking e Sobrinhos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = if (state.posicaoGrupo.isNotBlank()) state.posicaoGrupo else "Posição",
                        value = if (state.posicaoRanking > 0) "#${state.posicaoRanking}" else "-",
                        painter = painterResource(id = com.raizesvivas.app.R.drawable.posicao),
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (state.totalSobrinhos > 0) {
                        StatCard(
                            title = "Sobrinhos",
                            value = state.totalSobrinhos.toString(),
                            painter = painterResource(id = com.raizesvivas.app.R.drawable.sobrinhos),
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
                    onConfirmar = { novoNome: String ->
                        viewModel.atualizarNomeFamiliaZero(novoNome)
                    },
                    isLoading = state.isLoading
                )
            }
            
            // Modal para selecionar "Minha família"
            if (mostrarModalMinhaFamilia) {
                ModalSelecionarMinhaFamilia(
                    familias = familiaState.familias,
                    familiaSelecionadaId = minhaFamiliaId,
                    onDismiss = { viewModel.fecharModalMinhaFamilia() },
                    onSelecionar = { familiaId, familiaNome ->
                        viewModel.definirMinhaFamilia(familiaId, familiaNome)
                        viewModel.fecharModalMinhaFamilia()
                    },
                    onRemover = {
                        viewModel.removerMinhaFamilia()
                        viewModel.fecharModalMinhaFamilia()
                    }
                )
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        }
        
        if (mostrarModalNotificacoes) {
            NotificacoesModal(
                notificacoes = notificacoes,
                viewModel = notificacaoViewModel,
                onDismiss = { 
                    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                    mostrarModalNotificacoes = false
                },
                onNotificacaoClick = { notificacao ->
                    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                    mostrarModalNotificacoes = false
                    when (notificacao.tipo) {
                        TipoNotificacao.CONQUISTA_DESBLOQUEADA -> {
                            // Futuras navegações específicas podem ser adicionadas aqui
                        }
                        else -> Unit
                    }
                }
            )
        }
        
        // Modal festivo de aniversário
        notificacaoAniversario?.let { notificacao ->
            if (mostrarModalAniversario) {
                ModalFestivoAniversario(
                    notificacao = notificacao,
                    pessoaNome = pessoaAniversarioNome,
                    onEnviarMensagem = {
                        // IMPORTANTE: Capturar os valores da notificação atual ANTES de avançar o índice
                        val notificacaoAtual = notificacao
                        val pessoaIdAtual = notificacao.relacionadoId
                        val pessoaNomeAtual = pessoaAniversarioNome
                        val indiceAtual = indiceAniversarioAtual
                        
                        mostrarModalAniversario = false
                        
                        // Marcar como lida e navegar usando os valores capturados
                        scope.launch {
                            notificacaoViewModel.marcarComoLida(notificacaoAtual)
                            
                            // Verificar se o aniversariante é usuário da app usando os valores capturados
                            if (pessoaIdAtual != null) {
                                val usuario = viewModel.buscarUsuarioPorPessoaId(pessoaIdAtual)
                                if (usuario != null) {
                                    // Se for usuário, navegar para o chat usando os valores capturados
                                    val nome = pessoaNomeAtual ?: usuario.nome
                                    onNavigateToChat(usuario.id, nome)
                                } else {
                                    // Se não for usuário, navegar para detalhes da pessoa usando o ID capturado
                                    onNavigateToDetalhesPessoa(pessoaIdAtual)
                                }
                            }
                        }
                        
                        // Avançar para próximo aniversário da fila APÓS capturar os valores
                        if (indiceAtual < filaAniversarios.size - 1) {
                            indiceAniversarioAtual = indiceAtual + 1
                        } else {
                            // Limpar fila quando terminar
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            filaAniversarios = emptyList()
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            indiceAniversarioAtual = 0
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            notificacaoAniversario = null
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            pessoaAniversarioNome = null
                        }
                    },
                    onIgnorar = {
                        mostrarModalAniversario = false
                        // Marcar como lida ao ignorar
                        scope.launch {
                            notificacaoViewModel.marcarComoLida(notificacao)
                        }
                        
                        // Avançar para próximo aniversário da fila
                        if (indiceAniversarioAtual < filaAniversarios.size - 1) {
                            indiceAniversarioAtual++
                        } else {
                            // Limpar fila quando terminar
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            filaAniversarios = emptyList()
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            indiceAniversarioAtual = 0
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            notificacaoAniversario = null
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            pessoaAniversarioNome = null
                        }
                    },
                    onDismiss = {
                        mostrarModalAniversario = false
                        
                        // Avançar para próximo aniversário da fila
                        if (indiceAniversarioAtual < filaAniversarios.size - 1) {
                            indiceAniversarioAtual++
                        } else {
                            // Limpar fila quando terminar
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            filaAniversarios = emptyList()
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            indiceAniversarioAtual = 0
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            notificacaoAniversario = null
                            @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                            pessoaAniversarioNome = null
                        }
                    }
                )
            }
        }
        
        // Modal de notificação ADMIN_MENSAGEM
        notificacaoAdminMensagem?.let { notificacao ->
            if (mostrarModalAdminMensagem) {
                ModalNotificacaoAdmin(
                    notificacao = notificacao,
                    onMarcarComoLida = {
                        scope.launch {
                            notificacaoViewModel.marcarComoLida(notificacao)
                            mostrarModalAdminMensagem = false
                            notificacaoAdminMensagem = null
                        }
                    },
                    onDownloadClicked = { notif ->
                        notificacaoViewModel.registrarCliqueDownloadAtualizacao(notif)
                    }
                )
            }
        }
        
        // Modal de nova mensagem no chat
        if (mostrarModalNovaMensagem && remetenteIdMensagem != null && remetenteNomeMensagem != null) {
            ModalNovaMensagem(
                remetenteNome = remetenteNomeMensagem!!,
                quantidadeMensagens = quantidadeMensagens,
                onAbrirChat = {
                    mostrarModalNovaMensagem = false
                    val remetenteId = remetenteIdMensagem
                    val remetenteNome = remetenteNomeMensagem
                    if (remetenteId != null && remetenteNome != null) {
                        onNavigateToChat(remetenteId, remetenteNome)
                    }
                },
                onIgnorar = {
                    mostrarModalNovaMensagem = false
                    // Não remover de mensagensJaProcessadas para não exibir novamente
                }
            )
        }
    }
}

// Cards específicos no estilo Neon

/**
 * Card Família Zero - Estilo Neon
 */
@Composable
fun FamiliaZeroCard(
    familiaZeroNome: String?,
    familiaZeroExiste: Boolean,
    paiNome: String?,
    maeNome: String?,
    ehAdministrador: Boolean,
    onEditarNome: () -> Unit,
    onAbrirModal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = "Família Zero",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Nome da família e informações
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = familiaZeroNome?.uppercase() ?: if (familiaZeroExiste) "CRIADA" else "PENDENTE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (familiaZeroExiste && paiNome != null && maeNome != null) {
                    Text(
                        text = "$paiNome & $maeNome",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (familiaZeroExiste) {
                    Text(
                        text = "Casal fundador",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Toque para definir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Botões de ação - Layout vertical
            if (ehAdministrador) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (familiaZeroExiste) {
                        IconButton(
                            onClick = onEditarNome,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar nome",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onAbrirModal,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card Minha Família - Estilo Neon
 */
@Composable
fun MinhaFamiliaCard(
    familiaNome: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Minha família",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Nome da família
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = familiaNome?.uppercase() ?: "MINHA FAMÍLIA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (familiaNome != null) "Minha família selecionada" else "Toque para selecionar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Ícone de seta
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Abrir",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Card Usuário/Parentesco - Estilo Neon
 */
@Composable
fun UsuarioCard(
    pessoa: Pessoa,
    totalParentes: Int,
    mostrarDropdown: Boolean,
    onToggleDropdown: () -> Unit,
    onDismissDropdown: () -> Unit,
    parentescos: List<Pair<Pessoa, ParentescoCalculator.ResultadoParentesco>>,
    onNavigateToDetalhes: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clickable { onToggleDropdown() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                ) {
                    if (pessoa.fotoUrl != null && pessoa.fotoUrl.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(pessoa.fotoUrl)
                                    .build()
                            ),
                            contentDescription = "Foto de ${pessoa.nome}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                // Informações do usuário
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = pessoa.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Você • $totalParentes parentes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Ícone de seta (agora sempre ChevronRight pois abre modal)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver parentes",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Modal de parentes (substituindo Dropdown)
            if (mostrarDropdown) {
                ModalListaParentes(
                    parentescos = parentescos,
                    onDismiss = onDismissDropdown,
                    onNavigateToDetalhes = { id ->
                        onDismissDropdown()
                        onNavigateToDetalhes(id)
                    },
                    onVerMeuPerfil = {
                        onDismissDropdown()
                        onNavigateToDetalhes(pessoa.id)
                    }
                )
            }
        }
    }
}

@Composable
fun ModalListaParentes(
    parentescos: List<Pair<Pessoa, ParentescoCalculator.ResultadoParentesco>>,
    onDismiss: () -> Unit,
    onNavigateToDetalhes: (String) -> Unit,
    onVerMeuPerfil: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FamilyRestroom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Meus Parentes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (parentescos.isEmpty()) {
                    Text(
                        text = "Nenhum parente encontrado na árvore.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(parentescos, key = { it.first.id }) { pair ->
                            val (parente, resultadoParentesco) = pair
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToDetalhes(parente.id) },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Avatar menor
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        if (parente.fotoUrl != null && parente.fotoUrl.isNotBlank()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(
                                                    ImageRequest.Builder(LocalContext.current)
                                                        .data(parente.fotoUrl)
                                                        .build()
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = parente.nome,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = resultadoParentesco.parentesco,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
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
                onClick = onVerMeuPerfil
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver meu perfil")
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
 * Card de Estatística - Estilo Neon (branco)
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    painter: Painter? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                painter != null -> {
                    // Para imagens drawable (PNG), não aplicar tint para preservar cores originais
                    Icon(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
                icon != null -> {
                    // Para ImageVector, usar cor primária
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@Suppress("UNUSED")
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
fun ModalEditarNomeFamiliaZero(
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
                
                RaizesVivasTextField(
                    value = nomeEditado,
                    onValueChange = { nomeEditado = it },
                    label = "Nome da Família",
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

/**
 * Modal para selecionar "Minha família"
 */
@Composable
fun ModalSelecionarMinhaFamilia(
    familias: List<com.raizesvivas.app.presentation.screens.familia.FamiliaUiModel>,
    familiaSelecionadaId: String?,
    onDismiss: () -> Unit,
    onSelecionar: (String, String) -> Unit,
    onRemover: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Selecionar Minha Família",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (familias.isEmpty()) {
                    Text(
                        text = "Nenhuma família disponível",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(familias, key = { it.id }) { familia ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelecionar(familia.id, familia.nomeExibicao)
                                    },
                                color = if (familia.id == familiaSelecionadaId) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = familia.nomeExibicao,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (familia.id == familiaSelecionadaId) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        if (familia.conjuguePrincipal != null || familia.conjugueSecundario != null) {
                                            Text(
                                                text = buildString {
                                                    familia.conjuguePrincipal?.let { append(it.nome) }
                                                    if (familia.conjuguePrincipal != null && familia.conjugueSecundario != null) {
                                                        append(" & ")
                                                    }
                                                    familia.conjugueSecundario?.let { append(it.nome) }
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (familia.id == familiaSelecionadaId) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                }
                                            )
                                        }
                                    }
                                    if (familia.id == familiaSelecionadaId) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selecionada",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
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
            if (familiaSelecionadaId != null) {
                Button(
                    onClick = onRemover
                ) {
                    Text("Remover seleção")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun HomeDrawerContent(
    isAdmin: Boolean,
    isAdminSenior: Boolean = false,
    notificacoesNaoLidas: Int,
    pedidosPendentes: Int,
    onClose: () -> Unit,
    onOpenNotificacoes: () -> Unit,
    onNavigateToPerfil: () -> Unit = {},
    onGerenciarConvites: () -> Unit,
    onGerenciarEdicoes: () -> Unit,
    onResolverDuplicatas: () -> Unit,
    onGerenciarUsuarios: () -> Unit,
    onConfiguracoes: () -> Unit,
    onNavigateToPoliticaPrivacidade: () -> Unit,
    onSair: () -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.widthIn(min = 280.dp, max = 360.dp)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
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

            NavigationDrawerItem(
                label = { Text("Perfil") },
                selected = false,
                onClick = onNavigateToPerfil,
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("Contrato de Privacidade") },
                selected = false,
                onClick = onNavigateToPoliticaPrivacidade,
                icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            DrawerSectionTitle("Ações rápidas")

            if (isAdmin || isAdminSenior) {
                NavigationDrawerItem(
                    label = { Text("Gerenciar convites") },
                    selected = false,
                    onClick = onGerenciarConvites,
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    badge = {
                        if (pedidosPendentes > 0) {
                            Badge { Text(if (pedidosPendentes > 99) "99+" else pedidosPendentes.toString()) }
                        }
                    },
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
                
                NavigationDrawerItem(
                    label = { Text("Gerenciar usuários") },
                    selected = false,
                    onClick = onGerenciarUsuarios,
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
            
            // Configurações - apenas ADMIN SÊNIOR
            if (isAdminSenior) {
                NavigationDrawerItem(
                    label = { Text("Configurações") },
                    selected = false,
                    onClick = onConfiguracoes,
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

            DrawerSectionTitle("Tema")

            ThemeSelector(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

            SobreSection(
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
                modifier = Modifier.padding(horizontal = 20.dp)
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
fun DrawerSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
fun ThemeSelector(
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
fun ThemeOptionChip(
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
        // Sem bordas - estilo Neon
        border = null
    )
}

@Composable
private fun SobreSection(
    versionName: String,
    versionCode: Int,
    modifier: Modifier = Modifier
) {
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sobre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Versão do app: $versionName ($versionCode)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Dev: Rob Gomez",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Copyright © $currentYear Raízes Vivas. Todos os direitos reservados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Construído para preservar histórias e fortalecer conexões familiares.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

