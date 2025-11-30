package com.raizesvivas.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.TipoNucleoFamiliar
import com.raizesvivas.app.utils.ParentescoCalculator
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * Representa uma família (casal) com seus filhos e parentes colaterais
 */
data class FamiliaGrupo(
    val id: String, // ID único da família (pode ser o ID de um dos cônjuges)
    val conjugue1: Pessoa?,
    val conjugue2: Pessoa?,
    val filhos: List<Pessoa>,
    val ehFamiliaZero: Boolean = false,
    val ehFamiliaMonoparental: Boolean = false, // True quando há apenas um responsável (sem cônjuge)
    val ehFamiliaReconstituida: Boolean = false, // True quando é uma família de casamento anterior
    val conjugueAnterior: Pessoa? = null, // Ex-cônjuge que formou família anterior (se aplicável)
    val familiaAnteriorId: String? = null, // ID da família anterior relacionada (se aplicável)
    val tipoNucleoFamiliar: TipoNucleoFamiliar = TipoNucleoFamiliar.PARENTESCO, // Tipo de núcleo familiar
    val parentesColaterais: Map<Int, List<Pessoa>> = emptyMap() // Parentes colaterais por nível (1 = avós/tios, 2 = primos/sobrinhos, etc)
)

/**
 * Representa uma família monoparental pendente de confirmação (pai + filhos)
 * Requer confirmação do usuário antes de ser criada
 */
data class FamiliaMonoparentalPendente(
    val responsavel: Pessoa, // Pai responsável
    val filhos: List<Pessoa>,
    val parentesColaterais: Map<Int, List<Pessoa>> = emptyMap()
)

/**
 * Resultado do agrupamento de famílias, contendo famílias confirmadas e pendentes
 */
data class ResultadoAgrupamentoFamilias(
    val familias: List<FamiliaGrupo>,
    val familiasPendentes: List<FamiliaMonoparentalPendente> = emptyList()
)

/**
 * Componente de lista expandível hierárquica para árvore genealógica
 * 
 * Organiza pessoas em grupos de famílias (casais e filhos)
 * Permite expandir/recolher cada família
 */
@Composable
fun ListaHierarquicaArvore(
    pessoas: List<Pessoa>,
    pessoasMap: Map<String, Pessoa>,
    onPersonClick: (Pessoa) -> Unit,
    modifier: Modifier = Modifier
) {
    // Agrupar pessoas em famílias
    val familias = remember(pessoas, pessoasMap) {
        agruparPessoasPorFamilias(pessoas, pessoasMap)
    }
    
    // Estado para controlar quais famílias estão expandidas
    // Expandir Família Zero por padrão
    var familiasExpandidas by remember { mutableStateOf<Set<String>>(setOf()) }
    
    // Expandir Família Zero por padrão quando as famílias forem carregadas
    LaunchedEffect(familias.isNotEmpty()) {
        if (familias.isNotEmpty() && familiasExpandidas.isEmpty()) {
            val familiaZeroId = familias.firstOrNull { it.ehFamiliaZero }?.id
            familiaZeroId?.let {
                familiasExpandidas = setOf(it)
            }
        }
    }
    
    // Pessoas não agrupadas (sem cônjuge e sem filhos)
    val pessoasSemFamilia = remember(pessoas, familias) {
        val idsEmFamilias = familias.flatMap { familia ->
            listOfNotNull(familia.conjugue1?.id, familia.conjugue2?.id) + 
            familia.filhos.map { it.id }
        }.toSet()
        pessoas.filter { it.id !in idsEmFamilias }
    }
    
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Famílias (casais com filhos)
        items(familias, key = { it.id }) { familia ->
            FamiliaExpandivelCard(
                familia = familia,
                isExpanded = familiasExpandidas.contains(familia.id),
                onToggle = { 
                    familiasExpandidas = if (familiasExpandidas.contains(familia.id)) {
                        familiasExpandidas - familia.id
                    } else {
                        familiasExpandidas + familia.id
                    }
                },
                onPersonClick = onPersonClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Pessoas sem família
        if (pessoasSemFamilia.isNotEmpty()) {
            item {
                Text(
                    text = "Outros Familiares",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(pessoasSemFamilia, key = { it.id }) { pessoa ->
                PessoaCard(
                    pessoa = pessoa,
                    onClick = { onPersonClick(pessoa) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Card expandível para uma família
 */
@Composable
fun FamiliaExpandivelCard(
    familia: FamiliaGrupo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onPersonClick: (Pessoa) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (familia.ehFamiliaZero) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Cabeçalho da família (com ícone e nome)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ícone de expandir/recolher
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Ícone Família Zero (se for Família Zero)
                    if (familia.ehFamiliaZero) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Família Zero",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Nome da família
                    Text(
                        text = obterNomeFamilia(familia),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Conteúdo expandido: MAE -> PAI -> FILHOS
            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // MAE (sem indentação)
                    familia.conjugue2?.let { mae ->
                        PessoaCard(
                            pessoa = mae,
                            onClick = { onPersonClick(mae) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // PAI (indentado)
                    familia.conjugue1?.let { pai ->
                        PessoaCard(
                            pessoa = pai,
                            onClick = { onPersonClick(pai) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp)
                        )
                    }
                    
                    // FILHOS (mais indentado)
                    familia.filhos.forEach { filho ->
                        PessoaCard(
                            pessoa = filho,
                            onClick = { onPersonClick(filho) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 48.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card para uma pessoa individual
 */
@Composable
fun PessoaCard(
    pessoa: Pessoa,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar/Emoji
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = pessoa.genero?.let { genero ->
                            when (genero) {
                                com.raizesvivas.app.domain.model.Genero.MASCULINO -> "👨"
                                com.raizesvivas.app.domain.model.Genero.FEMININO -> "👩"
                                com.raizesvivas.app.domain.model.Genero.OUTRO -> "👤"
                            }
                        } ?: "👤",
                        fontSize = 24.sp
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pessoa.getNomeExibicao(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                pessoa.dataNascimento?.let { dataNasc ->
                    val anoNasc = dateFormat.format(dataNasc).split("/")[2]
                    Text(
                        text = anoNasc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalhes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Encontra parentes colaterais de um núcleo familiar
 * Usa ParentescoCalculator para identificar avós, tios, primos, sobrinhos, etc.
 * 
 * @param nucleoFamiliar Lista de pessoas do núcleo (casal + filhos diretos)
 * @param todasPessoas Lista de todas as pessoas disponíveis
 * @param pessoasMap Mapa de pessoas por ID (deve conter todas as pessoas de todasPessoas)
 * @param grauMaximo Grau máximo de parentesco a incluir (padrão: 2, deve ser entre 1 e 10)
 * @return Map com parentes colaterais agrupados por nível (1 = avós/tios, 2 = primos/sobrinhos)
 */
fun encontrarParentesColaterais(
    nucleoFamiliar: List<Pessoa>,
    todasPessoas: List<Pessoa>,
    pessoasMap: Map<String, Pessoa>,
    grauMaximo: Int = 2
): Map<Int, List<Pessoa>> {
    // Validações de entrada
    if (nucleoFamiliar.isEmpty()) return emptyMap()
    if (todasPessoas.isEmpty()) return emptyMap()
    if (pessoasMap.isEmpty()) return emptyMap()
    
    // Validar e limitar grauMaximo
    val grauMaximoValido = grauMaximo.coerceIn(1, 10)
    
    val parentesPorNivel = mutableMapOf<Int, MutableSet<String>>()
    val idsNucleo = nucleoFamiliar.map { it.id }.filter { it.isNotBlank() }.toSet()
    
    // Filtrar pessoas candidatas que não estão no núcleo e existem no mapa
    val pessoasCandidatas = todasPessoas.filter { pessoa ->
        pessoa.id.isNotBlank() && 
        pessoa.id !in idsNucleo &&
        pessoasMap.containsKey(pessoa.id)
    }
    
    // Para cada pessoa do núcleo, encontrar parentes colaterais
    nucleoFamiliar.forEach { pessoaNucleo ->
        if (pessoaNucleo.id.isBlank() || !pessoasMap.containsKey(pessoaNucleo.id)) {
            return@forEach // Pular se pessoa do núcleo não está no mapa
        }
        
        pessoasCandidatas.forEach { pessoaCandidata ->
            try {
                // Calcular parentesco
                val resultado = ParentescoCalculator.calcularParentesco(
                    pessoaNucleo,
                    pessoaCandidata,
                    pessoasMap
                )
                
                // Incluir apenas parentes consanguíneos com grau válido
                if (resultado.tipoRelacao == ParentescoCalculator.TipoRelacao.CONSANGUINEO && 
                    resultado.grau > 0 && resultado.grau <= grauMaximoValido) {
                    
                    // Determinar nível baseado no grau e distância
                    // Grau 1-2: Avós, tios (nível 1)
                    // Grau 3-4: Primos, sobrinhos (nível 2)
                    // Grau 5+: Outros parentes (nível 3+)
                    val nivel = when {
                        resultado.grau <= 2 -> 1
                        resultado.grau <= 4 -> 2
                        else -> minOf(3, (resultado.grau + 1) / 2)
                    }
                    
                    parentesPorNivel.getOrPut(nivel) { mutableSetOf() }.add(pessoaCandidata.id)
                }
            } catch (e: Exception) {
                // Logar erro mas continuar processamento
                Timber.e(e, "Erro ao calcular parentesco entre ${pessoaNucleo.id} e ${pessoaCandidata.id}")
            }
        }
    }
    
    // Converter IDs para objetos Pessoa e remover duplicatas
    return parentesPorNivel.mapValues { (_, ids) ->
        ids.mapNotNull { pessoasMap[it] }
            .filter { it.id.isNotBlank() }
            .distinctBy { it.id }
    }
}

/**
 * Agrupa pessoas por residência (localResidencia)
 * Cria famílias baseadas em pessoas que vivem no mesmo local
 */
fun agruparPorResidencia(
    pessoas: List<Pessoa>,
    pessoasMap: Map<String, Pessoa>
): List<FamiliaGrupo> {
    val familias = mutableListOf<FamiliaGrupo>()
    
    // Agrupar pessoas por localResidencia
    val pessoasPorResidencia = pessoas
        .filter { it.localResidencia != null && it.localResidencia.isNotBlank() }
        .groupBy { it.localResidencia }
    
    pessoasPorResidencia.forEach { (residencia, pessoasNaResidencia) ->
        // Criar família residencial apenas se houver mais de uma pessoa
        if (pessoasNaResidencia.size > 1) {
            // Separar em casais e filhos, ou apenas grupo de pessoas
            val casais = pessoasNaResidencia.filter { it.conjugeAtual != null && 
                pessoasNaResidencia.any { p -> p.id == it.conjugeAtual } }
            
            val pessoasSemConjuge = pessoasNaResidencia.filter { 
                it.conjugeAtual == null || !pessoasNaResidencia.any { p -> p.id == it.conjugeAtual }
            }
            
            // Se houver casais, criar família para cada casal
            if (casais.isNotEmpty()) {
                val casaisProcessados = mutableSetOf<String>()
                casais.forEach { pessoa ->
                    if (pessoa.id !in casaisProcessados) {
                        val conjuge = pessoa.conjugeAtual?.takeIf { pessoasMap.containsKey(it) }
                            ?.let { pessoasMap[it] }
                            ?.takeIf { it.localResidencia == residencia }
                        
                        if (conjuge != null) {
                            // Buscar filhos que também moram no mesmo local
                            val filhos = pessoasNaResidencia.filter { filho ->
                                (filho.pai == pessoa.id || filho.mae == pessoa.id) ||
                                (filho.pai == conjuge.id || filho.mae == conjuge.id)
                            }
                            
                            val familiaId = "residencial_${pessoa.id}_${conjuge.id}"
                            familias.add(
                                FamiliaGrupo(
                                    id = familiaId,
                                    conjugue1 = pessoa,
                                    conjugue2 = conjuge,
                                    filhos = filhos,
                                    ehFamiliaZero = false,
                                    ehFamiliaMonoparental = false,
                                    ehFamiliaReconstituida = false,
                                    conjugueAnterior = null,
                                    familiaAnteriorId = null,
                                    tipoNucleoFamiliar = TipoNucleoFamiliar.RESIDENCIAL,
                                    parentesColaterais = emptyMap()
                                )
                            )
                            
                            casaisProcessados.add(pessoa.id)
                            casaisProcessados.add(conjuge.id)
                        }
                    }
                }
                
                // Pessoas sem cônjuge que moram no mesmo local
                pessoasSemConjuge.forEach { pessoa ->
                    if (pessoa.id !in casaisProcessados) {
                        val filhos = pessoasNaResidencia.filter { filho ->
                            filho.pai == pessoa.id || filho.mae == pessoa.id
                        }
                        
                        if (filhos.isNotEmpty()) {
                            val familiaId = "residencial_${pessoa.id}"
                            familias.add(
                                FamiliaGrupo(
                                    id = familiaId,
                                    conjugue1 = pessoa,
                                    conjugue2 = null,
                                    filhos = filhos,
                                    ehFamiliaZero = false,
                                    ehFamiliaMonoparental = true,
                                    ehFamiliaReconstituida = false,
                                    conjugueAnterior = null,
                                    familiaAnteriorId = null,
                                    tipoNucleoFamiliar = TipoNucleoFamiliar.RESIDENCIAL
                                )
                            )
                        }
                    }
                }
            } else {
                // Grupo de pessoas sem relação de parentesco definida, mas que moram juntas
                // Criar uma família residencial genérica apenas se houver pelo menos 2 pessoas
                if (pessoasNaResidencia.size >= 2) {
                    val familiaId = "residencial_${residencia.hashCode()}"
                    familias.add(
                        FamiliaGrupo(
                            id = familiaId,
                            conjugue1 = pessoasNaResidencia.firstOrNull(),
                            conjugue2 = pessoasNaResidencia.getOrNull(1),
                            filhos = pessoasNaResidencia.drop(2),
                            ehFamiliaZero = false,
                            ehFamiliaMonoparental = false,
                            ehFamiliaReconstituida = false,
                            conjugueAnterior = null,
                            familiaAnteriorId = null,
                            tipoNucleoFamiliar = TipoNucleoFamiliar.RESIDENCIAL,
                            parentesColaterais = emptyMap()
                        )
                    )
                }
            }
        }
    }
    
    return familias
}

/**
 * Identifica famílias anteriores (de casamentos anteriores)
 * Retorna lista de famílias formadas por ex-cônjuges
 */
fun identificarFamiliasAnteriores(
    pessoa: Pessoa,
    pessoas: List<Pessoa>,
    pessoasMap: Map<String, Pessoa>
): List<FamiliaGrupo> {
    // Validação básica
    if (pessoa.exConjuges.isEmpty()) return emptyList()
    if (pessoas.isEmpty()) return emptyList()
    if (pessoasMap.isEmpty()) return emptyList()
    
    val familiasAnteriores = mutableListOf<FamiliaGrupo>()
    
    // Para cada ex-cônjuge, criar uma família anterior
    pessoa.exConjuges.forEach { exConjugeId ->
        if (exConjugeId.isBlank() || !pessoasMap.containsKey(exConjugeId)) {
            return@forEach // Pular se ex-cônjuge não existe no mapa
        }
        
        val exConjuge = pessoasMap[exConjugeId]
        if (exConjuge != null) {
            // Buscar filhos comuns (filhos que têm ambos como pais)
            val filhosComuns = pessoas.filter { filho ->
                val temPessoaComoPai = filho.pai == pessoa.id || filho.mae == pessoa.id
                val temExConjugeComoPai = filho.pai == exConjugeId || filho.mae == exConjugeId
                temPessoaComoPai && temExConjugeComoPai
            }
            
            // Criar família anterior apenas se houver filhos comuns
            if (filhosComuns.isNotEmpty()) {
                val familiaId = "${pessoa.id}_${exConjugeId}_anterior"
                familiasAnteriores.add(
                    FamiliaGrupo(
                        id = familiaId,
                        conjugue1 = pessoa,
                        conjugue2 = exConjuge,
                        filhos = filhosComuns,
                        ehFamiliaZero = false,
                        ehFamiliaMonoparental = false,
                        ehFamiliaReconstituida = true,
                        conjugueAnterior = exConjuge,
                        familiaAnteriorId = null,
                        tipoNucleoFamiliar = TipoNucleoFamiliar.RECONSTITUIDA,
                        parentesColaterais = emptyMap() // Famílias anteriores não incluem parentes colaterais por padrão
                    )
                )
            }
        }
    }
    
    return familiasAnteriores
}

/**
 * Agrupa pessoas em famílias (casais e filhos)
 * Inclui famílias atuais, famílias anteriores (reconstituídas) e famílias residenciais
 * 
 * @param pessoas Lista de todas as pessoas
 * @param pessoasMap Mapa de pessoas por ID
 * @param incluirResidenciais Se true, inclui famílias baseadas em residência (padrão: false)
 * @param incluirParentesColaterais Se true, inclui avós, tios, primos na mesma família (padrão: false)
 * @param grauMaximoParentesco Grau máximo de parentesco colateral a incluir (padrão: 2)
 */
fun agruparPessoasPorFamilias(
    pessoas: List<Pessoa>,
    pessoasMap: Map<String, Pessoa>,
    incluirResidenciais: Boolean = false,
    incluirParentesColaterais: Boolean = false,
    grauMaximoParentesco: Int = 2
): List<FamiliaGrupo> {
    // Wrapper para manter compatibilidade - retorna apenas as famílias confirmadas
    return agruparPessoasPorFamiliasComPendentes(
        pessoas, pessoasMap, incluirResidenciais, incluirParentesColaterais, grauMaximoParentesco
    ).familias
}

/**
 * Agrupa pessoas em famílias, retornando também famílias monoparentais pendentes (pai + filhos)
 * 
 * Regra: Famílias monoparentais são automaticamente criadas apenas para mãe + filhos.
 * Famílias com pai + filhos requerem confirmação do usuário.
 * 
 * @param familiasMonoparentaisConfirmadas Set de IDs de pais que foram confirmados para criar família monoparental
 * @param familiasMonoparentaisRejeitadas Set de IDs de pais que foram rejeitados (não serão sugeridos novamente)
 */
fun agruparPessoasPorFamiliasComPendentes(
    pessoas: List<Pessoa>,
    pessoasMap: Map<String, Pessoa>,
    incluirResidenciais: Boolean = false,
    incluirParentesColaterais: Boolean = false,
    grauMaximoParentesco: Int = 2,
    familiasMonoparentaisConfirmadas: Set<String> = emptySet(),
    familiasMonoparentaisRejeitadas: Set<String> = emptySet()
): ResultadoAgrupamentoFamilias {
    // Validação básica
    if (pessoas.isEmpty()) return ResultadoAgrupamentoFamilias(emptyList(), emptyList())
    
    // Garantir que pessoasMap contenha todas as pessoas (completar se necessário)
    val pessoasMapCompleto = if (pessoasMap.size < pessoas.size) {
        pessoasMap.toMutableMap().apply {
            pessoas.forEach { pessoa ->
                if (pessoa.id.isNotBlank() && !containsKey(pessoa.id)) {
                    put(pessoa.id, pessoa)
                }
            }
        }
    } else {
        pessoasMap
    }
    
    val familias = mutableListOf<FamiliaGrupo>()
    val familiasPendentes = mutableListOf<FamiliaMonoparentalPendente>()
    val pessoasProcessadas = mutableSetOf<String>()
    val familiasAnterioresProcessadas = mutableSetOf<String>() // Para evitar duplicatas
    val familiasResidenciaisProcessadas = mutableSetOf<String>() // Para evitar duplicatas residenciais
    
    // Validar e limitar grauMaximoParentesco
    val grauMaximoValido = grauMaximoParentesco.coerceIn(1, 10)
    
    // Primeiro, processar Família Zero
    val familiaZero = pessoas.filter { it.ehFamiliaZero }
    if (familiaZero.isNotEmpty()) {
        // Remover suposição de gênero: buscar os dois cônjuges sem assumir gênero específico
        val conjugue1 = familiaZero.firstOrNull()
        val conjugue2 = familiaZero.firstOrNull { it.id != conjugue1?.id }
        
        // Buscar filhos da Família Zero
        val filhosIds = mutableSetOf<String>()
        conjugue1?.filhos?.let { filhosIds.addAll(it) }
        conjugue2?.filhos?.let { filhosIds.addAll(it) }
        
        // Filhos também podem ser identificados por terem qualquer um dos cônjuges como pai ou mãe
        val filhosPorRelacao = pessoas.filter { filho ->
            (filho.pai == conjugue1?.id || filho.mae == conjugue1?.id) ||
            (filho.pai == conjugue2?.id || filho.mae == conjugue2?.id)
        }
        
        val todosFilhosIds = filhosIds + filhosPorRelacao.map { it.id }
        val filhos = todosFilhosIds.mapNotNull { pessoasMap[it] }
            .filter { pessoa -> 
                pessoa.pai == conjugue1?.id || pessoa.pai == conjugue2?.id || 
                pessoa.mae == conjugue1?.id || pessoa.mae == conjugue2?.id
            }
        
        val familiaId = conjugue1?.id ?: conjugue2?.id ?: "familia_zero"
        val ehMonoparental = conjugue2 == null
        
        // Encontrar parentes colaterais se solicitado
        val parentesColaterais = if (incluirParentesColaterais) {
            val nucleo = listOfNotNull(conjugue1, conjugue2) + filhos
            encontrarParentesColaterais(nucleo, pessoas, pessoasMapCompleto, grauMaximoValido)
        } else {
            emptyMap()
        }
        
        // Verificar se é família monoparental e se o responsável é pai (MASCULINO)
        // Família Zero monoparental: se for pai, requer confirmação
        if (ehMonoparental && filhos.isNotEmpty()) {
            val responsavel = conjugue1 ?: conjugue2
            if (responsavel != null && responsavel.genero == Genero.MASCULINO) {
                // Pai + filhos: verificar se foi confirmado ou rejeitado
                if (responsavel.id in familiasMonoparentaisConfirmadas) {
                    // Foi confirmado: criar a família normalmente
                    familias.add(
                        FamiliaGrupo(
                            id = familiaId,
                            conjugue1 = conjugue1,
                            conjugue2 = conjugue2,
                            filhos = filhos,
                            ehFamiliaZero = true,
                            ehFamiliaMonoparental = true,
                            ehFamiliaReconstituida = false,
                            conjugueAnterior = null,
                            familiaAnteriorId = null,
                            tipoNucleoFamiliar = TipoNucleoFamiliar.PARENTESCO,
                            parentesColaterais = parentesColaterais
                        )
                    )
                } else if (responsavel.id !in familiasMonoparentaisRejeitadas) {
                    // Não foi confirmado e não foi rejeitado: adicionar à lista de pendências
                    familiasPendentes.add(
                        FamiliaMonoparentalPendente(
                            responsavel = responsavel,
                            filhos = filhos,
                            parentesColaterais = parentesColaterais
                        )
                    )
                    // Não criar a família automaticamente
                }
                // Se foi rejeitado, não fazer nada (não criar família nem adicionar às pendências)
            } else {
                // Mãe + filhos ou responsável sem gênero definido: criar normalmente
                familias.add(
                    FamiliaGrupo(
                        id = familiaId,
                        conjugue1 = conjugue1,
                        conjugue2 = conjugue2,
                        filhos = filhos,
                        ehFamiliaZero = true,
                        ehFamiliaMonoparental = true,
                        ehFamiliaReconstituida = false,
                        conjugueAnterior = null,
                        familiaAnteriorId = null,
                        tipoNucleoFamiliar = TipoNucleoFamiliar.PARENTESCO,
                        parentesColaterais = parentesColaterais
                    )
                )
            }
        } else {
            // Família com casal ou sem filhos: criar normalmente
            familias.add(
                FamiliaGrupo(
                    id = familiaId,
                    conjugue1 = conjugue1,
                    conjugue2 = conjugue2,
                    filhos = filhos,
                    ehFamiliaZero = true,
                    ehFamiliaMonoparental = ehMonoparental,
                    ehFamiliaReconstituida = false,
                    conjugueAnterior = null,
                    familiaAnteriorId = null,
                    tipoNucleoFamiliar = TipoNucleoFamiliar.PARENTESCO,
                    parentesColaterais = parentesColaterais
                )
            )
        }
        
        conjugue1?.id?.let { pessoasProcessadas.add(it) }
        conjugue2?.id?.let { pessoasProcessadas.add(it) }
        filhos.forEach { pessoasProcessadas.add(it.id) }
    }
    
    // Processar outros casais (incluindo casais homoafetivos)
    pessoas.forEach { pessoa ->
        // Se já foi processada, pular
        if (pessoa.id in pessoasProcessadas) return@forEach
        
        // Verificar se tem cônjuge
        val conjugeId = pessoa.conjugeAtual
        val conjuge = conjugeId?.let { pessoasMapCompleto[it] }
        
        // Verificar se o relacionamento é bidirecional (ambos se referenciam como cônjuges)
        // Isso evita criar famílias quando um cônjuge foi removido mas o outro ainda tem a referência
        val relacionamentoBidirecional = conjuge != null && conjuge.conjugeAtual == pessoa.id
        
        // Se tem cônjuge, relacionamento é bidirecional e não é Família Zero
        if (conjuge != null && relacionamentoBidirecional && !pessoa.ehFamiliaZero && !conjuge.ehFamiliaZero) {
            val conjugue1 = pessoa
            val conjugue2 = conjuge
            
            // Buscar filhos do casal (sem suposição de gênero)
            val filhosIds = mutableSetOf<String>()
            conjugue1.filhos.forEach { filhosIds.add(it) }
            conjugue2.filhos.forEach { filhosIds.add(it) }
            
            // Filhos também podem ser identificados por terem qualquer um dos cônjuges como pai ou mãe
            val filhosPorRelacao = pessoas.filter { filho ->
                (filho.pai == conjugue1.id || filho.mae == conjugue1.id) &&
                (filho.pai == conjugue2.id || filho.mae == conjugue2.id)
            }
            
            val todosFilhosIds = filhosIds + filhosPorRelacao.map { it.id }
            val filhos = todosFilhosIds.mapNotNull { pessoasMapCompleto[it] }
            
            // Criar família (suporta casais homoafetivos)
            val familiaId = conjugue1.id
            
            // Encontrar parentes colaterais se solicitado
            val parentesColaterais = if (incluirParentesColaterais) {
                val nucleo = listOf(conjugue1, conjugue2) + filhos
                encontrarParentesColaterais(nucleo, pessoas, pessoasMapCompleto, grauMaximoValido)
            } else {
                emptyMap()
            }
            
            familias.add(
                FamiliaGrupo(
                    id = familiaId,
                    conjugue1 = conjugue1,
                    conjugue2 = conjugue2,
                    filhos = filhos,
                    ehFamiliaZero = false,
                    ehFamiliaMonoparental = false,
                    ehFamiliaReconstituida = false,
                    conjugueAnterior = null,
                    familiaAnteriorId = null,
                    tipoNucleoFamiliar = TipoNucleoFamiliar.PARENTESCO,
                    parentesColaterais = parentesColaterais
                )
            )
            
            // Marcar como processadas
            pessoasProcessadas.add(conjugue1.id)
            pessoasProcessadas.add(conjugue2.id)
            filhos.forEach { pessoasProcessadas.add(it.id) }
        }
    }
    
    // Processar famílias monoparentais (pai/mãe solteiro com filhos)
    // IMPORTANTE: Verificar TODAS as pessoas, mesmo as já processadas, porque relacionamentos bidirecionais
    // podem ter atualizado a lista de filhos após o processamento inicial
    pessoas.forEach { pessoa ->
        // Se já foi processada, verificar se tem filhos que foram adicionados depois
        // Se não tem filhos, pular (já foi processada em outro contexto)
        val temFilhosNaLista = pessoa.filhos.isNotEmpty()
        val temFilhosPorRelacao = pessoas.any { filho ->
            (filho.pai == pessoa.id || filho.mae == pessoa.id) && filho.id != pessoa.id
        }
        val temFilhos = temFilhosNaLista || temFilhosPorRelacao
        
        if (pessoa.id in pessoasProcessadas && !temFilhos) {
            Timber.d("⏭️ Pulando pessoa ${pessoa.nome} (ID: ${pessoa.id}) - já foi processada e não tem filhos")
            return@forEach
        }
        
        // Se já foi processada mas tem filhos, verificar se precisa criar família monoparental
        if (pessoa.id in pessoasProcessadas && temFilhos) {
            Timber.d("🔄 Re-verificando pessoa ${pessoa.nome} (ID: ${pessoa.id}) - já foi processada mas tem filhos (pode ser monoparental)")
            // Verificar se a pessoa já está em uma família (como cônjuge)
            val jaEstaEmFamilia = familias.any { familia ->
                familia.conjugue1?.id == pessoa.id || familia.conjugue2?.id == pessoa.id
            }
            if (jaEstaEmFamilia) {
                Timber.d("   ⏭️ Pessoa ${pessoa.nome} já está em uma família como cônjuge, não criar monoparental")
                return@forEach
            }
            // Se já foi processada mas não está em família, remover da lista de processadas
            // para permitir que seja processada novamente como família monoparental
            pessoasProcessadas.remove(pessoa.id)
            Timber.d("   ✅ Removendo ${pessoa.nome} da lista de processadas para re-avaliar como monoparental")
        }
        val naoTemConjuge = pessoa.conjugeAtual == null || pessoasMapCompleto[pessoa.conjugeAtual] == null
        val naoEhFamiliaZero = !pessoa.ehFamiliaZero
        
        Timber.d("🔍 Verificando família monoparental para ${pessoa.nome} (ID: ${pessoa.id}):")
        Timber.d("   - Gênero: ${pessoa.genero}")
        Timber.d("   - Tem filhos na lista: $temFilhosNaLista (${pessoa.filhos.size} filhos)")
        Timber.d("   - Tem filhos por relação: $temFilhosPorRelacao")
        Timber.d("   - Tem filhos: $temFilhos")
        Timber.d("   - Não tem cônjuge: $naoTemConjuge (conjugeAtual: ${pessoa.conjugeAtual})")
        Timber.d("   - Não é Família Zero: $naoEhFamiliaZero")
        
        if (temFilhos && naoTemConjuge && naoEhFamiliaZero) {
            Timber.d("   ✅ Condições atendidas: processando família monoparental")
            // Buscar filhos da pessoa de múltiplas formas para garantir que sejam encontrados
            val filhosIds = mutableSetOf<String>()
            pessoa.filhos.forEach { filhosIds.add(it) }
            
            // Filhos também podem ser identificados por terem esta pessoa como pai ou mãe
            val filhosPorRelacao = pessoas.filter { filho ->
                filho.pai == pessoa.id || filho.mae == pessoa.id
            }
            filhosPorRelacao.forEach { filhosIds.add(it.id) }
            
            // Buscar filhos no pessoasMapCompleto primeiro (mais completo), depois no pessoasMap
            val filhos = filhosIds.mapNotNull { filhoId ->
                pessoasMapCompleto[filhoId] ?: pessoasMap[filhoId]
            }.filter { filho ->
                // Verificar se realmente é filho desta pessoa
                filho.pai == pessoa.id || filho.mae == pessoa.id
            }
            
            // Criar família monoparental
            Timber.d("   - Filhos encontrados: ${filhos.size}")
            filhos.forEach { filho ->
                Timber.d("      • ${filho.nome} (ID: ${filho.id}, pai: ${filho.pai}, mae: ${filho.mae})")
            }
            
            if (filhos.isNotEmpty()) {
                // Encontrar parentes colaterais se solicitado
                val parentesColaterais = if (incluirParentesColaterais) {
                    val nucleo = listOf(pessoa) + filhos
                    encontrarParentesColaterais(nucleo, pessoas, pessoasMapCompleto, grauMaximoValido)
                } else {
                    emptyMap()
                }
                
                // Verificar gênero: apenas mães criam família monoparental automaticamente
                if (pessoa.genero == Genero.MASCULINO) {
                    Timber.d("   - É PAI: verificando confirmação/rejeição")
                    // Pai + filhos: verificar se foi confirmado ou rejeitado
                    if (pessoa.id in familiasMonoparentaisConfirmadas) {
                        // Foi confirmado: criar a família normalmente
                        familias.add(
                            FamiliaGrupo(
                                id = pessoa.id,
                                conjugue1 = pessoa,
                                conjugue2 = null,
                                filhos = filhos,
                                ehFamiliaZero = false,
                                ehFamiliaMonoparental = true,
                                ehFamiliaReconstituida = false,
                                conjugueAnterior = null,
                                familiaAnteriorId = null,
                                tipoNucleoFamiliar = TipoNucleoFamiliar.PARENTESCO,
                                parentesColaterais = parentesColaterais
                            )
                        )
                        
                        // Marcar como processadas
                        pessoasProcessadas.add(pessoa.id)
                        filhos.forEach { pessoasProcessadas.add(it.id) }
                    } else if (pessoa.id !in familiasMonoparentaisRejeitadas) {
                        // Não foi confirmado e não foi rejeitado: adicionar à lista de pendências
                        familiasPendentes.add(
                            FamiliaMonoparentalPendente(
                                responsavel = pessoa,
                                filhos = filhos,
                                parentesColaterais = parentesColaterais
                            )
                        )
                        // Não criar a família automaticamente, mas marcar como processada para evitar duplicatas
                        pessoasProcessadas.add(pessoa.id)
                        filhos.forEach { pessoasProcessadas.add(it.id) }
                    }
                    // Se foi rejeitado, não fazer nada (não criar família nem adicionar às pendências)
                } else {
                    // Mãe + filhos ou pessoa sem gênero definido: criar normalmente
                    Timber.d("   ✅ Criando família monoparental automaticamente para ${pessoa.nome} (${pessoa.genero ?: "gênero indefinido"})")
                    familias.add(
                        FamiliaGrupo(
                            id = pessoa.id,
                            conjugue1 = pessoa,
                            conjugue2 = null,
                            filhos = filhos,
                            ehFamiliaZero = false,
                            ehFamiliaMonoparental = true,
                            ehFamiliaReconstituida = false,
                            conjugueAnterior = null,
                            familiaAnteriorId = null,
                            tipoNucleoFamiliar = TipoNucleoFamiliar.PARENTESCO,
                            parentesColaterais = parentesColaterais
                        )
                    )
                    Timber.d("   ✅ Família monoparental criada com sucesso para ${pessoa.nome}")
                    
                    // Marcar como processadas
                    pessoasProcessadas.add(pessoa.id)
                    filhos.forEach { pessoasProcessadas.add(it.id) }
                }
            } else {
                Timber.d("   ❌ Não criando família: filhos.isEmpty() (${filhos.size} filhos encontrados)")
            }
        } else {
            Timber.d("   ❌ Condições NÃO atendidas para criar família monoparental")
            if (!temFilhos) Timber.d("      - Motivo: não tem filhos")
            if (!naoTemConjuge) Timber.d("      - Motivo: tem cônjuge (${pessoa.conjugeAtual})")
            if (!naoEhFamiliaZero) Timber.d("      - Motivo: é Família Zero")
        }
    }
    
    Timber.d("📊 Resumo: ${familias.size} famílias criadas, ${familiasPendentes.size} pendentes")
    
    // Processar famílias anteriores (casamentos anteriores)
    pessoas.forEach { pessoa ->
        // Pular se não tem ex-cônjuges
        if (pessoa.exConjuges.isEmpty()) {
            return@forEach
        }
        
        // Identificar famílias anteriores desta pessoa
        val familiasAnteriores = identificarFamiliasAnteriores(pessoa, pessoas, pessoasMap)
        
        familiasAnteriores.forEach { familiaAnterior ->
            val familiaAnteriorKey = "${familiaAnterior.conjugue1?.id}_${familiaAnterior.conjugue2?.id}_anterior"
            
            // Evitar duplicatas (mesma família pode ser identificada por ambos os ex-cônjuges)
            if (familiaAnteriorKey !in familiasAnterioresProcessadas) {
                familias.add(familiaAnterior)
                familiasAnterioresProcessadas.add(familiaAnteriorKey)
                
                // Marcar pessoas como processadas (mas permitir que apareçam em múltiplas famílias)
                // Não adicionar à pessoasProcessadas para permitir múltiplas famílias
            }
        }
    }
    
    // Processar famílias residenciais (se solicitado)
    if (incluirResidenciais) {
        val familiasResidenciais = agruparPorResidencia(pessoas, pessoasMap)
        
        familiasResidenciais.forEach { familiaResidencial ->
            // Evitar duplicatas com famílias já processadas por parentesco
            val familiaJaExiste = familias.any { familiaExistente ->
                // Verificar se é a mesma família (mesmos cônjuges)
                val mesmoConjugue1 = familiaExistente.conjugue1?.id == familiaResidencial.conjugue1?.id
                val mesmoConjugue2 = familiaExistente.conjugue2?.id == familiaResidencial.conjugue2?.id
                val mesmoConjugue1Invertido = familiaExistente.conjugue1?.id == familiaResidencial.conjugue2?.id
                val mesmoConjugue2Invertido = familiaExistente.conjugue2?.id == familiaResidencial.conjugue1?.id
                
                (mesmoConjugue1 && mesmoConjugue2) || (mesmoConjugue1Invertido && mesmoConjugue2Invertido)
            }
            
            // Adicionar apenas se não for duplicata de uma família por parentesco
            if (!familiaJaExiste && familiaResidencial.id !in familiasResidenciaisProcessadas) {
                familias.add(familiaResidencial)
                familiasResidenciaisProcessadas.add(familiaResidencial.id)
            }
        }
    }
    
    // Ordenar: Família Zero primeiro, depois famílias atuais, depois famílias anteriores, depois residenciais
    val familiasOrdenadas = familias.sortedWith(
        compareByDescending<FamiliaGrupo> { it.ehFamiliaZero }
            .thenBy { it.ehFamiliaReconstituida } // Famílias atuais antes de anteriores
            .thenBy { it.tipoNucleoFamiliar != TipoNucleoFamiliar.PARENTESCO } // Parentesco antes de outros tipos
            .thenByDescending { it.filhos.size }
    )
    
    return ResultadoAgrupamentoFamilias(
        familias = familiasOrdenadas,
        familiasPendentes = familiasPendentes
    )
}

/**
 * Obtém o nome da família para exibição
 * Se for Família Zero, tenta usar o arvoreNome se disponível
 * Suporta famílias homoafetivas, monoparentais, reconstituídas e residenciais
 */
fun obterNomeFamilia(familia: FamiliaGrupo): String {
    val nome1 = familia.conjugue1?.nome ?: ""
    val nome2 = familia.conjugue2?.nome ?: ""
    
    // Adicionar sufixo baseado no tipo de núcleo
    val sufixo = when (familia.tipoNucleoFamiliar) {
        TipoNucleoFamiliar.RESIDENCIAL -> {
            val residencia = familia.conjugue1?.localResidencia ?: familia.conjugue2?.localResidencia
            if (residencia != null) " - $residencia" else " (Residencial)"
        }
        TipoNucleoFamiliar.RECONSTITUIDA -> " (Anterior)"
        TipoNucleoFamiliar.EMOCIONAL -> " (Emocional)"
        TipoNucleoFamiliar.ADOTIVA -> " (Adotiva)"
        TipoNucleoFamiliar.PARENTESCO -> ""
    }
    
    return when {
        // Família monoparental: usar nome do responsável + "e filhos"
        familia.ehFamiliaMonoparental && nome1.isNotEmpty() -> {
            val primeiroNome = nome1.split(" ").firstOrNull() ?: nome1
            if (familia.filhos.size == 1) {
                "$primeiroNome e ${familia.filhos.firstOrNull()?.nome?.split(" ")?.firstOrNull() ?: "filho"}$sufixo"
            } else {
                "$primeiroNome e filhos$sufixo"
            }
        }
        // Casal: usar nomes dos dois cônjuges (neutro em gênero)
        nome1.isNotEmpty() && nome2.isNotEmpty() -> {
            val primeiroNome1 = nome1.split(" ").firstOrNull() ?: nome1
            val primeiroNome2 = nome2.split(" ").firstOrNull() ?: nome2
            "$primeiroNome1 & $primeiroNome2$sufixo"
        }
        // Apenas um cônjuge (caso raro)
        nome1.isNotEmpty() -> "${nome1.split(" ").firstOrNull() ?: nome1}$sufixo"
        nome2.isNotEmpty() -> "${nome2.split(" ").firstOrNull() ?: nome2}$sufixo"
        // Fallback
        else -> {
            val tipo = when {
                familia.ehFamiliaMonoparental -> "Família Monoparental"
                familia.tipoNucleoFamiliar == TipoNucleoFamiliar.RESIDENCIAL -> "Família Residencial"
                familia.tipoNucleoFamiliar == TipoNucleoFamiliar.RECONSTITUIDA -> "Família Anterior"
                else -> "Família"
            }
            tipo
        }
    }
}

