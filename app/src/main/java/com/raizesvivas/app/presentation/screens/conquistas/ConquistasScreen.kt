package com.raizesvivas.app.presentation.screens.conquistas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.CategoriaConquista
import com.raizesvivas.app.domain.model.RankingUsuario
import com.raizesvivas.app.presentation.viewmodel.GamificacaoViewModel
import com.raizesvivas.app.presentation.viewmodel.ConquistaComProgresso
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Tela de Conquistas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConquistasScreen(
    viewModel: GamificacaoViewModel = hiltViewModel()
) {
    val perfil by viewModel.perfil.collectAsState()
    val conquistasComProgresso by viewModel.conquistasComProgresso.collectAsState()
    
    // Estado para filtro de categoria
    var categoriaSelecionada by remember { mutableStateOf<CategoriaConquista?>(null) }
    
    // Estado para filtro "Conquistei" (apenas conquistas completadas)
    var mostrarApenasConquistadas by remember { mutableStateOf(false) }
    
    // Estado para modal de ranking
    var mostrarRanking by remember { mutableStateOf(false) }
    var ranking by remember { mutableStateOf<List<RankingUsuario>>(emptyList()) }
    var isLoadingRanking by remember { mutableStateOf(false) }
    val usuarioIdAtual = remember { 
        viewModel.obterUsuarioIdAtual() ?: ""
    }
    val scope = rememberCoroutineScope()
    
    // Filtrar conquistas por categoria e por status de conclusão
    val conquistasFiltradas = remember(conquistasComProgresso, categoriaSelecionada, mostrarApenasConquistadas) {
        var filtradas = conquistasComProgresso
        
        // Aplicar filtro de categoria
        if (categoriaSelecionada != null) {
            filtradas = filtradas.filter { it.conquista.categoria == categoriaSelecionada }
        }
        
        // Aplicar filtro "Conquistei" (apenas conquistas completadas)
        if (mostrarApenasConquistadas) {
            filtradas = filtradas.filter { it.progresso.concluida }
        }
        
        filtradas
    }
    
    // Função para carregar ranking
    fun carregarRanking() {
        if (usuarioIdAtual.isBlank()) return
        
        scope.launch {
            isLoadingRanking = true
            val resultado = viewModel.buscarRanking(usuarioIdAtual)
            resultado.onSuccess {
                ranking = it
                mostrarRanking = true
            }.onFailure {
                // Tratar erro se necessário
                Timber.e(it, "Erro ao carregar ranking")
            }
            isLoadingRanking = false
        }
    }
    
    // Buscar posição do usuário atual quando o ranking for carregado
    val posicaoRanking = remember(ranking, usuarioIdAtual) {
        ranking.find { it.usuarioId == usuarioIdAtual }?.posicao
    }
    
    // Carregar ranking inicial para mostrar posição (apenas uma vez quando o usuário estiver disponível)
    LaunchedEffect(usuarioIdAtual) {
        if (usuarioIdAtual.isNotBlank() && ranking.isEmpty() && !isLoadingRanking) {
            scope.launch {
                isLoadingRanking = true
                val resultado = viewModel.buscarRanking(usuarioIdAtual)
                resultado.onSuccess {
                    ranking = it
                }
                isLoadingRanking = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Conquistas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                ProgressoGeralCard(
                    perfil = perfil,
                    onRankingClick = { carregarRanking() },
                    posicaoRanking = posicaoRanking
                )
            }
            
            // Seção: Filtros por Categoria e "Conquistei"
            item {
                CategoriasTabs(
                    categoriaSelecionada = categoriaSelecionada,
                    onCategoriaSelecionada = { categoriaSelecionada = it },
                    mostrarApenasConquistadas = mostrarApenasConquistadas,
                    onToggleConquistadas = { mostrarApenasConquistadas = !mostrarApenasConquistadas }
                )
            }
            
            // Mensagem quando não há conquistas
            if (conquistasFiltradas.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🌱",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Text(
                                text = "Ainda não há conquistas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Comece a usar o app para desbloquear suas primeiras conquistas! Cada usuário tem sua própria jornada pessoal.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Lista de Conquistas com otimizações de performance
                items(
                    items = conquistasFiltradas,
                    key = { it.conquista.id },
                    contentType = { "conquista_card" } // Tipo de conteúdo para melhor performance
                ) { conquistaComProgresso ->
                    ConquistaCard(conquistaComProgresso = conquistaComProgresso)
                }
            }
        }
    }
    
    // Modal de Ranking
    if (mostrarRanking) {
        RankingModal(
            ranking = ranking,
            usuarioIdAtual = usuarioIdAtual,
            isLoading = isLoadingRanking,
            onDismiss = { mostrarRanking = false }
        )
    }
}

/**
 * Card de Progresso Geral
 */
@Composable
private fun ProgressoGeralCard(
    perfil: com.raizesvivas.app.domain.model.PerfilGamificacao?,
    onRankingClick: () -> Unit = {},
    posicaoRanking: Int? = null
) {
    // Calcular total dinâmico de conquistas
    val totalConquistasDisponiveis = com.raizesvivas.app.domain.model.SistemaConquistas.obterTodas().size
    
    val perfilAtual = perfil ?: com.raizesvivas.app.domain.model.PerfilGamificacao(
        usuarioId = "",
        nivel = 1,
        xpAtual = 0,
        xpProximoNivel = 500,
        conquistasDesbloqueadas = 0,
        totalConquistas = totalConquistasDisponiveis // Usar total dinâmico
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
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .fillMaxWidth(),
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
                
                HorizontalDivider(
                    modifier = Modifier.height(40.dp).width(1.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                
                // Ranking
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onRankingClick() }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Ranking",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = posicaoRanking?.let { "#$it" } ?: "--",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Ranking",
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
    onCategoriaSelecionada: (CategoriaConquista?) -> Unit,
    mostrarApenasConquistadas: Boolean,
    onToggleConquistadas: () -> Unit
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
                leadingIcon = if (categoriaSelecionada == null && !mostrarApenasConquistadas) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }

        item {
            // Tab "Conquistei"
            FilterChip(
                selected = mostrarApenasConquistadas,
                onClick = onToggleConquistadas,
                label = { Text("Conquistei") },
                leadingIcon = if (mostrarApenasConquistadas) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }
        
        // Tabs por categoria
        items(CategoriaConquista.entries.toList()) { categoria ->
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
        progresso.progresso.toFloat() / progresso.progressoTotal.toFloat()
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
            containerColor = if (progresso.concluida) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (progresso.concluida) 6.dp else 2.dp
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
                color = if (progresso.concluida) {
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
                        fontWeight = if (progresso.concluida) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (progresso.concluida) {
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
                if (!progresso.concluida) {
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
                            text = "${progresso.progresso} / ${progresso.progressoTotal}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        text = "✓ Concluída",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Modal de Ranking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RankingModal(
    ranking: List<RankingUsuario>,
    usuarioIdAtual: String,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ranking de Conquistas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (ranking.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🏆",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "Ainda não há ranking disponível",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp),
                        contentPadding = PaddingValues(horizontal = 1.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = ranking,
                            key = { it.usuarioId },
                            contentType = { "ranking_item" }
                        ) { usuario ->
                            RankingItem(
                                usuario = usuario,
                                isUsuarioAtual = usuario.usuarioId == usuarioIdAtual,
                                isTop3 = usuario.posicao <= 3
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

/**
 * Item do Ranking
 * Layout simplificado: Classificação | Nome (linha 1)
 *                      Pontuação | Nível (linha 2)
 * Sem foto do usuário
 */
@Composable
private fun RankingItem(
    usuario: RankingUsuario,
    isUsuarioAtual: Boolean,
    isTop3: Boolean
) {
    val backgroundColor = when {
        isUsuarioAtual -> MaterialTheme.colorScheme.primaryContainer
        isTop3 -> when (usuario.posicao) {
            1 -> Color(0xFFFFD700).copy(alpha = 0.2f) // Ouro
            2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f) // Prata
            3 -> Color(0xFFCD7F32).copy(alpha = 0.2f) // Bronze
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = when {
        isUsuarioAtual -> MaterialTheme.colorScheme.primary
        isTop3 -> when (usuario.posicao) {
            1 -> Color(0xFFFFD700) // Ouro
            2 -> Color(0xFFC0C0C0) // Prata
            3 -> Color(0xFFCD7F32) // Bronze
            else -> Color.Transparent
        }
        else -> Color.Transparent
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = if (borderColor != Color.Transparent) {
            BorderStroke(2.dp, borderColor)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isTop3) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Linha 1: Classificação | Nome
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${usuario.posicao}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = usuario.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isUsuarioAtual || isTop3) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isUsuarioAtual) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Você",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Linha 2: Pontuação | Nível
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pontuação (repetida na segunda linha conforme solicitado)
                Text(
                    text = "${usuario.xpTotal} XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Nível
                Text(
                    text = "Nível ${usuario.nivel}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

