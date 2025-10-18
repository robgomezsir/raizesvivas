package com.raizesvivas.feature.gamification.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.core.ui.components.ErrorScreen
import com.raizesvivas.core.ui.components.LoadingScreen
import com.raizesvivas.feature.gamification.presentation.viewmodel.GamificationState
import com.raizesvivas.feature.gamification.presentation.viewmodel.GamificationViewModel

/**
 * Tela de gamificação
 * 
 * Exibe os pontos, nível e conquistas do usuário
 * no sistema de gamificação.
 */
@Composable
fun GamificationScreen(
    userId: String,
    viewModel: GamificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Carregar dados quando a tela é criada
    LaunchedEffect(userId) {
        viewModel.loadUserGamification(userId)
    }
    
    when (state) {
        is GamificationState.Loading -> {
            LoadingScreen(
                message = "Carregando dados de gamificação..."
            )
        }
        is GamificationState.Success -> {
            GamificationContent(
                userPoints = state.userPoints,
                modifier = Modifier.fillMaxSize()
            )
        }
        is GamificationState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadUserGamification(userId) }
            )
        }
        else -> {
            // Estado inicial - não fazer nada
        }
    }
}

@Composable
private fun GamificationContent(
    userPoints: com.raizesvivas.core.domain.model.UserPoints,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = "Gamificação",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Card de nível e pontos
        LevelCard(userPoints = userPoints)
        
        // Card de progresso
        ProgressCard(userPoints = userPoints)
        
        // Card de conquistas
        AchievementsCard(userPoints = userPoints)
    }
}

@Composable
private fun LevelCard(
    userPoints: com.raizesvivas.core.domain.model.UserPoints,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nível ${userPoints.nivelAtual}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${userPoints.pontosTotais} pontos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                // Ícone de nível
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Nível",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(
    userPoints: com.raizesvivas.core.domain.model.UserPoints,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Progresso para o Próximo Nível",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            val progress = userPoints.getProgressToNextLevel()
            val currentLevelPoints = (userPoints.nivelAtual - 1) * 100
            val nextLevelPoints = userPoints.nivelAtual * 100
            
            Text(
                text = "${userPoints.pontosTotais - currentLevelPoints} / ${nextLevelPoints - currentLevelPoints} pontos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AchievementsCard(
    userPoints: com.raizesvivas.core.domain.model.UserPoints,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Conquistas",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Conquistas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "${userPoints.conquistasConquistadas} conquistas conquistadas",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Text(
                text = "Continue explorando sua árvore genealógica para desbloquear mais conquistas!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
