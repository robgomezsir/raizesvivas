package com.raizesvivas.feature.family.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.usecase.family.CreateFamilyZeroUseCase
import com.raizesvivas.core.domain.usecase.family.GetFamilyZeroUseCase
import com.raizesvivas.feature.family.presentation.viewmodel.FamilyZeroState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestão da família-zero
 * 
 * Gerencia a criação e visualização da família-zero
 * (raiz da árvore genealógica).
 */
@HiltViewModel
class FamilyZeroViewModel @Inject constructor(
    private val createFamilyZeroUseCase: CreateFamilyZeroUseCase,
    private val getFamilyZeroUseCase: GetFamilyZeroUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<FamilyZeroState>(FamilyZeroState.Initial)
    val state: StateFlow<FamilyZeroState> = _state.asStateFlow()
    
    /**
     * Carrega a família-zero do usuário
     */
    fun loadFamilyZero(userId: String) {
        viewModelScope.launch {
            _state.value = FamilyZeroState.Loading
            
            try {
                val result = getFamilyZeroUseCase(userId)
                if (result.isSuccess) {
                    val family = result.getOrNull()
                    if (family != null) {
                        _state.value = FamilyZeroState.Success(family)
                    } else {
                        _state.value = FamilyZeroState.NoFamilyZero
                    }
                } else {
                    _state.value = FamilyZeroState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao carregar família-zero"
                    )
                }
            } catch (e: Exception) {
                _state.value = FamilyZeroState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
    
    /**
     * Cria uma nova família-zero
     */
    fun createFamilyZero(userId: String, nome: String) {
        viewModelScope.launch {
            _state.value = FamilyZeroState.Loading
            
            try {
                val result = createFamilyZeroUseCase(userId, nome)
                if (result.isSuccess) {
                    _state.value = FamilyZeroState.Success(result.getOrNull()!!)
                } else {
                    _state.value = FamilyZeroState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao criar família-zero"
                    )
                }
            } catch (e: Exception) {
                _state.value = FamilyZeroState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}
