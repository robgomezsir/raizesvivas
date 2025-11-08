package com.raizesvivas.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.domain.model.Pessoa

/**
 * Componente de nó hierárquico para árvore genealógica
 * 
 * Exibe informações da pessoa em um card com botão de expandir/recolher
 * Similar ao TreeNode do exemplo React
 */
@Composable
fun NoHierarquico(
    pessoa: Pessoa,
    @Suppress("UNUSED_PARAMETER") nivel: Int,
    x: Float,
    y: Float,
    temFilhos: Boolean,
    isExpanded: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onDoubleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isRaiz: Boolean = false
) {
    val containerColor = when {
        isRaiz -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        pessoa.dataFalecimento != null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        isRaiz -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    val borderWidth = when {
        isRaiz -> 3.dp
        isSelected -> 2.dp
        else -> 1.dp
    }
    
    Row(
        modifier = modifier
            .offset(x = x.dp, y = y.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleClick?.invoke() }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botão expandir/recolher
        if (temFilhos) {
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Spacer(modifier = Modifier.size(32.dp))
        }
        
        // Card da pessoa
        Card(
            modifier = Modifier
                .size(if (isRaiz) 110.dp else 90.dp)
                .clickable { onClick() }
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = when {
                    isRaiz -> 8.dp
                    isSelected -> 6.dp
                    else -> 4.dp
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isRaiz) 10.dp else 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Badge Família Zero
                if (isRaiz && pessoa.ehFamiliaZero) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Família Zero",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Ícone
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size((if (isRaiz) 40.dp else 32.dp)),
                    tint = when {
                        isRaiz -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                
                Spacer(modifier = Modifier.height(if (isRaiz) 6.dp else 4.dp))
                
                // Nome
                Text(
                    text = pessoa.nome,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isRaiz) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = if (isRaiz) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        isRaiz -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // Idade
                pessoa.dataNascimento?.let { _ ->
                    val anos = pessoa.calcularIdade()
                    if (anos != null && anos > 0) {
                        Text(
                            text = "${anos} anos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Indicador de falecido
                if (pessoa.dataFalecimento != null) {
                    Text(
                        text = "†",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

