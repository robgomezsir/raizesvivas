package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import timber.log.Timber

/**
 * Calculadora de layout hierárquico vertical para árvore genealógica
 * 
 * Baseado em visualização hierárquica vertical organizada onde:
 * - Casal Família Zero fica no topo (raiz)
 * - Relacionamentos organizados em níveis verticais
 * - Layout limpo e organizado, sem sobreposições
 * - Conexões verticais e horizontais claras
 * - Suporte a expandir/recolher nós
 */
object ArvoreHierarquicaCalculator {
    
    // Espaçamento horizontal entre nós no mesmo nível
    private const val ESPACAMENTO_HORIZONTAL = 160f // Espaço entre nós no mesmo nível
    
    // Espaçamento vertical entre níveis
    private const val ESPACAMENTO_VERTICAL = 200f // Espaço entre níveis
    
    // Espaçamento do casal central (raiz)
    private const val ESPACAMENTO_CASAL = 100f // Espaço entre os dois membros do casal no topo
    
    // Indentação por nível (mantida para referência futura)
    @Suppress("UNUSED")
    private const val INDENTACAO_NIVEL = 80f // Indentação por nível de profundidade
    
    /**
     * Nó da árvore hierárquica com informações de layout
     */
    data class NoHierarquico(
        val pessoa: Pessoa,
        val nivel: Int,                    // Nível na hierarquia (0 = raiz)
        val x: Float,                      // Posição X
        val y: Float,                      // Posição Y
        val filhosIds: List<String>,       // IDs dos filhos diretos
        val tipoRelacao: TipoRelacao? = null,
        val isExpanded: Boolean = false    // Se o nó está expandido (padrão: contraído)
    )
    
    /**
     * Tipos de relacionamento
     */
    enum class TipoRelacao {
        PAI,
        MAE,
        CONJUGE,
        FILHO,
        IRMAO,
        @Suppress("UNUSED") AVO,
        @Suppress("UNUSED") NETO,
        @Suppress("UNUSED") OUTRO
    }
    
    /**
     * Resultado do cálculo de layout hierárquico
     */
    data class ResultadoLayout(
        val nos: List<NoHierarquico>,
        val larguraTotal: Float,
        val alturaTotal: Float
    )
    
    /**
     * Calcula layout hierárquico vertical organizado
     */
    fun calcularLayoutHierarquico(
        todasPessoas: List<Pessoa>,
        pessoaRaizId: String?,
        pessoasMap: Map<String, Pessoa>,
        nosExpandidos: Set<String> = emptySet(),
        casalFamiliaZero: Pair<Pessoa?, Pessoa?>? = null
    ): ResultadoLayout {
        if (todasPessoas.isEmpty()) {
            Timber.w("⚠️ Nenhuma pessoa disponível")
            return ResultadoLayout(emptyList(), 0f, 0f)
        }
        
        // Determinar raiz (casal Família Zero)
        val raiz: Pair<Pessoa?, Pessoa?> = when {
            pessoaRaizId != null -> {
                val pessoa = pessoasMap[pessoaRaizId]
                val conjuge = pessoa?.conjugeAtual?.let { pessoasMap[it] }
                Pair(pessoa, conjuge)
            }
            casalFamiliaZero != null && casalFamiliaZero.first != null -> {
                casalFamiliaZero
            }
            else -> {
                encontrarCasalFamiliaZero(todasPessoas)
            }
        }
        
        val pessoaRaiz = raiz.first
        if (pessoaRaiz == null) {
            Timber.w("⚠️ Não foi possível determinar pessoa raiz")
            return ResultadoLayout(emptyList(), 0f, 0f)
        }
        
        Timber.d("📍 Raiz: ${pessoaRaiz.nome}${raiz.second?.let { " e ${it.nome}" } ?: ""}")
        
        // Construir estrutura hierárquica
        val estrutura = construirEstruturaHierarquica(pessoaRaiz, raiz.second, todasPessoas, pessoasMap)
        
        // Calcular posições
        val nos = mutableListOf<NoHierarquico>()
        val posicoes = mutableMapOf<String, Pair<Float, Float>>()
        
        // Posicionar raiz no topo
        val raizX = if (raiz.second != null) {
            // Se há cônjuge, centralizar o casal
            0f - ESPACAMENTO_CASAL / 2f
        } else {
            0f
        }
        val raizY = 0f
        posicoes[pessoaRaiz.id] = Pair(raizX, raizY)
        
        // Adicionar raiz
        nos.add(
            NoHierarquico(
                pessoa = pessoaRaiz,
                nivel = 0,
                x = raizX,
                y = raizY,
                filhosIds = estrutura[pessoaRaiz.id]?.filhos ?: emptyList(),
                tipoRelacao = null,
                isExpanded = nosExpandidos.contains(pessoaRaiz.id)
            )
        )
        
        // Adicionar cônjuge ao lado da raiz se existir
        val conjugeX = raizX + ESPACAMENTO_CASAL
        raiz.second?.let { conjuge ->
            posicoes[conjuge.id] = Pair(conjugeX, raizY)
            nos.add(
                NoHierarquico(
                    pessoa = conjuge,
                    nivel = 0,
                    x = conjugeX,
                    y = raizY,
                    filhosIds = estrutura[conjuge.id]?.filhos ?: emptyList(),
                    tipoRelacao = TipoRelacao.CONJUGE,
                    isExpanded = nosExpandidos.contains(conjuge.id)
                )
            )
        }
        
        // Posicionar filhos recursivamente
        val visitados = mutableSetOf<String>()
        visitados.add(pessoaRaiz.id)
        raiz.second?.let { visitados.add(it.id) }
        
        // Centralizar horizontalmente a partir do ponto médio do casal
        val centroX = if (raiz.second != null) {
            (raizX + conjugeX) / 2f
        } else {
            raizX
        }
        
        // Verificar se raiz e cônjuge estão expandidos (sem expandir automaticamente)
        val raizExpandida = nosExpandidos.contains(pessoaRaiz.id)
        val conjugeExpandido = raiz.second?.let { nosExpandidos.contains(it.id) } ?: false
        
        // Posicionar filhos apenas se a raiz ou o cônjuge estiverem expandidos
        val maxY = if (raizExpandida || conjugeExpandido) {
            posicionarFilhos(
                pessoaRaiz.id,
                estrutura,
                pessoasMap,
                posicoes,
                nos,
                visitados,
                nosExpandidos,  // Usar apenas o conjunto original, sem substituição automática
                0,
                centroX,
                raizY + ESPACAMENTO_VERTICAL
            )
        } else {
            raizY
        }
        
        // Calcular dimensões totais
        val larguraTotal = posicoes.values.maxOfOrNull { it.first }?.plus(ESPACAMENTO_HORIZONTAL) ?: 0f
        val alturaTotal = maxY + ESPACAMENTO_VERTICAL
        
        Timber.d("✅ Layout hierárquico calculado: ${nos.size} nós, largura: $larguraTotal, altura: $alturaTotal")
        
        return ResultadoLayout(nos, larguraTotal, alturaTotal)
    }
    
    /**
     * Estrutura de um nó na hierarquia
     */
    private data class EstruturaNo(
        val pessoa: Pessoa,
        val filhos: List<String>,
        val tipoRelacao: TipoRelacao?
    )
    
    /**
     * Constrói estrutura hierárquica da árvore
     */
    private fun construirEstruturaHierarquica(
        raiz: Pessoa,
        conjugeRaiz: Pessoa?,
        @Suppress("UNUSED_PARAMETER") todasPessoas: List<Pessoa>,
        pessoasMap: Map<String, Pessoa>
    ): Map<String, EstruturaNo> {
        val estrutura = mutableMapOf<String, EstruturaNo>()
        val processados = mutableSetOf<String>()
        
        fun processarPessoa(pessoa: Pessoa, tipoRelacao: TipoRelacao?): List<String> {
            if (pessoa.id in processados) return emptyList()
            processados.add(pessoa.id)
            
            val filhosIds = mutableListOf<String>()
            
            // Filhos diretos
            pessoa.filhos.forEach { filhoId ->
                pessoasMap[filhoId]?.let { filho ->
                    if (filho.id !in processados) {
                        filhosIds.add(filhoId)
                        processarPessoa(filho, TipoRelacao.FILHO)
                    }
                }
            }
            
            estrutura[pessoa.id] = EstruturaNo(pessoa, filhosIds, tipoRelacao)
            return filhosIds
        }
        
        // Processar raiz
        processarPessoa(raiz, null)
        
        // Processar cônjuge da raiz
        conjugeRaiz?.let { 
            processarPessoa(it, TipoRelacao.CONJUGE)
            // Combinar filhos do cônjuge com os da raiz (filhos compartilhados)
            val filhosRaiz = estrutura[raiz.id]?.filhos ?: emptyList()
            val filhosConjuge = estrutura[it.id]?.filhos ?: emptyList()
            val todosFilhos = (filhosRaiz + filhosConjuge).distinct()
            estrutura[raiz.id] = estrutura[raiz.id]!!.copy(filhos = todosFilhos)
            estrutura[it.id] = estrutura[it.id]!!.copy(filhos = todosFilhos)
        }
        
        // Processar pais (acima da raiz) - adicionar como filhos da raiz
        val paisIds = mutableListOf<String>()
        raiz.pai?.let { paiId ->
            pessoasMap[paiId]?.let { pai ->
                if (pai.id !in processados) {
                    paisIds.add(paiId)
                    processarPessoa(pai, TipoRelacao.PAI)
                }
            }
        }
        raiz.mae?.let { maeId ->
            pessoasMap[maeId]?.let { mae ->
                if (mae.id !in processados) {
                    paisIds.add(maeId)
                    processarPessoa(mae, TipoRelacao.MAE)
                }
            }
        }
        
        // Adicionar pais como filhos da raiz (para visualização acima)
        if (paisIds.isNotEmpty()) {
            val estruturaRaiz = estrutura[raiz.id]!!
            estrutura[raiz.id] = estruturaRaiz.copy(filhos = paisIds + estruturaRaiz.filhos)
        }
        
        return estrutura
    }
    
    /**
     * Posiciona filhos recursivamente
     */
    private fun posicionarFilhos(
        paiId: String,
        estrutura: Map<String, EstruturaNo>,
        pessoasMap: Map<String, Pessoa>,
        posicoes: MutableMap<String, Pair<Float, Float>>,
        nos: MutableList<NoHierarquico>,
        visitados: MutableSet<String>,
        nosExpandidos: Set<String>,
        nivel: Int,
        xInicial: Float,
        yInicial: Float
    ): Float {
        val estruturaPai = estrutura[paiId] ?: return yInicial
        val filhosIds = estruturaPai.filhos.filter { it !in visitados }
        
        if (filhosIds.isEmpty()) return yInicial
        
        // Um nó está expandido apenas se estiver explicitamente na lista de nós expandidos
        // Todos os nós (incluindo a raiz/Família Zero) iniciam contraídos por padrão
        val isExpanded = nosExpandidos.contains(paiId)
        
        if (!isExpanded) return yInicial
        
        // Calcular posição inicial para centralizar horizontalmente
        val quantidadeFilhos = filhosIds.size
        val totalWidth = (quantidadeFilhos - 1) * ESPACAMENTO_HORIZONTAL
        var currentX = xInicial - (totalWidth / 2f)
        var currentY = yInicial
        
        filhosIds.forEach { filhoId ->
            val pessoa = pessoasMap[filhoId] ?: return@forEach
            
            if (filhoId in visitados) return@forEach
            visitados.add(filhoId)
            
            val estruturaFilho = estrutura[filhoId]
            val filhosFilho = estruturaFilho?.filhos ?: emptyList()
            
            // Posicionar nó
            posicoes[filhoId] = Pair(currentX, currentY)
            
            nos.add(
                NoHierarquico(
                    pessoa = pessoa,
                    nivel = nivel + 1,
                    x = currentX,
                    y = currentY,
                    filhosIds = filhosFilho,
                    tipoRelacao = estruturaFilho?.tipoRelacao,
                    // Um filho está expandido apenas se estiver explicitamente na lista de nós expandidos
                    isExpanded = nosExpandidos.contains(filhoId)
                )
            )
            
            // Posicionar filhos recursivamente e atualizar Y máximo
            if (filhosFilho.isNotEmpty()) {
                val alturaSubarvore = posicionarFilhos(
                    filhoId,
                    estrutura,
                    pessoasMap,
                    posicoes,
                    nos,
                    visitados,
                    nosExpandidos,
                    nivel + 1,
                    currentX,
                    currentY + ESPACAMENTO_VERTICAL
                )
                currentY = maxOf(currentY, alturaSubarvore)
            }
            
            // Avançar para próximo filho horizontalmente
            currentX += ESPACAMENTO_HORIZONTAL
        }
        
        return currentY
    }
    
    /**
     * Encontra o casal da Família Zero
     */
    fun encontrarCasalFamiliaZero(pessoas: List<Pessoa>): Pair<Pessoa?, Pessoa?> {
        if (pessoas.isEmpty()) {
            return Pair(null, null)
        }
        
        val familiaZero = pessoas.filter { it.ehFamiliaZero }
        
        Timber.d("🔍 Buscando Família Zero: ${familiaZero.size} pessoas marcadas como Família Zero de ${pessoas.size} total")
        
        if (familiaZero.size >= 2) {
            val pessoa1 = familiaZero[0]
            val pessoa2 = familiaZero[1]
            
            if (pessoa1.conjugeAtual == pessoa2.id) {
                Timber.d("✅ Casal Família Zero encontrado: ${pessoa1.nome} e ${pessoa2.nome}")
                return Pair(pessoa1, pessoa2)
            } else if (pessoa2.conjugeAtual == pessoa1.id) {
                Timber.d("✅ Casal Família Zero encontrado: ${pessoa2.nome} e ${pessoa1.nome}")
                return Pair(pessoa2, pessoa1)
            }
        }
        
        if (familiaZero.isNotEmpty()) {
            Timber.d("⚠️ Apenas uma pessoa Família Zero encontrada: ${familiaZero.first().nome}")
            return Pair(familiaZero.firstOrNull(), null)
        }
        
        // NÃO usar fallback - se não há Família Zero marcada, retornar null
        // Isso evita mostrar casais incorretos como Família Zero na árvore
        Timber.d("⚠️ Nenhuma Família Zero encontrada. A Família Zero deve ser definida explicitamente.")
        return Pair(null, null)
    }
}
