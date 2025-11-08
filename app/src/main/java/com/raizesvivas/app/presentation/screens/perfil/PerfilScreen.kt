package com.raizesvivas.app.presentation.screens.perfil

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.screens.cadastro.PessoaSelector
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Tela de Perfil do usuário
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = hiltViewModel(),
    onNavigateToCadastroPessoa: () -> Unit = {},
    onNavigateToFamiliaZero: () -> Unit = {},
    onNavigateToAceitarConvites: () -> Unit = {},
    onNavigateToGerenciarConvites: () -> Unit = {},
    onNavigateToGerenciarEdicoes: () -> Unit = {},
    onNavigateToResolverDuplicatas: () -> Unit = {},
    onNavigateToCadastroPessoaComId: (String?) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val pessoasDisponiveis by viewModel.pessoasDisponiveis.collectAsState()
    
    // Obter currentUserId do ViewModel
    val currentUserId = remember { viewModel.getCurrentUserId() }
    
    val snackbarHostState = remember { SnackbarHostState() }
    var ultimaVinculacaoId by remember { mutableStateOf<String?>(null) }
    
    // Estado para modal de confirmação de vinculação
    var pessoaParaVincular by remember { mutableStateOf<Pessoa?>(null) }
    
    // Estado para modal de confirmação de promoção/rebaixamento de admin
    var usuarioParaPromover by remember { mutableStateOf<com.raizesvivas.app.domain.model.Usuario?>(null) }
    var promoverAdmin by remember { mutableStateOf<Boolean?>(null) }
    
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
    
    // Lista de todos os usuários (para admins promoverem outros admins)
    val todosUsuarios by viewModel.todosUsuarios.collectAsState()
    
    // Mostrar mensagem de sucesso após promoção/rebaixamento
    var ultimaAcaoAdmin by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    LaunchedEffect(state.isLoading, ultimaAcaoAdmin, todosUsuarios) {
        val (userId, foiPromovido) = ultimaAcaoAdmin ?: return@LaunchedEffect
        if (!state.isLoading) {
            val usuario = todosUsuarios.find { it.id == userId }
            usuario?.let {
                val sucesso = (it.ehAdministrador == foiPromovido)
                if (sucesso) {
                    snackbarHostState.showSnackbar(
                        message = if (foiPromovido) {
                            "${usuario.nome.ifBlank { "Usuário" }} foi promovido a administrador"
                        } else {
                            "${usuario.nome.ifBlank { "Usuário" }} foi rebaixado de administrador"
                        },
                        duration = SnackbarDuration.Short
                    )
                    ultimaAcaoAdmin = null
                }
            }
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
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header fixo (Foto e Nome)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nome do usuário
                Text(
                    text = state.nome ?: "Usuário",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Email
                Text(
                    text = state.email ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Conteúdo scrollável
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Badge de status (Admin ou Familiar)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .padding(vertical = 2.dp),
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

                // Card de vinculação com pessoa
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .padding(vertical = 2.dp),
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
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(32.dp)
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
                
                // Seção de Administração (apenas para admins)
                if (state.ehAdmin) {
                    var mostrarGerenciarAdmin by remember { mutableStateOf(false) }
                    var modoEdicao by remember { mutableStateOf(false) }
                    var usuariosSelecionados by remember { mutableStateOf<Set<String>>(emptySet()) }
                    
                    // Carregar lista automaticamente quando expandir
                    LaunchedEffect(mostrarGerenciarAdmin) {
                        if (mostrarGerenciarAdmin) {
                            // Sempre carregar quando expandir para garantir que a lista esteja atualizada
                            // Não verificar isLoading para evitar condições de corrida
                            viewModel.recarregarListaUsuarios()
                        }
                    }
                    
                    // Limpar seleção quando sair do modo de edição
                    LaunchedEffect(modoEdicao) {
                        if (!modoEdicao) {
                            usuariosSelecionados = emptySet()
                        }
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        indication = rememberRipple(),
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { 
                                        mostrarGerenciarAdmin = !mostrarGerenciarAdmin 
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (mostrarGerenciarAdmin) {
                                            Icons.Default.ExpandLess
                                        } else {
                                            Icons.Default.ExpandMore
                                        },
                                        contentDescription = if (mostrarGerenciarAdmin) {
                                            "Recolher"
                                        } else {
                                            "Expandir"
                                        },
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Gerenciar Administradores",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (mostrarGerenciarAdmin) {
                                        IconButton(
                                            onClick = { 
                                                modoEdicao = !modoEdicao
                                                if (!modoEdicao) {
                                                    usuariosSelecionados = emptySet()
                                                }
                                            },
                                            enabled = !state.isLoading
                                        ) {
                                            Icon(
                                                imageVector = if (modoEdicao) {
                                                    Icons.Default.Close
                                                } else {
                                                    Icons.Default.Edit
                                                },
                                                contentDescription = if (modoEdicao) {
                                                    "Sair do modo de edição"
                                                } else {
                                                    "Modo de edição"
                                                },
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { 
                                            // Expandir se estiver fechado e recarregar lista
                                            if (!mostrarGerenciarAdmin) {
                                                mostrarGerenciarAdmin = true
                                            }
                                            viewModel.recarregarListaUsuarios() 
                                        },
                                        enabled = !state.isLoading
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Recarregar lista",
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            
                            if (mostrarGerenciarAdmin) {
                                HorizontalDivider()
                                
                                // Modo de edição: mostrar controles de seleção múltipla
                                if (modoEdicao) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${usuariosSelecionados.size} selecionado(s)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    usuariosSelecionados = todosUsuarios
                                                        .filter { it.id != currentUserId }
                                                        .map { it.id }
                                                        .toSet()
                                                },
                                                enabled = !state.isLoading
                                            ) {
                                                Text("Selecionar Todos")
                                            }
                                            TextButton(
                                                onClick = {
                                                    usuariosSelecionados = emptySet()
                                                },
                                                enabled = !state.isLoading
                                            ) {
                                                Text("Limpar")
                                            }
                                        }
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.promoverAdminEmLote(usuariosSelecionados, true)
                                                usuariosSelecionados = emptySet()
                                                modoEdicao = false
                                            },
                                            enabled = usuariosSelecionados.isNotEmpty() && !state.isLoading,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Promover Selecionados")
                                        }
                                        
                                        Button(
                                            onClick = {
                                                viewModel.promoverAdminEmLote(usuariosSelecionados, false)
                                                usuariosSelecionados = emptySet()
                                                modoEdicao = false
                                            },
                                            enabled = usuariosSelecionados.isNotEmpty() && !state.isLoading,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PersonRemove,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Rebaixar Selecionados")
                                        }
                                    }
                                    
                                    HorizontalDivider()
                                }
                                
                                // Mostrar loading apenas se estiver carregando
                                if (state.isLoading) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (todosUsuarios.isEmpty()) {
                                                "Carregando usuários..."
                                            } else {
                                                "Atualizando lista..."
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                } 
                                // Mostrar mensagem de lista vazia apenas se não estiver carregando E a lista estiver vazia
                                else if (todosUsuarios.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.People,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Nenhum usuário encontrado",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                        )
                                        TextButton(
                                            onClick = { viewModel.recarregarListaUsuarios() },
                                            enabled = !state.isLoading
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Recarregar")
                                        }
                                    }
                                } 
                                // Mostrar lista de usuários se houver dados
                                else {
                                    // Se estiver carregando mas já tem dados, mostrar indicador de atualização
                                    if (state.isLoading && todosUsuarios.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Atualizando...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    
                                    // Mostrar lista de usuários (pode estar vazia se ainda não carregou)
                                    if (todosUsuarios.isNotEmpty()) {
                                        val usuariosFiltrados = todosUsuarios.filter { it.id != currentUserId }
                                        
                                        if (usuariosFiltrados.isEmpty()) {
                                            // Se todos os usuários foram filtrados (apenas o usuário atual existe)
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.People,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(48.dp),
                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    text = "Apenas você está cadastrado no sistema",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                                )
                                            }
                                        } else {
                                            usuariosFiltrados.forEach { usuario ->
                                            val isSelecionado = usuariosSelecionados.contains(usuario.id)
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable(
                                                        enabled = modoEdicao,
                                                        indication = if (modoEdicao) rememberRipple() else null,
                                                        interactionSource = remember { MutableInteractionSource() }
                                                    ) {
                                                        if (modoEdicao) {
                                                            usuariosSelecionados = if (isSelecionado) {
                                                                usuariosSelecionados - usuario.id
                                                            } else {
                                                                usuariosSelecionados + usuario.id
                                                            }
                                                        }
                                                    },
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Checkbox (modo edição) ou espaço vazio
                                                    if (modoEdicao) {
                                                        Checkbox(
                                                            checked = isSelecionado,
                                                            onCheckedChange = { checked ->
                                                                usuariosSelecionados = if (checked) {
                                                                    usuariosSelecionados + usuario.id
                                                                } else {
                                                                    usuariosSelecionados - usuario.id
                                                                }
                                                            },
                                                            enabled = !state.isLoading
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.width(48.dp))
                                                    }
                                                    
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = usuario.nome.ifBlank { "Sem nome" },
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                                        )
                                                        Text(
                                                            text = usuario.email.ifBlank { "Sem email" },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                                        )
                                                        if (usuario.pessoaVinculada != null) {
                                                            val pessoaVinculada = pessoasDisponiveis.find { it.id == usuario.pessoaVinculada }
                                                            pessoaVinculada?.let {
                                                                Text(
                                                                    text = "Vinculado a: ${it.nome}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (usuario.ehAdministrador) {
                                                        Surface(
                                                            shape = MaterialTheme.shapes.small,
                                                            color = MaterialTheme.colorScheme.primary
                                                        ) {
                                                            Text(
                                                                text = "ADMIN",
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onPrimary
                                                            )
                                                        }
                                                    }
                                                    
                                                    // Botão individual (apenas fora do modo de edição)
                                                    if (!modoEdicao) {
                                                        IconButton(
                                                            onClick = {
                                                                usuarioParaPromover = usuario
                                                                promoverAdmin = !usuario.ehAdministrador
                                                            },
                                                            enabled = !state.isLoading
                                                        ) {
                                                            Icon(
                                                                imageVector = if (usuario.ehAdministrador) {
                                                                    Icons.Default.PersonRemove
                                                                } else {
                                                                    Icons.Default.AdminPanelSettings
                                                                },
                                                                contentDescription = if (usuario.ehAdministrador) {
                                                                    "Rebaixar Admin"
                                                                } else {
                                                                    "Promover Admin"
                                                                },
                                                                tint = if (usuario.ehAdministrador) {
                                                                    MaterialTheme.colorScheme.error
                                                                } else {
                                                                    MaterialTheme.colorScheme.primary
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                    } else if (!state.isLoading) {
                                        // Se não há dados e não está carregando, mostrar mensagem
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.People,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = "Nenhum usuário encontrado",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                            )
                                            TextButton(
                                                onClick = { viewModel.recarregarListaUsuarios() },
                                                enabled = !state.isLoading
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Recarregar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // Modal de confirmação de promoção/rebaixamento de admin
                usuarioParaPromover?.let { usuario ->
                    val acao = promoverAdmin ?: return@let
                    AlertDialog(
                        onDismissRequest = {
                            usuarioParaPromover = null
                            promoverAdmin = null
                        },
                        icon = {
                            Icon(
                                imageVector = if (acao) {
                                    Icons.Default.AdminPanelSettings
                                } else {
                                    Icons.Default.PersonRemove
                                },
                                contentDescription = null,
                                tint = if (acao) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        title = {
                            Text(
                                text = if (acao) {
                                    "Promover a Administrador"
                                } else {
                                    "Rebaixar de Administrador"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (acao) {
                                        "Deseja promover o usuário a administrador?"
                                    } else {
                                        "Deseja rebaixar o usuário de administrador?"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
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
                                        Text(
                                            text = usuario.nome.ifBlank { "Sem nome" },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = usuario.email.ifBlank { "Sem email" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                
                                if (acao) {
                                    Text(
                                        text = "O usuário terá acesso a funcionalidades administrativas como gerenciar convites, edições pendentes e resolver duplicatas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "O usuário perderá acesso a funcionalidades administrativas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val userId = usuario.id
                                    val ehAdmin = acao
                                    viewModel.promoverAdmin(userId, ehAdmin)
                                    ultimaAcaoAdmin = Pair(userId, ehAdmin)
                                    usuarioParaPromover = null
                                    promoverAdmin = null
                                },
                                enabled = !state.isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (acao) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (acao) "Promover" else "Rebaixar",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    usuarioParaPromover = null
                                    promoverAdmin = null
                                },
                                enabled = !state.isLoading
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
                
                // Ações
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToFamiliaZero() }
                        ) {
                            ListItem(
                                headlineContent = { Text("Criar Família Zero") },
                                supportingContent = { Text("Criar a raiz da árvore genealógica") },
                                leadingContent = {
                                    Icon(Icons.Default.FamilyRestroom, contentDescription = null)
                                },
                                trailingContent = {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToCadastroPessoa() }
                        ) {
                            ListItem(
                                headlineContent = { Text("Adicionar Pessoa") },
                                supportingContent = { Text("Cadastrar nova pessoa na árvore") },
                                leadingContent = {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                },
                                trailingContent = {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        // Aceitar convites (todos os usuários)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAceitarConvites() }
                        ) {
                            ListItem(
                                headlineContent = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Convites Pendentes")
                                        if (state.convitesPendentes > 0) {
                                            Badge {
                                                Text("${state.convitesPendentes}")
                                            }
                                        }
                                    }
                                },
                                supportingContent = { Text("Ver e aceitar convites pendentes") },
                                leadingContent = {
                                    Icon(Icons.Default.Mail, contentDescription = null)
                                },
                                trailingContent = {
                                    if (state.convitesPendentes > 0) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null)
                                    } else {
                                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                                    }
                                }
                            )
                        }
                        
                        // Gerenciar convites (apenas admin)
                        if (state.ehAdmin) {
                            HorizontalDivider()
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToGerenciarConvites() }
                            ) {
                                ListItem(
                                    headlineContent = { Text("Gerenciar Convites") },
                                    supportingContent = { Text("Criar e gerenciar convites (Admin)") },
                                    leadingContent = {
                                        Icon(Icons.Default.Group, contentDescription = null)
                                    },
                                    trailingContent = {
                                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                                    }
                                )
                            }
                            
                            HorizontalDivider()
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToGerenciarEdicoes() }
                            ) {
                                ListItem(
                                    headlineContent = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("Edições Pendentes")
                                            if (state.edicoesPendentes > 0) {
                                                Badge {
                                                    Text("${state.edicoesPendentes}")
                                                }
                                            }
                                        }
                                    },
                                    supportingContent = { Text("Revisar edições pendentes (Admin)") },
                                    leadingContent = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    },
                                    trailingContent = {
                                        if (state.edicoesPendentes > 0) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = null)
                                        } else {
                                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                                        }
                                    }
                                )
                            }
                            
                            HorizontalDivider()
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToResolverDuplicatas() }
                            ) {
                                ListItem(
                                    headlineContent = { Text("Resolver Duplicatas") },
                                    supportingContent = { Text("Detectar e resolver duplicatas (Admin)") },
                                    leadingContent = {
                                        Icon(Icons.Default.CopyAll, contentDescription = null)
                                    },
                                    trailingContent = {
                                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                                    }
                                )
                            }
                        }
                        
                        HorizontalDivider()
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.logout() }
                        ) {
                            ListItem(
                                headlineContent = { Text("Sair") },
                                supportingContent = { Text("Fazer logout do aplicativo") },
                                leadingContent = {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                },
                                trailingContent = {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

