package com.raizesvivas.app.utils

import com.raizesvivas.app.domain.model.ParentescoTipo
import com.raizesvivas.app.domain.model.Pessoa
import org.junit.Assert.*
import org.junit.Test
import java.util.Date
import java.util.UUID

/**
 * Testes para ParentescoCalculator
 */
class ParentescoCalculatorTest {
    
    @Test
    fun `calcular parentesco - mesma pessoa deve retornar EU`() {
        val pessoa = criarPessoa("João", "pessoa1")
        val pessoasMap = mapOf("pessoa1" to pessoa)
        
        val parentesco = ParentescoCalculator.calcularParentesco(
            pessoa1 = pessoa,
            pessoa2 = pessoa,
            pessoasMap = pessoasMap
        )
        
        assertEquals(ParentescoTipo.EU, parentesco)
    }
    
    @Test
    fun `calcular parentesco - pai deve retornar PAI`() {
        val pai = criarPessoa("João", "pai")
        val filho = criarPessoa("Pedro", "filho", paiId = "pai")
        val pessoasMap = mapOf("pai" to pai, "filho" to filho)
        
        val parentesco = ParentescoCalculator.calcularParentesco(
            pessoa1 = filho,
            pessoa2 = pai,
            pessoasMap = pessoasMap
        )
        
        assertEquals(ParentescoTipo.PAI, parentesco)
    }
    
    @Test
    fun `calcular parentesco - mãe deve retornar MAE`() {
        val mae = criarPessoa("Maria", "mae")
        val filho = criarPessoa("Pedro", "filho", maeId = "mae")
        val pessoasMap = mapOf("mae" to mae, "filho" to filho)
        
        val parentesco = ParentescoCalculator.calcularParentesco(
            pessoa1 = filho,
            pessoa2 = mae,
            pessoasMap = pessoasMap
        )
        
        assertEquals(ParentescoTipo.MAE, parentesco)
    }
    
    @Test
    fun `calcular parentesco - cônjuge deve retornar CONJUGE`() {
        val pessoa1 = criarPessoa("João", "pessoa1")
        val pessoa2 = criarPessoa("Maria", "pessoa2", conjugeId = "pessoa1")
        val pessoa1ComConjuge = pessoa1.copy(conjugeAtual = "pessoa2")
        val pessoasMap = mapOf("pessoa1" to pessoa1ComConjuge, "pessoa2" to pessoa2)
        
        val parentesco = ParentescoCalculator.calcularParentesco(
            pessoa1 = pessoa1ComConjuge,
            pessoa2 = pessoa2,
            pessoasMap = pessoasMap
        )
        
        assertEquals(ParentescoTipo.CONJUGE, parentesco)
    }
    
    @Test
    fun `calcular parentesco - irmãos devem retornar IRMAO`() {
        val pai = criarPessoa("João", "pai")
        val mae = criarPessoa("Maria", "mae")
        val irmao1 = criarPessoa("Pedro", "irmao1", paiId = "pai", maeId = "mae")
        val irmao2 = criarPessoa("Paulo", "irmao2", paiId = "pai", maeId = "mae")
        
        val pessoasMap = mapOf(
            "pai" to pai,
            "mae" to mae,
            "irmao1" to irmao1,
            "irmao2" to irmao2
        )
        
        val parentesco = ParentescoCalculator.calcularParentesco(
            pessoa1 = irmao1,
            pessoa2 = irmao2,
            pessoasMap = pessoasMap
        )
        
        assertEquals(ParentescoTipo.IRMAO, parentesco)
    }
    
    // Função auxiliar para criar pessoas de teste
    private fun criarPessoa(
        nome: String,
        id: String,
        paiId: String? = null,
        maeId: String? = null,
        conjugeId: String? = null
    ): Pessoa {
        return Pessoa(
            id = id,
            nome = nome,
            pai = paiId,
            mae = maeId,
            conjugeAtual = conjugeId,
            criadoEm = Date(),
            modificadoEm = Date()
        )
    }
}

