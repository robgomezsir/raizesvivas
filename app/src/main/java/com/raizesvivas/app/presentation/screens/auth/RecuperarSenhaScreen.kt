package com.raizesvivas.app.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.presentation.ui.theme.RaizesVivasButtonDefaults
import com.raizesvivas.app.presentation.components.RaizesVivasTextField

/**
 * Tela de Recuperação de Senha
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarSenhaScreen(
    viewModel: RecuperarSenhaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
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
                title = { Text("Recuperar Senha", style = MaterialTheme.typography.titleLarge) },
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
                        text = "Esqueceu sua senha?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colorScheme.primary
                    )

                    Text(
                        text = "Digite seu email e enviaremos um link para redefinir sua senha",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (state.enviado) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp,
                            color = colorScheme.primaryContainer.copy(alpha = 0.9f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Email enviado! Verifique sua caixa de entrada.",
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
                        value = state.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = "Email",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        enabled = !state.enviado,
                        isError = state.emailError != null,
                        supportingText = state.emailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!state.enviado) {
                                    viewModel.enviarEmail()
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.enviarEmail() },
                        enabled = !state.isLoading && !state.enviado,
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
                                text = if (state.enviado) "Email Enviado" else "Enviar Email",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

