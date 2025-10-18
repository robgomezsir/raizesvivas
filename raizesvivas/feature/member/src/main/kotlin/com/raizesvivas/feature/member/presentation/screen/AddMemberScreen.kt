package com.raizesvivas.feature.member.presentation.screen

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.feature.member.presentation.viewmodel.AddMemberState
import com.raizesvivas.feature.member.presentation.viewmodel.AddMemberViewModel

/**
 * Tela para adicionar novo membro
 * 
 * Permite que o usuário adicione um novo membro à família.
 */
@Composable
fun AddMemberScreen(
    userId: String,
    viewModel: AddMemberViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Adicionar Novo Membro",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AddMemberForm(
            state = state,
            onNameChange = viewModel::updateName,
            onBirthDateChange = viewModel::updateBirthDate,
            onLocationChange = viewModel::updateLocation,
            onProfessionChange = viewModel::updateProfession,
            onObservationsChange = viewModel::updateObservations,
            onSubmit = viewModel::addMember
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { viewModel.addMember() },
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
            Text("Adicionar Membro")
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
private fun AddMemberForm(
    state: AddMemberState,
    onNameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onProfessionChange: (String) -> Unit,
    onObservationsChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text("Nome Completo") },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = state.birthDate,
            onValueChange = onBirthDateChange,
            label = { Text("Data de Nascimento") },
            placeholder = { Text("YYYY-MM-DD") },
            isError = state.birthDateError != null,
            supportingText = state.birthDateError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = state.location,
            onValueChange = onLocationChange,
            label = { Text("Local de Nascimento") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = state.profession,
            onValueChange = onProfessionChange,
            label = { Text("Profissão") },
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
