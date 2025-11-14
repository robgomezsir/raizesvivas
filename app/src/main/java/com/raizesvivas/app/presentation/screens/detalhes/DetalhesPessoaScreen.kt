package com.raizesvivas.app.presentation.screens.detalhes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de Detalhes de uma Pessoa
 * 
 * Exibe todas as informações de uma pessoa e permite editar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesPessoaScreen(
    pessoaId: String,
    viewModel: DetalhesPessoaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEditar: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val mostrarModalConfirmacao by viewModel.mostrarModalConfirmacao.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    
    // Carregar pessoa ao entrar na tela
    LaunchedEffect(pessoaId) {
        viewModel.carregarPessoa(pessoaId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Pessoa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (state.pessoa != null) {
                        IconButton(onClick = { onNavigateToEditar(pessoaId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { viewModel.abrirModalConfirmacao() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.erro != null) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Erro ao carregar pessoa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = state.erro ?: "Erro desconhecido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(onClick = onNavigateBack) {
                        Text("Voltar")
                    }
                }
            }
        } else if (state.pessoa != null) {
            val pessoa = state.pessoa!!
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Foto e nome
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar/Foto
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                pessoa.fotoUrl?.let { fotoUrl ->
                                    if (fotoUrl.isNotBlank()) {
                                        // TODO: Carregar foto da pessoa usando Coil ou Glide
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                } ?: run {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = pessoa.nome,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        pessoa.apelido?.takeIf { it.isNotBlank() }?.let { apelido ->
                            Text(
                                text = apelido,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                        
                        if (pessoa.ehFamiliaZero) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.tertiary
                            ) {
                                Text(
                                    text = "Família Zero",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        }
                    }
                }
                
                // Informações Básicas
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Informações Básicas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider()
                        
                        InfoRow(
                            label = "Nome",
                            value = pessoa.nome,
                            icon = Icons.Default.Person
                        )

                        pessoa.apelido?.let { apelido ->
                            if (apelido.isNotBlank()) {
                                InfoRow(
                                    label = "Apelido",
                                    value = apelido,
                                    icon = Icons.Default.Star
                                )
                            }
                        }
                        
                        if (pessoa.genero != null) {
                            InfoRow(
                                label = "Gênero",
                                value = pessoa.genero.label,
                                icon = Icons.Default.Person
                            )
                        }
                        
                        if (pessoa.estadoCivil != null) {
                            InfoRow(
                                label = "Estado Civil",
                                value = pessoa.estadoCivil.label,
                                icon = Icons.Default.Favorite
                            )
                        }
                        
                        if (pessoa.dataNascimento != null) {
                            InfoRow(
                                label = "Data de Nascimento",
                                value = dateFormatter.format(pessoa.dataNascimento),
                                icon = Icons.Default.Cake
                            )
                        }
                        
                        if (pessoa.dataFalecimento != null) {
                            InfoRow(
                                label = "Data de Falecimento",
                                value = dateFormatter.format(pessoa.dataFalecimento),
                                icon = Icons.Default.Event
                            )
                        }
                        
                        pessoa.localNascimento?.let { local ->
                            if (local.isNotBlank()) {
                                InfoRow(
                                    label = "Local de Nascimento",
                                    value = local,
                                    icon = Icons.Default.LocationOn
                                )
                            }
                        }
                        
                        pessoa.localResidencia?.let { local ->
                            if (local.isNotBlank()) {
                                InfoRow(
                                    label = "Local de Residência",
                                    value = local,
                                    icon = Icons.Default.Home
                                )
                            }
                        }
                        
                        pessoa.profissao?.let { profissao ->
                            if (profissao.isNotBlank()) {
                                InfoRow(
                                    label = "Profissão",
                                    value = profissao,
                                    icon = Icons.Default.Work
                                )
                            }
                        }
                        
                        pessoa.telefone?.let { telefone ->
                            if (telefone.isNotBlank()) {
                                InfoRow(
                                    label = "Telefone/Celular",
                                    value = telefone,
                                    icon = Icons.Default.Phone
                                )
                            }
                        }
                    }
                }
                
                // Biografia
                pessoa.biografia?.let { biografia ->
                    if (biografia.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Biografia",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                HorizontalDivider()
                                
                                Text(
                                    text = biografia,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                // Relacionamentos
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Relacionamentos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        HorizontalDivider()
                        
                        // Pai
                        if (pessoa.pai != null) {
                            val paiNome = state.paiNome ?: "Carregando..."
                            InfoRow(
                                label = "Pai",
                                value = paiNome,
                                icon = Icons.Default.Man
                            )
                        }
                        
                        // Mãe
                        if (pessoa.mae != null) {
                            val maeNome = state.maeNome ?: "Carregando..."
                            InfoRow(
                                label = "Mãe",
                                value = maeNome,
                                icon = Icons.Default.Woman
                            )
                        }
                        
                        // Cônjuge
                        if (pessoa.conjugeAtual != null) {
                            val conjugeNome = state.conjugeNome ?: "Carregando..."
                            InfoRow(
                                label = "Cônjuge",
                                value = conjugeNome,
                                icon = Icons.Default.Favorite
                            )
                        }
                        
                        // Filhos
                        if (pessoa.filhos.isNotEmpty()) {
                            val filhosTexto = if (state.filhosNomes.isNotEmpty()) {
                                state.filhosNomes.joinToString(", ")
                            } else {
                                "${pessoa.filhos.size} filho(s)"
                            }
                            InfoRow(
                                label = "Filhos",
                                value = filhosTexto,
                                icon = Icons.Default.ChildCare
                            )
                        }
                    }
                }
            }
        }
        
        // Modal de confirmação de exclusão
        if (mostrarModalConfirmacao && state.pessoa != null) {
            ModalConfirmacaoExclusao(
                nomePessoa = state.pessoa!!.nome,
                isLoading = state.isLoading,
                onConfirmar = {
                    viewModel.deletarPessoa(pessoaId) {
                        onNavigateBack()
                    }
                },
                onCancelar = {
                    viewModel.fecharModalConfirmacao()
                }
            )
        }
    }
}

/**
 * Modal de confirmação para exclusão de pessoa
 */
@Composable
private fun ModalConfirmacaoExclusao(
    nomePessoa: String,
    isLoading: Boolean,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onCancelar() },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Confirmar Exclusão",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tem certeza que deseja excluir permanentemente o cadastro de:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = nomePessoa,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Esta ação não pode ser desfeita e removerá completamente o cadastro do banco de dados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Excluir")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Linha de informação com ícone
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

