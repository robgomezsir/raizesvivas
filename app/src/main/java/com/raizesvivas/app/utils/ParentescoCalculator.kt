package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import timber.log.Timber

/**
 * Calculadora de parentesco entre pessoas na árvore genealógica
 * 
 * Calcula o grau de parentesco entre duas pessoas usando ancestral comum
 */
object ParentescoCalculator {
    
    /**
     * Resultado do cálculo de parentesco
     */
    data class ResultadoParentesco(
        val parentesco: String,           // Descrição do parentesco (ex: "Pai", "Irmão", "Primo")
        val grau: Int,                     // Grau de parentesco (0 = mesma pessoa, 1 = parente direto)
        val distancia: Int                // Distância total na árvore
    )
    
    /**
     * Calcula o parentesco entre duas pessoas
     * 
     * @param pessoa1 Primeira pessoa (geralmente o usuário vinculado)
     * @param pessoa2 Segunda pessoa (o familiar)
     * @param pessoasMap Mapa de todas as pessoas para busca rápida
     * @return Resultado do parentesco
     */
    fun calcularParentesco(
        pessoa1: Pessoa,
        pessoa2: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): ResultadoParentesco {
        // Se são a mesma pessoa
        if (pessoa1.id == pessoa2.id) {
            return ResultadoParentesco("Você mesmo", 0, 0)
        }
        
        // Encontrar ancestral comum mais próximo
        val ancestralComum = encontrarAncestralComumMaisProximo(pessoa1, pessoa2, pessoasMap)
        
        if (ancestralComum == null) {
            return ResultadoParentesco("Sem parentesco conhecido", -1, -1)
        }
        
        // Calcular distâncias até o ancestral comum
        val distancia1 = calcularDistancia(pessoa1, ancestralComum.id, pessoasMap)
        val distancia2 = calcularDistancia(pessoa2, ancestralComum.id, pessoasMap)
        
        val distanciaTotal = distancia1 + distancia2
        
        // Determinar tipo de parentesco
        val parentesco = determinarTipoParentesco(distancia1, distancia2, pessoa1, pessoa2, pessoasMap)
        
        return ResultadoParentesco(parentesco, distanciaTotal, distanciaTotal)
    }
    
    /**
     * Encontra o ancestral comum mais próximo entre duas pessoas
     */
    private fun encontrarAncestralComumMaisProximo(
        pessoa1: Pessoa,
        pessoa2: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): Pessoa? {
        // Coletar todos os ancestrais da pessoa1
        val ancestrais1 = coletarAncestrais(pessoa1, pessoasMap)
        
        // Coletar todos os ancestrais da pessoa2
        val ancestrais2 = coletarAncestrais(pessoa2, pessoasMap)
        
        // Encontrar ancestral comum mais próximo (menor distância)
        var ancestralComum: Pessoa? = null
        var menorDistancia = Int.MAX_VALUE
        
        ancestrais1.forEach { (ancestralId, distancia) ->
            if (ancestrais2.containsKey(ancestralId)) {
                val distanciaTotal = distancia + ancestrais2[ancestralId]!!
                if (distanciaTotal < menorDistancia) {
                    menorDistancia = distanciaTotal
                    ancestralComum = pessoasMap[ancestralId]
                }
            }
        }
        
        return ancestralComum
    }
    
    /**
     * Coleta todos os ancestrais de uma pessoa com suas distâncias
     */
    private fun coletarAncestrais(
        pessoa: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): Map<String, Int> {
        val ancestrais = mutableMapOf<String, Int>()
        val visitados = mutableSetOf<String>()
        
        fun coletarRecursivo(p: Pessoa, distancia: Int) {
            if (p.id in visitados) return
            visitados.add(p.id)
            
            ancestrais[p.id] = distancia
            
            // Adicionar pai
            p.pai?.let { paiId ->
                pessoasMap[paiId]?.let { pai ->
                    coletarRecursivo(pai, distancia + 1)
                }
            }
            
            // Adicionar mãe
            p.mae?.let { maeId ->
                pessoasMap[maeId]?.let { mae ->
                    coletarRecursivo(mae, distancia + 1)
                }
            }
        }
        
        coletarRecursivo(pessoa, 0)
        return ancestrais
    }
    
    /**
     * Calcula a distância entre uma pessoa e um ancestral
     */
    private fun calcularDistancia(
        pessoa: Pessoa,
        ancestralId: String,
        pessoasMap: Map<String, Pessoa>
    ): Int {
        if (pessoa.id == ancestralId) return 0
        
        val visitados = mutableSetOf<String>()
        
        fun buscarRecursivo(p: Pessoa, distancia: Int): Int? {
            if (p.id == ancestralId) return distancia
            if (p.id in visitados) return null
            visitados.add(p.id)
            
            // Buscar no pai
            p.pai?.let { paiId ->
                pessoasMap[paiId]?.let { pai ->
                    buscarRecursivo(pai, distancia + 1)?.let { return it }
                }
            }
            
            // Buscar na mãe
            p.mae?.let { maeId ->
                pessoasMap[maeId]?.let { mae ->
                    buscarRecursivo(mae, distancia + 1)?.let { return it }
                }
            }
            
            return null
        }
        
        return buscarRecursivo(pessoa, 0) ?: -1
    }
    
    /**
     * Determina o tipo de parentesco baseado nas distâncias
     */
    private fun determinarTipoParentesco(
        distancia1: Int,
        distancia2: Int,
        pessoa1: Pessoa,
        pessoa2: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): String {
        // Parentesco direto (linha ascendente ou descendente)
        when {
            distancia1 == 0 && distancia2 == 1 -> {
                // pessoa2 é pai ou mãe de pessoa1
                return when {
                    pessoa2.id == pessoa1.pai -> "Pai"
                    pessoa2.id == pessoa1.mae -> "Mãe"
                    else -> "Pai/Mãe"
                }
            }
            
            distancia1 == 1 && distancia2 == 0 -> {
                // pessoa2 é filho de pessoa1
                return if (pessoa2.pai == pessoa1.id || pessoa2.mae == pessoa1.id) {
                    "Filho(a)"
                } else {
                    "Filho(a)"
                }
            }
            
            distancia1 == 0 && distancia2 == 2 -> {
                // pessoa2 é avô/avó de pessoa1
                return when {
                    pessoa2.id == pessoa1.pai?.let { pessoasMap[it]?.pai } -> "Avô"
                    pessoa2.id == pessoa1.pai?.let { pessoasMap[it]?.mae } -> "Avó"
                    pessoa2.id == pessoa1.mae?.let { pessoasMap[it]?.pai } -> "Avô"
                    pessoa2.id == pessoa1.mae?.let { pessoasMap[it]?.mae } -> "Avó"
                    else -> "Avô/Avó"
                }
            }
            
            distancia1 == 2 && distancia2 == 0 -> {
                // pessoa2 é neto de pessoa1
                return "Neto(a)"
            }
            
            distancia1 == 0 && distancia2 == 3 -> {
                // pessoa2 é bisavô/bisavó
                return "Bisavô/Bisavó"
            }
            
            distancia1 == 3 && distancia2 == 0 -> {
                // pessoa2 é bisneto
                return "Bisneto(a)"
            }
            
            distancia1 == 1 && distancia2 == 1 -> {
                // Irmãos (mesmos pais)
                val saoIrmaos = (pessoa1.pai != null && pessoa1.pai == pessoa2.pai) ||
                               (pessoa1.mae != null && pessoa1.mae == pessoa2.mae)
                
                if (saoIrmaos) {
                    return "Irmão(ã)"
                } else {
                    return "Primo(a)"
                }
            }
            
            distancia1 == 1 && distancia2 == 2 -> {
                // pessoa2 é tio/tia de pessoa1
                return when {
                    pessoa2.id == pessoa1.pai?.let { pessoasMap[it]?.let { p -> p.pai?.let { pessoasMap[it]?.filhos?.find { it != pessoa1.pai } } } } -> "Tio"
                    pessoa2.id == pessoa1.mae?.let { pessoasMap[it]?.let { p -> p.pai?.let { pessoasMap[it]?.filhos?.find { it != pessoa1.mae } } } } -> "Tio"
                    pessoa2.id == pessoa1.pai?.let { pessoasMap[it]?.let { p -> p.mae?.let { pessoasMap[it]?.filhos?.find { it != pessoa1.pai } } } } -> "Tia"
                    pessoa2.id == pessoa1.mae?.let { pessoasMap[it]?.let { p -> p.mae?.let { pessoasMap[it]?.filhos?.find { it != pessoa1.mae } } } } -> "Tia"
                    else -> "Tio/Tia"
                }
            }
            
            distancia1 == 2 && distancia2 == 1 -> {
                // pessoa2 é sobrinho de pessoa1
                return "Sobrinho(a)"
            }
            
            distancia1 == 1 && distancia2 == 3 -> {
                // pessoa2 é primo(a) de pessoa1
                return "Primo(a)"
            }
            
            distancia1 == 2 && distancia2 == 2 -> {
                // Mesmos avós
                return "Primo(a)"
            }
            
            distancia1 == 0 && distancia2 > 3 -> {
                // Ancestral mais distante
                return when (distancia2) {
                    4 -> "Trisavô/Trisavó"
                    else -> "Ancestral ($distancia2 gerações)"
                }
            }
            
            distancia1 > 3 && distancia2 == 0 -> {
                // Descendente mais distante
                return when (distancia1) {
                    4 -> "Trisneto(a)"
                    else -> "Descendente ($distancia1 gerações)"
                }
            }
            
            else -> {
                // Parentesco mais complexo
                val grau = distancia1 + distancia2
                return when {
                    grau <= 4 -> "Parente ($grau° grau)"
                    else -> "Parente distante"
                }
            }
        }
    }
    
    /**
     * Calcula parentesco para todas as pessoas
     */
    fun calcularTodosParentescos(
        pessoaReferencia: Pessoa,
        todasPessoas: List<Pessoa>,
        pessoasMap: Map<String, Pessoa>
    ): List<Pair<Pessoa, ResultadoParentesco>> {
        return todasPessoas
            .filter { it.id != pessoaReferencia.id }
            .map { pessoa ->
                val parentesco = calcularParentesco(pessoaReferencia, pessoa, pessoasMap)
                Pair(pessoa, parentesco)
            }
            .filter { it.second.grau >= 0 } // Filtrar apenas parentes conhecidos
            .sortedBy { it.second.grau } // Ordenar por grau de parentesco
    }
}