package com.raizesvivas.app.presentation.screens.mural

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raizesvivas.app.domain.model.Recado
import com.raizesvivas.app.presentation.ui.theme.RaizesVivasButtonDefaults
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Modal para fixar/desfixar um recado (apenas admin)
 */
@Composable
fun ModalFixarRecado(
    recado: Recado,
    onDismiss: () -> Unit,
    onConfirmar: (fixado: Boolean, fixadoAte: Date?) -> Unit,
    isLoading: Boolean
) {
    var fixado by remember { mutableStateOf(recado.estaFixadoEValido()) }
    var diasFixacao by remember { mutableStateOf(7) } // Padrão: 7 dias

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 10.dp,
            shadowElevation = 10.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (fixado) "Recado Fixado" else "Fixar Recado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Recado:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = recado.titulo.ifBlank { "(sem título)" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Autor: ${recado.autorNome}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Criado em: ${dateFormat.format(recado.criadoEm)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (fixado) {
                                "Este recado está fixado e visível a todos."
                            } else {
                                "Fixe o recado para mantê-lo no topo por mais tempo."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = fixado,
                        onCheckedChange = { fixado = it }
                    )
                }

                if (fixado) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Fixar por quantos dias?",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )

                        val opcoesDias = listOf(1, 7, 30, -1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            opcoesDias.forEach { dias ->
                                FilterChip(
                                    selected = diasFixacao == dias,
                                    onClick = { diasFixacao = dias },
                                    label = {
                                        Text(
                                            text = if (dias == -1) "Permanente" else "$dias dia${if (dias > 1) "s" else ""}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        val resumo = if (diasFixacao > 0) {
                            val dataFutura = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_MONTH, diasFixacao)
                            }.time
                            "Fixado até: ${dateFormat.format(dataFutura)}"
                        } else {
                            "Fixado permanentemente (não expira)"
                        }
                        Text(
                            text = resumo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "Ao desfixar, o recado será removido automaticamente após 24h da criação.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RaizesVivasButtonDefaults.Shape,
                        border = RaizesVivasButtonDefaults.outlineStroke(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val fixadoAte = if (fixado && diasFixacao > 0) {
                                Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_MONTH, diasFixacao)
                                }.time
                            } else null
                            onConfirmar(fixado, fixadoAte)
                        },
                        enabled = !isLoading,
                        shape = RaizesVivasButtonDefaults.Shape,
                        colors = RaizesVivasButtonDefaults.primaryColors(),
                        contentPadding = RaizesVivasButtonDefaults.ContentPadding,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (fixado) "Confirmar" else "Desfixar")
                    }
                }
            }
        }
    }
}

