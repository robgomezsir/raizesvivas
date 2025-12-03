package com.raizesvivas.app.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.presentation.components.RaizesVivasTextField
import com.raizesvivas.app.presentation.ui.theme.RaizesVivasButtonDefaults
import kotlinx.coroutines.delay

/**
 * Tela de Redefinição de Senha
 * 
 * Permite ao usuário definir uma nova senha após clicar no link do email
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedefinirSenhaScreen(
    oobCode: String,
    viewModel: RedefinirSenhaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSenhaRedefinida: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Inicializar o oobCode no ViewModel
    LaunchedEffect(oobCode) {
        viewModel.setOobCode(oobCode)
    }
    
    val colorScheme = MaterialTheme.colorScheme
    val gradient = remember(colorScheme) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.2f),
                colorScheme.secondary.copy(alpha = 0.16f),
                colorScheme.background
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Redefinir Senha", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = colorScheme.background.copy(alpha = 0.6f),
                    navigationIconContentColor = colorScheme.primary,
                    titleContentColor = colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 4.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Defina sua nova senha",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colorScheme.primary
                    )

                    Text(
                        text = "Digite uma nova senha segura para sua conta",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (state.senhaRedefinida) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp,
                            color = colorScheme.primaryContainer.copy(alpha = 0.9f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Senha redefinida com sucesso! Redirecionando para login...",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                color = colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    state.error?.let { error ->
                        Surface(
                            color = colorScheme.errorContainer.copy(alpha = 0.85f),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }

                    RaizesVivasTextField(
                        value = state.novaSenha,
                        onValueChange = { viewModel.onNovaSenhaChanged(it) },
                        label = "Nova Senha",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleSenhaVisibility() }) {
                                Icon(
                                    imageVector = if (state.senhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (state.senhaVisivel) "Ocultar senha" else "Mostrar senha"
                                )
                            }
                        },
                        singleLine = true,
                        enabled = !state.senhaRedefinida,
                        isError = state.senhaError != null,
                        supportingText = state.senhaError?.let { { Text(it) } },
                        visualTransformation = if (state.senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    RaizesVivasTextField(
                        value = state.confirmarSenha,
                        onValueChange = { viewModel.onConfirmarSenhaChanged(it) },
                        label = "Confirmar Senha",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleConfirmarSenhaVisibility() }) {
                                Icon(
                                    imageVector = if (state.confirmarSenhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (state.confirmarSenhaVisivel) "Ocultar senha" else "Mostrar senha"
                                )
                            }
                        },
                        singleLine = true,
                        enabled = !state.senhaRedefinida,
                        isError = state.confirmarSenhaError != null,
                        supportingText = state.confirmarSenhaError?.let { { Text(it) } },
                        visualTransformation = if (state.confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!state.senhaRedefinida) {
                                    viewModel.redefinirSenha()
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.redefinirSenha() },
                        enabled = !state.isLoading && !state.senhaRedefinida,
                        shape = RaizesVivasButtonDefaults.Shape,
                        colors = RaizesVivasButtonDefaults.primaryColors(),
                        contentPadding = RaizesVivasButtonDefaults.ContentPadding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (state.senhaRedefinida) "Senha Redefinida" else "Redefinir Senha",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Navegar para login após redefinição bem-sucedida
    LaunchedEffect(state.senhaRedefinida) {
        if (state.senhaRedefinida) {
            kotlinx.coroutines.delay(2000) // Aguardar 2 segundos para mostrar mensagem
            onSenhaRedefinida()
        }
    }
}

