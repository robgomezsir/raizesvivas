package com.raizesvivas.feature.relationship.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.RelationshipType
import com.raizesvivas.core.domain.usecase.relationship.AddRelationshipUseCase
import com.raizesvivas.feature.relationship.presentation.viewmodel.AddRelationshipState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para adicionar relacionamento
 * 
 * Gerencia o estado do formulário de adição de relacionamento
 * entre dois membros da família.
 */
@HiltViewModel
class AddRelationshipViewModel @Inject constructor(
    private val addRelationshipUseCase: AddRelationshipUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddRelationshipState())
    val state: StateFlow<AddRelationshipState> = _state.asStateFlow()
    
    /**
     * Atualiza o membro 1 selecionado
     */
    fun updateMember1(member: Member) {
        _state.value = _state.value.copy(
            member1 = member,
            member1Error = null
        )
    }
    
    /**
     * Atualiza o membro 2 selecionado
     */
    fun updateMember2(member: Member) {
        _state.value = _state.value.copy(
            member2 = member,
            member2Error = null
        )
    }
    
    /**
     * Atualiza o tipo de relacionamento
     */
    fun updateRelationshipType(type: RelationshipType) {
        _state.value = _state.value.copy(
            relationshipType = type,
            relationshipTypeError = null
        )
    }
    
    /**
     * Atualiza a data de início
     */
    fun updateStartDate(date: String) {
        _state.value = _state.value.copy(
            startDate = date,
            startDateError = validateDate(date)
        )
    }
    
    /**
     * Atualiza as observações
     */
    fun updateObservations(observations: String) {
        _state.value = _state.value.copy(observations = observations)
    }
    
    /**
     * Adiciona o relacionamento
     */
    fun addRelationship() {
        val currentState = _state.value
        if (!currentState.isFormValid) return
        
        viewModelScope.launch {
            _state.value = currentState.copy(isSubmitting = true)
            
            try {
                val result = addRelationshipUseCase(
                    userId = currentState.userId,
                    membro1Id = currentState.member1!!.id,
                    membro2Id = currentState.member2!!.id,
                    tipoRelacionamento = currentState.relationshipType!!,
                    dataInicio = currentState.startDate,
                    observacoes = currentState.observations
                )
                
                if (result.isSuccess) {
                    _state.value = AddRelationshipState.Success(result.getOrNull()!!)
                } else {
                    _state.value = currentState.copy(
                        isSubmitting = false,
                        error = result.exceptionOrNull()?.message ?: "Erro ao adicionar relacionamento"
                    )
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isSubmitting = false,
                    error = e.message ?: "Erro desconhecido"
                )
            }
        }
    }
    
    /**
     * Valida a data
     */
    private fun validateDate(date: String): String? {
        if (date.isBlank()) return null
        
        return try {
            java.time.LocalDate.parse(date)
            null
        } catch (e: Exception) {
            "Data inválida (use o formato YYYY-MM-DD)"
        }
    }
}
