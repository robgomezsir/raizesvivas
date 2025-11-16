package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.utils.DuplicateDetector
import timber.log.Timber
import java.text.Normalizer
import java.util.Date
import javax.inject.Inject
import kotlin.math.abs

/**
 * Use case para validar duplicatas antes de salvar uma pessoa
 * 
 * Implementa validação em múltiplos níveis:
 * - Nível 1 (CRÍTICO): Duplicata exata - Nome completo + Data de nascimento idênticos
 * - Nível 2 (ALTO): Duplicata provável - Nome muito similar + Data próxima + Mesmos pais
 * - Nível 3 (MÉDIO): Duplicata possível - Score de similaridade alto
 */
class ValidarDuplicataUseCase @Inject constructor(
    private val pessoaRepository: PessoaRepository
) {
    
    /**
     * Resultado da validação de duplicata
     */
    data class ResultadoValidacao(
        val temDuplicata: Boolean,
        val nivel: NivelDuplicata? = null,
        val duplicatasEncontradas: List<DuplicataDetalhada> = emptyList(),
        val mensagem: String? = null
    ) {
        /**
         * Indica se deve bloquear o cadastro
         */
        val deveBloquear: Boolean
            get() = nivel == NivelDuplicata.CRITICO
        
        /**
         * Indica se deve mostrar aviso ao usuário
         */
        val deveAvisar: Boolean
            get() = nivel == NivelDuplicata.ALTO || nivel == NivelDuplicata.MEDIO
    }
    
    /**
     * Níveis de duplicata
     */
    enum class NivelDuplicata {
        CRITICO,    // Duplicata exata - BLOQUEIA cadastro
        ALTO,       // Duplicata provável - AVISA e pede confirmação
        MEDIO       // Duplicata possível - AVISA mas permite
    }
    
    /**
     * Detalhes de uma duplicata encontrada
     */
    data class DuplicataDetalhada(
        val pessoa: Pessoa,
        val nivel: NivelDuplicata,
        val razoes: List<String>,
        val scoreSimilaridade: Float
    )
    
    /**
     * Valida se uma pessoa é duplicata antes de salvar
     * 
     * @param pessoa Pessoa a ser validada
     * @param toleranciaDias Tolerância em dias para data de nascimento (padrão: 0 = exato)
     * @return Resultado da validação
     */
    suspend fun validar(
        pessoa: Pessoa,
        toleranciaDias: Int = 0
    ): ResultadoValidacao {
        return try {
            // Buscar todas as pessoas (exceto a atual se estiver editando)
            val todasPessoas = pessoaRepository.buscarTodas()
                .filter { it.id != pessoa.id }
            
            if (todasPessoas.isEmpty()) {
                return ResultadoValidacao(
                    temDuplicata = false,
                    mensagem = null
                )
            }
            
            val duplicatasEncontradas = mutableListOf<DuplicataDetalhada>()
            
            // Nível 1: Duplicata CRÍTICA (exata) - Nome completo + Data nascimento idênticos
            val duplicatasCriticas = encontrarDuplicatasCriticas(pessoa, todasPessoas, toleranciaDias)
            if (duplicatasCriticas.isNotEmpty()) {
                duplicatasEncontradas.addAll(duplicatasCriticas)
                return ResultadoValidacao(
                    temDuplicata = true,
                    nivel = NivelDuplicata.CRITICO,
                    duplicatasEncontradas = duplicatasCriticas,
                    mensagem = "Já existe uma pessoa cadastrada com o mesmo nome completo e data de nascimento. " +
                            "Por favor, verifique se não é a mesma pessoa."
                )
            }
            
            // Nível 2: Duplicata ALTA (provável) - Nome muito similar + Data próxima + Mesmos pais
            val duplicatasAltas = encontrarDuplicatasAltas(pessoa, todasPessoas, toleranciaDias)
            if (duplicatasAltas.isNotEmpty()) {
                duplicatasEncontradas.addAll(duplicatasAltas)
                return ResultadoValidacao(
                    temDuplicata = true,
                    nivel = NivelDuplicata.ALTO,
                    duplicatasEncontradas = duplicatasAltas,
                    mensagem = "Foram encontradas pessoas muito similares. " +
                            "Por favor, confirme se não são duplicatas antes de continuar."
                )
            }
            
            // Nível 3: Duplicata MÉDIA (possível) - Score de similaridade alto
            val duplicatasMedias = encontrarDuplicatasMedias(pessoa, todasPessoas)
            if (duplicatasMedias.isNotEmpty()) {
                duplicatasEncontradas.addAll(duplicatasMedias)
                return ResultadoValidacao(
                    temDuplicata = true,
                    nivel = NivelDuplicata.MEDIO,
                    duplicatasEncontradas = duplicatasMedias,
                    mensagem = "Foram encontradas pessoas com características similares. " +
                            "Revise antes de continuar."
                )
            }
            
            ResultadoValidacao(
                temDuplicata = false,
                mensagem = null
            )
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao validar duplicatas")
            // Em caso de erro, não bloquear o cadastro mas logar o erro
            ResultadoValidacao(
                temDuplicata = false,
                mensagem = null
            )
        }
    }
    
    /**
     * Encontra duplicatas críticas (exatas)
     */
    private fun encontrarDuplicatasCriticas(
        pessoa: Pessoa,
        outrasPessoas: List<Pessoa>,
        toleranciaDias: Int
    ): List<DuplicataDetalhada> {
        val duplicatas = mutableListOf<DuplicataDetalhada>()
        val nomeNormalizado = normalizarNome(pessoa.nome)
        
        outrasPessoas.forEach { outra ->
            val nomeOutraNormalizado = normalizarNome(outra.nome)
            
            // Verificar se nomes são idênticos (normalizados)
            val nomesIdenticos = nomeNormalizado == nomeOutraNormalizado
            
            // Verificar se datas de nascimento são iguais ou muito próximas
            val datasIguais = when {
                pessoa.dataNascimento == null && outra.dataNascimento == null -> true
                pessoa.dataNascimento == null || outra.dataNascimento == null -> false
                else -> {
                    val diffDias = abs(pessoa.dataNascimento.time - outra.dataNascimento.time) / (1000L * 60 * 60 * 24)
                    diffDias <= toleranciaDias
                }
            }
            
            if (nomesIdenticos && datasIguais) {
                val razoes = mutableListOf<String>()
                razoes.add("Nome completo idêntico")
                razoes.add("Data de nascimento ${if (toleranciaDias == 0) "idêntica" else "muito próxima"}")
                
                // Adicionar informações adicionais se disponíveis
                if (pessoa.pai != null && outra.pai != null && pessoa.pai == outra.pai) {
                    razoes.add("Mesmo pai")
                }
                if (pessoa.mae != null && outra.mae != null && pessoa.mae == outra.mae) {
                    razoes.add("Mesma mãe")
                }
                if (pessoa.localNascimento != null && outra.localNascimento != null &&
                    normalizarTexto(pessoa.localNascimento) == normalizarTexto(outra.localNascimento)) {
                    razoes.add("Mesmo local de nascimento")
                }
                
                duplicatas.add(
                    DuplicataDetalhada(
                        pessoa = outra,
                        nivel = NivelDuplicata.CRITICO,
                        razoes = razoes,
                        scoreSimilaridade = 1.0f
                    )
                )
            }
        }
        
        return duplicatas
    }
    
    /**
     * Encontra duplicatas altas (prováveis)
     */
    private fun encontrarDuplicatasAltas(
        pessoa: Pessoa,
        outrasPessoas: List<Pessoa>,
        toleranciaDias: Int
    ): List<DuplicataDetalhada> {
        val duplicatas = mutableListOf<DuplicataDetalhada>()
        val toleranciaDiasExpandida = maxOf(toleranciaDias, 365) // Tolerância de 1 ano para nível alto
        
        outrasPessoas.forEach { outra ->
            val razoes = mutableListOf<String>()
            var score = 0f
            var peso = 0f
            
            // Nome muito similar (>= 90%)
            val nomeSimilaridade = compararNomes(pessoa.nome, outra.nome)
            if (nomeSimilaridade >= 0.9f) {
                razoes.add("Nome muito similar (${(nomeSimilaridade * 100).toInt()}%)")
                score += nomeSimilaridade * 0.4f
                peso += 0.4f
            } else {
                return@forEach // Nome não é suficientemente similar
            }
            
            // Data próxima (dentro de 1 ano)
            val dataSimilaridade = compararDatas(pessoa.dataNascimento, outra.dataNascimento, toleranciaDiasExpandida)
            if (dataSimilaridade >= 0.7f) {
                razoes.add("Data de nascimento próxima")
                score += dataSimilaridade * 0.3f
                peso += 0.3f
            }
            
            // Mesmos pais
            val paisSimilaridade = compararPais(pessoa, outra)
            if (paisSimilaridade >= 0.5f) {
                razoes.add("Mesmos pais")
                score += paisSimilaridade * 0.3f
                peso += 0.3f
            }
            
            val scoreFinal = if (peso > 0) score / peso else 0f
            
            // Se score final >= 0.85, considerar duplicata alta
            if (scoreFinal >= 0.85f) {
                duplicatas.add(
                    DuplicataDetalhada(
                        pessoa = outra,
                        nivel = NivelDuplicata.ALTO,
                        razoes = razoes,
                        scoreSimilaridade = scoreFinal
                    )
                )
            }
        }
        
        return duplicatas
    }
    
    /**
     * Encontra duplicatas médias (possíveis) usando o DuplicateDetector existente
     */
    private fun encontrarDuplicatasMedias(
        pessoa: Pessoa,
        outrasPessoas: List<Pessoa>
    ): List<DuplicataDetalhada> {
        val resultados = DuplicateDetector.detectarDuplicatas(
            pessoa = pessoa,
            outrasPessoas = outrasPessoas,
            threshold = 0.75f // Threshold mais baixo para nível médio
        )
        
        return resultados.map { resultado ->
            DuplicataDetalhada(
                pessoa = resultado.pessoa2,
                nivel = NivelDuplicata.MEDIO,
                razoes = resultado.razoes,
                scoreSimilaridade = resultado.scoreSimilaridade
            )
        }
    }
    
    /**
     * Normaliza nome para comparação
     */
    private fun normalizarNome(nome: String): String {
        return normalizarTexto(nome.lowercase().trim())
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
     * Compara dois nomes
     */
    private fun compararNomes(nome1: String, nome2: String): Float {
        if (nome1.isBlank() || nome2.isBlank()) return 0f
        
        val nome1Normalizado = normalizarNome(nome1)
        val nome2Normalizado = normalizarNome(nome2)
        
        if (nome1Normalizado == nome2Normalizado) return 1.0f
        
        // Usar Levenshtein
        val distancia = levenshteinDistance(nome1Normalizado, nome2Normalizado)
        val tamanhoMax = maxOf(nome1Normalizado.length, nome2Normalizado.length)
        
        if (tamanhoMax == 0) return 0f
        
        return 1f - (distancia.toFloat() / tamanhoMax)
    }
    
    /**
     * Compara duas datas com tolerância
     */
    private fun compararDatas(data1: Date?, data2: Date?, toleranciaDias: Int): Float {
        if (data1 == null || data2 == null) return 0f
        
        val diffDias = abs(data1.time - data2.time) / (1000L * 60 * 60 * 24)
        
        return when {
            diffDias == 0L -> 1.0f
            diffDias <= toleranciaDias -> {
                // Quanto mais próximo, maior o score
                1.0f - (diffDias.toFloat() / toleranciaDias.toFloat() * 0.3f)
            }
            else -> 0f
        }
    }
    
    /**
     * Compara pais de duas pessoas
     */
    private fun compararPais(pessoa1: Pessoa, pessoa2: Pessoa): Float {
        var matches = 0
        var total = 0
        
        if (pessoa1.pai != null && pessoa2.pai != null) {
            total++
            if (pessoa1.pai == pessoa2.pai) matches++
        }
        
        if (pessoa1.mae != null && pessoa2.mae != null) {
            total++
            if (pessoa1.mae == pessoa2.mae) matches++
        }
        
        return if (total > 0) matches.toFloat() / total else 0f
    }
    
    /**
     * Calcula distância de Levenshtein
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
                        dp[i - 1][j],
                        dp[i][j - 1],
                        dp[i - 1][j - 1]
                    )
                }
            }
        }
        
        return dp[len1][len2]
    }
}

