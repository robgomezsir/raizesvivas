package com.raizesvivas.app.presentation.screens.convites

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.presentation.components.RaizesVivasTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedirConviteScreen(
    onNavigateBack: () -> Unit,
    viewModel: PedirConviteViewModel = hiltViewModel()
){
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedir Convite") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Informe seu e-mail para solicitar um convite. Opcionalmente, adicione nome e telefone.",
                style = MaterialTheme.typography.bodyMedium
            )

            RaizesVivasTextField(
                value = state.email,
                onValueChange = viewModel::atualizarEmail,
                label = "E-mail",
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            RaizesVivasTextField(
                value = state.nome,
                onValueChange = viewModel::atualizarNome,
                label = "Nome (opcional)",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            RaizesVivasTextField(
                value = state.telefone,
                onValueChange = viewModel::atualizarTelefone,
                label = "Telefone (opcional)",
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            state.erro?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            state.sucesso?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = viewModel::enviarPedido,
                enabled = !state.isLoading && state.email.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Enviando...")
                } else {
                    Text("Enviar pedido")
                }
            }
        }
    }
}


