package com.raizesvivas.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raizesvivas.app.domain.model.Notificacao
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

/**
 * Modal para exibir notificações ADMIN_MENSAGEM
 * Bloqueia interação até o usuário marcar como lida
 */
@Composable
fun ModalNotificacaoAdmin(
    notificacao: Notificacao,
    onMarcarComoLida: () -> Unit,
    onDownloadClicked: ((Notificacao) -> Unit)? = null
) {
    // Animação de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else (-100).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "slide"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    val density = LocalDensity.current
    
    Dialog(
        onDismissRequest = { /* Não permite fechar sem marcar como lida */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val context = LocalContext.current
            val downloadUrl = remember(notificacao) { notificacao.dadosExtras["downloadUrl"] }
            val hasDownload = !downloadUrl.isNullOrBlank()
            val isUpdate = hasDownload
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = with(density) { offsetY.toPx() }
                        this.alpha = alpha
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUpdate) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Ícone de admin
                    Icon(
                        imageVector = if (isUpdate) Icons.Default.SystemUpdate else Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (isUpdate) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                    
                    // Título
                    Text(
                        text = notificacao.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (isUpdate) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Mensagem
                    Text(
                        text = notificacao.mensagem,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = if (isUpdate) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (hasDownload) {
                        // Botão BAIXAR
                        Button(
                            onClick = {
                                onDownloadClicked?.invoke(notificacao)
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) { /* ignora erro de intent */ }
                                onMarcarComoLida()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BAIXAR")
                        }
                    } else {
                        // Botão de marcar como lida padrão
                        Button(
                            onClick = onMarcarComoLida,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Entendi")
                        }
                    }
                }
            }
        }
    }
}

