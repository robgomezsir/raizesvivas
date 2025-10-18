package com.raizesvivas.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raizesvivas.core.domain.model.TreeElement

/**
 * Componente para exibir ícone de elemento da árvore
 * 
 * Representa visualmente o elemento que o membro representa
 * na árvore genealógica (raiz, tronco, galho, folha, etc.)
 */
@Composable
fun TreeElementIcon(
    element: TreeElement,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    showBackground: Boolean = true
) {
    val (icon, color) = when (element) {
        TreeElement.ROOT -> Icons.Default.Nature to Color(0xFF5D4037) // Marrom escuro
        TreeElement.TRUNK -> Icons.Default.AccountTree to Color(0xFF8D6E63) // Marrom médio
        TreeElement.BRANCH -> Icons.Default.Park to Color(0xFFA1887F) // Bege
        TreeElement.LEAF -> Icons.Default.Grass to Color(0xFF8BC34A) // Verde claro
        TreeElement.FLOWER -> Icons.Default.LocalFlorist to Color(0xFFE91E63) // Rosa
        TreeElement.POLLINATOR -> Icons.Default.BugReport to Color(0xFFFFA726) // Laranja
        TreeElement.BIRD -> Icons.Default.Flight to Color(0xFF42A5F5) // Azul
    }
    
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBackground) {
                    Modifier
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f))
                        .border(
                            width = 2.dp,
                            color = color,
                            shape = CircleShape
                        )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = element.description,
            tint = color,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
