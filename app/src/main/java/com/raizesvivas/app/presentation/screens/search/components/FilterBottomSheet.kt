package com.raizesvivas.app.presentation.screens.search.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.PessoaFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filtroAtual: PessoaFilter,
    onAplicar: (Genero?, String?, Date?, Date?, Boolean) -> Unit,
    onLimpar: () -> Unit,
    onDismiss: () -> Unit
) {
    var generoSelecionado by remember { mutableStateOf(filtroAtual.genero) }
    var localNascimento by remember { mutableStateOf(filtroAtual.localNascimento ?: "") }
    var dataInicio by remember { mutableStateOf(filtroAtual.dataNascimentoInicio) }
    var dataFim by remember { mutableStateOf(filtroAtual.dataNascimentoFim) }
    var apenasVivos by remember { mutableStateOf(filtroAtual.apenasVivos) }

    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    // DatePicker State (simplificado: dialogs de estado local)
    var showDataInicioPicker by remember { mutableStateOf(false) }
    var showDataFimPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Fechar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gênero
        Text("Gênero", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth()) {
            Genero.values().forEach { genero ->
                FilterChip(
                    selected = generoSelecionado == genero,
                    onClick = { 
                        generoSelecionado = if (generoSelecionado == genero) null else genero 
                    },
                    label = { Text(genero.label) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Local de Nascimento
        OutlinedTextField(
            value = localNascimento,
            onValueChange = { localNascimento = it },
            label = { Text("Local de Nascimento") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Datas de Nascimento
        Text("Data de Nascimento", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dataInicio?.let { dateFormat.format(it) } ?: "",
                onValueChange = { /* Read only via click */ },
                label = { Text("De") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDataInicioPicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Selecionar Data")
                    }
                }
            )
            
            OutlinedTextField(
                value = dataFim?.let { dateFormat.format(it) } ?: "",
                onValueChange = { /* Read only via click */ },
                label = { Text("Até") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDataFimPicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Selecionar Data")
                    }
                }
            )
        }
        
        // DatePickers Dialogs implementation would go here (using DatePickerDialog from Android View or Material3 DatePicker)
        // For brevity, assuming manual input or simple logic is acceptable, but let's add basic DatePicker logic if creating fully.
        // Since `DatePicker` API in M3 Compose is available, but bulky code, we'll skip the full implementation details inside this snippet 
        // and focus on main logic assuming user knows we'd use a picker.
        // Actually, let's implement a simple dialog trigger if requested.
        
        if (showDataInicioPicker) {
            DatePickerDialogSimple(
                initialDate = dataInicio,
                onDateSelected = { dataInicio = it; showDataInicioPicker = false },
                onDismiss = { showDataInicioPicker = false }
            )
        }
        
        if (showDataFimPicker) {
            DatePickerDialogSimple(
                initialDate = dataFim,
                onDateSelected = { dataFim = it; showDataFimPicker = false },
                onDismiss = { showDataFimPicker = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apenas Vivos
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Apenas Vivos", modifier = Modifier.weight(1f))
            Switch(
                checked = apenasVivos,
                onCheckedChange = { apenasVivos = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botões de Ação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    // Reset local
                    generoSelecionado = null
                    localNascimento = ""
                    dataInicio = null
                    dataFim = null
                    apenasVivos = false
                    onLimpar()
                    onDismiss()
                }
            ) {
                Text("Limpar")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    onAplicar(generoSelecionado, localNascimento, dataInicio, dataFim, apenasVivos)
                    onDismiss()
                }
            ) {
                Text("Aplicar")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogSimple(
    initialDate: Date?,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.time ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(Date(it)) }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
