package com.raizesvivas.feature.tree.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.usecase.family.CreateSubfamilyUseCase
import com.raizesvivas.core.domain.usecase.family.GetSubfamiliesUseCase
import com.raizesvivas.feature.tree.presentation.viewmodel.SubfamilyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestão de subfamílias
 * 
 * Gerencia a criação e visualização de subfamílias
 * a partir de casamentos entre membros.
 */
@HiltViewModel
class SubfamilyViewModel @Inject constructor(
    private val createSubfamilyUseCase: CreateSubfamilyUseCase,
    private val getSubfamiliesUseCase: GetSubfamiliesUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<SubfamilyState>(SubfamilyState.Initial)
    val state: StateFlow<SubfamilyState> = _state.asStateFlow()
    
    /**
     * Carrega subfamílias de uma família pai
     */
    fun loadSubfamilies(userId: String, parentFamilyId: String) {
        viewModelScope.launch {
            _state.value = SubfamilyState.Loading
            
            try {
                val subfamilies = getSubfamiliesUseCase(parentFamilyId, userId)
                _state.value = SubfamilyState.Success(subfamilies)
            } catch (e: Exception) {
                _state.value = SubfamilyState.Error(e.message ?: "Erro ao carregar subfamílias")
            }
        }
    }
    
    /**
     * Cria uma nova subfamília
     */
    fun createSubfamily(
        userId: String,
        nome: String,
        familiaPaiId: String,
        membroOrigem1Id: String,
        membroOrigem2Id: String,
        iconeArvore: String? = null
    ) {
        viewModelScope.launch {
            _state.value = SubfamilyState.Loading
            
            try {
                val result = createSubfamilyUseCase(
                    userId = userId,
                    nome = nome,
                    familiaPaiId = familiaPaiId,
                    membroOrigem1Id = membroOrigem1Id,
                    membroOrigem2Id = membroOrigem2Id,
                    iconeArvore = iconeArvore
                )
                
                if (result.isSuccess) {
                    _state.value = SubfamilyState.Success(listOf(result.getOrNull()!!))
                } else {
                    _state.value = SubfamilyState.Error(
                        result.exceptionOrNull()?.message ?: "Erro ao criar subfamília"
                    )
                }
            } catch (e: Exception) {
                _state.value = SubfamilyState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}
