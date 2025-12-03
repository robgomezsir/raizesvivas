@file:Suppress("SpellCheckingInspection")

package com.raizesvivas.app.presentation.screens.arvore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.PersonAvatar
import com.raizesvivas.app.presentation.screens.familia.FamiliaViewModel
import timber.log.Timber

// Cores do estilo Raízes Vivas (Nova Paleta Verde)
private val Emerald = Color(0xFF67EF1F) // Color1 - Verde mais claro/vibrante
private val Amber = Color(0xFF34C910) // Color3 - Verde médio
private val Slate = Color(0xFF00A200) // Color5 - Verde escuro
private val Heritage50 = Color(0xFFF5FDF7) // Verde pastel muito claro
private val Heritage900 = Color(0xFF0A3D00) // Verde muito escuro

// Cores para linhagem
private val MaternalColor = Color(0xFF4CAF50) // Verde
private val PaternalColor = Color(0xFFFFA726) // Laranja

/**
 * Tela de Árvore Genealógica Vertical
 * 
 * Visualização Vertical (Cima -> Baixo)
 * Estilo Hierárquico Clássico com curvas suaves.
 * Suporta expansão de ramos e visualização de cônjuges (toggle).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArvoreHierarquicaScreen(
    viewModel: FamiliaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDetalhesPessoa: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Extrair todas as pessoas
    val todasPessoas = remember(state.familias) {
        state.familias.flatMap { familia ->
            val pessoas = mutableSetOf<Pessoa>()
            familia.conjuguePrincipal?.let { pessoas.add(it) }
            familia.conjugueSecundario?.let { pessoas.add(it) }
            familia.membrosFlatten.forEach { item ->
                pessoas.add(item.pessoa)
                item.conjuge?.let { pessoas.add(it) }
            }
            familia.membrosExtras.forEach { pessoas.add(it) }
            pessoas.toList()
        }.distinctBy { it.id }
    }
    
    // Construir estrutura de árvore recursiva
    val arvoreRaiz = remember(todasPessoas) {
        construirArvoreRecursiva(todasPessoas)
    }
    
    // Helper para encontrar irmãos de um nó
    fun encontrarIrmaos(nodeId: String, raiz: NoArvore?): List<String> {
        if (raiz == null) return emptyList()
        val irmaos = mutableListOf<String>()
        
        fun buscarIrmaos(no: NoArvore) {
            no.filhos.forEach { filho ->
                if (filho.pessoa.id == nodeId) {
                    // Encontrou o nó, adicionar os irmãos
                    no.filhos.forEach { irmao ->
                        if (irmao.pessoa.id != nodeId) {
                            irmaos.add(irmao.pessoa.id)
                        }
                    }
                } else {
                    buscarIrmaos(filho)
                }
            }
        }
        
        buscarIrmaos(raiz)
        return irmaos
    }
    
    // Helper para encontrar o caminho de ancestrais até um nó
    fun encontrarCaminhoAncestral(nodeId: String, raiz: NoArvore?): List<String> {
        if (raiz == null) return emptyList()
        val caminho = mutableListOf<String>()
        
        fun buscarCaminho(no: NoArvore): Boolean {
            if (no.pessoa.id == nodeId) {
                caminho.add(no.pessoa.id)
                return true
            }
            
            for (filho in no.filhos) {
                if (buscarCaminho(filho)) {
                    caminho.add(0, no.pessoa.id) // Adicionar no início
                    return true
                }
            }
            
            return false
        }
        
        buscarCaminho(raiz)
        return caminho
    }
    
    // Helper para coletar todos os nós em níveis anteriores ao nó especificado
    fun coletarNosNiveisAnteriores(nodeId: String, raiz: NoArvore?): List<String> {
        if (raiz == null) return emptyList()
        val todosNos = mutableListOf<String>()
        var nivelAlvo = -1
        
        // Primeiro, encontrar o nível do nó
        fun encontrarNivel(no: NoArvore, nivelAtual: Int): Int {
            if (no.pessoa.id == nodeId) return nivelAtual
            for (filho in no.filhos) {
                val nivel = encontrarNivel(filho, nivelAtual + 1)
                if (nivel != -1) return nivel
            }
            return -1
        }
        
        nivelAlvo = encontrarNivel(raiz, 0)
        if (nivelAlvo == -1) return emptyList()
        
        // Coletar todos os nós em níveis anteriores
        fun coletar(no: NoArvore, nivelAtual: Int) {
            if (nivelAtual < nivelAlvo) {
                todosNos.add(no.pessoa.id)
                no.filhos.forEach { coletar(it, nivelAtual + 1) }
            }
        }
        
        coletar(raiz, 0)
        return todosNos
    }
    
    // Estado de expansão dos FILHOS (IDs dos nós com filhos visíveis)
    val expandedNodeIds = remember { mutableStateListOf<String>() }
    
    // Estado de expansão dos CÔNJUGES (IDs dos nós com cônjuge visível)
    val expandedSpouseIds = remember { mutableStateListOf<String>() }
    
    LaunchedEffect(arvoreRaiz) {
        // Árvore inicia totalmente contraída
        // Usuário deve tocar para expandir os nós desejados
    }
    
    // Calcular posições Verticais com base na expansão
    val verticalNodes = remember(arvoreRaiz, expandedNodeIds.toList(), expandedSpouseIds.toList()) {
        if (arvoreRaiz != null) {
            calcularLayoutVertical(arvoreRaiz, expandedNodeIds.toSet(), expandedSpouseIds.toSet())
        } else {
            emptyList()
        }
    }
    
    // Estado para zoom e pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.2f, 4f)
        offset += panChange
    }

    Scaffold(
        containerColor = Heritage50,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Árvore Genealógica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Heritage900
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Heritage900
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Heritage900)
                    }
                    IconButton(onClick = { scale = (scale + 0.5f).coerceAtMost(4f) }) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Heritage900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Heritage50)
                .clip(RoundedCornerShape(0.dp))
                .transformable(state = transformableState)
        ) {
            when {
                state.isLoading && todasPessoas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Emerald)
                    }
                }
                
                arvoreRaiz == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Árvore genealógica incompleta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Heritage900
                            )
                            Text(
                                text = "Adicione a Família Zero (Raiz) para visualizar a árvore.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                else -> {
                    // Canvas Infinito Vertical
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    ) {
                        val density = LocalDensity.current
                        
                        // Centralizar a Raiz inicialmente no topo/centro
                        val startX = constraints.maxWidth / 2f
                        val startY = 100f
                        
                        // 1. Desenhar Conexões (Background)
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            translate(left = startX, top = startY) {
                                verticalNodes.forEach { node ->
                                    node.childrenIds.forEach { childId ->
                                        val childNode = verticalNodes.find { it.id == childId }
                                        if (childNode != null) {
                                            // Ajustar ponto de partida para considerar o cônjuge (largura do card)
                                            // Se o cônjuge estiver visível, o centro do card muda.
                                            // O layout já deve calcular a posição correta do centro do bloco.
                                            
                                            val start = node.position.copy(y = node.position.y + 35f) // Sair de baixo do avatar
                                            val end = childNode.position.copy(y = childNode.position.y - 35f) // Chegar em cima do avatar
                                            
                                            // Cor baseada no gênero do filho (Maternal/Paternal)
                                            val color = when (childNode.genero) {
                                                com.raizesvivas.app.domain.model.Genero.FEMININO -> MaternalColor
                                                com.raizesvivas.app.domain.model.Genero.MASCULINO -> PaternalColor
                                                else -> Slate
                                            }
                                            
                                            // Curva Bezier Vertical
                                            val path = androidx.compose.ui.graphics.Path()
                                            path.moveTo(start.x, start.y)
                                            
                                            val midY = (start.y + end.y) / 2
                                            
                                            // Curva suave vertical
                                            path.cubicTo(
                                                start.x, midY,
                                                end.x, midY,
                                                end.x, end.y
                                            )
                                            
                                            drawPath(
                                                path = path,
                                                color = color,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = 3f,
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 2. Desenhar Nós (Foreground)
                        verticalNodes.forEach { node ->
                            val nodeX = startX + node.position.x
                            val nodeY = startY + node.position.y
                            // Tamanho base do avatar
                            val avatarSize = 70.dp
                            // Largura total do card depende se o cônjuge está visível
                            val isSpouseVisible = expandedSpouseIds.contains(node.id) && node.conjuge != null
                            val cardWidth = if (isSpouseVisible) avatarSize * 2 + 10.dp else avatarSize
                            
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = with(density) { nodeX.toDp() } - (cardWidth / 2),
                                        y = with(density) { nodeY.toDp() } - (avatarSize / 2)
                                    )
                                    .width(cardWidth)
                                    .height(avatarSize + 50.dp) // Espaço extra para nome e label
                            ) {
                                PessoaVerticalCard(
                                    pessoa = node.pessoa,
                                    conjuge = node.conjuge,
                                    corBorda = getCorPorNivel(node.level),
                                    tamanho = avatarSize,
                                    isSpouseVisible = isSpouseVisible,
                                    onSingleTap = { 
                                        // Toggle Cônjuge E Filhos com FOCO HIERÁRQUICO
                                        val isCurrentlyExpanded = expandedNodeIds.contains(node.id) || expandedSpouseIds.contains(node.id)
                                        
                                        if (isCurrentlyExpanded) {
                                            // Recolher tudo
                                            expandedNodeIds.remove(node.id)
                                            expandedSpouseIds.remove(node.id)
                                        } else {
                                            // FOCO HIERÁRQUICO: Colapsar todos os níveis anteriores exceto ancestrais diretos
                                            
                                            // 1. Encontrar caminho ancestral (raiz até este nó)
                                            val caminhoAncestral = encontrarCaminhoAncestral(node.id, arvoreRaiz)
                                            
                                            // 2. Encontrar todos os nós em níveis anteriores
                                            val nosNiveisAnteriores = coletarNosNiveisAnteriores(node.id, arvoreRaiz)
                                            
                                            // 3. Colapsar todos exceto os ancestrais diretos
                                            nosNiveisAnteriores.forEach { noId ->
                                                if (!caminhoAncestral.contains(noId)) {
                                                    expandedNodeIds.remove(noId)
                                                    expandedSpouseIds.remove(noId)
                                                }
                                            }
                                            
                                            // 4. Colapsar irmãos no mesmo nível
                                            val irmaos = encontrarIrmaos(node.id, arvoreRaiz)
                                            irmaos.forEach { irmaoId ->
                                                expandedNodeIds.remove(irmaoId)
                                                expandedSpouseIds.remove(irmaoId)
                                            }
                                            
                                            // 5. Expandir o nó atual
                                            if (node.hasChildren) {
                                                expandedNodeIds.add(node.id)
                                            }
                                            if (node.conjuge != null) {
                                                expandedSpouseIds.add(node.id)
                                            }
                                        }
                                    },
                                    onDoubleTap = {
                                        onNavigateToDetalhesPessoa(node.id)
                                    },
                                    label = getLabelPorNivel(node.level, node.pessoa.genero),
                                    isExpanded = expandedNodeIds.contains(node.id) || expandedSpouseIds.contains(node.id),
                                    hasChildren = node.hasChildren
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Layout Vertical ---

data class VerticalNode(
    val id: String,
    val pessoa: Pessoa,
    val conjuge: Pessoa?,
    val level: Int,
    val position: Offset, // (x, y) - Centro do nó (ou par)
    val childrenIds: List<String>,
    val genero: com.raizesvivas.app.domain.model.Genero?,
    val hasChildren: Boolean
)

fun calcularLayoutVertical(
    raiz: NoArvore, 
    expandedNodeIds: Set<String>, 
    expandedSpouseIds: Set<String>
): List<VerticalNode> {
    val nodes = mutableListOf<VerticalNode>()
    val levelSpacing = 320f // Distância vertical entre gerações (dobrado para melhor visualização)
    val nodeSpacing = 200f // Distância horizontal mínima entre nós irmãos (aumentado para mais espaço)
    val spouseExtraWidth = 80f // Largura extra quando cônjuge está visível
    
    // Calcular a largura necessária para cada subárvore
    fun calcularLarguraSubarvore(no: NoArvore): Float {
        // Se filhos não estão expandidos, largura é a do próprio nó
        if (!expandedNodeIds.contains(no.pessoa.id) || no.filhos.isEmpty()) {
            val baseWidth = nodeSpacing
            return if (expandedSpouseIds.contains(no.pessoa.id) && no.conjuge != null) baseWidth + spouseExtraWidth else baseWidth
        }
        
        // Se expandido, largura é a soma das larguras dos filhos
        val childrenWidth = no.filhos.sumOf { calcularLarguraSubarvore(it).toDouble() }.toFloat()
        
        // A largura do nó pai também deve ser considerada (pode ser maior que a soma dos filhos se tiver poucos filhos)
        val selfWidth = if (expandedSpouseIds.contains(no.pessoa.id) && no.conjuge != null) nodeSpacing + spouseExtraWidth else nodeSpacing
        
        return maxOf(childrenWidth, selfWidth)
    }
    
    fun processNode(no: NoArvore, y: Float, xLeft: Float, width: Float) {
        val xCenter = xLeft + width / 2
        
        nodes.add(
            VerticalNode(
                id = no.pessoa.id,
                pessoa = no.pessoa,
                conjuge = no.conjuge,
                level = no.nivel,
                position = Offset(xCenter, y),
                childrenIds = no.filhos.map { it.pessoa.id },
                genero = no.pessoa.genero,
                hasChildren = no.filhos.isNotEmpty()
            )
        )
        
        // Só processar filhos se estiver expandido
        if (expandedNodeIds.contains(no.pessoa.id)) {
            var currentX = xLeft
            // Centralizar filhos abaixo do pai se a largura total dos filhos for menor que a largura alocada
            // (Isso já é tratado pelo xCenter do pai, mas precisamos distribuir os filhos corretamente no espaço width)
            
            // Recalcular largura total real dos filhos para centralização fina
            val realChildrenWidth = no.filhos.sumOf { calcularLarguraSubarvore(it).toDouble() }.toFloat()
            val startX = xLeft + (width - realChildrenWidth) / 2
            
            currentX = startX
            
            no.filhos.forEach { filho ->
                val childWidth = calcularLarguraSubarvore(filho)
                processNode(filho, y + levelSpacing, currentX, childWidth)
                currentX += childWidth
            }
        }
    }
    
    val totalWidth = calcularLarguraSubarvore(raiz)
    processNode(raiz, 0f, -totalWidth / 2, totalWidth)
    
    return nodes
}

// --- Componentes ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PessoaVerticalCard(
    pessoa: Pessoa,
    conjuge: Pessoa?,
    corBorda: Color,
    tamanho: Dp,
    isSpouseVisible: Boolean,
    onSingleTap: () -> Unit,
    onDoubleTap: () -> Unit,
    label: String? = null,
    isExpanded: Boolean,
    hasChildren: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentWidth()
    ) {
        // Linha com Avatares (Pessoa + Cônjuge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .wrapContentWidth()
                .combinedClickable(
                    onClick = onSingleTap,
                    onDoubleClick = onDoubleTap
                )
        ) {
            // Avatar Principal
            AvatarComBorda(pessoa, corBorda, tamanho)
            
            // Avatar Cônjuge (Animado)
            AnimatedVisibility(
                visible = isSpouseVisible && conjuge != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                if (conjuge != null) {
                    Row {
                        Spacer(modifier = Modifier.width(8.dp))
                        AvatarComBorda(conjuge, corBorda, tamanho)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Nome(s) com fundo
        val nomePrincipal = pessoa.nome.split(" ").first()
        val nomeConjuge = conjuge?.nome?.split(" ")?.first()
        val textoNome = if (isSpouseVisible && nomeConjuge != null) "$nomePrincipal & $nomeConjuge" else nomePrincipal
        
        Surface(
            modifier = Modifier.wrapContentWidth(),
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(6.dp),
            shadowElevation = 2.dp
        ) {
            Text(
                text = textoNome,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Heritage900,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        
        // Label e Indicador de Expansão com fundo
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .padding(top = 2.dp),
            color = Heritage50.copy(alpha = 0.9f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                
                if (hasChildren || conjuge != null) {
                    // Indicador visual de estado (expandido/recolhido)
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Expandido" else "Recolhido",
                        modifier = Modifier.size(14.dp),
                        tint = if (isExpanded) Emerald else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarComBorda(
    pessoa: Pessoa,
    corBorda: Color,
    tamanho: Dp
) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .shadow(4.dp, CircleShape)
            .background(Color.White, CircleShape)
            .border(3.dp, corBorda, CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (pessoa.fotoUrl != null && pessoa.fotoUrl.isNotBlank()) {
            AsyncImage(
                model = pessoa.fotoUrl,
                contentDescription = pessoa.nome,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            PersonAvatar(
                personId = pessoa.id,
                personName = pessoa.nome,
                size = tamanho,
                textSize = (tamanho.value * 0.4).sp
            )
        }
    }
}

fun getLabelPorNivel(nivel: Int, genero: com.raizesvivas.app.domain.model.Genero?): String? {
    val isFemale = genero == com.raizesvivas.app.domain.model.Genero.FEMININO
    return when (nivel) {
        0 -> if (isFemale) "Mãe/Raiz" else "Pai/Raiz"
        1 -> if (isFemale) "Filha" else "Filho"
        2 -> if (isFemale) "Neta" else "Neto"
        else -> "${nivel}ª Geração"
    }
}

// --- Estruturas Auxiliares (Mantidas) ---

data class NoArvore(
    val pessoa: Pessoa,
    val conjuge: Pessoa? = null,
    val filhos: List<NoArvore> = emptyList(),
    val nivel: Int = 0,
    val isMaternal: Boolean = true
)

fun construirArvoreRecursiva(pessoas: List<Pessoa>): NoArvore? {
    if (pessoas.isEmpty()) return null
    val pessoasMap = pessoas.associateBy { it.id }
    val familiaZeroList = pessoas.filter { it.ehFamiliaZero }
    if (familiaZeroList.isEmpty()) return null
    
    val raizPrincipal = familiaZeroList.first()
    val raizConjuge = if (familiaZeroList.size > 1) {
        val otherZero = familiaZeroList.first { it.id != raizPrincipal.id }
        if (raizPrincipal.conjugeAtual == otherZero.id || otherZero.conjugeAtual == raizPrincipal.id) otherZero else null
    } else null
    
    fun construirNo(pessoa: Pessoa, conjuge: Pessoa?, nivel: Int, isMaternal: Boolean): NoArvore {
        val filhosIds = mutableSetOf<String>()
        pessoa.filhos.forEach { filhosIds.add(it) }
        conjuge?.filhos?.forEach { filhosIds.add(it) }
        
        val filhosPorRef = pessoas.filter { p ->
            (p.pai == pessoa.id || p.mae == pessoa.id) || (conjuge != null && (p.pai == conjuge.id || p.mae == conjuge.id))
        }
        filhosPorRef.forEach { filhosIds.add(it.id) }
        
        val filhosNos = filhosIds.mapNotNull { id -> pessoasMap[id] }
            .distinctBy { it.id }
            .map { filho ->
                val conjugeFilhoId = filho.conjugeAtual
                val conjugeFilho = if (conjugeFilhoId != null) pessoasMap[conjugeFilhoId] else null
                construirNo(filho, conjugeFilho, nivel + 1, isMaternal)
            }
            .sortedBy { it.pessoa.dataNascimento }
            
        return NoArvore(pessoa, conjuge, filhosNos, nivel, isMaternal)
    }
    
    return construirNo(raizPrincipal, raizConjuge, 0, true)
}

fun getCorPorNivel(nivel: Int): Color {
    return when (nivel) {
        0 -> Color(0xFF00A200)
        1 -> Color(0xFF67EF1F)
        2 -> Color(0xFF4DDC17)
        else -> Color(0xFF1AB508)
    }
}
