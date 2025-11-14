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
        
        return buildTreeNode(
            pessoa = pessoaRaiz,
            conjuge = casalFamiliaZero.second,
            pessoasMap = pessoasMap,
            nivel = 0,
            nosExpandidos = nosExpandidos,
            visitados = mutableSetOf(),
            caminhoAtual = mutableListOf()
        )
    }
    
    /**
     * Constrói recursivamente um nó da árvore com TODOS os seus relacionamentos
     * Busca dinamicamente todos os familiares seguindo regras de famílias
     */
    private fun buildTreeNode(
        pessoa: Pessoa,
        conjuge: Pessoa?,
        pessoasMap: Map<String, Pessoa>,
        nivel: Int,
        nosExpandidos: Set<String>,
        visitados: MutableSet<String>,
        caminhoAtual: MutableList<String>
    ): TreeNodeData {
        // Detectar ciclos no caminho
        if (pessoa.id in caminhoAtual) {
            Timber.w("⚠️ Ciclo detectado para pessoa: ${pessoa.id} no caminho: $caminhoAtual")
            return TreeNodeData(pessoa = pessoa, nivel = nivel)
        }
        
        // Evitar processar a mesma pessoa múltiplas vezes no mesmo ramo
        // Mas permitir que apareça em ramos diferentes (ex: avô que aparece em múltiplas famílias)
        val visitadosNesteRamo = visitados.toMutableSet()
        visitadosNesteRamo.add(pessoa.id)
        val novoCaminho = caminhoAtual.toMutableList()
        novoCaminho.add(pessoa.id)
        
        // Coletar TODOS os filhos diretos e indiretos
        val filhosIds = mutableSetOf<String>()
        
        // Filhos diretos da pessoa (filtrar apenas IDs que existem no mapa)
        pessoa.filhos.filter { it.isNotBlank() && it in pessoasMap }.forEach { filhosIds.add(it) }
        
        // Se há cônjuge, incluir filhos do cônjuge também (filtrar apenas IDs que existem)
        conjuge?.let { c ->
            c.filhos.filter { it.isNotBlank() && it in pessoasMap }.forEach { filhosIds.add(it) }
        }
        
        // Buscar filhos adicionais através do cônjuge (casos onde o cônjuge tem filhos de relacionamento anterior)
        conjuge?.let { c ->
            // Se o cônjuge tem filhos que não estão na lista de filhos da pessoa atual
            // mas são filhos biológicos do cônjuge, incluir também
            pessoasMap.values.forEach { pessoaCadastrada ->
                if ((pessoaCadastrada.pai == c.id || pessoaCadastrada.mae == c.id) && 
                    pessoaCadastrada.id.isNotBlank() && 
                    pessoaCadastrada.id in pessoasMap) {
                    filhosIds.add(pessoaCadastrada.id)
                }
            }
        }
        
        // Construir nós filhos recursivamente (agora todos os IDs já foram validados)
        val children = filhosIds.mapNotNull { filhoId ->
            val filho = pessoasMap[filhoId]
            if (filho != null) {
                // Encontrar cônjuge do filho se existir
                val conjugeFilho = filho.conjugeAtual?.takeIf { 
                    it.isNotBlank() && it in pessoasMap 
                }?.let { pessoasMap[it] }
                
                // Verificar se já foi visitado neste ramo específico
                val visitadosFilhos = if (filho.id in visitadosNesteRamo) {
                    // Se já foi visitado, criar novo set para permitir aparecer em outro ramo
                    mutableSetOf<String>()
                } else {
                    visitadosNesteRamo.toMutableSet()
                }
                
                buildTreeNode(
                    pessoa = filho,
                    conjuge = conjugeFilho,
                    pessoasMap = pessoasMap,
                    nivel = nivel + 1,
                    nosExpandidos = nosExpandidos,
                    visitados = visitadosFilhos,
                    caminhoAtual = novoCaminho.toMutableList()
                )
            } else {
                // Este caso não deveria acontecer mais, mas mantemos como fallback
                Timber.w("⚠️ Filho não encontrado após validação: $filhoId")
                null
            }
        }
        
        // Determinar se está expandido
        val isExpanded = nosExpandidos.isEmpty() || nosExpandidos.contains(pessoa.id) || nivel == 0
        
        // Mostrar casal em TODOS os níveis quando houver relacionamento válido
        // Verificar se há cônjuge e se é um relacionamento válido (bidirecional)
        val temRelacionamentoValido = conjuge != null && (
            nivel == 0 || // Sempre mostrar na raiz
            conjuge.conjugeAtual == pessoa.id || // Cônjuge aponta para pessoa
            pessoa.conjugeAtual == conjuge.id    // Pessoa aponta para cônjuge
        )
        
        val mostrarConjuge = temRelacionamentoValido
        
        return TreeNodeData(
            pessoa = pessoa,
            conjuge = if (mostrarConjuge) conjuge else null,
            children = children,
            isExpanded = isExpanded,
            nivel = nivel
        )
    }
}

