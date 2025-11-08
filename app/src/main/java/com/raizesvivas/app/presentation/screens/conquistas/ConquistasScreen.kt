package com.raizesvivas.app.presentation.screens.conquistas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.CategoriaConquista
import com.raizesvivas.app.presentation.viewmodel.GamificacaoViewModel
import com.raizesvivas.app.presentation.viewmodel.ConquistaComProgresso

/**
 * Tela de Conquistas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConquistasScreen(
    viewModel: GamificacaoViewModel = hiltViewModel()
) {
    @Suppress("UNUSED_VARIABLE")
    val state by viewModel.state.collectAsState()
    val perfil by viewModel.perfil.collectAsState()
    val conquistasComProgresso by viewModel.conquistasComProgresso.collectAsState()
    
    // Estado para filtro de categoria
    var categoriaSelecionada by remember { mutableStateOf<CategoriaConquista?>(null) }
    
    // Filtrar conquistas por categoria
    val conquistasFiltradas = remember(conquistasComProgresso, categoriaSelecionada) {
        if (categoriaSelecionada == null) {
            conquistasComProgresso
        } else {
            conquistasComProgresso.filter { it.conquista.categoria == categoriaSelecionada }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conquistas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção: Progresso Geral
            item {
                ProgressoGeralCard(perfil = perfil)
            }
            
            // Seção: Filtros por Categoria
            item {
                CategoriasTabs(
                    categoriaSelecionada = categoriaSelecionada,
                    onCategoriaSelecionada = { categoriaSelecionada = it }
                )
            }
            
            // Lista de Conquistas
            items(
                items = conquistasFiltradas,
                key = { it.conquista.id }
            ) { conquistaComProgresso ->
                ConquistaCard(conquistaComProgresso = conquistaComProgresso)
            }
        }
    }
}

/**
 * Card de Progresso Geral
 */
@Composable
private fun ProgressoGeralCard(perfil: com.raizesvivas.app.domain.model.PerfilGamificacao?) {
    val perfilAtual = perfil ?: com.raizesvivas.app.domain.model.PerfilGamificacao(
        usuarioId = "",
        nivel = 1,
        xpAtual = 0,
        xpProximoNivel = 500,
        conquistasDesbloqueadas = 0,
        totalConquistas = 14
    )
    
    // Calcular XP total acumulado (aproximação baseada no nível atual)
    val xpTotalAcumulado = calcularXPTotalAcumulado(perfilAtual.nivel, perfilAtual.xpAtual)
    
    // Calcular progresso para o próximo nível
    val progresso = if (perfilAtual.xpProximoNivel > 0) {
        perfilAtual.xpAtual.toFloat() / perfilAtual.xpProximoNivel.toFloat()
    } else {
        0f
    }
    
    val progressoAnimado by animateFloatAsState(
        targetValue = progresso,
        animationSpec = tween(1000),
        label = "progresso"
    )
    
    // Obter níveis alcançados (do nível atual até o nível 1)
    val niveisAlcancados = (1..perfilAtual.nivel).toList()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Seção: Níveis Alcançados com Badges e XP
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Níveis Alcançados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Lista horizontal de níveis alcançados (scrollable se necessário)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(
                        items = niveisAlcancados.reversed(), // Mostrar do maior para o menor
                        key = { it }
                    ) { nivel ->
                        NivelBadgeCard(
                            nivel = nivel,
                            isNivelAtual = nivel == perfilAtual.nivel,
                            xpNivel = calcularXPDoNivel(nivel)
                        )
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            
            // Seção: Progresso para o Próximo Nível
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nível ${perfilAtual.nivel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = obterBrasaoNivel(perfilAtual.nivel),
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${perfilAtual.xpAtual} / ${perfilAtual.xpProximoNivel} XP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Próximo: Nível ${perfilAtual.nivel + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Barra de Progresso Linear
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progressoAnimado },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(progressoAnimado * 100).toInt()}% completo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Faltam ${perfilAtual.xpProximoNivel - perfilAtual.xpAtual} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Informações adicionais
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$xpTotalAcumulado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "XP Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.height(40.dp).width(1.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${perfilAtual.conquistasDesbloqueadas} / ${perfilAtual.totalConquistas}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Conquistas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Card de Badge de Nível
 */
@Composable
private fun NivelBadgeCard(
    nivel: Int,
    isNivelAtual: Boolean,
    xpNivel: Int
) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNivelAtual) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isNivelAtual) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badge do nível
            Text(
                text = obterBrasaoNivel(nivel),
                style = MaterialTheme.typography.displaySmall
            )
            
            // Número do nível
            Text(
                text = "Nível $nivel",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isNivelAtual) FontWeight.Bold else FontWeight.Normal,
                color = if (isNivelAtual) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            // XP do nível
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isNivelAtual) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ) {
                Text(
                    text = "$xpNivel XP",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isNivelAtual) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            if (isNivelAtual) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Atual",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Calcula XP total acumulado baseado no nível atual
 */
private fun calcularXPTotalAcumulado(nivel: Int, xpAtual: Int): Int {
    // Fórmula simplificada: XP acumulado = soma de todos os níveis anteriores + XP atual
    // Cada nível requer aproximadamente 500 XP (ou pode variar)
    var xpTotal = xpAtual
    for (i in 1 until nivel) {
        xpTotal += calcularXPDoNivel(i)
    }
    return xpTotal
}

/**
 * Calcula XP necessário para um nível específico
 */
private fun calcularXPDoNivel(nivel: Int): Int {
    // Fórmula: XP base aumenta conforme o nível
    // Nível 1: 500 XP, Nível 2: 600 XP, etc.
    return 500 + (nivel - 1) * 100
}

/**
 * Tabs de Categorias
 */
@Composable
private fun CategoriasTabs(
    categoriaSelecionada: CategoriaConquista?,
    onCategoriaSelecionada: (CategoriaConquista?) -> Unit
) {
    // Usar LazyRow para permitir scroll horizontal se necessário
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        item {
            // Tab "Todas"
            FilterChip(
                selected = categoriaSelecionada == null,
                onClick = { onCategoriaSelecionada(null) },
                label = { Text("Todas") },
                leadingIcon = if (categoriaSelecionada == null) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }
        
        // Tabs por categoria
        items(CategoriaConquista.values().toList()) { categoria ->
            FilterChip(
                selected = categoriaSelecionada == categoria,
                onClick = { 
                    onCategoriaSelecionada(
                        if (categoriaSelecionada == categoria) null else categoria
                    )
                },
                label = { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(categoria.icone, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            categoria.descricao, 
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                },
                leadingIcon = if (categoriaSelecionada == categoria) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

/**
 * Retorna o brasão/emoji correspondente ao nível
 */
private fun obterBrasaoNivel(nivel: Int): String {
    return when {
        nivel >= 50 -> "👑" // Nível 50+: Coroa
        nivel >= 40 -> "🏆" // Nível 40-49: Troféu
        nivel >= 30 -> "⭐" // Nível 30-39: Estrela
        nivel >= 20 -> "🌟" // Nível 20-29: Estrela brilhante
        nivel >= 10 -> "🎖️" // Nível 10-19: Medalha
        nivel >= 5 -> "🎯" // Nível 5-9: Alvo
        nivel >= 3 -> "📜" // Nível 3-4: Pergaminho
        nivel >= 2 -> "🌱" // Nível 2: Broto
        else -> "🌿" // Nível 1: Folha
    }
}

/**
 * Card de Conquista Individual
 */
@Composable
private fun ConquistaCard(
    conquistaComProgresso: ConquistaComProgresso
) {
    val conquista = conquistaComProgresso.conquista
    val progresso = conquistaComProgresso.progresso
    
    val progressoPercentual = if (progresso.progressoTotal > 0) {
        progresso.progressoAtual.toFloat() / progresso.progressoTotal.toFloat()
    } else {
        0f
    }
    
    val progressoAnimado by animateFloatAsState(
        targetValue = progressoPercentual,
        animationSpec = tween(500),
        label = "progresso_conquista"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (progresso.desbloqueada) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (progresso.desbloqueada) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone da Conquista
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (progresso.desbloqueada) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conquista.icone ?: "🏆",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            
            // Conteúdo
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conquista.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (progresso.desbloqueada) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (progresso.desbloqueada) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "+${conquista.recompensaXP} XP",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                Text(
                    text = conquista.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Barra de Progresso
                if (!progresso.desbloqueada) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progressoAnimado },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${progresso.progressoAtual} / ${progresso.progressoTotal}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        text = "✓ Desbloqueada",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

