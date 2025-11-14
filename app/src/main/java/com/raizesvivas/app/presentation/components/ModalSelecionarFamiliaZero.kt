package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.Pessoa

/**
 * Modal para selecionar a Família Zero (pai e mãe)
 */
@Composable
fun ModalSelecionarFamiliaZero(
    pessoas: List<Pessoa>,
    onDismiss: () -> Unit,
    onConfirmar: (paiId: String, maeId: String) -> Unit
) {
    var paiSelecionado by remember { mutableStateOf<Pessoa?>(null) }
    var maeSelecionada by remember { mutableStateOf<Pessoa?>(null) }
    var mostrarConfirmacaoFinal by remember { mutableStateOf(false) }
    
    // Criar mapa de TODAS as pessoas para a função agruparPessoasPorFamilias
    // Usar TODAS as pessoas para identificar os casais (mesmo que tenham pais)
    // Isso garante que os mesmos casais apareçam no modal e na árvore
    val pessoasMap = remember(pessoas) {
        pessoas.associateBy { it.id }
    }
    
    // Usar a MESMA função da árvore para identificar casais
    // Passar TODAS as pessoas para garantir que todos os casais sejam encontrados
    val familias = remember(pessoas, pessoasMap) {
        agruparPessoasPorFamilias(pessoas, pessoasMap)
    }
    
    // Filtrar apenas famílias que são casais (tem ambos os cônjuges) e não são Família Zero
    // No modal, queremos mostrar TODOS os casais disponíveis, incluindo a Família Zero atual
    // para permitir redefinição
    val casaisDisponiveis = remember(familias) {
        familias.mapNotNull { familia ->
            val conjugue1 = familia.conjugue1
            val conjugue2 = familia.conjugue2
            
            if (conjugue1 != null && conjugue2 != null) {
                // Identificar pai e mãe pelo gênero (mesma lógica da árvore)
                val pai = when {
                    conjugue1.genero == Genero.MASCULINO -> conjugue1
                    conjugue2.genero == Genero.MASCULINO -> conjugue2
                    else -> conjugue1 // Fallback: primeiro como pai
                }
                val mae = when {
                    conjugue2.genero == Genero.FEMININO -> conjugue2
                    conjugue1.genero == Genero.FEMININO -> conjugue1
                    else -> conjugue2 // Fallback: segundo como mãe
                }
                
                Pair(pai, mae)
            } else {
                null
            }
        }
    }
    
    // Modal deve mostrar APENAS casais - não permitir seleção de pessoas solteiras
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cabeçalho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selecionar Família Zero",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Instruções
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Selecione um casal para ser a Família Zero",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = "A Família Zero deve ser um casal (pai e mãe) sem pais cadastrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Lista de casais e pessoas solteiras
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Casal selecionado (dentro da lista para scroll)
                    if (paiSelecionado != null && maeSelecionada != null) {
                        item {
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
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = paiSelecionado!!.getNomeExibicao(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "&",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = maeSelecionada!!.getNomeExibicao(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(onClick = { 
                                        paiSelecionado = null
                                        maeSelecionada = null
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remover")
                                    }
                                }
                            }
                        }
                    }
                    // Seção de casais disponíveis
                    if (casaisDisponiveis.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Casais Disponíveis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = "${casaisDisponiveis.size} casal(is)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        items(
                            items = casaisDisponiveis,
                            key = { casal -> "${casal.first.id}_${casal.second.id}" } // Key estável para otimização
                        ) { casal ->
                            val pai = casal.first
                            val mae = casal.second
                            val isSelected = paiSelecionado?.id == pai.id && 
                                           maeSelecionada?.id == mae.id
                            
                            // Criar FamiliaGrupo temporário para usar obterNomeFamilia
                            val familiaGrupo = FamiliaGrupo(
                                id = pai.id,
                                conjugue1 = pai,
                                conjugue2 = mae,
                                filhos = emptyList(),
                                ehFamiliaZero = false,
                                ehFamiliaMonoparental = false,
                                ehFamiliaReconstituida = false,
                                conjugueAnterior = null,
                                familiaAnteriorId = null,
                                tipoNucleoFamiliar = com.raizesvivas.app.domain.model.TipoNucleoFamiliar.PARENTESCO,
                                parentesColaterais = emptyMap()
                            )
                            val nomeFamilia = obterNomeFamilia(familiaGrupo)
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        paiSelecionado = pai
                                        maeSelecionada = mae
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 6.dp else 2.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Nome do casal (mesmo formato da árvore)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = nomeFamilia,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Selecionado",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Card do Pai
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(90.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = pai.getNomeExibicao(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    maxLines = 2,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                pai.dataNascimento?.let {
                                                    val idade = pai.calcularIdade()
                                                    if (idade != null && idade > 0) {
                                                        Text(
                                                            text = "$idade anos",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Ícone de conexão
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        
                                        // Card da Mãe
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(90.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.secondary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = mae.getNomeExibicao(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    maxLines = 2,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                mae.dataNascimento?.let {
                                                    val idade = mae.calcularIdade()
                                                    if (idade != null && idade > 0) {
                                                        Text(
                                                            text = "$idade anos",
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
                        }
                    }
                    
                    // Mensagem quando não há casais disponíveis
                    if (casaisDisponiveis.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Nenhum casal disponível",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = "Cadastre casais sem pais cadastrados primeiro.\nPara formar um casal, vincule pessoas como cônjuges no perfil de cada pessoa.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Botões de ação (fixos na parte inferior)
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            if (paiSelecionado != null && maeSelecionada != null) {
                                mostrarConfirmacaoFinal = true
                            }
                        },
                        enabled = paiSelecionado != null && maeSelecionada != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmação final
    if (mostrarConfirmacaoFinal && paiSelecionado != null && maeSelecionada != null) {
        val pai = paiSelecionado!!
        val mae = maeSelecionada!!
        
        @Suppress("UNUSED_VALUE")
        fun fecharDialogoConfirmacao() {
            mostrarConfirmacaoFinal = false
        }
        
        AlertDialog(
            onDismissRequest = { fecharDialogoConfirmacao() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Aviso",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "⚠️ Confirmação Final",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Esta ação é IRREVERSÍVEL e afetará toda a árvore genealógica!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Text(
                        text = "Você está definindo a Família Zero (raiz da árvore) com:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "👨 Pai: ${pai.getNomeExibicao()}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                            Text(
                                text = "👩 Mãe: ${mae.getNomeExibicao()}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        }
                    }
                    
                    Text(
                        text = "• Todos os cálculos de parentesco serão baseados nesta família\n• A distância à Família Zero será recalculada para todas as pessoas\n• Esta ação só pode ser realizada por administradores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Tem CERTEZA que deseja continuar?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmar(pai.id, mae.id)
                        fecharDialogoConfirmacao()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "SIM, CONFIRMAR",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { fecharDialogoConfirmacao() }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

