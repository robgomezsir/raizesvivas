package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.domain.model.Pessoa
import kotlin.math.abs

/**
 * Posição de um nó na tela
 */
data class NodePosition(
    val x: Float = 0f,
    val y: Float = 0f
)

/**
 * Canvas para desenhar conexões entre nós da árvore
 * Baseado no exemplo tree-hierarchy-ui.tsx
 */
@Composable
fun ConnectionCanvas(
    nodePositions: Map<String, NodePosition>,
    treeData: TreeNodeData,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") editMode: Boolean = false,
    zoomState: ZoomState? = null
) {
    // Observar mudanças no zoom/pan e posições para forçar recomposição
    @Suppress("UNUSED_VARIABLE")
    val scale = zoomState?.scale ?: 1f
    @Suppress("UNUSED_VARIABLE")
    val offsetX = zoomState?.offsetX ?: 0f
    @Suppress("UNUSED_VARIABLE")
    val offsetY = zoomState?.offsetY ?: 0f
    
    // Usar key baseado nas posições e zoom para forçar recomposição quando necessário
    @Suppress("UNUSED_VARIABLE")
    val positionsKey = remember(nodePositions.keys.sorted().joinToString()) {
        nodePositions.keys.sorted().joinToString()
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        fun drawConnections(node: TreeNodeData, parentPos: NodePosition?) {
            // Usar posição da pessoa principal ou do cônjuge se existir
            val nodePos = nodePositions[node.pessoa.id] 
                ?: node.conjuge?.let { nodePositions[it.id] }
            
            if (parentPos != null && nodePos != null) {
                // As posições já estão em coordenadas relativas ao Canvas
                // que está na mesma camada transformada pelo ZoomableBox
                // Isso garante que as linhas se movem junto com os cards
                val startX = parentPos.x
                val startY = parentPos.y
                val endX = nodePos.x
                val endY = nodePos.y
                
                // Calcular ponto de controle para curva suave
                val controlPointOffset = abs(endY - startY) * 0.4f
                
                val path = Path().apply {
                    moveTo(startX, startY)
                    cubicTo(
                        x1 = startX,
                        y1 = startY + controlPointOffset,
                        x2 = endX,
                        y2 = endY - controlPointOffset,
                        x3 = endX,
                        y3 = endY
                    )
                }
                
                // Ajustar espessura da linha baseado no zoom para manter visibilidade
                val currentScale = zoomState?.scale ?: 1f
                val lineWidth = (6f / currentScale).coerceIn(2f, 8f)
                
                drawPath(
                    path = path,
                    color = Color(0xFF64B5F6),
                    style = Stroke(
                        width = lineWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }
            
            // Desenhar conexões para filhos se o nó estiver expandido
            if (node.isExpanded) {
                // Usar posição da pessoa principal ou do cônjuge
                val currentNodePos = nodePositions[node.pessoa.id] 
                    ?: node.conjuge?.let { nodePositions[it.id] }
                
                node.children.forEach { child ->
                    if (currentNodePos != null) {
                        drawConnections(child, currentNodePos)
                    }
                }
            }
        }
        
        // Desenhar conexões começando da raiz
        // Se for casal, usar posição da primeira pessoa ou do cônjuge
        val rootPos = nodePositions[treeData.pessoa.id] 
            ?: treeData.conjuge?.let { nodePositions[it.id] }
        
        if (rootPos != null) {
            treeData.children.forEach { child ->
                drawConnections(child, rootPos)
            }
        }
    }
}

