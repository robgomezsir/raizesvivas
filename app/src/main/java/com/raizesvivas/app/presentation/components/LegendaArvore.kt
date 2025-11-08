package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Componente de legenda para a árvore genealógica
 */
@Composable
fun LegendaArvore(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Legenda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Recolher" else "Expandir"
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }
                }
            }
            
            if (expanded) {
                HorizontalDivider()
                
                // Tipos de conexões
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Conexões",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    LegendaItem(
                        cor = Color(0xFF2196F3),
                        texto = "Relação Pai-Filho"
                    )
                    LegendaItem(
                        cor = Color(0xFFE91E63),
                        texto = "Relação Mãe-Filho"
                    )
                    LegendaItem(
                        cor = Color(0xFFFF9800),
                        texto = "Casamento"
                    )
                }
                
                HorizontalDivider()
                
                // Informações de geração
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Gerações",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "• Nível 0: Família Zero (Casal Raiz)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Nível 1: Filhos diretos",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Nível 2: Netos",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Nível 3+: Descendentes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                HorizontalDivider()
                
                // Interações
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Interações",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "• Toque único: Selecionar membro",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Toque duplo: Ver detalhes",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Pinça: Zoom",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Arrastar: Mover visualização",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendaItem(
    cor: Color,
    texto: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(16.dp),
            shape = RoundedCornerShape(2.dp),
            color = cor
        ) {}
        
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

