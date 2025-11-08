package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
 * Componente representando um nó (pessoa) na árvore genealógica
 * 
 * Exibe informações da pessoa em um card circular/retangular
 * Com destaque especial para o casal Família Zero no centro
 */
@Composable
fun NoPessoa(
    pessoa: Pessoa,
    onClick: () -> Unit = {},
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isCentral: Boolean = false,
    isFamiliaZero: Boolean = false,
    size: Float = 100f // Tamanho do nó em dp
) {
    // Cores especiais para Família Zero
    val containerColor = when {
        isFamiliaZero && isCentral -> {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        }
        isSelected -> {
            MaterialTheme.colorScheme.secondaryContainer
        }
        pessoa.dataFalecimento != null -> {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        }
        isCentral -> {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
        }
        else -> {
            MaterialTheme.colorScheme.surface
        }
    }
    
    val borderColor = when {
        isFamiliaZero && isCentral -> {
            MaterialTheme.colorScheme.primary
        }
        isSelected -> {
            MaterialTheme.colorScheme.secondary
        }
        isCentral -> {
            MaterialTheme.colorScheme.tertiary
        }
        else -> {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        }
    }
    
    val borderWidth = when {
        isFamiliaZero && isCentral -> 4.dp
        isCentral -> 3.dp
        isSelected -> 2.dp
        else -> 1.dp
    }
    
    Card(
        modifier = modifier
            .size(size.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleClick?.invoke() },
                    onLongPress = { onLongClick?.invoke() }
                )
            }
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(if (isCentral) 16.dp else 12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(if (isCentral) 16.dp else 12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isFamiliaZero && isCentral -> 12.dp
                isCentral -> 8.dp
                isSelected -> 6.dp
                else -> 4.dp
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCentral) 10.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Badge Família Zero
            if (isFamiliaZero && isCentral) {
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
            
            // Foto ou ícone
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size((size * if (isCentral) 0.45f else 0.4f).dp),
                tint = when {
                    isFamiliaZero && isCentral -> MaterialTheme.colorScheme.primary
                    isCentral -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(if (isCentral) 6.dp else 4.dp))
            
            // Nome
            Text(
                text = pessoa.nome,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isCentral) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = if (isCentral) 3 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    isFamiliaZero && isCentral -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            // Data de nascimento (se disponível)
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

