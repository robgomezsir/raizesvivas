package com.raizesvivas.feature.member.presentation.viewmodel

import com.raizesvivas.core.domain.model.Member

/**
 * Estado do formulário de adicionar membro
 * 
 * Contém todos os campos do formulário e validações.
 */
data class AddMemberState(
    val userId: String = "",
    val name: String = "",
    val nameError: String? = null,
    val birthDate: String = "",
    val birthDateError: String? = null,
    val location: String = "",
    val profession: String = "",
    val observations: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    /**
     * Verifica se o formulário é válido
     */
    val isFormValid: Boolean
        get() = name.isNotBlank() && 
                nameError == null && 
                birthDateError == null &&
                !isSubmitting
    
    /**
     * Estado de sucesso
     */
    data class Success(val member: Member) : AddMemberState()
}
