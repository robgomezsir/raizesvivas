package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.layout.*
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
import com.raizesvivas.app.domain.model.SugestaoSubfamilia
import com.raizesvivas.app.presentation.viewmodel.SubfamiliaViewModel
import androidx.compose.runtime.collectAsState

/**
 * Card para exibir sugestões de subfamílias pendentes
 * 
 * Mostra sugestões quando existem, ou um botão para detectar quando não há sugestões
 */
@Composable
fun SugestoesSubfamiliaCard(
    sugestoes: List<SugestaoSubfamilia>,
    viewModel: SubfamiliaViewModel,
    modifier: Modifier = Modifier,
    familiaZeroId: String? = null,
    onDetalhesClick: ((SugestaoSubfamilia) -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    
    // Se não há sugestões, mostrar card com botão de detecção
    if (sugestoes.isEmpty() && familiaZeroId == null) {
        return
    }
    
    if (sugestoes.isEmpty() && familiaZeroId != null) {
        // Card para detectar subfamílias quando não há sugestões
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FamilyRestroom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Detectar Subfamílias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Procure por casais que podem formar novas subfamílias",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Button(
                    onClick = {
                        viewModel.detectarSubfamilias(familiaZeroId)
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detectando...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detectar Agora")
                    }
                }
            }
        }
        return
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Sugestões de Subfamílias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                Badge {
                    Text(text = sugestoes.size.toString())
                }
            }
            
            Text(
                text = "O sistema detectou casais que podem formar novas subfamílias",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
            
            // Lista de sugestões (máximo 3 visíveis)
            val sugestoesVisiveis = sugestoes.take(3)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sugestoesVisiveis.forEach { sugestao ->
                    SugestaoItem(
                        sugestao = sugestao,
                        onAceitar = { viewModel.aceitarSugestao(sugestao) },
                        onRejeitar = { viewModel.rejeitarSugestao(sugestao) },
                        onDetalhes = onDetalhesClick?.let { { it(sugestao) } }
                    )
                }
            }
            
            if (sugestoes.size > 3) {
                Text(
                    text = "+${sugestoes.size - 3} outras sugestões",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Botão para detectar novas subfamílias
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f)
            )
            
            OutlinedButton(
                onClick = { 
                    familiaZeroId?.let { viewModel.detectarSubfamilias(it) }
                },
                enabled = !state.isLoading && familiaZeroId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detectando...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscar Novas Subfamílias")
                }
            }
        }
    }
}

/**
 * Item individual de sugestão
 */
@Composable
private fun SugestaoItem(
    sugestao: SugestaoSubfamilia,
    onAceitar: () -> Unit,
    onRejeitar: () -> Unit,
    onDetalhes: (() -> Unit)? = null
) {
    var mostrarDialogAceitar by remember { mutableStateOf(false) }
    var mostrarDialogRejeitar by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sugestao.nomeSugerido,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${sugestao.membrosIncluidos.size} membros incluídos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (onDetalhes != null) {
                    IconButton(
                        onClick = onDetalhes,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ver detalhes",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { mostrarDialogAceitar = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aceitar")
                }
                
                OutlinedButton(
                    onClick = { mostrarDialogRejeitar = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rejeitar")
                }
            }
        }
    }
    
    // Dialog de confirmação para aceitar
    if (mostrarDialogAceitar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogAceitar = false },
            title = { Text("Aceitar Sugestão?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Deseja criar a subfamília:")
                    Text(
                        text = sugestao.nomeSugerido,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Isso incluirá ${sugestao.membrosIncluidos.size} membros na nova subfamília.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogAceitar = false
                        onAceitar()
                    }
                ) {
                    Text("Aceitar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogAceitar = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Dialog de confirmação para rejeitar
    if (mostrarDialogRejeitar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogRejeitar = false },
            title = { Text("Rejeitar Sugestão?") },
            text = {
                Text("Tem certeza que deseja rejeitar esta sugestão? Ela não será mais exibida.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogRejeitar = false
                        onRejeitar()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rejeitar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogRejeitar = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
