package com.raizesvivas.feature.gamification.presentation.viewmodel

import com.raizesvivas.core.domain.model.UserPoints

/**
 * Estados possíveis da gamificação
 * 
 * Define todos os estados que a gamificação pode estar
 * durante o ciclo de vida da aplicação.
 */
sealed class GamificationState {
    /**
     * Estado inicial - ainda não carregou dados
     */
    object Initial : GamificationState()
    
    /**
     * Carregando - operação em andamento
     */
    object Loading : GamificationState()
    
    /**
     * Dados carregados com sucesso
     */
    data class Success(val userPoints: UserPoints) : GamificationState()
    
    /**
     * Erro ao carregar dados
     */
    data class Error(val message: String) : GamificationState()
}
