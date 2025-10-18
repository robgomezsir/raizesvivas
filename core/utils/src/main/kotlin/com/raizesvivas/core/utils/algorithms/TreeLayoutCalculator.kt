package com.raizesvivas.core.utils.algorithms

import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.domain.model.RelationshipType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.pow

/**
 * Calculadora de layout para árvore genealógica
 * 
 * Calcula posições X e Y para cada membro na visualização
 * da árvore genealógica.
 */
class TreeLayoutCalculator {
    
    data class TreeNode(
        val member: Member,
        val children: MutableList<TreeNode> = mutableListOf(),
        var x: Float = 0f,
        var y: Float = 0f,
        var width: Float = 0f
    )
    
    data class TreeLayout(
        val nodes: List<TreeNode>,
        val width: Float,
        val height: Float
    )
    
    companion object {
        private const val NODE_WIDTH = 120f
        private const val NODE_HEIGHT = 80f
        private const val HORIZONTAL_SPACING = 20f
        private const val VERTICAL_SPACING = 40f
    }
    
    /**
     * Calcula o layout da árvore genealógica
     */
    suspend fun calculateLayout(
        members: List<Member>,
        relationships: List<Relationship>,
        rootMemberId: String? = null
    ): TreeLayout = withContext(Dispatchers.Default) {
        try {
            // Construir árvore
            val tree = buildTree(members, relationships, rootMemberId)
            
            if (tree.isEmpty()) {
                return@withContext TreeLayout(emptyList(), 0f, 0f)
            }
            
            // Calcular posições
            calculatePositions(tree)
            
            // Calcular dimensões totais
            val width = calculateTreeWidth(tree)
            val height = calculateTreeHeight(tree)
            
            TreeLayout(tree, width, height)
            
        } catch (e: Exception) {
            TreeLayout(emptyList(), 0f, 0f)
        }
    }
    
    /**
     * Constrói a árvore a partir dos membros e relacionamentos
     */
    private fun buildTree(
        members: List<Member>,
        relationships: List<Relationship>,
        rootMemberId: String?
    ): List<TreeNode> {
        val memberMap = members.associateBy { it.id }
        val relationshipMap = relationships.groupBy { it.membro1Id }
        
        // Encontrar raiz da árvore
        val rootId = rootMemberId ?: findRootMember(members, relationships)
        
        if (rootId == null) {
            return emptyList()
        }
        
        val rootMember = memberMap[rootId] ?: return emptyList()
        val rootNode = TreeNode(rootMember)
        
        // Construir árvore recursivamente
        buildSubtree(rootNode, relationshipMap, memberMap)
        
        return listOf(rootNode)
    }
    
    /**
     * Encontra o membro raiz da árvore
     */
    private fun findRootMember(
        members: List<Member>,
        relationships: List<Relationship>
    ): String? {
        // Buscar membro que não tem pais
        val children = relationships
            .filter { it.tipoRelacionamento == RelationshipType.PAI || it.tipoRelacionamento == RelationshipType.MAE }
            .map { it.membro2Id }
            .toSet()
        
        return members.find { it.id !in children }?.id
    }
    
    /**
     * Constrói subárvore recursivamente
     */
    private fun buildSubtree(
        node: TreeNode,
        relationshipMap: Map<String, List<Relationship>>,
        memberMap: Map<String, Member>
    ) {
        val children = relationshipMap[node.member.id]?.filter { relationship ->
            relationship.tipoRelacionamento == RelationshipType.FILHO || 
            relationship.tipoRelacionamento == RelationshipType.FILHA
        } ?: emptyList()
        
        children.forEach { relationship ->
            val childMember = memberMap[relationship.membro2Id]
            if (childMember != null) {
                val childNode = TreeNode(childMember)
                node.children.add(childNode)
                buildSubtree(childNode, relationshipMap, memberMap)
            }
        }
    }
    
    /**
     * Calcula posições dos nós
     */
    private fun calculatePositions(nodes: List<TreeNode>) {
        nodes.forEach { node ->
            calculateNodePositions(node, 0f, 0f)
        }
    }
    
    /**
     * Calcula posições de um nó e seus filhos
     */
    private fun calculateNodePositions(
        node: TreeNode,
        parentX: Float,
        parentY: Float
    ) {
        // Posicionar nó atual
        node.x = parentX
        node.y = parentY
        node.width = NODE_WIDTH
        
        if (node.children.isEmpty()) {
            return
        }
        
        // Calcular largura total dos filhos
        val childrenWidth = calculateChildrenWidth(node.children)
        
        // Posicionar filhos
        var currentX = parentX - childrenWidth / 2 + NODE_WIDTH / 2
        
        node.children.forEach { child ->
            calculateNodePositions(
                child,
                currentX,
                parentY + NODE_HEIGHT + VERTICAL_SPACING
            )
            currentX += child.width + HORIZONTAL_SPACING
        }
    }
    
    /**
     * Calcula largura total dos filhos
     */
    private fun calculateChildrenWidth(children: List<TreeNode>): Float {
        if (children.isEmpty()) return 0f
        
        return children.sumOf { it.width.toDouble() }.toFloat() + 
               (children.size - 1) * HORIZONTAL_SPACING
    }
    
    /**
     * Calcula largura total da árvore
     */
    private fun calculateTreeWidth(nodes: List<TreeNode>): Float {
        if (nodes.isEmpty()) return 0f
        
        val minX = nodes.minOfOrNull { it.x } ?: 0f
        val maxX = nodes.maxOfOrNull { it.x + it.width } ?: 0f
        
        return maxX - minX
    }
    
    /**
     * Calcula altura total da árvore
     */
    private fun calculateTreeHeight(nodes: List<TreeNode>): Float {
        if (nodes.isEmpty()) return 0f
        
        val maxY = nodes.maxOfOrNull { it.y + NODE_HEIGHT } ?: 0f
        return maxY
    }
    
    /**
     * Calcula nível de um membro na árvore
     */
    fun calculateMemberLevel(
        memberId: String,
        members: List<Member>,
        relationships: List<Relationship>
    ): Int {
        val rootId = findRootMember(members, relationships) ?: return 0
        
        if (memberId == rootId) return 0
        
        // Buscar caminho até a raiz
        val path = findPathToRoot(memberId, rootId, relationships)
        return path.size - 1
    }
    
    /**
     * Encontra caminho de um membro até a raiz
     */
    private fun findPathToRoot(
        memberId: String,
        rootId: String,
        relationships: List<Relationship>
    ): List<String> {
        val path = mutableListOf<String>()
        var currentId = memberId
        
        while (currentId != rootId && path.size < 10) { // Limite para evitar loops
            path.add(currentId)
            
            val parent = relationships.find { 
                it.membro2Id == currentId && 
                (it.tipoRelacionamento == RelationshipType.PAI || it.tipoRelacionamento == RelationshipType.MAE)
            }
            
            if (parent != null) {
                currentId = parent.membro1Id
            } else {
                break
            }
        }
        
        if (currentId == rootId) {
            path.add(rootId)
        }
        
        return path
    }
}
