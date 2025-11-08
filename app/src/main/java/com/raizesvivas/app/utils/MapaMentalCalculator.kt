package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import timber.log.Timber
import kotlin.math.*

/**
 * Calculadora de layout estilo Mapa Mental Hierárquico Organizado para árvore genealógica
 * 
 * Baseado em visualização hierárquica organizada onde:
 * - Casal Família Zero fica claramente no centro (lado a lado)
 * - Relacionamentos organizados em camadas bem espaçadas
 * - Sem sobreposições - cada nó tem espaço garantido
 * - Layout limpo e organizado por tipo de relacionamento
 * - Conexões claras e diretas
 * 
 * NOTA: Este arquivo está mantido para referência futura. Atualmente usa-se ArvoreHierarquicaCalculator.
 */
@Suppress("UNUSED", "UnusedPrivateMember")
object MapaMentalCalculator {
    
    // Espaçamento radial entre camadas (aumentado para evitar sobreposições)
    @Suppress("UNUSED")
    private const val RAIO_CAMADA_0 = 0f      // Centro (casal Família Zero)
    private const val RAIO_CAMADA_1 = 250f   // Pais (topo)
    private const val RAIO_CAMADA_2 = 400f    // Filhos (baixo)
    private const val RAIO_CAMADA_3 = 600f   // Netos/Avós
    @Suppress("UNUSED")
    private const val RAIO_CAMADA_4 = 800f   // Outros relacionamentos
    
    // Espaçamento mínimo entre nós na mesma camada
    private const val ESPACAMENTO_MINIMO_NOS = 140f // Espaço mínimo entre centros dos cards (120dp card + 20dp margin)
    
    // Espaçamento do casal central
    private const val ESPACAMENTO_CASAL = 80f // Espaço entre os dois membros do casal
    
    // Ângulos de posicionamento por tipo de relacionamento
    private const val ANGULO_PAI = -PI / 2        // Topo (0°)
    @Suppress("UNUSED")
    private const val ANGULO_MAE = -PI / 2 + PI / 12  // Topo-direita (15°)
    private const val ANGULO_TOP_ESQUERDA = -PI / 2 - PI / 3  // Topo-esquerda (-60°)
    private const val ANGULO_TOP_DIREITA = -PI / 2 + PI / 3   // Topo-direita (60°)
    private const val ANGULO_BOTTOM = PI / 2     // Parte inferior (180°)
    
    /**
     * Posição de um nó no mapa mental
     */
    data class PosicaoNo(
        val pessoa: Pessoa,
        val x: Float,
        val y: Float,
        val camada: Int,                    // Camada radial (0 = centro)
        val tipoRelacao: TipoRelacao? = null,
        val angulo: Float = 0f              // Ângulo em radianos para animações
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
        AVO,
        NETO,
        OUTRO
    }
    
    /**
     * Calcula posições estilo mapa mental hierárquico organizado
     * Evita sobreposições usando espaçamento adequado e organização por camadas
     */
    @Suppress("UNUSED")
    fun calcularPosicoes(
        todasPessoas: List<Pessoa>,
        pessoaCentralId: String?,
        pessoasMap: Map<String, Pessoa>,
        casalFamiliaZero: Pair<Pessoa?, Pessoa?>? = null
    ): List<PosicaoNo> {
        if (todasPessoas.isEmpty()) {
            Timber.w("⚠️ Nenhuma pessoa disponível")
            return emptyList()
        }
        
        val posicoes = mutableListOf<PosicaoNo>()
        val visitados = mutableSetOf<String>()
        
        // Determinar casal central (Família Zero ou pessoa individual)
        val casalCentral: Pair<Pessoa?, Pessoa?> = when {
            pessoaCentralId != null -> {
                val pessoa = pessoasMap[pessoaCentralId]
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
        
        val pessoa1 = casalCentral.first
        val pessoa2 = casalCentral.second
        
        if (pessoa1 == null) {
            Timber.w("⚠️ Não foi possível determinar pessoa central")
            return emptyList()
        }
        
        Timber.d("📍 Casal central: ${pessoa1.nome}${pessoa2?.let { " e ${it.nome}" } ?: ""}")
        
        // CAMADA 0: Posicionar casal central no centro (lado a lado, bem espaçado)
        if (pessoa2 != null) {
            // Casal: posicionar lado a lado no centro com espaçamento adequado
            posicoes.add(PosicaoNo(pessoa1, -ESPACAMENTO_CASAL / 2f, 0f, 0, TipoRelacao.CONJUGE, 0f))
            posicoes.add(PosicaoNo(pessoa2, ESPACAMENTO_CASAL / 2f, 0f, 0, TipoRelacao.CONJUGE, 0f))
            visitados.add(pessoa1.id)
            visitados.add(pessoa2.id)
        } else {
            // Apenas uma pessoa central
            posicoes.add(PosicaoNo(pessoa1, 0f, 0f, 0, null, 0f))
            visitados.add(pessoa1.id)
        }
        
        @Suppress("UNUSED_VARIABLE")
        val pessoaReferencia = pessoa1
        
        // CAMADA 1: Pais do casal central (topo da árvore)
        val camada1Pais = mutableListOf<Pair<Pessoa, TipoRelacao>>()
        
        // Coletar todos os pais únicos
        val paisUnicos = mutableSetOf<String>()
        
        pessoaReferencia.pai?.let { paiId ->
            pessoasMap[paiId]?.let { pai ->
                if (pai.id !in visitados && paiId !in paisUnicos) {
                    camada1Pais.add(Pair(pai, TipoRelacao.PAI))
                    visitados.add(pai.id)
                    paisUnicos.add(paiId)
                }
            }
        }
        
        pessoaReferencia.mae?.let { maeId ->
            pessoasMap[maeId]?.let { mae ->
                if (mae.id !in visitados && maeId !in paisUnicos) {
                    camada1Pais.add(Pair(mae, TipoRelacao.MAE))
                    visitados.add(mae.id)
                    paisUnicos.add(maeId)
                }
            }
        }
        
        pessoa2?.let { p2 ->
            p2.pai?.let { paiId ->
                pessoasMap[paiId]?.let { pai ->
                    if (pai.id !in visitados && paiId !in paisUnicos) {
                        camada1Pais.add(Pair(pai, TipoRelacao.PAI))
                        visitados.add(pai.id)
                        paisUnicos.add(paiId)
                    }
                }
            }
            
            p2.mae?.let { maeId ->
                pessoasMap[maeId]?.let { mae ->
                    if (mae.id !in visitados && maeId !in paisUnicos) {
                        camada1Pais.add(Pair(mae, TipoRelacao.MAE))
                        visitados.add(mae.id)
                        paisUnicos.add(maeId)
                    }
                }
            }
        }
        
        // Posicionar camada 1 no topo, distribuindo horizontalmente
        if (camada1Pais.isNotEmpty()) {
            val totalWidth = (camada1Pais.size - 1) * ESPACAMENTO_MINIMO_NOS
            val startX = -totalWidth / 2f
            
            camada1Pais.forEachIndexed { index, (pessoa, tipo) ->
                val x = startX + (index * ESPACAMENTO_MINIMO_NOS)
                val y = -RAIO_CAMADA_1
                val angulo = ANGULO_PAI // Todos no topo
                posicoes.add(PosicaoNo(pessoa, x, y, 1, tipo, angulo.toFloat()))
            }
        }
        
        // CAMADA 2: Filhos do casal central (parte inferior)
        val camada2Filhos = mutableListOf<Pair<Pessoa, TipoRelacao>>()
        
        // Coletar todos os filhos únicos
        val filhosUnicos = mutableSetOf<String>()
        
        pessoaReferencia.filhos.forEach { filhoId ->
            pessoasMap[filhoId]?.let { filho ->
                if (filho.id !in visitados && filhoId !in filhosUnicos) {
                    camada2Filhos.add(Pair(filho, TipoRelacao.FILHO))
                    visitados.add(filho.id)
                    filhosUnicos.add(filhoId)
                }
            }
        }
        
        pessoa2?.filhos?.forEach { filhoId ->
            pessoasMap[filhoId]?.let { filho ->
                if (filho.id !in visitados && filhoId !in filhosUnicos) {
                    camada2Filhos.add(Pair(filho, TipoRelacao.FILHO))
                    visitados.add(filho.id)
                    filhosUnicos.add(filhoId)
                }
            }
        }
        
        // Irmãos (filhos dos pais do casal) - adicionar depois dos filhos
        todasPessoas.forEach { pessoa ->
            if (pessoa.id !in visitados) {
                val ehIrmao = (pessoa.pai == pessoaReferencia.pai && pessoa.pai != null) ||
                             (pessoa.mae == pessoaReferencia.mae && pessoa.mae != null) ||
                             (pessoa2 != null && (
                                (pessoa.pai == pessoa2.pai && pessoa.pai != null) ||
                                (pessoa.mae == pessoa2.mae && pessoa.mae != null)
                             ))
                
                if (ehIrmao) {
                    camada2Filhos.add(Pair(pessoa, TipoRelacao.IRMAO))
                    visitados.add(pessoa.id)
                }
            }
        }
        
        // Posicionar camada 2 na parte inferior, distribuindo horizontalmente
        if (camada2Filhos.isNotEmpty()) {
            val totalWidth = (camada2Filhos.size - 1) * ESPACAMENTO_MINIMO_NOS
            val startX = -totalWidth / 2f
            
            camada2Filhos.forEachIndexed { index, (pessoa, tipo) ->
                val x = startX + (index * ESPACAMENTO_MINIMO_NOS)
                val y = RAIO_CAMADA_2
                val angulo = ANGULO_BOTTOM // Todos na parte inferior
                posicoes.add(PosicaoNo(pessoa, x, y, 2, tipo, angulo.toFloat()))
            }
        }
        
        // CAMADA 3: Netos (filhos dos filhos) - abaixo da camada 2
        val camada3Netos = mutableListOf<Pair<Pessoa, TipoRelacao>>()
        val netosUnicos = mutableSetOf<String>()
        
        pessoaReferencia.filhos.forEach { filhoId ->
            pessoasMap[filhoId]?.let { filho ->
                filho.filhos.forEach { netoId ->
                    pessoasMap[netoId]?.let { neto ->
                        if (neto.id !in visitados && netoId !in netosUnicos) {
                            camada3Netos.add(Pair(neto, TipoRelacao.NETO))
                            visitados.add(neto.id)
                            netosUnicos.add(netoId)
                        }
                    }
                }
            }
        }
        
        pessoa2?.filhos?.forEach { filhoId ->
            pessoasMap[filhoId]?.let { filho ->
                filho.filhos.forEach { netoId ->
                    pessoasMap[netoId]?.let { neto ->
                        if (neto.id !in visitados && netoId !in netosUnicos) {
                            camada3Netos.add(Pair(neto, TipoRelacao.NETO))
                            visitados.add(neto.id)
                            netosUnicos.add(netoId)
                        }
                    }
                }
            }
        }
        
        // Posicionar netos abaixo dos filhos
        if (camada3Netos.isNotEmpty()) {
            val totalWidth = (camada3Netos.size - 1) * ESPACAMENTO_MINIMO_NOS
            val startX = -totalWidth / 2f
            
            camada3Netos.forEachIndexed { index, (pessoa, tipo) ->
                val x = startX + (index * ESPACAMENTO_MINIMO_NOS)
                val y = RAIO_CAMADA_2 + RAIO_CAMADA_1 // Abaixo da camada 2
                posicoes.add(PosicaoNo(pessoa, x, y, 3, tipo, ANGULO_BOTTOM.toFloat()))
            }
        }
        
        // CAMADA 4: Avós (pais dos pais) - acima da camada 1
        val camada4Avos = mutableListOf<Pair<Pessoa, TipoRelacao>>()
        val avosUnicos = mutableSetOf<String>()
        
        fun adicionarAvos(pessoa: Pessoa) {
            pessoa.pai?.let { paiId ->
                pessoasMap[paiId]?.let { pai ->
                    pai.pai?.let { avoId ->
                        pessoasMap[avoId]?.let { avo ->
                            if (avo.id !in visitados && avoId !in avosUnicos) {
                                camada4Avos.add(Pair(avo, TipoRelacao.AVO))
                                visitados.add(avo.id)
                                avosUnicos.add(avoId)
                            }
                        }
                    }
                    pai.mae?.let { avoId ->
                        pessoasMap[avoId]?.let { avo ->
                            if (avo.id !in visitados && avoId !in avosUnicos) {
                                camada4Avos.add(Pair(avo, TipoRelacao.AVO))
                                visitados.add(avo.id)
                                avosUnicos.add(avoId)
                            }
                        }
                    }
                }
            }
            
            pessoa.mae?.let { maeId ->
                pessoasMap[maeId]?.let { mae ->
                    mae.pai?.let { avoId ->
                        pessoasMap[avoId]?.let { avo ->
                            if (avo.id !in visitados && avoId !in avosUnicos) {
                                camada4Avos.add(Pair(avo, TipoRelacao.AVO))
                                visitados.add(avo.id)
                                avosUnicos.add(avoId)
                            }
                        }
                    }
                    mae.mae?.let { avoId ->
                        pessoasMap[avoId]?.let { avo ->
                            if (avo.id !in visitados && avoId !in avosUnicos) {
                                camada4Avos.add(Pair(avo, TipoRelacao.AVO))
                                visitados.add(avo.id)
                                avosUnicos.add(avoId)
                            }
                        }
                    }
                }
            }
        }
        
        adicionarAvos(pessoaReferencia)
        pessoa2?.let { adicionarAvos(it) }
        
        // Posicionar avós acima dos pais
        if (camada4Avos.isNotEmpty()) {
            val totalWidth = (camada4Avos.size - 1) * ESPACAMENTO_MINIMO_NOS
            val startX = -totalWidth / 2f
            
            camada4Avos.forEachIndexed { index, (pessoa, tipo) ->
                val x = startX + (index * ESPACAMENTO_MINIMO_NOS)
                val y = -(RAIO_CAMADA_1 + RAIO_CAMADA_1) // Acima da camada 1
                posicoes.add(PosicaoNo(pessoa, x, y, 3, tipo, ANGULO_PAI.toFloat()))
            }
        }
        
        // CAMADA 5: Outros relacionamentos conectados - laterais
        val camada5Outros = todasPessoas.filter { it.id !in visitados }
        
        if (camada5Outros.isNotEmpty()) {
            // Distribuir nas laterais (esquerda e direita)
            val metade = camada5Outros.size / 2
            val esquerda = camada5Outros.take(metade)
            val direita = camada5Outros.drop(metade)
            
            // Esquerda
            esquerda.forEachIndexed { index, pessoa ->
                val y = (index - esquerda.size / 2f) * ESPACAMENTO_MINIMO_NOS
                val x = -RAIO_CAMADA_3
                posicoes.add(PosicaoNo(pessoa, x, y, 4, TipoRelacao.OUTRO, ANGULO_TOP_ESQUERDA.toFloat()))
            }
            
            // Direita
            direita.forEachIndexed { index, pessoa ->
                val y = (index - direita.size / 2f) * ESPACAMENTO_MINIMO_NOS
                val x = RAIO_CAMADA_3
                posicoes.add(PosicaoNo(pessoa, x, y, 4, TipoRelacao.OUTRO, ANGULO_TOP_DIREITA.toFloat()))
            }
        }
        
        Timber.d("✅ Mapa mental hierárquico organizado: ${posicoes.size} pessoas posicionadas sem sobreposições")
        
        return posicoes
    }
    
    /**
     * Encontra o casal da Família Zero
     * Se não houver Família Zero marcada, retorna as primeiras pessoas disponíveis
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
            
            val pai = familiaZero.firstOrNull { it.pai == null }
            val mae = familiaZero.firstOrNull { it.mae == null }
            
            if (pai != null && mae != null) {
                Timber.d("✅ Casal Família Zero encontrado por pais: ${pai.nome} e ${mae.nome}")
                return Pair(pai, mae)
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

