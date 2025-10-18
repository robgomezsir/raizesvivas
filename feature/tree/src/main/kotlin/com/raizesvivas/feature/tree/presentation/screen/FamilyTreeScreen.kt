package com.raizesvivas.feature.tree.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.raizesvivas.core.ui.components.ErrorScreen
import com.raizesvivas.core.ui.components.LoadingScreen
import com.raizesvivas.core.ui.components.TreeElementIcon
import com.raizesvivas.feature.tree.presentation.viewmodel.FamilyTreeState
import com.raizesvivas.feature.tree.presentation.viewmodel.FamilyTreeViewModel

/**
 * Tela principal da árvore genealógica
 * 
 * Exibe a árvore genealógica da família com visualização
 * interativa dos membros e seus relacionamentos.
 */
@Composable
fun FamilyTreeScreen(
    userId: String,
    familyId: String? = null,
    viewModel: FamilyTreeViewModel = hiltViewModel(),
    onNavigateToAddMember: () -> Unit = {},
    onNavigateToAddRelationship: () -> Unit = {},
    onNavigateToMemberDetail: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    // Carregar árvore quando a tela é criada
    LaunchedEffect(userId, familyId) {
        viewModel.loadFamilyTree(userId, familyId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (state) {
                            is FamilyTreeState.Success -> state.family.nome
                            else -> "Árvore Genealógica"
                        }
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadFamilyTree(userId, familyId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMember,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Membro")
            }
        }
    ) { paddingValues ->
        when (state) {
            is FamilyTreeState.Loading -> {
                LoadingScreen(
                    message = "Carregando árvore genealógica...",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is FamilyTreeState.Success -> {
                FamilyTreeContent(
                    state = state,
                    onMemberClick = { memberId ->
                        viewModel.selectMember(memberId)
                        onNavigateToMemberDetail(memberId)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is FamilyTreeState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.loadFamilyTree(userId, familyId) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                // Estado inicial - não fazer nada
            }
        }
    }
}

@Composable
private fun FamilyTreeContent(
    state: FamilyTreeState.Success,
    onMemberClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Informações da família
        FamilyInfoCard(family = state.family)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Árvore genealógica
        TreeVisualization(
            members = state.members,
            layout = state.layout,
            selectedMemberId = state.selectedMemberId,
            onMemberClick = onMemberClick
        )
    }
}

@Composable
private fun FamilyInfoCard(
    family: com.raizesvivas.core.domain.model.Family,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = family.nome,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tipo: ${family.tipo.value}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            if (family.iconeArvore != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Ícone: ${family.iconeArvore}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TreeVisualization(
    members: List<com.raizesvivas.core.domain.model.Member>,
    layout: com.raizesvivas.core.utils.algorithms.TreeLayoutCalculator.TreeLayout,
    selectedMemberId: String?,
    onMemberClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (members.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum membro encontrado na família",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    // Por enquanto, exibir lista simples dos membros
    // TODO: Implementar visualização de árvore com SVG
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Membros da Família",
            style = MaterialTheme.typography.titleMedium
        )
        
        members.forEach { member ->
            MemberTreeNode(
                member = member,
                isSelected = member.id == selectedMemberId,
                onClick = { onMemberClick(member.id) }
            )
        }
    }
}

@Composable
private fun MemberTreeNode(
    member: com.raizesvivas.core.domain.model.Member,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto do membro
            AsyncImage(
                model = member.fotoUrl,
                contentDescription = "Foto de ${member.nomeCompleto}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Informações do membro
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = member.nomeCompleto,
                    style = MaterialTheme.typography.titleMedium
                )
                
                member.dataNascimento?.let { date ->
                    Text(
                        text = date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Elemento da árvore
            if (member.elementosVisuais.isNotEmpty()) {
                TreeElementIcon(
                    element = member.elementosVisuais.first(),
                    size = 24.dp
                )
            }
        }
    }
}
