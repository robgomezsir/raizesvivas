package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.Pessoa
import timber.log.Timber

/**
 * Tipo de operação matemática para cálculo de geração
 */
private enum class TipoOperacao {
    SUBTRACAO,  // distancia2 - distancia1
    ADICAO      // distancia1 - distancia2
}

/**
 * Interface para telemetria de validações
 * Permite registrar métricas quando validações falham
 */
private interface ValidacaoTelemetria {
    /**
     * Registra uma falha de validação
     *
     * @param tipoValidacao Tipo da validação que falhou
     * @param contexto Contexto adicional da validação
     * @param valores Valores que causaram a falha
     */
    fun registrarFalhaValidacao(
        tipoValidacao: String,
        contexto: String,
        valores: Map<String, Any>
    )
}

/**
 * Implementação padrão de telemetria que usa logging
 */
private object LoggingTelemetria : ValidacaoTelemetria {
    override fun registrarFalhaValidacao(
        tipoValidacao: String,
        contexto: String,
        valores: Map<String, Any>
    ) {
        val valoresStr = valores.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Timber.e("📊 Métrica de validação: tipo=$tipoValidacao, contexto=$contexto, valores={$valoresStr}")
    }
}

/**
 * Configuração de validações para o calculador de parentesco
 */
private data class ConfiguracaoValidacao(
    val habilitarLogging: Boolean = true,
    val habilitarTelemetria: Boolean = true,
    val telemetria: ValidacaoTelemetria = LoggingTelemetria
)

/**
 * Calculadora de parentesco entre pessoas na árvore genealógica
 *
 * Versão robusta com suporte para parentes consanguíneos e por afinidade,
 * incluindo diferenciação por gênero e cache de ancestrais.
 */
object ParentescoCalculator {
    
    /**
     * Configuração de validações (pode ser customizada no futuro)
     */
    private val configuracaoValidacao = ConfiguracaoValidacao()

    /**
     * Resultado do cálculo de parentesco
     */
    data class ResultadoParentesco(
        val parentesco: String,
        val grau: Int,
        val distancia: Int,
        val tipoRelacao: TipoRelacao = TipoRelacao.CONSANGUINEO
    )

    enum class TipoRelacao {
        CONSANGUINEO,
        AFINIDADE,
        DESCONHECIDO
    }

    private val cacheAncestrais = mutableMapOf<String, Map<String, Int>>()

    fun limparCache() {
        cacheAncestrais.clear()
    }

    fun calcularParentesco(
        pessoa1: Pessoa,
        pessoa2: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): ResultadoParentesco {
        if (pessoa1.id == pessoa2.id) {
            return ResultadoParentesco("Você mesmo(a)", 0, 0)
        }

        val consanguineo = calcularParentescoConsanguineo(pessoa1, pessoa2, pessoasMap)
        if (consanguineo.grau >= 0) {
            return consanguineo
        }

        val afinidade = calcularParentescoPorAfinidade(pessoa1, pessoa2, pessoasMap)
        if (afinidade.grau >= 0) {
            return afinidade
        }

        return ResultadoParentesco("Sem parentesco conhecido", -1, -1, TipoRelacao.DESCONHECIDO)
    }

    private fun calcularParentescoConsanguineo(
        pessoa1: Pessoa,
        pessoa2: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): ResultadoParentesco {
        val temPai1 = pessoa1.pai?.isNotBlank() == true
        val temMae1 = pessoa1.mae?.isNotBlank() == true
        val temPai2 = pessoa2.pai?.isNotBlank() == true
        val temMae2 = pessoa2.mae?.isNotBlank() == true

        if (!temPai1 && !temMae1 && !temPai2 && !temMae2) {
            return ResultadoParentesco("Sem parentesco conhecido", -1, -1, TipoRelacao.DESCONHECIDO)
        }

        val ancestralComum = encontrarAncestralComumMaisProximo(pessoa1, pessoa2, pessoasMap)
            ?: return ResultadoParentesco("Sem parentesco conhecido", -1, -1, TipoRelacao.DESCONHECIDO)

        val distancia1 = calcularDistancia(pessoa1, ancestralComum.id, pessoasMap)
        val distancia2 = calcularDistancia(pessoa2, ancestralComum.id, pessoasMap)

        if (distancia1 < 0 || distancia2 < 0) {
            return ResultadoParentesco("Sem parentesco conhecido", -1, -1, TipoRelacao.DESCONHECIDO)
        }

        val distanciaTotal = distancia1 + distancia2
        val parentesco = determinarTipoParentesco(
            distancia1,
            distancia2,
            pessoa1,
            pessoa2
        )

        return ResultadoParentesco(parentesco, distanciaTotal, distanciaTotal, TipoRelacao.CONSANGUINEO)
    }

    private fun calcularParentescoPorAfinidade(
        pessoa1: Pessoa,
        pessoa2: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): ResultadoParentesco {
        if (pessoa1.conjugeAtual == pessoa2.id || pessoa2.conjugeAtual == pessoa1.id) {
            val genero2 = pessoa2.genero
            val descricao = when {
                ehMasculino(genero2) -> "Marido"
                ehFeminino(genero2) -> "Esposa"
                else -> "Cônjuge"
            }
            return ResultadoParentesco(descricao, 1, 1, TipoRelacao.AFINIDADE)
        }

        val conjugePessoa1 = pessoa1.conjugeAtual?.let { pessoasMap[it] }
        if (conjugePessoa1 != null) {
            if (pessoa2.id == conjugePessoa1.pai) {
                return ResultadoParentesco("Sogro", 2, 2, TipoRelacao.AFINIDADE)
            }
            if (pessoa2.id == conjugePessoa1.mae) {
                return ResultadoParentesco("Sogra", 2, 2, TipoRelacao.AFINIDADE)
            }

            val saoIrmaos = (conjugePessoa1.pai != null && conjugePessoa1.pai == pessoa2.pai) ||
                (conjugePessoa1.mae != null && conjugePessoa1.mae == pessoa2.mae)

            if (saoIrmaos && conjugePessoa1.id != pessoa2.id) {
                val genero2 = pessoa2.genero
                val descricao = when {
                    ehMasculino(genero2) -> "Cunhado"
                    ehFeminino(genero2) -> "Cunhada"
                    else -> "Cunhado(a)"
                }
                return ResultadoParentesco(descricao, 2, 2, TipoRelacao.AFINIDADE)
            }
        }

        val conjugePessoa2 = pessoa2.conjugeAtual?.let { pessoasMap[it] }
        if (conjugePessoa2 != null) {
            if (conjugePessoa2.pai == pessoa1.id || conjugePessoa2.mae == pessoa1.id) {
                val genero2 = pessoa2.genero
                val descricao = when {
                    ehMasculino(genero2) -> "Genro"
                    ehFeminino(genero2) -> "Nora"
                    else -> "Genro/Nora"
                }
                return ResultadoParentesco(descricao, 2, 2, TipoRelacao.AFINIDADE)
            }
        }

        return ResultadoParentesco("Sem parentesco conhecido", -1, -1, TipoRelacao.DESCONHECIDO)
    }

    private fun encontrarAncestralComumMaisProximo(
        pessoa1: Pessoa,
        pessoa2: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): Pessoa? {
        val ancestrais1 = coletarAncestraisComCache(pessoa1, pessoasMap)
        val ancestrais2 = coletarAncestraisComCache(pessoa2, pessoasMap)

        var ancestralComum: Pessoa? = null
        var menorDistancia = Int.MAX_VALUE

        ancestrais1.forEach { (ancestralId, distancia1) ->
            ancestrais2[ancestralId]?.let { distancia2 ->
                val distanciaTotal = distancia1 + distancia2
                if (distanciaTotal < menorDistancia) {
                    menorDistancia = distanciaTotal
                    ancestralComum = pessoasMap[ancestralId]
                }
            }
        }

        return ancestralComum
    }

    private fun coletarAncestraisComCache(
        pessoa: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): Map<String, Int> {
        return cacheAncestrais.getOrPut(pessoa.id) {
            coletarAncestrais(pessoa, pessoasMap)
        }
    }

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

            p.pai?.takeIf { it.isNotBlank() }?.let { paiId ->
                pessoasMap[paiId]?.let { pai ->
                    coletarRecursivo(pai, distancia + 1)
                }
            }

            p.mae?.takeIf { it.isNotBlank() }?.let { maeId ->
                pessoasMap[maeId]?.let { mae ->
                    coletarRecursivo(mae, distancia + 1)
                }
            }
        }

        coletarRecursivo(pessoa, 0)
        return ancestrais
    }

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

            p.pai?.takeIf { it.isNotBlank() }?.let { paiId ->
                pessoasMap[paiId]?.let { pai ->
                    buscarRecursivo(pai, distancia + 1)?.let { return it }
                }
            }

            p.mae?.takeIf { it.isNotBlank() }?.let { maeId ->
                pessoasMap[maeId]?.let { mae ->
                    buscarRecursivo(mae, distancia + 1)?.let { return it }
                }
            }

            return null
        }

        return buscarRecursivo(pessoa, 0) ?: -1
    }

    private fun determinarTipoParentesco(
        distancia1: Int,
        distancia2: Int,
        pessoa1: Pessoa,
        pessoa2: Pessoa
    ): String {
        return when {
            // Se pessoa1 é o ancestral comum (distancia1 == 0), então pessoa2 é descendente de pessoa1
            distancia1 == 0 && distancia2 > 0 -> {
                determinarDescendente(distancia2, pessoa1, pessoa2)
            }

            // Se pessoa2 é o ancestral comum (distancia2 == 0), então pessoa1 é descendente de pessoa2
            // Logo, pessoa2 é ascendente de pessoa1
            distancia1 > 0 && distancia2 == 0 -> {
                determinarAscendente(distancia1, pessoa1, pessoa2)
            }

            distancia1 > 0 && distancia2 > 0 -> {
                determinarColateral(distancia1, distancia2, pessoa1, pessoa2)
            }

            else -> "Parentesco desconhecido"
        }
    }

    private fun determinarAscendente(
        geracao: Int,
        pessoa: Pessoa,
        ancestral: Pessoa
    ): String {
        val genero = ancestral.genero

        return when (geracao) {
            1 -> when {
                ancestral.id == pessoa.pai -> if (ehFeminino(genero)) "Mãe" else "Pai"
                ancestral.id == pessoa.mae -> if (ehMasculino(genero)) "Pai" else "Mãe"
                ehMasculino(genero) -> "Pai"
                ehFeminino(genero) -> "Mãe"
                else -> "Pai/Mãe"
            }

            2 -> when {
                ehMasculino(genero) -> "Avô"
                ehFeminino(genero) -> "Avó"
                else -> "Avô/Avó"
            }

            3 -> when {
                ehMasculino(genero) -> "Bisavô"
                ehFeminino(genero) -> "Bisavó"
                else -> "Bisavô/Bisavó"
            }

            4 -> when {
                ehMasculino(genero) -> "Trisavô"
                ehFeminino(genero) -> "Trisavó"
                else -> "Trisavô/Trisavó"
            }

            else -> "Ancestral ($geracao gerações)"
        }
    }

    @Suppress("unused")
    private fun determinarDescendente(
        geracao: Int,
        @Suppress("UNUSED_PARAMETER") pessoa: Pessoa,
        descendente: Pessoa
    ): String {
        val genero = descendente.genero

        return when (geracao) {
            1 -> when {
                ehMasculino(genero) -> "Filho"
                ehFeminino(genero) -> "Filha"
                else -> "Filho(a)"
            }

            2 -> when {
                ehMasculino(genero) -> "Neto"
                ehFeminino(genero) -> "Neta"
                else -> "Neto(a)"
            }

            3 -> when {
                ehMasculino(genero) -> "Bisneto"
                ehFeminino(genero) -> "Bisneta"
                else -> "Bisneto(a)"
            }

            4 -> when {
                ehMasculino(genero) -> "Trisneto"
                ehFeminino(genero) -> "Trisneta"
                else -> "Trisneto(a)"
            }

            else -> "Descendente ($geracao gerações)"
        }
    }

    /**
     * Valida que uma distância é positiva
     *
     * @param distancia Distância a validar
     * @param nomeParametro Nome do parâmetro para mensagens de erro
     * @throws IllegalArgumentException se a distância não for positiva
     */
    private fun validarDistanciaPositiva(distancia: Int, nomeParametro: String) {
        if (distancia <= 0) {
            val mensagem = "$nomeParametro deve ser maior que 0, recebido: $distancia"
            
            if (configuracaoValidacao.habilitarLogging) {
                Timber.e("❌ Validação falhou: $mensagem")
            }
            
            if (configuracaoValidacao.habilitarTelemetria) {
                configuracaoValidacao.telemetria.registrarFalhaValidacao(
                    tipoValidacao = "DISTANCIA_POSITIVA",
                    contexto = nomeParametro,
                    valores = mapOf(
                        "distancia" to distancia,
                        "nomeParametro" to nomeParametro
                    )
                )
            }
            
            throw IllegalArgumentException(mensagem)
        }
    }

    /**
     * Valida que distancia1 é menor que distancia2
     *
     * @param distancia1 Primeira distância
     * @param distancia2 Segunda distância
     * @param contexto Contexto da validação para mensagens de erro
     * @throws IllegalArgumentException se distancia1 >= distancia2
     */
    private fun validarDistanciaMenor(
        distancia1: Int,
        distancia2: Int,
        contexto: String
    ) {
        if (distancia1 >= distancia2) {
            val mensagem = "distancia1 ($distancia1) deve ser menor que distancia2 ($distancia2) para $contexto"
            
            if (configuracaoValidacao.habilitarLogging) {
                Timber.e("❌ Validação falhou: $mensagem")
            }
            
            if (configuracaoValidacao.habilitarTelemetria) {
                configuracaoValidacao.telemetria.registrarFalhaValidacao(
                    tipoValidacao = "DISTANCIA_MENOR",
                    contexto = contexto,
                    valores = mapOf(
                        "distancia1" to distancia1,
                        "distancia2" to distancia2,
                        "contexto" to contexto
                    )
                )
            }
            
            throw IllegalArgumentException(mensagem)
        }
    }

    /**
     * Valida que distancia1 é maior que distancia2
     *
     * @param distancia1 Primeira distância
     * @param distancia2 Segunda distância
     * @param contexto Contexto da validação para mensagens de erro
     * @throws IllegalArgumentException se distancia1 <= distancia2
     */
    private fun validarDistanciaMaior(
        distancia1: Int,
        distancia2: Int,
        contexto: String
    ) {
        if (distancia1 <= distancia2) {
            val mensagem = "distancia1 ($distancia1) deve ser maior que distancia2 ($distancia2) para $contexto"
            
            if (configuracaoValidacao.habilitarLogging) {
                Timber.e("❌ Validação falhou: $mensagem")
            }
            
            if (configuracaoValidacao.habilitarTelemetria) {
                configuracaoValidacao.telemetria.registrarFalhaValidacao(
                    tipoValidacao = "DISTANCIA_MAIOR",
                    contexto = contexto,
                    valores = mapOf(
                        "distancia1" to distancia1,
                        "distancia2" to distancia2,
                        "contexto" to contexto
                    )
                )
            }
            
            throw IllegalArgumentException(mensagem)
        }
    }

    /**
     * Valida que o cálculo de geração está correto
     *
     * @param geracao Valor calculado da geração
     * @param distancia1 Primeira distância
     * @param distancia2 Segunda distância
     * @param operacao Operação esperada
     * @param contexto Contexto da validação para mensagens de erro
     * @throws IllegalArgumentException se o cálculo estiver incorreto
     */
    private fun validarCalculoGeracao(
        geracao: Int,
        distancia1: Int,
        distancia2: Int,
        operacao: TipoOperacao,
        contexto: String
    ) {
        val esperado = when (operacao) {
            TipoOperacao.SUBTRACAO -> distancia2 - distancia1
            TipoOperacao.ADICAO -> distancia1 - distancia2
        }

        if (geracao != esperado) {
            val operacaoTexto = when (operacao) {
                TipoOperacao.SUBTRACAO -> "distancia2 ($distancia2) - distancia1 ($distancia1)"
                TipoOperacao.ADICAO -> "distancia1 ($distancia1) - distancia2 ($distancia2)"
            }
            val mensagem = "$contexto: geracao ($geracao) deve ser igual a $operacaoTexto = $esperado"
            
            if (configuracaoValidacao.habilitarLogging) {
                Timber.e("❌ Validação falhou: $mensagem")
            }
            
            if (configuracaoValidacao.habilitarTelemetria) {
                configuracaoValidacao.telemetria.registrarFalhaValidacao(
                    tipoValidacao = "CALCULO_GERACAO",
                    contexto = contexto,
                    valores = mapOf(
                        "geracao" to geracao,
                        "distancia1" to distancia1,
                        "distancia2" to distancia2,
                        "operacao" to operacao.name,
                        "esperado" to esperado
                    )
                )
            }
            
            throw IllegalArgumentException(mensagem)
        }
    }

    /**
     * Representa os diferentes tipos de parentesco colateral baseado nas distâncias ao ancestral comum
     */
    private sealed class TipoParentescoColateral {
        /**
         * Mesma geração - primos (distancia1 == distancia2)
         * @param distancia Distância comum ao ancestral comum
         */
        data class MesmaGeracao(val distancia: Int) : TipoParentescoColateral()

        /**
         * Pessoa2 é descendente colateral de pessoa1 (distancia1 < distancia2)
         * @param distanciaPessoa1 Distância de pessoa1 ao ancestral comum
         * @param distanciaPessoa2 Distância de pessoa2 ao ancestral comum
         * @param geracaoDescendente Diferença de gerações (distanciaPessoa2 - distanciaPessoa1)
         */
        data class DescendenteColateral(
            val distanciaPessoa1: Int,
            val distanciaPessoa2: Int,
            val geracaoDescendente: Int
        ) : TipoParentescoColateral() {
            init {
                // Usar validações centralizadas com logging
                validarDistanciaPositiva(distanciaPessoa1, "distanciaPessoa1")
                validarDistanciaPositiva(distanciaPessoa2, "distanciaPessoa2")
                validarDistanciaMenor(
                    distanciaPessoa1,
                    distanciaPessoa2,
                    "descendente colateral"
                )
                validarCalculoGeracao(
                    geracaoDescendente,
                    distanciaPessoa1,
                    distanciaPessoa2,
                    TipoOperacao.SUBTRACAO,
                    "DescendenteColateral"
                )
            }
        }

        /**
         * Pessoa2 é ascendente colateral de pessoa1 (distancia1 > distancia2)
         * @param distanciaPessoa1 Distância de pessoa1 ao ancestral comum
         * @param distanciaPessoa2 Distância de pessoa2 ao ancestral comum
         * @param geracaoAscendente Diferença de gerações (distanciaPessoa1 - distanciaPessoa2)
         */
        data class AscendenteColateral(
            val distanciaPessoa1: Int,
            val distanciaPessoa2: Int,
            val geracaoAscendente: Int
        ) : TipoParentescoColateral() {
            init {
                // Usar validações centralizadas com logging
                validarDistanciaPositiva(distanciaPessoa1, "distanciaPessoa1")
                validarDistanciaPositiva(distanciaPessoa2, "distanciaPessoa2")
                validarDistanciaMaior(
                    distanciaPessoa1,
                    distanciaPessoa2,
                    "ascendente colateral"
                )
                validarCalculoGeracao(
                    geracaoAscendente,
                    distanciaPessoa1,
                    distanciaPessoa2,
                    TipoOperacao.ADICAO,
                    "AscendenteColateral"
                )
            }
        }
    }

    /**
     * Determina o tipo de parentesco colateral baseado nas distâncias ao ancestral comum
     *
     * @param distancia1 Distância de pessoa1 ao ancestral comum (deve ser >= 1)
     * @param distancia2 Distância de pessoa2 ao ancestral comum (deve ser >= 1)
     * @return Tipo de parentesco colateral
     * @throws IllegalArgumentException se as distâncias forem inválidas
     */
    private fun determinarTipoColateral(
        distancia1: Int,
        distancia2: Int
    ): TipoParentescoColateral {
        // Usar validações centralizadas com logging
        validarDistanciaPositiva(distancia1, "distancia1")
        validarDistanciaPositiva(distancia2, "distancia2")

        return when {
            distancia1 == distancia2 -> TipoParentescoColateral.MesmaGeracao(distancia1)
            distancia1 < distancia2 -> TipoParentescoColateral.DescendenteColateral(
                distanciaPessoa1 = distancia1,
                distanciaPessoa2 = distancia2,
                geracaoDescendente = distancia2 - distancia1
            )
            distancia1 > distancia2 -> TipoParentescoColateral.AscendenteColateral(
                distanciaPessoa1 = distancia1,
                distanciaPessoa2 = distancia2,
                geracaoAscendente = distancia1 - distancia2
            )
            else -> {
                // Este caso não deveria acontecer devido às validações anteriores
                val erro = "Erro inesperado: distâncias inválidas (distancia1=$distancia1, distancia2=$distancia2)"
                Timber.e("❌ $erro")
                throw IllegalStateException(erro)
            }
        }
    }

    /**
     * Determina o parentesco quando pessoa2 é descendente colateral de pessoa1
     * (pessoa1 está mais próximo do ancestral comum)
     *
     * @param tipoColateral Tipo de parentesco colateral (deve ser DescendenteColateral)
     * @param genero2 Gênero de pessoa2
     * @return String com o parentesco
     * @throws IllegalArgumentException se tipoColateral não for DescendenteColateral
     */
    private fun determinarDescendenteColateral(
        tipoColateral: TipoParentescoColateral.DescendenteColateral,
        genero2: Genero?
    ): String {
        val distanciaPessoa1 = tipoColateral.distanciaPessoa1
        val geracaoDescendente = tipoColateral.geracaoDescendente

        return when {
            distanciaPessoa1 == 1 && geracaoDescendente == 1 -> when {
                ehMasculino(genero2) -> "Sobrinho"
                ehFeminino(genero2) -> "Sobrinha"
                else -> "Sobrinho(a)"
            }
            distanciaPessoa1 == 1 && geracaoDescendente == 2 -> when {
                ehMasculino(genero2) -> "Sobrinho-neto"
                ehFeminino(genero2) -> "Sobrinha-neta"
                else -> "Sobrinho(a)-neto(a)"
            }
            else -> {
                "Descendente colateral ($geracaoDescendente gerações)"
            }
        }
    }

    /**
     * Determina o parentesco quando pessoa2 é ascendente colateral de pessoa1
     * (pessoa2 está mais próximo do ancestral comum)
     *
     * @param tipoColateral Tipo de parentesco colateral (deve ser AscendenteColateral)
     * @param genero2 Gênero de pessoa2
     * @return String com o parentesco
     * @throws IllegalArgumentException se tipoColateral não for AscendenteColateral
     */
    private fun determinarAscendenteColateral(
        tipoColateral: TipoParentescoColateral.AscendenteColateral,
        genero2: Genero?
    ): String {
        val distanciaPessoa2 = tipoColateral.distanciaPessoa2
        val geracaoAscendente = tipoColateral.geracaoAscendente

        return when {
            distanciaPessoa2 == 1 && geracaoAscendente == 1 -> when {
                ehMasculino(genero2) -> "Tio"
                ehFeminino(genero2) -> "Tia"
                else -> "Tio/Tia"
            }
            distanciaPessoa2 == 1 && geracaoAscendente == 2 -> when {
                ehMasculino(genero2) -> "Tio-avô"
                ehFeminino(genero2) -> "Tia-avó"
                else -> "Tio-avô/Tia-avó"
            }
            else -> {
                "Ascendente colateral ($geracaoAscendente gerações)"
            }
        }
    }

    /**
     * Determina o parentesco colateral entre duas pessoas
     *
     * @param distancia1 Distância de pessoa1 ao ancestral comum (deve ser >= 1)
     * @param distancia2 Distância de pessoa2 ao ancestral comum (deve ser >= 1)
     * @param pessoa1 Primeira pessoa
     * @param pessoa2 Segunda pessoa
     * @return String com o parentesco colateral
     * @throws IllegalArgumentException se as distâncias forem inválidas
     */
    private fun determinarColateral(
        distancia1: Int,
        distancia2: Int,
        pessoa1: Pessoa,
        pessoa2: Pessoa
    ): String {
        // Usar validações centralizadas com logging
        validarDistanciaPositiva(distancia1, "distancia1")
        validarDistanciaPositiva(distancia2, "distancia2")

        val genero2 = pessoa2.genero
        val tipoColateral = determinarTipoColateral(distancia1, distancia2)

        return when {
            // Caso especial: irmãos (mesma geração, distância 1)
            distancia1 == 1 && distancia2 == 1 -> {
                val temMesmoPai = pessoa1.pai != null && pessoa1.pai == pessoa2.pai
                val temMesmaMae = pessoa1.mae != null && pessoa1.mae == pessoa2.mae

                when {
                    temMesmoPai && temMesmaMae -> when {
                        ehMasculino(genero2) -> "Irmão"
                        ehFeminino(genero2) -> "Irmã"
                        else -> "Irmão(ã)"
                    }

                    temMesmoPai || temMesmaMae -> when {
                        ehMasculino(genero2) -> "Meio-irmão"
                        ehFeminino(genero2) -> "Meia-irmã"
                        else -> "Meio-irmão(ã)"
                    }

                    else -> "Parente colateral"
                }
            }

            // Caso especial: primos (mesma geração, distância >= 2)
            tipoColateral is TipoParentescoColateral.MesmaGeracao -> {
                val distancia = tipoColateral.distancia
                when (distancia) {
                    2 -> when {
                        ehMasculino(genero2) -> "Primo"
                        ehFeminino(genero2) -> "Prima"
                        else -> "Primo(a)"
                    }
                    else -> {
                        val grauPrimo = distancia - 1
                        when {
                            ehMasculino(genero2) -> "Primo de ${grauPrimo}º grau"
                            ehFeminino(genero2) -> "Prima de ${grauPrimo}º grau"
                            else -> "Primo(a) de ${grauPrimo}º grau"
                        }
                    }
                }
            }

            // Pessoa2 é descendente colateral de pessoa1
            tipoColateral is TipoParentescoColateral.DescendenteColateral -> {
                determinarDescendenteColateral(tipoColateral, genero2)
            }

            // Pessoa2 é ascendente colateral de pessoa1
            tipoColateral is TipoParentescoColateral.AscendenteColateral -> {
                determinarAscendenteColateral(tipoColateral, genero2)
            }

            // Caso genérico (não deveria acontecer, mas mantido para segurança)
            else -> {
                val grauTotal = distancia1 + distancia2
                "Parente colateral ($grauTotal° grau)"
            }
        }
    }

    fun calcularTodosParentescos(
        pessoaReferencia: Pessoa,
        todasPessoas: List<Pessoa>,
        pessoasMap: Map<String, Pessoa>
    ): List<Pair<Pessoa, ResultadoParentesco>> {
        return todasPessoas
            .filter { it.id != pessoaReferencia.id && it.id.isNotBlank() }
            .map { pessoa ->
                val parentesco = calcularParentesco(pessoaReferencia, pessoa, pessoasMap)
                Pair(pessoa, parentesco)
            }
            .filter { it.second.grau >= 0 }
            .sortedWith(
                compareBy<Pair<Pessoa, ResultadoParentesco>> { it.second.grau }
                    .thenBy { it.first.nome }
            )
    }

    @Suppress("unused")
    fun agruparParentesPorTipo(
        pessoaReferencia: Pessoa,
        todasPessoas: List<Pessoa>,
        pessoasMap: Map<String, Pessoa>
    ): Map<String, List<Pair<Pessoa, ResultadoParentesco>>> {
        val todosParentescos = calcularTodosParentescos(pessoaReferencia, todasPessoas, pessoasMap)

        return todosParentescos.groupBy { it.second.parentesco }
            .mapValues { (_, lista) ->
                lista.sortedBy { it.first.nome }
            }
    }

    private fun ehMasculino(genero: Genero?) = genero == Genero.MASCULINO
    private fun ehFeminino(genero: Genero?) = genero == Genero.FEMININO
}