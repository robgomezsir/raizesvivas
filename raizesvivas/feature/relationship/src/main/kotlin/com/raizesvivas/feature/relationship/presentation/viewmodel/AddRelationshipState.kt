package com.raizesvivas.feature.relationship.presentation.viewmodel

import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.domain.model.RelationshipType

/**
 * Estado do formulário de adicionar relacionamento
 * 
 * Contém todos os campos do formulário e validações.
 */
data class AddRelationshipState(
    val userId: String = "",
    val member1: Member? = null,
    val member1Error: String? = null,
    val member2: Member? = null,
    val member2Error: String? = null,
    val relationshipType: RelationshipType? = null,
    val relationshipTypeError: String? = null,
    val startDate: String = "",
    val startDateError: String? = null,
    val observations: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    /**
     * Verifica se o formulário é válido
     */
    val isFormValid: Boolean
        get() = member1 != null && 
                member2 != null && 
                relationshipType != null &&
                member1Error == null &&
                member2Error == null &&
                relationshipTypeError == null &&
                startDateError == null &&
                !isSubmitting
    
    /**
     * Estado de sucesso
     */
    data class Success(val relationship: Relationship) : AddRelationshipState()
}
