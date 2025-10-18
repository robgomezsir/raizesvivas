package com.raizesvivas.feature.family.presentation.viewmodel

import com.raizesvivas.core.domain.model.Family

/**
 * Estados possíveis da família-zero
 * 
 * Define todos os estados que a família-zero pode estar
 * durante o ciclo de vida da aplicação.
 */
sealed class FamilyZeroState {
    /**
     * Estado inicial - ainda não verificou família-zero
     */
    object Initial : FamilyZeroState()
    
    /**
     * Carregando - operação em andamento
     */
    object Loading : FamilyZeroState()
    
    /**
     * Família-zero carregada com sucesso
     */
    data class Success(val family: Family) : FamilyZeroState()
    
    /**
     * Usuário não possui família-zero
     */
    object NoFamilyZero : FamilyZeroState()
    
    /**
     * Erro ao carregar/criar família-zero
     */
    data class Error(val message: String) : FamilyZeroState()
}
