package com.raizesvivas.app.presentation.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Tela de criação da Família Zero (onboarding)
 * 
 * Permite ao primeiro usuário criar o casal raiz da árvore genealógica.
 * Esta tela só aparece uma vez, quando não existe Família Zero ainda.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaZeroScreen(
    viewModel: FamiliaZeroViewModel = hiltViewModel(),
    onFamiliaZeroCriada: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    // Navegar quando criação for bem-sucedida
    LaunchedEffect(state.sucesso) {
        if (state.sucesso) {
            onFamiliaZeroCriada()
        }
    }
    
    // Se já existe Família Zero, mostrar mensagem
    if (state.familiaZeroJaExiste) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Família Zero já existe!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A raiz da árvore genealógica já foi criada.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Família Zero") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícone/Título
            Icon(
                imageVector = Icons.Default.FamilyRestroom,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Bem-vindo ao Raízes Vivas!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Vamos criar a raiz da sua árvore genealógica. Digite os nomes do casal patriarca/matriarca.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            
            // Mensagem de erro geral
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Campo Nome da Árvore (opcional)
            OutlinedTextField(
                value = state.nomeArvore,
                onValueChange = { viewModel.onNomeArvoreChanged(it) },
                label = { Text("Nome da Árvore (opcional)") },
                placeholder = { Text("Ex: Família Silva") },
                leadingIcon = {
                    Icon(Icons.Default.AccountTree, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Campo Nome do Pai (Patriarca)
            Text(
                text = "Patriarca",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = state.nomePai,
                onValueChange = { viewModel.onNomePaiChanged(it) },
                label = { Text("Nome do Patriarca") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                isError = state.nomePaiError != null,
                supportingText = state.nomePaiError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Campo Nome da Mãe (Matriarca)
            Text(
                text = "Matriarca",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = state.nomeMae,
                onValueChange = { viewModel.onNomeMaeChanged(it) },
                label = { Text("Nome da Matriarca") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                isError = state.nomeMaeError != null,
                supportingText = state.nomeMaeError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.criarFamiliaZero()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botão Criar Família Zero
            Button(
                onClick = { showConfirmDialog = true },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Criar Família Zero", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            // Dialog de confirmação
            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Confirmar criação da Família Zero") },
                    text = {
                        Column {
                            Text("Você está prestes a criar a raiz da árvore genealógica com:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Patriarca: ${state.nomePai}", fontWeight = FontWeight.Bold)
                            Text("• Matriarca: ${state.nomeMae}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("⚠️ Esta ação só pode ser feita uma vez e não pode ser desfeita.", 
                                color = MaterialTheme.colorScheme.error)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmDialog = false
                                viewModel.criarFamiliaZero()
                            }
                        ) {
                            Text("Confirmar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Informação
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "A Família Zero só pode ser criada uma vez. O casal raiz não pode ser deletado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

