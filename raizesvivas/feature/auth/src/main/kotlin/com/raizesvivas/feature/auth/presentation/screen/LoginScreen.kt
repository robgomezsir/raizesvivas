package com.raizesvivas.feature.auth.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.core.ui.components.ErrorScreen
import com.raizesvivas.core.ui.components.LoadingScreen
import com.raizesvivas.feature.auth.presentation.viewmodel.AuthState
import com.raizesvivas.feature.auth.presentation.viewmodel.AuthViewModel

/**
 * Tela de login
 * 
 * Permite que o usuário faça login com email e senha.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is AuthState.Loading -> {
            LoadingScreen(message = "Fazendo login...")
        }
        is AuthState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { /* Pode ser implementado retry específico */ }
            )
        }
        is AuthState.Authenticated -> {
            // Navegar para tela principal
            onNavigateToMain()
        }
        else -> {
            LoginContent(
                onLogin = { email, password ->
                    viewModel.signIn(email, password)
                },
                onNavigateToRegister = onNavigateToRegister
            )
        }
    }
}

@Composable
private fun LoginContent(
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Raízes Vivas",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
        )
        
        Text(
            text = "Faça login para continuar",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text("Entrar")
        }
        
        TextButton(onClick = onNavigateToRegister) {
            Text("Não tem conta? Registre-se")
        }
    }
}
