package com.raizesvivas.app.presentation.screens.convites

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.StatusConvite
import com.raizesvivas.app.presentation.screens.cadastro.PessoaSelector
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de gerenciamento de convites (apenas admin)
 * 
 * Permite criar novos convites e visualizar todos os convites
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarConvitesScreen(
    viewModel: GerenciarConvitesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val convites by viewModel.convites.collectAsState()
    val pessoasDisponiveis by viewModel.pessoasDisponiveis.collectAsState()
    val context = LocalContext.current
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    
    // Compartilhar template de email quando gerado
    LaunchedEffect(state.templateEmail) {
        state.templateEmail?.let { template ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Convite para Árvore Genealógica - Raízes Vivas")
                putExtra(Intent.EXTRA_TEXT, template)
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar Convite"))
            // Limpar template após compartilhar
            viewModel.limparTemplateEmail()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Convites") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.carregarConvites() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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
                            text = "Apenas administradores podem gerenciar convites",
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
                        .padding(bottom = 16.dp),
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
            if (state.sucesso) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
                            text = "Convite criado com sucesso!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Formulário de novo convite
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Novo Convite",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = state.emailConvidado,
                        onValueChange = { viewModel.onEmailConvidadoChanged(it) },
                        label = { Text("Email do Convidado") },
                        placeholder = { Text("exemplo@email.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        singleLine = true,
                        isError = state.emailError != null,
                        supportingText = state.emailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    )
                    
                    // Seletor de pessoa vinculada (opcional)
                    PessoaSelector(
                        label = "Pessoa Vinculada (opcional)",
                        pessoaId = state.pessoaVinculadaId,
                        pessoasDisponiveis = pessoasDisponiveis,
                        onPessoaSelecionada = { pessoa ->
                            viewModel.onPessoaVinculadaChanged(pessoa?.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botão para compartilhar template de email
                        OutlinedButton(
                            onClick = { viewModel.gerarTemplateEmail() },
                            enabled = !state.isLoading && state.emailConvidado.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compartilhar")
                        }
                        
                        // Botão para criar convite
                        Button(
                            onClick = { viewModel.criarConvite() },
                            enabled = !state.isLoading && state.emailConvidado.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Criar Convite")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Convites Pendentes
            val convitesPendentes = convites.filter { it.status == StatusConvite.PENDENTE }
            Text(
                text = "Convites Pendentes (${convitesPendentes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (convitesPendentes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nenhum convite pendente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                convitesPendentes.forEach { convite ->
                    ConviteCard(
                        convite = convite,
                        dateFormatter = dateFormatter,
                        onDeletar = { viewModel.deletarConvite(convite.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Lista de convites
            Text(
                text = "Todos os Convites (${convites.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Filtrar convites que não são pendentes (para evitar duplicação)
            val convitesNaoPendentes = convites.filter { it.status != StatusConvite.PENDENTE }
            
            if (convitesNaoPendentes.isEmpty() && convitesPendentes.isEmpty()) {
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
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nenhum convite encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (convitesNaoPendentes.isEmpty()) {
                // Se só há convites pendentes, mostrar mensagem
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Todos os convites estão pendentes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                convitesNaoPendentes.forEach { convite ->
                    ConviteCard(
                        convite = convite,
                        dateFormatter = dateFormatter,
                        onDeletar = { viewModel.deletarConvite(convite.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card de exibição de convite
 */
@Composable
private fun ConviteCard(
    convite: com.raizesvivas.app.domain.model.Convite,
    dateFormatter: SimpleDateFormat,
    onDeletar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (convite.status) {
        StatusConvite.PENDENTE -> MaterialTheme.colorScheme.primary
        StatusConvite.ACEITO -> MaterialTheme.colorScheme.tertiary
        StatusConvite.REJEITADO -> MaterialTheme.colorScheme.error
        StatusConvite.EXPIRADO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val statusIcon = when (convite.status) {
        StatusConvite.PENDENTE -> Icons.Default.Schedule
        StatusConvite.ACEITO -> Icons.Default.CheckCircle
        StatusConvite.REJEITADO -> Icons.Default.Cancel
        StatusConvite.EXPIRADO -> Icons.Default.Warning
    }
    
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = convite.status.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                
                IconButton(onClick = onDeletar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            HorizontalDivider()
            
            Text(
                text = "Email: ${convite.emailConvidado}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (convite.pessoaVinculada != null) {
                Text(
                    text = "Pessoa vinculada: ${convite.pessoaVinculada}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "Criado em: ${dateFormatter.format(convite.criadoEm)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Expira em: ${dateFormatter.format(convite.expiraEm)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (convite.expirou) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            if (!convite.estaValido && convite.status == StatusConvite.PENDENTE) {
                Text(
                    text = "⚠️ Convite expirado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

