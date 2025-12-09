package com.raizesvivas.app.presentation.screens.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.raizesvivas.app.domain.model.FotoAlbum
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.screens.cadastro.PessoaSelector
import com.raizesvivas.app.presentation.screens.album.AvatarUsuario
import com.raizesvivas.app.presentation.viewmodel.GamificacaoViewModel
import com.raizesvivas.app.presentation.viewmodel.ConquistaComProgresso
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import timber.log.Timber
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material.icons.filled.MoreVert
import com.raizesvivas.app.presentation.screens.home.HomeDrawerContent
import com.raizesvivas.app.presentation.theme.LocalThemeController
import com.raizesvivas.app.presentation.theme.ThemeMode
import com.raizesvivas.app.presentation.viewmodel.NotificacaoViewModel
import kotlinx.coroutines.launch

/**
 * Tela de Perfil do usuário
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = hiltViewModel(),
    gamificacaoViewModel: GamificacaoViewModel = hiltViewModel(),
    onNavigateToCadastroPessoaComId: (String?) -> Unit = {},
    onNavigateToEditar: (String) -> Unit = {},
    onNavigateToGerenciarConvites: () -> Unit = {},
    onNavigateToGerenciarEdicoes: () -> Unit = {},
    onNavigateToResolverDuplicatas: () -> Unit = {},
    onNavigateToGerenciarUsuarios: () -> Unit = {},
    onNavigateToConfiguracoes: () -> Unit = {},
    onNavigateToFotoAlbum: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val pessoasDisponiveis by viewModel.pessoasDisponiveis.collectAsState()
    
    // Dados de gamificação
    val perfilGamificacao by gamificacaoViewModel.perfil.collectAsState()
    val conquistasComProgresso by gamificacaoViewModel.conquistasComProgresso.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var ultimaVinculacaoId by remember { mutableStateOf<String?>(null) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    
    // Estado para modal de confirmação de vinculação
    var pessoaParaVincular by remember { mutableStateOf<Pessoa?>(null) }
    
    // Fechar modal quando vinculação for bem-sucedida
    LaunchedEffect(state.pessoaVinculadaId, state.isLoading, pessoaParaVincular?.id) {
        val pessoaSelecionadaId = pessoaParaVincular?.id
        if (!state.isLoading && 
            pessoaSelecionadaId != null && 
            state.pessoaVinculadaId == pessoaSelecionadaId) {
            // Vinculação bem-sucedida, fechar modal
            pessoaParaVincular = null
        }
    }
    
    // Fechar modal quando houver erro (será mostrado via snackbar)
    LaunchedEffect(state.erro) {
        if (state.erro != null && pessoaParaVincular != null) {
            // Não fechar modal imediatamente, deixar usuário ver o erro
            // O modal só fecha se o usuário clicar em Cancelar ou se a vinculação for bem-sucedida
        }
    }
    
    // Mostrar mensagens de erro
    LaunchedEffect(state.erro) {
        state.erro?.let { erro ->
            snackbarHostState.showSnackbar(
                message = erro,
                duration = SnackbarDuration.Long
            )
        }
    }
    
    // Mostrar mensagem de sucesso quando vinculação for concluída
    LaunchedEffect(state.pessoaVinculadaId, state.isLoading) {
        if (!state.isLoading && state.pessoaVinculadaId != null && state.pessoaVinculadaId != ultimaVinculacaoId) {
            ultimaVinculacaoId = state.pessoaVinculadaId
            snackbarHostState.showSnackbar(
                message = "Vinculação realizada com sucesso!",
                duration = SnackbarDuration.Short
            )
        } else if (!state.isLoading && state.pessoaVinculadaId == null && ultimaVinculacaoId != null) {
            ultimaVinculacaoId = null
            snackbarHostState.showSnackbar(
                message = "Desvinculação realizada com sucesso!",
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Notification ViewModel for drawer
    val notificacaoViewModel: NotificacaoViewModel = hiltViewModel()
    val contadorNaoLidas by notificacaoViewModel.contadorNaoLidas.collectAsState()
    
    // Drawer state
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val themeController = LocalThemeController.current
    val isAdmin = state.ehAdmin
    val isAdminSenior = false // PerfilScreen doesn't have admin senior info
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                isAdmin = isAdmin,
                isAdminSenior = isAdminSenior,
                notificacoesNaoLidas = contadorNaoLidas,
                pedidosPendentes = 0,
                onClose = { scope.launch { drawerState.close() } },
                onOpenNotificacoes = { scope.launch { drawerState.close() } },
                onNavigateToPerfil = { scope.launch { drawerState.close() } },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil") },
                windowInsets = WindowInsets(0.dp),
                actions = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Abrir menu lateral")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Conteúdo scrollável
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Header com foto, nome e email (estilo DetalhesPessoa)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar/Foto - usar foto da pessoa vinculada se houver, senão foto do usuário
                    val fotoParaExibir = state.pessoaVinculada?.fotoUrl ?: state.fotoUrl
                    val nomeParaExibir = state.pessoaVinculada?.nome ?: state.nome ?: "Usuário"
                    
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            fotoParaExibir?.let { fotoUrl ->
                                if (fotoUrl.isNotBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            ImageRequest.Builder(LocalContext.current)
                                                .data(fotoUrl)
                                                .crossfade(true)
                                                .build()
                                        ),
                                        contentDescription = "Foto de $nomeParaExibir",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } ?: run {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = nomeParaExibir,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    state.pessoaVinculada?.apelido?.takeIf { it.isNotBlank() }?.let { apelido ->
                        Text(
                            text = apelido,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                    
                    // Email do usuário
                    state.email?.let { email ->
                        if (email.isNotBlank()) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    if (state.pessoaVinculada?.ehFamiliaZero == true) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(
                                text = "Família Zero",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Badge de status (Admin ou Familiar)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.ehAdmin) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.ehAdmin) {
                                Icons.Default.AdminPanelSettings
                            } else {
                                Icons.Default.Person
                            },
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (state.ehAdmin) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (state.ehAdmin) {
                                    "FAMILIAR ADMIN"
                                } else {
                                    "FAMILIAR"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (state.ehAdmin) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                            if (state.ehAdmin) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Administrador da família",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Membro da família",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Seção de Conquistas (Expansível)
                SecaoConquistasExpansivel(
                    perfilGamificacao = perfilGamificacao,
                    conquistasComProgresso = conquistasComProgresso,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Card de vinculação com pessoa
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Vinculação com Familiar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (state.pessoaVinculadaNome != null) {
                                        "Vinculado a: ${state.pessoaVinculadaNome}"
                                    } else {
                                        "Nenhum familiar vinculado"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Seletor de pessoa
                        Box(modifier = Modifier.fillMaxWidth()) {
                            PessoaSelector(
                                label = "Selecionar familiar",
                                pessoaId = state.pessoaVinculadaId,
                                pessoasDisponiveis = pessoasDisponiveis,
                                onPessoaSelecionada = { pessoa ->
                                    // Ao selecionar, mostrar modal de confirmação
                                    if (pessoa != null) {
                                        pessoaParaVincular = pessoa
                                    }
                                },
                                mostrarAdicionarNovo = true,
                                onAdicionarNovo = { onNavigateToCadastroPessoaComId(null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // Indicador de loading
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 16.dp)
                                        .size(20.dp)
                                )
                            }
                        }
                        
                        if (state.pessoaVinculadaId != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            TextButton(
                                onClick = { viewModel.vincularPessoa(null) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Desvincular")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.LinkOff, contentDescription = null)
                            }
                        }
                    }
                }
                


                Spacer(modifier = Modifier.height(16.dp))

                // Modal de confirmação de vinculação (fora do scroll, mas dentro do Column principal)
                pessoaParaVincular?.let { pessoa ->
                    AlertDialog(
                        onDismissRequest = { pessoaParaVincular = null },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Confirmar Vinculação",
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
                                Text(
                                    text = "Deseja vincular sua conta ao familiar:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            AvatarUsuario(
                                                fotoUrl = pessoa.fotoUrl,
                                                nome = pessoa.nome,
                                                size = 40
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = pessoa.nome,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                pessoa.dataNascimento?.let {
                                                    val idade = pessoa.calcularIdade()
                                                    if (idade != null) {
                                                        Text(
                                                            text = "${idade} anos",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Text(
                                    text = "Esta vinculação permitirá que você veja seus parentescos com outros familiares cadastrados.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.vincularPessoa(pessoa.id)
                                    // Não fechar modal imediatamente - aguardar resultado
                                },
                                enabled = !state.isLoading
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Vincular")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { pessoaParaVincular = null },
                                enabled = !state.isLoading
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
                
                // Seção de Administração removida - usar "Gerenciar Usuários" na sidebar
                // A funcionalidade de gerenciar administradores foi movida para a tela dedicada
                
                // Detalhes da Pessoa Vinculada (se houver)
                state.pessoaVinculada?.let { pessoa ->
                    // Botão para editar pessoa
                    if (state.pessoaVinculadaId != null) {
                        OutlinedButton(
                            onClick = { onNavigateToEditar(state.pessoaVinculadaId!!) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar Familiar")
                        }
                    }
                    
                    // Informações Básicas
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Informações Básicas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            HorizontalDivider()
                            
                            InfoRow(
                                label = "Nome",
                                value = pessoa.nome,
                                icon = Icons.Default.Person
                            )

                            pessoa.apelido?.let { apelido ->
                                if (apelido.isNotBlank()) {
                                    InfoRow(
                                        label = "Apelido",
                                        value = apelido,
                                        icon = Icons.Default.Star
                                    )
                                }
                            }
                            
                            if (pessoa.genero != null) {
                                InfoRow(
                                    label = "Gênero",
                                    value = pessoa.genero.label,
                                    icon = Icons.Default.Person
                                )
                            }
                            
                            if (pessoa.estadoCivil != null) {
                                InfoRow(
                                    label = "Estado Civil",
                                    value = pessoa.estadoCivil.label,
                                    icon = Icons.Default.Favorite
                                )
                            }
                            
                            if (pessoa.dataNascimento != null) {
                                InfoRow(
                                    label = "Data de Nascimento",
                                    value = dateFormatter.format(pessoa.dataNascimento),
                                    icon = Icons.Default.Cake
                                )
                            }
                            
                            if (pessoa.dataFalecimento != null) {
                                InfoRow(
                                    label = "Data de Falecimento",
                                    value = dateFormatter.format(pessoa.dataFalecimento),
                                    icon = Icons.Default.Event
                                )
                            }
                            
                            pessoa.localNascimento?.let { local ->
                                if (local.isNotBlank()) {
                                    InfoRow(
                                        label = "Local de Nascimento",
                                        value = local,
                                        icon = Icons.Default.LocationOn
                                    )
                                }
                            }
                            
                            pessoa.localResidencia?.let { local ->
                                if (local.isNotBlank()) {
                                    InfoRow(
                                        label = "Local de Residência",
                                        value = local,
                                        icon = Icons.Default.Home
                                    )
                                }
                            }
                            
                            pessoa.profissao?.let { profissao ->
                                if (profissao.isNotBlank()) {
                                    InfoRow(
                                        label = "Profissão",
                                        value = profissao,
                                        icon = Icons.Default.Work
                                    )
                                }
                            }
                            
                            pessoa.telefone?.let { telefone ->
                                if (telefone.isNotBlank()) {
                                    InfoRow(
                                        label = "Telefone/Celular",
                                        value = telefone,
                                        icon = Icons.Default.Phone
                                    )
                                }
                            }
                        }
                    }
                    
                    // Biografia
                    pessoa.biografia?.let { biografia ->
                        if (biografia.isNotBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Biografia",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    HorizontalDivider()
                                    
                                    Text(
                                        text = biografia,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    
                    // Relacionamentos
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Relacionamentos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            HorizontalDivider()
                            
                            // Pai
                            if (pessoa.pai != null) {
                                val paiNome = state.paiNome ?: "Carregando..."
                                InfoRow(
                                    label = "Pai",
                                    value = paiNome,
                                    icon = Icons.Default.Man
                                )
                            }
                            
                            // Mãe
                            if (pessoa.mae != null) {
                                val maeNome = state.maeNome ?: "Carregando..."
                                InfoRow(
                                    label = "Mãe",
                                    value = maeNome,
                                    icon = Icons.Default.Woman
                                )
                            }
                            
                            // Cônjuge
                            if (pessoa.conjugeAtual != null) {
                                val conjugeNome = state.conjugeNome ?: "Carregando..."
                                InfoRow(
                                    label = "Cônjuge",
                                    value = conjugeNome,
                                    icon = Icons.Default.Favorite
                                )
                            }
                            
                            // Filhos
                            if (pessoa.filhos.isNotEmpty() || state.filhosNomes.isNotEmpty()) {
                                val filhosTexto = if (state.filhosNomes.isNotEmpty()) {
                                    state.filhosNomes.joinToString(", ")
                                } else {
                                    "${pessoa.filhos.size} filho(s)"
                                }
                                InfoRow(
                                    label = "Filhos",
                                    value = filhosTexto,
                                    icon = Icons.Default.ChildCare
                                )
                            }
                        }
                    }
                    
                    // Fotos do Álbum
                    if (state.fotosAlbum.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Fotos do Álbum",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                HorizontalDivider()
                                
                                // Grid de fotos (3 colunas)
                                val fotosChunked = remember(state.fotosAlbum) {
                                    state.fotosAlbum.chunked(3)
                                }
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    fotosChunked.forEach { rowFotos ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            rowFotos.forEach { foto ->
                                                FotoAlbumItem(
                                                    foto = foto,
                                                    modifier = Modifier.weight(1f),
                                                    onClick = { onNavigateToFotoAlbum(foto.id) }
                                                )
                                            }
                                            // Espaçador se linha incompleta
                                            repeat(3 - rowFotos.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Ações
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
    }
}

/**
 * Linha de informação com ícone
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Item de foto do álbum
 */
@Composable
private fun FotoAlbumItem(
    foto: FotoAlbum,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(foto.url)
                        .build()
                ),
                contentDescription = foto.descricao.ifBlank { "Foto de ${foto.pessoaNome}" },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Overlay com descrição se houver
            if (foto.descricao.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    androidx.compose.ui.graphics.Color.Transparent,
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = foto.descricao,
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Seção expansível de Conquistas
 */
@Composable
private fun SecaoConquistasExpansivel(
    perfilGamificacao: com.raizesvivas.app.domain.model.PerfilGamificacao?,
    conquistasComProgresso: List<ConquistaComProgresso>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val perfilAtual = perfilGamificacao ?: com.raizesvivas.app.domain.model.PerfilGamificacao(
        usuarioId = "",
        nivel = 1,
        xpAtual = 0,
        xpProximoNivel = 500,
        conquistasDesbloqueadas = 0,
        totalConquistas = com.raizesvivas.app.domain.model.SistemaConquistas.obterTodas().size
    )
    
    // Filtrar apenas conquistas concluídas
    val conquistasConcluidas = remember(conquistasComProgresso) {
        conquistasComProgresso.filter { it.progresso.concluida }
    }
    
    // Calcular progresso para o próximo nível
    val progresso = if (perfilAtual.xpProximoNivel > 0) {
        perfilAtual.xpAtual.toFloat() / perfilAtual.xpProximoNivel.toFloat()
    } else {
        0f
    }
    
    val progressoAnimado by animateFloatAsState(
        targetValue = progresso,
        animationSpec = tween(1000),
        label = "progresso"
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header clicável
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Conquistas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Nível ${perfilAtual.nivel} • ${perfilAtual.conquistasDesbloqueadas}/${perfilAtual.totalConquistas} conquistas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Conteúdo expansível
            androidx.compose.animation.AnimatedVisibility(
                visible = isExpanded,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    
                    // Informações do Nível
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Nível ${perfilAtual.nivel}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = obterBrasaoNivel(perfilAtual.nivel),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${perfilAtual.xpAtual} / ${perfilAtual.xpProximoNivel} XP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Próximo: Nível ${perfilAtual.nivel + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // Barra de Progresso
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progressoAnimado },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(progressoAnimado * 100).toInt()}% completo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Faltam ${perfilAtual.xpProximoNivel - perfilAtual.xpAtual} XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    
                    // Lista de Conquistas (apenas concluídas)
                    if (conquistasConcluidas.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🌱",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Text(
                                    text = "Ainda não há conquistas concluídas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Continue usando o app para desbloquear conquistas!",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            conquistasConcluidas.forEach { conquistaComProgresso ->
                                ConquistaItemCard(conquistaComProgresso = conquistaComProgresso)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de Conquista Individual (versão compacta para perfil)
 */
@Composable
private fun ConquistaItemCard(
    conquistaComProgresso: ConquistaComProgresso
) {
    val conquista = conquistaComProgresso.conquista
    val progresso = conquistaComProgresso.progresso
    
    val progressoPercentual = if (progresso.progressoTotal > 0) {
        progresso.progresso.toFloat() / progresso.progressoTotal.toFloat()
    } else {
        0f
    }
    
    val progressoAnimado by animateFloatAsState(
        targetValue = progressoPercentual,
        animationSpec = tween(500),
        label = "progresso_conquista"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (progresso.concluida) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (progresso.concluida) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone da Conquista
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (progresso.concluida) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conquista.icone ?: "🏆",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            // Conteúdo
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conquista.nome,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (progresso.concluida) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                    
                    if (progresso.concluida) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = conquista.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                
                // Barra de Progresso
                if (!progresso.concluida) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progressoAnimado },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${progresso.progresso} / ${progresso.progressoTotal}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        text = "Concluída • +${conquista.recompensaXP} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Retorna o brasão/emoji correspondente ao nível
 */
private fun obterBrasaoNivel(nivel: Int): String {
    return when {
        nivel >= 50 -> "👑"
        nivel >= 40 -> "🏆"
        nivel >= 30 -> "⭐"
        nivel >= 20 -> "🌟"
        nivel >= 10 -> "🎖️"
        nivel >= 5 -> "🎯"
        nivel >= 3 -> "📜"
        nivel >= 2 -> "🌱"
        else -> "🌿"
    }
}
