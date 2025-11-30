package com.raizesvivas.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * Wrapper que adiciona animação de entrada suave para cards
 * 
 * O card aparece com fade in e slide up, criando uma entrada elegante
 * Usa spring animation para um efeito mais natural e fluido
 * 
 * @param delay Delay em milissegundos antes de iniciar a animação
 * @param content Conteúdo a ser animado
 */
@Composable
fun AnimatedCard(
    delay: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (delay > 0) {
            delay(delay.toLong())
        }
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutLinearInEasing
            )
        ) + slideOutVertically(
            targetOffsetY = { -it / 4 },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutLinearInEasing
            )
        )
    ) {
        content()
    }
}
