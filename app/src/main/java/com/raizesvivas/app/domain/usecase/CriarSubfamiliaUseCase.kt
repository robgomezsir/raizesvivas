package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.SubfamiliaRepository
import com.raizesvivas.app.domain.model.*
import com.raizesvivas.app.domain.usecase.VerificarConquistasUseCase
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * UseCase para criar subfamília a partir de sugestão aceita
 * 
 * Cria a subfamília e registra todos os membros com seus papéis corretos
 */
class CriarSubfamiliaUseCase @Inject constructor(
    private val pessoaRepository: PessoaRepository,
    private val subfamiliaRepository: SubfamiliaRepository,
    private val verificarConquistasUseCase: VerificarConquistasUseCase
) {
    
    /**
     * Cria subfamília a partir de sugestão aceita
     * 
     * @param sugestao Sugestão aceita
     * @param nomePersonalizado Nome personalizado (opcional, usa nomeSugerido se null)
     * @param usuarioId ID do usuário que está criando
     * @return Subfamília criada
     */
    suspend fun executar(
        sugestao: SugestaoSubfamilia,
        nomePersonalizado: String? = null,
        usuarioId: String
    ): Result<Subfamilia> {
        return try {
            Timber.d("🌳 Criando subfamília a partir de sugestão: ${sugestao.id}")
            
            // Buscar pessoas fundadoras
            val membro1 = pessoaRepository.buscarPorId(sugestao.membro1Id)
            val membro2 = pessoaRepository.buscarPorId(sugestao.membro2Id)
            
            if (membro1 == null || membro2 == null) {
                Timber.e("❌ Um ou ambos os membros fundadores não foram encontrados")
                return Result.failure(
                    IllegalArgumentException("Membros fundadores não encontrados")
                )
            }
            
            // Determinar família pai (buscar nível hierárquico)
            val nivelHierarquico = calcularNivelHierarquico(membro1, membro2)
            
            // Criar subfamília
            val subfamilia = Subfamilia(
                id = UUID.randomUUID().toString(),
                nome = nomePersonalizado ?: sugestao.nomeSugerido,
                tipo = TipoFamilia.SUBFAMILIA,
                familiaPaiId = sugestao.familiaZeroId, // Por enquanto, sempre filha da Família Zero
                membroOrigem1Id = membro1.id,
                membroOrigem2Id = membro2.id,
                nivelHierarquico = nivelHierarquico,
                criadoEm = Date(),
                criadoPor = usuarioId,
                ativa = true
            )
            
            // Salvar subfamília
            val resultadoSalvar = subfamiliaRepository.salvar(subfamilia)
            
            resultadoSalvar.onSuccess {
                // Criar registros de membros da família
                criarRegistrosDeMembros(subfamilia, sugestao.membrosIncluidos)
                
                // Marcar sugestão como aceita
                subfamiliaRepository.atualizarStatusSugestao(
                    sugestao.id,
                    StatusSugestao.ACEITA
                )
                
                // Verificar conquistas relacionadas (criar subfamília)
                verificarConquistasUseCase.verificarTodasConquistas(usuarioId)
                
                Timber.d("✅ Subfamília criada com sucesso: ${subfamilia.id}")
            }
            
            resultadoSalvar.map { subfamilia }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao criar subfamília")
            Result.failure(e)
        }
    }
    
    /**
     * Calcula o nível hierárquico da subfamília
     */
    private suspend fun calcularNivelHierarquico(
        @Suppress("UNUSED_PARAMETER") membro1: Pessoa,
        @Suppress("UNUSED_PARAMETER") membro2: Pessoa
    ): Int {
        // Para simplificar, sempre começar no nível 1
        // Em versões futuras, pode calcular baseado na distância da Família Zero
        return 1
    }
    
    /**
     * Cria registros de membros da família com papéis corretos
     */
    private suspend fun criarRegistrosDeMembros(
        subfamilia: Subfamilia,
        membrosIds: List<String>
    ) {
        val todasPessoas = pessoaRepository.buscarTodas()
        val pessoasMap = todasPessoas.associateBy { it.id }
        
        membrosIds.forEach { membroId ->
            val pessoa = pessoasMap[membroId] ?: return@forEach
            
            // Determinar papel na família
            val papel = determinarPapelNaFamilia(pessoa, subfamilia, pessoasMap)
            
            // Determinar elemento da árvore
            val elemento = determinarElementoArvore(pessoa, subfamilia, pessoasMap)
            
            // Determinar geração na família
            val geracao = determinarGeracaoNaFamilia(pessoa, subfamilia, pessoasMap)
            
            // Criar registro
            val membroFamilia = MembroFamilia(
                id = "${membroId}_${subfamilia.id}",
                membroId = membroId,
                familiaId = subfamilia.id,
                papelNaFamilia = papel,
                elementoNestaFamilia = elemento,
                geracaoNaFamilia = geracao
            )
            
            // Salvar
            subfamiliaRepository.adicionarMembroAFamilia(membroFamilia)
        }
    }
    
    /**
     * Determina o papel de uma pessoa na família
     */
    private fun determinarPapelNaFamilia(
        pessoa: Pessoa,
        subfamilia: Subfamilia,
        pessoasMap: Map<String, Pessoa>
    ): PapelFamilia {
        // Fundadores
        if (pessoa.id == subfamilia.membroOrigem1Id || pessoa.id == subfamilia.membroOrigem2Id) {
            // Determinar se é pai ou mãe baseado no gênero (simplificado)
            // Por padrão, primeiro membro = pai, segundo = mãe
            return if (pessoa.id == subfamilia.membroOrigem1Id) {
                PapelFamilia.PAI
            } else {
                PapelFamilia.MAE
            }
        }
        
        // Filhos dos fundadores
        val membro1 = pessoasMap[subfamilia.membroOrigem1Id]
        val membro2 = pessoasMap[subfamilia.membroOrigem2Id]
        
        if (membro1 != null && membro2 != null) {
            if (pessoa.pai == membro1.id || pessoa.pai == membro2.id ||
                pessoa.mae == membro1.id || pessoa.mae == membro2.id) {
                // É filho dos fundadores
                return if (pessoa.nome.contains("a", ignoreCase = true)) {
                    PapelFamilia.FILHA
                } else {
                    PapelFamilia.FILHO
                }
            }
            
            // Avós (pais dos fundadores)
            if (pessoa.id == membro1.pai || pessoa.id == membro1.mae ||
                pessoa.id == membro2.pai || pessoa.id == membro2.mae) {
                // Simplificado: sempre AVO_PATERNO ou AVO_PATERNA
                return if (pessoa.nome.contains("a", ignoreCase = true)) {
                    PapelFamilia.AVO_PATERNA
                } else {
                    PapelFamilia.AVO_PATERNO
                }
            }
        }
        
        return PapelFamilia.OUTRO
    }
    
    /**
     * Determina o elemento da árvore para uma pessoa na família
     */
    private fun determinarElementoArvore(
        pessoa: Pessoa,
        subfamilia: Subfamilia,
        pessoasMap: Map<String, Pessoa>
    ): ElementoArvore {
        // Fundadores = Caule
        if (pessoa.id == subfamilia.membroOrigem1Id || pessoa.id == subfamilia.membroOrigem2Id) {
            return ElementoArvore.CAULE
        }
        
        // Filhos = Galhos
        val membro1 = pessoasMap[subfamilia.membroOrigem1Id]
        val membro2 = pessoasMap[subfamilia.membroOrigem2Id]
        
        if (membro1 != null && membro2 != null) {
            if (pessoa.pai == membro1.id || pessoa.pai == membro2.id ||
                pessoa.mae == membro1.id || pessoa.mae == membro2.id) {
                return ElementoArvore.GALHO
            }
            
            // Avós = Casca
            if (pessoa.id == membro1.pai || pessoa.id == membro1.mae ||
                pessoa.id == membro2.pai || pessoa.id == membro2.mae) {
                return ElementoArvore.CASCA
            }
        }
        
        return ElementoArvore.OUTRO
    }
    
    /**
     * Determina a geração na família (0 = fundadores)
     */
    private fun determinarGeracaoNaFamilia(
        pessoa: Pessoa,
        subfamilia: Subfamilia,
        pessoasMap: Map<String, Pessoa>
    ): Int {
        // Fundadores = geração 0
        if (pessoa.id == subfamilia.membroOrigem1Id || pessoa.id == subfamilia.membroOrigem2Id) {
            return 0
        }
        
        // Filhos = geração 1
        val membro1 = pessoasMap[subfamilia.membroOrigem1Id]
        val membro2 = pessoasMap[subfamilia.membroOrigem2Id]
        
        if (membro1 != null && membro2 != null) {
            if (pessoa.pai == membro1.id || pessoa.pai == membro2.id ||
                pessoa.mae == membro1.id || pessoa.mae == membro2.id) {
                return 1
            }
            
            // Avós = geração -1
            if (pessoa.id == membro1.pai || pessoa.id == membro1.mae ||
                pessoa.id == membro2.pai || pessoa.id == membro2.mae) {
                return -1
            }
        }
        
        return 0
    }
}
