package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import timber.log.Timber
import java.text.Normalizer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Detector de duplicatas usando algoritmo de similaridade
 * 
 * Implementa múltiplas técnicas de comparação:
 * - Comparação de nomes (Levenshtein + normalização)
 * - Comparação de datas (nascimento, falecimento)
 * - Comparação de pais (relacionamentos familiares)
 */
object DuplicateDetector {
    
    /**
     * Resultado da detecção de duplicata
     */
    data class DuplicataResultado(
        val pessoa1: Pessoa,
        val pessoa2: Pessoa,
        val scoreSimilaridade: Float, // 0.0 a 1.0
        val razoes: List<String> = emptyList()
    ) {
        val isDuplicata: Boolean
            get() = scoreSimilaridade >= 0.8f // Threshold de 80%
    }
    
    /**
     * Detecta possíveis duplicatas para uma pessoa
     * 
     * @param pessoa Pessoa a ser verificada
     * @param outrasPessoas Lista de outras pessoas para comparar
     * @param threshold Score mínimo para considerar duplicata (padrão: 0.8)
     * @return Lista de resultados de duplicatas encontradas
     */
    fun detectarDuplicatas(
        pessoa: Pessoa,
        outrasPessoas: List<Pessoa>,
        threshold: Float = 0.8f
    ): List<DuplicataResultado> {
        val resultados = mutableListOf<DuplicataResultado>()
        
        outrasPessoas.forEach { outraPessoa ->
            if (pessoa.id != outraPessoa.id) {
                val resultado = compararPessoas(pessoa, outraPessoa)
                
                if (resultado.scoreSimilaridade >= threshold) {
                    resultados.add(resultado)
                }
            }
        }
        
        // Ordenar por score decrescente
        return resultados.sortedByDescending { it.scoreSimilaridade }
    }
    
    /**
     * Compara duas pessoas e retorna score de similaridade
     */
    private fun compararPessoas(
        pessoa1: Pessoa,
        pessoa2: Pessoa
    ): DuplicataResultado {
        val razoes = mutableListOf<String>()
        var scoreTotal = 0f
        var pesoTotal = 0f
        
        // 1. Comparação de nomes (peso: 40%)
        val nomeSimilaridade = compararNomes(pessoa1.nome, pessoa2.nome)
        if (nomeSimilaridade > 0.7f) {
            razoes.add("Nomes similares (${(nomeSimilaridade * 100).toInt()}%)")
        }
        scoreTotal += nomeSimilaridade * 0.4f
        pesoTotal += 0.4f
        
        // 2. Comparação de data de nascimento (peso: 25%)
        val dataSimilaridade = compararDatas(
            pessoa1.dataNascimento,
            pessoa2.dataNascimento
        )
        if (dataSimilaridade > 0.7f) {
            razoes.add("Datas de nascimento similares")
        }
        scoreTotal += dataSimilaridade * 0.25f
        pesoTotal += 0.25f
        
        // 3. Comparação de pais (peso: 20%)
        val paisSimilaridade = compararPais(pessoa1, pessoa2)
        if (paisSimilaridade > 0.7f) {
            razoes.add("Mesmos pais")
        }
        scoreTotal += paisSimilaridade * 0.2f
        pesoTotal += 0.2f
        
        // 4. Comparação de local de nascimento (peso: 10%)
        val localSimilaridade = compararLocais(
            pessoa1.localNascimento,
            pessoa2.localNascimento
        )
        if (localSimilaridade > 0.7f) {
            razoes.add("Locais de nascimento similares")
        }
        scoreTotal += localSimilaridade * 0.1f
        pesoTotal += 0.1f
        
        // 5. Comparação de data de falecimento (peso: 5%)
        val falecimentoSimilaridade = compararDatas(
            pessoa1.dataFalecimento,
            pessoa2.dataFalecimento
        )
        if (falecimentoSimilaridade > 0.7f) {
            razoes.add("Datas de falecimento similares")
        }
        scoreTotal += falecimentoSimilaridade * 0.05f
        pesoTotal += 0.05f
        
        // Score final normalizado
        val scoreFinal = if (pesoTotal > 0) scoreTotal / pesoTotal else 0f
        
        return DuplicataResultado(
            pessoa1 = pessoa1,
            pessoa2 = pessoa2,
            scoreSimilaridade = scoreFinal.coerceIn(0f, 1f),
            razoes = razoes
        )
    }
    
    /**
     * Compara dois nomes usando Levenshtein + normalização
     */
    private fun compararNomes(nome1: String, nome2: String): Float {
        if (nome1.isBlank() || nome2.isBlank()) {
            return 0f
        }
        
        val nome1Normalizado = normalizarTexto(nome1.lowercase())
        val nome2Normalizado = normalizarTexto(nome2.lowercase())
        
        // Comparação exata
        if (nome1Normalizado == nome2Normalizado) {
            return 1.0f
        }
        
        // Comparação com Levenshtein
        val distancia = levenshteinDistance(nome1Normalizado, nome2Normalizado)
        val tamanhoMax = max(nome1Normalizado.length, nome2Normalizado.length)
        
        if (tamanhoMax == 0) return 0f
        
        val similaridade = 1f - (distancia.toFloat() / tamanhoMax)
        
        // Verificar se um nome contém o outro
        val contemSimilaridade = when {
            nome1Normalizado.contains(nome2Normalizado) || 
            nome2Normalizado.contains(nome1Normalizado) -> 0.9f
            else -> 0f
        }
        
        return max(similaridade, contemSimilaridade)
    }
    
    /**
     * Compara duas datas
     */
    private fun compararDatas(data1: java.util.Date?, data2: java.util.Date?): Float {
        if (data1 == null || data2 == null) {
            return 0f
        }
        
        // Se datas são iguais
        if (data1 == data2) {
            return 1.0f
        }
        
        // Comparar diferença em dias (tolerância de 1 ano = 365 dias)
        val diffDias = abs(data1.time - data2.time) / (1000L * 60 * 60 * 24)
        
        return when {
            diffDias == 0L -> 1.0f
            diffDias <= 30L -> 0.95f // Mesmo mês
            diffDias <= 365L -> 0.8f // Mesmo ano
            diffDias <= 365L * 2 -> 0.5f // 2 anos
            else -> 0f
        }
    }
    
    /**
     * Compara pais de duas pessoas
     */
    private fun compararPais(pessoa1: Pessoa, pessoa2: Pessoa): Float {
        var matches = 0
        var total = 0
        
        // Comparar pai
        when {
            pessoa1.pai != null && pessoa2.pai != null -> {
                total++
                if (pessoa1.pai == pessoa2.pai) {
                    matches++
                }
            }
            pessoa1.pai == null && pessoa2.pai == null -> {
                total++
                matches++ // Ambos não têm pai (conta como match parcial)
            }
        }
        
        // Comparar mãe
        when {
            pessoa1.mae != null && pessoa2.mae != null -> {
                total++
                if (pessoa1.mae == pessoa2.mae) {
                    matches++
                }
            }
            pessoa1.mae == null && pessoa2.mae == null -> {
                total++
                matches++ // Ambos não têm mãe (conta como match parcial)
            }
        }
        
        return if (total > 0) matches.toFloat() / total else 0f
    }
    
    /**
     * Compara locais de nascimento
     */
    private fun compararLocais(local1: String?, local2: String?): Float {
        if (local1.isNullOrBlank() || local2.isNullOrBlank()) {
            return 0f
        }
        
        val local1Normalizado = normalizarTexto(local1.lowercase())
        val local2Normalizado = normalizarTexto(local2.lowercase())
        
        if (local1Normalizado == local2Normalizado) {
            return 1.0f
        }
        
        // Verificar se um contém o outro
        return when {
            local1Normalizado.contains(local2Normalizado) || 
            local2Normalizado.contains(local1Normalizado) -> 0.8f
            else -> compararNomes(local1Normalizado, local2Normalizado)
        }
    }
    
    /**
     * Normaliza texto removendo acentos e caracteres especiais
     */
    private fun normalizarTexto(texto: String): String {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
            .replace(Regex("[^a-z0-9\\s]"), "")
            .trim()
    }
    
    /**
     * Calcula distância de Levenshtein entre duas strings
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val len1 = str1.length
        val len2 = str2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) {
            for (j in 0..len2) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    str1[i - 1] == str2[j - 1] -> dp[i][j] = dp[i - 1][j - 1]
                    else -> dp[i][j] = 1 + minOf(
                        dp[i - 1][j],      // Deletar
                        dp[i][j - 1],      // Inserir
                        dp[i - 1][j - 1]   // Substituir
                    )
                }
            }
        }
        
        return dp[len1][len2]
    }
}

