package com.raizesvivas.feature.tree.presentation.viewmodel

import com.raizesvivas.core.domain.model.Family

/**
 * Estados possíveis das subfamílias
 * 
 * Define todos os estados que as subfamílias podem estar
 * durante o ciclo de vida da aplicação.
 */
sealed class SubfamilyState {
    /**
     * Estado inicial - ainda não carregou subfamílias
     */
    object Initial : SubfamilyState()
    
    /**
     * Carregando - operação em andamento
     */
    object Loading : SubfamilyState()
    
    /**
     * Subfamílias carregadas com sucesso
     */
    data class Success(val subfamilies: List<Family>) : SubfamilyState()
    
    /**
     * Erro ao carregar/criar subfamílias
     */
    data class Error(val message: String) : SubfamilyState()
}
