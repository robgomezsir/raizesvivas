package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Componente para estados vazios com ícone e mensagem
 * 
 * Exibe uma mensagem amigável quando não há dados para mostrar
 * Inclui ícone, título, descrição e ação opcional
 * 
 * @param icon Ícone a ser exibido
 * @param title Título do estado vazio
 * @param description Descrição opcional
 * @param actionText Texto do botão de ação (opcional)
 * @param onActionClick Callback para o botão de ação
 * @param modifier Modificador para customização
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícone grande
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Título
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        // Descrição (opcional)
        description?.let { desc ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        // Botão de ação (opcional)
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(actionText)
            }
        }
    }
}

/**
 * Estados vazios pré-configurados para uso comum
 */
object EmptyStates {
    @Composable
    fun NoData(
        onActionClick: (() -> Unit)? = null
    ) {
        EmptyState(
            icon = Icons.Default.Inbox,
            title = "Nenhum dado encontrado",
            description = "Não há informações para exibir no momento",
            actionText = if (onActionClick != null) "Adicionar" else null,
            onActionClick = onActionClick
        )
    }
    
    @Composable
    fun NoResults(
        searchQuery: String = ""
    ) {
        EmptyState(
            icon = Icons.Default.SearchOff,
            title = "Nenhum resultado encontrado",
            description = if (searchQuery.isNotEmpty()) {
                "Não encontramos resultados para \"$searchQuery\""
            } else {
                "Tente ajustar sua pesquisa"
            }
        )
    }
    
    @Composable
    fun NoPeople(
        onAddClick: () -> Unit
    ) {
        EmptyState(
            icon = Icons.Default.People,
            title = "Nenhum familiar cadastrado",
            description = "Comece adicionando membros da sua família",
            actionText = "Adicionar Pessoa",
            onActionClick = onAddClick
        )
    }
    
    @Composable
    fun NoPhotos(
        onAddClick: () -> Unit
    ) {
        EmptyState(
            icon = Icons.Default.PhotoLibrary,
            title = "Álbum vazio",
            description = "Adicione fotos para começar o álbum da família",
            actionText = "Adicionar Foto",
            onActionClick = onAddClick
        )
    }
}
