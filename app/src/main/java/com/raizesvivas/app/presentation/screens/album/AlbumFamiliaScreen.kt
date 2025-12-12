package com.raizesvivas.app.presentation.screens.album

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
// HorizontalPager não disponível nesta versão, usando LazyRow como alternativa
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import com.raizesvivas.app.presentation.screens.home.HomeDrawerContent
import com.raizesvivas.app.presentation.theme.LocalThemeController
import com.raizesvivas.app.presentation.viewmodel.NotificacaoViewModel
import com.raizesvivas.app.presentation.theme.ThemeMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalConfiguration
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.raizesvivas.app.domain.model.FotoAlbum
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.presentation.components.ImagePicker
import com.raizesvivas.app.presentation.components.RaizesVivasTextField
import com.raizesvivas.app.presentation.components.AnimatedSearchBar
import com.raizesvivas.app.utils.TextUtils
import com.raizesvivas.app.utils.TimeUtils
import com.raizesvivas.app.utils.rememberHapticFeedback
import com.raizesvivas.app.utils.HapticFeedback
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import kotlin.math.abs

/**
 * Tela de Álbum de Família
 * Exibe fotos em formato de livro interativo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumFamiliaScreen(
    viewModel: AlbumFamiliaViewModel = hiltViewModel(),
    fotoIdParaAbrir: String? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetalhesPessoa: ((String) -> Unit)? = null,
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToGerenciarConvites: () -> Unit = {},
    onNavigateToGerenciarEdicoes: () -> Unit = {},
    onNavigateToResolverDuplicatas: () -> Unit = {},
    onNavigateToGerenciarUsuarios: () -> Unit = {},
    onNavigateToConfiguracoes: () -> Unit = {},
    onNavigateToPoliticaPrivacidade: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val fotos by viewModel.fotos.collectAsState()
    val pessoas by viewModel.pessoas.collectAsState()
    val usuarioAtual by viewModel.usuarioAtual.collectAsState()
    
    // Estados para modais
    var descricaoFoto by remember { mutableStateOf("") }
    var fotoExpandida by remember { mutableStateOf<FotoAlbum?>(null) }
    
    // Auto-expandir foto se vier via deep link
    LaunchedEffect(fotoIdParaAbrir, fotos) {
        if (fotoIdParaAbrir != null && fotos.isNotEmpty() && fotoExpandida == null) {
            val foto = fotos.find { it.id == fotoIdParaAbrir }
            if (foto != null) {
                fotoExpandida = foto
            }
        }
    }
    
    // Estado para pesquisa
    var queryPesquisa by remember { mutableStateOf("") }
    var mostrarPesquisa by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Sidebar States
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val themeController = LocalThemeController.current
    val notificacaoViewModel: NotificacaoViewModel = hiltViewModel()
    val contadorNaoLidas by notificacaoViewModel.contadorNaoLidas.collectAsState()
    
    // Permissões corretas baseadas no usuário atual
    val isAdmin = usuarioAtual?.ehAdmin == true
    val isAdminSenior = usuarioAtual?.ehAdminSenior == true
    
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
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                isAdmin = isAdmin,
                isAdminSenior = isAdminSenior,
                notificacoesNaoLidas = contadorNaoLidas,
                pedidosPendentes = 0,
                onClose = { scope.launch { drawerState.close() } },
                onOpenNotificacoes = { scope.launch { drawerState.close() } },
                onNavigateToPerfil = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToPerfil()
                    }
                },
                onGerenciarConvites = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToGerenciarConvites()
                    }
                },
                onGerenciarEdicoes = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToGerenciarEdicoes()
                    }
                },
                onResolverDuplicatas = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToResolverDuplicatas()
                    }
                },
                onGerenciarUsuarios = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToGerenciarUsuarios()
                    }
                },
                onConfiguracoes = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToConfiguracoes()
                    }
                },
                onNavigateToPoliticaPrivacidade = {
                    scope.launch {
                        drawerState.close()
                        onNavigateToPoliticaPrivacidade()
                    }
                },
                onSair = {
                    scope.launch {
                        drawerState.close()
                        // Logout handled by navigation
                    }
                },
                themeMode = themeController.modo,
                onThemeModeChange = { mode -> 
                    themeController.selecionarModo(mode) 
                }
            )
        }
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (mostrarPesquisa) {
                        AnimatedSearchBar(
                            query = queryPesquisa,
                            onQueryChange = { queryPesquisa = it },
                            isSearchActive = mostrarPesquisa,
                            onSearchActiveChange = { 
                                mostrarPesquisa = it
                                if (!it) queryPesquisa = ""
                            },
                            placeholder = "Pesquisar familiar...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Álbum de Família",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp),
                actions = {
                    if (!mostrarPesquisa) {
                        // Ícone de adicionar foto
                        IconButton(onClick = { viewModel.abrirModalAdicionar() }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = if (fotos.isEmpty()) "Adicionar primeira foto" else "Adicionar foto"
                            )
                        }
                        // Ícone de pesquisa
                        IconButton(onClick = { mostrarPesquisa = true }) {
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = "Pesquisar familiar"
                            )
                        }
                        // Menu lateral
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Abrir menu lateral")
                        }
                    }
                }
            )
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
                else -> {
                    // Feed estilo Instagram
                    val fotosFiltradas = remember(fotos, queryPesquisa) {
                        if (queryPesquisa.isNotBlank()) {
                            val queryLower = queryPesquisa.lowercase().trim()
                            fotos.filter { foto ->
                                foto.pessoaNome.lowercase().contains(queryLower, ignoreCase = true) ||
                                foto.descricao.lowercase().contains(queryLower, ignoreCase = true)
                            }
                        } else {
                            fotos
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(
                            items = fotosFiltradas.sortedByDescending { it.criadoEm.time },
                            key = { it.id }
                        ) { foto ->
                            PostItemInstagram(
                                foto = foto,
                                viewModel = viewModel,
                                onFotoClick = {
                                    fotoExpandida = foto
                                },
                                onFotoLongPress = {
                                    viewModel.abrirModalApoio(foto)
                                },
                                onNavigateToDetalhesPessoa = onNavigateToDetalhesPessoa
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                thickness = 8.dp
                            )
                        }
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
                    carregando = state.carregando,
                    onConfirmar = {
                        viewModel.deletarFoto(state.fotoSelecionadaParaDeletar!!)
                    },
                    onCancelar = { viewModel.fecharModalDeletar() }
                )
            }
            
            // Modal de seleção de apoio/emoção
            if (state.mostrarModalApoio && state.fotoSelecionadaParaApoio != null) {
                ModalSelecionarApoio(
                    foto = state.fotoSelecionadaParaApoio!!,
                    onApoioSelecionado = { tipoApoio ->
                        viewModel.adicionarApoio(state.fotoSelecionadaParaApoio!!, tipoApoio)
                    },
                    onCancelar = { viewModel.fecharModalApoio() }
                )
            }
            
            // Modal de foto expandida com zoom
            fotoExpandida?.let { foto ->
                Timber.d("📸 Exibindo modal para foto: ${foto.id}, ${foto.pessoaNome}")
                ModalFotoExpandida(
                    foto = foto,
                    viewModel = viewModel,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
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
                .padding(8.dp)
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
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Grid de fotos (2 colunas)
                val fotosChunked = remember(fotos) {
                    fotos.chunked(2)
                }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(fotosChunked.size) { rowIndex ->
                        val rowFotos = fotosChunked[rowIndex]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            
            // Overlay com descrição e apoios
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
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
                            text = TextUtils.capitalizarTexto(foto.descricao),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    }
                    
                    // Exibir apoios se houver - mostrar TODAS as reações diferentes
                    if (foto.totalApoios > 0) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mostrar TODOS os tipos de emoção presentes (sem limitação)
                            val tiposApoio = com.raizesvivas.app.domain.model.TipoApoioFoto.values()
                                .map { tipo -> tipo to foto.contarApoiosPorTipo(tipo) }
                                .filter { it.second > 0 }
                                .sortedByDescending { it.second }
                            
                            tiposApoio.forEach { (tipo, count) ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tipo.emoji,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    if (count > 1) {
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = "$index/$total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
                    RaizesVivasTextField(
                        value = pessoaSelecionada?.nome ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = "Selecione a pessoa",
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                        },
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
                    RaizesVivasTextField(
                        value = descricao,
                        onValueChange = onDescricaoChange,
                        label = "Descrição (opcional)",
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
    carregando: Boolean = false,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Deletar Foto") },
        text = {
            Column {
                if (carregando) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Aguarde...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text("Tem certeza que deseja deletar esta foto?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pessoa: ${foto.pessoaNome}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (foto.descricao.isNotBlank()) {
                    Text(
                        text = "Descrição: ${TextUtils.capitalizarTexto(foto.descricao)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !carregando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Deletar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar,
                enabled = !carregando
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Modal para deletar comentário
 */
@Composable
fun ModalDeletarComentario(
    comentario: com.raizesvivas.app.domain.model.ComentarioFoto,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    var mostrarDialog by remember { mutableStateOf(true) }
    
    if (mostrarDialog) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialog = false
                onCancelar()
            },
            title = { Text("Deletar Comentário") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tem certeza que deseja deletar este comentário?")
                    
                    // Avatar e informações do usuário
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarUsuario(
                            fotoUrl = comentario.usuarioFotoUrl,
                            nome = comentario.usuarioNome,
                            size = 40
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = comentario.usuarioNome,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = TimeUtils.formatRelativeTimeShort(comentario.criadoEm),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (comentario.texto.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "\"${TextUtils.capitalizarTexto(comentario.texto)}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialog = false
                        onConfirmar()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Deletar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialog = false
                    onCancelar()
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Modal de foto expandida com zoom e gestos
 */
@Composable
fun ModalFotoExpandida(
    foto: FotoAlbum,
    viewModel: AlbumFamiliaViewModel,
    onDismiss: () -> Unit,
    onDeletar: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val comentariosPorFoto by viewModel.comentariosPorFoto.collectAsState()
    val usuarioAtual by viewModel.usuarioAtual.collectAsState()
    val comentarios = comentariosPorFoto[foto.id] ?: emptyList()
    val comentariosExpandidos = state.fotosComComentariosExpandidos.contains(foto.id)
    val isAdmin = usuarioAtual?.ehAdmin == true
    
    // Iniciar observação quando o modal abrir
    LaunchedEffect(foto.id) {
        viewModel.observarComentarios(foto.id)
    }
    
    // Parar observação quando o modal fechar
    DisposableEffect(foto.id) {
        onDispose {
            viewModel.pararObservarComentarios(foto.id)
        }
    }
    // Usar Animatable para zoom suave e controlável
    val scaleAnimatable = remember { Animatable(1f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Sincronizar scale com Animatable para animações suaves
    LaunchedEffect(scale) {
        scaleAnimatable.animateTo(
            targetValue = scale,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = 300f
            )
        )
    }
    
    // Estado transformável para zoom (pinch) e pan (arrastar) - mais fluído
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        // Zoom com pinch - mais controlável com limites suaves
        val currentScale = scaleAnimatable.value
        val newScale = (currentScale * zoomChange).coerceIn(1f, 4f)
        
        // Atualizar scale imediatamente para resposta rápida
        scale = newScale
        
        // Pan (arrastar) apenas se estiver com zoom > 1f
        if (newScale > 1f) {
            val newOffset = offset + panChange
            
            // Calcular limites baseados no tamanho da imagem e container
            val maxOffsetX = if (imageSize.width > 0 && containerSize.width > 0) {
                val scaledWidth = imageSize.width * newScale
                val maxPan = (scaledWidth - containerSize.width) / 2f
                maxPan.coerceAtLeast(0f)
            } else Float.MAX_VALUE
            
            val maxOffsetY = if (imageSize.height > 0 && containerSize.height > 0) {
                val scaledHeight = imageSize.height * newScale
                val maxPan = (scaledHeight - containerSize.height) / 2f
                maxPan.coerceAtLeast(0f)
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
    
    val scope = rememberCoroutineScope()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .onSizeChanged { size ->
                    containerSize = size
                }
        ) {
            // Container principal da imagem - sempre visível
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformableState)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { _ ->
                                // Progressive zoom animado: 1x → 2x → 1x
                                scope.launch {
                                    val targetScale = if (scaleAnimatable.value < 1.5f) 2f else 1f
                                    scale = targetScale
                                    if (targetScale == 1f) {
                                        offset = Offset.Zero
                                    }
                                }
                            }
                        )
                    }
            ) {
                // Imagem sempre visível com transformações suaves
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
                        .scale(scaleAnimatable.value)
                        .offset {
                            IntOffset(
                                offset.x.roundToInt(),
                                offset.y.roundToInt()
                            )
                        },
                    contentScale = ContentScale.Fit
                )
            }
            
            // Comentários fixos na parte inferior (sempre visíveis)
            SecaoComentariosEApoios(
                foto = foto,
                comentarios = comentarios,
                comentariosExpandidos = comentariosExpandidos,
                isAdmin = isAdmin,
                onExpandirComentarios = { viewModel.expandirComentarios(foto.id) },
                onContrairComentarios = { viewModel.contrairComentarios(foto.id) },
                onAdicionarComentario = { texto -> viewModel.adicionarComentario(foto, texto) },
                onDeletarComentario = { comentarioId -> viewModel.deletarComentario(foto, comentarioId) },
                onApoioClick = { viewModel.abrirModalApoio(foto) },
                modifier = Modifier.align(Alignment.BottomCenter),
                usuarioAtualId = usuarioAtual?.id
            )
            
            // Barra superior com botões de ação (sempre visível)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de deletar (esquerda) - Apenas se tiver permissão
                val podeDeletar = usuarioAtual != null && (usuarioAtual?.id == foto.criadoPor || isAdmin)
                
                if (podeDeletar) {
                    IconButton(
                        onClick = {
                            onDeletar?.invoke()
                            onDismiss()
                        },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar foto",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    // Spacer para manter o botão de fechar alinhado à direita
                    Spacer(modifier = Modifier.size(48.dp))
                }
                
                // Botão de fechar (direita)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onSurface,
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
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
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
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.onSurface,
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
                            tint = MaterialTheme.colorScheme.onSurface,
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
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Seção de comentários e apoios estilo Instagram
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecaoComentariosEApoios(
    foto: FotoAlbum,
    comentarios: List<com.raizesvivas.app.domain.model.ComentarioFoto>,
    comentariosExpandidos: Boolean,
    isAdmin: Boolean = false,
    onExpandirComentarios: () -> Unit,
    onContrairComentarios: () -> Unit,
    onAdicionarComentario: (String) -> Unit,
    onDeletarComentario: (String) -> Unit,
    onApoioClick: () -> Unit,
    modifier: Modifier = Modifier,
    usuarioAtualId: String? = null  // ID do usuário atual para verificar permissões
) {
    var textoComentario by remember { mutableStateOf("") }
    var comentarioParaDeletar by remember { mutableStateOf<com.raizesvivas.app.domain.model.ComentarioFoto?>(null) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Nome da pessoa e descrição
            Text(
                text = foto.pessoaNome,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            if (foto.descricao.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = TextUtils.capitalizarTexto(foto.descricao),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Apoios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de apoio - remove se já apoiou, abre modal se não apoiou
                val usuarioDeuApoio = foto.usuarioDeuApoio(usuarioAtualId)
                IconButton(
                    onClick = onApoioClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (usuarioDeuApoio) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Outlined.Favorite
                        },
                        contentDescription = if (usuarioDeuApoio) "Remover apoio" else "Dar apoio",
                        tint = if (usuarioDeuApoio) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Exibir apoios existentes - mostrar TODAS as reações diferentes
                if (foto.totalApoios > 0) {
                    val tiposApoio = com.raizesvivas.app.domain.model.TipoApoioFoto.values()
                        .map { tipo -> tipo to foto.contarApoiosPorTipo(tipo) }
                        .filter { it.second > 0 }
                        .sortedByDescending { it.second }
                    
                    // Usar LazyRow para permitir scroll horizontal se houver muitas reações
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(tiposApoio.size) { index ->
                            val (tipo, count) = tiposApoio[index]
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tipo.emoji,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (count > 1) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Comentários (contraído/expandido)
            if (comentarios.isNotEmpty() || comentariosExpandidos) {
                // Botão para expandir/contrair comentários
                TextButton(
                    onClick = {
                        if (comentariosExpandidos) {
                            onContrairComentarios()
                        } else {
                            onExpandirComentarios()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (comentariosExpandidos) {
                            "Ocultar ${comentarios.size} comentário${if (comentarios.size != 1) "s" else ""}"
                        } else {
                            "Ver ${comentarios.size} comentário${if (comentarios.size != 1) "s" else ""}"
                        },
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                // Lista de comentários (se expandido)
                AnimatedVisibility(
                    visible = comentariosExpandidos,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        comentarios.forEach { comentario ->
                            ComentarioItem(
                                comentario = comentario,
                                isAdmin = isAdmin,
                                onDeletar = { comentarioParaDeletar = comentario },
                                textColor = MaterialTheme.colorScheme.onSurface,
                                secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                usuarioAtualId = usuarioAtualId
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Campo de adicionar comentário
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoComentario,
                    onValueChange = { textoComentario = it },
                    placeholder = { 
                        Text(
                            "Adicionar comentário...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    ),
                    maxLines = 3,
                    singleLine = false
                )
                
                IconButton(
                    onClick = {
                        if (textoComentario.isNotBlank()) {
                            val texto = textoComentario.trim()
                            textoComentario = "" // Limpar imediatamente
                            onAdicionarComentario(texto)
                        }
                    },
                    enabled = textoComentario.isNotBlank(),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar comentário",
                        tint = if (textoComentario.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        // Modal de confirmação para deletar comentário
        comentarioParaDeletar?.let { comentario ->
            ModalDeletarComentario(
                comentario = comentario,
                onConfirmar = {
                    // Fechar modal imediatamente
                    comentarioParaDeletar = null
                    // Deletar comentário
                    onDeletarComentario(comentario.id)
                },
                onCancelar = {
                    comentarioParaDeletar = null
                }
            )
        }
    }
}

/**
 * Item de comentário individual
 */
@Composable
fun ComentarioItem(
    comentario: com.raizesvivas.app.domain.model.ComentarioFoto,
    isAdmin: Boolean = false,
    onDeletar: () -> Unit,
    textColor: Color? = null,
    secondaryTextColor: Color? = null,
    usuarioAtualId: String? = null  // ID do usuário atual para verificar se é o dono do comentário
) {
    val defaultTextColor = textColor ?: MaterialTheme.colorScheme.onSurface
    val defaultSecondaryTextColor = secondaryTextColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    
    // Estado para armazenar a foto do usuário (pode ser buscada se não estiver no comentário)
    var fotoUsuario by remember { mutableStateOf<String?>(comentario.usuarioFotoUrl) }
    
    // Buscar foto do usuário se não estiver disponível no comentário
    // Nota: Como não podemos injetar diretamente em @Composable, vamos usar uma abordagem diferente
    // Vamos verificar se a foto está disponível e, se não estiver, usar apenas o inicial
    LaunchedEffect(comentario.usuarioId, comentario.usuarioFotoUrl) {
        fotoUsuario = comentario.usuarioFotoUrl
    }
    
    // Verificar se pode deletar: admin pode deletar qualquer comentário, usuário pode deletar seus próprios
    val podeDeletar = isAdmin || (usuarioAtualId != null && comentario.usuarioId == usuarioAtualId)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar do usuário usando componente reutilizável
        AvatarUsuario(
            fotoUrl = fotoUsuario,
            nome = comentario.usuarioNome,
            size = 36
        )
        
        Column(modifier = Modifier.weight(1f)) {
            // Nome e timestamp
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comentario.usuarioApelido?.takeIf { it.isNotBlank() }
                        ?: comentario.usuarioNome.split(" ").firstOrNull()
                        ?: comentario.usuarioNome,
                    style = MaterialTheme.typography.labelMedium,
                    color = defaultTextColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = TimeUtils.formatRelativeTimeShort(comentario.criadoEm),
                    style = MaterialTheme.typography.labelSmall,
                    color = defaultSecondaryTextColor
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Texto do comentário
            Text(
                text = TextUtils.capitalizarTexto(comentario.texto),
                style = MaterialTheme.typography.bodyMedium,
                color = defaultTextColor,
                textAlign = TextAlign.Start
            )
        }
        
        // Botão de deletar (admin pode deletar qualquer comentário, usuário pode deletar seus próprios)
        if (podeDeletar) {
            IconButton(
                onClick = onDeletar,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Deletar comentário",
                    tint = defaultSecondaryTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Modal para selecionar tipo de apoio/emoção
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModalSelecionarApoio(
    foto: FotoAlbum,
    onApoioSelecionado: (com.raizesvivas.app.domain.model.TipoApoioFoto) -> Unit,
    onCancelar: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    
    Dialog(
        onDismissRequest = onCancelar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = "Reagir à foto",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                // Grid de emojis com contadores
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val tiposApoio = com.raizesvivas.app.domain.model.TipoApoioFoto.values().toList()
                    tiposApoio.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { tipoApoio ->
                                val count = foto.contarApoiosPorTipo(tipoApoio)
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .combinedClickable(
                                            onClick = {
                                                haptic(HapticFeedback.FeedbackType.SUCCESS)
                                                onApoioSelecionado(tipoApoio)
                                                onCancelar()
                                            }
                                        )
                                ) {
                                    Text(
                                        text = tipoApoio.emoji,
                                        style = MaterialTheme.typography.displayLarge
                                    )
                                    if (count > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente de Avatar do Usuário
 */
@Composable
fun AvatarUsuario(
    fotoUrl: String?,
    nome: String,
    size: Int = 32,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!fotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fotoUrl)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Avatar de $nome",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder com inicial do nome
            Text(
                text = nome.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Post individual estilo Instagram
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItemInstagram(
    foto: FotoAlbum,
    viewModel: AlbumFamiliaViewModel,
    onFotoClick: () -> Unit,
    onFotoLongPress: () -> Unit,
    onNavigateToDetalhesPessoa: ((String) -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val comentariosPorFoto by viewModel.comentariosPorFoto.collectAsState()
    val usuarioAtual by viewModel.usuarioAtual.collectAsState()
    val pessoas by viewModel.pessoas.collectAsState()
    val comentarios = comentariosPorFoto[foto.id] ?: emptyList()
    val comentariosExpandidos = state.fotosComComentariosExpandidos.contains(foto.id)
    val isAdmin = usuarioAtual?.ehAdmin == true
    
    // Buscar pessoa para obter fotoUrl
    val pessoa = remember(foto.pessoaId) {
        pessoas.find { it.id == foto.pessoaId }
    }
    
    // Iniciar observação de comentários quando o post aparecer ou quando expandido
    LaunchedEffect(foto.id, comentariosExpandidos) {
        if (comentariosExpandidos) {
            viewModel.observarComentarios(foto.id)
        }
    }
    
    // Parar observação quando o post desaparecer
    DisposableEffect(foto.id) {
        onDispose {
            viewModel.pararObservarComentarios(foto.id)
        }
    }
    
    var textoComentario by remember { mutableStateOf("") }
    var comentarioParaDeletar by remember { mutableStateOf<com.raizesvivas.app.domain.model.ComentarioFoto?>(null) }
    val haptic = rememberHapticFeedback()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header do post (avatar + nome) - clicável para navegar aos detalhes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clickable {
                    foto.pessoaId.let { pessoaId ->
                        Timber.d("🔄 Navegando para detalhes da pessoa: $pessoaId")
                        onNavigateToDetalhesPessoa?.invoke(pessoaId)
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarUsuario(
                fotoUrl = pessoa?.fotoUrl,
                nome = foto.pessoaNome,
                size = 40
            )
            Text(
                text = pessoa?.apelido?.takeIf { it.isNotBlank() } 
                    ?: foto.pessoaNome.split(" ").firstOrNull() 
                    ?: foto.pessoaNome,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Foto principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
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
                    .combinedClickable(
                        onClick = onFotoClick,
                        onLongClick = {
                            haptic(HapticFeedback.FeedbackType.MEDIUM)
                            onFotoLongPress()
                        }
                    ),
                contentScale = ContentScale.Crop
            )
        }
        
        // Barra de ações (coração, comentário, compartilhar, salvar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coração (apoio)
            IconButton(
                onClick = {
                    haptic(HapticFeedback.FeedbackType.LIGHT)
                    onFotoLongPress()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (foto.usuarioDeuApoio(usuarioAtual?.id)) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Outlined.Favorite
                    },
                    contentDescription = if (foto.usuarioDeuApoio(usuarioAtual?.id)) "Remover apoio" else "Dar apoio",
                    tint = if (foto.usuarioDeuApoio(usuarioAtual?.id)) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Comentário
            IconButton(
                onClick = {
                    haptic(HapticFeedback.FeedbackType.LIGHT)
                    if (comentariosExpandidos) {
                        viewModel.contrairComentarios(foto.id)
                    } else {
                        viewModel.expandirComentarios(foto.id)
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Comment,
                    contentDescription = "Comentar",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Compartilhar
            val context = LocalContext.current
            IconButton(
                onClick = {
                    haptic(HapticFeedback.FeedbackType.LIGHT)
                    compartilharFoto(context, foto)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Compartilhar",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Curtidas/apoios - mostrar TODAS as reações diferentes
        if (foto.totalApoios > 0) {
            val tiposApoio = com.raizesvivas.app.domain.model.TipoApoioFoto.values()
                .map { tipo -> tipo to foto.contarApoiosPorTipo(tipo) }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }
            
            // Mostrar todas as reações em uma linha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tiposApoio.forEachIndexed { index, (tipo, count) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tipo.emoji,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Adicionar separador entre reações (exceto na última)
                    if (index < tiposApoio.size - 1) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
        
        // Legenda (nome + descrição)
        if (foto.descricao.isNotBlank()) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        val nomeExibicao = pessoa?.apelido?.takeIf { it.isNotBlank() }
                            ?: foto.pessoaNome.split(" ").firstOrNull()
                            ?: foto.pessoaNome
                        append("$nomeExibicao ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        append(TextUtils.capitalizarTexto(foto.descricao))
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                textAlign = TextAlign.Start
            )
        } else {
            Text(
                text = foto.pessoaNome,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                textAlign = TextAlign.Start
            )
        }
        
        // Ver comentários (se houver)
        if (comentarios.isNotEmpty() && !comentariosExpandidos) {
            TextButton(
                onClick = { viewModel.expandirComentarios(foto.id) },
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "Ver ${comentarios.size} comentário${if (comentarios.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Comentários expandidos - mostrar sempre que expandidos, mesmo se vazios
        AnimatedVisibility(
            visible = comentariosExpandidos,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                if (comentarios.isNotEmpty()) {
                    comentarios.forEach { comentario ->
                        ComentarioItem(
                            comentario = comentario,
                            isAdmin = isAdmin,
                            onDeletar = { comentarioParaDeletar = comentario },
                            usuarioAtualId = usuarioAtual?.id
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                } else {
                    // Mostrar mensagem quando não houver comentários mas estiver expandido
                    Text(
                        text = "Nenhum comentário ainda. Seja o primeiro a comentar!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        
        // Data
        Text(
            text = TimeUtils.formatRelativeTime(foto.criadoEm),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        
        // Campo de adicionar comentário
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar do usuário (não clicável - apenas para exibição)
            AvatarUsuario(
                fotoUrl = usuarioAtual?.fotoUrl,
                nome = usuarioAtual?.nome ?: "U",
                size = 32
            )
            
            OutlinedTextField(
                value = textoComentario,
                onValueChange = { textoComentario = it },
                placeholder = { 
                    Text(
                        "Adicionar comentário...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 80.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                ),
                maxLines = 3,
                singleLine = false
            )
            
            TextButton(
                onClick = {
                    if (textoComentario.isNotBlank()) {
                        val texto = textoComentario.trim()
                        textoComentario = "" // Limpar imediatamente para melhor UX
                        viewModel.adicionarComentario(foto, texto)
                        // Expandir comentários se não estiverem expandidos
                        if (!comentariosExpandidos) {
                            viewModel.expandirComentarios(foto.id)
                        }
                    }
                },
                enabled = textoComentario.isNotBlank()
            ) {
                Text(
                    "Publicar",
                    color = if (textoComentario.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        // Modal de confirmação para deletar comentário
        comentarioParaDeletar?.let { comentario ->
            ModalDeletarComentario(
                comentario = comentario,
                onConfirmar = {
                    // Fechar modal imediatamente
                    comentarioParaDeletar = null
                    // Deletar comentário
                    viewModel.deletarComentario(foto, comentario.id)
                },
                onCancelar = {
                    comentarioParaDeletar = null
                }
            )
        }
    }
}

/**
 * Função helper para compartilhar uma foto usando o sistema de compartilhamento do Android
 */
private fun compartilharFoto(context: android.content.Context, foto: FotoAlbum) {
    try {
        // Criar mensagem de compartilhamento
        val mensagem = buildString {
            append("📸 Foto de ${foto.pessoaNome}")
            if (foto.descricao.isNotBlank()) {
                append("\n${TextUtils.capitalizarTexto(foto.descricao)}")
            }
            append("\n\n")
            append("Veja no Raizes Vivas: ${foto.url}")
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensagem)
            putExtra(Intent.EXTRA_SUBJECT, "Foto de ${foto.pessoaNome} - Raizes Vivas")
        }
        
        val chooser = Intent.createChooser(intent, "Compartilhar foto")
        context.startActivity(chooser)
    } catch (e: Exception) {
        Timber.e(e, "Erro ao compartilhar foto")
    }
}


