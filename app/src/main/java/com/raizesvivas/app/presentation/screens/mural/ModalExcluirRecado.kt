package com.raizesvivas.app.presentation.screens.mural

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import com.raizesvivas.app.domain.model.Recado

@Composable
fun ModalExcluirRecado(
    recado: Recado,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit
) {
    val descricaoRecado = recado.titulo
        .takeIf { it.isNotBlank() }
        ?.trim()
        ?: recado.mensagem.trim()
    val recadoHighlight = if (descricaoRecado.length > 60) {
        "${descricaoRecado.take(57)}..."
    } else {
        descricaoRecado
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Excluir recado?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Tem certeza de que deseja excluir o recado \"$recadoHighlight\"? Essa ação não pode ser desfeita.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !isLoading
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}


