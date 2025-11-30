package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.presentation.theme.RaizesElevation

/**
 * Surface com efeito glassmorphism (vidro fosco)
 * 
 * Cria um efeito de vidro translúcido moderno
 * Requer API 31+ para blur, em versões anteriores renderiza como Surface normal
 * 
 * @param modifier Modificador para customização
 * @param content Conteúdo a ser exibido
 */
@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = RaizesElevation.modal,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
        ) {
            content()
        }
    }
}
