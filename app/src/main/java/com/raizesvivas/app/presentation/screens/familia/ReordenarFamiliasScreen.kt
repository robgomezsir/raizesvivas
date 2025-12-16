package com.raizesvivas.app.presentation.screens.familia

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Genero
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState

/**
 * Tela dedicada para reorganização de famílias
 * Permite arrastar e soltar cards para mudar a ordem
 * Exclui automaticamente a Família Zero
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReordenarFamiliasScreen(
    viewModel: FamiliaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Filtrar Família Zero
    val familiasReordenar = remember(state.familias) {
        state.familias.filter { !it.ehFamiliaZero }
    }
    
    // Estado da ordem atual
    var ordemAtual by remember {
        mutableStateOf(familiasReordenar.map { it.id })
    }
    
    // Atualizar ordem quando as famílias mudarem
    LaunchedEffect(familiasReordenar) {
        ordemAtual = familiasReordenar.map { it.id }
    }
    
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        ordemAtual = ordemAtual.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Reorganizar Famílias",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.reordenarFamilias(ordemAtual)
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salvar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Instruções
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pressione e arraste os cards para reorganizar a ordem das famílias",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Lista de famílias
            if (familiasReordenar.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma família para reorganizar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = ordemAtual.size,
                        key = { index -> ordemAtual[index] }
                    ) { index ->
                        ReorderableItem(
                            reorderableLazyListState,
                            key = ordemAtual[index]
                        ) { isDragging ->
                            val familia = familiasReordenar.find { it.id == ordemAtual[index] }
                            if (familia != null) {
                                FamiliaCardReorder(
                                    familia = familia,
                                    isDragging = isDragging,
                                    posicao = index + 1,
                                    modifier = Modifier.longPressDraggableHandle()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card simplificado para reorganização
 */
@Composable
private fun FamiliaCardReorder(
    familia: FamiliaUiModel,
    isDragging: Boolean,
    posicao: Int,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 2.dp,
        label = "elevation"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isDragging) 0.9f else 1f,
        label = "alpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation)
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ícone de drag handle
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Arrastar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            
            // Número da posição
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "$posicao",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Ícone do tipo de família
            Icon(
                imageVector = if (familia.ehFamiliaMonoparental) {
                    Icons.Default.Person
                } else {
                    Icons.Default.FamilyRestroom
                },
                contentDescription = null,
                tint = if (familia.ehFamiliaMonoparental) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.tertiary
                },
                modifier = Modifier.size(28.dp)
            )
            
            // Nome da família
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = familia.nomeExibicao,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Informação adicional
                val membrosCount = familia.membrosExtras.size
                
                Text(
                    text = "$membrosCount ${if (membrosCount == 1) "membro" else "membros"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
