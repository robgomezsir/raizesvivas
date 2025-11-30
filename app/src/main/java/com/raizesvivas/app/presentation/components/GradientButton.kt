package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Botão primário com gradiente para ações importantes
 * 
 * Usa gradiente horizontal entre primary e secondary para criar profundidade
 * Ideal para CTAs (Call-to-Action) e ações principais
 * 
 * @param text Texto do botão
 * @param onClick Callback quando o botão é clicado
 * @param modifier Modificador para customização
 * @param enabled Se o botão está habilitado
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = MaterialTheme.shapes.large
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        enabled = enabled,
        shape = MaterialTheme.shapes.large
    ) {
        Text(text)
    }
}
