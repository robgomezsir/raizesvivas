package com.raizesvivas.feature.gamification.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.core.domain.usecase.gamification.GetUserPointsUseCase
import com.raizesvivas.feature.gamification.presentation.viewmodel.GamificationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gamificação
 * 
 * Gerencia o estado da gamificação, incluindo pontos,
 * níveis e conquistas do usuário.
 */
@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val getUserPointsUseCase: GetUserPointsUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<GamificationState>(GamificationState.Initial)
    val state: StateFlow<GamificationState> = _state.asStateFlow()
    
    /**
     * Carrega os dados de gamificação do usuário
     */
    fun loadUserGamification(userId: String) {
        viewModelScope.launch {
            _state.value = GamificationState.Loading
            
            try {
                val result = getUserPointsUseCase(userId)
                if (result.isSuccess) {
                    val userPoints = result.getOrNull()
                    if (userPoints != null) {
                        _state.value = GamificationState.Success(userPoints)
                    } else {
                        _state.value = GamificationState.Error("Pontos do usuário não encontrados")
                    }
                } else {
                    _state.value = GamificationState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao carregar pontos"
                    )
                }
            } catch (e: Exception) {
                _state.value = GamificationState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}
