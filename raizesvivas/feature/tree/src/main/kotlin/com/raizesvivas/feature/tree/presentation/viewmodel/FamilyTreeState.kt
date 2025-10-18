package com.raizesvivas.feature.tree.presentation.viewmodel

import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.utils.algorithms.TreeLayoutCalculator

/**
 * Estados possíveis da árvore genealógica
 * 
 * Define todos os estados que a árvore pode estar
 * durante o ciclo de vida da aplicação.
 */
sealed class FamilyTreeState {
    /**
     * Estado inicial - ainda não carregou a árvore
     */
    object Initial : FamilyTreeState()
    
    /**
     * Carregando - operação em andamento
     */
    object Loading : FamilyTreeState()
    
    /**
     * Árvore carregada com sucesso
     */
    data class Success(
        val family: Family,
        val members: List<Member>,
        val relationships: List<Relationship>,
        val layout: TreeLayoutCalculator.TreeLayout,
        val selectedMemberId: String? = null
    ) : FamilyTreeState()
    
    /**
     * Erro ao carregar árvore
     */
    data class Error(val message: String) : FamilyTreeState()
}
