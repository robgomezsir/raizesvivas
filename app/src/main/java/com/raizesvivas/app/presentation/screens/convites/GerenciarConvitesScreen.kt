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
import com.raizesvivas.app.presentation.components.RaizesVivasTextField
import com.raizesvivas.app.presentation.components.AnimatedSearchBar
import com.raizesvivas.app.presentation.screens.cadastro.PessoaSelector
import com.raizesvivas.app.presentation.ui.theme.InputShapeSuave
import com.raizesvivas.app.presentation.ui.theme.inputColorsPastel
import com.raizesvivas.app.presentation.ui.theme.RaizesVivasButtonDefaults
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
    val pedidos by viewModel.pedidos.collectAsState()
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
            if (state.sucesso) {
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
                            text = "Convite criado com sucesso!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Novo Convite",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    RaizesVivasTextField(
                        value = state.emailConvidado,
                        onValueChange = { viewModel.onEmailConvidadoChanged(it) },
                        label = "Email do Convidado",
                        placeholder = { Text("exemplo@email.com") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
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
                    
                    // Botões em linha horizontal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botão para compartilhar template de email
                        OutlinedButton(
                            onClick = { viewModel.gerarTemplateEmail() },
                            enabled = !state.isLoading && state.emailConvidado.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RaizesVivasButtonDefaults.Shape,
                            border = RaizesVivasButtonDefaults.outlineStroke(),
                            contentPadding = RaizesVivasButtonDefaults.ContentPadding
                        ) {
                            Icon(
                                Icons.Default.Share, 
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Compartilhar",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        
                        // Botão para criar convite
                        Button(
                            onClick = { viewModel.criarConvite() },
                            enabled = !state.isLoading && state.emailConvidado.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = RaizesVivasButtonDefaults.Shape,
                            colors = RaizesVivasButtonDefaults.primaryColors(),
                            contentPadding = RaizesVivasButtonDefaults.ContentPadding
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send, 
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Criar Convite",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pedidos de Convite (novos)
            Text(
                text = "Pedidos de Convite (${pedidos.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estado local para controle da animação de busca
                var isSearchActive by remember { mutableStateOf(false) }
                val filtroEmail = viewModel.filtroEmail.collectAsState().value

                AnimatedSearchBar(
                    query = filtroEmail,
                    onQueryChange = { viewModel.atualizarFiltroEmail(it) },
                    isSearchActive = isSearchActive || filtroEmail.isNotEmpty(),
                    onSearchActiveChange = { isSearchActive = it },
                    placeholder = "Filtrar por e-mail...",
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = viewModel.filtroStatus == "pending",
                    onClick = { 
                        viewModel.atualizarFiltroStatus(
                            if (viewModel.filtroStatus == "pending") "" else "pending"
                        ) 
                    },
                    label = { 
                        Text(
                            if (viewModel.filtroStatus == "pending") "Pendentes" else "Todos",
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    shape = MaterialTheme.shapes.medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (pedidos.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Nenhum pedido pendente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                pedidos.forEach { req ->
                    PedidoConviteCard(
                        request = req,
                        pessoasDisponiveis = pessoasDisponiveis,
                        onAprovar = { pessoaId -> viewModel.aprovarPedido(req, pessoaId) },
                        onRejeitar = { viewModel.rejeitarPedido(req.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                if (viewModel.pedidosHasMore.collectAsState().value) {
                    OutlinedButton(
                        onClick = { viewModel.carregarPedidos(reset = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RaizesVivasButtonDefaults.Shape,
                        border = RaizesVivasButtonDefaults.outlineStroke(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Carregar mais",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

@Composable
private fun PedidoConviteCard(
    request: com.raizesvivas.app.domain.model.AccessRequest,
    pessoasDisponiveis: List<Pessoa>,
    onAprovar: (String?) -> Unit,
    onRejeitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pessoaSelecionada by remember { mutableStateOf<Pessoa?>(null) }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp), 
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        request.email, 
                        style = MaterialTheme.typography.titleSmall, 
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                FilterChip(
                    selected = true,
                    onClick = {},
                    enabled = false,
                    label = { 
                        Text(
                            request.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    shape = MaterialTheme.shapes.small
                )
            }
            if (!request.nome.isNullOrBlank()) {
                Text("Nome: ${request.nome}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!request.telefone.isNullOrBlank()) {
                Text("Telefone: ${request.telefone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            PessoaSelector(
                label = "Vincular a (opcional)",
                pessoaId = pessoaSelecionada?.id,
                pessoasDisponiveis = pessoasDisponiveis,
                onPessoaSelecionada = { pessoa -> pessoaSelecionada = pessoa },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRejeitar, 
                    modifier = Modifier.weight(1f),
                    shape = RaizesVivasButtonDefaults.Shape,
                    border = RaizesVivasButtonDefaults.outlineStroke(),
                    contentPadding = RaizesVivasButtonDefaults.ContentPadding
                ) {
                    Icon(
                        Icons.Default.Close, 
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Rejeitar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Button(
                    onClick = { onAprovar(pessoaSelecionada?.id) }, 
                    modifier = Modifier.weight(1f),
                    shape = RaizesVivasButtonDefaults.Shape,
                    colors = RaizesVivasButtonDefaults.primaryColors(),
                    contentPadding = RaizesVivasButtonDefaults.ContentPadding
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send, 
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Aprovar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

