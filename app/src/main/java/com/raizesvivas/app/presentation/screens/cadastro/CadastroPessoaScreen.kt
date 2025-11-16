package com.raizesvivas.app.presentation.screens.cadastro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.EstadoCivil
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.presentation.components.DatePickerDialog
import com.raizesvivas.app.presentation.components.ImagePicker
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tela de Cadastro/Edição de Pessoa
 * 
 * Formulário completo para cadastrar ou editar uma pessoa na árvore genealógica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroPessoaScreen(
    pessoaId: String? = null,
    viewModel: CadastroPessoaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onSalvo: () -> Unit = {},
    onNavigateToCadastroPessoa: (String?) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val pessoasDisponiveis by viewModel.pessoasDisponiveis.collectAsState()
    val focusManager = LocalFocusManager.current
    
    // Estados para DatePicker
    var showDateNascPicker by remember { mutableStateOf(false) }
    var showDateFalecPicker by remember { mutableStateOf(false) }
    
    // Carregar pessoa se estiver editando
    LaunchedEffect(pessoaId) {
        if (pessoaId != null) {
            viewModel.carregarPessoa(pessoaId)
        }
    }
    
    // Navegar quando salvo com sucesso
    LaunchedEffect(state.sucesso) {
        if (state.sucesso) {
            onSalvo()
        }
    }
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Editar Pessoa" else "Nova Pessoa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        // DatePickers renderizados no nível do Scaffold para garantir que apareçam acima de tudo
        if (showDateNascPicker) {
            DatePickerDialog(
                onDateSelected = { date ->
                    viewModel.onDataNascimentoChanged(date)
                    showDateNascPicker = false
                },
                onDismiss = { showDateNascPicker = false },
                initialDate = state.dataNascimento
            )
        }
        
        if (showDateFalecPicker) {
            DatePickerDialog(
                onDateSelected = { date ->
                    viewModel.onDataFalecimentoChanged(date)
                    showDateFalecPicker = false
                },
                onDismiss = { showDateFalecPicker = false },
                initialDate = state.dataFalecimento
            )
        }
        
        // Diálogo de Duplicatas
        if (state.mostrarDialogDuplicata) {
            DialogDuplicatas(
                duplicatas = state.duplicatasEncontradas,
                mensagem = state.erro ?: state.avisoDuplicatas,
                isBloqueio = state.erro != null, // Se tem erro, é bloqueio crítico
                onConfirmar = { viewModel.confirmarContinuarComDuplicata() },
                onCancelar = { viewModel.cancelarPorDuplicata() },
                onDismiss = { viewModel.fecharDialogDuplicata() }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Mensagem de erro geral
            state.erro?.let { erro ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = erro,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Campos obrigatórios
            Text(
                text = "Informações Básicas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Foto da pessoa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImagePicker(
                    imagePath = state.fotoPath,
                    onImageSelected = { path ->
                        if (path.isBlank()) {
                            viewModel.removerFoto()
                        } else {
                            viewModel.onFotoSelecionada(path)
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Nome (obrigatório)
            OutlinedTextField(
                value = state.nome,
                onValueChange = { viewModel.onNomeChanged(it) },
                label = { Text("Nome Completo *") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                isError = state.nomeError != null,
                supportingText = state.nomeError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.apelido,
                onValueChange = { viewModel.onApelidoChanged(it) },
                label = { Text("Apelido") },
                leadingIcon = {
                    Icon(Icons.Default.Star, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Data de Nascimento
            val dataNascTexto = state.dataNascimento?.let { dateFormatter.format(it) } ?: ""
            
            OutlinedTextField(
                value = dataNascTexto,
                onValueChange = { },
                readOnly = true,
                label = { Text("Data de Nascimento") },
                placeholder = { Text("Clique para selecionar") },
                leadingIcon = {
                    Icon(Icons.Default.Cake, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showDateNascPicker = true }) {
                        Icon(
                            Icons.Default.DateRange, 
                            contentDescription = "Selecionar data"
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDateNascPicker = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data de Falecimento
            val dataFalecTexto = state.dataFalecimento?.let { dateFormatter.format(it) } ?: ""
            
            OutlinedTextField(
                value = dataFalecTexto,
                onValueChange = { },
                readOnly = true,
                label = { Text("Data de Falecimento (opcional)") },
                placeholder = { Text("Clique para selecionar") },
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showDateFalecPicker = true }) {
                        Icon(
                            Icons.Default.DateRange, 
                            contentDescription = "Selecionar data"
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDateFalecPicker = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Localização
            Text(
                text = "Localização",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = state.localNascimento,
                onValueChange = { viewModel.onLocalNascimentoChanged(it) },
                label = { Text("Local de Nascimento") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = state.localResidencia,
                onValueChange = { viewModel.onLocalResidenciaChanged(it) },
                label = { Text("Local de Residência") },
                leadingIcon = {
                    Icon(Icons.Default.Home, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profissão e Biografia
            Text(
                text = "Outras Informações",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = state.profissao,
                onValueChange = { viewModel.onProfissaoChanged(it) },
                label = { Text("Profissão") },
                leadingIcon = {
                    Icon(Icons.Default.Work, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = state.telefone,
                onValueChange = { viewModel.onTelefoneChanged(it) },
                label = { Text("Telefone/Celular") },
                placeholder = { Text("(00) 00000-0000") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = state.biografia,
                onValueChange = { viewModel.onBiografiaChanged(it) },
                label = { Text("Biografia") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Gênero
            var expandedGenero by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expandedGenero,
                onExpandedChange = { expandedGenero = !expandedGenero },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.genero?.label ?: "",
                    onValueChange = {},
                    label = { Text("Gênero") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenero)
                    },
                    readOnly = true,
                    placeholder = { Text("Selecione o gênero") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedGenero,
                    onDismissRequest = { expandedGenero = false }
                ) {
                    // Opção "Nenhum"
                    DropdownMenuItem(
                        text = { Text("Não informado") },
                        onClick = {
                            viewModel.onGeneroChanged(null)
                            expandedGenero = false
                        }
                    )
                    
                    HorizontalDivider()
                    
                    val generosOrdenados = remember {
                        Genero.values()
                            .sortedBy { it.label.lowercase(Locale.getDefault()) }
                    }
                    generosOrdenados.forEach { genero ->
                        DropdownMenuItem(
                            text = { Text(genero.label) },
                            onClick = {
                                viewModel.onGeneroChanged(genero)
                                expandedGenero = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estado Civil
            var expandedEstadoCivil by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expandedEstadoCivil,
                onExpandedChange = { expandedEstadoCivil = !expandedEstadoCivil },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.estadoCivil?.label ?: "",
                    onValueChange = {},
                    label = { Text("Estado Civil") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstadoCivil)
                    },
                    readOnly = true,
                    placeholder = { Text("Selecione o estado civil") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedEstadoCivil,
                    onDismissRequest = { expandedEstadoCivil = false }
                ) {
                    // Opção "Nenhum"
                    DropdownMenuItem(
                        text = { Text("Não informado") },
                        onClick = {
                            viewModel.onEstadoCivilChanged(null)
                            expandedEstadoCivil = false
                        }
                    )
                    
                    HorizontalDivider()
                    
                    val estadosCivisOrdenados = remember {
                        EstadoCivil.values()
                            .sortedBy { it.label.lowercase(Locale.getDefault()) }
                    }
                    estadosCivisOrdenados.forEach { estadoCivil ->
                        DropdownMenuItem(
                            text = { Text(estadoCivil.label) },
                            onClick = {
                                viewModel.onEstadoCivilChanged(estadoCivil)
                                expandedEstadoCivil = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Relacionamentos
            Text(
                text = "Relacionamentos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Seleção de Pai - apenas pessoas do gênero Masculino ou Outro
            PessoaSelector(
                label = "Pai",
                pessoaId = state.paiId,
                pessoasDisponiveis = pessoasDisponiveis.filter { 
                    it.id != pessoaId && 
                    (it.genero == Genero.MASCULINO || it.genero == Genero.OUTRO)
                },
                onPessoaSelecionada = { pessoa -> viewModel.onPaiChanged(pessoa?.id) },
                mostrarAdicionarNovo = true,
                onAdicionarNovo = { onNavigateToCadastroPessoa(null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Seleção de Mãe - apenas pessoas do gênero Feminino ou Outro
            PessoaSelector(
                label = "Mãe",
                pessoaId = state.maeId,
                pessoasDisponiveis = pessoasDisponiveis.filter { 
                    it.id != pessoaId && 
                    (it.genero == Genero.FEMININO || it.genero == Genero.OUTRO)
                },
                onPessoaSelecionada = { pessoa -> viewModel.onMaeChanged(pessoa?.id) },
                mostrarAdicionarNovo = true,
                onAdicionarNovo = { onNavigateToCadastroPessoa(null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Seleção de Cônjuge (apenas se estado civil for Casado ou União Estável)
            if (state.estadoCivil == EstadoCivil.CASADO || state.estadoCivil == EstadoCivil.UNIAO_ESTAVEL) {
                PessoaSelector(
                    label = "Cônjuge",
                    pessoaId = state.conjugeId,
                    pessoasDisponiveis = pessoasDisponiveis.filter { it.id != pessoaId },
                    onPessoaSelecionada = { pessoa -> viewModel.onConjugeChanged(pessoa?.id) },
                    mostrarAdicionarNovo = true,
                    onAdicionarNovo = { onNavigateToCadastroPessoa(null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botão Salvar
            Button(
                onClick = { viewModel.salvar() },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isEditing) "Atualizar" else "Salvar",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PessoaSelector(
    label: String,
    pessoaId: String?,
    pessoasDisponiveis: List<Pessoa>,
    onPessoaSelecionada: (Pessoa?) -> Unit,
    mostrarAdicionarNovo: Boolean = false,
    onAdicionarNovo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val pessoasOrdenadas = remember(pessoasDisponiveis) {
        pessoasDisponiveis.sortedWith(
            compareBy<Pessoa> { it.getNomeExibicao().lowercase(Locale.getDefault()) }
                .thenBy { it.id }
        )
    }
    val pessoaSelecionada = pessoasDisponiveis.find { it.id == pessoaId }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = pessoaSelecionada?.nome ?: "",
            onValueChange = {},
            label = { Text(label) },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Opção "Nenhum"
            DropdownMenuItem(
                text = { Text("Nenhum") },
                onClick = {
                    onPessoaSelecionada(null)
                    expanded = false
                }
            )
            
            HorizontalDivider()
            
            // Lista de pessoas
            pessoasOrdenadas.forEach { pessoa ->
                DropdownMenuItem(
                    text = { Text(pessoa.getNomeExibicao()) },
                    onClick = {
                        onPessoaSelecionada(pessoa)
                        expanded = false
                    }
                )
            }
            
            // Opção "Adicionar Novo" (apenas se solicitado)
            if (mostrarAdicionarNovo && (label == "Pai" || label == "Mãe" || label == "Cônjuge")) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { 
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(
                                when (label) {
                                    "Pai" -> "Adicionar novo pai"
                                    "Mãe" -> "Adicionar nova mãe"
                                    "Cônjuge" -> "Adicionar novo cônjuge"
                                    else -> "Adicionar novo"
                                }
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        onAdicionarNovo()
                    }
                )
            }
        }
    }
}

/**
 * Diálogo para exibir duplicatas encontradas
 */
@Composable
fun DialogDuplicatas(
    duplicatas: List<DuplicataInfo>,
    mensagem: String?,
    isBloqueio: Boolean,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    
    AlertDialog(
        onDismissRequest = {
            if (!isBloqueio) {
                onDismiss()
            }
        },
        icon = {
            Icon(
                imageVector = when {
                    isBloqueio -> Icons.Outlined.Error
                    else -> Icons.Outlined.Warning
                },
                contentDescription = null,
                tint = when {
                    isBloqueio -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        },
        title = {
            Text(
                text = when {
                    isBloqueio -> "Duplicata Encontrada"
                    else -> "Possível Duplicata"
                },
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mensagem principal
                mensagem?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isBloqueio) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                if (duplicatas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Pessoa(s) similar(es) encontrada(s):",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Lista de duplicatas
                    duplicatas.forEachIndexed { index, duplicata ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when (duplicata.nivel) {
                                    "CRITICO" -> MaterialTheme.colorScheme.errorContainer
                                    "ALTO" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Nome e nível
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = duplicata.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // Badge do nível
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                text = when (duplicata.nivel) {
                                                    "CRITICO" -> "Crítico"
                                                    "ALTO" -> "Alto"
                                                    else -> "Médio"
                                                },
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = when (duplicata.nivel) {
                                                "CRITICO" -> MaterialTheme.colorScheme.error
                                                "ALTO" -> MaterialTheme.colorScheme.tertiary
                                                else -> MaterialTheme.colorScheme.secondary
                                            }
                                        )
                                    )
                                }
                                
                                // Data de nascimento
                                duplicata.dataNascimento?.let { data ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Nascimento: ${dateFormatter.format(data)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                // Razões da similaridade
                                if (duplicata.razoes.isNotEmpty()) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Razões:",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        duplicata.razoes.forEach { razao ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = razao,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Score de similaridade
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Similaridade:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LinearProgressIndicator(
                                        progress = { duplicata.scoreSimilaridade },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .padding(horizontal = 8.dp),
                                        color = when (duplicata.nivel) {
                                            "CRITICO" -> MaterialTheme.colorScheme.error
                                            "ALTO" -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.secondary
                                        }
                                    )
                                    Text(
                                        text = "${(duplicata.scoreSimilaridade * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        if (index < duplicatas.size - 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isBloqueio) {
                // Em caso de bloqueio, apenas botão "Entendi"
                TextButton(onClick = onCancelar) {
                    Text("Entendi")
                }
            } else {
                // Em caso de aviso, botão "Continuar mesmo assim"
                Button(onClick = onConfirmar) {
                    Text("Continuar mesmo assim")
                }
            }
        },
        dismissButton = {
            if (!isBloqueio) {
                // Em caso de aviso, botão "Cancelar"
                TextButton(onClick = onCancelar) {
                    Text("Cancelar")
                }
            }
            // Em caso de bloqueio, não há dismiss (só "Entendi")
        }
    )
}

