package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.domain.model.Pessoa
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PessoaListItem(
    pessoa: Pessoa,
    onNavigateToPessoa: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onNavigateToPessoa(pessoa.id) },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = pessoa.getNomeExibicao(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                val idade = pessoa.calcularIdade()
                val idadeTexto = idade?.let { "$it anos" }
                    ?: pessoa.dataNascimento?.let { data -> dateFormat.format(data) }
                    ?: "Idade não informada"
                val apelido = pessoa.apelido?.takeIf { it.isNotBlank() }
                val linhaSecundaria = when {
                    apelido != null && idadeTexto.isNotBlank() -> "$apelido - $idadeTexto"
                    apelido != null -> apelido
                    else -> idadeTexto
                }
                Text(
                    text = linhaSecundaria,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = "Ver detalhes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onNavigateToPessoa(pessoa.id) }
            )
        }
    }
}
