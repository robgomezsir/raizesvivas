package com.raizesvivas.app.presentation.screens.cadastro

import com.raizesvivas.app.data.remote.firebase.AuthService
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.data.repository.UsuarioRepository
import com.raizesvivas.app.domain.model.Pessoa
import com.raizesvivas.app.domain.model.ValidationResult
import com.raizesvivas.app.utils.NetworkUtils
import com.raizesvivas.app.utils.ValidationUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

/**
 * Testes unitários para validações e utilitários relacionados ao CadastroPessoaViewModel
 * 
 * Nota: Testes completos do ViewModel requerem configuração adicional de Hilt e corrotinas
 */
class CadastroPessoaViewModelTest {
    
    @Test
    fun `validar nome - deve rejeitar nome vazio`() {
        val result = ValidationUtils.validarNome("")
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }
    
    @Test
    fun `validar nome - deve rejeitar nome com menos de 3 caracteres`() {
        val result = ValidationUtils.validarNome("AB")
        assertFalse(result.isValid)
    }
    
    @Test
    fun `validar nome - deve aceitar nome válido`() {
        val result = ValidationUtils.validarNome("João Silva")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun `Pessoa validar - deve validar nome obrigatório`() {
        val pessoa = Pessoa(nome = "")
        val result = pessoa.validar()
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Nome é obrigatório"))
    }
    
    @Test
    fun `Pessoa validar - deve validar nome mínimo 3 caracteres`() {
        val pessoa = Pessoa(nome = "AB")
        val result = pessoa.validar()
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Nome deve ter pelo menos 3 caracteres"))
    }
    
    @Test
    fun `Pessoa validar - deve validar datas inconsistentes`() {
        val dataNasc = Date(System.currentTimeMillis() - 86400000) // Ontem
        val dataFalec = Date(System.currentTimeMillis() - 172800000) // 2 dias atrás
        
        val pessoa = Pessoa(
            nome = "João",
            dataNascimento = dataNasc,
            dataFalecimento = dataFalec // Falecimento antes do nascimento
        )
        
        val result = pessoa.validar()
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("falecimento") })
    }
    
    @Test
    fun `Pessoa validar - deve aceitar pessoa válida`() {
        val pessoa = Pessoa(
            nome = "João Silva",
            dataNascimento = Date(System.currentTimeMillis() - 94608000000) // 30 anos atrás
        )
        
        val result = pessoa.validar()
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
}

