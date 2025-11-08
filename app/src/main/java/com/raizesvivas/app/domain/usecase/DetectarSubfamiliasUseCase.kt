package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.SubfamiliaRepository
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.SugestaoSubfamilia
import com.raizesvivas.app.domain.model.StatusSugestao
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * UseCase para detectar automaticamente casais que podem formar subfamílias
 * 
 * Executa a lógica de detecção:
 * 1. Buscar todos os casamentos confirmados
 * 2. Verificar se já existe subfamília para esse casal
 * 3. Buscar filhos do casal
 * 4. Buscar pais dos fundadores (avós)
 * 5. Gerar nome sugerido
 * 6. Criar registro em sugestoes_subfamilias
 */
class DetectarSubfamiliasUseCase @Inject constructor(
    private val pessoaRepository: PessoaRepository,
    private val subfamiliaRepository: SubfamiliaRepository
) {
    
    /**
     * Detecta e cria sugestões de subfamílias para todos os casais elegíveis
     * 
     * @param usuarioId ID do usuário que receberá as sugestões
     * @param familiaZeroId ID da Família Zero
     * @return Lista de sugestões criadas
     */
    suspend fun executar(usuarioId: String, familiaZeroId: String): List<SugestaoSubfamilia> {
        return try {
            Timber.d("🔍 Iniciando detecção de subfamílias...")
            
            val todasPessoas = pessoaRepository.buscarTodas()
            val pessoasMap = todasPessoas.associateBy { it.id }
            
            // Buscar todos os casamentos confirmados (ambos têm conjugeAtual apontando um para o outro)
            val casaisConfirmados = encontrarCasaisConfirmados(todasPessoas)
            Timber.d("💑 Encontrados ${casaisConfirmados.size} casais confirmados")
            
            // Buscar subfamílias existentes (primeiro valor do Flow)
            val subfamiliasExistentes = try {
                subfamiliaRepository.observarTodasSubfamilias().first()
            } catch (e: Exception) {
                Timber.w(e, "⚠️ Erro ao buscar subfamílias existentes")
                emptyList()
            }
            
            val sugestoesCriadas = mutableListOf<SugestaoSubfamilia>()
            
            casaisConfirmados.forEach { (pessoa1, pessoa2) ->
                // Verificar se já existe subfamília para esse casal
                val jaExiste = subfamiliasExistentes.any { subfamilia ->
                    (subfamilia.membroOrigem1Id == pessoa1.id && subfamilia.membroOrigem2Id == pessoa2.id) ||
                    (subfamilia.membroOrigem1Id == pessoa2.id && subfamilia.membroOrigem2Id == pessoa1.id)
                }
                
                if (!jaExiste) {
                    // Buscar membros que seriam incluídos na subfamília
                    val membrosIncluidos = buscarMembrosParaSubfamilia(pessoa1, pessoa2, pessoasMap)
                    
                    // Gerar nome sugerido
                    val nomeSugerido = gerarNomeSugerido(pessoa1, pessoa2)
                    
                    // Criar sugestão
                    val sugestao = SugestaoSubfamilia(
                        id = UUID.randomUUID().toString(),
                        membro1Id = pessoa1.id,
                        membro2Id = pessoa2.id,
                        nomeSugerido = nomeSugerido,
                        membrosIncluidos = membrosIncluidos,
                        status = StatusSugestao.PENDENTE,
                        criadoEm = Date(),
                        usuarioId = usuarioId,
                        familiaZeroId = familiaZeroId
                    )
                    
                    // Salvar sugestão
                    val resultado = subfamiliaRepository.salvarSugestao(sugestao)
                    resultado.onSuccess {
                        sugestoesCriadas.add(sugestao)
                        Timber.d("✅ Sugestão criada: $nomeSugerido")
                    }.onFailure { erro ->
                        Timber.e(erro, "❌ Erro ao salvar sugestão")
                    }
                }
            }
            
            Timber.d("✅ Detecção concluída: ${sugestoesCriadas.size} sugestões criadas")
            sugestoesCriadas
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao detectar subfamílias")
            emptyList()
        }
    }
    
    /**
     * Encontra todos os casais confirmados (bidirecionais)
     */
    private fun encontrarCasaisConfirmados(pessoas: List<Pessoa>): List<Pair<Pessoa, Pessoa>> {
        val casais = mutableListOf<Pair<Pessoa, Pessoa>>()
        val processados = mutableSetOf<String>()
        
        pessoas.forEach { pessoa1 ->
            val conjugeId = pessoa1.conjugeAtual
            if (conjugeId != null && !processados.contains(pessoa1.id)) {
                val pessoa2 = pessoas.find { it.id == conjugeId }
                if (pessoa2 != null && pessoa2.conjugeAtual == pessoa1.id) {
                    // Casal confirmado (bidirecional)
                    casais.add(Pair(pessoa1, pessoa2))
                    processados.add(pessoa1.id)
                    processados.add(pessoa2.id)
                }
            }
        }
        
        return casais
    }
    
    /**
     * Busca todos os membros que seriam incluídos na subfamília
     */
    private fun buscarMembrosParaSubfamilia(
        membro1: Pessoa,
        membro2: Pessoa,
        pessoasMap: Map<String, Pessoa>
    ): List<String> {
        val membrosIncluidos = mutableSetOf<String>()
        
        // Incluir os fundadores
        membrosIncluidos.add(membro1.id)
        membrosIncluidos.add(membro2.id)
        
        // Buscar filhos do casal (filhos que têm ambos como pais OU pelo menos um)
        val filhosIds = mutableSetOf<String>()
        membro1.filhos.forEach { filhosIds.add(it) }
        membro2.filhos.forEach { filhosIds.add(it) }
        
        // Também buscar filhos onde um dos membros é pai ou mãe
        pessoasMap.values.forEach { pessoa ->
            if (pessoa.pai == membro1.id || pessoa.mae == membro1.id ||
                pessoa.pai == membro2.id || pessoa.mae == membro2.id) {
                filhosIds.add(pessoa.id)
            }
        }
        
        membrosIncluidos.addAll(filhosIds)
        
        // Buscar pais dos fundadores (avós)
        membro1.pai?.let { membrosIncluidos.add(it) }
        membro1.mae?.let { membrosIncluidos.add(it) }
        membro2.pai?.let { membrosIncluidos.add(it) }
        membro2.mae?.let { membrosIncluidos.add(it) }
        
        return membrosIncluidos.toList()
    }
    
    /**
     * Gera nome sugerido para a subfamília
     */
    private fun gerarNomeSugerido(pessoa1: Pessoa, pessoa2: Pessoa): String {
        // Tentar extrair sobrenomes dos nomes
        val sobrenome1 = pessoa1.nome.split(" ").lastOrNull() ?: pessoa1.nome
        val sobrenome2 = pessoa2.nome.split(" ").lastOrNull() ?: pessoa2.nome
        
        return if (sobrenome1 != sobrenome2) {
            "Família $sobrenome1-$sobrenome2"
        } else {
            "Família $sobrenome1"
        }
    }
}
