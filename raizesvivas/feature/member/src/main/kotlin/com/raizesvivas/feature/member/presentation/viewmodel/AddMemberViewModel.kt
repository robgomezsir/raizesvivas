package com.raizesvivas.feature.member.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.usecase.member.AddMemberUseCase
import com.raizesvivas.feature.member.presentation.viewmodel.AddMemberState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para adicionar membro
 * 
 * Gerencia o estado do formulário de adição de membro
 * e validações de dados.
 */
@HiltViewModel
class AddMemberViewModel @Inject constructor(
    private val addMemberUseCase: AddMemberUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddMemberState())
    val state: StateFlow<AddMemberState> = _state.asStateFlow()
    
    /**
     * Atualiza o nome completo
     */
    fun updateName(name: String) {
        _state.value = _state.value.copy(
            name = name,
            nameError = if (name.isBlank()) "Nome é obrigatório" else null
        )
    }
    
    /**
     * Atualiza a data de nascimento
     */
    fun updateBirthDate(date: String) {
        _state.value = _state.value.copy(
            birthDate = date,
            birthDateError = validateDate(date)
        )
    }
    
    /**
     * Atualiza o local de nascimento
     */
    fun updateLocation(location: String) {
        _state.value = _state.value.copy(location = location)
    }
    
    /**
     * Atualiza a profissão
     */
    fun updateProfession(profession: String) {
        _state.value = _state.value.copy(profession = profession)
    }
    
    /**
     * Atualiza as observações
     */
    fun updateObservations(observations: String) {
        _state.value = _state.value.copy(observations = observations)
    }
    
    /**
     * Adiciona o membro
     */
    fun addMember() {
        val currentState = _state.value
        if (!currentState.isFormValid) return
        
        viewModelScope.launch {
            _state.value = currentState.copy(isSubmitting = true)
            
            try {
                val result = addMemberUseCase(
                    userId = currentState.userId,
                    nomeCompleto = currentState.name,
                    nomeAbreviado = currentState.name.split(" ").firstOrNull(),
                    dataNascimento = currentState.birthDate,
                    localNascimento = currentState.location,
                    profissao = currentState.profession,
                    observacoes = currentState.observations
                )
                
                if (result.isSuccess) {
                    _state.value = AddMemberState.Success(result.getOrNull()!!)
                } else {
                    _state.value = currentState.copy(
                        isSubmitting = false,
                        error = result.exceptionOrNull()?.message ?: "Erro ao adicionar membro"
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
