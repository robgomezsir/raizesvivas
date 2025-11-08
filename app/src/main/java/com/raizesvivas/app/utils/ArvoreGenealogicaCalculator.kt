package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import timber.log.Timber
import kotlin.math.*

/**
 * Calculadora de layout otimizado para árvore genealógica
 * 
 * Implementa algoritmo de posicionamento baseado em agrupamento familiar:
 * - Casais ficam lado a lado
 * - Filhos ficam abaixo dos pais, organizados horizontalmente
 * - Layout vertical tradicional mas otimizado
 * - Evita sobreposições e maximiza espaço
 */
object ArvoreGenealogicaCalculator {
    
    // Espaçamento entre elementos
    private const val ESPACO_HORIZONTAL_FAMILIA = 200f // Espaço entre famílias (casais + filhos)
    private const val ESPACO_VERTICAL_GERACAO = 180f   // Espaço entre gerações
    private const val ESPACO_CONJUGE = 60f              // Espaço entre cônjuges
    private const val ESPACO_FILHOS = 120f             // Espaço entre filhos
    private const val NO_SIZE = 90f                    // Tamanho do nó
    
    /**
     * Posição de um nó na árvore
     */
    data class PosicaoNo(
        val pessoa: Pessoa,
        val x: Float,
        val y: Float,
        val geracao: Int,           // Geração (0 = raiz)
        val tipoRelacao: TipoRelacao? = null
    )
    
    /**
     * Tipo de relacionamento
     */
    enum class TipoRelacao {
        PAI,
        MAE,
        CONJUGE,
        FILHO,
        IRMAO,
        AVO,
        NETO,
        OUTRO
    }
    
    /**
     * Calcula posições da árvore genealógica
     * 
     * @param todasPessoas Todas as pessoas disponíveis
     * @param raizId ID da pessoa raiz (null = Família Zero)
     * @param pessoasMap Mapa de pessoas por ID
     * @return Lista de posições calculadas
     */
    fun calcularPosicoes(
        todasPessoas: List<Pessoa>,
        raizId: String?,
        pessoasMap: Map<String, Pessoa>
    ): List<PosicaoNo> {
        if (todasPessoas.isEmpty()) {
            Timber.w("⚠️ Nenhuma pessoa disponível")
            return emptyList()
        }
        
        // Determinar raiz
        val raiz = when {
            raizId != null -> pessoasMap[raizId]
            else -> {
                todasPessoas.firstOrNull { it.ehFamiliaZero }
                    ?: todasPessoas.firstOrNull { it.pai == null && it.mae == null }
                    ?: todasPessoas.first()
            }
        }
        
        if (raiz == null) {
            Timber.w("⚠️ Não foi possível determinar raiz")
            return emptyList()
        }
        
        Timber.d("🌳 Calculando árvore a partir de: ${raiz.nome}")
        
        // Coletar todas as pessoas conectadas à raiz (incluindo a raiz)
        val pessoasConectadas = coletarPessoasConectadas(raiz, pessoasMap) + raiz
        
        // Calcular gerações relativas à raiz (incluindo a raiz)
        val geracoes = calcularGeracoesRelativas(raiz, pessoasConectadas, pessoasMap)
        
        // Normalizar gerações para começar em 0
        val minGeracao = (geracoes.values + 0).min()
        val geracoesNormalizadas = geracoes.mapValues { it.value - minGeracao }.toMutableMap()
        
        // Incluir raiz com geração normalizada 0
        if (raiz.id !in geracoesNormalizadas) {
            geracoesNormalizadas[raiz.id] = 0
        }
        
        // Agrupar por geração normalizada (incluindo todas as pessoas conectadas + raiz)
        val todasPessoasParaPosicionar = pessoasConectadas.distinctBy { it.id }
        val pessoasPorGeracao = todasPessoasParaPosicionar
            .groupBy { geracoesNormalizadas[it.id] ?: 0 }
            .mapValues { (_, pessoas) -> pessoas }
        
        Timber.d("📊 Gerações: ${pessoasPorGeracao.keys.min()} a ${pessoasPorGeracao.keys.max()}")
        
        val posicoes = mutableListOf<PosicaoNo>()
        val visitados = mutableSetOf<String>()
        
        // Posicionar por geração, começando da mais antiga
        pessoasPorGeracao.toSortedMap().forEach { (geracao, pessoas) ->
            // Agrupar cônjuges juntos
            val pessoasAgrupadas = agruparConjuges(pessoas, pessoasMap)
            
            // Calcular largura total necessária para esta geração
            val larguraTotal = calcularLarguraGeracaoAgrupadas(pessoasAgrupadas)
            
            // Posição Y (geração mais antiga no topo)
            val y = geracao * ESPACO_VERTICAL_GERACAO
            
            // Posicionar pessoas horizontalmente, centralizadas
            var xAtual = -larguraTotal / 2f
            
            pessoasAgrupadas.forEach { grupo ->
                val larguraGrupo = calcularLarguraGrupo(grupo)
                
                grupo.forEachIndexed { index, pessoa ->
                    if (pessoa.id !in visitados) {
                        visitados.add(pessoa.id)
                        
                        val xPessoa = xAtual + (larguraGrupo / 2f) + 
                            (index - (grupo.size - 1) / 2f) * ESPACO_CONJUGE
                        
                        val tipoRelacao = determinarTipoRelacao(pessoa, raiz, geracaoNormalizada = geracao)
                        
                        posicoes.add(PosicaoNo(pessoa, xPessoa, y, geracao, tipoRelacao))
                    }
                }
                
                xAtual += larguraGrupo + ESPACO_HORIZONTAL_FAMILIA
            }
        }
        
        // Adicionar raiz se não estiver nas posições
        if (raiz.id !in visitados) {
            posicoes.add(PosicaoNo(raiz, 0f, 0f, 0, null))
        }
        
        // Centralizar horizontalmente
        val minX = posicoes.minOfOrNull { it.x } ?: 0f
        val maxX = posicoes.maxOfOrNull { it.x } ?: 0f
        val centroX = (minX + maxX) / 2f
        
        Timber.d("✅ Árvore calculada: ${posicoes.size} pessoas posicionadas")
        
        return posicoes.map { pos ->
            pos.copy(x = pos.x - centroX)
        }
    }
    
    /**
     * Coleta todas as pessoas conectadas à raiz
     */
    private fun coletarPessoasConectadas(
        raiz: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): List<Pessoa> {
        val visitados = mutableSetOf<String>()
        val resultado = mutableListOf<Pessoa>()
        
        fun coletarRecursivo(pessoa: Pessoa) {
            if (pessoa.id in visitados) return
            visitados.add(pessoa.id)
            
            if (pessoa.id != raiz.id) {
                resultado.add(pessoa)
            }
            
            // Adicionar cônjuge
            pessoa.conjugeAtual?.let { conjugeId ->
                pessoasMap[conjugeId]?.let { conjuge ->
                    coletarRecursivo(conjuge)
                }
            }
            
            // Adicionar pais
            pessoa.pai?.let { paiId ->
                pessoasMap[paiId]?.let { pai ->
                    coletarRecursivo(pai)
                }
            }
            
            pessoa.mae?.let { maeId ->
                pessoasMap[maeId]?.let { mae ->
                    coletarRecursivo(mae)
                }
            }
            
            // Adicionar filhos
            pessoa.filhos.forEach { filhoId ->
                pessoasMap[filhoId]?.let { filho ->
                    coletarRecursivo(filho)
                }
            }
        }
        
        // Começar pela raiz e expandir
        visitados.add(raiz.id)
        
        raiz.conjugeAtual?.let { conjugeId ->
            pessoasMap[conjugeId]?.let { coletarRecursivo(it) }
        }
        
        raiz.pai?.let { paiId ->
            pessoasMap[paiId]?.let { coletarRecursivo(it) }
        }
        
        raiz.mae?.let { maeId ->
            pessoasMap[maeId]?.let { coletarRecursivo(it) }
        }
        
        raiz.filhos.forEach { filhoId ->
            pessoasMap[filhoId]?.let { coletarRecursivo(it) }
        }
        
        return resultado
    }
    
    /**
     * Calcula gerações relativas à raiz
     */
    private fun calcularGeracoesRelativas(
        raiz: Pessoa,
        @Suppress("UNUSED_PARAMETER") pessoasConectadas: List<Pessoa>,
        pessoasMap: Map<String, Pessoa>
    ): Map<String, Int> {
        val geracoes = mutableMapOf<String, Int>()
        val visitados = mutableSetOf<String>()
        
        fun calcularRecursivo(pessoa: Pessoa, geracao: Int) {
            if (pessoa.id in visitados) return
            visitados.add(pessoa.id)
            
            geracoes[pessoa.id] = geracao
            
            // Cônjuge (mesma geração)
            pessoa.conjugeAtual?.let { conjugeId ->
                pessoasMap[conjugeId]?.let { conjuge ->
                    if (conjuge.id !in visitados) {
                        calcularRecursivo(conjuge, geracao)
                    }
                }
            }
            
            // Pais (geração -1)
            pessoa.pai?.let { paiId ->
                pessoasMap[paiId]?.let { pai ->
                    calcularRecursivo(pai, geracao - 1)
                }
            }
            
            pessoa.mae?.let { maeId ->
                pessoasMap[maeId]?.let { mae ->
                    calcularRecursivo(mae, geracao - 1)
                }
            }
            
            // Filhos (geração +1)
            pessoa.filhos.forEach { filhoId ->
                pessoasMap[filhoId]?.let { filho ->
                    calcularRecursivo(filho, geracao + 1)
                }
            }
        }
        
        // Começar pela raiz na geração 0
        visitados.add(raiz.id)
        geracoes[raiz.id] = 0
        
        raiz.conjugeAtual?.let { conjugeId ->
            pessoasMap[conjugeId]?.let { calcularRecursivo(it, 0) }
        }
        
        raiz.pai?.let { paiId ->
            pessoasMap[paiId]?.let { calcularRecursivo(it, -1) }
        }
        
        raiz.mae?.let { maeId ->
            pessoasMap[maeId]?.let { calcularRecursivo(it, -1) }
        }
        
        raiz.filhos.forEach { filhoId ->
            pessoasMap[filhoId]?.let { calcularRecursivo(it, 1) }
        }
        
        return geracoes
    }
    
    /**
     * Agrupa pessoas que são cônjuges
     */
    private fun agruparConjuges(
        pessoas: List<Pessoa>,
        pessoasMap: Map<String, Pessoa>
    ): List<List<Pessoa>> {
        val grupos = mutableListOf<List<Pessoa>>()
        val processados = mutableSetOf<String>()
        
        pessoas.forEach { pessoa ->
            if (pessoa.id in processados) return@forEach
            
            val grupo = mutableListOf<Pessoa>()
            
            // Adicionar pessoa atual
            grupo.add(pessoa)
            processados.add(pessoa.id)
            
            // Adicionar cônjuge se existir na mesma geração
            pessoa.conjugeAtual?.let { conjugeId ->
                pessoasMap[conjugeId]?.let { conjuge ->
                    if (conjuge.id !in processados && pessoas.contains(conjuge)) {
                        grupo.add(conjuge)
                        processados.add(conjuge.id)
                    }
                }
            }
            
            grupos.add(grupo)
        }
        
        return grupos
    }
    
    /**
     * Calcula largura necessária para uma geração (agrupada)
     */
    private fun calcularLarguraGeracaoAgrupadas(grupos: List<List<Pessoa>>): Float {
        if (grupos.isEmpty()) return 0f
        
        val larguraGrupos = grupos.sumOf { calcularLarguraGrupo(it).toDouble() }.toFloat()
        val espacosEntreGrupos = max(0f, (grupos.size - 1) * ESPACO_HORIZONTAL_FAMILIA)
        
        return larguraGrupos + espacosEntreGrupos
    }
    
    /**
     * Calcula largura de um grupo (casal ou pessoa solteira)
     */
    private fun calcularLarguraGrupo(grupo: List<Pessoa>): Float {
        return when {
            grupo.isEmpty() -> 0f
            grupo.size == 1 -> NO_SIZE
            else -> NO_SIZE * grupo.size + ESPACO_CONJUGE * (grupo.size - 1)
        }
    }
    
    /**
     * Estrutura de família (casal + filhos) - REMOVIDO (não usado mais)
     */
    @Suppress("UNUSED")
    private data class Familia(
        val pais: List<Pessoa>,      // Casal (pai e mãe)
        val filhos: List<String>     // IDs dos filhos
    )
    
    /**
     * Agrupa pessoas em famílias (casais + filhos) - REMOVIDO (não usado mais)
     */
    @Suppress("UNUSED")
    private fun agruparEmFamilias(
        pessoas: List<Pessoa>,
        pessoasMap: Map<String, Pessoa>
    ): List<Familia> {
        val familias = mutableListOf<Familia>()
        val processados = mutableSetOf<String>()
        
        // Primeiro, agrupar por casais
        pessoas.forEach { pessoa ->
            if (pessoa.id in processados) return@forEach
            
            val pais = mutableListOf<Pessoa>()
            val filhos = mutableSetOf<String>()
            
            // Adicionar pessoa atual como pai/mãe
            pais.add(pessoa)
            processados.add(pessoa.id)
            
            // Adicionar cônjuge se existir na mesma geração
            pessoa.conjugeAtual?.let { conjugeId ->
                pessoasMap[conjugeId]?.let { conjuge ->
                    if (conjuge.id !in processados && pessoas.contains(conjuge)) {
                        pais.add(conjuge)
                        processados.add(conjuge.id)
                    }
                }
            }
            
            // Adicionar filhos compartilhados (filhos que têm ambos os pais nesta família)
            // NÃO marcar como processados aqui, pois eles serão posicionados na próxima geração
            pessoa.filhos.forEach { filhoId ->
                val filho = pessoasMap[filhoId]
                if (filho != null) {
                    val outroPai = filho.pai?.let { pessoasMap[it] } ?: filho.mae?.let { pessoasMap[it] }
                    // Se o outro pai também está na lista de pais ou se não há outro pai
                    if (outroPai == null || pais.contains(outroPai)) {
                        filhos.add(filhoId)
                        // NÃO processar aqui - filhos serão processados na próxima geração
                    }
                }
            }
            
            // Também adicionar filhos do cônjuge se ele estiver na lista
            pais.forEach { pai ->
                if (pai.id != pessoa.id) {
                    pai.filhos.forEach { filhoId ->
                        if (filhoId !in filhos) {
                            filhos.add(filhoId)
                            // NÃO processar aqui - filhos serão processados na próxima geração
                        }
                    }
                }
            }
            
            familias.add(Familia(pais, filhos.toList()))
        }
        
        return familias
    }
    
    /**
     * Calcula largura necessária para uma geração
     */
    @Suppress("UNUSED")
    private fun calcularLarguraGeracao(familias: List<Familia>): Float {
        if (familias.isEmpty()) return 0f
        
        val larguraFamilias = familias.sumOf { calcularLarguraFamilia(it).toDouble() }.toFloat()
        val espacosEntreFamilias = max(0f, (familias.size - 1) * ESPACO_HORIZONTAL_FAMILIA)
        
        return larguraFamilias + espacosEntreFamilias
    }
    
    /**
     * Calcula largura de uma família
     */
    @Suppress("UNUSED")
    private fun calcularLarguraFamilia(familia: Familia): Float {
        val larguraCasal = when (familia.pais.size) {
            1 -> NO_SIZE
            2 -> NO_SIZE * 2 + ESPACO_CONJUGE
            else -> NO_SIZE * familia.pais.size + ESPACO_CONJUGE * (familia.pais.size - 1)
        }
        
        // Filhos não afetam a largura da família nesta geração
        return larguraCasal
    }
    
    /**
     * Determina tipo de relacionamento
     */
    private fun determinarTipoRelacao(
        pessoa: Pessoa,
        raiz: Pessoa,
        geracaoNormalizada: Int
    ): TipoRelacao {
        return when {
            pessoa.id == raiz.pai -> TipoRelacao.PAI
            pessoa.id == raiz.mae -> TipoRelacao.MAE
            pessoa.id == raiz.conjugeAtual -> TipoRelacao.CONJUGE
            pessoa.id in raiz.filhos -> TipoRelacao.FILHO
            geracaoNormalizada < 0 -> TipoRelacao.AVO
            geracaoNormalizada > 1 -> TipoRelacao.NETO
            geracaoNormalizada == 1 -> TipoRelacao.FILHO
            else -> TipoRelacao.OUTRO
        }
    }
    
    /**
     * Encontra o casal da Família Zero
     */
    fun encontrarCasalFamiliaZero(pessoas: List<Pessoa>): Pair<Pessoa?, Pessoa?> {
        val familiaZero = pessoas.filter { it.ehFamiliaZero }
        
        if (familiaZero.size >= 2) {
            val pessoa1 = familiaZero[0]
            val pessoa2 = familiaZero[1]
            
            if (pessoa1.conjugeAtual == pessoa2.id) {
                return Pair(pessoa1, pessoa2)
            } else if (pessoa2.conjugeAtual == pessoa1.id) {
                return Pair(pessoa2, pessoa1)
            }
            
            val pai = familiaZero.firstOrNull { it.pai == null }
            val mae = familiaZero.firstOrNull { it.mae == null }
            
            if (pai != null && mae != null) {
                return Pair(pai, mae)
            }
        }
        
        return Pair(familiaZero.firstOrNull(), null)
    }
}
