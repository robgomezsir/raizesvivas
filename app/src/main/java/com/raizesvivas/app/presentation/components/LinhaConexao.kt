package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Linha conectando dois nós na árvore genealógica
 * 
 * Desenha linha de conexão entre parentesco (pai-filho, mãe-filho, cônjuge-cônjuge)
 */
@Composable
fun LinhaConexao(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    tipo: TipoLinha = TipoLinha.PARENTESCO,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    val color = when (tipo) {
        TipoLinha.PARENTESCO -> primaryColor.copy(alpha = 0.6f)
        TipoLinha.CONJUGE -> secondaryColor.copy(alpha = 0.6f)
    }
    
    val strokeWidth = when (tipo) {
        TipoLinha.PARENTESCO -> 2.dp
        TipoLinha.CONJUGE -> 1.5.dp
    }
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawLine(
            color = color,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Tipos de linha de conexão
 */
enum class TipoLinha {
    PARENTESCO,  // Pai-filho, mãe-filho
    CONJUGE      // Cônjuge-cônjuge
}

