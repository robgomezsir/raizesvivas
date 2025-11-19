package com.raizesvivas.app.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shape suave e arredondado para inputs seguindo Material 3
 */
val InputShapeSuave = RoundedCornerShape(16.dp)

/**
 * Cores pastéis suaves para inputs seguindo Material 3
 * 
 * Inputs sem bordas, com cores pastéis suaves que combinam com o sistema de cores.
 * Visual limpo e moderno seguindo as diretrizes do Material 3.
 */
@Composable
fun inputColorsPastel(): TextFieldColors {
    val colorScheme = MaterialTheme.colorScheme
    return TextFieldDefaults.colors(
        // Cores de fundo pastéis suaves
        focusedContainerColor = colorScheme.primaryContainer.copy(alpha = 0.3f),
        unfocusedContainerColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
        // Sem bordas (transparentes)
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        // Labels
        focusedLabelColor = colorScheme.onSurfaceVariant,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        disabledLabelColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        // Ícones
        focusedLeadingIconColor = colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        focusedTrailingIconColor = colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        // Texto e placeholders
        focusedTextColor = colorScheme.onSurface,
        unfocusedTextColor = colorScheme.onSurface,
        disabledTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
        focusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        // Texto de suporte
        focusedSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        unfocusedSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        // Estados de erro com cores pastéis
        errorContainerColor = colorScheme.errorContainer.copy(alpha = 0.3f),
        errorIndicatorColor = Color.Transparent,
        errorLabelColor = colorScheme.error,
        errorSupportingTextColor = colorScheme.error.copy(alpha = 0.8f),
        errorPlaceholderColor = colorScheme.error.copy(alpha = 0.6f),
        errorTextColor = colorScheme.onErrorContainer
    )
}

/**
 * Cores pastéis suaves para inputs secundários (usando secondaryContainer)
 */
@Composable
fun inputColorsPastelSecundario(): TextFieldColors {
    val colorScheme = MaterialTheme.colorScheme
    return TextFieldDefaults.colors(
        focusedContainerColor = colorScheme.secondaryContainer.copy(alpha = 0.3f),
        unfocusedContainerColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedLabelColor = colorScheme.onSurfaceVariant,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        disabledLabelColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        focusedLeadingIconColor = colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        focusedTrailingIconColor = colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        focusedTextColor = colorScheme.onSurface,
        unfocusedTextColor = colorScheme.onSurface,
        disabledTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
        focusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        focusedSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        unfocusedSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        errorContainerColor = colorScheme.errorContainer.copy(alpha = 0.3f),
        errorIndicatorColor = Color.Transparent,
        errorLabelColor = colorScheme.error,
        errorSupportingTextColor = colorScheme.error.copy(alpha = 0.8f),
        errorPlaceholderColor = colorScheme.error.copy(alpha = 0.6f),
        errorTextColor = colorScheme.onErrorContainer
    )
}

/**
 * Cores pastéis suaves para inputs terciários (usando tertiaryContainer)
 */
@Composable
fun inputColorsPastelTerciario(): TextFieldColors {
    val colorScheme = MaterialTheme.colorScheme
    return TextFieldDefaults.colors(
        focusedContainerColor = colorScheme.tertiaryContainer.copy(alpha = 0.3f),
        unfocusedContainerColor = colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedLabelColor = colorScheme.onSurfaceVariant,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        disabledLabelColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        focusedLeadingIconColor = colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        focusedTrailingIconColor = colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        focusedTextColor = colorScheme.onSurface,
        unfocusedTextColor = colorScheme.onSurface,
        disabledTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
        focusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        focusedSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        unfocusedSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        disabledSupportingTextColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        errorContainerColor = colorScheme.errorContainer.copy(alpha = 0.3f),
        errorIndicatorColor = Color.Transparent,
        errorLabelColor = colorScheme.error,
        errorSupportingTextColor = colorScheme.error.copy(alpha = 0.8f),
        errorPlaceholderColor = colorScheme.error.copy(alpha = 0.6f),
        errorTextColor = colorScheme.onErrorContainer
    )
}

private fun blend(colorA: Color, colorB: Color, ratio: Float): Color {
    val inverse = 1f - ratio
    return Color(
        red = (colorA.red * ratio) + (colorB.red * inverse),
        green = (colorA.green * ratio) + (colorB.green * inverse),
        blue = (colorA.blue * ratio) + (colorB.blue * inverse),
        alpha = (colorA.alpha * ratio) + (colorB.alpha * inverse)
    )
}

