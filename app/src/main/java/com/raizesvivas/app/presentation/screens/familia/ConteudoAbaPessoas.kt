package com.raizesvivas.app.presentation.screens.familia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.PessoaListItem
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ConteudoAbaPessoas(
    viewModel: FamiliaViewModel,
    onNavigateToPessoa: (String) -> Unit
) {
    val pessoasPaginadas = viewModel.pessoasPaginadas.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(
            count = pessoasPaginadas.itemCount,
            key = pessoasPaginadas.itemKey { it.id }
        ) { index ->
            val pessoa = pessoasPaginadas[index]
            if (pessoa != null) {
                PessoaListItem(
                    pessoa = pessoa,
                    onNavigateToPessoa = onNavigateToPessoa
                )
            }
        }

        // Estado de Loading e Error para append (paginação)
        when (val state = pessoasPaginadas.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Erro ao carregar mais itens", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { pessoasPaginadas.retry() }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }
            is LoadState.NotLoading -> {}
        }
        
        // Estado de Loading e Error para refresh (primeira carga)
        when (val state = pessoasPaginadas.loadState.refresh) {
             is LoadState.Loading -> {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Erro ao carregar lista de pessoas", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { pessoasPaginadas.retry() }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }
            is LoadState.NotLoading -> {
                if (pessoasPaginadas.itemCount == 0) {
                     item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma pessoa encontrada.")
                        }
                    }
                }
            }
        }
    }
}


