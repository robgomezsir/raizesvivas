package com.raizesvivas.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raizesvivas.app.domain.model.Notificacao

/**
 * Modal festivo para exibir notificações de aniversário
 * Aparece no topo da tela com animações e botões de ação
 */
@Composable
fun ModalFestivoAniversario(
    notificacao: Notificacao,
    pessoaNome: String? = null,
    onEnviarMensagem: () -> Unit,
    onIgnorar: () -> Unit,
    onDismiss: () -> Unit
) {
    // Animações
    val infiniteTransition = rememberInfiniteTransition(label = "festive_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Slide in animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else (-200).dp,
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
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 40.dp)
                .graphicsLayer {
                    translationY = offsetY.toPx()
                    this.alpha = alpha
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationZ = rotation
                        scaleX = scale * pulse
                        scaleY = scale * pulse
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                // Gradiente festivo de fundo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFE082), // Amarelo claro
                                    Color(0xFFFFD54F), // Amarelo
                                    Color(0xFFFFC107), // Amarelo médio
                                    Color(0xFFFFB300)  // Amarelo escuro
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Ícones decorativos festivos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .graphicsLayer {
                                        rotationZ = -15f
                                    },
                                tint = Color(0xFFFF6F00)
                            )
                            
                            // Balões festivos
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(3) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Celebration,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer {
                                                rotationZ = (index * 20f - 20f)
                                            },
                                        tint = Color(0xFFE91E63)
                                    )
                                }
                            }
                            
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .graphicsLayer {
                                        rotationZ = 15f
                                    },
                                tint = Color(0xFFFF6F00)
                            )
                        }
                        
                        // Título principal
                        Text(
                            text = notificacao.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000),
                            textAlign = TextAlign.Center
                        )
                        
                        // Mensagem de aniversário
                        Text(
                            text = notificacao.mensagem,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF000000).copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Nome da pessoa (se disponível)
                        pessoaNome?.let { nome ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6F00),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF000000)
                                    )
                                }
                            }
                        }
                        
                        // Botões de ação
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Botão Ignorar
                            OutlinedButton(
                                onClick = onIgnorar,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF000000)
                                ),
                                // Sem bordas - estilo Neon
                                border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ignorar", fontWeight = FontWeight.SemiBold)
                            }
                            
                            // Botão Enviar Mensagem
                            Button(
                                onClick = onEnviarMensagem,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE91E63),
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enviar Mensagem", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

