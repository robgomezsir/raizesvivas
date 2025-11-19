package com.raizesvivas.app.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.MainActivity
import com.raizesvivas.app.presentation.ui.theme.RaizesVivasButtonDefaults
import com.raizesvivas.app.presentation.ui.theme.InputShapeSuave
import com.raizesvivas.app.presentation.ui.theme.inputColorsPastel
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Tela de Login
 * 
 * Permite que usuários façam login com email e senha.
 * Oferece navegação para cadastro e recuperação de senha.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRecuperarSenha: () -> Unit,
    onNavigateToAceitarConvite: () -> Unit,
    onNavigateToPedirConvite: () -> Unit,
    onNavigateToCadastro: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = remember { context as? MainActivity }

    var biometricPromptShown by remember { mutableStateOf(false) }

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            biometricPromptShown = false
            onLoginSuccess()
        }
    }

    LaunchedEffect(state.lastEmail) {
        if (state.lastEmail != null && state.email.isEmpty()) {
            viewModel.onEmailChanged(state.lastEmail!!)
        }
    }

    // LaunchedEffect para login automático com biometria
    LaunchedEffect(state.biometricAvailable, state.biometricEnabled, state.lastEmail, activity) {
        Timber.d(
            "🔐 LaunchedEffect biometria - available: ${state.biometricAvailable}, enabled: ${state.biometricEnabled}, " +
                "lastEmail: ${state.lastEmail}, activity: ${activity != null}, alreadyShown: $biometricPromptShown"
        )

        if (
            state.biometricAvailable &&
            state.biometricEnabled &&
            state.lastEmail != null &&
            activity != null &&
            !biometricPromptShown &&
            !state.loginSuccess
        ) {
            Timber.d("🔐 Condições atendidas, aguardando delay antes de mostrar biometria")
            delay(800) // Aumentar delay para garantir que a tela está pronta
            if (!biometricPromptShown && !state.loginSuccess) {
                biometricPromptShown = true
                Timber.d("🔐 Chamando loginWithBiometric")
                viewModel.loginWithBiometric(activity) {
                    Timber.d("🔐 Callback onBiometricSuccess chamado")
                }
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val backgroundBrush = remember(colorScheme) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.22f),
                colorScheme.secondary.copy(alpha = 0.18f),
                colorScheme.background
            )
        )
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 4.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val isDarkTheme = isSystemInDarkTheme()
                    val logoRes = if (isDarkTheme) {
                        com.raizesvivas.app.R.drawable.logo512x512_escuro
                    } else {
                        com.raizesvivas.app.R.drawable.logo512x512_claro
                    }

                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Brasão da Família Gomes",
                        modifier = Modifier.size(128.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Raízes Vivas",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary
                        )
                        Text(
                            text = "Conecte-se à sua história familiar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
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

                    TextField(
                        value = state.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        },
                        singleLine = true,
                        isError = state.emailError != null,
                        supportingText = state.emailError?.let { { Text(it) } },
                        shape = InputShapeSuave,
                        colors = inputColorsPastel(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    var senhaVisivel by remember { mutableStateOf(false) }

                    TextField(
                        value = state.senha,
                        onValueChange = { viewModel.onSenhaChanged(it) },
                        label = { Text("Senha") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                                )
                            }
                        },
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = state.senhaError != null,
                        supportingText = state.senhaError?.let { { Text(it) } },
                        shape = InputShapeSuave,
                        colors = inputColorsPastel(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = onNavigateToPedirConvite) {
                            Text("Pedir convite")
                        }
                        TextButton(onClick = onNavigateToAceitarConvite) {
                            Text("Aceitar convite")
                        }
                        TextButton(onClick = onNavigateToRecuperarSenha) {
                            Text("Esqueci minha senha")
                        }
                    }
                    
                    // Link para cadastro
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Foi convidado? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onNavigateToCadastro,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Criar conta",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary
                            )
                        }
                    }

                    if (state.biometricAvailable && state.biometricEnabled && state.lastEmail != null && activity != null) {
                        OutlinedButton(
                            onClick = {
                                biometricPromptShown = false // Resetar para permitir nova tentativa
                                viewModel.loginWithBiometric(activity) {
                                    // Login automático após biometria
                                }
                            },
                            enabled = !state.isLoading,
                            shape = RaizesVivasButtonDefaults.Shape,
                            border = RaizesVivasButtonDefaults.outlineStroke(),
                            contentPadding = RaizesVivasButtonDefaults.ContentPadding,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Entrar com Biometria", style = MaterialTheme.typography.titleMedium)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "ou",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }
                    }

                    Button(
                        onClick = { viewModel.login() },
                        enabled = !state.isLoading,
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
                            Text("Entrar", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    // Cadastro aberto removido. Fluxo agora usa convites/aprovação.
                }
            }
        }
    }
}

