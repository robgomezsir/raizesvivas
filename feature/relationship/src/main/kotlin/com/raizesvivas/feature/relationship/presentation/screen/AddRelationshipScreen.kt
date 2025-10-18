package com.raizesvivas.feature.relationship.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.core.domain.model.RelationshipType
import com.raizesvivas.feature.relationship.presentation.viewmodel.AddRelationshipState
import com.raizesvivas.feature.relationship.presentation.viewmodel.AddRelationshipViewModel

/**
 * Tela para adicionar novo relacionamento
 * 
 * Permite que o usuário adicione um novo relacionamento
 * entre dois membros da família.
 */
@Composable
fun AddRelationshipScreen(
    userId: String,
    viewModel: AddRelationshipViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Adicionar Relacionamento",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AddRelationshipForm(
            state = state,
            onMember1Change = viewModel::updateMember1,
            onMember2Change = viewModel::updateMember2,
            onRelationshipTypeChange = viewModel::updateRelationshipType,
            onStartDateChange = viewModel::updateStartDate,
            onObservationsChange = viewModel::updateObservations,
            onSubmit = viewModel::addRelationship
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { viewModel.addRelationship() },
            enabled = state.isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Adicionar Relacionamento")
        }
        
        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.error!!,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AddRelationshipForm(
    state: AddRelationshipState,
    onMember1Change: (com.raizesvivas.core.domain.model.Member) -> Unit,
    onMember2Change: (com.raizesvivas.core.domain.model.Member) -> Unit,
    onRelationshipTypeChange: (RelationshipType) -> Unit,
    onStartDateChange: (String) -> Unit,
    onObservationsChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    var relationshipTypeExpanded by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TODO: Implementar seleção de membros
        OutlinedTextField(
            value = state.member1?.nomeCompleto ?: "",
            onValueChange = { },
            label = { Text("Membro 1") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = state.member2?.nomeCompleto ?: "",
            onValueChange = { },
            label = { Text("Membro 2") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Seleção de tipo de relacionamento
        ExposedDropdownMenuBox(
            expanded = relationshipTypeExpanded,
            onExpandedChange = { relationshipTypeExpanded = !relationshipTypeExpanded }
        ) {
            OutlinedTextField(
                value = state.relationshipType?.description ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Tipo de Relacionamento") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relationshipTypeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = relationshipTypeExpanded,
                onDismissRequest = { relationshipTypeExpanded = false }
            ) {
                RelationshipType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.description) },
                        onClick = {
                            onRelationshipTypeChange(type)
                            relationshipTypeExpanded = false
                        }
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = state.startDate,
            onValueChange = onStartDateChange,
            label = { Text("Data de Início") },
            placeholder = { Text("YYYY-MM-DD") },
            isError = state.startDateError != null,
            supportingText = state.startDateError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = state.observations,
            onValueChange = onObservationsChange,
            label = { Text("Observações") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
