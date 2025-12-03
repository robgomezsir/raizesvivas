package com.raizesvivas.app.presentation.screens.arvore

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Desenha linhas curvas conectando membros da família
 * Estilo "chave" para conexões mais intuitivas (Cubic Bezier)
 */
@Composable
fun FamilyTreeConnector(
    startOffset: Offset,
    endOffset: Offset,
    color: Color,
    strokeWidth: Float = 3f,
    curveIntensity: Float = 0.5f // 0.0 a 1.0 - quanto maior, mais curva
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        
        // Ponto inicial
        path.moveTo(startOffset.x, startOffset.y)
        
        // Calcula pontos de controle para curva Bezier
        val midY = (startOffset.y + endOffset.y) / 2
        val controlDistance = (endOffset.x - startOffset.x) * curveIntensity
        
        // Primeira curva (saindo do pai/mãe)
        path.cubicTo(
            startOffset.x + controlDistance, startOffset.y, // controle 1
            startOffset.x + controlDistance, midY,          // controle 2
            (startOffset.x + endOffset.x) / 2, midY         // ponto final da curva
        )
        
        // Segunda curva (chegando ao filho)
        path.cubicTo(
            endOffset.x - controlDistance, midY,            // controle 1
            endOffset.x - controlDistance, endOffset.y,     // controle 2
            endOffset.x, endOffset.y                        // ponto final
        )
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }
}

/**
 * Conector suave com curvas Bezier quadráticas (mais performático)
 * Ideal para árvores com muitos membros (100+)
 */
@Composable
fun SmoothConnector(
    startOffset: Offset,
    endOffset: Offset,
    color: Color,
    strokeWidth: Float = 3f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        path.moveTo(startOffset.x, startOffset.y)
        
        val midY = (startOffset.y + endOffset.y) / 2
        
        // Curva quadrática para melhor performance
        path.quadraticBezierTo(
            startOffset.x, midY,
            (startOffset.x + endOffset.x) / 2, midY
        )
        
        path.quadraticBezierTo(
            endOffset.x, midY,
            endOffset.x, endOffset.y
        )
        
        drawPath(path, color, style = Stroke(width = strokeWidth))
    }
}
