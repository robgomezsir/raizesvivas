package com.raizesvivas.app.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.MensagemChat
import com.raizesvivas.app.presentation.components.RaizesVivasTextField
import com.raizesvivas.app.presentation.ui.theme.FabDefaults
import com.raizesvivas.app.presentation.ui.theme.fabContainerColor
import com.raizesvivas.app.presentation.ui.theme.fabContentColor
import com.raizesvivas.app.presentation.ui.theme.fabElevation
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
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
    val conversaState by viewModel.conversa.collectAsState()
    val currentUserId = viewModel.currentUserId
    var textoMensagem by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme
    val mostrarModalLimpar = state.mostrarModalLimparMensagens

    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing && !state.isRefreshingConversa) {
            viewModel.recarregarConversa()
        }
    }

    LaunchedEffect(state.isRefreshingConversa) {
        if (!state.isRefreshingConversa && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    // Auto-scroll para última mensagem quando nova mensagem chega (se usuário estiver próximo do final)
    LaunchedEffect(conversaState.mensagens.size) {
        val mensagens = conversaState.mensagens
        if (mensagens.isNotEmpty() && !conversaState.isCarregandoMais) {
            val lastIndex = mensagens.lastIndex
            val nearBottom = listState.firstVisibleItemIndex >= (lastIndex - 1)
            if (nearBottom) {
                Timber.d("📱 Auto-scroll até a posição $lastIndex")
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    LaunchedEffect(listState, conversaState.possuiMaisAntigas, conversaState.isCarregandoMais) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collectLatest { index ->
                if (
                    index <= 2 &&
                    conversaState.possuiMaisAntigas &&
                    !conversaState.isCarregandoMais &&
                    !conversaState.isLoadingInicial
                ) {
                    Timber.d("⬆️ Solicitando carregamento de mensagens antigas")
                    viewModel.carregarMensagensAntigas()
                }
            }
    }

    // CRÍTICO: Inicia o listener quando a tela é composta
    // Segue o padrão do possivel.txt: chama iniciarConversa() diretamente
    LaunchedEffect(destinatarioId, currentUserId) {
        val remetenteId = currentUserId
        if (remetenteId != null) {
            Timber.d("🚀 LaunchedEffect: Iniciando conversa - remetenteId=$remetenteId, destinatarioId=$destinatarioId")
            // abrirConversa() já chama iniciarConversa() internamente
            viewModel.abrirConversa(destinatarioId, destinatarioNome)
        } else {
            Timber.e("❌ LaunchedEffect: currentUserId é null")
        }
    }
    
    // CRÍTICO: Limpa quando sai da tela para evitar memory leaks
    // IMPORTANTE: Sempre chame limparConversa() ao sair da tela
    DisposableEffect(Unit) {
        Timber.d("🔍 DisposableEffect: Configurando limpeza para conversa")
        onDispose {
            Timber.d("🧹 DisposableEffect: Limpando conversa ao sair da tela")
            viewModel.limparConversa()
        }
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
                            text = if (conversaState.mensagens.isNotEmpty()) "Online" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    if (conversaState.mensagens.isNotEmpty()) {
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
                if (state.isRefreshingConversa) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                val mensagensDistinct = remember(conversaState.mensagens) {
                    val ordenadas = conversaState.mensagens.sortedBy { it.enviadoEm }
                    val mapa = LinkedHashMap<String, MensagemChat>()
                    ordenadas.forEach { mensagem ->
                        mapa[mensagem.id] = mensagem
                    }
                    mapa.values.toList()
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (conversaState.isCarregandoMais) {
                        item("loading_more") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    items(
                        items = mensagensDistinct,
                        key = { mensagem -> mensagem.id },
                        contentType = { "message" }
                    ) { mensagem ->
                        var mostrarMenu by remember { mutableStateOf(false) }
                        
                        MessageBubble(
                            mensagem = mensagem,
                            isOwnMessage = mensagem.remetenteId == currentUserId,
                            onLongPress = {
                                mostrarMenu = true
                            }
                        )
                        
                        // Menu de contexto para excluir mensagem
                        if (mostrarMenu) {
                            AlertDialog(
                                onDismissRequest = { mostrarMenu = false },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                title = {
                                    Text("Excluir Mensagem")
                                },
                                text = {
                                    Text("Tem certeza que deseja excluir esta mensagem? Esta ação não pode ser desfeita.")
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.deletarMensagem(mensagem.id)
                                            mostrarMenu = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Excluir")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { mostrarMenu = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }
                    }

                    when {
                        conversaState.isLoadingInicial && mensagensDistinct.isEmpty() -> {
                            item("initial_loading") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        mensagensDistinct.isEmpty() -> {
                            item("empty_state") {
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
                                            Icons.AutoMirrored.Filled.Chat,
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

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
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
                    text = "Tem certeza que deseja apagar todas as mensagens que você enviou para $destinatarioNome?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Esta ação não pode ser desfeita e removerá permanentemente apenas as mensagens que você enviou. As mensagens recebidas serão mantidas.",
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
    isOwnMessage: Boolean,
    onLongPress: () -> Unit = {}
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongPress() }
                    )
                },
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
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RaizesVivasTextField(
                value = texto,
                onValueChange = onTextoChange,
                modifier = Modifier.weight(1f),
                label = "",
                placeholder = {
                    Text(
                        "Digite uma mensagem...",
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                enabled = enabled,
                maxLines = 4
            )

            FloatingActionButton(
                onClick = {
                    if (texto.isNotBlank() && enabled) {
                        onSendClick()
                    }
                },
                containerColor = if (texto.isNotBlank() && enabled) {
                    fabContainerColor()
                } else {
                    colorScheme.surfaceVariant
                },
                contentColor = if (texto.isNotBlank() && enabled) {
                    fabContentColor()
                } else {
                    colorScheme.onSurfaceVariant
                },
                elevation = fabElevation()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar"
                )
            }
        }
    }
}
