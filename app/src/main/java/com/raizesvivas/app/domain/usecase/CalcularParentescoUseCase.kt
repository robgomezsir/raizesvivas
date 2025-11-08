package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.ParentescoTipo
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.utils.ParentescoCalculator
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase para calcular o parentesco entre duas pessoas
 * 
 * Coordena a busca de pessoas e o cálculo de parentesco
 */
class CalcularParentescoUseCase @Inject constructor(
    private val pessoaRepository: PessoaRepository
) {
    /**
     * Calcula o parentesco entre duas pessoas por ID
     * 
     * @param pessoa1Id ID da primeira pessoa
     * @param pessoa2Id ID da segunda pessoa
     * @return O tipo de parentesco encontrado
     */
    suspend fun executar(
        pessoa1Id: String,
        pessoa2Id: String
    ): ParentescoTipo {
        return try {
            val pessoa1 = pessoaRepository.buscarPorId(pessoa1Id)
            val pessoa2 = pessoaRepository.buscarPorId(pessoa2Id)
            
            if (pessoa1 == null || pessoa2 == null) {
                Timber.w("Uma das pessoas não foi encontrada: $pessoa1Id, $pessoa2Id")
                return ParentescoTipo.DESCONHECIDO
            }
            
            // Buscar todas as pessoas para o mapa de navegação
            val todasPessoas = pessoaRepository.buscarTodas()
            val pessoasMap = todasPessoas.associateBy { it.id }
            
            val resultado = ParentescoCalculator.calcularParentesco(pessoa1, pessoa2, pessoasMap)
            converterParaParentescoTipo(resultado)
            
        } catch (e: Exception) {
            Timber.e(e, "Erro ao calcular parentesco entre $pessoa1Id e $pessoa2Id")
            ParentescoTipo.DESCONHECIDO
        }
    }
    
    /**
     * Calcula o parentesco entre duas pessoas (objetos)
     * 
     * @param pessoa1 Primeira pessoa
     * @param pessoa2 Segunda pessoa
     * @param todasPessoas Lista de todas as pessoas para navegação
     * @return O tipo de parentesco encontrado
     */
    fun executar(
        pessoa1: Pessoa,
        pessoa2: Pessoa,
        todasPessoas: List<Pessoa>
    ): ParentescoTipo {
        return try {
            val pessoasMap = todasPessoas.associateBy { it.id }
            val resultado = ParentescoCalculator.calcularParentesco(pessoa1, pessoa2, pessoasMap)
            converterParaParentescoTipo(resultado)
        } catch (e: Exception) {
            Timber.e(e, "Erro ao calcular parentesco")
            ParentescoTipo.DESCONHECIDO
        }
    }
    
    /**
     * Converte ResultadoParentesco para ParentescoTipo
     */
    private fun converterParaParentescoTipo(resultado: ParentescoCalculator.ResultadoParentesco): ParentescoTipo {
        return when {
            resultado.grau < 0 -> ParentescoTipo.DESCONHECIDO
            resultado.grau == 0 -> ParentescoTipo.EU
            resultado.parentesco.contains("Pai", ignoreCase = true) && !resultado.parentesco.contains("Mãe", ignoreCase = true) && !resultado.parentesco.contains("Pai/Mãe", ignoreCase = true) -> ParentescoTipo.PAI
            resultado.parentesco.contains("Mãe", ignoreCase = true) && !resultado.parentesco.contains("Pai/Mãe", ignoreCase = true) -> ParentescoTipo.MAE
            resultado.parentesco.contains("Filho", ignoreCase = true) -> ParentescoTipo.FILHO
            resultado.parentesco.contains("Filha", ignoreCase = true) -> ParentescoTipo.FILHA
            resultado.parentesco.contains("Irmão", ignoreCase = true) && !resultado.parentesco.contains("Meio", ignoreCase = true) -> ParentescoTipo.IRMAO
            resultado.parentesco.contains("Irmã", ignoreCase = true) && !resultado.parentesco.contains("Meia", ignoreCase = true) -> ParentescoTipo.IRMA
            resultado.parentesco.contains("Avô", ignoreCase = true) || resultado.parentesco.contains("Avó", ignoreCase = true) -> ParentescoTipo.AVO_PATERNO
            resultado.parentesco.contains("Neto", ignoreCase = true) -> ParentescoTipo.NETO
            resultado.parentesco.contains("Neta", ignoreCase = true) -> ParentescoTipo.NETA
            resultado.parentesco.contains("Tio", ignoreCase = true) && !resultado.parentesco.contains("Tia", ignoreCase = true) -> ParentescoTipo.TIO_PATERNO
            resultado.parentesco.contains("Tia", ignoreCase = true) -> ParentescoTipo.TIA_PATERNA
            resultado.parentesco.contains("Sobrinho", ignoreCase = true) -> ParentescoTipo.SOBRINHO
            resultado.parentesco.contains("Sobrinha", ignoreCase = true) -> ParentescoTipo.SOBRINHA
            resultado.parentesco.contains("Primo", ignoreCase = true) && !resultado.parentesco.contains("Prima", ignoreCase = true) -> ParentescoTipo.PRIMO
            resultado.parentesco.contains("Prima", ignoreCase = true) -> ParentescoTipo.PRIMA
            resultado.grau <= 4 -> ParentescoTipo.PARENTE_DISTANTE
            else -> ParentescoTipo.DESCONHECIDO
        }
    }
}

