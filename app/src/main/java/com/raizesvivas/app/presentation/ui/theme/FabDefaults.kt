package com.raizesvivas.app.presentation.ui.theme

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Configurações padrão para FloatingActionButton seguindo Material 3
 * 
 * De acordo com as diretrizes do Material Design 3:
 * - Tamanho padrão: 56dp
 * - Margem da borda: 16dp (mobile) / 24dp (tablet)
 * - Margem acima da barra de navegação: 16dp
 * - Cor padrão: primaryContainer
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
 * Cores padrão para FAB principal
 */
@Composable
fun fabContainerColor() = MaterialTheme.colorScheme.primaryContainer

/**
 * Cores padrão para conteúdo do FAB principal
 */
@Composable
fun fabContentColor() = MaterialTheme.colorScheme.onPrimaryContainer

/**
 * Cores padrão para FAB secundário
 */
@Composable
fun fabSecondaryContainerColor() = MaterialTheme.colorScheme.secondaryContainer

/**
 * Cores padrão para conteúdo do FAB secundário
 */
@Composable
fun fabSecondaryContentColor() = MaterialTheme.colorScheme.onSecondaryContainer

/**
 * Elevação padrão do FAB principal
 */
@Composable
fun fabElevation() = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)

