package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.*

/**
 * Dialog de seleção de data usando DatePicker nativo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Date?) -> Unit,
    onDismiss: () -> Unit,
    initialDate: Date? = null
) {
    // Converter Date para millis, ajustando para o início do dia para evitar problemas de timezone
    val initialMillis = remember(initialDate) {
        initialDate?.let { date ->
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        }
    }
    
    // Criar estado do DatePicker com a data inicial
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )
    
    // Garantir que o Dialog apareça acima de tudo
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // Permitir que o DatePicker defina sua própria largura
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .wrapContentSize(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selecione a data",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 16.dp)
                )
                
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // O DatePicker retorna millis em UTC para meia-noite do dia selecionado.
                                // Quando criamos um Date, ele representa aquele instante UTC.
                                // Precisamos garantir que ao formatar, pegamos o dia correto.
                                // Solução: usar Calendar para extrair dia/mês/ano e recriar no timezone local
                                val calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                    timeInMillis = millis
                                }
                                
                                val calendarLocal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, calendarUtc.get(Calendar.YEAR))
                                    set(Calendar.MONTH, calendarUtc.get(Calendar.MONTH))
                                    set(Calendar.DAY_OF_MONTH, calendarUtc.get(Calendar.DAY_OF_MONTH))
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                
                                onDateSelected(calendarLocal.time)
                            } ?: run {
                                onDateSelected(null)
                            }
                            onDismiss()
                        }
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

