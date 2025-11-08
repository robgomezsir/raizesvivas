package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import timber.log.Timber
import java.util.*

/**
 * Calculadora de layout para posicionamento de nós na árvore genealógica
 * 
 * Implementa algoritmo de posicionamento hierárquico
 */
object ArvoreLayoutCalculator {
    
    // Espaçamento entre nós
    private const val ESPACO_HORIZONTAL = 150f // Espaçamento entre gerações
    private const val ESPACO_VERTICAL = 120f   // Espaçamento entre irmãos
    private const val ESPACO_CONJUGE = 80f     // Espaçamento entre cônjuges
    private const val NO_SIZE = 100f           // Tamanho do nó
    
    /**
     * Posição de um nó na árvore
     */
    data class PosicaoNo(
        val pessoa: Pessoa,
        val x: Float,
        val y: Float,
        val nivel: Int // Geração (0 = raiz)
    )
    
    /**
     * Calcula posições de todos os nós da árvore
     * 
     * @param pessoas Todas as pessoas da árvore (mantido para compatibilidade)
     * @param raizId ID da pessoa raiz (Família Zero - pai ou mãe)
     * @param pessoasMap Mapa de pessoas por ID para acesso rápido
     * @return Lista de posições calculadas
     */
    fun calcularPosicoes(
        @Suppress("UNUSED_PARAMETER") pessoas: List<Pessoa>,
        raizId: String?,
        pessoasMap: Map<String, Pessoa>
    ): List<PosicaoNo> {
        if (raizId == null || pessoasMap.isEmpty()) {
            Timber.w("⚠️ Raiz não encontrada ou árvore vazia")
            return emptyList()
        }
        
        val raiz = pessoasMap[raizId] ?: run {
            Timber.w("⚠️ Pessoa raiz não encontrada: $raizId")
            return emptyList()
        }
        
        val posicoes = mutableListOf<PosicaoNo>()
        val visitados = mutableSetOf<String>()
        
        // Verificar se é Família Zero e buscar o cônjuge
        if (raiz.ehFamiliaZero) {
            // Buscar o cônjuge da Família Zero
            val conjugeId = raiz.conjugeAtual
            val conjuge = conjugeId?.let { pessoasMap[it] }
            
            if (conjuge != null && conjuge.ehFamiliaZero) {
                // Ambos são Família Zero - identificar pai e mãe
                // Pai é quem não tem pai (só tem mãe ou nenhum), mãe é quem não tem mãe (só tem pai ou nenhum)
                val pai = if (raiz.pai == null) raiz else conjuge
                val mae = if (pai.id == raiz.id) conjuge else raiz
                
                // Posicionar pai e mãe lado a lado no nível 0
                val yPai = -ESPACO_CONJUGE / 2f
                val yMae = ESPACO_CONJUGE / 2f
                
                posicoes.add(PosicaoNo(pai, 0f, yPai, 0))
                posicoes.add(PosicaoNo(mae, 0f, yMae, 0))
                visitados.add(pai.id)
                visitados.add(mae.id)
                
                // Processar filhos do casal
                val filhosCasal = pessoasMap.values.filter { pessoa ->
                    (pessoa.pai == pai.id || pessoa.pai == mae.id) &&
                    (pessoa.mae == mae.id || pessoa.mae == pai.id)
                }.distinct()
                
                calcularFilhos(pai, mae, filhosCasal, pessoasMap, visitados, posicoes, 1)
            } else {
                // Apenas uma pessoa Família Zero encontrada
                calcularProfundidades(raiz, pessoasMap, visitados, 0, posicoes)
            }
        } else {
            // Raiz normal - calcular normalmente
            calcularProfundidades(raiz, pessoasMap, visitados, 0, posicoes)
        }
        
        // Centralizar árvore (normalizar coordenadas)
        val minX = posicoes.minOfOrNull { it.x } ?: 0f
        val minY = posicoes.minOfOrNull { it.y } ?: 0f
        
        return posicoes.map { pos ->
            pos.copy(
                x = pos.x - minX,
                y = pos.y - minY
            )
        }
    }
    
    /**
     * Calcula filhos de um casal
     */
    private fun calcularFilhos(
        @Suppress("UNUSED_PARAMETER") pai: Pessoa,
        @Suppress("UNUSED_PARAMETER") mae: Pessoa,
        filhos: List<Pessoa>,
        pessoasMap: Map<String, Pessoa>,
        visitados: MutableSet<String>,
        posicoes: MutableList<PosicaoNo>,
        nivel: Int
    ) {
        if (filhos.isEmpty()) return
        
        // Ordenar filhos por data de nascimento
        val filhosOrdenados = filhos.sortedBy { it.dataNascimento?.time ?: Long.MAX_VALUE }
        
        // Calcular posição Y inicial para centralizar filhos
        val yInicial = -(filhosOrdenados.size - 1) * ESPACO_VERTICAL / 2f
        
        filhosOrdenados.forEachIndexed { index, filho ->
            if (filho.id !in visitados) {
                visitados.add(filho.id)
                
                val y = yInicial + index * ESPACO_VERTICAL
                val x = nivel * ESPACO_HORIZONTAL
                
                posicoes.add(PosicaoNo(filho, x, y, nivel))
                
                // Processar filhos deste filho
                val netosIds = filho.filhos
                if (netosIds.isNotEmpty()) {
                    val netos = netosIds.mapNotNull { pessoasMap[it] }
                    calcularFilhos(
                        pai = filho,
                        mae = filho.conjugeAtual?.let { pessoasMap[it] } ?: filho,
                        filhos = netos,
                        pessoasMap = pessoasMap,
                        visitados = visitados,
                        posicoes = posicoes,
                        nivel = nivel + 1
                    )
                }
                
                // Adicionar cônjuge se existir
                filho.conjugeAtual?.let { conjugeId ->
                    val conjuge = pessoasMap[conjugeId]
                    if (conjuge != null && conjuge.id !in visitados) {
                        visitados.add(conjuge.id)
                        // Posicionar cônjuge ao lado do filho
                        val yConjuge = y + ESPACO_CONJUGE / 2f
                        posicoes.add(PosicaoNo(conjuge, x, yConjuge, nivel))
                    }
                }
            }
        }
    }
    
    /**
     * Calcula profundidades recursivamente (método antigo para compatibilidade)
     */
    private fun calcularProfundidades(
        pessoa: Pessoa,
        pessoasMap: Map<String, Pessoa>,
        visitados: MutableSet<String>,
        nivel: Int,
        posicoes: MutableList<PosicaoNo>
    ) {
        if (pessoa.id in visitados) {
            return // Evitar loops
        }
        
        visitados.add(pessoa.id)
        
        // Adicionar pessoa atual
        val filhos = pessoa.filhos.mapNotNull { pessoasMap[it] }
        val yPosicao = if (filhos.isEmpty()) 0f else -(filhos.size - 1) * ESPACO_VERTICAL / 2f
        
        posicoes.add(PosicaoNo(pessoa, nivel * ESPACO_HORIZONTAL, yPosicao, nivel))
        
        // Processar filhos
        pessoa.filhos.forEach { filhoId ->
            val filho = pessoasMap[filhoId]
            if (filho != null && filho.id !in visitados) {
                calcularProfundidades(filho, pessoasMap, visitados, nivel + 1, posicoes)
            }
        }
    }
    
    /**
     * Encontra o casal da Família Zero (ambos os pais)
     */
    fun encontrarCasalFamiliaZero(pessoas: List<Pessoa>): Pair<Pessoa?, Pessoa?> {
        val familiaZero = pessoas.filter { it.ehFamiliaZero }
        
        if (familiaZero.size >= 2) {
            // Encontrar pai e mãe pelo relacionamento de cônjuge
            val pai = familiaZero.firstOrNull { it.pai == null }
            val mae = familiaZero.firstOrNull { it.mae == null }
            
            if (pai != null && mae != null) {
                return Pair(pai, mae)
            }
            
            // Se não encontrou pelo relacionamento, buscar pelo conjugeAtual
            val pessoa1 = familiaZero[0]
            val pessoa2 = familiaZero[1]
            
            if (pessoa1.conjugeAtual == pessoa2.id) {
                return Pair(pessoa1, pessoa2)
            } else if (pessoa2.conjugeAtual == pessoa1.id) {
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
    
    /**
     * Encontra a raiz da árvore (pessoa sem pai/mãe ou Família Zero)
     */
    fun encontrarRaiz(pessoas: List<Pessoa>): Pessoa? {
        // Buscar pessoa Família Zero
        val familiaZero = pessoas.firstOrNull { it.ehFamiliaZero }
        if (familiaZero != null) {
            return familiaZero
        }
        
        // Buscar pessoa sem pai e mãe
        return pessoas.firstOrNull { 
            it.pai == null && it.mae == null 
        }
    }
    
    /**
     * Busca pessoa na árvore e retorna posição aproximada
     */
    fun encontrarPosicaoPessoa(
        pessoaId: String,
        posicoes: List<PosicaoNo>
    ): PosicaoNo? {
        return posicoes.firstOrNull { it.pessoa.id == pessoaId }
    }
}

