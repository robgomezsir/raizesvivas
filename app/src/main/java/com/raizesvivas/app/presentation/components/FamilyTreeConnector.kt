package com.raizesvivas.app.presentation.components

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
 * Estilo "chave" para conexões mais intuitivas
 * 
 * @param startOffset Ponto inicial (pai/mãe)
 * @param endOffset Ponto final (filho)
 * @param color Cor da linha
 * @param strokeWidth Largura da linha
 * @param curveIntensity Intensidade da curva (0.0 a 1.0) - quanto maior, mais curva
 */
@Composable
fun FamilyTreeConnector(
    startOffset: Offset,
    endOffset: Offset,
    color: Color,
    strokeWidth: Float = 3f,
    curveIntensity: Float = 0.4f // 0.0 a 1.0 - quanto maior, mais curva
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        
        // Ponto inicial
        path.moveTo(startOffset.x, startOffset.y)
        
        // Calcula pontos de controle para curva Bézier
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
            style = Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

/**
 * Conector suave com curvas Bézier quadráticas (mais performático)
 * Use este para árvores com 100+ membros
 * 
 * @param startOffset Ponto inicial
 * @param endOffset Ponto final
 * @param color Cor da linha
 * @param strokeWidth Largura da linha
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
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

/**
 * Componente para conexões hierárquicas com múltiplas ramificações
 * Um pai/mãe conectando a vários filhos
 * 
 * @param parentOffset Posição do pai/mãe
 * @param childrenOffsets Lista de posições dos filhos
 * @param isMaternal Se true, usa cor verde (materna), senão laranja (paterna)
 * @param strokeWidth Largura da linha
 */
@Composable
fun HierarchicalConnector(
    parentOffset: Offset,
    childrenOffsets: List<Offset>,
    isMaternal: Boolean = true,
    strokeWidth: Float = 3f,
    lineColor: Color? = null
) {
    val color = lineColor ?: if (isMaternal) 
        Color(0xFF67EF1F) // Color1 - Verde claro para linhagem materna
    else 
        Color(0xFF34C910) // Color3 - Verde médio para linhagem paterna
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Linha vertical do pai até o ponto de distribuição
        val distributionY = parentOffset.y + 60.dp.toPx()
        val path = Path()
        
        // Curva inicial do pai
        path.moveTo(parentOffset.x, parentOffset.y)
        path.cubicTo(
            parentOffset.x, parentOffset.y + 20.dp.toPx(),
            parentOffset.x, distributionY - 20.dp.toPx(),
            parentOffset.x, distributionY
        )
        
        drawPath(
            path, 
            color, 
            style = Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
        
        // Linhas curvas para cada filho
        childrenOffsets.forEach { childOffset ->
            val childPath = Path()
            childPath.moveTo(parentOffset.x, distributionY)
            
            // Curva estilo "chave"
            val midX = (parentOffset.x + childOffset.x) / 2
            childPath.cubicTo(
                parentOffset.x, distributionY + 20.dp.toPx(),
                midX - 20.dp.toPx(), (distributionY + childOffset.y) / 2,
                midX, (distributionY + childOffset.y) / 2
            )
            childPath.cubicTo(
                midX + 20.dp.toPx(), (distributionY + childOffset.y) / 2,
                childOffset.x, childOffset.y - 20.dp.toPx(),
                childOffset.x, childOffset.y
            )
            
            drawPath(
                childPath, 
                color, 
                style = Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}

/**
 * Alternativa: Connector com linha horizontal no meio (estilo clássico)
 * Mais simples e performático, mas menos visual
 */
@Composable
fun ClassicConnector(
    parentOffset: Offset,
    childOffset: Offset,
    color: Color,
    strokeWidth: Float = 3f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path()
        val midY = (parentOffset.y + childOffset.y) / 2
        
        // Linha vertical do pai
        path.moveTo(parentOffset.x, parentOffset.y)
        path.lineTo(parentOffset.x, midY)
        
        // Linha horizontal
        path.lineTo(childOffset.x, midY)
        
        // Linha vertical até o filho
        path.lineTo(childOffset.x, childOffset.y)
        
        drawPath(
            path, 
            color, 
            style = Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

