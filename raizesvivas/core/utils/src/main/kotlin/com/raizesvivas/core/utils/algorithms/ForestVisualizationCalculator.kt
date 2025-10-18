package com.raizesvivas.core.utils.algorithms

import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.model.UserPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Calculadora de visualização da floresta
 * 
 * Calcula posições e elementos visuais para a floresta
 * de famílias no sistema Raízes Vivas.
 */
class ForestVisualizationCalculator {
    
    data class ForestTree(
        val family: Family,
        val userPoints: UserPoints,
        var x: Float = 0f,
        var y: Float = 0f,
        var size: Float = 0f,
        var color: String = "#4CAF50"
    )
    
    data class ForestLayout(
        val trees: List<ForestTree>,
        val width: Float,
        val height: Float
    )
    
    companion object {
        private const val BASE_TREE_SIZE = 50f
        private const val MAX_TREE_SIZE = 200f
        private const val MIN_TREE_SIZE = 30f
        private const val FOREST_WIDTH = 1000f
        private const val FOREST_HEIGHT = 800f
        private const val MIN_DISTANCE = 100f
    }
    
    /**
     * Calcula o layout da floresta
     */
    suspend fun calculateForestLayout(
        families: List<Family>,
        userPointsList: List<UserPoints>
    ): ForestLayout = withContext(Dispatchers.Default) {
        try {
            if (families.isEmpty()) {
                return@withContext ForestLayout(emptyList(), FOREST_WIDTH, FOREST_HEIGHT)
            }
            
            // Criar árvores da floresta
            val trees = createForestTrees(families, userPointsList)
            
            // Calcular posições
            calculateTreePositions(trees)
            
            ForestLayout(trees, FOREST_WIDTH, FOREST_HEIGHT)
            
        } catch (e: Exception) {
            ForestLayout(emptyList(), FOREST_WIDTH, FOREST_HEIGHT)
        }
    }
    
    /**
     * Cria árvores da floresta
     */
    private fun createForestTrees(
        families: List<Family>,
        userPointsList: List<UserPoints>
    ): List<ForestTree> {
        val userPointsMap = userPointsList.associateBy { it.userId }
        
        return families.map { family ->
            val userPoints = userPointsMap[family.userId] ?: createDefaultUserPoints(family.userId)
            
            ForestTree(
                family = family,
                userPoints = userPoints,
                size = calculateTreeSize(userPoints),
                color = calculateTreeColor(userPoints)
            )
        }
    }
    
    /**
     * Calcula o tamanho da árvore baseado nos pontos
     */
    private fun calculateTreeSize(userPoints: UserPoints): Float {
        val sizeMultiplier = (userPoints.pontosTotais / 100f).coerceIn(0.5f, 3f)
        return (BASE_TREE_SIZE * sizeMultiplier).coerceIn(MIN_TREE_SIZE, MAX_TREE_SIZE)
    }
    
    /**
     * Calcula a cor da árvore baseada no nível
     */
    private fun calculateTreeColor(userPoints: UserPoints): String {
        return when {
            userPoints.nivelAtual >= 10 -> "#4CAF50" // Verde
            userPoints.nivelAtual >= 7 -> "#8BC34A" // Verde claro
            userPoints.nivelAtual >= 5 -> "#FFC107" // Amarelo
            userPoints.nivelAtual >= 3 -> "#FF9800" // Laranja
            else -> "#FF5722" // Vermelho
        }
    }
    
    /**
     * Calcula posições das árvores
     */
    private fun calculateTreePositions(trees: List<ForestTree>) {
        if (trees.isEmpty()) return
        
        // Ordenar árvores por pontos (maior para menor)
        val sortedTrees = trees.sortedByDescending { it.userPoints.pontosTotais }
        
        // Posicionar árvore principal no centro
        if (sortedTrees.isNotEmpty()) {
            val mainTree = sortedTrees[0]
            mainTree.x = FOREST_WIDTH / 2
            mainTree.y = FOREST_HEIGHT / 2
        }
        
        // Posicionar outras árvores em círculo
        if (sortedTrees.size > 1) {
            val radius = minOf(FOREST_WIDTH, FOREST_HEIGHT) * 0.3f
            val angleStep = 2 * Math.PI / (sortedTrees.size - 1)
            
            for (i in 1 until sortedTrees.size) {
                val tree = sortedTrees[i]
                val angle = angleStep * i
                
                tree.x = (FOREST_WIDTH / 2 + radius * cos(angle)).toFloat()
                tree.y = (FOREST_HEIGHT / 2 + radius * sin(angle)).toFloat()
            }
        }
        
        // Ajustar posições para evitar sobreposição
        adjustPositionsToAvoidOverlap(trees)
    }
    
    /**
     * Ajusta posições para evitar sobreposição
     */
    private fun adjustPositionsToAvoidOverlap(trees: List<ForestTree>) {
        for (i in trees.indices) {
            for (j in i + 1 until trees.size) {
                val tree1 = trees[i]
                val tree2 = trees[j]
                
                val distance = sqrt(
                    (tree1.x - tree2.x).pow(2) + (tree1.y - tree2.y).pow(2)
                )
                
                if (distance < MIN_DISTANCE) {
                    val direction = atan2(
                        (tree1.y - tree2.y).toDouble(),
                        (tree1.x - tree2.x).toDouble()
                    )
                    
                    val newX1 = tree1.x + (MIN_DISTANCE / 2 * cos(direction)).toFloat()
                    val newY1 = tree1.y + (MIN_DISTANCE / 2 * sin(direction)).toFloat()
                    val newX2 = tree2.x - (MIN_DISTANCE / 2 * cos(direction)).toFloat()
                    val newY2 = tree2.y - (MIN_DISTANCE / 2 * sin(direction)).toFloat()
                    
                    // Verificar se as novas posições estão dentro dos limites
                    if (newX1 in 0f..FOREST_WIDTH && newY1 in 0f..FOREST_HEIGHT) {
                        tree1.x = newX1
                        tree1.y = newY1
                    }
                    
                    if (newX2 in 0f..FOREST_WIDTH && newY2 in 0f..FOREST_HEIGHT) {
                        tree2.x = newX2
                        tree2.y = newY2
                    }
                }
            }
        }
    }
    
    /**
     * Cria pontos padrão para usuários sem pontos
     */
    private fun createDefaultUserPoints(userId: String): UserPoints {
        return UserPoints(
            id = userId,
            userId = userId,
            pontosTotais = 0,
            nivelAtual = 1,
            pontosProximoNivel = 100,
            conquistasConquistadas = 0,
            ultimaAtualizacao = java.time.LocalDateTime.now(),
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
    }
}
