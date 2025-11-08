package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.domain.model.Notificacao
import com.raizesvivas.app.domain.model.TipoNotificacao
import com.raizesvivas.app.presentation.viewmodel.NotificacaoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Drawer de notificações
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacoesDrawer(
    notificacoes: List<Notificacao>,
    viewModel: NotificacaoViewModel,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onNotificacaoClick: ((Notificacao) -> Unit)? = null
) {
    // Estado para filtro de tipo
    var filtroTipo by remember { mutableStateOf<TipoNotificacao?>(null) }
    
    // Filtrar notificações
    val notificacoesFiltradas = remember(notificacoes, filtroTipo) {
        if (filtroTipo == null) {
            notificacoes
        } else {
            notificacoes.filter { it.tipo == filtroTipo }
        }
    }
    
    // Contar notificações por tipo
    val contadoresPorTipo = remember(notificacoes) {
        notificacoes.groupingBy { it.tipo }.eachCount()
    }
    
    // Agrupar notificações por data
    val notificacoesAgrupadas = remember(notificacoesFiltradas) {
        @Suppress("UNUSED_VARIABLE")
        val agora = System.currentTimeMillis()
        val hoje = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val ontem = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val grupos = mutableMapOf<String, MutableList<Notificacao>>()
        
        notificacoesFiltradas.forEach { notificacao ->
            val timestamp = notificacao.criadaEm.time
            
            val grupo = when {
                timestamp >= hoje -> "Hoje"
                timestamp >= ontem -> "Ontem"
                else -> {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateFormat.format(notificacao.criadaEm)
                }
            }
            
            grupos.getOrPut(grupo) { mutableListOf() }.add(notificacao)
        }
        
        grupos.toList().sortedByDescending { (_, notifs) ->
            notifs.maxOfOrNull { it.criadaEm.time } ?: 0L
        }
    }
    ModalDrawerSheet(
        modifier = modifier.width(360.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notificações",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (notificacoes.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                viewModel.marcarTodasComoLidas()
                            }
                        ) {
                            Text("Marcar todas como lidas")
                        }
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
            }
            
            HorizontalDivider()
            
            // Filtros por tipo
            if (notificacoes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botão "Todas"
                    FilterChip(
                        selected = filtroTipo == null,
                        onClick = { filtroTipo = null },
                        label = {
                            Text("Todas")
                        },
                        leadingIcon = if (filtroTipo == null) {
                            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                        } else null
                    )
                    
                    // Filtros por tipo
                    TipoNotificacao.values().forEach { tipo ->
                        val count = contadoresPorTipo[tipo] ?: 0
                        if (count > 0) {
                            FilterChip(
                                selected = filtroTipo == tipo,
                                onClick = { filtroTipo = if (filtroTipo == tipo) null else tipo },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tipo.descricao)
                                        Badge {
                                            Text(count.toString())
                                        }
                                    }
                                },
                                leadingIcon = if (filtroTipo == tipo) {
                                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }
                }
                
                HorizontalDivider()
            }
            
            // Lista de notificações
            if (notificacoesFiltradas.isEmpty()) {
                // Estado vazio
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nenhuma notificação",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (filtroTipo != null) {
                            Text(
                                text = "Nenhuma notificação do tipo selecionado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        } else {
                            Text(
                                text = "Você será notificado sobre novas sugestões e atualizações",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Agrupar por data
                    notificacoesAgrupadas.forEach { (grupo, notificacoesGrupo) ->
                        item(key = "header_$grupo") {
                            Text(
                                text = grupo,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            )
                        }
                        
                        items(
                            items = notificacoesGrupo.sortedByDescending { it.criadaEm },
                            key = { it.id }
                        ) { notificacao ->
                            NotificacaoItem(
                                notificacao = notificacao,
                                onClick = {
                                    if (!notificacao.lida) {
                                        viewModel.marcarComoLida(notificacao)
                                    }
                                    onNotificacaoClick?.invoke(notificacao)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Item individual de notificação
 */
@Composable
private fun NotificacaoItem(
    notificacao: Notificacao,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dataFormatada = dateFormat.format(notificacao.criadaEm)
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = onClick,
        color = if (notificacao.lida) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ícone de tipo
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = when (notificacao.tipo) {
                    com.raizesvivas.app.domain.model.TipoNotificacao.SUGESTAO_SUBFAMILIA -> 
                        MaterialTheme.colorScheme.tertiaryContainer
                    com.raizesvivas.app.domain.model.TipoNotificacao.CONQUISTA_DESBLOQUEADA -> 
                        MaterialTheme.colorScheme.secondaryContainer
                    else -> 
                        MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (notificacao.tipo) {
                            com.raizesvivas.app.domain.model.TipoNotificacao.SUGESTAO_SUBFAMILIA -> 
                                Icons.Default.FamilyRestroom
                            com.raizesvivas.app.domain.model.TipoNotificacao.CONQUISTA_DESBLOQUEADA -> 
                                Icons.Default.Star
                            else -> 
                                Icons.Default.Info
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when (notificacao.tipo) {
                            com.raizesvivas.app.domain.model.TipoNotificacao.SUGESTAO_SUBFAMILIA -> 
                                MaterialTheme.colorScheme.onTertiaryContainer
                            com.raizesvivas.app.domain.model.TipoNotificacao.CONQUISTA_DESBLOQUEADA -> 
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else -> 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
            
            // Conteúdo
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notificacao.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notificacao.lida) FontWeight.Normal else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = notificacao.mensagem,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = dataFormatada,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // Indicador de não lida
            if (!notificacao.lida) {
                Surface(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.CenterVertically),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
            }
        }
    }
}
