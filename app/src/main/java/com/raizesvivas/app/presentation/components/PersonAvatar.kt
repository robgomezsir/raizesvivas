package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Avatar de pessoa com gradiente único baseado no ID
 * 
 * Cada pessoa tem um gradiente de cores único gerado a partir do hash do ID
 * Exibe as iniciais do nome quando não há foto
 * 
 * @param personId ID único da pessoa (usado para gerar gradiente)
 * @param personName Nome da pessoa (usado para iniciais)
 * @param size Tamanho do avatar
 * @param modifier Modificador para customização
 */
@Composable
fun PersonAvatar(
    personId: String,
    personName: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val gradientColors = remember(personId) {
        // Gerar gradiente único baseado no ID
        val hue = personId.hashCode() % 360
        listOf(
            Color.hsl(hue.toFloat(), 0.6f, 0.5f),
            Color.hsl((hue + 30) % 360f, 0.6f, 0.6f)
        )
    }
    
    val initials = remember(personName) {
        personName.trim()
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .take(2)
            .ifEmpty { "?" }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(gradientColors),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
