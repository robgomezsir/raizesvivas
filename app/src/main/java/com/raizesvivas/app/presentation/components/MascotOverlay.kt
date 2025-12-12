package com.raizesvivas.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun MascotOverlay() {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize() // Ocupa toda a tela, mas permite clicks pass through onde não tem mascote
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        val maxHeightPx = with(density) { this@BoxWithConstraints.maxHeight.toPx() }
        val mascotSize = 80.dp
        val mascotSizePx = with(density) { mascotSize.toPx() }

        // Estados de animação
        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }
        val hoverY = remember { Animatable(0f) }

        // Estados de interação e controle
        var isLocked by remember { mutableStateOf(false) }
        var isDragging by remember { mutableStateOf(false) }
        var currentJob by remember { mutableStateOf<Job?>(null) }
        
        // Imagens do mascote
        val mascotImages = remember {
            listOf(
                R.drawable.mascote,
                R.drawable.mascote1,
                R.drawable.mascote2,
                R.drawable.mascote3
            )
        }
        var currentImageIndex by remember { mutableIntStateOf(0) }

        // Função para cancelar o voo atual
        fun cancelFlight() {
            currentJob?.cancel()
            currentJob = null
        }

        // Lógica de Voo Automático
        LaunchedEffect(isLocked, isDragging) {
            // Se estiver travado ou sendo arrastado, não voa automaticamente
            if (isLocked || isDragging) {
                cancelFlight()
                if (isLocked) {
                    // Feedback visual de "travado" (opcional - tremer ou parar hover)
                    hoverY.snapTo(0f)
                }
                return@LaunchedEffect
            }

            // Se não, inicia/retoma o loop de voo
            while (isActive) {
                // 1. Decidir nova posição aleatória
                val targetX = Random.nextFloat() * (maxWidthPx - mascotSizePx)
                val targetY = Random.nextFloat() * (maxHeightPx - mascotSizePx)

                // 2. Voar até lá
                val flightDuration = Random.nextInt(2000, 5000)
                
                // Resetar hover antes de voar
                hoverY.snapTo(0f)
                val easing = FastOutSlowInEasing
                
                // Voar
                launch {
                    offsetX.animateTo(targetX, tween(flightDuration, easing = easing))
                }
                launch {
                    offsetY.animateTo(targetY, tween(flightDuration, easing = easing))
                }.join() // Espera chegar

                // 3. Pousar/Pairar
                val hoverDuration = Random.nextLong(3000, 8000)
                val startTime = System.currentTimeMillis()
                
                while (isActive && System.currentTimeMillis() - startTime < hoverDuration) {
                    hoverY.animateTo(-10f, tween(1000, easing = LinearEasing))
                    hoverY.animateTo(0f, tween(1000, easing = LinearEasing))
                }
            }
        }

        // Renderizar o mascote
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = offsetX.value.toInt(),
                        y = (offsetY.value + hoverY.value).toInt()
                    )
                }
                .size(mascotSize)
                .graphicsLayer {
                    // Feedback visual se estiver travado (escala um pouco menor ou opacidade)
                    scaleX = if (isLocked) 0.9f else 1f
                    scaleY = if (isLocked) 0.9f else 1f
                    alpha = if (isLocked) 0.8f else 1f
                }
                // Gestos: Toque e Arraste
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            isLocked = !isLocked // Travar/Destravar
                        },
                        onTap = {
                            // Trocar imagem
                            currentImageIndex = (currentImageIndex + 1) % mascotImages.size
                            
                            // Se não estiver travado, voar para outro lugar imediatamente
                            if (!isLocked) {
                                // Cancelar job atual e forçar recomposição do LaunchedEffect (hack simples: toggle fake state ou confiar no loop random)
                                // Na verdade, o loop LaunchedEffect(isLocked...) é robusto.
                                // Para forçar voo imediato, podemos apenas alterar o target dentro do loop ou...
                                // Simplificação: O usuário toca, muda a "roupa". Se quiser que ele saia, arrasta ou espera.
                                // Se quisermos forçar:
                                cancelFlight() // Isso vai reiniciar o LaunchedEffect se as keys mudarem, mas keys são isLocked/isDragging.
                                // Melhor não forçar restart complexo agora p/ não brecar a UI.
                            }
                        }
                    )
                }
        ) {
            // Helper para arrastar (hack para chamar snapTo de dentro do onDrag que não é suspend)
            val scope = rememberCoroutineScope()
            
            // Re-aplicar pointerInput do Drag corretamente com acesso ao scope
            // Nota: Modifier.pointerInput acima estava separado para Tap. Vamos juntar ou reescrever o drag.
            // Precisamos do scope PARA DENTRO do onDrag.
            
            // Configuração do ImageLoader para suportar GIFs
            val context = androidx.compose.ui.platform.LocalContext.current
            val imageLoader = remember {
                coil.ImageLoader.Builder(context)
                    .components {
                        if (android.os.Build.VERSION.SDK_INT >= 28) {
                            add(coil.decode.ImageDecoderDecoder.Factory())
                        } else {
                            add(coil.decode.GifDecoder.Factory())
                        }
                    }
                    .build()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                }
                            }
                        )
                    }
            ) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(mascotImages[currentImageIndex])
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Mascote Interativo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
