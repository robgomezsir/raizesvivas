package com.raizesvivas.app.presentation.screens.usuarios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.presentation.components.RaizesVivasTextField
import com.raizesvivas.app.presentation.components.AnimatedSearchBar
import com.raizesvivas.app.domain.model.Usuario
import com.raizesvivas.app.domain.model.NivelPermissao
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de gerenciamento de usuários (apenas admin)
 * 
 * Permite editar, deletar e enviar email de reset de senha para usuários
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarUsuariosScreen(
    viewModel: GerenciarUsuariosViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    
    // Estado para busca
    var mostrarBusca by rememberSaveable { mutableStateOf(false) }
    var termoBusca by rememberSaveable { mutableStateOf("") }
    
    // Estado para modal de edição
    var usuarioEditando by remember { mutableStateOf<Usuario?>(null) }
    var nomeEditado by remember { mutableStateOf("") }
    var emailEditado by remember { mutableStateOf("") }
    var ehAdminEditado by remember { mutableStateOf(false) }
    
    // Estado para confirmação de exclusão
    var usuarioParaDeletar by remember { mutableStateOf<Usuario?>(null) }
    
    // Filtrar usuários baseado no termo de busca
    val usuariosFiltrados = remember(state.usuarios, termoBusca) {
        if (termoBusca.isBlank()) {
            state.usuarios
        } else {
            val termoLower = termoBusca.lowercase(Locale.getDefault())
            state.usuarios.filter { usuario ->
                usuario.nome.lowercase(Locale.getDefault()).contains(termoLower) ||
                usuario.email.lowercase(Locale.getDefault()).contains(termoLower)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (mostrarBusca) {
                        AnimatedSearchBar(
                            query = termoBusca,
                            onQueryChange = { termoBusca = it },
                            isSearchActive = mostrarBusca,
                            onSearchActiveChange = { 
                                mostrarBusca = it
                                if (!it) termoBusca = ""
                            },
                            placeholder = "Buscar usuário...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Gerenciar Usuários")
                    }
                },
                navigationIcon = {
                    if (!mostrarBusca) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    if (!mostrarBusca) {
                        IconButton(onClick = { mostrarBusca = true }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Buscar"
                            )
                        }
                        IconButton(onClick = { viewModel.carregarUsuarios() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Verificar permissões
            if (!state.ehAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Acesso Restrito",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Apenas administradores podem gerenciar usuários",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                return@Scaffold
            }
            
            // Mensagem de erro
            state.erro?.let { erro ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = erro,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.limparErro() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Mensagem de sucesso
            state.sucesso?.let { sucesso ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sucesso,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.limparSucesso() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Lista de usuários
            Text(
                text = "Usuários (${usuariosFiltrados.size}${if (termoBusca.isNotBlank()) " de ${state.usuarios.size}" else ""})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (usuariosFiltrados.isEmpty() && !state.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (termoBusca.isNotBlank()) "Nenhum usuário encontrado para \"$termoBusca\"" else "Nenhum usuário encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                usuariosFiltrados.forEach { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        dateFormatter = dateFormatter,
                        onEditar = {
                            usuarioEditando = usuario
                            nomeEditado = usuario.nome
                            emailEditado = usuario.email
                            ehAdminEditado = usuario.ehAdministrador
                        },
                        onDeletar = {
                            usuarioParaDeletar = usuario
                        },
                        onEnviarResetSenha = {
                            viewModel.enviarEmailResetSenha(usuario.email)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        nomePessoaVinculada = state.nomesPessoasVinculadas[usuario.pessoaVinculada]
                    )
                }
            }
        }
    }
    
    // Modal de edição
    usuarioEditando?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioEditando = null },
            title = { Text("Editar Usuário") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RaizesVivasTextField(
                        value = nomeEditado,
                        onValueChange = { nomeEditado = it },
                        label = "Nome",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    )
                    
                    RaizesVivasTextField(
                        value = emailEditado,
                        onValueChange = { emailEditado = it },
                        label = "Email",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false, // Email não pode ser alterado
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Administrador:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = ehAdminEditado,
                            onCheckedChange = { ehAdminEditado = it },
                            enabled = !state.isLoading
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val usuarioAtualizado = usuario.copy(
                            nome = nomeEditado.trim(),
                            ehAdministrador = ehAdminEditado
                        )
                        viewModel.atualizarUsuario(usuarioAtualizado)
                        usuarioEditando = null
                    },
                    enabled = nomeEditado.trim().isNotBlank() && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Salvar")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { usuarioEditando = null },
                    enabled = !state.isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Dialog de confirmação de exclusão
    usuarioParaDeletar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { usuarioParaDeletar = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Confirmar Exclusão") },
            text = {
                Text(
                    "Tem certeza que deseja deletar o usuário \"${usuario.nome}\" (${usuario.email})?\n\n" +
                    "Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletarUsuario(usuario.id)
                        usuarioParaDeletar = null
                    },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Deletar")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { usuarioParaDeletar = null },
                    enabled = !state.isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Card de exibição de usuário
 */
@Composable
private fun UsuarioCard(
    usuario: Usuario,
    dateFormatter: SimpleDateFormat,
    onEditar: () -> Unit,
    onDeletar: () -> Unit,
    onEnviarResetSenha: () -> Unit,
    modifier: Modifier = Modifier,
    nomePessoaVinculada: String? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when {
                            usuario.ehAdministradorSenior -> Icons.Default.AdminPanelSettings
                            usuario.ehAdministrador -> Icons.Default.AdminPanelSettings
                            else -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = when {
                            usuario.ehAdministradorSenior -> MaterialTheme.colorScheme.primary
                            usuario.ehAdministrador -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = usuario.nome.ifBlank { "Sem nome" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = usuario.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Badge de classificação
                val (textoClassificacao, corContainer, corTexto) = when {
                    usuario.ehAdministradorSenior -> Triple(
                        "ADMIN SR",
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    usuario.ehAdministrador -> Triple(
                        "ADMIN",
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    else -> Triple(
                        "FAMILIAR",
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = corContainer
                ) {
                    Text(
                        text = textoClassificacao,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = corTexto,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            HorizontalDivider()
            
            Text(
                text = "Criado em: ${dateFormatter.format(usuario.criadoEm)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (usuario.pessoaVinculada != null) {
                val textoVinculo = if (nomePessoaVinculada != null) {
                    "Pessoa vinculada: $nomePessoaVinculada"
                } else {
                    "Pessoa vinculada: ${usuario.pessoaVinculada}"
                }
                
                Text(
                    text = textoVinculo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditar
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
                
                OutlinedButton(
                    onClick = onEnviarResetSenha
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Senha")
                }
                
                OutlinedButton(
                    onClick = onDeletar,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deletar")
                }
            }
        }
    }
}

