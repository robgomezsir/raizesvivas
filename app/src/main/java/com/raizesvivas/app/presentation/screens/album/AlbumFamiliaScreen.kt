package com.raizesvivas.app.presentation.screens.album

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
// HorizontalPager não disponível nesta versão, usando LazyRow como alternativa
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.raizesvivas.app.domain.model.FotoAlbum
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.ImagePicker
import com.raizesvivas.app.presentation.ui.theme.FabDefaults
import com.raizesvivas.app.presentation.ui.theme.fabContainerColor
import com.raizesvivas.app.presentation.ui.theme.fabContentColor
import com.raizesvivas.app.presentation.ui.theme.fabElevation
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Tela de Álbum de Família
 * Exibe fotos em formato de livro interativo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumFamiliaScreen(
    viewModel: AlbumFamiliaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val fotos by viewModel.fotos.collectAsState()
    val pessoas by viewModel.pessoas.collectAsState()
    
    // Estados para modais
    var descricaoFoto by remember { mutableStateOf("") }
    var fotoExpandida by remember { mutableStateOf<FotoAlbum?>(null) }
    
    // Estado para pesquisa
    var queryPesquisa by remember { mutableStateOf("") }
    var mostrarPesquisa by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Agrupar fotos por pessoa (com pesquisa)
    val fotosPorPessoa = remember(fotos, pessoas, queryPesquisa) {
        Timber.d("🖼️ Agrupando fotos: ${fotos.size} fotos, ${pessoas.size} pessoas")
        
        // Se não há fotos, retornar vazio
        if (fotos.isEmpty()) {
            Timber.d("📚 Nenhuma foto para agrupar")
            return@remember emptyList()
        }
        
        // Filtrar pessoas por pesquisa (se houver query)
        val pessoasFiltradas = if (queryPesquisa.isNotBlank()) {
            val queryLower = queryPesquisa.lowercase().trim()
            pessoas.filter { pessoa ->
                pessoa.nome.lowercase().contains(queryLower, ignoreCase = true)
            }
        } else {
            pessoas
        }
        
        // Mapear pessoas para suas fotos
        val resultado = mutableListOf<Pair<Pessoa, List<FotoAlbum>>>()
        
        pessoasFiltradas.forEach { pessoa ->
            val fotosDaPessoa = fotos.filter { it.pessoaId == pessoa.id }
            Timber.d("👤 Pessoa ${pessoa.nome} (${pessoa.id}): ${fotosDaPessoa.size} fotos")
            if (fotosDaPessoa.isNotEmpty()) {
                fotosDaPessoa.forEach { foto ->
                    Timber.d("   📷 Foto: ${foto.id}, pessoaId: ${foto.pessoaId}, pessoaNome: ${foto.pessoaNome}")
                }
                resultado.add(pessoa to fotosDaPessoa)
            }
        }
        
        // Se há fotos mas nenhuma pessoa correspondente, criar entradas para pessoas das fotos
        val pessoasIdsComFotos = fotos.map { it.pessoaId }.distinct()
        val pessoasSemCorrespondencia = pessoasIdsComFotos.filter { pessoaId ->
            !pessoasFiltradas.any { it.id == pessoaId }
        }
        
        if (pessoasSemCorrespondencia.isNotEmpty()) {
            Timber.w("⚠️ Encontradas ${pessoasSemCorrespondencia.size} fotos de pessoas não encontradas na lista")
            pessoasSemCorrespondencia.forEach { pessoaId ->
                val fotosDaPessoa = fotos.filter { it.pessoaId == pessoaId }
                val primeiraFoto = fotosDaPessoa.first()
                
                // Se há pesquisa, verificar se o nome da pessoa nas fotos corresponde à pesquisa
                val correspondePesquisa = if (queryPesquisa.isNotBlank()) {
                    val queryLower = queryPesquisa.lowercase().trim()
                    primeiraFoto.pessoaNome.lowercase().contains(queryLower, ignoreCase = true)
                } else {
                    true
                }
                
                if (correspondePesquisa) {
                    Timber.w("   📷 Pessoa não encontrada: ${primeiraFoto.pessoaNome} (${pessoaId}) - ${fotosDaPessoa.size} fotos")
                    
                    // Criar uma pessoa temporária para exibir as fotos
                    val pessoaTemporaria = Pessoa(
                        id = pessoaId,
                        nome = primeiraFoto.pessoaNome,
                        familias = listOf(primeiraFoto.familiaId),
                        criadoPor = "",
                        criadoEm = java.util.Date()
                    )
                    resultado.add(pessoaTemporaria to fotosDaPessoa)
                }
            }
        }
        
        Timber.d("📚 Total de páginas (pessoas com fotos): ${resultado.size}")
        resultado
    }
    
    // Log quando fotos mudarem
    LaunchedEffect(fotos.size) {
        Timber.d("📸 Estado de fotos mudou: ${fotos.size} fotos no total")
        fotos.forEach { foto ->
            Timber.d("  - Foto: ${foto.id}, pessoa: ${foto.pessoaNome}, URL: ${foto.url.take(50)}...")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Álbum de Família",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    // Ícone de pesquisa
                    IconButton(onClick = { mostrarPesquisa = !mostrarPesquisa }) {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "Pesquisar familiar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB sempre visível para adicionar foto
            FloatingActionButton(
                onClick = { viewModel.abrirModalAdicionar() },
                modifier = Modifier.padding(
                    end = 4.dp,
                    bottom = 4.dp
                ),
                containerColor = fabContainerColor(),
                contentColor = fabContentColor(),
                elevation = fabElevation()
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = if (fotos.isEmpty()) "Adicionar primeira foto" else "Adicionar foto"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.carregando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                fotos.isEmpty() -> {
                    EmptyAlbumState(
                        onAdicionarFoto = { viewModel.abrirModalAdicionar() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                fotosPorPessoa.isEmpty() -> {
                    // Há fotos mas nenhuma pessoa correspondente - exibir fotos mesmo assim
                    Timber.w("⚠️ Há ${fotos.size} fotos mas nenhuma pessoa correspondente. Exibindo fotos diretamente...")
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Fotos do Álbum",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(fotos) { foto ->
                                FotoThumbnail(
                                    foto = foto,
                                    index = fotos.indexOf(foto) + 1,
                                    total = fotos.size,
                                    onClick = {
                                        Timber.d("📸 Foto clicada: ${foto.id}, ${foto.pessoaNome}")
                                        fotoExpandida = foto
                                        Timber.d("📸 fotoExpandida definida: ${fotoExpandida?.id}")
                                    },
                                    onLongPress = { 
                                        Timber.d("📸 Foto pressionada longamente: ${foto.id}")
                                        viewModel.abrirModalDeletar(foto) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.75f)
                                )
                            }
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Barra de pesquisa (se ativada)
                        AnimatedVisibility(
                            visible = mostrarPesquisa,
                            enter = slideInHorizontally() + fadeIn(),
                            exit = slideOutHorizontally() + fadeOut()
                        ) {
                            TextField(
                                value = queryPesquisa,
                                onValueChange = { queryPesquisa = it },
                                label = { Text("Pesquisar familiar") },
                                placeholder = { Text("Digite o nome do familiar...") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    if (queryPesquisa.isNotBlank()) {
                                        IconButton(onClick = { queryPesquisa = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Limpar pesquisa")
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        AlbumBookView(
                            fotosPorPessoa = fotosPorPessoa,
                            onFotoClick = { foto ->
                                Timber.d("📸 Foto clicada no livro: ${foto.id}, ${foto.pessoaNome}")
                                fotoExpandida = foto
                                Timber.d("📸 fotoExpandida definida: ${fotoExpandida?.id}")
                            },
                            onFotoLongPress = { foto ->
                                viewModel.abrirModalDeletar(foto)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }
                }
            }
            
            // Snackbar para erros
            state.erro?.let { erro ->
                LaunchedEffect(erro) {
                    scope.launch {
                        // Erro será mostrado via Snackbar
                    }
                }
            }
            
            // Modal de adicionar foto
            LaunchedEffect(state.mostrarModalAdicionar) {
                Timber.d("📱 Estado do modal adicionar mudou: ${state.mostrarModalAdicionar}")
            }
            if (state.mostrarModalAdicionar) {
                ModalAdicionarFoto(
                    pessoas = pessoas,
                    fotosPorPessoa = fotosPorPessoa,
                    carregando = state.carregando,
                    onDismiss = { viewModel.fecharModalAdicionar() },
                    onPessoaSelecionada = { },
                    onImageSelected = { },
                    descricao = descricaoFoto,
                    onDescricaoChange = { desc: String ->
                        descricaoFoto = desc
                    },
                    onConfirmar = { pessoa: Pessoa?, path: String?, desc: String ->
                        if (pessoa != null && path != null) {
                            viewModel.adicionarFoto(path, pessoa.id, desc)
                            descricaoFoto = ""
                        }
                    }
                )
            }
            
            // Modal de deletar foto
            LaunchedEffect(state.mostrarModalDeletar) {
                Timber.d("📱 Estado do modal deletar mudou: ${state.mostrarModalDeletar}")
            }
            if (state.mostrarModalDeletar && state.fotoSelecionadaParaDeletar != null) {
                ModalDeletarFoto(
                    foto = state.fotoSelecionadaParaDeletar!!,
                    onConfirmar = {
                        viewModel.deletarFoto(state.fotoSelecionadaParaDeletar!!)
                    },
                    onCancelar = { viewModel.fecharModalDeletar() }
                )
            }
            
            // Modal de foto expandida com zoom
            fotoExpandida?.let { foto ->
                Timber.d("📸 Exibindo modal para foto: ${foto.id}, ${foto.pessoaNome}")
                ModalFotoExpandida(
                    foto = foto,
                    onDismiss = {
                        Timber.d("📸 Fechando modal de foto")
                        fotoExpandida = null
                    },
                    onDeletar = {
                        Timber.d("📸 Deletar foto do modal: ${foto.id}")
                        viewModel.abrirModalDeletar(foto)
                    }
                )
            }
        }
    }
}

/**
 * Estado vazio do álbum
 */
@Composable
fun EmptyAlbumState(
    onAdicionarFoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Álbum vazio",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Adicione fotos para começar o álbum da família",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAdicionarFoto) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar primeira foto")
        }
    }
}

/**
 * Visualização do álbum estilo livro com animações
 */
@Composable
fun AlbumBookView(
    fotosPorPessoa: List<Pair<Pessoa, List<FotoAlbum>>>,
    onFotoClick: (FotoAlbum) -> Unit,
    onFotoLongPress: (FotoAlbum) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var currentPage by remember { mutableIntStateOf(0) }
    
    // Detectar página atual baseado no scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                currentPage = index
            }
    }
    
    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(fotosPorPessoa.size) { index ->
            val (pessoa, fotos) = fotosPorPessoa[index]
            val isCurrentPage = index == currentPage
            
            // Animar apenas páginas próximas
            val shouldAnimate = kotlin.math.abs(index - currentPage) <= 1
            
            if (shouldAnimate) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(
                        initialOffsetX = { if (index > currentPage) it else -it },
                        animationSpec = spring(dampingRatio = 0.8f)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutHorizontally(
                        targetOffsetX = { if (index > currentPage) it else -it },
                        animationSpec = spring(dampingRatio = 0.8f)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    BookPage(
                        pessoa = pessoa,
                        fotos = fotos,
                        onFotoClick = onFotoClick,
                        onFotoLongPress = onFotoLongPress,
                        modifier = Modifier
                            .fillParentMaxWidth(0.9f)
                            .fillMaxHeight(),
                        isCurrentPage = isCurrentPage
                    )
                }
            } else {
                // Renderizar sem animação para páginas distantes
                BookPage(
                    pessoa = pessoa,
                    fotos = fotos,
                    onFotoClick = onFotoClick,
                    onFotoLongPress = onFotoLongPress,
                    modifier = Modifier
                        .fillParentMaxWidth(0.9f)
                        .fillMaxHeight(),
                    isCurrentPage = isCurrentPage
                )
            }
        }
    }
}

/**
 * Página do livro
 */
@Composable
fun BookPage(
    pessoa: Pessoa,
    fotos: List<FotoAlbum>,
    onFotoClick: (FotoAlbum) -> Unit,
    onFotoLongPress: (FotoAlbum) -> Unit,
    modifier: Modifier = Modifier,
    isCurrentPage: Boolean = true
) {
    val elevation by animateFloatAsState(
        targetValue = if (isCurrentPage) 8f else 2f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "elevation"
    )
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabeçalho da página
                Text(
                    text = pessoa.nome,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // Grid de fotos (2 colunas)
                val fotosChunked = remember(fotos) {
                    fotos.chunked(2)
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(fotosChunked.size) { rowIndex ->
                        val rowFotos = fotosChunked[rowIndex]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowFotos.forEachIndexed { colIndex, foto ->
                                val globalIndex = rowIndex * 2 + colIndex
                                FotoThumbnail(
                                    foto = foto,
                                    index = globalIndex + 1,
                                    total = fotos.size,
                                    onClick = { onFotoClick(foto) },
                                    onLongPress = { onFotoLongPress(foto) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.75f)
                                )
                            }
                            // Espaçador se linha incompleta
                            if (rowFotos.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Miniatura de foto
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FotoThumbnail(
    foto: FotoAlbum,
    index: Int,
    total: Int,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(foto.url)
                        .build()
                ),
                contentDescription = foto.descricao,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Overlay com descrição
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = Float.POSITIVE_INFINITY,
                            endY = 0f
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    if (foto.descricao.isNotBlank()) {
                        Text(
                            text = foto.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 2
                        )
                    }
                    Text(
                        text = "$index/$total",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modal para adicionar foto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalAdicionarFoto(
    pessoas: List<Pessoa>,
    fotosPorPessoa: List<Pair<Pessoa, List<FotoAlbum>>>,
    carregando: Boolean = false,
    onDismiss: () -> Unit,
    onPessoaSelecionada: (Pessoa) -> Unit,
    onImageSelected: (String) -> Unit,
    descricao: String,
    onDescricaoChange: (String) -> Unit,
    onConfirmar: (Pessoa?, String?, String) -> Unit
) {
    var pessoaSelecionada by remember { mutableStateOf<Pessoa?>(null) }
    var imagePath by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = {
            // Resetar estado ao fechar
            onDismiss()
        },
        title = { Text("Adicionar Foto ao Álbum") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Seletor de pessoa com dropdown
                var expandedDropdown by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    TextField(
                        value = pessoaSelecionada?.nome ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Selecione a pessoa") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        supportingText = {
                            pessoaSelecionada?.let { pessoa ->
                                val fotosDaPessoa = fotosPorPessoa.find { it.first.id == pessoa.id }?.second?.size ?: 0
                                Text(
                                    text = "$fotosDaPessoa/5 fotos",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        pessoas.forEach { pessoa ->
                            val fotosDaPessoa = fotosPorPessoa.find { it.first.id == pessoa.id }?.second?.size ?: 0
                            val podeAdicionar = fotosDaPessoa < 5
                            
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(pessoa.nome)
                                        Text(
                                            text = "$fotosDaPessoa/5 fotos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (podeAdicionar) 
                                                MaterialTheme.colorScheme.onSurface 
                                            else 
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                onClick = {
                                    if (podeAdicionar) {
                                        pessoaSelecionada = pessoa
                                        onPessoaSelecionada(pessoa)
                                        expandedDropdown = false
                                    }
                                },
                                enabled = podeAdicionar
                            )
                        }
                    }
                }
                
                // Seletor de imagem
                if (pessoaSelecionada != null) {
                    Text(
                        text = "Selecione a foto:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    ImagePicker(
                        imagePath = imagePath,
                        onImageSelected = { path ->
                            imagePath = path
                            onImageSelected(path)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        size = 200
                    )
                    
                    // Campo de descrição
                    TextField(
                        value = descricao,
                        onValueChange = onDescricaoChange,
                        label = { Text("Descrição (opcional)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pessoaSelecionada != null && imagePath != null && !carregando) {
                        onConfirmar(pessoaSelecionada, imagePath, descricao)
                    }
                },
                enabled = pessoaSelecionada != null && imagePath != null && !carregando
            ) {
                if (carregando) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Carregando...")
                    }
                } else {
                    Text("Adicionar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                // Resetar estado ao cancelar
                onDismiss()
            }) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Modal para deletar foto
 */
@Composable
fun ModalDeletarFoto(
    foto: FotoAlbum,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Deletar Foto") },
        text = {
            Column {
                Text("Tem certeza que deseja deletar esta foto?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pessoa: ${foto.pessoaNome}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (foto.descricao.isNotBlank()) {
                    Text(
                        text = "Descrição: ${foto.descricao}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Deletar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Modal de foto expandida com zoom e gestos
 */
@Composable
fun ModalFotoExpandida(
    foto: FotoAlbum,
    onDismiss: () -> Unit,
    onDeletar: (() -> Unit)? = null
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Estado transformável para zoom (pinch) e pan (arrastar)
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        // Zoom com pinch (pinçar para aumentar/diminuir)
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale
        
        // Pan (arrastar) apenas se estiver com zoom
        if (newScale > 1f) {
            val newOffset = offset + panChange
            
            // Limitar offset para manter a imagem dentro da tela
            val maxOffsetX = if (imageSize.width > 0) {
                (imageSize.width * (newScale - 1f) / 2f).coerceAtLeast(0f)
            } else Float.MAX_VALUE
            val maxOffsetY = if (imageSize.height > 0) {
                (imageSize.height * (newScale - 1f) / 2f).coerceAtLeast(0f)
            } else Float.MAX_VALUE
            
            offset = Offset(
                newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
            )
        } else {
            // Reset offset quando voltar ao zoom 1x
            offset = Offset.Zero
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Container para zoom e pan (apenas a imagem)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformableState)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                // Reset zoom ao dar duplo toque
                                scale = if (scale > 1f) 1f else 2f
                                offset = Offset.Zero
                            }
                        )
                    }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(foto.url)
                            .crossfade(true)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                    ),
                    contentDescription = foto.descricao,
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size ->
                            imageSize = size
                        }
                        .scale(scale)
                        .offset {
                            IntOffset(
                                offset.x.roundToInt(),
                                offset.y.roundToInt()
                            )
                        },
                    contentScale = ContentScale.Fit
                )
            }
            
            // Descrição na parte inferior (fora do container transformável, sempre fixa)
            if (foto.descricao.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = foto.pessoaNome,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (foto.descricao.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = foto.descricao,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                        }
                    }
                }
            }
            
            // Barra superior com botões de ação (sempre visível)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.9f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de deletar (esquerda)
                IconButton(
                    onClick = {
                        onDeletar?.invoke()
                        onDismiss()
                    },
                    modifier = Modifier
                        .background(
                            Color.Red.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar foto",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Botão de fechar (direita)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Controles de zoom (se zoom > 1)
            AnimatedVisibility(
                visible = scale > 1f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            scale = (scale - 0.5f).coerceAtLeast(1f)
                            if (scale == 1f) offset = Offset.Zero
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Diminuir zoom",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 8.dp)
                    )
                    
                    IconButton(
                        onClick = {
                            scale = (scale + 0.5f).coerceAtMost(5f)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar zoom",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.FitScreen,
                            contentDescription = "Resetar zoom",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

