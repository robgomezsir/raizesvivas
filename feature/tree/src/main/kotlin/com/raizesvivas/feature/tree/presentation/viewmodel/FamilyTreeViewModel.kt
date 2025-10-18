package com.raizesvivas.feature.tree.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raizesvivas.core.domain.model.Family
import com.raizesvivas.core.domain.model.Member
import com.raizesvivas.core.domain.model.Relationship
import com.raizesvivas.core.domain.repository.FamilyRepository
import com.raizesvivas.core.domain.repository.MemberRepository
import com.raizesvivas.core.domain.repository.RelationshipRepository
import com.raizesvivas.core.utils.algorithms.TreeLayoutCalculator
import com.raizesvivas.feature.tree.presentation.viewmodel.FamilyTreeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para visualização da árvore genealógica
 * 
 * Gerencia o estado da árvore genealógica e calcula
 * o layout para visualização.
 */
@HiltViewModel
class FamilyTreeViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val memberRepository: MemberRepository,
    private val relationshipRepository: RelationshipRepository,
    private val treeLayoutCalculator: TreeLayoutCalculator
) : ViewModel() {
    
    private val _state = MutableStateFlow<FamilyTreeState>(FamilyTreeState.Initial)
    val state: StateFlow<FamilyTreeState> = _state.asStateFlow()
    
    /**
     * Carrega a árvore genealógica
     */
    fun loadFamilyTree(userId: String, familyId: String? = null) {
        viewModelScope.launch {
            _state.value = FamilyTreeState.Loading
            
            try {
                // Buscar família
                val family = if (familyId != null) {
                    familyRepository.getFamilyById(familyId, userId)
                } else {
                    familyRepository.getFamilyZero(userId)
                }
                
                if (family == null) {
                    _state.value = FamilyTreeState.Error("Família não encontrada")
                    return@launch
                }
                
                // Buscar membros
                val members = memberRepository.getAllMembers(userId)
                val relationships = relationshipRepository.getAllRelationships(userId)
                
                // Calcular layout da árvore
                val layout = treeLayoutCalculator.calculateLayout(
                    members = members,
                    relationships = relationships,
                    rootMemberId = null
                )
                
                _state.value = FamilyTreeState.Success(
                    family = family,
                    members = members,
                    relationships = relationships,
                    layout = layout
                )
                
            } catch (e: Exception) {
                _state.value = FamilyTreeState.Error(e.message ?: "Erro ao carregar árvore")
            }
        }
    }
    
    /**
     * Seleciona um membro na árvore
     */
    fun selectMember(memberId: String) {
        val currentState = _state.value
        if (currentState is FamilyTreeState.Success) {
            _state.value = currentState.copy(selectedMemberId = memberId)
        }
    }
    
    /**
     * Limpa seleção de membro
     */
    fun clearMemberSelection() {
        val currentState = _state.value
        if (currentState is FamilyTreeState.Success) {
            _state.value = currentState.copy(selectedMemberId = null)
        }
    }
}
