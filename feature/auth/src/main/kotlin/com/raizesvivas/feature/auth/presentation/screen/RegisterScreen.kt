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
 * Tela de registro
 * 
 * Permite que o usuário crie uma nova conta.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is AuthState.Loading -> {
            LoadingScreen(message = "Criando conta...")
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
            RegisterContent(
                onRegister = { email, password ->
                    viewModel.signUp(email, password)
                },
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}

@Composable
private fun RegisterContent(
    onRegister: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Criar Conta",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
        )
        
        Text(
            text = "Crie sua conta para começar",
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
                .padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
        
        Button(
            onClick = { onRegister(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = email.isNotBlank() && 
                     password.isNotBlank() && 
                     confirmPassword.isNotBlank() &&
                     password == confirmPassword
        ) {
            Text("Criar Conta")
        }
        
        TextButton(onClick = onNavigateToLogin) {
            Text("Já tem conta? Faça login")
        }
    }
}
