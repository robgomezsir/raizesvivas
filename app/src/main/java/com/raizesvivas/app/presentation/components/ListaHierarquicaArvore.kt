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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Representa uma família (casal) com seus filhos
 */
data class FamiliaGrupo(
    val id: String, // ID único da família (pode ser o ID de um dos cônjuges)
    val conjugue1: Pessoa?,
    val conjugue2: Pessoa?,
    val filhos: List<Pessoa>,
    val ehFamiliaZero: Boolean = false
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
            containerColor = Color.White
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
 * Agrupa pessoas em famílias (casais e filhos)
 */
fun agruparPessoasPorFamilias(
    pessoas: List<Pessoa>,
    pessoasMap: Map<String, Pessoa>
): List<FamiliaGrupo> {
    val familias = mutableListOf<FamiliaGrupo>()
    val pessoasProcessadas = mutableSetOf<String>()
    
    // Primeiro, processar Família Zero
    val familiaZero = pessoas.filter { it.ehFamiliaZero }
    if (familiaZero.isNotEmpty()) {
        val pai = familiaZero.find { it.genero == com.raizesvivas.app.domain.model.Genero.MASCULINO }
            ?: familiaZero.firstOrNull()
        val mae = familiaZero.find { it.genero == com.raizesvivas.app.domain.model.Genero.FEMININO }
            ?: familiaZero.firstOrNull { it.id != pai?.id }
        
        // Buscar filhos da Família Zero
        val filhosIds = mutableSetOf<String>()
        pai?.filhos?.let { filhosIds.addAll(it) }
        mae?.filhos?.let { filhosIds.addAll(it) }
        
        // Filhos também podem ser identificados por terem estes dois como pais
        val filhosPorRelacao = pessoas.filter { filho ->
            (filho.pai == pai?.id || filho.mae == mae?.id) ||
            (filho.pai == mae?.id || filho.mae == pai?.id)
        }
        
        val todosFilhosIds = filhosIds + filhosPorRelacao.map { it.id }
        val filhos = todosFilhosIds.mapNotNull { pessoasMap[it] }
            .filter { pessoa -> 
                pessoa.pai == pai?.id || pessoa.pai == mae?.id || 
                pessoa.mae == pai?.id || pessoa.mae == mae?.id
            }
        
        val familiaId = pai?.id ?: mae?.id ?: "familia_zero"
        familias.add(
            FamiliaGrupo(
                id = familiaId,
                conjugue1 = pai,
                conjugue2 = mae,
                filhos = filhos,
                ehFamiliaZero = true
            )
        )
        
        pessoasProcessadas.add(pai?.id ?: "")
        mae?.id?.let { pessoasProcessadas.add(it) }
        filhos.forEach { pessoasProcessadas.add(it.id) }
    }
    
    // Processar outros casais
    pessoas.forEach { pessoa ->
        // Se já foi processada, pular
        if (pessoa.id in pessoasProcessadas) return@forEach
        
        // Verificar se tem cônjuge
        val conjugeId = pessoa.conjugeAtual
        val conjuge = conjugeId?.let { pessoasMap[it] }
        
        // Se tem cônjuge e não é Família Zero
        if (conjuge != null && !pessoa.ehFamiliaZero && !conjuge.ehFamiliaZero) {
            val conjugue1 = pessoa
            val conjugue2 = conjuge
            
            // Buscar filhos do casal
            val filhosIds = mutableSetOf<String>()
            conjugue1.filhos.forEach { filhosIds.add(it) }
            conjugue2.filhos.forEach { filhosIds.add(it) }
            
            // Filhos também podem ser identificados por terem estes dois como pais
            val filhosPorRelacao = pessoas.filter { filho ->
                (filho.pai == conjugue1.id || filho.pai == conjugue2.id) &&
                (filho.mae == conjugue1.id || filho.mae == conjugue2.id)
            }
            
            val todosFilhosIds = filhosIds + filhosPorRelacao.map { it.id }
            val filhos = todosFilhosIds.mapNotNull { pessoasMap[it] }
            
            // Criar família
            val familiaId = conjugue1.id
            familias.add(
                FamiliaGrupo(
                    id = familiaId,
                    conjugue1 = conjugue1,
                    conjugue2 = conjugue2,
                    filhos = filhos,
                    ehFamiliaZero = false
                )
            )
            
            // Marcar como processadas
            pessoasProcessadas.add(conjugue1.id)
            pessoasProcessadas.add(conjugue2.id)
            filhos.forEach { pessoasProcessadas.add(it.id) }
        }
    }
    
    // Ordenar: Família Zero primeiro, depois por número de filhos
    return familias.sortedWith(
        compareByDescending<FamiliaGrupo> { it.ehFamiliaZero }
            .thenByDescending { it.filhos.size }
    )
}

/**
 * Obtém o nome da família para exibição
 * Se for Família Zero, tenta usar o arvoreNome se disponível
 */
fun obterNomeFamilia(familia: FamiliaGrupo): String {
    // Se for Família Zero, tentar usar arvoreNome (será buscado do repositório)
    // Por enquanto, usar nomes dos cônjuges
    val nome1 = familia.conjugue1?.nome ?: ""
    val nome2 = familia.conjugue2?.nome ?: ""
    
    return when {
        nome1.isNotEmpty() && nome2.isNotEmpty() -> {
            // Extrair primeiro nome de cada
            val primeiroNome1 = nome1.split(" ").firstOrNull() ?: nome1
            val primeiroNome2 = nome2.split(" ").firstOrNull() ?: nome2
            "$primeiroNome1 & $primeiroNome2"
        }
        nome1.isNotEmpty() -> nome1.split(" ").firstOrNull() ?: nome1
        nome2.isNotEmpty() -> nome2.split(" ").firstOrNull() ?: nome2
        else -> "Família"
    }
}

