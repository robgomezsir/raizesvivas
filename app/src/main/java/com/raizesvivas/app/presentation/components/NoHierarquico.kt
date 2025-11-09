package com.raizesvivas.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.domain.model.Pessoa
import java.util.Calendar

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
    val colorScheme = MaterialTheme.colorScheme
    val baseElevation = when {
        isRaiz -> 12.dp
        isSelected -> 8.dp
        else -> 4.dp
    }

    val containerColor = remember(isRaiz, isSelected, pessoa.dataFalecimento, colorScheme) {
        when {
            isRaiz -> colorScheme.primaryContainer
            isSelected -> colorScheme.secondaryContainer
            pessoa.dataFalecimento != null -> colorScheme.surfaceVariant.copy(alpha = 0.8f)
            else -> colorScheme.surfaceVariant
        }
    }

    val iconContainerColor = remember(isRaiz, isSelected, pessoa.dataFalecimento, colorScheme) {
        when {
            isRaiz -> colorScheme.primary
            isSelected -> colorScheme.secondary
            pessoa.dataFalecimento != null -> colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else -> colorScheme.surfaceVariant
        }
    }

    val iconContentColor = remember(isRaiz, isSelected, pessoa.dataFalecimento, colorScheme) {
        when {
            isRaiz -> colorScheme.onPrimary
            isSelected -> colorScheme.onSecondary
            pessoa.dataFalecimento != null -> colorScheme.onSurfaceVariant
            else -> colorScheme.onSurfaceVariant
        }
    }

    val idadeOuAno = remember(pessoa.dataNascimento, pessoa.dataFalecimento) {
        pessoa.calcularIdade()?.takeIf { it >= 0 }?.let { idade ->
            "$idade anos"
        } ?: pessoa.dataNascimento?.let { data ->
            Calendar.getInstance().apply { time = data }.get(Calendar.YEAR).toString()
        }
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
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = when {
                    isRaiz -> colorScheme.onPrimaryContainer
                    isSelected -> colorScheme.onSecondaryContainer
                    pessoa.dataFalecimento != null -> colorScheme.onSurfaceVariant
                    else -> colorScheme.onSurfaceVariant
                }
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = baseElevation
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
                        color = colorScheme.primary,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Família Zero",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Ícone
                Surface(
                    modifier = Modifier.size(if (isRaiz) 44.dp else 36.dp),
                    shape = CircleShape,
                    color = iconContainerColor,
                    tonalElevation = if (isRaiz || isSelected) 6.dp else 2.dp,
                    shadowElevation = if (isRaiz || isSelected) 6.dp else 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = iconContentColor,
                            modifier = Modifier.size(if (isRaiz) 28.dp else 22.dp)
                        )
                    }
                }
                
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
                        isRaiz -> colorScheme.onPrimaryContainer
                        isSelected -> colorScheme.onSecondaryContainer
                        else -> colorScheme.onSurface
                    }
                )
                
                // Idade ou ano de nascimento
                idadeOuAno?.let { texto ->
                    Text(
                        text = texto,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isRaiz -> colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            isSelected -> colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                            pessoa.dataFalecimento != null -> colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else -> colorScheme.onSurfaceVariant.copy(alpha = 0.95f)
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Indicador de falecido
                if (pessoa.dataFalecimento != null) {
                    Text(
                        text = "†",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

