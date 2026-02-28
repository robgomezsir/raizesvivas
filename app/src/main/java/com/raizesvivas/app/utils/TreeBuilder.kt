package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.TreeNodeData
import timber.log.Timber

/**
 * Construtor de estrutura de árvore hierárquica dinâmica
 * Converte lista de Pessoa em estrutura TreeNodeData recursiva
 * Busca TODOS os relacionamentos familiares seguindo regras de famílias
 */
object TreeBuilder {
    
    /**
     * Constrói estrutura de árvore a partir do casal Família Zero
     */
    fun buildTree(
        pessoas: List<Pessoa>,
        casalFamiliaZero: Pair<Pessoa?, Pessoa?>,
        nosExpandidos: Set<String> = emptySet()
    ): TreeNodeData? {
        val pessoasMap = pessoas.associateBy { it.id }
        val pessoaRaiz = casalFamiliaZero.first ?: return null
        
        // OTIMIZAÇÃO: Pré-calcular mapa de filhos para busca O(1) durante a recursão
        val childrenMap = mutableMapOf<String, MutableSet<String>>()
        pessoas.forEach { p ->
            if (p.id.isNotBlank()) {
                p.pai?.takeIf { it.isNotBlank() }?.let { paiId ->
                    childrenMap.getOrPut(paiId) { mutableSetOf() }.add(p.id)
                }
                p.mae?.takeIf { it.isNotBlank() }?.let { maeId ->
                    childrenMap.getOrPut(maeId) { mutableSetOf() }.add(p.id)
                }
            }
        }
        
        return buildTreeNode(
            pessoa = pessoaRaiz,
            conjuge = casalFamiliaZero.second,
            pessoasMap = pessoasMap,
            childrenMap = childrenMap,
            nivel = 0,
            nosExpandidos = nosExpandidos,
            caminhoAtual = mutableListOf()
        )
    }
    
    /**
     * Constrói recursivamente um nó da árvore com TODOS os seus relacionamentos
     */
    private fun buildTreeNode(
        pessoa: Pessoa,
        conjuge: Pessoa?,
        pessoasMap: Map<String, Pessoa>,
        childrenMap: Map<String, Set<String>>,
        nivel: Int,
        nosExpandidos: Set<String>,
        caminhoAtual: MutableList<String>
    ): TreeNodeData {
        // Detectar ciclos no caminho
        if (pessoa.id in caminhoAtual) {
            Timber.w("⚠️ Ciclo detectado para pessoa: ${pessoa.id} no caminho: $caminhoAtual")
            return TreeNodeData(pessoa = pessoa, nivel = nivel)
        }
        
        // Adicionar ao caminho atual (usando lista para preservar ordem e detectar ciclos)
        caminhoAtual.add(pessoa.id)
        
        // Coletar IDs de filhos usando o mapa pré-calculado O(1)
        val filhosIds = mutableSetOf<String>()
        
        // 1. Filhos declarados no objeto Pessoa (mantendo compatibilidade)
        pessoa.filhos.filter { it.isNotBlank() && it in pessoasMap }.forEach { filhosIds.add(it) }
        conjuge?.let { c ->
            c.filhos.filter { it.isNotBlank() && it in pessoasMap }.forEach { filhosIds.add(it) }
        }
        
        // 2. Filhos que apontam para esta pessoa/conjuge (via mapa pré-calculado)
        childrenMap[pessoa.id]?.let { filhosIds.addAll(it) }
        conjuge?.id?.let { conjugeId ->
            childrenMap[conjugeId]?.let { filhosIds.addAll(it) }
        }
        
        // Construir nós filhos recursivamente
        val children = if (filhosIds.isEmpty()) {
            emptyList()
        } else {
            filhosIds.mapNotNull { filhoId ->
                val filho = pessoasMap[filhoId] ?: return@mapNotNull null
                
                val conjugeFilho = filho.conjugeAtual?.takeIf { 
                    it.isNotBlank() && it in pessoasMap 
                }?.let { pessoasMap[it] }
                
                buildTreeNode(
                    pessoa = filho,
                    conjuge = conjugeFilho,
                    pessoasMap = pessoasMap,
                    childrenMap = childrenMap,
                    nivel = nivel + 1,
                    nosExpandidos = nosExpandidos,
                    caminhoAtual = caminhoAtual
                )
            }
        }
        
        // Remover do caminho após processar filhos (backtracking)
        caminhoAtual.removeAt(caminhoAtual.size - 1)
        
        val isExpanded = nosExpandidos.isEmpty() || nosExpandidos.contains(pessoa.id) || nivel == 0
        
        val temRelacionamentoValido = conjuge != null && (
            nivel == 0 || 
            conjuge.conjugeAtual == pessoa.id || 
            pessoa.conjugeAtual == conjuge.id
        )
        
        return TreeNodeData(
            pessoa = pessoa,
            conjuge = if (temRelacionamentoValido) conjuge else null,
            children = children,
            isExpanded = isExpanded,
            nivel = nivel
        )
    }
}

