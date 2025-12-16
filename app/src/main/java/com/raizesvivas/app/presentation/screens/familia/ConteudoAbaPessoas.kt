package com.raizesvivas.app.presentation.screens.familia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.presentation.components.PessoaListItem

@Composable
fun ConteudoAbaPessoas(
    viewModel: FamiliaViewModel,
    onNavigateToPessoa: (String) -> Unit,
    termoBusca: String = ""
) {
    val state by viewModel.state.collectAsState()
    
    // Coletar todas as pessoas de todas as famílias
    val todasPessoas = remember(state.familias, state.outrosFamiliares) {
        val pessoasDasFamilias = state.familias.flatMap { familia ->
            buildList {
                familia.conjuguePrincipal?.let { add(it) }
                familia.conjugueSecundario?.let { add(it) }
                addAll(familia.membrosFlatten.map { it.pessoa })
                familia.membrosFlatten.forEach { item ->
                    item.conjuge?.let { add(it) }
                }
                addAll(familia.membrosExtras)
            }
        }
        (pessoasDasFamilias + state.outrosFamiliares).distinctBy { it.id }
    }
    
    // Filtrar pessoas localmente usando o termo de busca da TopAppBar
    val pessoasFiltradas = remember(todasPessoas, termoBusca) {
        if (termoBusca.isBlank()) {
            todasPessoas
        } else {
            val query = termoBusca.lowercase().trim()
            todasPessoas.filter { pessoa ->
                pessoa.nome.lowercase().contains(query) ||
                pessoa.apelido?.lowercase()?.contains(query) == true
            }
        }
    }

    // Lista de pessoas (sem campo de busca interno)
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (pessoasFiltradas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (termoBusca.isBlank()) {
                    "Nenhuma pessoa encontrada."
                } else {
                    "Nenhuma pessoa encontrada para \"$termoBusca\""
                }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(
                items = pessoasFiltradas,
                key = { pessoa -> pessoa.id }
            ) { pessoa ->
                PessoaListItem(
                    pessoa = pessoa,
                    onNavigateToPessoa = onNavigateToPessoa
                )
            }
        }
    }
}

