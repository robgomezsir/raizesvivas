package com.raizesvivas.app.presentation.ui.theme

import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Configurações padrão para FloatingActionButton seguindo Material 3
 * 
 * De acordo com as diretrizes do Material Design 3:
 * - Tamanho padrão: 56dp
 * - Margem da borda: 16dp (mobile) / 24dp (tablet)
 * - Margem acima da barra de navegação: 16dp
 * - Cor padrão: primaryContainer (tema escuro) ou primary (tema claro para maior contraste)
 */
object FabDefaults {
    /**
     * Padding padrão para FAB principal
     * 16dp da borda direita e 16dp acima da barra de navegação
     */
    val padding = 16.dp
    
    /**
     * Tamanho padrão do FAB principal
     */
    val size = 56.dp
}

/**
 * Verifica se o tema atual é claro baseado na cor de background
 */
@Composable
private fun isLightTheme(): Boolean {
    val background = MaterialTheme.colorScheme.background
    // Se o background for mais claro que 0.5 (128/255), consideramos tema claro
    return background.red > 0.5f || background.green > 0.5f || background.blue > 0.5f
}

/**
 * Cores padrão para FAB principal
 * No tema claro, usa primary para maior contraste com o background
 * No tema escuro, usa primaryContainer conforme Material 3
 */
@Composable
fun fabContainerColor(): Color {
    return if (isLightTheme()) {
        // No tema claro, usa primary para maior contraste
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
}

/**
 * Cores padrão para conteúdo do FAB principal
 */
@Composable
fun fabContentColor(): Color {
    return if (isLightTheme()) {
        // No tema claro, usa onPrimary para contraste com primary
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
}

/**
 * Cores padrão para FAB secundário
 * No tema claro, usa secondary para maior contraste
 * No tema escuro, usa secondaryContainer conforme Material 3
 */
@Composable
fun fabSecondaryContainerColor(): Color {
    return if (isLightTheme()) {
        // No tema claro, usa secondary para maior contraste
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
}

/**
 * Cores padrão para conteúdo do FAB secundário
 */
@Composable
fun fabSecondaryContentColor(): Color {
    return if (isLightTheme()) {
        // No tema claro, usa onSecondary para contraste com secondary
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
}

/**
 * Elevação padrão do FAB principal
 */
@Composable
fun fabElevation() = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)


