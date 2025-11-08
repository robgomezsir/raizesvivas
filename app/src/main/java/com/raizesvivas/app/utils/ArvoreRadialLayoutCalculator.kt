package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import timber.log.Timber
import kotlin.math.*

/**
 * Calculadora de layout radial para árvore genealógica (mapa mental)
 * 
 * Implementa algoritmo de posicionamento radial:
 * - Família Zero (casal) no centro
 * - Membros organizados em círculos ao redor, por geração
 * - Conexões radiais entre nós
 */
object ArvoreRadialLayoutCalculator {
    
    // Parâmetros do layout radial
    private const val RADIO_INICIAL = 150f // Raio do primeiro círculo (Família Zero)
    private const val ESPACO_ENTRE_CIRCULOS = 200f // Espaçamento entre círculos de gerações
    private const val ESPACO_ENTRE_CASAL = 80f // Espaçamento entre cônjuges no centro
    private const val NO_SIZE = 100f // Tamanho do nó
    
    /**
     * Posição de um nó na árvore radial
     */
    data class PosicaoNoRadial(
        val pessoa: Pessoa,
        val x: Float,
        val y: Float,
        val angulo: Float, // Ângulo em radianos
        val raio: Float, // Raio do centro
        val nivel: Int // Geração (0 = centro, Família Zero)
    )
    
    /**
     * Calcula posições radiais de todos os nós da árvore
     * 
     * @param pessoas Todas as pessoas da árvore
     * @param raizId ID da pessoa raiz (Família Zero)
     * @param pessoasMap Mapa de pessoas por ID
     * @return Lista de posições calculadas
     */
    fun calcularPosicoesRadiais(
        @Suppress("UNUSED_PARAMETER") pessoas: List<Pessoa>,
        raizId: String?,
        pessoasMap: Map<String, Pessoa>
    ): List<PosicaoNoRadial> {
        if (raizId == null || pessoasMap.isEmpty()) {
            Timber.w("⚠️ Raiz não encontrada ou árvore vazia")
            return emptyList()
        }
        
        val raiz = pessoasMap[raizId] ?: run {
            Timber.w("⚠️ Pessoa raiz não encontrada: $raizId")
            return emptyList()
        }
        
        val posicoes = mutableListOf<PosicaoNoRadial>()
        val visitados = mutableSetOf<String>()
        
        // Buscar cônjuge da Família Zero
        val conjugeId = raiz.conjugeAtual
        val conjuge = conjugeId?.let { pessoasMap[it] }
        
        // Posicionar Família Zero (casal) no centro
        if (conjuge != null && raiz.ehFamiliaZero && conjuge.ehFamiliaZero) {
            // Identificar pai e mãe
            val pai = if (raiz.pai == null) raiz else conjuge
            val mae = if (pai.id == raiz.id) conjuge else raiz
            
            // Posicionar pai e mãe lado a lado no centro
            posicoes.add(PosicaoNoRadial(
                pessoa = pai,
                x = -ESPACO_ENTRE_CASAL / 2f,
                y = 0f,
                angulo = 0f,
                raio = 0f,
                nivel = 0
            ))
            
            posicoes.add(PosicaoNoRadial(
                pessoa = mae,
                x = ESPACO_ENTRE_CASAL / 2f,
                y = 0f,
                angulo = PI.toFloat(),
                raio = 0f,
                nivel = 0
            ))
            
            visitados.add(pai.id)
            visitados.add(mae.id)
            
            // Processar descendentes (filhos, netos, etc.)
            processarDescendentes(pai, mae, pessoasMap, visitados, posicoes, 1)
            
            // Processar ascendentes (avós, bisavós, etc.)
            processarAscendentes(pai, mae, pessoasMap, visitados, posicoes, -1)
        } else {
            // Apenas uma pessoa Família Zero - posicionar no centro
            posicoes.add(PosicaoNoRadial(
                pessoa = raiz,
                x = 0f,
                y = 0f,
                angulo = 0f,
                raio = 0f,
                nivel = 0
            ))
            visitados.add(raiz.id)
            
            // Processar descendentes
            @Suppress("UNUSED_VARIABLE")
            val filhos = pessoasMap.values.filter { 
                it.pai == raiz.id || it.mae == raiz.id 
            }
            processarDescendentes(raiz, null, pessoasMap, visitados, posicoes, 1)
            
            // Processar ascendentes
            processarAscendentes(raiz, null, pessoasMap, visitados, posicoes, -1)
        }
        
        return posicoes
    }
    
    /**
     * Processa descendentes (filhos, netos, etc.) de forma radial
     */
    private fun processarDescendentes(
        pai: Pessoa?,
        mae: Pessoa?,
        pessoasMap: Map<String, Pessoa>,
        visitados: MutableSet<String>,
        posicoes: MutableList<PosicaoNoRadial>,
        nivel: Int
    ) {
        if (pai == null && mae == null) return
        
        // Buscar filhos do casal
        val filhos = pessoasMap.values.filter { pessoa ->
            !visitados.contains(pessoa.id) && (
                (pai != null && pessoa.pai == pai.id) ||
                (mae != null && pessoa.mae == mae.id) ||
                (pai != null && mae != null && 
                 ((pessoa.pai == pai.id && pessoa.mae == mae.id) ||
                  (pessoa.pai == mae.id && pessoa.mae == pai.id)))
            )
        }.distinct()
        
        if (filhos.isEmpty()) return
        
        // Calcular raio para este nível
        val raio = RADIO_INICIAL + (nivel - 1) * ESPACO_ENTRE_CIRCULOS
        
        // Distribuir filhos em círculo
        val anguloIncremento = (2 * PI) / filhos.size
        var anguloAtual = 0.0
        
        filhos.forEach { filho ->
            val x = (raio * cos(anguloAtual)).toFloat()
            val y = (raio * sin(anguloAtual)).toFloat()
            
            posicoes.add(PosicaoNoRadial(
                pessoa = filho,
                x = x,
                y = y,
                angulo = anguloAtual.toFloat(),
                raio = raio,
                nivel = nivel
            ))
            
            visitados.add(filho.id)
            
            // Buscar cônjuge do filho
            val conjugeFilho = filho.conjugeAtual?.let { pessoasMap[it] }
            
            // Processar filhos deste filho (netos) - recursão
            if (conjugeFilho != null && !visitados.contains(conjugeFilho.id)) {
                // Posicionar cônjuge próximo ao filho (mesmo círculo, ligeiramente deslocado)
                val anguloConjuge = anguloAtual + (anguloIncremento * 0.2)
                val raioConjuge = raio + 40f // Ligeiramente mais externo
                val xConjuge = (raioConjuge * cos(anguloConjuge)).toFloat()
                val yConjuge = (raioConjuge * sin(anguloConjuge)).toFloat()
                
                posicoes.add(PosicaoNoRadial(
                    pessoa = conjugeFilho,
                    x = xConjuge,
                    y = yConjuge,
                    angulo = anguloConjuge.toFloat(),
                    raio = raioConjuge,
                    nivel = nivel
                ))
                
                visitados.add(conjugeFilho.id)
                
                // Processar netos (próximo nível)
                processarDescendentes(filho, conjugeFilho, pessoasMap, visitados, posicoes, nivel + 1)
            } else {
                // Processar netos apenas com o filho
                processarDescendentes(filho, null, pessoasMap, visitados, posicoes, nivel + 1)
            }
            
            anguloAtual += anguloIncremento
        }
    }
    
    /**
     * Processa ascendentes (avós, bisavós, etc.) de forma radial
     */
    private fun processarAscendentes(
        pessoa: Pessoa?,
        @Suppress("UNUSED_PARAMETER") conjuge: Pessoa?,
        pessoasMap: Map<String, Pessoa>,
        visitados: MutableSet<String>,
        posicoes: MutableList<PosicaoNoRadial>,
        nivel: Int
    ) {
        if (pessoa == null) return
        
        // Buscar pais
        val pai = pessoa.pai?.let { pessoasMap[it] }
        val mae = pessoa.mae?.let { pessoasMap[it] }
        
        if (pai == null && mae == null) return
        
        // Calcular raio para este nível (negativo para ascendentes)
        val raio = RADIO_INICIAL + abs(nivel) * ESPACO_ENTRE_CIRCULOS
        
        // Distribuir pais em círculo (lado oposto aos descendentes)
        val anguloBase = PI // Começar do lado oposto
        
        if (pai != null && !visitados.contains(pai.id)) {
            val x = (raio * cos(anguloBase)).toFloat()
            val y = (raio * sin(anguloBase)).toFloat()
            
            posicoes.add(PosicaoNoRadial(
                pessoa = pai,
                x = x,
                y = y,
                angulo = anguloBase.toFloat(),
                raio = raio,
                nivel = nivel
            ))
            
            visitados.add(pai.id)
        }
        
        if (mae != null && !visitados.contains(mae.id)) {
            val angulo = anguloBase + (PI / 4) // Próximo ao pai
            val x = (raio * cos(angulo)).toFloat()
            val y = (raio * sin(angulo)).toFloat()
            
            posicoes.add(PosicaoNoRadial(
                pessoa = mae,
                x = x,
                y = y,
                angulo = angulo.toFloat(),
                raio = raio,
                nivel = nivel
            ))
            
            visitados.add(mae.id)
            
            // Processar avós (próximo nível ascendente)
            if (pai != null) {
                processarAscendentes(pai, mae, pessoasMap, visitados, posicoes, nivel - 1)
            } else {
                processarAscendentes(mae, null, pessoasMap, visitados, posicoes, nivel - 1)
            }
        } else if (pai != null) {
            processarAscendentes(pai, null, pessoasMap, visitados, posicoes, nivel - 1)
        }
    }
    
    /**
     * Calcula conexões entre nós para o layout radial
     */
    fun calcularConexoes(
        posicoes: List<PosicaoNoRadial>,
        @Suppress("UNUSED_PARAMETER") pessoasMap: Map<String, Pessoa>
    ): List<ConexaoRadial> {
        val conexoes = mutableListOf<ConexaoRadial>()
        
        posicoes.forEach { posicao ->
            val pessoa = posicao.pessoa
            
            // Conexão com pai
            pessoa.pai?.let { paiId ->
                val paiPosicao = posicoes.find { it.pessoa.id == paiId }
                if (paiPosicao != null) {
                    conexoes.add(ConexaoRadial(
                        origem = paiPosicao,
                        destino = posicao,
                        tipo = TipoConexao.PAI_FILHO
                    ))
                }
            }
            
            // Conexão com mãe
            pessoa.mae?.let { maeId ->
                val maePosicao = posicoes.find { it.pessoa.id == maeId }
                if (maePosicao != null) {
                    conexoes.add(ConexaoRadial(
                        origem = maePosicao,
                        destino = posicao,
                        tipo = TipoConexao.MAE_FILHO
                    ))
                }
            }
            
            // Conexão com cônjuge
            pessoa.conjugeAtual?.let { conjugeId ->
                val conjugePosicao = posicoes.find { it.pessoa.id == conjugeId }
                if (conjugePosicao != null && !conexoes.any { 
                    it.origem.pessoa.id == pessoa.id && it.destino.pessoa.id == conjugeId ||
                    it.origem.pessoa.id == conjugeId && it.destino.pessoa.id == pessoa.id
                }) {
                    conexoes.add(ConexaoRadial(
                        origem = posicao,
                        destino = conjugePosicao,
                        tipo = TipoConexao.CASAL
                    ))
                }
            }
        }
        
        return conexoes
    }
    
    /**
     * Conexão entre dois nós
     */
    data class ConexaoRadial(
        val origem: PosicaoNoRadial,
        val destino: PosicaoNoRadial,
        val tipo: TipoConexao
    )
    
    /**
     * Tipo de conexão
     */
    enum class TipoConexao {
        PAI_FILHO,
        MAE_FILHO,
        CASAL
    }
}

