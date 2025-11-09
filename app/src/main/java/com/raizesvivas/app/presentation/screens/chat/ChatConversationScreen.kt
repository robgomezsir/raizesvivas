package com.raizesvivas.app.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.MensagemChat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de conversa do chat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    destinatarioId: String,
    destinatarioNome: String,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val mensagens by viewModel.mensagens.collectAsState()
    val currentUserId = viewModel.currentUserId
    var textoMensagem by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme
    val mostrarModalLimpar = state.mostrarModalLimparMensagens

    val listState = rememberLazyListState()

    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) {
            listState.animateScrollToItem(mensagens.size - 1)
        }
    }

    LaunchedEffect(destinatarioId, destinatarioNome) {
        viewModel.abrirConversa(destinatarioId, destinatarioNome)
    }

    val backgroundBrush = remember(colorScheme) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.05f),
                colorScheme.background
            )
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            destinatarioNome,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (mensagens.isNotEmpty()) "Online" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    if (mensagens.isNotEmpty()) {
                        IconButton(onClick = { viewModel.abrirModalLimparMensagens() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpar mensagens",
                                tint = colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = mensagens,
                        key = { it.id }
                    ) { mensagem ->
                        MessageBubble(
                            mensagem = mensagem,
                            isOwnMessage = mensagem.remetenteId == currentUserId
                        )
                    }

                    if (mensagens.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        "Nenhuma mensagem ainda",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Envie a primeira mensagem!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                MessageInputBar(
                    texto = textoMensagem,
                    onTextoChange = { textoMensagem = it },
                    onSendClick = {
                        if (textoMensagem.isNotBlank()) {
                            viewModel.enviarMensagem(textoMensagem)
                            textoMensagem = ""
                        }
                    },
                    enabled = !state.isLoading
                )
            }
        }
        
        // Modal de confirmação para limpar mensagens
        if (mostrarModalLimpar) {
            ModalLimparMensagens(
                destinatarioNome = destinatarioNome,
                isLoading = state.isLoading,
                onConfirmar = {
                    viewModel.limparMensagensConversa()
                },
                onCancelar = {
                    viewModel.fecharModalLimparMensagens()
                }
            )
        }
    }
}

/**
 * Modal de confirmação para limpar mensagens da conversa
 */
@Composable
private fun ModalLimparMensagens(
    destinatarioNome: String,
    isLoading: Boolean,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onCancelar() },
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Limpar Mensagens",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tem certeza que deseja limpar todas as mensagens da conversa com $destinatarioNome?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Esta ação não pode ser desfeita e removerá permanentemente todas as mensagens desta conversa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Limpar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Bolha de mensagem
 */
@Composable
private fun MessageBubble(
    mensagem: MensagemChat,
    isOwnMessage: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale("pt", "BR")) }
    val horaFormatada = remember(mensagem.enviadoEm) {
        dateFormat.format(mensagem.enviadoEm)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isOwnMessage) {
                        colorScheme.primary
                    } else {
                        colorScheme.surfaceVariant
                    }
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {
            if (!isOwnMessage) {
                Text(
                    text = mensagem.remetenteNome,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = mensagem.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOwnMessage) {
                    colorScheme.onPrimary
                } else {
                    colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = horaFormatada,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOwnMessage) {
                        colorScheme.onPrimary.copy(alpha = 0.8f)
                    } else {
                        colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )

                if (isOwnMessage && mensagem.lida) {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = "Lida",
                        modifier = Modifier.size(14.dp),
                        tint = colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Barra de entrada de mensagem
 */
@Composable
private fun MessageInputBar(
    texto: String,
    onTextoChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Digite uma mensagem...",
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                enabled = enabled,
                maxLines = 4,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            FloatingActionButton(
                onClick = {
                    if (texto.isNotBlank() && enabled) {
                        onSendClick()
                    }
                },
                modifier = Modifier.size(56.dp),
                containerColor = if (texto.isNotBlank() && enabled) {
                    colorScheme.primary
                } else {
                    colorScheme.surfaceVariant
                },
                contentColor = if (texto.isNotBlank() && enabled) {
                    colorScheme.onPrimary
                } else {
                    colorScheme.onSurfaceVariant
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar"
                )
            }
        }
    }
}
