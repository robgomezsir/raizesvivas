package com.raizesvivas.app.presentation.screens.configuracoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tela de Configurações - Apenas ADMIN SÊNIOR
 * Permite enviar notificações para todos os usuários
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConfiguracoesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Limpar mensagens após alguns segundos
    LaunchedEffect(state.sucesso) {
        if (state.sucesso != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.limparSucesso()
        }
    }
    
    LaunchedEffect(state.erro) {
        if (state.erro != null) {
            kotlinx.coroutines.delay(5000)
            viewModel.limparErro()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!state.ehAdminSenior) {
            // Mostrar mensagem de acesso negado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Acesso Negado",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Apenas ADMIN SÊNIOR pode acessar esta página",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Seção: Usuário Mais Antigo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Usuário Mais Antigo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        state.usuarioMaisAntigo?.let { usuario ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = usuario.nome.ifBlank { "Sem nome" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = usuario.email.ifBlank { "Sem email" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
                                    Text(
                                        text = "Cadastrado em: ${dateFormat.format(usuario.criadoEm)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                    )
                                    if (usuario.ehAdministradorSenior) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = "ADMIN SÊNIOR",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        } ?: run {
                            Text(
                                text = "Carregando informações...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Título da seção
                Text(
                    text = "Enviar Notificação para Todos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Envie uma mensagem importante para todos os usuários do sistema. A mensagem aparecerá em um modal na tela inicial após o login.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Campo de título
                OutlinedTextField(
                    value = state.titulo,
                    onValueChange = viewModel::atualizarTitulo,
                    label = { Text("Título da Notificação") },
                    placeholder = { Text("Ex: Atualização Importante") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading
                )
                
                // Campo de mensagem
                OutlinedTextField(
                    value = state.mensagem,
                    onValueChange = viewModel::atualizarMensagem,
                    label = { Text("Mensagem") },
                    placeholder = { Text("Digite a mensagem que será enviada para todos os usuários...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10,
                    enabled = !state.isLoading
                )
                
                // Botão de enviar
                Button(
                    onClick = viewModel::enviarNotificacao,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading && state.titulo.isNotBlank() && state.mensagem.isNotBlank()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando...")
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar para Todos")
                    }
                }
                
                // Mensagem de sucesso
                state.sucesso?.let { sucesso ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = sucesso,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Mensagem de erro
                state.erro?.let { erro ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = erro,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // ============================================
                // Seção: Aviso de Atualização do App
                // ============================================
                HorizontalDivider()
                Text(
                    text = "Atualização do App",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure o aviso de atualização que aparecerá em um modal após o login, com um botão BAIXAR que abre o link no navegador.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Campo versão (ex: v2.5)
                OutlinedTextField(
                    value = state.versaoAtualizacao,
                    onValueChange = viewModel::atualizarVersaoAtualizacao,
                    label = { Text("Versão (ex: v2.5)") },
                    placeholder = { Text("v2.5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSendingUpdate
                )

                // Campo link de download
                OutlinedTextField(
                    value = state.linkDownloadAtualizacao,
                    onValueChange = viewModel::atualizarLinkDownloadAtualizacao,
                    label = { Text("Link para página de download") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isSendingUpdate
                )

                // Preview do texto que será enviado
                if (state.versaoAtualizacao.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "Pré-visualização da mensagem:",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        Text(
                            text = "Nova atualização!\nBaixe agora mesmo a nova atualização ${state.versaoAtualizacao} do app Raízes Vivas",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Button(
                    onClick = viewModel::enviarAtualizacao,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSendingUpdate && state.versaoAtualizacao.isNotBlank() && state.linkDownloadAtualizacao.isNotBlank()
                ) {
                    if (state.isSendingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicando aviso...")
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publicar aviso de atualização")
                    }
                }
            }
        }
    }
}

