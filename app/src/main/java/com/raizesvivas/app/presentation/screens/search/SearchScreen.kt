package com.raizesvivas.app.presentation.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.raizesvivas.app.presentation.components.PessoaListItem
import com.raizesvivas.app.presentation.screens.search.components.FilterBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPessoa: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val pessoasPaginadas = viewModel.pessoasPaginadas.collectAsLazyPagingItems()
    val filtro by viewModel.filtro.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Sincronizar texto da busca com filtro
    LaunchedEffect(filtro.termoBusca) {
        if (searchText != filtro.termoBusca) {
            searchText = filtro.termoBusca
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            FilterBottomSheet(
                filtroAtual = filtro,
                onAplicar = { gen, local, ini, fim, vivos ->
                    viewModel.aplicarFiltros(gen, local, ini, fim, vivos)
                    showFilterSheet = false
                },
                onLimpar = {
                    viewModel.limparFiltros()
                },
                onDismiss = { showFilterSheet = false }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchText,
                        onValueChange = { 
                            searchText = it
                            viewModel.atualizarTermoBusca(it)
                        },
                        placeholder = { Text("Buscar nome...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (filtro.hasFilters() && filtro.copy(termoBusca = "").hasFilters()) {
                                    Badge()
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, "Filtros")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = pessoasPaginadas.itemCount,
                key = { index ->
                    pessoasPaginadas[index]?.id?.takeIf { it.isNotBlank() } ?: "index_$index"
                }
            ) { index ->
                val pessoa = pessoasPaginadas[index]
                if (pessoa != null) {
                    PessoaListItem(
                        pessoa = pessoa,
                        onNavigateToPessoa = onNavigateToPessoa
                    )
                }
            }

            when (pessoasPaginadas.loadState.refresh) {
                is LoadState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Erro ao carregar resultados", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                is LoadState.NotLoading -> {
                    if (pessoasPaginadas.itemCount == 0) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Nenhum resultado encontrado for '${searchText}'")
                            }
                        }
                    }
                }
            }
            
            when (pessoasPaginadas.loadState.append) {
                is LoadState.Loading -> {
                     item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                       Text("Erro ao carregar mais", modifier = Modifier.padding(16.dp))
                    }
                }
                else -> {}
            }
        }
    }
}
