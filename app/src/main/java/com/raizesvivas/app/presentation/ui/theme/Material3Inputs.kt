package com.raizesvivas.app.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shape suave e arredondado para inputs seguindo Material 3
 */
val InputShapeSuave = RoundedCornerShape(20.dp)

/**
 * Cores suaves para inputs seguindo Material 3
 * 
 * As bordas são mais suaves e discretas, com alpha reduzido para um visual mais elegante.
 */
@Composable
fun inputColorsSuaves(): TextFieldColors {
    val colorScheme = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colorScheme.surface.copy(alpha = 0.96f),
        unfocusedContainerColor = colorScheme.surface.copy(alpha = 0.92f),
        disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f),
        // Bordas com gradação suave
        focusedBorderColor = colorScheme.primary.copy(alpha = 0.7f),
        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.25f),
        disabledBorderColor = colorScheme.outline.copy(alpha = 0.12f),
        // Labels e ícones com leve transparência
        focusedLabelColor = colorScheme.primary.copy(alpha = 0.85f),
        unfocusedLabelColor = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        focusedLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        unfocusedLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        disabledLeadingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
        focusedTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        unfocusedTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        disabledTrailingIconColor = colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
        // Texto e placeholders
        focusedTextColor = colorScheme.onSurface,
        unfocusedTextColor = colorScheme.onSurface,
        disabledTextColor = colorScheme.onSurface.copy(alpha = 0.6f),
        focusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        unfocusedPlaceholderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        // Estados de erro com saturação controlada
        errorContainerColor = blend(colorScheme.errorContainer, colorScheme.surface, 0.7f),
        errorBorderColor = colorScheme.error.copy(alpha = 0.75f),
        errorLabelColor = colorScheme.error.copy(alpha = 0.9f),
        errorSupportingTextColor = colorScheme.error.copy(alpha = 0.85f)
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

