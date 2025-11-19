package com.raizesvivas.app.presentation.screens.album

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.domain.model.Pessoa

/**
 * Componente de filtro por pessoa
 */
@Composable
fun FiltroPessoa(
    pessoas: List<Pessoa>,
    pessoaSelecionada: Pessoa?,
    onPessoaSelecionada: (Pessoa?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtrar por pessoa:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            
            if (pessoaSelecionada != null) {
                AssistChip(
                    onClick = { onPessoaSelecionada(null) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Limpar filtro")
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Opção "Todas"
            item {
                FilterChip(
                    selected = pessoaSelecionada == null,
                    onClick = { onPessoaSelecionada(null) },
                    label = { Text("Todas") }
                )
            }
            
            // Pessoas
            items(pessoas) { pessoa ->
                FilterChip(
                    selected = pessoaSelecionada?.id == pessoa.id,
                    onClick = {
                        onPessoaSelecionada(
                            if (pessoaSelecionada?.id == pessoa.id) null else pessoa
                        )
                    },
                    label = { Text(pessoa.nome) }
                )
            }
        }
    }
}

