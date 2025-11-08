package com.raizesvivas.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.utils.ArvoreRadialLayoutCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.math.*

/**
 * Componente de visualização radial da árvore genealógica (mapa mental)
 * 
 * Família Zero (casal) no centro, membros organizados em círculos por geração
 */
@Composable
fun RadialTreeView(
    pessoas: List<Pessoa>,
    raizId: String?,
    pessoasMap: Map<String, Pessoa>,
    selectedNodeId: String?,
    onNodeClick: (Pessoa) -> Unit,
    onNodeDoubleClick: (Pessoa) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calcular posições radiais
    val posicoes = remember(pessoas, raizId, pessoasMap) {
        ArvoreRadialLayoutCalculator.calcularPosicoesRadiais(
            pessoas = pessoas,
            raizId = raizId,
            pessoasMap = pessoasMap
        )
    }
    
    // Calcular conexões
    val conexoes = remember(posicoes, pessoasMap) {
        ArvoreRadialLayoutCalculator.calcularConexoes(posicoes, pessoasMap)
    }
    
    // Estado para posições dos cards na tela
    val cardPositions = remember { mutableStateMapOf<String, Offset>() }
    
    // Calcular centro da tela para posicionar Família Zero
    val density = LocalDensity.current
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Canvas para desenhar conexões
        Canvas(modifier = Modifier.fillMaxSize()) {
            conexoes.forEach { conexao ->
                val origemPos = cardPositions[conexao.origem.pessoa.id]
                val destinoPos = cardPositions[conexao.destino.pessoa.id]
                
                if (origemPos != null && destinoPos != null) {
                    val cor = when (conexao.tipo) {
                        ArvoreRadialLayoutCalculator.TipoConexao.PAI_FILHO -> Color(0xFF2196F3)
                        ArvoreRadialLayoutCalculator.TipoConexao.MAE_FILHO -> Color(0xFFE91E63)
                        ArvoreRadialLayoutCalculator.TipoConexao.CASAL -> Color(0xFFFF9800)
                    }
                    
                    // Desenhar linha curva (arco)
                    val path = Path().apply {
                        moveTo(origemPos.x, origemPos.y)
                        val controlX = (origemPos.x + destinoPos.x) / 2f
                        val controlY = (origemPos.y + destinoPos.y) / 2f - 50f
                        quadraticBezierTo(controlX, controlY, destinoPos.x, destinoPos.y)
                    }
                    
                    drawPath(
                        path = path,
                        color = cor,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
        
        // Renderizar nós - centralizar no centro da tela
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            posicoes.forEach { posicao ->
                val isSelected = selectedNodeId == posicao.pessoa.id
                val isFamiliaZero = posicao.nivel == 0
                
                // Converter coordenadas radiais (em pixels) para dp
                // Posições radiais são relativas ao centro (0,0)
                // O offset é relativo ao centro do Box
                val xOffset = with(density) { posicao.x.toDp() }
                val yOffset = with(density) { posicao.y.toDp() }
                
                RadialNodeCard(
                    pessoa = posicao.pessoa,
                    posicao = posicao,
                    isSelected = isSelected,
                    isFamiliaZero = isFamiliaZero,
                    onTap = { onNodeClick(posicao.pessoa) },
                    onDoubleTap = { onNodeDoubleClick(posicao.pessoa) },
                    onPositionChanged = { offset ->
                        cardPositions[posicao.pessoa.id] = offset
                    },
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                )
            }
        }
    }
}

/**
 * Card de nó radial
 */
@Composable
private fun RadialNodeCard(
    pessoa: Pessoa,
    @Suppress("UNUSED_PARAMETER") posicao: ArvoreRadialLayoutCalculator.PosicaoNoRadial,
    isSelected: Boolean,
    isFamiliaZero: Boolean,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit,
    onPositionChanged: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Card(
        modifier = modifier
            .size(if (isFamiliaZero) 120.dp else 100.dp)
            .scale(scale)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val centerX = position.x + coordinates.size.width / 2f
                val centerY = position.y + coordinates.size.height / 2f
                onPositionChanged(Offset(centerX, centerY))
            }
            .pointerInput(pessoa.id) {
                var lastTapTime = 0L
                val scope = CoroutineScope(Dispatchers.Main)
                detectTapGestures(
                    onTap = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 300) {
                            // Double tap
                            onDoubleTap()
                            lastTapTime = 0
                        } else {
                            // Primeiro tap - aguardar para ver se é double
                            val tapTime = currentTime
                            lastTapTime = currentTime
                            scope.launch {
                                delay(350)
                                if (lastTapTime == tapTime) {
                                    // Single tap
                                    onTap()
                                    lastTapTime = 0
                                }
                            }
                        }
                    }
                )
            },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isFamiliaZero) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                // Avatar/Foto
                Surface(
                    modifier = Modifier.size(if (isFamiliaZero) 48.dp else 40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = pessoa.nome.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Nome
                Text(
                    text = pessoa.nome.split(" ").take(2).joinToString(" "),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isFamiliaZero) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            // Badge para Família Zero
            if (isFamiliaZero) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

