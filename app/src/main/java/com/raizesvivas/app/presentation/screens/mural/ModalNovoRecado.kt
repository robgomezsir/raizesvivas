package com.raizesvivas.app.presentation.screens.mural

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.ui.theme.RaizesVivasButtonDefaults
import com.raizesvivas.app.presentation.ui.theme.InputShapeSuave
import com.raizesvivas.app.presentation.ui.theme.inputColorsSuaves
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults

/**
 * Modal para criar um novo recado
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModalNovoRecado(
    pessoas: List<Pessoa>,
    onDismiss: () -> Unit,
    onConfirmar: (titulo: String, mensagem: String, destinatarioId: String?, cor: String) -> Unit,
    isLoading: Boolean
) {
    var titulo by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    var destinatarioSelecionado by remember { mutableStateOf<Pessoa?>(null) }
    var corSelecionada by remember { mutableStateOf("primary") }
    var mostrarSeletorDestinatario by remember { mutableStateOf(false) }
    
    // Obter cores dos inputs uma vez (dentro do contexto @Composable)
    val inputColors = inputColorsSuaves()
    
    val coresDisponiveis = listOf(
        "primary" to "Brisa Hortelã",
        "secondary" to "Lavanda Sereno",
        "tertiary" to "Pôr-do-sol Suave",
        "success" to "Verde Floresta",
        "warning" to "Creme Solar",
        "info" to "Azul Serenidade",
        "error" to "Rosé Intenso"
    )
    
    val isFormValido = titulo.isNotBlank() && mensagem.isNotBlank()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Permite controle total da largura
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 20.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 10.dp,
            shadowElevation = 10.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Novo Recado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                // Aviso sobre vida útil de 24h
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = "Recados duram 24h — administradores podem fixar os especiais.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Formulário
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    // Título
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { novoValor: String -> titulo = novoValor },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Título do Recado") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Title,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        colors = inputColors,
                        shape = InputShapeSuave
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Mensagem
                    OutlinedTextField(
                        value = mensagem,
                        onValueChange = { novoValor: String -> mensagem = novoValor },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        label = { Text("Mensagem") },
                        placeholder = { Text("Escreva sua mensagem aqui...") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Message,
                                contentDescription = null
                            )
                        },
                        maxLines = 8,
                        colors = inputColors,
                        shape = InputShapeSuave
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Destinatário
                    Column {
                        Text(
                            text = "Destinatário",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Box {
                            OutlinedTextField(
                                value = destinatarioSelecionado?.nome ?: "Todos (Recado Geral)",
                                onValueChange = { },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        if (destinatarioSelecionado == null) Icons.Default.Public else Icons.Default.Person,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Selecionar destinatário"
                                    )
                                },
                                colors = inputColors,
                                shape = InputShapeSuave
                            )
                            
                            // Overlay clicável sobre o campo
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clickable { mostrarSeletorDestinatario = !mostrarSeletorDestinatario }
                            )
                        }
                        
                        if (mostrarSeletorDestinatario) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                shape = MaterialTheme.shapes.large,
                                tonalElevation = 4.dp,
                                shadowElevation = 6.dp
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    destinatarioSelecionado = null
                                                    mostrarSeletorDestinatario = false
                                                },
                                            color = if (destinatarioSelecionado == null) 
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                            else 
                                                Color.Transparent
                                        ) {
                                            ListItem(
                                                headlineContent = { Text("Todos (Recado Geral)") },
                                                leadingContent = {
                                                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(20.dp))
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        HorizontalDivider()
                                    }
                                    
                                    itemsIndexed(
                                        items = pessoas
                                    ) { index, pessoa ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    destinatarioSelecionado = pessoa
                                                    mostrarSeletorDestinatario = false
                                                },
                                            color = if (destinatarioSelecionado?.id == pessoa.id) 
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                            else 
                                                Color.Transparent
                                        ) {
                                            ListItem(
                                                headlineContent = { Text(pessoa.nome) },
                                                leadingContent = {
                                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        if (index < pessoas.lastIndex) {
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Cor - 2 linhas roláveis na horizontal
                    Column {
                        Text(
                            text = "Cor do Card",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            coresDisponiveis.forEach { (cor, nome) ->
                                val cores = getCoresRecadoParaModal(cor)
                                val isSelecionado = corSelecionada == cor

                                FilterChip(
                                    selected = isSelecionado,
                                    onClick = { corSelecionada = cor },
                                    label = {
                                        Text(
                                            nome,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = cores.first,
                                        selectedLabelColor = cores.third,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.height(36.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                // Botões de ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RaizesVivasButtonDefaults.Shape,
                        border = RaizesVivasButtonDefaults.outlineStroke(),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            onConfirmar(
                                titulo,
                                mensagem,
                                destinatarioSelecionado?.id,
                                corSelecionada
                            )
                        },
                        shape = RaizesVivasButtonDefaults.Shape,
                        colors = RaizesVivasButtonDefaults.primaryColors(),
                        contentPadding = RaizesVivasButtonDefaults.ContentPadding,
                        modifier = Modifier.weight(1f),
                        enabled = isFormValido && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Publicar")
                    }
                }
            }
        }
    }
}

/**
 * Retorna as cores para o seletor de cor no modal
 */
@Composable
private fun getCoresRecadoParaModal(corNome: String): Triple<Color, Color, Color> {
    val palette = getCoresRecado(corNome)
    return Triple(palette.background, palette.accent, palette.content)
}

