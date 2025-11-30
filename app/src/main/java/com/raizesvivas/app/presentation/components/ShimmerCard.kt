package com.raizesvivas.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Card com efeito shimmer para estado de loading
 * 
 * Cria um efeito de "brilho" pulsante que indica carregamento
 * Usa as cores do tema automaticamente
 * 
 * @param modifier Modificador para customização
 * @param height Altura do card de loading
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .alpha(shimmerAlpha),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Box(modifier = Modifier.fillMaxWidth())
    }
}
