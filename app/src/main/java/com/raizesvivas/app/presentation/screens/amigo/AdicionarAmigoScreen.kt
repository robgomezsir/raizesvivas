package com.raizesvivas.app.presentation.screens.amigo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.RaizesVivasTextField

/**
 * Tela de Cadastro de Amigo da Família
 * 
 * IMPORTANTE: Todos os usuários autenticados podem adicionar amigos da família.
 * Não há restrições de administrador para esta funcionalidade.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarAmigoScreen(
    viewModel: AdicionarAmigoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val pessoasDisponiveis by viewModel.pessoasDisponiveis.collectAsState()
    
    // Navegar quando salvo com sucesso
    LaunchedEffect(state.sucesso) {
        if (state.sucesso) {
            viewModel.limparSucesso()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Amigo da Família") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo Nome
            RaizesVivasTextField(
                value = state.nome,
                onValueChange = viewModel::atualizarNome,
                label = "Nome *",
                placeholder = { Text("Digite o nome do amigo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading
            )
            
            // Campo Telefone
            RaizesVivasTextField(
                value = state.telefone,
                onValueChange = viewModel::atualizarTelefone,
                label = "Telefone",
                placeholder = { Text("(00) 00000-0000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                enabled = !state.isLoading
            )
            
            // Dropdown de Familiares
            var expandedDropdown by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                RaizesVivasTextField(
                    value = pessoasDisponiveis
                        .firstOrNull { it.id == state.familiarSelecionado }
                        ?.getNomeExibicao() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = "Vincular Familiar",
                    placeholder = { Text("Selecione um familiar") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !state.isLoading && pessoasDisponiveis.isNotEmpty()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    // Opção para remover seleção
                    DropdownMenuItem(
                        text = { Text("Nenhum") },
                        onClick = {
                            viewModel.atualizarFamiliarSelecionado(null)
                            expandedDropdown = false
                        }
                    )
                    
                    pessoasDisponiveis.forEach { pessoa ->
                        DropdownMenuItem(
                            text = { Text(pessoa.getNomeExibicao()) },
                            onClick = {
                                viewModel.atualizarFamiliarSelecionado(pessoa.id)
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botão Salvar
            Button(
                onClick = viewModel::salvarAmigo,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.nome.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvando...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar")
                }
            }
            
            // Mensagem de erro
            state.erro?.let { erro ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = erro,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = viewModel::limparErro) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

