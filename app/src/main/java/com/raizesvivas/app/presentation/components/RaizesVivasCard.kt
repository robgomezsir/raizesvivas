package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raizesvivas.app.presentation.theme.RaizesElevation

/**
 * Card padrão do Raízes Vivas com estilo consistente
 * 
 * Usa elevação adequada e sempre respeita o tema (claro/escuro)
 * Nunca usa cores hardcoded
 * 
 * @param modifier Modificador para customização
 * @param elevation Elevação do card (padrão: RaizesElevation.cardDefault = 6dp)
 * @param onClick Callback opcional para tornar o card clicável
 * @param content Conteúdo do card
 */
@Composable
fun RaizesVivasCard(
    modifier: Modifier = Modifier,
    elevation: Dp = RaizesElevation.cardDefault,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
