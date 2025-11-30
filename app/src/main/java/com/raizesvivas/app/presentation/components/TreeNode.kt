package com.raizesvivas.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raizesvivas.app.domain.model.Pessoa
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Dados do nó da árvore genealógica
 */
data class TreeNodeData(
    val pessoa: Pessoa,
    val conjuge: Pessoa? = null,
    val children: List<TreeNodeData> = emptyList(),
    var isExpanded: Boolean = true,
    val nivel: Int = 0
)

/**
 * Componente TreeNode recursivo baseado no exemplo tree-hierarchy-ui.tsx
 * 
 * Renderiza um nó da árvore com seus filhos de forma hierárquica
 */
@Composable
fun TreeNode(
    node: TreeNodeData,
    selectedNodeId: String?,
    onNodeClick: (Pessoa) -> Unit,
    onToggle: (TreeNodeData) -> Unit,
    modifier: Modifier = Modifier,
    pessoasMap: Map<String, Pessoa> = emptyMap(),
    onPositionChanged: ((String, NodePosition) -> Unit)? = null
) {
    val isSelected = selectedNodeId == node.pessoa.id
    val isCouple = node.conjuge != null
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Card do Nó
        Card(
            modifier = Modifier
                .scale(scale)
                .onGloballyPositioned { coordinates ->
                    // Calcular posição do centro do Card
                    // positionInRoot() retorna posição após transformações (zoom/pan)
                    // Isso é chamado sempre que o card é reposicionado ou transformado
                    val position = coordinates.positionInRoot()
                    val cardCenterX = position.x + coordinates.size.width / 2f
                    val cardCenterY = position.y + coordinates.size.height / 2f
                    
                    // Notificar a posição do centro do Card dinamicamente
                    // Para casais, usar a posição do centro do card do casal
                    onPositionChanged?.invoke(
                        node.pessoa.id,
                        NodePosition(
                            x = cardCenterX,
                            y = cardCenterY
                        )
                    )
                    
                    // Se houver cônjuge, também notificar sua posição (mesma do casal)
                    if (isCouple && node.conjuge != null) {
                        onPositionChanged?.invoke(
                            node.conjuge.id,
                            NodePosition(
                                x = cardCenterX,
                                y = cardCenterY
                            )
                        )
                    }
                }
                .pointerInput(node.pessoa.id) {
                    detectTapGestures(
                        onTap = {
                            // Tap único: expandir/recolher se tiver filhos
                            if (node.children.isNotEmpty()) {
                                onToggle(node)
                            }
                        },
                        onDoubleTap = {
                            // Double tap: sempre abrir popup
                            onNodeClick(node.pessoa)
                        }
                    )
                }
                .shadow(
                    elevation = if (isSelected) 12.dp else 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surface
            ),
            // Sem bordas - estilo Neon
            border = null
        ) {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCouple) {
                    CoupleNodeContent(node.pessoa, node.conjuge!!)
                } else {
                    RegularNodeContent(node.pessoa)
                }
            }
        }
        
        // Botão de Expandir/Recolher
        if (node.children.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            IconButton(
                onClick = { onToggle(node) },
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    // Sem bordas - estilo Neon
            ) {
                @Suppress("DEPRECATION")
                val icon: ImageVector = if (node.isExpanded) {
                    Icons.Default.KeyboardArrowDown
                } else {
                    Icons.Default.KeyboardArrowRight
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle",
                    tint = Color(0xFF424242)
                )
            }
        }
        
        // Filhos
        if (node.children.isNotEmpty() && node.isExpanded) {
            Spacer(Modifier.height(40.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(64.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                node.children.forEach { child ->
                    Box(modifier = Modifier.weight(1f)) {
                        TreeNode(
                            node = child,
                            selectedNodeId = selectedNodeId,
                            onNodeClick = onNodeClick,
                            onToggle = onToggle,
                            pessoasMap = pessoasMap,
                            onPositionChanged = onPositionChanged
                        )
                    }
                }
            }
        }
    }
}

/**
 * Conteúdo do nó para casal (Família Zero)
 */
@Composable
fun CoupleNodeContent(pessoa1: Pessoa, pessoa2: Pessoa) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Parceiro 1
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFCE4EC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                pessoa1.nome,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        
        // Coração
        Icon(
            Icons.Default.Favorite,
            contentDescription = "Love",
            tint = Color(0xFFEF5350),
            modifier = Modifier.size(20.dp)
        )
        
        // Parceiro 2
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE3F2FD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                pessoa2.nome,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Conteúdo do nó regular (pessoa individual)
 */
@Composable
fun RegularNodeContent(pessoa: Pessoa) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 80.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = if (pessoa.dataFalecimento != null) Color.Gray else Color(0xFF2196F3),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            pessoa.nome,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        val idade = pessoa.calcularIdade()
        val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
        val idadeTexto = when {
            idade != null && idade > 0 -> "$idade anos"
            pessoa.dataNascimento != null -> dateFormat.format(pessoa.dataNascimento)
            else -> ""
        }
        val apelido = pessoa.apelido?.takeIf { it.isNotBlank() }
        val linhaSecundaria = when {
            apelido != null && idadeTexto.isNotBlank() -> "$apelido - $idadeTexto"
            apelido != null -> apelido
            else -> idadeTexto
        }
        if (linhaSecundaria.isNotBlank()) {
            Text(
                linhaSecundaria,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        if (pessoa.dataFalecimento != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                "†",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

