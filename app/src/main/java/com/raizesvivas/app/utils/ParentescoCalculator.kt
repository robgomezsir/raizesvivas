package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Genero
import com.raizesvivas.app.domain.model.Pessoa

/**
 * Calculadora de parentesco entre pessoas na árvore genealógica
 *
 * Versão robusta com suporte para parentes consanguíneos e por afinidade,
 * incluindo diferenciação por gênero e cache de ancestrais.
 */
object ParentescoCalculator {

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
            distancia1 == 0 && distancia2 > 0 -> {
                determinarAscendente(distancia2, pessoa1, pessoa2)
            }

            distancia1 > 0 && distancia2 == 0 -> {
                determinarDescendente(distancia1, pessoa1, pessoa2)
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

    private fun determinarDescendente(
        geracao: Int,
        pessoa: Pessoa,
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

    private fun determinarColateral(
        distancia1: Int,
        distancia2: Int,
        pessoa1: Pessoa,
        pessoa2: Pessoa
    ): String {
        val genero2 = pessoa2.genero

        return when {
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

            distancia1 == 1 && distancia2 == 2 -> when {
                ehMasculino(genero2) -> "Tio"
                ehFeminino(genero2) -> "Tia"
                else -> "Tio/Tia"
            }

            distancia1 == 2 && distancia2 == 1 -> when {
                ehMasculino(genero2) -> "Sobrinho"
                ehFeminino(genero2) -> "Sobrinha"
                else -> "Sobrinho(a)"
            }

            distancia1 == 2 && distancia2 == 2 -> when {
                ehMasculino(genero2) -> "Primo"
                ehFeminino(genero2) -> "Prima"
                else -> "Primo(a)"
            }

            distancia1 == 1 && distancia2 == 3 -> when {
                ehMasculino(genero2) -> "Tio-avô"
                ehFeminino(genero2) -> "Tia-avó"
                else -> "Tio-avô/Tia-avó"
            }

            distancia1 == 3 && distancia2 == 1 -> when {
                ehMasculino(genero2) -> "Sobrinho-neto"
                ehFeminino(genero2) -> "Sobrinha-neta"
                else -> "Sobrinho(a)-neto(a)"
            }

            distancia1 == distancia2 && distancia1 >= 3 -> {
                val grauPrimo = distancia1 - 1
                when {
                    ehMasculino(genero2) -> "Primo de ${grauPrimo}º grau"
                    ehFeminino(genero2) -> "Prima de ${grauPrimo}º grau"
                    else -> "Primo(a) de ${grauPrimo}º grau"
                }
            }

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