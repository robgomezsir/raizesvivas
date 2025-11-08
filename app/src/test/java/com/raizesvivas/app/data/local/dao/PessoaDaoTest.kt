package com.raizesvivas.app.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raizesvivas.app.data.local.RaizesVivasDatabase
import com.raizesvivas.app.data.local.entities.PessoaEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Testes para o PessoaDao
 * 
 * Estes testes verificam se as operações do banco de dados
 * estão funcionando corretamente.
 */
@RunWith(AndroidJUnit4::class)
class PessoaDaoTest {
    
    private lateinit var database: RaizesVivasDatabase
    private lateinit var pessoaDao: PessoaDao
    
    /**
     * Configura banco de dados em memória antes de cada teste
     */
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RaizesVivasDatabase::class.java
        ).build()
        pessoaDao = database.pessoaDao()
    }
    
    /**
     * Fecha banco de dados após cada teste
     */
    @After
    fun tearDown() {
        database.close()
    }
    
    /**
     * Testa inserção e busca de pessoa
     */
    @Test
    fun inserirEBuscarPessoa() = runBlocking {
        // Arrange
        val pessoa = criarPessoaTeste(
            id = "pessoa1",
            nome = "João Silva"
        )
        
        // Act
        pessoaDao.inserir(pessoa)
        val resultado = pessoaDao.buscarPorId("pessoa1")
        
        // Assert
        assertNotNull(resultado)
        assertEquals("João Silva", resultado?.nome)
    }
    
    /**
     * Testa busca de filhos
     */
    @Test
    fun buscarFilhos() = runBlocking {
        // Arrange
        val pai = criarPessoaTeste("pai1", "Pai")
        val mae = criarPessoaTeste("mae1", "Mãe")
        val filho1 = criarPessoaTeste("filho1", "Filho 1", pai = "pai1", mae = "mae1")
        val filho2 = criarPessoaTeste("filho2", "Filho 2", pai = "pai1", mae = "mae1")
        
        pessoaDao.inserir(pai)
        pessoaDao.inserir(mae)
        pessoaDao.inserir(filho1)
        pessoaDao.inserir(filho2)
        
        // Act
        val filhos = pessoaDao.buscarFilhos("pai1")
        
        // Assert
        assertEquals(2, filhos.size)
        assertTrue(filhos.any { it.nome == "Filho 1" })
        assertTrue(filhos.any { it.nome == "Filho 2" })
    }
    
    /**
     * Testa busca de irmãos
     */
    @Test
    fun buscarIrmaos() = runBlocking {
        // Arrange
        val irmao1 = criarPessoaTeste("i1", "Irmão 1", pai = "pai", mae = "mae")
        val irmao2 = criarPessoaTeste("i2", "Irmão 2", pai = "pai", mae = "mae")
        val irmao3 = criarPessoaTeste("i3", "Irmão 3", pai = "pai", mae = "mae")
        
        pessoaDao.inserir(irmao1)
        pessoaDao.inserir(irmao2)
        pessoaDao.inserir(irmao3)
        
        // Act
        val irmaos = pessoaDao.buscarIrmaos("pai", "mae", "i1")
        
        // Assert
        assertEquals(2, irmaos.size)
        assertFalse(irmaos.any { it.id == "i1" }) // Não inclui a própria pessoa
    }
    
    /**
     * Testa busca por nome
     */
    @Test
    fun buscarPorNome() = runBlocking {
        // Arrange
        pessoaDao.inserir(criarPessoaTeste("1", "João Silva"))
        pessoaDao.inserir(criarPessoaTeste("2", "Maria Silva"))
        pessoaDao.inserir(criarPessoaTeste("3", "Pedro Santos"))
        
        // Act
        val resultado = pessoaDao.buscarPorNome("Silva")
        
        // Assert
        assertEquals(2, resultado.size)
    }
    
    /**
     * Testa contagem de pessoas
     */
    @Test
    fun contarPessoas() = runBlocking {
        // Arrange
        pessoaDao.inserir(criarPessoaTeste("1", "Pessoa 1"))
        pessoaDao.inserir(criarPessoaTeste("2", "Pessoa 2"))
        pessoaDao.inserir(criarPessoaTeste("3", "Pessoa 3"))
        
        // Act
        val total = pessoaDao.contarPessoas()
        
        // Assert
        assertEquals(3, total)
    }
    
    /**
     * Função auxiliar para criar pessoa de teste
     */
    private fun criarPessoaTeste(
        id: String,
        nome: String,
        pai: String? = null,
        mae: String? = null
    ) = PessoaEntity(
        id = id,
        nome = nome,
        dataNascimento = Date(),
        dataFalecimento = null,
        localNascimento = null,
        localResidencia = null,
        profissao = null,
        biografia = null,
        pai = pai,
        mae = mae,
        conjugeAtual = null,
        exConjuges = emptyList(),
        filhos = emptyList(),
        fotoUrl = null,
        criadoPor = "teste",
        criadoEm = Date(),
        modificadoPor = "teste",
        modificadoEm = Date(),
        aprovado = true,
        versao = 1,
        ehFamiliaZero = false,
        distanciaFamiliaZero = 0,
        precisaSincronizar = false
    )
}

