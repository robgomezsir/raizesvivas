package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.utils.DuplicateDetector
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case para detectar pessoas duplicadas
 * 
 * Usa algoritmo de similaridade para identificar possíveis duplicatas
 */
class DetectarDuplicatasUseCase @Inject constructor(
    private val pessoaRepository: PessoaRepository
) {
    
    /**
     * Detecta duplicatas para uma pessoa recém-cadastrada/editada
     * 
     * @param pessoa Pessoa a ser verificada
     * @param threshold Score mínimo para considerar duplicata (padrão: 0.8)
     * @return Lista de resultados de duplicatas encontradas
     */
    suspend fun executar(
        pessoa: Pessoa,
        threshold: Float = 0.8f
    ): List<DuplicateDetector.DuplicataResultado> {
        return try {
            // Buscar todas as pessoas (exceto a atual)
            val todasPessoas = pessoaRepository.buscarTodas()
                .filter { it.id != pessoa.id }
            
            // Detectar duplicatas
            val duplicatas = DuplicateDetector.detectarDuplicatas(
                pessoa = pessoa,
                outrasPessoas = todasPessoas,
                threshold = threshold
            )
            
            if (duplicatas.isNotEmpty()) {
                Timber.w("⚠️ ${duplicatas.size} possível(is) duplicata(s) encontrada(s) para ${pessoa.nome}")
            }
            
            duplicatas
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao detectar duplicatas")
            emptyList()
        }
    }
    
    /**
     * Detecta todas as duplicatas no sistema
     * 
     * @param threshold Score mínimo para considerar duplicata (padrão: 0.8)
     * @return Lista de pares de pessoas duplicadas
     */
    suspend fun detectarTodasDuplicatas(
        threshold: Float = 0.8f
    ): List<DuplicateDetector.DuplicataResultado> {
        return try {
            val todasPessoas = pessoaRepository.buscarTodas()
            val duplicatasEncontradas = mutableListOf<DuplicateDetector.DuplicataResultado>()
            val processadas = mutableSetOf<Pair<String, String>>()
            
            todasPessoas.forEach { pessoa ->
                val duplicatas = DuplicateDetector.detectarDuplicatas(
                    pessoa = pessoa,
                    outrasPessoas = todasPessoas,
                    threshold = threshold
                )
                
                duplicatas.forEach { resultado ->
                    // Evitar duplicatas inversas (A-B e B-A)
                    val par1 = Pair(pessoa.id, resultado.pessoa2.id)
                    val par2 = Pair(resultado.pessoa2.id, pessoa.id)
                    
                    if (par1 !in processadas && par2 !in processadas) {
                        duplicatasEncontradas.add(resultado)
                        processadas.add(par1)
                        processadas.add(par2)
                    }
                }
            }
            
            Timber.d("✅ ${duplicatasEncontradas.size} pares de duplicatas encontrados")
            duplicatasEncontradas
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao detectar todas as duplicatas")
            emptyList()
        }
    }
}

