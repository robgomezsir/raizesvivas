package com.raizesvivas.app.presentation.screens.arvore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.ListaHierarquicaArvore
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tela de visualização da árvore genealógica
 * 
 * Suporta dois modos de visualização:
 * - RADIAL: Mapa mental com Família Zero no centro
 * - HIERARQUICO: Árvore tradicional vertical com Família Zero no topo
 * 
 * Funcionalidades:
 * - Zoom e pan
 * - Expandir/recolher nós (modo hierárquico)
 * - Legenda interativa
 * - Popup de informações (double tap)
 * - Painel de informações do nó selecionado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArvoreScreenNew(
    viewModel: ArvoreViewModel = hiltViewModel(),
    onNavigateToDetalhesPessoa: (String) -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onNavigateToCadastroPessoa: () -> Unit = {}
) {
    val pessoas by viewModel.pessoas.collectAsState()
    val state by viewModel.state.collectAsState()
    
    // Cache de pessoasMap para evitar recálculos
    val pessoasMap = remember(pessoas) {
        pessoas.associateBy { it.id }
    }
    
    val isRefreshing = state.isLoading
    val pullToRefreshState = rememberPullToRefreshState()
    
    // Atualizar quando pull-to-refresh for acionado
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing && !isRefreshing) {
            viewModel.recarregar()
        }
    }
    
    // Finalizar refresh quando carregamento terminar
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }
    
    // Estado local para busca
    var mostrarBusca by remember { mutableStateOf(false) }
    var termoBusca by remember { mutableStateOf("") }
    
    // Sincronizar busca com ViewModel
    LaunchedEffect(termoBusca) {
        viewModel.atualizarBusca(termoBusca)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (mostrarBusca) {
                        TextField(
                            value = termoBusca,
                            onValueChange = { termoBusca = it },
                            placeholder = { Text("Buscar pessoas...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    mostrarBusca = false
                                    termoBusca = ""
                                    viewModel.atualizarBusca("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Fechar busca")
                                }
                            }
                        )
                    } else {
                        Text(
                            "Árvore Genealógica",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (!mostrarBusca) {
                        IconButton(onClick = { 
                            mostrarBusca = true
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFE3F2FD),
                            Color(0xFFE8EAF6)
                        )
                    )
                )
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            if (pessoas.isEmpty()) {
                // Estado vazio
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (pessoas.isEmpty()) {
                            "Nenhuma pessoa encontrada"
                        } else {
                            "Nenhuma pessoa para exibir"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (pessoas.isEmpty()) {
                            "Sincronize os dados do Firestore ou adicione pessoas na tela de cadastro"
                        } else {
                            "Ajuste os filtros para ver mais pessoas ou certifique-se de que há uma Família Zero cadastrada"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (pessoas.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.recarregar() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizar do Firestore")
                        }
                    }
                }
            } else {
                // Filtrar pessoas por busca
                val pessoasFiltradas = remember(pessoas, state.termoBusca) {
                    if (state.termoBusca.isBlank()) {
                        pessoas
                    } else {
                        val termo = state.termoBusca.lowercase()
                        pessoas.filter { pessoa ->
                            pessoa.nome.lowercase().contains(termo) ||
                            pessoa.profissao?.lowercase()?.contains(termo) == true
                        }
                    }
                }
                
                // Visualização em lista expandível hierárquica (vertical)
                ListaHierarquicaArvore(
                    pessoas = pessoasFiltradas,
                    pessoasMap = pessoasMap,
                    onPersonClick = { pessoa ->
                        onNavigateToDetalhesPessoa(pessoa.id)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
        }
    }
}

/**
 * Painel de informações do nó selecionado
 */
@Composable
@Suppress("UNUSED_FUNCTION")
fun SelectedNodeInfo(
    pessoa: Pessoa,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            Color(0xFF2196F3), 
                            CircleShape
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Nó Selecionado",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Nome", pessoa.nome, Modifier.weight(1f))
                pessoa.dataNascimento?.let {
                    pessoa.calcularIdade()?.let { idade ->
                        InfoItem("Idade", "$idade anos", Modifier.weight(1f))
                    } ?: Spacer(Modifier.weight(1f))
                } ?: Spacer(Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("ID", pessoa.id.take(8), Modifier.weight(1f))
                if (pessoa.ehFamiliaZero) {
                    InfoItem("Tipo", "Família Zero", Modifier.weight(1f))
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            value,
            fontSize = 13.sp,
            color = Color(0xFF424242)
        )
    }
}

/**
 * Popup de informações da pessoa (double tap)
 */
@Composable
@Suppress("UNUSED_FUNCTION")
private fun PopupInformacoesPessoa(
    pessoa: Pessoa,
    onDismiss: () -> Unit,
    onVerDetalhes: () -> Unit
) {
    // Usar Locale para formatação de data
    val dateFormat = remember { 
        @Suppress("DEPRECATION")
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = pessoa.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InformacaoLinha(label = "Nome completo", valor = pessoa.nome)
                
                pessoa.dataNascimento?.let { data ->
                    val idade = pessoa.calcularIdade()
                    InformacaoLinha(
                        label = "Nascimento",
                        valor = "${dateFormat.format(data)}${idade?.let { " ($it anos)" } ?: ""}"
                    )
                }
                
                pessoa.dataFalecimento?.let { data ->
                    InformacaoLinha(
                        label = "Falecimento",
                        valor = dateFormat.format(data)
                    )
                }
                
                pessoa.estadoCivil?.let { estado ->
                    InformacaoLinha(
                        label = "Estado civil",
                        valor = estado.label
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onVerDetalhes) {
                Text("Ver detalhes completos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

/**
 * Linha de informação no popup
 */
@Composable
private fun InformacaoLinha(
    label: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

