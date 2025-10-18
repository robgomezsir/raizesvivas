package com.raizesvivas.feature.family.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.core.ui.components.ErrorScreen
import com.raizesvivas.core.ui.components.LoadingScreen
import com.raizesvivas.feature.family.presentation.viewmodel.FamilyZeroState
import com.raizesvivas.feature.family.presentation.viewmodel.FamilyZeroViewModel

/**
 * Tela de setup da família-zero
 * 
 * Permite que o usuário crie sua família-zero (raiz da árvore genealógica).
 */
@Composable
fun FamilyZeroSetupScreen(
    userId: String,
    viewModel: FamilyZeroViewModel = hiltViewModel(),
    onNavigateToFamilyOverview: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is FamilyZeroState.Loading -> {
            LoadingScreen(message = "Criando sua família-zero...")
        }
        is FamilyZeroState.Success -> {
            // Navegar para overview da família
            onNavigateToFamilyOverview()
        }
        is FamilyZeroState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { /* Pode ser implementado retry específico */ }
            )
        }
        else -> {
            FamilyZeroSetupContent(
                onCreateFamily = { name ->
                    viewModel.createFamilyZero(userId, name)
                }
            )
        }
    }
}

@Composable
private fun FamilyZeroSetupContent(
    onCreateFamily: (String) -> Unit
) {
    var familyName by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bem-vindo ao Raízes Vivas!",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Para começar, vamos criar sua família-zero. Esta será a raiz da sua árvore genealógica.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        OutlinedTextField(
            value = familyName,
            onValueChange = { familyName = it },
            label = { Text("Nome da sua família") },
            placeholder = { Text("Ex: Família Silva") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        
        Button(
            onClick = { onCreateFamily(familyName) },
            modifier = Modifier.fillMaxWidth(),
            enabled = familyName.isNotBlank()
        ) {
            Text("Criar Família-Zero")
        }
    }
}
