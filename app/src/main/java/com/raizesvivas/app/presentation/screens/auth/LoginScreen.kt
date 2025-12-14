package com.raizesvivas.app.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.raizesvivas.app.presentation.components.RaizesVivasTextField
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
    
    // Launcher para solicitar permissão de notificações (Android 13+)
    val notificationPermissionLauncher = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Timber.d("✅ Permissão de notificações concedida")
            } else {
                Timber.w("⚠️ Permissão de notificações negada")
            }
        }
    } else {
        null
    }

    // Solicitar permissão de notificações após login bem-sucedido
    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            biometricPromptShown = false
            
            // Solicitar permissão de notificações (Android 13+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            
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
    
    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Imagem de background
            Image(
                painter = painterResource(id = com.raizesvivas.app.R.drawable.login_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Overlay semi-transparente para melhorar legibilidade
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.25f)
                            )
                        )
                    )
            )
            
            // Conteúdo do login
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color.White.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .background(Color.Transparent)
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        modifier = Modifier.size(100.dp)
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
                            color = colorScheme.primary.copy(alpha = 0.95f)
                        )
                        Text(
                            text = "Conecte-se à sua história familiar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }

                    state.error?.let { error ->
                        Surface(
                            color = colorScheme.errorContainer.copy(alpha = 0.75f),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 0.dp,
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
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = colorScheme.tertiary
                            )
                        },
                        singleLine = true,
                        isError = state.emailError != null,
                        supportingText = state.emailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textColor = Color.Black
                    )

                    var senhaVisivel by remember { mutableStateOf(false) }

                    RaizesVivasTextField(
                        value = state.senha,
                        onValueChange = { viewModel.onSenhaChanged(it) },
                        label = "Senha",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = colorScheme.tertiary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha",
                                    tint = colorScheme.tertiary
                                )
                            }
                        },
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = state.senhaError != null,
                        supportingText = state.senhaError?.let { { Text(it) } },
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
                        modifier = Modifier.fillMaxWidth(),
                        textColor = Color.Black
                    )


                    // Manter Conectado e Esqueci Senha
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Checkbox(
                                checked = state.keepConnected,
                                onCheckedChange = { viewModel.onKeepConnectedChanged(it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorScheme.tertiary,
                                    uncheckedColor = colorScheme.outline
                                ),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Manter conectado",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.8f)
                            )
                        }
                        
                        TextButton(
                            onClick = onNavigateToRecuperarSenha,
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Esqueci a senha",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Ações Secundárias (Convites)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onNavigateToPedirConvite,
                            colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.tertiary),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Pedir convite", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "•",
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        TextButton(
                            onClick = onNavigateToAceitarConvite,
                            colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.tertiary),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Aceitar convite", style = MaterialTheme.typography.bodyMedium)
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
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        TextButton(
                            onClick = onNavigateToCadastro,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.tertiary),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Criar conta",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
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
                                tint = colorScheme.tertiary
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
                                color = Color.Black.copy(alpha = 0.75f)
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
}

