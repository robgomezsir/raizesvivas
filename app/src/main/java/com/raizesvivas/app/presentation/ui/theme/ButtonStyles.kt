package com.raizesvivas.app.presentation.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Conjunto de conveniências para manter botões consistentes com o novo visual pastel.
 */
object RaizesVivasButtonDefaults {
    val Shape
        @Composable get() = RoundedCornerShape(22.dp)

    val ContentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)

    @Composable
    fun primaryColors(): ButtonColors {
        val colorScheme = MaterialTheme.colorScheme
        return ButtonDefaults.buttonColors(
            containerColor = colorScheme.primary,
            contentColor = colorScheme.onPrimary,
            disabledContainerColor = colorScheme.primary.copy(alpha = 0.32f),
            disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.38f)
        )
    }

    @Composable
    fun tonalColors(): ButtonColors {
        val colorScheme = MaterialTheme.colorScheme
        return ButtonDefaults.buttonColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
            disabledContainerColor = colorScheme.secondaryContainer.copy(alpha = 0.32f),
            disabledContentColor = colorScheme.onSecondaryContainer.copy(alpha = 0.38f)
        )
    }

    @Composable
    fun outlineStroke(): BorderStroke {
        // Sem bordas - estilo Neon
        return BorderStroke(0.dp, Color.Transparent)
    }

    @Composable
    fun interactionSource(): MutableInteractionSource = remember { MutableInteractionSource() }
}

@Composable
fun Modifier.primaryButtonElevation(): Modifier = this // placeholder for future custom elevation logic

