package com.raizesvivas.app.presentation.screens.moderacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.AuditLog
import com.raizesvivas.app.domain.model.TipoAcaoAudit
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeracaoScreen(
    viewModel: ModeracaoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moderação") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.recarregar() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recarregar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!state.ehAdminSenior) {
            // Acesso negado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Acesso Restrito",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Esta página é visível apenas para Administradores Sênior",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Cabeçalho com estatísticas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.History,
                        label = "Total de Ações",
                        value = state.logs.size.toString()
                    )
                    StatItem(
                        icon = Icons.Default.Person,
                        label = "Usuários Ativos",
                        value = state.usuariosUnicos.size.toString()
                    )
                    StatItem(
                        icon = Icons.Default.Today,
                        label = "Hoje",
                        value = state.logs.count {
                            val hoje = Calendar.getInstance()
                            val logCal = Calendar.getInstance().apply { time = it.timestamp }
                            hoje.get(Calendar.DAY_OF_YEAR) == logCal.get(Calendar.DAY_OF_YEAR) &&
                                    hoje.get(Calendar.YEAR) == logCal.get(Calendar.YEAR)
                        }.toString()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros
            FiltrosSection(
                filtroAcao = state.filtroAcao,
                filtroUsuario = state.filtroUsuario,
                usuariosDisponiveis = state.usuariosUnicos,
                onFiltrarPorAcao = { viewModel.filtrarPorAcao(it) },
                onFiltrarPorUsuario = { viewModel.filtrarPorUsuario(it) },
                onLimparFiltros = { viewModel.limparFiltros() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de logs
            Text(
                text = "Logs de Auditoria (${state.logsFiltrados.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.logsFiltrados.isEmpty()) {
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nenhum log encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.logsFiltrados,
                        key = { it.id }
                    ) { log ->
                        AuditLogCard(
                            log = log,
                            dateFormatter = dateFormatter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosSection(
    filtroAcao: TipoAcaoAudit?,
    filtroUsuario: String?,
    usuariosDisponiveis: List<Pair<String, String>>,
    onFiltrarPorAcao: (TipoAcaoAudit?) -> Unit,
    onFiltrarPorUsuario: (String?) -> Unit,
    onLimparFiltros: () -> Unit
) {
    var expandedAcao by remember { mutableStateOf(false) }
    var expandedUsuario by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (filtroAcao != null || filtroUsuario != null) {
                    TextButton(onClick = onLimparFiltros) {
                        Text("Limpar filtros")
                    }
                }
            }

            // Filtro por ação
            ExposedDropdownMenuBox(
                expanded = expandedAcao,
                onExpandedChange = { expandedAcao = it }
            ) {
                OutlinedTextField(
                    value = filtroAcao?.name ?: "Todas as ações",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Ação") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAcao)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedAcao,
                    onDismissRequest = { expandedAcao = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas as ações") },
                        onClick = {
                            onFiltrarPorAcao(null)
                            expandedAcao = false
                        }
                    )
                    TipoAcaoAudit.entries.forEach { acao ->
                        DropdownMenuItem(
                            text = { Text(obterNomeAcao(acao)) },
                            onClick = {
                                onFiltrarPorAcao(acao)
                                expandedAcao = false
                            }
                        )
                    }
                }
            }

            // Filtro por usuário
            ExposedDropdownMenuBox(
                expanded = expandedUsuario,
                onExpandedChange = { expandedUsuario = it }
            ) {
                OutlinedTextField(
                    value = usuariosDisponiveis.find { it.first == filtroUsuario }?.second
                        ?: "Todos os usuários",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Usuário") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUsuario)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedUsuario,
                    onDismissRequest = { expandedUsuario = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos os usuários") },
                        onClick = {
                            onFiltrarPorUsuario(null)
                            expandedUsuario = false
                        }
                    )
                    usuariosDisponiveis.forEach { (id, nome) ->
                        DropdownMenuItem(
                            text = { Text(nome) },
                            onClick = {
                                onFiltrarPorUsuario(id)
                                expandedUsuario = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(
    log: AuditLog,
    dateFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cabeçalho com ação e timestamp
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
                        imageVector = obterIconeAcao(log.acao),
                        contentDescription = null,
                        tint = obterCorAcao(log.acao)
                    )
                    Text(
                        text = obterNomeAcao(log.acao),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = obterCorAcao(log.acao)
                    )
                }
                Badge(
                    containerColor = obterCorAcao(log.acao).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = log.entidade,
                        color = obterCorAcao(log.acao)
                    )
                }
            }

            HorizontalDivider()

            // Detalhes
            Text(
                text = log.detalhes,
                style = MaterialTheme.typography.bodyMedium
            )

            // Informações do usuário
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${log.usuarioNome} (${log.usuarioEmail})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = dateFormatter.format(log.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Informações técnicas (IP e Device)
            if (log.ipAddress != null || log.deviceInfo != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    log.ipAddress?.let { ip ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = ip,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    log.deviceInfo?.let { device ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = device,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun obterIconeAcao(acao: TipoAcaoAudit): androidx.compose.ui.graphics.vector.ImageVector {
    return when (acao) {
        TipoAcaoAudit.CRIAR -> Icons.Default.Add
        TipoAcaoAudit.EDITAR -> Icons.Default.Edit
        TipoAcaoAudit.EXCLUIR -> Icons.Default.Delete
        TipoAcaoAudit.RESTAURAR -> Icons.Default.Restore
        TipoAcaoAudit.APROVAR -> Icons.Default.Check
        TipoAcaoAudit.REJEITAR -> Icons.Default.Close
        TipoAcaoAudit.LOGIN -> Icons.Default.Login
        TipoAcaoAudit.LOGOUT -> Icons.Default.Logout
        TipoAcaoAudit.OUTRO -> Icons.Default.MoreHoriz
    }
}

@Composable
private fun obterCorAcao(acao: TipoAcaoAudit): androidx.compose.ui.graphics.Color {
    return when (acao) {
        TipoAcaoAudit.CRIAR -> MaterialTheme.colorScheme.primary
        TipoAcaoAudit.EDITAR -> MaterialTheme.colorScheme.tertiary
        TipoAcaoAudit.EXCLUIR -> MaterialTheme.colorScheme.error
        TipoAcaoAudit.RESTAURAR -> MaterialTheme.colorScheme.secondary
        TipoAcaoAudit.APROVAR -> MaterialTheme.colorScheme.primary
        TipoAcaoAudit.REJEITAR -> MaterialTheme.colorScheme.error
        TipoAcaoAudit.LOGIN -> MaterialTheme.colorScheme.secondary
        TipoAcaoAudit.LOGOUT -> MaterialTheme.colorScheme.onSurfaceVariant
        TipoAcaoAudit.OUTRO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun obterNomeAcao(acao: TipoAcaoAudit): String {
    return when (acao) {
        TipoAcaoAudit.CRIAR -> "Criação"
        TipoAcaoAudit.EDITAR -> "Edição"
        TipoAcaoAudit.EXCLUIR -> "Exclusão"
        TipoAcaoAudit.RESTAURAR -> "Restauração"
        TipoAcaoAudit.APROVAR -> "Aprovação"
        TipoAcaoAudit.REJEITAR -> "Rejeição"
        TipoAcaoAudit.LOGIN -> "Login"
        TipoAcaoAudit.LOGOUT -> "Logout"
        TipoAcaoAudit.OUTRO -> "Outra Ação"
    }
}
