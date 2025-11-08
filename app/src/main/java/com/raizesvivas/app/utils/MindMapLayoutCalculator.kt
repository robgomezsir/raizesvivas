package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.Pessoa
import kotlin.math.*
import timber.log.Timber

/**
 * Calculadora de layout estilo Mapa Mental para árvore genealógica
 * 
 * Implementa algoritmo de posicionamento radial/organico onde:
 * - Pessoa central fica no centro
 * - Relacionamentos são posicionados em círculos ao redor
 * - Layout mais intuitivo e fluido
 */
object MindMapLayoutCalculator {
    
    // Espaçamento entre camadas
    private const val RAIO_CAMADA = 180f // Raio de cada camada circular
    private const val NO_SIZE = 100f // Tamanho do nó
    
    /**
     * Posição de um nó no mapa mental
     */
    data class PosicaoNo(
        val pessoa: Pessoa,
        val x: Float,
        val y: Float,
        val camada: Int, // Camada radial (0 = centro)
        val tipoRelacao: TipoRelacao? = null // Tipo de relacionamento com o centro
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
        OUTRO
    }
    
    /**
     * Calcula posições usando layout radial estilo mapa mental
     * 
     * @param todasPessoas Todas as pessoas disponíveis
     * @param pessoaCentralId ID da pessoa central (null = Família Zero)
     * @param pessoasMap Mapa de pessoas por ID
     * @return Lista de posições calculadas
     */
    fun calcularPosicoesMapaMental(
        todasPessoas: List<Pessoa>,
        pessoaCentralId: String?,
        pessoasMap: Map<String, Pessoa>
    ): List<PosicaoNo> {
        if (todasPessoas.isEmpty()) {
            Timber.w("⚠️ Nenhuma pessoa disponível")
            return emptyList()
        }
        
        val posicoes = mutableListOf<PosicaoNo>()
        val visitados = mutableSetOf<String>()
        
        // Determinar pessoa central
        val pessoaCentral = when {
            pessoaCentralId != null -> pessoasMap[pessoaCentralId]
            else -> {
                // Se não há pessoa central, usar Família Zero ou primeira pessoa sem pais
                todasPessoas.firstOrNull { it.ehFamiliaZero } 
                    ?: todasPessoas.firstOrNull { it.pai == null && it.mae == null }
                    ?: todasPessoas.first()
            }
        }
        
        if (pessoaCentral == null) {
            Timber.w("⚠️ Não foi possível determinar pessoa central")
            return emptyList()
        }
        
        Timber.d("📍 Pessoa central: ${pessoaCentral.nome} (ID: ${pessoaCentral.id})")
        
        // Posicionar pessoa central no centro
        posicoes.add(PosicaoNo(pessoaCentral, 0f, 0f, 0, null))
        visitados.add(pessoaCentral.id)
        
        // Camada 1: Relacionamentos diretos (pais, cônjuge)
        val camada1 = mutableListOf<Pessoa>()
        
        // Pai
        pessoaCentral.pai?.let { paiId ->
            pessoasMap[paiId]?.let { pai ->
                if (pai.id !in visitados) {
                    camada1.add(pai)
                    visitados.add(pai.id)
                }
            }
        }
        
        // Mãe
        pessoaCentral.mae?.let { maeId ->
            pessoasMap[maeId]?.let { mae ->
                if (mae.id !in visitados) {
                    camada1.add(mae)
                    visitados.add(mae.id)
                }
            }
        }
        
        // Cônjuge
        pessoaCentral.conjugeAtual?.let { conjugeId ->
            pessoasMap[conjugeId]?.let { conjuge ->
                if (conjuge.id !in visitados) {
                    camada1.add(conjuge)
                    visitados.add(conjuge.id)
                }
            }
        }
        
        // Posicionar camada 1 (pais e cônjuge) - parte superior
        if (camada1.isNotEmpty()) {
            val anguloInicial = -PI / 2 // Topo (-90 graus)
            val anguloPorPessoa = (2 * PI) / camada1.size
            
            camada1.forEachIndexed { index, pessoa ->
                val angulo = anguloInicial + (index * anguloPorPessoa)
                val x = sin(angulo).toFloat() * RAIO_CAMADA
                val y = -cos(angulo).toFloat() * RAIO_CAMADA
                
                val tipoRelacao = when {
                    pessoa.id == pessoaCentral.pai -> TipoRelacao.PAI
                    pessoa.id == pessoaCentral.mae -> TipoRelacao.MAE
                    pessoa.id == pessoaCentral.conjugeAtual -> TipoRelacao.CONJUGE
                    else -> TipoRelacao.OUTRO
                }
                
                posicoes.add(PosicaoNo(pessoa, x, y, 1, tipoRelacao))
            }
        }
        
        // Camada 2: Filhos e irmãos
        val camada2 = mutableListOf<Pessoa>()
        
        // Filhos
        pessoaCentral.filhos.forEach { filhoId ->
            pessoasMap[filhoId]?.let { filho ->
                if (filho.id !in visitados) {
                    camada2.add(filho)
                    visitados.add(filho.id)
                }
            }
        }
        
        // Irmãos (mesmos pais)
        val irmaos = todasPessoas.filter { irmao ->
            irmao.id != pessoaCentral.id &&
            irmao.id !in visitados &&
            ((irmao.pai == pessoaCentral.pai && irmao.pai != null) ||
             (irmao.mae == pessoaCentral.mae && irmao.mae != null))
        }
        
        camada2.addAll(irmaos)
        irmaos.forEach { visitados.add(it.id) }
        
        // Posicionar camada 2 (filhos e irmãos) - parte inferior
        if (camada2.isNotEmpty()) {
            val anguloInicial = PI / 2 // Parte inferior (90 graus)
            val anguloPorPessoa = (2 * PI) / camada2.size
            
            camada2.forEachIndexed { index, pessoa ->
                val angulo = anguloInicial + (index * anguloPorPessoa)
                val x = sin(angulo).toFloat() * RAIO_CAMADA * 2
                val y = -cos(angulo).toFloat() * RAIO_CAMADA * 2
                
                val tipoRelacao = when {
                    pessoa.id in pessoaCentral.filhos -> TipoRelacao.FILHO
                    else -> TipoRelacao.IRMAO
                }
                
                posicoes.add(PosicaoNo(pessoa, x, y, 2, tipoRelacao))
            }
        }
        
        // Camada 3: Netos e outros relacionamentos
        val camada3 = mutableListOf<Pessoa>()
        
        // Netos (filhos dos filhos)
        pessoaCentral.filhos.forEach { filhoId ->
            pessoasMap[filhoId]?.let { filho ->
                filho.filhos.forEach { netoId ->
                    pessoasMap[netoId]?.let { neto ->
                        if (neto.id !in visitados) {
                            camada3.add(neto)
                            visitados.add(neto.id)
                        }
                    }
                }
            }
        }
        
        // Avós (pais dos pais)
        pessoaCentral.pai?.let { paiId ->
            pessoasMap[paiId]?.let { pai ->
                pai.pai?.let { avoPaiId ->
                    pessoasMap[avoPaiId]?.let { avo ->
                        if (avo.id !in visitados) {
                            camada3.add(avo)
                            visitados.add(avo.id)
                        }
                    }
                }
                pai.mae?.let { avoPaiMaeId ->
                    pessoasMap[avoPaiMaeId]?.let { avo ->
                        if (avo.id !in visitados) {
                            camada3.add(avo)
                            visitados.add(avo.id)
                        }
                    }
                }
            }
        }
        
        pessoaCentral.mae?.let { maeId ->
            pessoasMap[maeId]?.let { mae ->
                mae.pai?.let { avoMaePaiId ->
                    pessoasMap[avoMaePaiId]?.let { avo ->
                        if (avo.id !in visitados) {
                            camada3.add(avo)
                            visitados.add(avo.id)
                        }
                    }
                }
                mae.mae?.let { avoMaeMaeId ->
                    pessoasMap[avoMaeMaeId]?.let { avo ->
                        if (avo.id !in visitados) {
                            camada3.add(avo)
                            visitados.add(avo.id)
                        }
                    }
                }
            }
        }
        
        // Posicionar camada 3
        if (camada3.isNotEmpty()) {
            val anguloPorPessoa = (2 * PI) / camada3.size
            
            camada3.forEachIndexed { index, pessoa ->
                val angulo = index * anguloPorPessoa
                val x = sin(angulo).toFloat() * RAIO_CAMADA * 3
                val y = -cos(angulo).toFloat() * RAIO_CAMADA * 3
                
                posicoes.add(PosicaoNo(pessoa, x, y, 3, TipoRelacao.OUTRO))
            }
        }
        
        // Adicionar todas as outras pessoas não conectadas diretamente (camada externa)
        val outrasPessoas = todasPessoas.filter { it.id !in visitados }
        
        if (outrasPessoas.isNotEmpty()) {
            val anguloPorPessoa = (2 * PI) / outrasPessoas.size
            
            outrasPessoas.forEachIndexed { index, pessoa ->
                val angulo = index * anguloPorPessoa
                val x = sin(angulo).toFloat() * RAIO_CAMADA * 4
                val y = -cos(angulo).toFloat() * RAIO_CAMADA * 4
                
                posicoes.add(PosicaoNo(pessoa, x, y, 4, TipoRelacao.OUTRO))
            }
        }
        
        // Centralizar tudo (normalizar coordenadas)
        val minX = posicoes.minOfOrNull { it.x } ?: 0f
        val minY = posicoes.minOfOrNull { it.y } ?: 0f
        val maxX = posicoes.maxOfOrNull { it.x } ?: 0f
        val maxY = posicoes.maxOfOrNull { it.y } ?: 0f
        
        val centroX = (minX + maxX) / 2f
        val centroY = (minY + maxY) / 2f
        
        Timber.d("✅ Mapa mental calculado: ${posicoes.size} pessoas posicionadas (central: ${pessoaCentral.nome})")
        
        return posicoes.map { pos ->
            pos.copy(
                x = pos.x - centroX,
                y = pos.y - centroY
            )
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
            
            // Verificar relacionamento de cônjuge
            if (pessoa1.conjugeAtual == pessoa2.id) {
                return Pair(pessoa1, pessoa2)
            } else if (pessoa2.conjugeAtual == pessoa1.id) {
                return Pair(pessoa2, pessoa1)
            }
            
            // Verificar pelo relacionamento pai/mãe
            val pai = familiaZero.firstOrNull { it.pai == null }
            val mae = familiaZero.firstOrNull { it.mae == null }
            
            if (pai != null && mae != null) {
                return Pair(pai, mae)
            }
        }
        
        return Pair(familiaZero.firstOrNull(), null)
    }
}
